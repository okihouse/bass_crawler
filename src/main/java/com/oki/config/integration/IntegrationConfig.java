package com.oki.config.integration;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.transaction.Transactional;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;

import com.oki.bass.blog.domain.entity.Blog;
import com.oki.bass.blog.domain.repository.BassBlogRepository;
import com.oki.bass.content.domain.entity.Content;
import com.oki.bass.content.domain.repository.BassContentRepository;
import com.oki.bass.type.BassType;
import com.oki.config.locking.FilteredLockingManager;
import com.oki.config.locking.rss.RssLockingService;
import com.oki.config.log.BassLogManager;
import com.oki.config.meta.MetadataParser;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;

@Configuration
@EnableAutoConfiguration
public class IntegrationConfig {
	
	private static final Logger logger = LoggerFactory.getLogger(IntegrationConfig.class);
	
	@Autowired
	private RssUrlManager rssUrlManager;
	
	@Bean
	@InboundChannelAdapter(value = "feedChannel", poller = @Poller(maxMessagesPerPoll = "1", fixedRate = "1800000"))
	public RssUrlManager feedAdapter() throws MalformedURLException {
		return rssUrlManager;
	}
	
	@MessageEndpoint
	public static class Endpoint {
		
		@Autowired
		private MetadataParser imageMetadataParser;
		
		@Autowired
		private BassBlogRepository bassBlogRepository;
		
		@Autowired
		private BassContentRepository bassContentRepository;
		
		@ServiceActivator(inputChannel = "feedChannel")
		@Transactional 
		public void log(Message<List<SyndFeed>> message) throws IOException {
			
			FilteredLockingManager filteredLockingManager = (FilteredLockingManager) RssLockingService.getInstance();
			if (filteredLockingManager.isFilterEmpty()) {
				List<String> filters = bassBlogRepository.findAllFilters();
				if (filteredLockingManager.filterRegister(filters) == false) {
					logger.warn("filter registration is failed.");
				}
			}
			
			Date today = new Date();
			if (filteredLockingManager.isEmpty()) {
				List<String> contents = bassContentRepository.findByRegisterDateStartDateAfter(DateUtils.truncate(today, Calendar.DAY_OF_MONTH));
				filteredLockingManager.register(contents);
			}
			
//			Calendar calendar = Calendar.getInstance();
//			calendar.add(Calendar.DATE, -30);
//			Date customDay = calendar.getTime();
			
			List<SyndFeed> syndFeeds = message.getPayload();
			for (SyndFeed syndFeed : syndFeeds) {
				for (SyndEntry syndEntry : syndFeed.getEntries()) {
					Date publishedDate = syndEntry.getPublishedDate();
					
					String id = syndEntry.getAuthor();
					if (id == null || id.isEmpty()) {
						id = getId(syndEntry.getLink());
						if (id == null || id.isEmpty()) {
							logger.warn("Id is not existed. link={}", syndEntry.getLink());
							return;
						}
					}
					
					if (DateUtils.isSameDay(publishedDate, today) == false) continue;
//					if (customDay.getTime() > publishedDate.getTime()) continue;
					if (!filteredLockingManager.isAllow(syndEntry.getCategories().get(0).getName())) continue;
					if (filteredLockingManager.isRegistered(syndEntry.getTitle())) continue;
					
					String url = syndEntry.getLink();
					String img = imageMetadataParser.get(url, id, MetadataParser.META_TYPE.IMAGE);
					
					Blog blog = bassBlogRepository.findById(id);
					if (blog == null) {
						logger.warn("Blog is not existed. id={}, url={}", id, url);
						return;
					}
					
					Content content = new Content();
					content.setName(syndFeed.getTitle());
					content.setRegisterDate(publishedDate);
					content.setUrl(url);
					content.setTitle(syndEntry.getTitle());
					content.setCategory(BassType.CONTENT_CATEGORY.FISHING);
					content.setThumbnail(img);
					content.setBlog(blog);
					logger.info("rss item added. category={}, content={}", syndEntry.getCategories().get(0).getName(), content);
					
					bassContentRepository.save(content);
				}
			}
			
			logger.info("Blog crawling is done, date={}", today);
		}
		
		private String getId(String url){
			String id = null;
			try {
				Pattern pattern = Pattern.compile("http://(.*?).blog.me/");
				Matcher matcher = pattern.matcher(url);
				if (matcher.find() == true) {
					id = matcher.group(1);
				}
			} catch (Exception e) {
				logger.warn("pattern processing is failed. url={}, error={}", url, BassLogManager.makeLog(e.getMessage(), e));
			}
			return id;
		}
		
	}
	
}
