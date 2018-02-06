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

import lombok.AllArgsConstructor;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;

/**
 * Gather data in order to use {@code StandardDialectResolver} for dialect resolution.
 * <p>If automatic dialect resolution is used, it means that no jdbc url is provided in plugin configuration.
 * In this case, carnet ddl plugin managed to resolve dialect to use using database prodcut name and version. Version
 * is split into major and minor part. Those three information will next be given to {@code StandardDialectResolver} to
 * get the more accurate dialect for script generation.</p>
 * <p>In order to give {@code DDLDialectResolutionInfo} to {@code StandardDialectResolver} this class need to implement
 * {@code DialectResolutionInfo} which is tje input argument type of {@code StandardDialectResolver}.</p>
 */
@AllArgsConstructor
final class DDLDialectResolutionInfo implements DialectResolutionInfo {
    private final String productName;
    private final Integer majorVersion;
    private final Integer minorVersion;

    @Override
    public String getDriverName() {
        return null;
    }

    @Override
    public int getDriverMinorVersion() {
        return 0;
    }

    @Override
    public int getDriverMajorVersion() {
        return 0;
    }

    @Override
    public String getDatabaseName() {
        return productName;
    }

    @Override
    public int getDatabaseMinorVersion() {
        return minorVersion == null ? 0 : minorVersion;
    }

    @Override
    public int getDatabaseMajorVersion() {
        return majorVersion == null ? 0 : majorVersion;
    }
}
