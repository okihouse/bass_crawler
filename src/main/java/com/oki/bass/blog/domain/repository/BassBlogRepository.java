package com.oki.bass.blog.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.oki.bass.blog.domain.entity.Blog;

public interface BassBlogRepository extends JpaRepository<Blog, Long>{

	List<Blog> findByUrl(String url);

	@Query(value = "select b.profile from Blog b where b.id = :id")
	String fineTop1ById(@Param("id") String id);

	Blog findById(String id);

	@Query(value = "select b.name from Blog b")
	List<String> findAllFilters();

}
