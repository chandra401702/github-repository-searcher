
package com.freightfox.githubsearcher.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.freightfox.githubsearcher.entity.GitHubRepository;

public interface GitHubRepositoryRepository
		extends JpaRepository<GitHubRepository, Long>, JpaSpecificationExecutor<GitHubRepository> {

	List<GitHubRepository> findByLanguageIgnoreCase(String language);

	List<GitHubRepository> findByStarsGreaterThanEqual(Integer minStars);

	List<GitHubRepository> findByLanguageIgnoreCaseAndStarsGreaterThanEqual(String language, Integer minStars);
}
