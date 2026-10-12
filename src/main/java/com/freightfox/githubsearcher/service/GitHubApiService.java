
package com.freightfox.githubsearcher.service;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.freightfox.githubsearcher.entity.GitHubRepository;
import com.freightfox.githubsearcher.repository.GitHubRepositoryRepository;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class GitHubApiService {

    private final GitHubRepositoryRepository githubRepositoryRepository;
    private final RestClient restClient;

    public GitHubApiService(
            GitHubRepositoryRepository githubRepositoryRepository) {

        this.githubRepositoryRepository = githubRepositoryRepository;

        this.restClient = RestClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader(
                        "Accept",
                        "application/vnd.github+json")
                .build();
    }

    // Search GitHub repositories and save/update them
    public List<GitHubRepository> searchRepositories(
            String query, String language, String sort) {

        // 1. Validate search query
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException(
                    "Search query cannot be empty");
        }

        // 2. Validate sort option
        if (sort != null && !sort.isBlank()) {

            List<String> allowedSortOptions = List.of(
                    "stars",
                    "forks",
                    "help-wanted-issues",
                    "updated"
            );

            sort = sort.trim().toLowerCase();

            if (!allowedSortOptions.contains(sort)) {
                throw new IllegalArgumentException(
                        "Invalid sort option. Allowed values: "
                        + "stars, forks, help-wanted-issues, updated");
            }

        } else {
            sort = "stars";
        }

        // 3. Build GitHub search query
        StringBuilder queryBuilder =
                new StringBuilder(query.trim());

        if (language != null && !language.isBlank()) {
            queryBuilder.append(" language:")
                    .append(language.trim());
        }

        final String githubQuery = queryBuilder.toString();
        final String sortOption = sort;

        // 4. Call GitHub Search API
        GitHubSearchResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search/repositories")
                        .queryParam("q", githubQuery)
                        .queryParam("sort", sortOption)
                        .queryParam("order", "desc")
                        .queryParam("per_page", 100)
                        .build())
                .retrieve()
                .body(GitHubSearchResponse.class);

        // 5. Handle missing or empty results
        if (response == null
                || response.items == null
                || response.items.isEmpty()) {
            return List.of();
        }

        // 6. Convert GitHub API response into entity objects
        List<GitHubRepository> repositories =
                response.items.stream()
                        .map(item -> {
                            GitHubRepository repository =
                                    new GitHubRepository();

                            repository.setId(item.id);
                            repository.setName(item.name);
                            repository.setDescription(item.description);

                            repository.setOwner(
                                    item.owner != null
                                            ? item.owner.login
                                            : null);

                            repository.setLanguage(item.language);
                            repository.setStars(item.stars);
                            repository.setForks(item.forks);
                            repository.setLastUpdated(item.lastUpdated);

                            return repository;
                        })
                        .toList();

        // 7. UPSERT: update existing records or insert new records
        List<GitHubRepository> repositoriesToSave =
                repositories.stream()
                        .map(repository -> {

                            GitHubRepository existing =
                                    githubRepositoryRepository
                                            .findById(repository.getId())
                                            .orElse(null);

                            if (existing != null) {

                                // Update existing repository
                                existing.setName(repository.getName());
                                existing.setDescription(
                                        repository.getDescription());
                                existing.setOwner(repository.getOwner());
                                existing.setLanguage(
                                        repository.getLanguage());
                                existing.setStars(repository.getStars());
                                existing.setForks(repository.getForks());
                                existing.setLastUpdated(
                                        repository.getLastUpdated());

                                return existing;
                            }

                            // Insert new repository
                            return repository;
                        })
                        .toList();

        // 8. Save all new and updated repositories
        return githubRepositoryRepository.saveAll(repositoriesToSave);
    }

    // GitHub API response wrapper
    public static class GitHubSearchResponse {

        public List<GitHubRepositoryItem> items;
    }

    // Individual repository returned by GitHub
    public static class GitHubRepositoryItem {

        public Long id;
        public String name;
        public String description;
        public GitHubOwner owner;
        public String language;

        @JsonProperty("stargazers_count")
        public Integer stars;

        @JsonProperty("forks_count")
        public Integer forks;

        @JsonProperty("updated_at")
        public Instant lastUpdated;
    }

    // Repository owner details
    public static class GitHubOwner {

        public String login;
    }
}
