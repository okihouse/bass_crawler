package com.oki.config.locking.rss;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oki.config.locking.DuplicateLockingManager;
import com.oki.config.locking.FilteredLockingManager;
import com.oki.config.log.BassLogManager;
import com.oki.util.crypto.KeyGenerator;

public class RssLockingService implements FilteredLockingManager {

	private static final Logger logger = LoggerFactory.getLogger(RssLockingService.class);

	private static final RssLockingService INSTANCE_SINGLETON = new RssLockingService();
	
	public static DuplicateLockingManager getInstance() {
		return INSTANCE_SINGLETON;
	}
	
	private RssLockingService() {
	}

    private Map<String, String> registeredContent = new ConcurrentHashMap<>();
    private Map<String, String> filters = new ConcurrentHashMap<>();
	
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
				logger.error("error={}", BassLogManager.makeLog(e.getMessage(), e));
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
				logger.error("error={}", BassLogManager.makeLog(e.getMessage(), e));
			}
		}
	}

	@Override
	public boolean isAllow(String content) {
		synchronized (this) {
			boolean isAllow = false;
			try {
				isAllow = filters.containsKey(KeyGenerator.makeKey(content));
			} catch (Exception e) {
				logger.error("error={}", BassLogManager.makeLog(e.getMessage(), e));
			}
			return isAllow;
		}
	}

	@Override
	public boolean filterRegister(String filter) {
		synchronized (this) {
			boolean register = false;
			try {
				String key = KeyGenerator.makeKey(filter);
				if (!filters.containsKey(key)) {
					filters.put(key, filter);
					register = true;
				}
			} catch (Exception e) {
				logger.error("filter register is failed. error={}", BassLogManager.makeLog(e.getMessage(), e));
			}
			return register;
		}
	}

	@Override
	public boolean filterRegister(List<String> filterStrings) {
		synchronized (this) {
			boolean register = false;
			try {
				for (String filter : filterStrings) {
					if (filter.contains(",")) { // array??
						for (String splitFilter : filter.replace("[", "").replace("]", "").split(", ")) {
							this.filters.put(KeyGenerator.makeKey(splitFilter), splitFilter);
						}
					} else {
						String filterString = filter.substring(1, filter.length() - 1);
						this.filters.put(KeyGenerator.makeKey(filterString), filterString);
					}
				}
				register = true;
			} catch (Exception e) {
				logger.error("filters register is failed. error={}", BassLogManager.makeLog(e.getMessage(), e));
			}
			return register;
		}
	}

	@Override
	public boolean isFilterEmpty() {
		return filters.isEmpty();
	}
	
}
