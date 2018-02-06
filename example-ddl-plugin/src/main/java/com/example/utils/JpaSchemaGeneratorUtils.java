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

package com.example.utils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.example.JpaSchemaGeneratorPlugin;
import com.example.model.Vendor;
import org.apache.commons.lang.StringUtils;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.dialect.internal.StandardDialectResolver;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;
import org.hibernate.jpa.AvailableSettings;

import javax.persistence.spi.PersistenceProvider;
import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Optional.ofNullable;
import static org.springframework.util.StringUtils.isEmpty;

/**
 * This class is an utility one for {@link PersistenceProvider}'s properties configuration and constants definition.
 * <p>{@link JpaSchemaGeneratorPlugin} uses {@link PersistenceProvider} as an underlying tool for script generation.
 * This provider needs some properties like JPA entity package to scan, JDBC data, database dialect etc. in order to
 * know how to write correct SQL DDL script. Each database has its own dialect that respects some conventions but inner
 * data types or some options are not always available. That is why we need to know which database is used and where JPA
 * entities are in order to generate a create and drop script.
 * </p>
 */
public final class JpaSchemaGeneratorUtils {

    public static final Map<String, String> LINE_SEPARATOR_MAP;
    public static final Map<Vendor, Class<? extends PersistenceProvider>> PROVIDER_MAP;

    static {
        LINE_SEPARATOR_MAP = ImmutableMap.<String, String>builder()
                .put("CR", "\r")
                .put("LF", "\n")
                .put("CRLF", "\r\n")
                .build();

        PROVIDER_MAP = ImmutableMap.<Vendor, Class<? extends PersistenceProvider>>builder()
                .put(Vendor.HIBERNATE, org.hibernate.jpa.HibernatePersistenceProvider.class).build();
    }

    private JpaSchemaGeneratorUtils() {
        //prevent instantiation
    }

    /**
     * Build {@link PersistenceProvider}'s properties map in order to generate create and/or drop schema script.
     *
     * @param mojo plugin implementation for DDL script generation
     * @return a {@link Map} holding all necessaries properties for file generation.
     */
    public static Map<String, Object> buildProperties(JpaSchemaGeneratorPlugin mojo) {
        Map<String, Object> result = new HashMap<>();
        Map<String, String> properties = mojo.getProperties();

        handleMode(mojo, result);

        handleOutputFiles(mojo, result);

        handleDatabaseEmulation(mojo, result);

        handleJdbc(mojo, result);

        // Hibernate specific auto-detect
        result.put(org.hibernate.cfg.AvailableSettings.SCANNER_DISCOVERY, "class,hbm");

        handleDialect(mojo, result, properties);

        handleNamingStrategy(mojo, result);

        result.putAll(properties);

        // force override JTA to RESOURCE_LOCAL
        result.put(PersistenceUnitProperties.TRANSACTION_TYPE, "RESOURCE_LOCAL");
        result.put(PersistenceUnitProperties.VALIDATION_MODE, "NONE");

        // normalize - remove null - can happen if mojo contains null properties
        ImmutableSet.copyOf(result.keySet()).stream().filter(key -> result.get(key) == null).forEach(result::remove);

        return result;
    }

    private static void handleNamingStrategy(JpaSchemaGeneratorPlugin mojo, Map<String, Object> result) {
        ofNullable(mojo.getNamingStrategy())
                .filter(StringUtils::isNotBlank)
                .ifPresent(strategy -> result.put(org.hibernate.cfg.AvailableSettings.PHYSICAL_NAMING_STRATEGY, strategy));
    }

    private static void handleDialect(JpaSchemaGeneratorPlugin mojo,
                                      Map<String, Object> result,
                                      Map<String, String> properties) {
        //trying to get explicite dialect from Mojo properties
        Optional<String> dialect = ofNullable(properties.get(org.hibernate.cfg.AvailableSettings.DIALECT));

        // automatic dialect resolution for undefined jdbc connection
        if ((!dialect.isPresent() || isEmpty(dialect.get())) && isEmpty(mojo.getJdbcUrl())) {

            DialectResolutionInfo info = new DDLDialectResolutionInfo(
                    checkNotNull(mojo.getDatabaseProductName(),
                            "DatabaseProductName property is required when no jdbc data are provided"),
                    mojo.getDatabaseMajorVersion(),
                    mojo.getDatabaseMinorVersion());

            dialect = ofNullable(StandardDialectResolver.INSTANCE.resolveDialect(info))
                    .map(Dialect::getClass)
                    .map(Class::getName);
        }

        dialect.ifPresent(d -> {
            properties.remove(org.hibernate.cfg.AvailableSettings.DIALECT);
            result.put(org.hibernate.cfg.AvailableSettings.DIALECT, d);
        });
    }

    private static void handleJdbc(JpaSchemaGeneratorPlugin mojo, Map<String, Object> result) {
        ofNullable(mojo.getJdbcDriver()).ifPresent(j -> result.put(PersistenceUnitProperties.JDBC_DRIVER, j));
        ofNullable(mojo.getJdbcUser()).ifPresent(j -> result.put(PersistenceUnitProperties.JDBC_USER, j));
        ofNullable(mojo.getJdbcPassword()).ifPresent(j -> result.put(PersistenceUnitProperties.JDBC_PASSWORD, j));

        String jdbcUrl = mojo.getJdbcUrl();
        if (isEmpty(jdbcUrl)) {
            result.put(AvailableSettings.SCHEMA_GEN_CONNECTION,
                    new ConnectionMock(
                            checkNotNull(mojo.getDatabaseProductName(),
                                    "DatabaseProductName property is required when no jdbc data are provided"),
                            mojo.getDatabaseMajorVersion(),
                            mojo.getDatabaseMinorVersion()));
        } else {
            result.put(PersistenceUnitProperties.JDBC_URL, jdbcUrl);
        }
    }

    private static void handleMode(JpaSchemaGeneratorPlugin mojo, Map<String, Object> result) {
        result.put(PersistenceUnitProperties.SCHEMA_GENERATION_DATABASE_ACTION, "none");
        result.put(PersistenceUnitProperties.SCHEMA_GENERATION_SCRIPTS_ACTION, mojo.getScriptAction().toLowerCase());
    }

    private static void handleOutputFiles(JpaSchemaGeneratorPlugin mojo, Map<String, Object> result) {
        mojo.getCreateOutputFile().map(File::toURI).map(URI::toString)
                .ifPresent(c -> result.put(PersistenceUnitProperties.SCHEMA_GENERATION_SCRIPTS_CREATE_TARGET, c));
        mojo.getDropOutputFile().map(File::toURI).map(URI::toString)
                .ifPresent(c -> result.put(PersistenceUnitProperties.SCHEMA_GENERATION_SCRIPTS_DROP_TARGET, c));
    }

    private static void handleDatabaseEmulation(JpaSchemaGeneratorPlugin mojo, Map<String, Object> result) {
        ofNullable(mojo.getDatabaseProductName())
                .ifPresent(p -> result.put(PersistenceUnitProperties.SCHEMA_DATABASE_PRODUCT_NAME, p));
        ofNullable(mojo.getDatabaseMajorVersion()).map(String::valueOf)
                .ifPresent(p -> result.put(PersistenceUnitProperties.SCHEMA_DATABASE_MAJOR_VERSION, p));
        ofNullable(mojo.getDatabaseMinorVersion()).map(String::valueOf)
                .ifPresent(p -> result.put(PersistenceUnitProperties.SCHEMA_DATABASE_MINOR_VERSION, p));
    }
}
