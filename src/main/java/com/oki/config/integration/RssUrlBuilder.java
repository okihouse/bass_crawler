package com.oki.config.integration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import com.oki.bass.blog.domain.entity.Blog;
import com.oki.bass.blog.domain.repository.BassBlogRepository;
import com.oki.config.log.BassLogManager;
import com.oki.error.notification.BassErrorNotificateSlack;
import com.rometools.fetcher.FeedFetcher;
import com.rometools.fetcher.impl.FeedFetcherCache;
import com.rometools.fetcher.impl.HashMapFeedInfoCache;
import com.rometools.fetcher.impl.HttpURLFeedFetcher;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;

public class RssUrlBuilder implements MessageSource<SyndEntry>, InitializingBean{

	private static final Logger logger = LoggerFactory.getLogger(RssUrlBuilder.class);
	
	@Autowired
	private BassBlogRepository bassBlogRepository;
	
	@Autowired
	private BassErrorNotificateSlack bassErrorNotificateSlack;
	
	private FeedFetcherCache feedInfoCache;
    private FeedFetcher feedFetcher;
    
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Message receive() {
		return MessageBuilder.withPayload(obtainFeedItems()).build();
	}
	
	private List<SyndFeed> obtainFeedItems() {
        List<SyndFeed> feeds = new ArrayList<>();
        try {
        	List<Blog> blogs = bassBlogRepository.findAll();
        	for (Blog blog : blogs) {
        		feeds.add(feedFetcher.retrieveFeed(new URL(blog.getUrl())));
			}
        } catch (Exception e) {
            logger.error("Problem while retrieving feed. error={}", BassLogManager.makeLog(e.getMessage(), e));
            bassErrorNotificateSlack.send(e);
        }
        return feeds;
    }
	
	@Override
	public void afterPropertiesSet() throws Exception {
		feedInfoCache = HashMapFeedInfoCache.getInstance();
		feedFetcher = new HttpURLFeedFetcher(feedInfoCache);
	}

}
