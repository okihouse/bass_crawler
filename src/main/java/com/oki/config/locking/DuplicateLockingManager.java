package com.oki.config.locking;

import java.util.List;

public interface DuplicateLockingManager {

	public boolean isEmpty();
	
	public boolean isRegistered(String content);
	
	public void register(List<String> contents);
	
}
