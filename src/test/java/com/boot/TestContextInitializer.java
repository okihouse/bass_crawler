package com.boot;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

public class TestContextInitializer implements ApplicationContextInitializer<GenericApplicationContext> {

	@Override
	public void initialize(GenericApplicationContext applicationContext) {
		applicationContext.getEnvironment().getSystemProperties().put("jasypt.encryptor.password", "BASS_STORY");
	}
	
}
