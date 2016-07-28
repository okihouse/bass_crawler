package com.oki.config.youtube;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.oki.config.log.BassLogManager;

@Service
public class YoutubeConnectionService {

	private static final Logger logger = LoggerFactory.getLogger(YoutubeConnectionService.class);
	
	@Value("${google.secret.id}")
	private String key;
	
	/**
	 * Global instance of the max number of videos we want returned (50 = upper
	 * limit per page).
	 */
	public static final long NUMBER_OF_VIDEOS_RETURNED = 3;
	
	/** Global instance of Youtube object to make all API requests. */
	private static YouTube youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
		public void initialize(HttpRequest request) throws IOException {
		}
	}).build();
	
	public PlaylistItemListResponse getPlaylistItems(String playlistId){
		PlaylistItemListResponse playlistItemListResponse = new PlaylistItemListResponse();
		try {
			YouTube.PlaylistItems.List items = youtube.playlistItems().list("snippet,contentDetails");
			items.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);
			
			items.setKey(key);
			items.setPlaylistId(playlistId);
			
			playlistItemListResponse = items.execute();
		} catch (IOException e) {
			logger.error("YoutubeConnectionService getPlaylistItems is failed. error={}", BassLogManager.makeLog(e.getMessage(), e));
		}
		return playlistItemListResponse;
	}
	
	public List<PlaylistItemListResponse> getPlaylistItemsSortByLastest(String playlistId, String pageToken){
		List<PlaylistItemListResponse> playlistItemListResponses = new ArrayList<>();
		try {
			YouTube.PlaylistItems.List items = youtube.playlistItems().list("snippet,contentDetails");
			items.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);
			
			items.setKey(key);
			items.setPlaylistId(playlistId);
			
			String nextPageToken = pageToken;
			while (true) {
				items.setPageToken(nextPageToken);
				PlaylistItemListResponse playlistItemListResponse = items.execute();
				
				playlistItemListResponses.add(playlistItemListResponse);
				nextPageToken = playlistItemListResponse.getNextPageToken();
				if (nextPageToken == null) {
					break;
				}
			}
		} catch (IOException e) {
			logger.error("YoutubeConnectionService getPlaylistItems is failed. error={}", BassLogManager.makeLog(e.getMessage(), e));
		}
		return playlistItemListResponses;
	}
	
	public List<Video> getVideo(String videoId){
		List<Video> videoList = new ArrayList<>();
		try {
			YouTube.Videos.List videos = youtube.videos().list("contentDetails");
			videos.setMaxResults(1L);
			
			videos.setKey(key);
			videos.setId(videoId);
			
			VideoListResponse videoListResponse = videos.execute();
			videoList = videoListResponse.getItems();
		} catch (Exception e) {
			logger.error("YoutubeConnectionService getVideo is failed. video id={}, error={}", videoId, BassLogManager.makeLog(e.getMessage(), e));
		}
		return videoList;
	}
	
	public List<Channel> getThumbnail(String channelId){
		List<Channel> channels = new ArrayList<>();
		try {
			YouTube.Channels.List channelInfoList = youtube.channels().list("snippet");
			channelInfoList.setMaxResults(1L);
			
			channelInfoList.setKey(key);
			channelInfoList.setId(channelId);
			
			ChannelListResponse channelListResponse = channelInfoList.execute();
			channels = channelListResponse.getItems();
		} catch (Exception e) {
			logger.error("YoutubeConnectionService getThumbnail is failed. channelId id={}, error={}", channelId, BassLogManager.makeLog(e.getMessage(), e));
		}
		return channels;
	}
	
}
