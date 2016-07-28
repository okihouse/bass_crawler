package com.oki.bass.blog.vo;

import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import lombok.Data;

@Data
public class BassBlogInsertVO {

	@NotEmpty(message = "{validation.notnull.message}")
	private String url;
	
	@NotEmpty(message = "{validation.notnull.message}")
	private List<String> filters;
	
}
