package com.oki.config.meta.naver;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.oki.config.log.BassLogManager;
import com.oki.config.meta.MetadataParser;

@Service
public class ImageMetadataParser implements MetadataParser {

	private static final Logger logger = LoggerFactory.getLogger(ImageMetadataParser.class);
	
	@Override
	public String get(String url, MetadataParser.META_TYPE type) {
		String image = null;
		try {
			Connection connection = Jsoup.connect(url);
			if (url.contains("blog.naver.com")) {
				boolean noindex = connection.get().select("meta[name=robots]").attr("content").contains("noindex");
				if (noindex) {
					connection = Jsoup.connect("http://blog.naver.com" + connection.get().select("frame#mainFrame").attr("src"));
				}
			} else if (url.contains("blog.me")) {
				boolean noindex = connection.get().select("meta[name=robots]").attr("content").contains("noindex");
				if (noindex) {
					connection = Jsoup.connect(connection.get().select("frame#screenFrame").attr("src"));
					connection = Jsoup.connect("http://blog.naver.com" + connection.get().select("frame#mainFrame").attr("src"));
				}
			}
			image = encodeString(connection.get().select("meta[property=og:image]").attr("content"));
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

}
