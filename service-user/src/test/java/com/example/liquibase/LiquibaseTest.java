package com.example.liquibase;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Table;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LiquibaseTest {


    @Autowired
    private DataSource dataSource;

    private Database database;

    @Before
    public void setUp() throws Exception {

        database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(dataSource.getConnection()));

    }


    @Test
    public void should_find_table() throws Exception {

        database.setDefaultSchemaName("service_user");
        DatabaseObject[] databaseObjectExamples = new DatabaseObject[] { new Table() };
        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance()
                .createSnapshot(databaseObjectExamples, database, new SnapshotControl(database));

        List<String> result = snapshot.get(Table.class).stream()
                .map(DatabaseObject::getName)
                .collect(Collectors.toList());

        assertThat(result).contains("user");

    }
}
