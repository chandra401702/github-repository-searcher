
package com.freightfox.githubsearcher.controller;

import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.freightfox.githubsearcher.entity.GitHubRepository;
import com.freightfox.githubsearcher.repository.GitHubRepositoryRepository;
import com.freightfox.githubsearcher.service.GitHubApiService;


import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class GitHubRepositoryController {

    private final GitHubApiService gitHubApiService;
    private final GitHubRepositoryRepository repository;

    public GitHubRepositoryController(
            GitHubApiService gitHubApiService,
            GitHubRepositoryRepository repository) {

        this.gitHubApiService = gitHubApiService;
        this.repository = repository;
    }

    @GetMapping("/api/repositories/search")
    public List<GitHubRepository> searchRepositories(
            @RequestParam("q") String query) {

        return gitHubApiService.searchRepositories(
                query, null, "stars");
    }

    // Retrieve saved repositories from PostgreSQL
    @GetMapping("/api/github/repositories")
    public RepositoriesResponse getStoredRepositories(
            @RequestParam(required = false) String language,
            @RequestParam(required = false) Integer minStars,
            @RequestParam(defaultValue = "stars") String sort) {

        if (minStars != null && minStars < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "minStars cannot be negative");
        }

        String sortField = switch (sort.toLowerCase(Locale.ROOT)) {
            case "stars" -> "stars";
            case "forks" -> "forks";
            case "updated" -> "lastUpdated";
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid sort value. Use stars, forks, or updated");
        };

        // Build database filters
        Specification<GitHubRepository> spec =
                (root, query, cb) -> cb.conjunction();

        if (language != null && !language.isBlank()) {
            String languageFilter =
                    language.trim().toLowerCase(Locale.ROOT);

            spec = spec.and((root, query, cb) ->
                    cb.equal(
                            cb.lower(root.get("language")),
                            languageFilter));
        }

        if (minStars != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(
                            root.<Integer>get("stars"),
                            minStars));
        }

        // Query PostgreSQL with filtering and sorting
        List<GitHubRepository> repositories =
                repository.findAll(
                        spec,
                        Sort.by(Sort.Direction.DESC, sortField));

        return new RepositoriesResponse(repositories);
    }

    // Response wrapper required by the assignment
    public record RepositoriesResponse(
            List<GitHubRepository> repositories) {
    }
    
    
    
    
    

@PostMapping("/api/github/search")
public SearchResponse searchGitHubRepositories(
        @RequestBody SearchRequest request) {

    if (request.query() == null
            || request.query().isBlank()) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "query is required");
    }

    String sort = request.sort() == null
            || request.sort().isBlank()
                    ? "stars"
                    : request.sort()
                            .trim()
                            .toLowerCase(Locale.ROOT);

    if (!List.of("stars", "forks", "updated")
            .contains(sort)) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "sort must be stars, forks, or updated");
    }

    List<GitHubRepository> repositories =
            gitHubApiService.searchRepositories(
                    request.query(),
                    request.language(),
                    sort);

    return new SearchResponse(
            "Repositories fetched and saved successfully",
            repositories);
}

public record SearchRequest(
        String query,
        String language,
        String sort) {
}

public record SearchResponse(
        String message,
        List<GitHubRepository> repositories) {
}

}