package com.oki.config.meta.naver;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.oki.config.log.BassLogManager;
import com.oki.config.meta.MetadataParser;

@Service
public class ImageMetadataParser implements MetadataParser {

	private static final Logger logger = LoggerFactory.getLogger(ImageMetadataParser.class);
	
	@Override
	public String get(String url, String id, MetadataParser.META_TYPE type) {
		String image = null;
		try {
			Connection connection = Jsoup.connect(url);
			Document document = connection.get();
			if (url.contains("http://blog.naver.com") && !url.contains("PostView")) {
				document = Jsoup.connect("http://blog.naver.com" + document.head().nextElementSibling().children().eq(0).attr("src")).get();
			} else if (url.contains("blog.me")) {
				String newUrl = "http://blog.naver.com/PostView.nhn?blogId=" + id + "&logNo=" + getPostId(url);
				document = Jsoup.connect(newUrl).get();
			}
			
			String title = document.select("meta[property=og:title]").attr("content");
			if (title == null || title.isEmpty()) {
				title = document.select("title").text();
			}
			image = encodeString(document.select("meta[property=og:image]").attr("content"));
		} catch (IOException e) {
			logger.error("error={}", BassLogManager.makeLog(e.getMessage(), e));
		}
		return image;
	}
	
	private String encodeString(String str) throws UnsupportedEncodingException{
		String regex = "^[ㄱ-ㅎㅏ-ㅣ가-힣]+$";
		
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < str.length(); i++) {
			String word = String.valueOf(str.charAt(i));
			if (word.matches(regex)) word = URLEncoder.encode(word, "utf-8");
			sb.append(word);
		}
		return sb.toString();
	}
	
	private String getPostId(String url){
		String postId = null;
		try {
			Pattern pattern = Pattern.compile("[^\\/]*[0-9]$");
			Matcher matcher = pattern.matcher(url);
			if (matcher.find() == true) {
				postId = matcher.group(0);
			}
		} catch (Exception e) {
			logger.warn("pattern processing is failed. url={}, error={}", url, BassLogManager.makeLog(e.getMessage(), e));
		}
		return postId;
	}

}
