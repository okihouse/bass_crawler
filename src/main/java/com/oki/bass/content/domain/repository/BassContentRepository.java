package com.oki.bass.content.domain.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.oki.bass.content.domain.entity.Content;

public interface BassContentRepository extends JpaRepository<Content, Long>{

	@Query(value = "select c.title from Content c where c.registerDate > :today")
	List<String> findByRegisterDateStartDateAfter(@Param("today") Date today);

}
