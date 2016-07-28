package com.oki.bass.youtube.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.oki.bass.youtube.service.BassYoutubeService;
import com.oki.config.vo.SuccessVO;
import com.oki.error.exception.BassParamException;

@RestController
@RequestMapping(value = "/youtube")
public class BassYoutubeController {
	
	@Autowired
	private BassYoutubeService bassYoutubeService;
	
	@RequestMapping(value = "/insert", method = RequestMethod.PUT)
	public SuccessVO insert(String playlistId) throws BassParamException {
		bassYoutubeService.insertYoutube(playlistId);
		return new SuccessVO();
	}
	
}
