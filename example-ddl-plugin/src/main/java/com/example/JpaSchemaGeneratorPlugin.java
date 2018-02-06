/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.example;

import com.example.model.Vendor;
import com.example.utils.JpaSchemaGeneratorUtils;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager;
import org.springframework.orm.jpa.persistenceunit.SmartPersistenceUnitInfo;

import javax.persistence.spi.PersistenceProvider;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.utils.JpaSchemaGeneratorUtils.LINE_SEPARATOR_MAP;
import static com.example.utils.JpaSchemaGeneratorUtils.PROVIDER_MAP;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Generator for DDL scripts.
 */
@Mojo(name = "generateSchema", defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class JpaSchemaGeneratorPlugin extends AbstractMojo {

    private final Log log = this.getLog();

    @Parameter(defaultValue = "${session}", readonly = true)
    @Setter
    private MavenSession session;

    @Parameter(defaultValue = "${project}", readonly = true)
    @Setter
    private MavenProject project;

    @Component
    private ArtifactResolver resolver;

    /**
     * <p>
     * skip schema generation
     * </p>
     */
    @Parameter(property = "jpa-schema.generate.skip", required = true, defaultValue = "false")
    @Getter
    private boolean skip = false;

    @Parameter(property = "jpa-schema.generate.scan-test-classes", required = true, defaultValue = "false")
    @Getter
    private boolean scanTestClasses = false;

    /**
     * <p>
     * unit name of default autoconfiguration persistence unit
     * </p>
     */
    @Parameter(required = true, defaultValue = "default")
    @Getter
    private String persistenceUnitName = "default";

    /**
     * <p>
     * schema generation action for script
     * </p>
     * <p>
     * support value is {@code none}, {@code create}, {@code drop}, or {@code drop-and-create}.
     * </p>
     */
    @Parameter(required = true, defaultValue = PersistenceUnitProperties.SCHEMA_GENERATION_NONE_ACTION)
    @Getter
    @NonNull
    private String scriptAction = PersistenceUnitProperties.SCHEMA_GENERATION_NONE_ACTION;

    /**
     * <p>
     * output directory for generated ddl scripts
     * </p>
     * <p></p>
     * <b>Required for script generation. default value is ${project.build.directory}/generated-schema</b>
     * </p>
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-schema")
    @Getter
    @NonNull
    private File outputDirectory;

    /**
     * <p>
     * generated create script name
     * </p>
     * <p>
     * <b>Required for script generation. default value is <u>create.sql</u></b>
     * </p>
     */
    @Parameter(defaultValue = "create.sql")
    @Getter
    private String createOutputFileName = "create.sql";

    /**
     * <p>
     * generated drop script name
     * </p>
     * <p>
     * <b>Required for script generation. default value is <u>drop.sql</u></b>
     * </p>
     */
    @Parameter(defaultValue = "drop.sql")
    @Getter
    private String dropOutputFileName = "drop.sql";

    /**
     * <p>
     * jdbc driver class name
     * </p>
     */
    @Parameter
    @Getter
    private String jdbcDriver;

    /**
     * <p>
     * jdbc connection url
     * </p>
     */
    @Parameter
    @Getter
    private String jdbcUrl;

    /**
     * <p>
     * jdbc connection username
     * </p>
     */
    @Parameter
    @Getter
    private String jdbcUser;

    /**
     * <p>
     * jdbc connection password
     * </p>
     */
    @Parameter
    @Getter
    private String jdbcPassword;

    /**
     * <p>
     * database product name for emulate database connection.
     * </p>
     * <p>
     * <ul>
     * <li><b>Must be specified if connection to the target database is not supplied.</b></li>
     * <li>The value of this property must be the value returned for the target database by
     * {@link DatabaseMetaData#getDatabaseProductName()}</li>
     * </ul>
     * </p>
     * <p>
     * automatic dialect resolver accept :
     * <ul>
     * <li>Firebird</li>
     * <liI></liI>
     * <li>HDB</li>
     * <li>Oracle12</li>
     * <li>Oracle11</li>
     * <li>Oracle10</li>
     * <li>Oracle9</li>
     * <li>Oracle</li>
     * <li>DB2/</li>
     * <li>DB2 UDB for AS/400</li>
     * <li>Informix Dynamic Server</li>
     * <li>Adaptive Server Anywhere</li>
     * <li>Adaptive Server Enterprise</li>
     * <li>Sybase SQL Server</li>
     * <li>Microsoft SQL Server</li>
     * <li>ingres</li>
     * <li>Apache Derby</li>
     * <li>EnterpriseDB</li>
     * <li>PostgreSQL</li>
     * <li>MySQL</li>
     * <li>H2</li>
     * <li>CUBRID</li>
     * <li>HSQL Database Engine</li>
     * </ul>
     * </p>
     */
    @Parameter
    @Getter
    private String databaseProductName;

    /**
     * <p>
     * database major version for emulate database connection.
     * </p>
     * <p>
     * <ul>
     * <li><b>Must be specified if sufficient database version information is not included from
     * {@link DatabaseMetaData#getDatabaseProductName()}</b></li>
     * <li>The value of this property should be the value returned for the target database by
     * {@link DatabaseMetaData#getDatabaseMajorVersion()}</li>
     * </ul>
     * </p>
     */
    @Parameter
    @Getter
    private Integer databaseMajorVersion;

    /**
     * database minor version for emulate database connection.
     * <ul>
     * <li><b>Must be specified if sufficient database version information is not included from
     * {@link DatabaseMetaData#getDatabaseProductName()}</b></li>
     * <li>The value of this property should be the value returned for the target database by
     * {@link DatabaseMetaData#getDatabaseMinorVersion()}</li>
     * </ul>
     */
    @Parameter
    @Getter
    private Integer databaseMinorVersion;

    /**
     * <p>
     * line separator for generated schema file.
     * </p>
     * <p>
     * support value is one of {@code CRLF} (windows default), {@code LF} (*nix, max osx), and {@code CR}
     * (classic mac), in case-insensitive.
     * </p>
     * <p>
     * default value is system property {@code line.separator}. if JVM cannot detect {@code line.separator},
     * then use {@code LF} by <a href="http://git-scm.com/book/en/Customizing-Git-Git-Configuration">git
     * {@code core.autocrlf} handling</a>.
     * </p>
     */
    @Parameter
    private String lineSeparator = System.getProperty("line.separator", "\n");

    /**
     * <p>
     * JPA vendor specific properties.
     * </p>
     * <p>
     * This parameters can be use to define explicitly some overiding properties like :
     * <ul>
     * <li>hibernate.dialect. please ref to {@link org.hibernate.dialect} to see which ones are available</li>
     * </ul>
     * </p>
     */
    @Parameter
    @Getter
    private Map<String, String> properties = new HashMap<>();

    /**
     * <p>
     * JPA vendor name of vendor's {@link PersistenceProvider} implemention.
     * </p>
     * <p>
     * vendor name is one of
     * <ul>
     * <li>{@code HIBERNATE}</li>
     * </ul>
     * </p>
     * <p>
     * <b>REQUIRED for project without {@code persistence.xml}</b>
     * </p>
     */
    @Parameter
    @Getter
    private Vendor vendor;

    /**
     * <p>
     * list of package name for scan entity classes
     * </p>
     * <p>
     * <b>REQUIRED for project without {@code persistence.xml}</b>
     * </p>
     */
    @Parameter
    @Getter
    private List<String> packageToScan = new ArrayList<>();

    /**
     * <p>
     * Used to specify the {@link org.hibernate.boot.model.naming.PhysicalNamingStrategy} class to use.
     * </p>
     * <p>
     * autorized values are :
     * <ul>
     * <li>org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl</li>
     * <li>org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy</li>
     * </ul>
     * </p>
     * <p>
     * default value is {@link org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy}
     * </p>
     */
    @Parameter
    @Getter
    private String namingStrategy = "org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy";

    public Optional<File> getCreateOutputFile() {
        return ofNullable(createOutputFileName).map(o -> new File(outputDirectory, createOutputFileName));
    }

    public Optional<File> getDropOutputFile() {
        return ofNullable(dropOutputFileName).map(o -> new File(outputDirectory, dropOutputFileName));
    }

    public Class<? extends PersistenceProvider> getProviderClass() {
        return PROVIDER_MAP.get(vendor);
    }

    public String getLineSeparator() {
        String actual = StringUtils.isEmpty(lineSeparator) ? null : LINE_SEPARATOR_MAP.get(lineSeparator.toUpperCase());
        return actual == null ? System.getProperty("line.separator", "\n") : actual;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            log.info("schema generation skipped");
            return;
        }

        if (scriptAction == null) {
            throw new MojoExecutionException("Script action is required, please define one.");
        }

        if (outputDirectory == null) {
            throw new MojoExecutionException("Output directory is required, please define one.");
        }

        if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
            throw new MojoExecutionException("Cannot create output directory : " + outputDirectory.toString());
        }

        final ClassLoader classLoader = getProjectClassLoader();

        // driver load hack
        // http://stackoverflow.com/questions/288828/how-to-use-a-jdbc-driver-from-an-arbitrary-location
        if (isNotBlank(jdbcDriver)) {
            try {
                Driver driver = (Driver) classLoader.loadClass(jdbcDriver).newInstance();
                DriverManager.registerDriver(driver);
            } catch (ClassNotFoundException | IllegalAccessException | SQLException | InstantiationException e) {
                throw new MojoExecutionException(
                        String.format("Dependency for driver-class %s is missing!", jdbcDriver), e);
            }
        }

        Thread thread = Thread.currentThread();
        ClassLoader currentClassLoader = thread.getContextClassLoader();
        try {
            thread.setContextClassLoader(classLoader);
            generate();
        } catch (IllegalAccessException | InstantiationException e) {
            throw new MojoExecutionException("Error while generating DDL file", e);
        } finally {
            thread.setContextClassLoader(currentClassLoader);
        }
    }

    private ClassLoader getProjectClassLoader() throws MojoExecutionException {
        try {
            List<String> classfiles = project.getCompileClasspathElements();
            if (scanTestClasses) {
                classfiles.addAll(project.getTestClasspathElements());
            }
            Set<URL> classURLs = classfiles.stream()
                    .map(File::new)
                    .map(JpaSchemaGeneratorPlugin::mapFileToUrl)
                    .collect(Collectors.toSet());

            ArtifactRepository repository = session.getLocalRepository();
            ArtifactResolutionRequest sharedreq = new ArtifactResolutionRequest()
                    .setResolveRoot(true)
                    .setResolveTransitively(true)
                    .setLocalRepository(repository)
                    .setRemoteRepositories(project.getRemoteArtifactRepositories());

            project.getArtifacts().stream()
                    .filter(artifact -> !Artifact.SCOPE_TEST.equalsIgnoreCase(artifact.getScope()))
                    .filter(artifact -> canResolve(sharedreq, artifact))
                    // We can't use a method reference because repository can be null and the reference is resolved eagerly
                    .map(artifact -> repository.find(artifact))
                    .map(Artifact::getFile)
                    .filter(Objects::nonNull)
                    .map(JpaSchemaGeneratorPlugin::mapFileToUrl)
                    .forEach(classURLs::add);

            return new URLClassLoader(classURLs.toArray(new URL[classURLs.size()]), this.getClass().getClassLoader());

        } catch (DependencyResolutionRequiredException e) {
            log.error(e);
            throw new MojoExecutionException("Error while creating classloader", e);
        }
    }

    private static URL mapFileToUrl(File file) {
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    private boolean canResolve(ArtifactResolutionRequest sharedreq, Artifact artifact) {
        return resolver.resolve(new ArtifactResolutionRequest(sharedreq).setArtifact(artifact)).isSuccess();
    }

    private void generate() throws IllegalAccessException, InstantiationException {
        if (getVendor() == null) {
            throw new IllegalStateException("Vendor is required, please check your configuration");
        }

        List<String> packages = getPackageToScan();
        if (packages.isEmpty()) {
            throw new IllegalArgumentException("packageToScan is required, please check your configuration");
        }

        DefaultPersistenceUnitManager manager = new DefaultPersistenceUnitManager();
        manager.setDefaultPersistenceUnitName(getPersistenceUnitName());
        manager.setPackagesToScan(packages.toArray(new String[packages.size()]));
        manager.afterPropertiesSet();

        SmartPersistenceUnitInfo info = (SmartPersistenceUnitInfo) manager.obtainDefaultPersistenceUnitInfo();
        PersistenceProvider provider = getProviderClass().newInstance();
        info.setPersistenceProviderPackageName(provider.getClass().getName());
        Map<String, Object> map = JpaSchemaGeneratorUtils.buildProperties(this);
        info.getProperties().putAll(map);

        provider.generateSchema(info, map);

        getCreateOutputFile().ifPresent(this::reformatFileIfNecessary);
        getDropOutputFile().ifPresent(this::reformatFileIfNecessary);

    }

    private void reformatFileIfNecessary(File file) {

        if (file == null || !file.exists()) {
            return;
        }

        try {
            File tempFile = File.createTempFile("script", null, this.getOutputDirectory());

            try (Stream<String> stream = Files.lines(file.toPath());
                 BufferedWriter writer = Files.newBufferedWriter(tempFile.toPath())) {

                stream
                        .filter(StringUtils::isNotBlank)
                        .forEach(line -> writeLine(writer, line));

                writer.flush();

            } finally {
                if (!file.delete()) {
                    log.warn("Original file could not be deleted. Please try to delete it yourself (" +
                            file.toPath().toAbsolutePath() + ") after retrieving the script from " + tempFile.toPath());
                } else if (!tempFile.renameTo(file)) {
                    log.warn("Temporary file could not be renamed. Please try to rename it yourself (" +
                            tempFile.toPath().toAbsolutePath() + ")");
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("An error occurred while reformating file content", e);
        }
    }

    private void writeLine(BufferedWriter writer, String line) {
        final String linesep = getLineSeparator();
        String trimLine = line.trim();
        try {
            writer.write(trimLine);
            writer.write(";");
            writer.write(linesep);
        } catch (IOException e) {
            throw new IllegalStateException("Could not write the temporary file", e);
        }
    }
}
