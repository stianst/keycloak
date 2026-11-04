package org.keycloak.misc.vulns.github;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;

public class GitHubIssueBuilder {

    private final String repositoryFullName;
    private final GitHubRequest request;
    private final GitHubIssue existing;

    private final Map<String, Object> requestBody = new LinkedHashMap<>();
    private Set<String> labels;

    GitHubIssueBuilder(String repositoryFullName, GitHubRequest request, GitHubIssue issue) {
        this.existing = issue;
        this.repositoryFullName = repositoryFullName;
        this.request = request;
        this.labels = new HashSet<>(existing.labels());
    }

    GitHubIssueBuilder(String repositoryFullName, GitHubRequest request, String title) {
        this.existing = null;
        this.repositoryFullName = repositoryFullName;
        this.request = request;
        title(title);
    }

    public GitHubIssueBuilder title(String title) {
        return put("title", title);
    }

    public GitHubIssueBuilder body(String body) {
        return put("body", body);
    }

    public GitHubIssueBuilder type(String type) {
        return put("type", type);
    }

    public GitHubIssueBuilder reopen() {
        return put("state", "open");
    }

    public GitHubIssueBuilder completed() {
        return put("state", "closed").put("state_reason", "completed");
    }

    public GitHubIssueBuilder notPlanned() {
        return put("state", "closed").put("state_reason", "not_planned");
    }

    public GitHubIssueBuilder label(String... labels) {
        return label(Arrays.asList(labels));
    }

    public GitHubIssueBuilder label(Collection<String> labels) {
        if (this.labels == null) {
            this.labels = new HashSet<>();
        }
        this.labels.addAll(labels);
        return this;
    }

    public GitHubIssueBuilder removeLabel(String... labels) {
        if (this.labels != null) {
            Arrays.asList(labels).forEach(this.labels::remove);
        }
        return this;
    }

    public GitHubIssueBuilder clearLabels() {
        if (this.labels == null) {
            this.labels = new HashSet<>();
        }
        this.labels.clear();
        return this;
    }

    public GitHubIssue send() throws GitHubApiException {
        if (existing == null) {
            if (this.labels != null) {
                requestBody.put("labels", this.labels);
            }
            URI uri = GitHubUriBuilder.create("repos", repositoryFullName, "issues").build();
            return GitHubApi.asIssue(request.post(uri, requestBody));
        }

        String number = Integer.toString(existing.number());
        URI issueUri = GitHubUriBuilder.create("repos", repositoryFullName, "issues", number).build();
        JsonNode json = requestBody.isEmpty() ? null : request.patch(issueUri, requestBody);

        // Change labels incrementally so concurrent label changes are not overwritten
        Set<String> toAdd = new HashSet<>(this.labels);
        toAdd.removeAll(existing.labels());
        Set<String> toRemove = new HashSet<>(existing.labels());
        toRemove.removeAll(this.labels);

        if (!toAdd.isEmpty()) {
            URI labelsUri = GitHubUriBuilder.create("repos", repositoryFullName, "issues", number, "labels").build();
            request.postLabels(labelsUri, Map.of("labels", toAdd));
        }
        for (String label : toRemove) {
            URI labelUri = GitHubUriBuilder.create("repos", repositoryFullName, "issues", number, "labels", URLEncoder.encode(label, StandardCharsets.UTF_8).replace("+", "%20")).build();
            request.delete(labelUri);
        }

        if (json == null || !toAdd.isEmpty() || !toRemove.isEmpty()) {
            json = request.get(issueUri);
        }
        return GitHubApi.asIssue(json);
    }

    private GitHubIssueBuilder put(String key, Object value) {
        requestBody.put(key, value);
        return this;
    }

}
