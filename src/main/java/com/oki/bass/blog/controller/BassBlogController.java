package com.oki.bass.blog.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.oki.bass.blog.service.BassBlogService;
import com.oki.bass.blog.vo.BassBlogInsertVO;
import com.oki.config.vo.SuccessVO;
import com.oki.error.exception.BassBindingException;
import com.oki.error.exception.BassProcessException;

@RestController
@RequestMapping(value = "/blog")
public class BassBlogController {

	@Autowired
	private BassBlogService bassBlogService;
	
	@RequestMapping(value = "/insert", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public SuccessVO insert(@RequestBody @Valid BassBlogInsertVO bassBlogInsertVO, BindingResult bindingResult) throws BassBindingException, BassProcessException {
		if (bindingResult.hasErrors()) {
			throw new BassBindingException(bindingResult);
		}
		bassBlogService.insert(bassBlogInsertVO);
		return new SuccessVO();
	}
	
	@RequestMapping(value = "/update/profile", method = RequestMethod.GET)
	@ResponseBody
	public SuccessVO profile() {
		bassBlogService.profile();
		return new SuccessVO();
	}
	
}
