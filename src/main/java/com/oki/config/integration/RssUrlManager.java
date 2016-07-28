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
import org.springframework.stereotype.Service;

import com.oki.bass.blog.domain.entity.Blog;
import com.oki.bass.blog.domain.repository.BassBlogRepository;
import com.oki.config.log.BassLogManager;
import com.oki.error.notification.BassErrorNotificateSlack;
import com.rometools.fetcher.FeedFetcher;
import com.rometools.fetcher.impl.HashMapFeedInfoCache;
import com.rometools.fetcher.impl.HttpURLFeedFetcher;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;

@Service
public class RssUrlManager implements MessageSource<SyndEntry>, InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(RssUrlManager.class);
	
	@Autowired
	private BassBlogRepository bassBlogRepository;
	
	@Autowired
	private BassErrorNotificateSlack bassErrorNotificateSlack;
	
	private FeedFetcher feedFetcher;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		feedFetcher = new HttpURLFeedFetcher(HashMapFeedInfoCache.getInstance());
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Message receive() {
		return MessageBuilder.withPayload(getFeeds()).build();
	}
	
	private List<SyndFeed> getFeeds() {
        List<SyndFeed> feeds = new ArrayList<>();
        try {
        	//feeds.add(feedFetcher.retrieveFeed(new URL("http://blog.rss.naver.com/jesuskim16.xml")));
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
	
}
