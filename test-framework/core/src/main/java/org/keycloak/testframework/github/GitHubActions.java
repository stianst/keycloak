package org.keycloak.testframework.github;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.keycloak.testframework.config.Config;

import org.junit.jupiter.api.extension.ExtensionContext;

public class GitHubActions {

    private final boolean enabled;
    private final boolean annotations;
    private final File gitHubStepSummary;
    private final String gitRoot = findGitRoot();

    private final long slowTestTimeout;

    private long testStartedAt;

    private int successTestCount;

    private List<Failure> failures = new LinkedList<>();
    private List<Slow> slowTests = new LinkedList<>();

    public GitHubActions() {
        String githubStepSummary = System.getenv("GITHUB_STEP_SUMMARY");
        this.gitHubStepSummary = githubStepSummary != null ? new File(githubStepSummary) : null;
        this.enabled = Config.get("kc.test.github.enabled", true, Boolean.class) && gitHubStepSummary != null;
        this.slowTestTimeout = TimeUnit.SECONDS.toMillis(Config.get("kc.test.github.slow", 1L, Long.class));
        this.annotations = Config.get("kc.test.github.annotations", false, Boolean.class);

        System.out.println("SUMMARY: " + githubStepSummary);
        System.out.println("Enabled: " + enabled);
        System.out.println("Timeout: " + slowTestTimeout);
        System.out.println("Annotations: " + annotations);

    }

    public void onClassError(ExtensionContext context) {
        if (enabled) {
            onError(context, false);
        }
    }

    public void onMethodStart() {
        if (enabled && slowTestTimeout >= -1) {
            testStartedAt = System.currentTimeMillis();
        }
    }

    public void onMethodSuccess(ExtensionContext context) {
        if (enabled) {
            if (slowTestTimeout >= -1) {
                long executionTime = System.currentTimeMillis() - testStartedAt;
                if (executionTime > slowTestTimeout) {
                    slowTests.add(new Slow(context.getRequiredTestClass().getName(), context.getRequiredTestMethod().getName(), executionTime));
                }
            }

            successTestCount++;
        }
    }

    public void onMethodFailed(ExtensionContext context) {
        if (enabled) {
            onError(context, true);
        }
    }

    public void printSummary() {
        if (enabled) {
            try {
                PrintWriter printWriter = new PrintWriter(new FileWriter(gitHubStepSummary, true));

                if (failures.isEmpty() && slowTests.isEmpty()) {
                    printWriter.println("## :white_check_mark: All tests passed");
                    printWriter.println("Executed " + successTestCount + " tests");
                } else {
                    if (!failures.isEmpty()) {
                        printWriter.println("## :x: Failed tests");
                        printWriter.println("| Test class | Test method | Failure |");
                        printWriter.println("| ---------- | ----------- | ------- |");

                        failures.stream().sorted(Comparator.comparing(Failure::className)).forEach(f ->
                                printWriter.println("| " + f.className() + " | " + f.methodName + " |" + f.message() + " |")
                        );
                    }

                    if (!slowTests.isEmpty()) {
                        printWriter.println("## :hourglass: Slow tests detected");
                        printWriter.println("| Test class | Test method | Execution time |");
                        printWriter.println("| ---------- | ----------- | -------------- |");

                        slowTests.stream().sorted(Comparator.comparing(Slow::executionTime).reversed()).forEach(s ->
                                printWriter.println("| " + s.className() + " | " + s.methodName() + " | " + s.executionTime() + " |")
                        );
                    }
                }

                printWriter.println("## Env");
                printWriter.println("| Key | Value |");
                printWriter.println("| --- | ----- |");
                System.getenv().forEach((k, v) -> printWriter.println("| " + k + " | " + v + " |"));

                printWriter.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void onError(ExtensionContext context, boolean method) {
        Optional<Throwable> executionException = context.getExecutionException();
        if (executionException.isPresent()) {
            Class<?> testClass = context.getRequiredTestClass();
            String file = findJavaClass(testClass);

            Method testMethod = method ? context.getRequiredTestMethod() : null;

            Throwable throwable = executionException.get();

            String message = throwable.getMessage();

            StackTraceElement stackTraceElement = throwable.getStackTrace()[throwable.getStackTrace().length - 1];
            int line = stackTraceElement.getLineNumber();

            String title = "Test '" + context.getRequiredTestMethod().getName() + "' failed";

            printErrorAnnotation(file, line, title, message);
            failures.add(new Failure(testClass.getName(), testMethod != null ? testMethod.getName() : "", message));
        }
    }

    private void printErrorAnnotation(String file, int line, String title, String message) {
        if (annotations) {
            System.out.print("::error ");
            System.out.print("file=" + file);
            System.out.print(",line=" + line);
            System.out.print(",title=" + title);
            System.out.println("::" + message);
        }
    }

    private String findJavaClass(Class<?> testClass) {
        String classFile = testClass.getResource("/" + testClass.getName().replace('.', '/') + ".class").getFile();
        return classFile.replace(gitRoot + "/", "").replace("target/test-classes", "src/test/java").replace(".class", ".java");
    }

    private String findGitRoot() {
        File file = new File(System.getProperty("user.dir"));
        while (file.isDirectory()) {
            if (new File(file, ".git").isDirectory()) {
                return file.getAbsolutePath();
            }
            file = file.getParentFile();
        }
        throw new RuntimeException("Failed to find .git directory");
    }

    private record Slow(String className, String methodName, long executionTime) {}

    private record Failure(String className, String methodName, String message) {}

}
