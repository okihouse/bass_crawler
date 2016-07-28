package com.oki.bass.youtube.service;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.api.services.youtube.model.PlaylistItem;
import com.oki.bass.type.BassType;
import com.oki.bass.youtube.domain.entity.YoutubeChannel;
import com.oki.bass.youtube.domain.repository.BassYoutubeChannelRepository;
import com.oki.config.property.PropertiesResource;
import com.oki.config.youtube.YoutubeConnectionService;
import com.oki.error.constant.BassErrorConstant;
import com.oki.error.exception.BassParamException;

@Service
public class BassYoutubeService {
	
	private static final Logger logger = LoggerFactory.getLogger(BassYoutubeService.class);

	@Autowired
	private BassYoutubeChannelRepository bassYoutubeChannelRepository;
	
	@Autowired
	private YoutubeConnectionService youtubeConnectionService;
	
	@Autowired
	private PropertiesResource propertiesResource;

	@Transactional
	public void insertYoutube(String playlistId) throws BassParamException {
		if (playlistId == null || playlistId.isEmpty()) {
			logger.warn("Insert youtube channel item is failed. playlist id is not valid. playlistId={}", playlistId);
			throw new BassParamException(propertiesResource.getErrorValue(BassErrorConstant.MISSING_PARAMETER_ERROR_KEY));
		}
		
		YoutubeChannel channel = bassYoutubeChannelRepository.findById(playlistId);
		if (channel != null) return; // passing when id exist.
		
		List<PlaylistItem> playlistItems = youtubeConnectionService.getPlaylistItems(playlistId).getItems();
		for (PlaylistItem playlistItem : playlistItems) {
			YoutubeChannel youtubeChannel = new YoutubeChannel();
			youtubeChannel.setId(playlistId);
			youtubeChannel.setTitle(playlistItem.getSnippet().getChannelTitle());
			youtubeChannel.setDescription(playlistItem.getKind());
			youtubeChannel.setRegisteredDate(new Date());
			youtubeChannel.setIsUse(BassType.YN.Y);
			bassYoutubeChannelRepository.save(youtubeChannel);
			logger.info("New youtube playlist is added. item={}", youtubeChannel);
			break;
		}
	}

}
