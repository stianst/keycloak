package org.keycloak.licenses;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;

import javax.inject.Provider;
import java.io.File;
import java.util.List;


@Mojo(name = "license-report", defaultPhase = LifecyclePhase.INSTALL)
public class LicenseReportMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Parameter(property = "additionalConfig", required = true)
    private File additionalConfig;

    @Parameter(property = "licensesDir", required = true)
    private File licensesDir;

    @Parameter(property = "reports")
    private File[] reports;

    @Parameter(property = "mvnReports")
    private File[] mvnReports;

    @Parameter(property = "pnpmReports")
    private File[] pnpmReports;
@Component
private MavenSession mavenSessionProvider;
    @Component
    private ProjectBuilder mavenProjectBuilder;
    @Parameter(defaultValue = "${project.remoteArtifactRepositories}", readonly = true)
    protected List<ArtifactRepository> remoteRepositories;
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        ProjectBuildingRequest projectBuildingRequest = new DefaultProjectBuildingRequest(
                mavenSessionProvider.getProjectBuildingRequest())
                .setRemoteRepositories(remoteRepositories)
                .setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL)
                .setResolveDependencies(false)
                .setProcessPlugins(false);


        getLog().info(mavenProjectBuilder.toString());
        for (Artifact a : project.getDependencyArtifacts()) {
            getLog().info(a.toString());
            try {
                ProjectBuildingResult build = mavenProjectBuilder.build(a, true, projectBuildingRequest);
                getLog().info(build.getProject().getLicenses().get(0).getName());
            } catch (ProjectBuildingException e) {
                throw new RuntimeException(e);
            }
        }

        File outputDir = project.getBasedir().toPath().resolve("target").resolve("licenses").toFile();

        getLog().info("Additional config: " + additionalConfig.getAbsolutePath());

        LicensesReport licensesReport = new LicensesReport(additionalConfig);

        for (File f : mvnReports) {
            try {
                getLog().info("Parsing Maven licenses from: " + f.getCanonicalFile().getAbsolutePath());
                licensesReport.parseMaven(f);
            } catch (Exception e) {
                throw new MojoFailureException("Failed to parse Maven license file", e);
            }
        }
        for (File f : pnpmReports) {
            try {
                getLog().info("Parsing PNPM licenses from: " + f.getCanonicalFile().getAbsolutePath());
                licensesReport.parsePnpm(f);
            } catch (Exception e) {
                throw new MojoFailureException("Failed to parse PNPM license file", e);
            }
        }

        try {
            licensesReport.createReport(outputDir, licensesDir, false, reports);

            getLog().info("Report: " + new File(outputDir, "report.html").toURI().toURL());
            getLog().info("CNCF Report: " + new File(outputDir, "cncf-report.html").toURI().toURL());
        } catch (Exception e) {
            throw new MojoFailureException("Failed to generate report", e);
        }


    }
}
