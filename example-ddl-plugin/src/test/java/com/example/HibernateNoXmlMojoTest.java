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

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.resources.TestResources;
import org.apache.maven.project.MavenProject;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class HibernateNoXmlMojoTest {

    @Rule
    public MojoRule rule = new MojoRule();

    @Rule
    public TestResources resources = new TestResources();

    @Test
    public void testGenerateScriptUsingHibernate() throws Exception {
        File projectCopy = resources.getBasedir("hibernate-noxml-script-test");
        File pom = new File(projectCopy, "pom.xml");

        assertThat(pom.exists()).isTrue();

        String parent = pom.getParent();
        // create mojo

        JpaSchemaGeneratorPlugin mojo = (JpaSchemaGeneratorPlugin) rule.lookupMojo("generateSchema", pom);

        // configure project mock
        MavenProject projectMock = mock(MavenProject.class);
        doReturn(Collections.singletonList(parent + "/target/classes")).when(projectMock).getCompileClasspathElements();

        mojo.setProject(projectMock);
        mojo.setSession(rule.newMavenSession(projectMock));

        mojo.execute();

        File createScriptFile = mojo.getCreateOutputFile().get();
        assertThat(createScriptFile.exists()).isTrue();

        final String expectCreate = FileUtils.readFileToString(Paths.get("src/test/projects/hibernate-noxml-script-test/expected-create.txt").toFile());
        assertThat(FileUtils.readFileToString(createScriptFile)).isEqualTo(expectCreate);

        File dropScriptFile = mojo.getDropOutputFile().get();
        assertThat(dropScriptFile.exists()).isTrue();

        final String expectDrop = FileUtils.readFileToString(new File("src/test/projects/hibernate-noxml-script-test/expected-drop.txt"));
        assertThat(FileUtils.readFileToString(dropScriptFile)).isEqualTo(expectDrop);
    }
}
