package org.keycloak.misc;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHIssueStateReason;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.PagedSearchIterable;

public class VulnToIssues {

    private static final List<String> DEV_PATHS = List.of("misc/", "testsuite/", "util/", "test-framework/", "tests/");

    public static void main(String[] args) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
//        TrivyReport trivyReport = objectMapper.reader().readValue(new File("/home/st/dev/keycloak/trivy.log"), TrivyReport.class);
        TrivyReport trivyReport = objectMapper.reader().readValue(new File("/home/st/dev/keycloak/trivy-report-refs_heads_release_26.7/trivy-report.json"), TrivyReport.class);


        List<String> vulnerabilityIds = listVulnerabilityIds(trivyReport);

        GitHub github = new GitHubBuilder().withJwtToken(getTokenFromGhCli()).build();

        for (String vulnerabilityId : vulnerabilityIds) {
            TrivyReport.Vulnerability vulnerability = getVulnerability(trivyReport, vulnerabilityId);
            System.out.println(vulnerability.vulnerabilityId() + " " + vulnerability.severity() + " " + vulnerability.pkgName() + " " + vulnerability.installedVersion() + " " + vulnerability.fixedVersion());

//            System.out.println("Targets: " + listTargetsByVulnerabilityId(trivyReport, vulnerabilityId).collect(Collectors.joining(", ")));
            System.out.println("Prod Targets: " + listTargetsByVulnerabilityId(trivyReport, vulnerabilityId).filter(TargetPredicate.PROD).collect(Collectors.joining(", ")));
            System.out.println("Dev Targets: " + listTargetsByVulnerabilityId(trivyReport, vulnerabilityId).filter(TargetPredicate.DEV).collect(Collectors.joining(", ")));

//            PagedSearchIterable<GHIssue> ghIssues = github.searchIssues().repo("keycloak", "keycloak").q(vulnerabilityId).list();
//            for (GHIssue i : ghIssues) {
//                String state = switch (i.getState()) {
//                    case OPEN -> "OPEN";
//                    case CLOSED -> {
//                        if (i.getStateReason() != null) {
//                            yield i.getStateReason().equals(GHIssueStateReason.NOT_PLANNED) ? "REJECTED" : "DONE";
//                        } else if (i.isPullRequest()) {
//                            GHPullRequest pullRequest = github.getRepository("keycloak/keycloak").getPullRequest(i.getNumber());
//                            yield  pullRequest.isMerged() ? "DONE" : "REJECTED";
//                        } else {
//                            yield "UNKNOWN";
//                        }
//                    }
//                    case ALL -> "UNKNOWN";
//                };
//
//                System.out.println(" - " + (i.isPullRequest() ? "PULL_REQUEST " : "ISSUE ") + state + " " + i.getHtmlUrl() + " " + i.getTitle());
//            }
            System.out.println();
        }
    }

    private static Stream<String> listTargetsByVulnerabilityId(TrivyReport trivyReport, String vulnerabilityId) {
        List<TrivyReport.Result> resultStream = listResultsByVulnerabilityId(trivyReport, vulnerabilityId);
        return resultStream.stream().map(TrivyReport.Result::target).map(s -> s.replaceAll("/pom.xml", "/")).filter(Predicate.not(TargetPredicate.MAVEN_TARGET)).sorted();
    }
    
    private static TrivyReport.Vulnerability getVulnerability(TrivyReport trivyReport, String vulnerabilityId) {
        return trivyReport.results().stream()
                .filter(r -> r.vulnerabilities() != null)
                .flatMap(r -> r.vulnerabilities().stream())
                .filter(v -> v.vulnerabilityId().equals(vulnerabilityId))
                .findFirst().orElse(null);
    }

    private static List<String> listVulnerabilityIds(TrivyReport trivyReport) {
        return trivyReport.results().stream()
                .filter(r -> r.vulnerabilities() != null)
                .flatMap(r -> r.vulnerabilities().stream())
                .map(TrivyReport.Vulnerability::vulnerabilityId)
                .sorted().distinct().toList();
    }

    private static List<TrivyReport.Result> listResultsByVulnerabilityId(TrivyReport trivyReport, String vulnerabilityId) {
        return trivyReport.results().stream()
                .filter(r -> r.vulnerabilities() != null && r.vulnerabilities().stream()
                        .map(TrivyReport.Vulnerability::vulnerabilityId)
                        .anyMatch(v -> v.equals(vulnerabilityId))).toList();
    }

    private static String getTokenFromGhCli() throws IOException {
        ProcessBuilder pb = new ProcessBuilder("gh", "auth", "token");
        Process process = pb.start();
        return new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
    }

    public static final class TargetPredicate {
        public static final Predicate<String> DEV = s -> DEV_PATHS.stream().anyMatch(s::startsWith);
        public static final Predicate<String> PROD = s -> DEV_PATHS.stream().noneMatch(s::startsWith);
        public static final Predicate<String> MAVEN_TARGET = s -> s.contains("/target/");
    }
}
