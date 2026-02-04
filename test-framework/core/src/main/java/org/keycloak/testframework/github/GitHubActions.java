package org.keycloak.testframework.github;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class GitHubActions {

    private File gitHubStepSummary;

    private int classSuccess;
    private int classError;
    private int methodSuccess;
    private int methodFailed;
    private int methodAborted;
    private int methodDisabled;

    public GitHubActions() {
        String githubStepSummary = System.getenv("GITHUB_STEP_SUMMARY");
        if (githubStepSummary != null) {
            this.gitHubStepSummary = new File(githubStepSummary);
        }
    }

    public void onClassSuccess() {
        if (gitHubStepSummary != null) {
            classSuccess++;
        }
    }

    public void onClassError() {
        if (gitHubStepSummary != null) {
            classError++;
        }
    }

    public void onMethodSuccess() {
        if (gitHubStepSummary != null) {
            methodSuccess++;
        }
    }

    public void onMethodFailed() {
        if (gitHubStepSummary != null) {
            System.out.println("::error file=somefile,line=20,title=Some title::Some message");
            methodFailed++;
        }
    }

    public void onMethodAborted() {
        if (gitHubStepSummary != null) {
            methodAborted++;
        }
    }

    public void onMethodDisabled() {
        if (gitHubStepSummary != null) {
            methodDisabled++;
        }
    }

    public void printSummary() {
        if (gitHubStepSummary != null) {
            System.out.println("classSuccess=" + classSuccess + ",classError=" + classError + ",success=" + methodSuccess + ",failed=" + methodFailed + ",disabled=" + methodDisabled + ",aborted=" + methodAborted);

            try {
                PrintWriter printWriter = new PrintWriter(new FileWriter(gitHubStepSummary, true));
                printWriter.println("classSuccess=" + classSuccess + ",classError=" + classError + ",success=" + methodSuccess + ",failed=" + methodFailed + ",disabled=" + methodDisabled + ",aborted=" + methodAborted);
                printWriter.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
