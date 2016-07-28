package com.oki.config.locking.youtube;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oki.config.locking.DuplicateLockingManager;
import com.oki.config.log.BassLogManager;
import com.oki.util.crypto.KeyGenerator;

public class YoutubeLockingService implements DuplicateLockingManager {

	private static final Logger logger = LoggerFactory.getLogger(YoutubeLockingService.class);
	
	private static final YoutubeLockingService INSTANCE_SINGLETON = new YoutubeLockingService();
	
	private YoutubeLockingService() {
	}
	
	public static YoutubeLockingService getInstance(){
		return INSTANCE_SINGLETON;
	}
	
	private Map<String, String> registeredContent = new ConcurrentHashMap<>();

	@Override
	public boolean isEmpty() {
		synchronized (this) {
    		if (registeredContent.size() > 1000) registeredContent.clear();
        	return registeredContent.isEmpty();
        }
	}
	
	@Override
	public boolean isRegistered(String content) {
		synchronized (this) {
        	boolean isRegistered = true;
        	try {
				String key = KeyGenerator.makeKey(content);
				if (!registeredContent.containsKey(key)) {
					registeredContent.put(key, content);
					isRegistered = false;
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("YoutubeLockingService can't registered item. error={}", BassLogManager.makeLog(e.getMessage(), e));
			}
        	return isRegistered;
        }
	}
	
	@Override
	public void register(List<String> contents) {
		synchronized (this) {
			try {
				for (String content : contents) {
					String key = KeyGenerator.makeKey(content);
					if (!registeredContent.containsKey(key)) {
						registeredContent.put(key, content);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("YoutubeLockingService can't registered items. error={}", BassLogManager.makeLog(e.getMessage(), e));
			}
		}
	}
    
}
