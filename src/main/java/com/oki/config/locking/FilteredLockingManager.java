package com.oki.config.locking;

import java.util.List;

public interface FilteredLockingManager extends DuplicateLockingManager{

	public boolean isAllow(String content);
	
	public boolean filterRegister(String filter);
	
	public boolean filterRegister(List<String> filters);
	
	public boolean isFilterEmpty();
	
}
