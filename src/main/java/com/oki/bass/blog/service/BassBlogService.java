package com.oki.bass.blog.service;

import java.net.URL;
import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.oki.bass.blog.domain.entity.Blog;
import com.oki.bass.blog.domain.repository.BassBlogRepository;
import com.oki.bass.blog.vo.BassBlogInsertVO;
import com.oki.config.locking.FilteredLockingManager;
import com.oki.config.locking.rss.RssLockingService;
import com.oki.config.log.BassLogManager;
import com.oki.config.property.PropertiesResource;
import com.oki.error.constant.BassErrorConstant;
import com.oki.error.exception.BassProcessException;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndImage;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

@Service
public class BassBlogService {

	private static final Logger logger = LoggerFactory.getLogger(BassBlogService.class);
	
	@Autowired
	private BassBlogRepository bassBlogRepository;
	
	@Autowired
	private PropertiesResource propertiesResource;

	@Transactional
	public void insert(BassBlogInsertVO bassBlogInsertVO) throws BassProcessException {
		
		List<Blog> blogs = bassBlogRepository.findByUrl(bassBlogInsertVO.getUrl());
		if (blogs.isEmpty() == false) {
			logger.info("Blog is existed. blog={}", blogs);
			return;
		}
		
		FilteredLockingManager filteredLockingManager = (FilteredLockingManager) RssLockingService.getInstance();
		boolean registered = filteredLockingManager.filterRegister(bassBlogInsertVO.getFilters());
		
		if (registered == false) {
			throw new RuntimeException("Blog rss fileter register is failed. check it!!");
		}
		
		String profile = "";
		String title = "";
		String id = "";
		try {
			SyndFeedInput input = new SyndFeedInput();
			SyndFeed syndFeed = input.build(new XmlReader(new URL(bassBlogInsertVO.getUrl())));
			id = syndFeed.getEntries().get(0).getAuthor();
			title = syndFeed.getTitle();
			profile = syndFeed.getImage().getUrl();
		} catch (Exception e) {
			logger.error("Rss feed parsing process is failed. error={}", BassLogManager.makeLog(e.getMessage(), e));
			throw new BassProcessException(propertiesResource.getErrorValue(BassErrorConstant.RSS_FEED_PARSING_FAILURE));
		}
		
		Blog blog = new Blog();
		blog.setName(bassBlogInsertVO.getFilters().toString().trim());
		blog.setTitle(title);
		blog.setUrl(bassBlogInsertVO.getUrl());
		blog.setId(id);
		blog.setProfile(profile);
		
		bassBlogRepository.save(blog);
		logger.info("New blog is added. item={}", blog);
	}

//	private String parseId(String url) {
//		String id = "";
//		if (url.contains("blog.rss.naver.com")) {
//			id = url.replace("http://blog.rss.naver.com/", "").replace(".xml", "");
//    	} else if (url.contains("blog.me")) {
//    		id = url.replace("http://", "").replace(".blog.me/rss", "");
//    	}
//		return id;
//	}

	@Transactional
	public void profile() {
		SyndFeedInput input = new SyndFeedInput();
		String author = "";
		String id = "";
		String url = "";
		String profile = "";
		try {
			List<Blog> blogs = bassBlogRepository.findAll();
			
			for (Blog blog : blogs) {
				SyndFeed syndFeed = input.build(new XmlReader(new URL(blog.getUrl())));
				SyndImage syndImage = syndFeed.getImage();
				if (syndImage != null) {
					profile = syndImage.getUrl();
				}
				author = blog.getTitle();
				id = blog.getId();
				url = blog.getUrl();
				if (!profile.isEmpty() && !profile.equalsIgnoreCase(blog.getProfile()) ) {
					blog.setProfile(profile);
					bassBlogRepository.save(blog);
					logger.info("Profile image is updated. author={}, id={}, image={}", author, id, profile);
				}
			}
		} catch (Exception e) {
			logger.error("Profile image update process is stopped. author={}, id={}, url={}, profile={}, error={}", author, id, url, profile, BassLogManager.makeLog(e.getMessage(), e));
		}
		
	}
	
}
