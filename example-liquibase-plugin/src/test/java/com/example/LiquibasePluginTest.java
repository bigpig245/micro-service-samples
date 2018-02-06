package com.example;

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.resources.TestResources;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class LiquibasePluginTest {

    @Rule
    public MojoRule rule = new MojoRule();

    @Rule
    public TestResources resources = new TestResources();

    @Test
    public void should_retrieve_valid_sql_changelog() throws Exception {
        File pom = getExistingTestPom();

        LiquibasePlugin myMojo = (LiquibasePlugin) rule.lookupMojo("generateChangeLog", pom);

        Resource resource = new Resource();
        resource.setDirectory("src/test/resources");

        Collection<String> strings =myMojo.collectSqlFiles(resource).collect(Collectors.toCollection(TreeSet::new));

        assertThat(strings).containsExactly("sql/db.changelog-0.1.sql", "sql/db.changelog-0.2.sql", "sql/v0.3/db.changelog-0.3.sql");

    }

    @Test
    public void should_write_out_liquibase_master_file() throws Exception {
        File pom = getExistingTestPom();

        LiquibasePlugin myMojo = (LiquibasePlugin) rule.lookupMojo("generateChangeLog", pom);

        myMojo.writeOutputFile(
            ImmutableList.of("sql/db.changelog-0.1.sql", "sql/db.changelog-0.2.sql", "sql/v0.3/db.changelog-0.3.sql"),
            Paths.get("target/test-classes/sql"));

        String s = FileUtils.readFileToString(
            Paths.get("target/test-classes/sql", LiquibasePlugin.SPRING_BOOT_DEFAULT_CHANGE_LOG_MASTER_FILE_NAME).toFile());

        assertThat(s).contains("<include file=\"sql/db.changelog-0.1.sql\"")
            .contains("<include file=\"sql/db.changelog-0.2.sql\"")
            .contains("<include file=\"sql/v0.3/db.changelog-0.3.sql\"")
            .contains("<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"")
            .contains("xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/")
            .contains("</databaseChangeLog>");
    }

    private File getExistingTestPom() throws IOException {
        File projectCopy = resources.getBasedir("project-to-test");
        File pom = new File(projectCopy, "pom.xml");
        assertThat(pom.exists()).isTrue();
        return pom;
    }
}
