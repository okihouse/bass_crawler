package com.oki.config.meta;

public interface MetadataParser {

	public enum META_TYPE {IMAGE, TITLE, DESCRIPTION};
	
	public String get(String url, String id, META_TYPE type);
	
}
