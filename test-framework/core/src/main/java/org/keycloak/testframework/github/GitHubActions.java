package org.keycloak.testframework.github;

import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class GitHubActions {

    private final File gitHubStepSummary;
    private final String gitRoot = findGitRoot();

    private static final long SLOW_CLASS_TIMEOUT = TimeUnit.SECONDS.toMillis(2);
    private static final long SLOW_METHOD_TIMEOUT = TimeUnit.SECONDS.toMillis(1);

    private long classStartedAt;
    private long methodStartedAt;

    private int classSuccess;
    private int classError;
    private int methodSuccess;
    private int methodFailed;
    private int methodAborted;
    private int methodDisabled;

    private List<Class<?>> failedClasses = new LinkedList<>();
    private List<Method> failedMethods = new LinkedList<>();
    private Map<Class<?>, Long> slowClasses = new HashMap<>();
    private Map<Method, Long> slowMethods = new HashMap<>();

    public static void main(String[] args) {
        GitHubActions gitHubActions = new GitHubActions();
    }

    public GitHubActions() {
        String githubStepSummary = System.getenv("GITHUB_STEP_SUMMARY");
        this.gitHubStepSummary = githubStepSummary != null ? new File(githubStepSummary) : null;
    }

    public void onClassStart(ExtensionContext context) {
        classStartedAt = System.currentTimeMillis();
    }

    public void onClassSuccess(ExtensionContext context) {
        if (gitHubStepSummary != null) {
            long classExecutionTime = System.currentTimeMillis() - classStartedAt;
            if (classExecutionTime > SLOW_CLASS_TIMEOUT) {
                String file = findJavaClass(context.getRequiredTestClass());
                String title = "Slow test class detected";
                String message = "Took " + classExecutionTime + "ms to execute";
                printWarnAnnotation(file, title, message);
                slowClasses.put(context.getRequiredTestClass(), classExecutionTime);
            }

            classSuccess++;
        }
    }

    public void onClassError(ExtensionContext context) {
        if (gitHubStepSummary != null) {
            failedClasses.add(context.getRequiredTestClass());
            classError++;
        }
    }

    public void onMethodStart(ExtensionContext context) {
        methodStartedAt = System.currentTimeMillis();
    }

    public void onMethodSuccess(ExtensionContext context) {
        long methodExecutionTime = System.currentTimeMillis() - methodStartedAt;
        if (methodExecutionTime > SLOW_METHOD_TIMEOUT) {
            String file = findJavaClass(context.getRequiredTestClass());
            String title = "Slow test method detected: '" + context.getRequiredTestMethod().getName() + "'";
            String message = "Took " + methodExecutionTime + "ms to execute";
            printWarnAnnotation(file, title, message);
            slowMethods.put(context.getRequiredTestMethod(), methodExecutionTime);
        }

        if (gitHubStepSummary != null) {
            methodSuccess++;
        }
    }

    public void onMethodFailed(ExtensionContext context) {
        if (gitHubStepSummary != null) {
            String file = findJavaClass(context.getRequiredTestClass());

            Throwable throwable = context.getExecutionException().get();

            StackTraceElement stackTraceElement = throwable.getStackTrace()[throwable.getStackTrace().length - 1];
            String message = throwable.getMessage();
            int line = stackTraceElement.getLineNumber();

            String title = "Test '" + context.getRequiredTestMethod().getName() + "' failed";

            printErrorAnnotation(file, line, title, message);
            failedMethods.add(context.getRequiredTestMethod());
            methodFailed++;
        }
    }

    public void onMethodAborted(ExtensionContext context) {
        if (gitHubStepSummary != null) {
            methodAborted++;
        }
    }

    public void onMethodDisabled(ExtensionContext context) {
        if (gitHubStepSummary != null) {
            methodDisabled++;
        }
    }

    public void printSummary() {
        if (gitHubStepSummary != null) {
            try {
                PrintWriter printWriter = new PrintWriter(new FileWriter(gitHubStepSummary, true));
                printWriter.println("## Test results");
                printWriter.println("| Class Success | Class Failures | Success | Failures | Disabled |" );
                printWriter.println("| ------------- | -------------- | ------- | -------- | -------- |" );
                printWriter.println("| " + classSuccess + " | " + classError + " | " + methodSuccess + " | " + methodFailed + " | " + methodDisabled + " |" );

                if (!failedClasses.isEmpty()) {
                    printWriter.println("### Test classes with errors");
                    failedClasses.forEach(c -> printWriter.println(" * " + c.getName()));
                }

                if (!failedMethods.isEmpty()) {
                    printWriter.println("### Tests with errors");
                    failedMethods.forEach(m -> printWriter.println(" * " + testName(m)));
                }

                if (!slowClasses.isEmpty()) {
                    printWriter.println("### Slow test classes");
                    printWriter.println("| Class | Execution time |" );
                    printWriter.println("| ----- | -------------- |" );
                    slowClasses.forEach((key, value) -> printWriter.println("| " + key.getName() + " | " + value + " |"));
                }

                if (!slowMethods.isEmpty()) {
                    printWriter.println("### Slow test classes");
                    printWriter.println("| Class | Execution time |" );
                    printWriter.println("| ----- | -------------- |" );
                    slowMethods.forEach((key, value) -> printWriter.println("| " + testName(key) + " | " + value + " |"));
                }

                printWriter.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void printWarnAnnotation(String file, String title, String message) {
        System.out.print("::warning ");
        System.out.print("file=" + file);
        System.out.print(",title=" + title);
        System.out.println("::" + message);
    }

    private void printErrorAnnotation(String file, int line, String title, String message) {
        System.out.print("::error ");
        System.out.print("file=" + file);
        System.out.print(",line=" + line);
        System.out.print(",title=" + title);
        System.out.println("::" + message);
    }

    private String testName(Method method) {
        return method.getDeclaringClass().getName() + "#" + method.getName();
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

}
