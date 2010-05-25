/*
 * Copyright 2009 Toni Menzel.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ops4j.pax.exam.mavenplugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.DefaultArtifactCollector;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;

/**
 * @author Toni Menzel (tonit)
 * @goal generate-config
 * @phase generate-resources
 * @since Mar 17, 2009
 */
public class GenerateConfigMojo
        extends AbstractMojo {

    protected static final String SEPARATOR = "/";

    private static final String SETTINGS_DEPENDENCY_OPTIONS =
            "dependency_options";

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * The file to generate
     *
     * @parameter default-value=
     *            "${project.build.directory}/test-classes/META-INF/maven/paxexam-config.args"
     */

    private File configOutput;

    /**
     * @parameter default-value="provided"
     */
    private String dependencyScope;

    /**
     * pax runner arguments defined in <options> tag in configuration of plugin.
     *
     * @parameter default-value="{}"
     */
    private Map<String, String> options;

    /**
     * settings for this plugin in <settings> tag.
     *
     * @parameter default-value="{}"
     */
    private Map<String, String> settings;

    /**
     * @component
     */
    protected ArtifactMetadataSource artifactMetadataSource;

    /**
     * @component
     */
    protected ArtifactResolver resolver;

    protected ArtifactCollector collector = new DefaultArtifactCollector();

    /**
     * @component
     */
    protected ArtifactFactory factory;

    /**
     * @parameter expression="${project.remoteArtifactRepositories}"
     */
    private List<ArtifactRepository> remoteRepositories;

    /**
     * The Maven Session Object
     *
     * @parameter expression="${session}"
     * @required
     * @readonly
     */
    private MavenSession session;

    /**
     * @parameter default-value="module"
     */
    private String dependencyCollectionScope;

    /** @component */
    private MavenProjectBuilder mavenProjectBuilder;

    /** @component */
    private org.apache.maven.artifact.factory.ArtifactFactory artifactFactory;

    private DependencyCollector dependencyCollector;

    public void execute() throws MojoExecutionException, MojoFailureException {
        OutputStream out = null;
        try {
            configOutput.getParentFile().mkdirs();
            out = new FileOutputStream(configOutput);
            PrintStream printer = new PrintStream(out);

            List<Dependency> dependencies;
            dependencies = getProvisionableDependencies();

            writeHeader(printer);
            writeProvisioning(printer, dependencies);
            writeSettings(printer);

            getLog()
                    .info("Generated configuration as Pax Runner arguments file "
                            + configOutput);
        } catch (Exception e) {
            throw new MojoExecutionException("Unable to create dependencies file: "
                                                     + e,
                                             e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    getLog().info("Failed to close: " + configOutput
                                          + ". Reason: " + e,
                                  e);
                }
            }
        }
    }

    private void writeHeader(PrintStream out) {
        out.println("# Configurations generated by the Pax Exam Maven Plugin");
        out.println("# Generated at: " + new Date());
        out.println();
        out.println("# groupId = " + project.getGroupId());
        out.println("# artifactId = " + project.getArtifactId());
        out.println("# version = " + project.getVersion());
        out.println("# " + project.getGroupId() + SEPARATOR
                + project.getArtifactId() + SEPARATOR + "version = "
                + project.getVersion());
        out.println();
    }

    private void writeSettings(PrintStream out) {
        out.println("# Settings parsed from pom.xml in settings of plugin");
        for (String key : options.keySet()) {
            out.println("--" + key + "=" + options.get(key));
        }
        out.println();
    }

    /**
     * Dependency resolution inspired by servicemix depends-maven-plguin
     *
     * @return list of dependencies to be written to disk.
     * @throws MojoExecutionException
     */
    private List<Dependency> getProvisionableDependencies()
            throws MojoExecutionException {
        List<Dependency> dependencies = new ArrayList<Dependency>();
        getLog().info("Adding dependencies in scope " + dependencyScope);
        for (Dependency d : getDependencies()) {
            if (d.getScope() != null
                    && d.getScope().equalsIgnoreCase(dependencyScope)) {
                dependencies.add(d);
            }
        }

        return dependencies;
    }

    @SuppressWarnings("unchecked")
    private List<Dependency> getDependencies() throws MojoExecutionException {
        return getDependencyCollector().getDependencies();
    }

    protected void writeProvisioning(PrintStream out,
                                     List<Dependency> dependencies)
            throws ArtifactResolutionException, ArtifactNotFoundException {
        out.println("# provisioning");
        out.println();

        for (Dependency dependency : dependencies) {
            Artifact artifact =
                    factory
                            .createDependencyArtifact(dependency.getGroupId(),
                                                      dependency
                                                              .getArtifactId(),
                                                      VersionRange
                                                              .createFromVersion(dependency
                                                                      .getVersion()),
                                                      dependency.getType(),
                                                      dependency
                                                              .getClassifier(),
                                                      dependency.getScope());

            // try to find
            boolean found = false;
            for (MavenProject project : (List<MavenProject>) session
                    .getSortedProjects()) {

                Artifact projectArtifact = project.getArtifact();
                if (projectArtifact.getArtifactId().equals(artifact
                        .getArtifactId())
                        && (projectArtifact.getGroupId().equals(artifact
                                .getGroupId()) && projectArtifact.getVersion()
                                .equals(artifact.getVersion()))) {
                    artifact = projectArtifact;
                    found = true;
                    break;
                }

            }

            if (!found) {
                resolver.resolve(artifact, remoteRepositories, session
                        .getLocalRepository());
            }

            out
                    .println(

                    createPaxRunnerScan(artifact,
                                        getSettingsForArtifact(settings
                                                                       .get(SETTINGS_DEPENDENCY_OPTIONS),
                                                               artifact
                                                                       .getGroupId(),
                                                               artifact
                                                                       .getArtifactId())));

            getLog().debug("Dependency: " + dependency + " classifier: "
                    + dependency.getClassifier() + " type: "
                    + dependency.getType());
        }
        out.println();
    }

    /**
     * Example: getSettingsForArtifact (
     * "foo:bar@1,chees:ham2@3@nostart","cheese","ham") --> @3@nostart
     *
     * @param fullSettings
     *        settings separated by comma. GA patter + @options
     * @param groupId
     *        GA part groupId to be matched inside fulllSettings
     * @param artifactId
     *        GA part artifactId to be matched inside fulllSettings
     * @return option portion of matched part in fullSettings or empty string if
     *         no matching.
     */
    public String getSettingsForArtifact(String fullSettings,
                                         String groupId,
                                         String artifactId) {
        if (fullSettings != null) {
            for (String token : fullSettings.split(",")) {
                int end =
                        (token.indexOf("@") >= 0) ? token.indexOf("@") : token
                                .length();
                String ga_part[] = token.substring(0, end).split(":");
                if (ga_part[0].equals(groupId) && ga_part[1].equals(artifactId)) {
                    return token.substring(end);
                }

            }
        }
        return "";
    }

    /**
     * Creates scanner directives from artifact to be parsed by pax runner. Also
     * includes options found and matched in settings part of configuration.
     *
     * @param artifact
     *        to be used to create scanner directive.
     * @param optionTokens
     *        to be used to create scanner directive.
     * @return pax runner compatible scanner directive.
     */
    private String createPaxRunnerScan(Artifact artifact, String optionTokens) {
        return "scan-bundle:"
                + artifact.getFile().toURI().normalize().toString() + "@update"
                + optionTokens;
    }

    private DependencyCollector getDependencyCollector()
            throws MojoExecutionException {
        if (null == dependencyCollector) {
            if ("module".equals(dependencyCollectionScope)) {
                dependencyCollector =
                        new SingleModuleDependencyCollector(project);
            } else if ("transitive".equals(dependencyCollectionScope)) {
                ArtifactRepository localRepo = session.getLocalRepository();
                dependencyCollector =
                        new TransitiveDependencyCollector(project,
                                                          artifactFactory,
                                                          mavenProjectBuilder,
                                                          remoteRepositories,
                                                          localRepo);
            } else {
                throw new MojoExecutionException("Unknown dependency collection scope: "
                        + dependencyCollectionScope);
            }
        }
        return dependencyCollector;
    }

}