import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class SureFireReports {

    private final File rootDir;
    private final List<File> sureFireReportDirs = new LinkedList<>();

    public static void main(String[] args) {
        new SureFireReports(new File(System.getProperty("user.dir")));
    }

    public SureFireReports(File rootDir) {
        this.rootDir = rootDir.getAbsoluteFile();
        scan(rootDir);
        findFailures();
    }

    public void scan(File dir) {
        if (dir.getName().equals("surefire-reports")) {
            sureFireReportDirs.add(dir);
        } else if (!dir.getName().startsWith(".")) {
            File[] subdirectories = dir.listFiles(File::isDirectory);
            if (subdirectories != null) {
                for (File d : subdirectories) {
                    scan(d);
                }
            }
        }
    }

    public void findFailures() {
        for (File sureFireReportDir : sureFireReportDirs) {
            File[] txtReports = sureFireReportDir.listFiles(d -> d.getName().endsWith(".txt"));
            if (txtReports != null) {
                Arrays.stream(txtReports).forEach(this::scanTxtReport);
            }
        }
    }

    public void scanTxtReport(File txtReport) {
        String srcPath = txtReport.getParentFile().getParentFile().getParentFile().getAbsolutePath().substring(rootDir.getAbsolutePath().length() + 1) + "/src/test/java";

        try (BufferedReader br = new BufferedReader(new FileReader(txtReport))) {
            for (int i = 0; i < 3; i++) {
                br.readLine();
            }

            if (br.readLine().contains(" <<< FAILURE")) {
                for (String l = br.readLine(); l != null; l = br.readLine()) {
                    if (l.contains("<<< FAILURE!") || l.contains("<<< ERROR!")) {
                        String test = l.substring(0, l.indexOf(" -- "));

                        List<String> failure = new LinkedList<>();
                        for (l = br.readLine(); !l.trim().isBlank(); l = br.readLine()) {
                            failure.add(l);
                        }

                        String message = failure.get(0);

                        String last = failure.getLast().trim();

                        String file = last.substring(last.indexOf(' ') + 1, last.indexOf('('));
                        file = file.substring(0, file.lastIndexOf('.')).replace('.', '/') + ".java";
                        file = srcPath + "/" + file;

                        String line = last.substring(last.indexOf(':') + 1, last.indexOf(')'));

                        System.out.print("::error ");
                        System.out.print("file=" + file + ",");
                        System.out.print("line=" + line + ",");
                        System.out.print("title=" + test + "::");
                        System.out.println(message);
                    }
                }
            }
        } catch (IOException e) {
        }
    }

}
