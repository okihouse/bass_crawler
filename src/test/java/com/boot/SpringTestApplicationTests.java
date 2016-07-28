package com.boot;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.oki.BassApplication;
import com.oki.bass.blog.domain.entity.Blog;
import com.oki.bass.blog.domain.repository.BassBlogRepository;
import com.oki.bass.content.domain.entity.Content;
import com.oki.bass.content.domain.repository.BassContentRepository;
import com.oki.bass.type.BassType;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = BassApplication.class, initializers = TestContextInitializer.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
@Rollback
@Transactional
@ActiveProfiles(value = "local")
public class SpringTestApplicationTests {
	
	private static final Logger logger = LoggerFactory.getLogger(SpringTestApplicationTests.class);

	@Autowired
	private WebApplicationContext webApplicationContext;
	
	private MockMvc mockMvc;
	
	@Before
	public void before(){
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
	}
	
	@Test
	public void test_youtube() throws Exception {
		// request put
		mockMvc.perform(
				put("/youtube/insert")
				.param("playlistId", "PL3bCnjuQmAocMGz8YXQf3IctuXl6TV3cs")
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
					.andDo(print())
					.andExpect(status().is2xxSuccessful());
	}
	
	@Autowired
	private BassBlogRepository bassBlogRepository;
	
	@Autowired
	private BassContentRepository bassContentRepository;
	
	@Test
	public void test_blog() throws Exception {
		String id = "dolong16";
		
		Blog blog = bassBlogRepository.findById(id);
		if (blog == null) {
			logger.warn("Blog is not existed. id={}", id);
			return;
		}
		
		Content content = new Content();
		content.setName("name");
		content.setRegisterDate(new Date());
		content.setUrl(blog.getUrl());
		content.setTitle("title");
		content.setCategory(BassType.CONTENT_CATEGORY.FISHING);
		content.setThumbnail("image");
		content.setBlog(blog);
		
		Content savedContent = bassContentRepository.save(content);
		System.out.println(savedContent);
	}

}
