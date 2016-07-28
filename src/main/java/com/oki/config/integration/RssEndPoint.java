package com.oki.config.integration;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;

import com.oki.bass.content.domain.entity.Content;
import com.oki.bass.content.domain.repository.BassContentRepository;
import com.oki.bass.type.BassType;
import com.oki.config.locking.FilteredLockingManager;
import com.oki.config.locking.rss.RssLockingService;
import com.oki.config.meta.MetadataParser;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;

@MessageEndpoint
public class RssEndPoint {

	private static final Logger logger = LoggerFactory.getLogger(RssEndPoint.class);
	
	@Autowired
	private MetadataParser imageMetadataParser;
	
	@Autowired
	private BassContentRepository bassContentRepository;
	
	@ServiceActivator(inputChannel = "rssInbound")
	@Transactional 
	public void log(Message<List<SyndFeed>> message) throws IOException{
		
		FilteredLockingManager filteredLockingManager = (FilteredLockingManager) RssLockingService.getInstance();
		Date today = new Date();
		if (filteredLockingManager.isEmpty()) {
			List<String> contents = bassContentRepository.findByRegisterDateStartDateAfter(DateUtils.truncate(today, Calendar.DAY_OF_MONTH));
			filteredLockingManager.register(contents);
		}
		
		List<SyndFeed> syndFeeds = message.getPayload();
		for (SyndFeed syndFeed : syndFeeds) {
			for (SyndEntry syndEntry : syndFeed.getEntries()) {
				
				Date publishedDate = syndEntry.getPublishedDate();
				if (DateUtils.isSameDay(publishedDate, today) == false || !filteredLockingManager.isAllow(syndEntry.getCategories().get(0).getName())) continue; 
				//if (!duplicateLockingManager.isAllow(syndEntry.getCategories().get(0).getName())) continue;
				if (filteredLockingManager.isRegistered(syndEntry.getTitle())) continue;
				
				String url = syndEntry.getLink();
				String img = imageMetadataParser.get(url, MetadataParser.META_TYPE.IMAGE);
				
				Content content = new Content();
				content.setName(syndFeed.getTitle());
				content.setRegisterDate(publishedDate);
				content.setUrl(url);
				content.setTitle(syndEntry.getTitle());
				content.setCategory(BassType.CONTENT_CATEGORY.FISHING);
				content.setThumbnail(img);
				logger.info("rss item added. {}", content);
				bassContentRepository.save(content);
			}
		}
		
	}
	
}
