package com.oki.config.schedule;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.Video;
import com.oki.bass.type.BassType;
import com.oki.bass.youtube.domain.entity.Youtube;
import com.oki.bass.youtube.domain.entity.YoutubeChannel;
import com.oki.bass.youtube.domain.repository.BassYoutubeChannelRepository;
import com.oki.bass.youtube.domain.repository.BassYoutubeRepository;
import com.oki.config.locking.DuplicateLockingManager;
import com.oki.config.locking.youtube.YoutubeLockingService;
import com.oki.config.log.BassLogManager;
import com.oki.config.youtube.YoutubeConnectionService;

@Component
public class YoutubeScheduler {

	private static final Logger logger = LoggerFactory.getLogger(YoutubeScheduler.class);

	@Autowired
	private YoutubeConnectionService youtubeConnectionService;
	
	@Autowired
	private BassYoutubeRepository bassYoutubeRepository;
	
	@Autowired
	private BassYoutubeChannelRepository bassYoutubeChannelRepository;
	
	private DuplicateLockingManager duplicateLockingManager = YoutubeLockingService.getInstance();
	
	@Scheduled(fixedRate = 1800000)
	@Transactional
	public void youtube() {
		
		String youtubePlaylistid = null;
		Date today = new Date();
		try {
			List<String> itemlist = getItemList();
			validateList(today);
			
//			Calendar calendar = Calendar.getInstance();
//			calendar.add(Calendar.DATE, -110);
//			Date customDay = calendar.getTime();
			
			for (String playlistId : itemlist) {
				youtubePlaylistid = playlistId;
				PlaylistItemListResponse playlistItemListResponse = youtubeConnectionService.getPlaylistItems(playlistId);
				List<PlaylistItem> playlistItems = playlistItemListResponse.getItems();
				
				
				playlistItemListResponse.getPageInfo().getTotalResults();
				
				List<PlaylistItemListResponse> playlistItemListResponses = new ArrayList<>();
				if (playlistItemListResponse.getPageInfo().getTotalResults() > YoutubeConnectionService.NUMBER_OF_VIDEOS_RETURNED) {
					// if list sorted by publised time asc --> resorted by all list publised time desc
					if (playlistItems.get(0).getSnippet().getPublishedAt().getValue() < playlistItems.get(1).getSnippet().getPublishedAt().getValue()) {
						playlistItemListResponses = youtubeConnectionService.getPlaylistItemsSortByLastest(playlistId, playlistItemListResponse.getNextPageToken());
						
						for (PlaylistItemListResponse items : playlistItemListResponses) {
							playlistItems.addAll(items.getItems());
						}
					}
				}
				
				for (PlaylistItem playlistItem : playlistItems) {
					Date publishedDate = new Date(playlistItem.getSnippet().getPublishedAt().getValue());
					String id = playlistItem.getId();
					if (!DateUtils.isSameDay(today, publishedDate)) continue;
//					if (customDay.getTime() > publishedDate.getTime()) continue;
					if (duplicateLockingManager.isRegistered(id)) continue;
					
					
					List<Video> videos = youtubeConnectionService.getVideo(playlistItem.getContentDetails().getVideoId());
					Long duration = 0L;
					try {
						if (videos.isEmpty() == false) {
							duration = Duration.parse(videos.get(0).getContentDetails().getDuration()).getSeconds();
						}
					} catch (Exception e) {
						duration = 0L;
						logger.error("youtube duration processing is failed. error={}", BassLogManager.makeLog(e.getMessage(), e));
					}
					
					List<Channel> channels = youtubeConnectionService.getThumbnail(playlistItem.getSnippet().getChannelId());
					String profile = "";
					try {
						if (channels.isEmpty() == false) {
							profile = channels.get(0).getSnippet().getThumbnails().getMedium().getUrl();
						}
					} catch (Exception e) {
						logger.error("youtube thumbnail processing is failed. error={}", BassLogManager.makeLog(e.getMessage(), e));
					}
					
					YoutubeChannel youtubeChannel = bassYoutubeChannelRepository.findById(playlistItem.getSnippet().getPlaylistId());
					if (youtubeChannel == null) {
						logger.warn("YoutubeChannel is not existed. playlist id={}", playlistItem.getSnippet().getPlaylistId());
						return;
					}
					
					// If thumbnail is null then this video is PRIVATE(NOT PUB))
					String thumbnail = "";
					try {
						thumbnail = playlistItem.getSnippet().getThumbnails().getHigh().getUrl();
					} catch (Exception e) {
						logger.warn("This video is private. playlist id={}", playlistItem.getSnippet().getPlaylistId());
						continue;
					}
					
					Youtube youtube = new Youtube();
					youtube.setId(id);
					youtube.setTitle(playlistItem.getSnippet().getTitle());
					youtube.setDescription(playlistItem.getSnippet().getDescription());
					youtube.setThumbnail(thumbnail);
					youtube.setVideoId(playlistItem.getSnippet().getResourceId().getVideoId());
					youtube.setDuration(duration);
					youtube.setProfile(profile);
					youtube.setPublishedDate(publishedDate);
					youtube.setView(BassType.YN.Y);
					
					youtube.setYoutubeChannel(youtubeChannel);
					bassYoutubeRepository.save(youtube);
					
					logger.info("New youtube playlist item is added. item={}", youtube);
				}
			}
		} catch (Exception e) {
			logger.error("YoutubeScheduler youtube processing is failed. playlistId={}, error={}", youtubePlaylistid, BassLogManager.makeLog(e.getMessage(), e));
		}
		logger.info("Youtube crawling is done, date={}", today);
	}
	
	private List<String> getItemList() {
		return bassYoutubeChannelRepository.findByIsUse(BassType.YN.Y);
	}

	private void validateList(Date today){
		if (duplicateLockingManager.isEmpty()) {
			List<String> ids = bassYoutubeRepository.findByPublishedDateStartDateAfter(DateUtils.truncate(today, Calendar.DAY_OF_MONTH));
			duplicateLockingManager.register(ids);
		}
	}
	
}
