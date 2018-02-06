package com.example;

import com.google.common.annotations.VisibleForTesting;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Plugin that create liquibase master database changelog file.
 * This plugin walk through resources/sql maven sub folder and collect all liquibase formated sql files.
 * Those files will be automatically added to liquibase master database changelog file through <include /> tag.
 *
 * @goal generateChangeLog
 */
public class LiquibasePlugin extends AbstractMojo {
    public static final String SPRING_BOOT_DEFAULT_CHANGE_LOG_MASTER_FILE_NAME = "db.changelog-master.xml";

    private static final String SQL_FOLDER = "sql";
    private static final String SQL_FILE_EXTENSION = ".sql";
    private static final String LIQUIBASE_SQL_FILE_HEADER = "--liquibase formatted sql";
    private static final String SPRING_BOOT_DEFAULT_CHANGE_LOG_MASTER_FILE_LOCATION = "/target/classes/db/changelog";

    private static final String LIQUIBASE_NS_URI = "http://www.liquibase.org/xml/ns/dbchangelog";
    private static final String XML_SCHEMA_NS_URI = "http://www.w3.org/2001/XMLSchema-instance";

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            MavenProject project = (MavenProject) getPluginContext().get("project");

            Set<String> sqlFiles = project.getResources().stream()
                    .flatMap(this::collectSqlFiles)
                    .collect(Collectors.toCollection(TreeSet::new));

            getLog().info("Changesets included in the master changelog: " + sqlFiles);

            Path outputFolder = Paths.get(project.getBasedir().getAbsolutePath(),
                    SPRING_BOOT_DEFAULT_CHANGE_LOG_MASTER_FILE_LOCATION);

            writeOutputFile(sqlFiles, outputFolder);

            getLog().info("Master database changelog successfully written ( File : " +
                    Paths.get(outputFolder.toString(), SPRING_BOOT_DEFAULT_CHANGE_LOG_MASTER_FILE_NAME) + " )");

        } catch (IOException | XMLStreamException | IllegalArgumentException e) {
            getLog().error("Encountered error:", e);
            throw new MojoFailureException("Encountered an error durring processing", e);
        }
    }

    /**
     * Write out liquibase xml master database changelog into /target/classes/db/changelog
     *
     * @param sqlFiles sql files to be include into liquibase xml file
     */
    @VisibleForTesting
    void writeOutputFile(Iterable<String> sqlFiles, Path outputFolder) throws IOException, XMLStreamException {

        if (!Files.exists(outputFolder) && !outputFolder.toFile().mkdirs()) {
            throw new IllegalStateException("Cannot create " + outputFolder.toString());
        }
        try (FileOutputStream stream =
                     new FileOutputStream(Paths.get(outputFolder.toString(),
                             SPRING_BOOT_DEFAULT_CHANGE_LOG_MASTER_FILE_NAME).toFile())) {

            String encoding = StandardCharsets.UTF_8.name();
            XMLStreamWriter xsw = XMLOutputFactory.newInstance().createXMLStreamWriter(stream, encoding);
            xsw.writeStartDocument(encoding, "1.0");
            xsw.writeStartElement("databaseChangeLog");
            xsw.writeDefaultNamespace(LIQUIBASE_NS_URI);
            xsw.writeNamespace("xsi", XML_SCHEMA_NS_URI);
            xsw.setPrefix("xsi", XML_SCHEMA_NS_URI);
            xsw.writeAttribute(XML_SCHEMA_NS_URI, "schemaLocation",
                    "http://www.liquibase.org/xml/ns/dbchangelog " +
                            "http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-2.0.xsd");

            for (String file : sqlFiles) {
                xsw.writeEmptyElement("include");
                xsw.writeAttribute("file", file);
                xsw.writeAttribute("relativeToChangelogFile", "false");
            }

            xsw.writeEndElement();
            xsw.writeEndDocument();
            xsw.close();
        }
    }

    /**
     * Inner method for valid liquibase sql file collect
     *
     * @param resource resource directory available in project
     */
    @VisibleForTesting
    Stream<String> collectSqlFiles(Resource resource) {
        final Path sqlPath = Paths.get(resource.getDirectory(), SQL_FOLDER);

        if (Files.isDirectory(sqlPath)) {

            final Path sqlPathAbsolute = sqlPath.toAbsolutePath();
            final int prefixPathLength = sqlPathAbsolute.toString().length() + 1;

            try {
                return Files.walk(sqlPathAbsolute)
                        .filter(this::isValidFile)
                        .map(f -> f.toString().substring(prefixPathLength))
                        .map(s -> SQL_FOLDER + "/" + s);

            } catch (IOException e) {
                getLog().error("Cannot walk through sub file system " + sqlPathAbsolute, e);
                throw new IllegalArgumentException("Cannot walk through sub file system " + sqlPathAbsolute, e);
            }
        }

        return Stream.empty();
    }

    private boolean isValidFile(Path path) {
        if (!Files.isRegularFile(path) || !path.toFile().getName().endsWith(SQL_FILE_EXTENSION)) {
            return false;
        }

        try (BufferedReader reader = Files.newBufferedReader(path, Charset.defaultCharset())) {

            String lineFromFile = reader.readLine();
            return LIQUIBASE_SQL_FILE_HEADER.equals(lineFromFile);

        } catch (IOException e) {
            getLog().error("Cannot read file " + path.toAbsolutePath(), e);
            throw new IllegalArgumentException("Cannot read file " + path.toAbsolutePath(), e);
        }
    }
}
