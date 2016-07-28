package com.oki.bass.youtube.domain.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.oki.bass.youtube.domain.entity.Youtube;

public interface BassYoutubeRepository extends JpaRepository<Youtube, Long> {

	@Query(value = "select y.id from Youtube y where y.publishedDate > :today")
	List<String> findByPublishedDateStartDateAfter(@Param("today") Date today);

}
