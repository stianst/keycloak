package org.keycloak.misc.vulns;

import java.io.File;

public class Config {

    public static String REPOSITORY;
    public static File OUTPUT;
    public static File IGNORE;
    public static File REPORTS;
    public static String STREAM;

    public static boolean UPDATE_ISSUES = false;

    public static void init(String[] args) {
        for (String a : args) {
            if (a.equals("--update-issues")) {
                UPDATE_ISSUES = true;
            } else if (a.startsWith("--ignore=")) {
                IGNORE = new File(a.split("=")[1]);
            } else if (a.startsWith("--repository=")) {
                REPOSITORY = a.split("=")[1];
            } else if (a.startsWith("--output")) {
                OUTPUT = new File(a.split("=")[1]);
            } else if (a.startsWith("--stream")) {
                STREAM = a.split("=")[1];
            } else if (!a.startsWith("--")) {
                REPORTS = new File(a);
            } else {
                throw new RuntimeException("Unknown option " + a);
            }
        }

        if (REPOSITORY == null && System.getenv().containsKey("GITHUB_REPOSITORY")) {
            REPOSITORY = System.getenv("GITHUB_REPOSITORY");
        }
        if (OUTPUT == null && System.getenv().containsKey("GITHUB_STEP_SUMMARY")) {
            OUTPUT = new File(System.getenv("GITHUB_STEP_SUMMARY"));
        }
    }

}
