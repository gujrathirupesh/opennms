/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.distributed.cassandra.impl;

import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;

import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.Statement;
import org.opennms.features.distributed.cassandra.api.CassandraSession;
import org.opennms.features.distributed.cassandra.api.CassandraSessionFactory;

/**
 * Serves the Cassandra session initiated by Newts by proxying it through our own {@link CassandraSession session}
 * object.
 */
public class NewtsCassandraSessionFactory implements CassandraSessionFactory {
    private final CassandraSession proxySession;

    public NewtsCassandraSessionFactory(org.opennms.newts.cassandra.CassandraSession newtsCassandraSession) {
        Objects.requireNonNull(newtsCassandraSession);

        // Map between our proxy session and the session owned by newts
        proxySession = NewtsCassandraSessionFactory.of(newtsCassandraSession);
    }

    public static CassandraSession of(org.opennms.newts.cassandra.CassandraSession newtsCassandraSession) {
        return new CassandraSession() {
            @Override
            public PreparedStatement prepare(String statement) {
                return newtsCassandraSession.prepare(statement);
            }

            @Override
            public PreparedStatement prepare(SimpleStatement statement) {
                return newtsCassandraSession.prepare(statement);
            }

            @Override
            public CompletionStage<AsyncResultSet> executeAsync(Statement statement) {
                return newtsCassandraSession.executeAsync(statement);
            }

            @Override
            public ResultSet execute(Statement statement) {
                return newtsCassandraSession.execute(statement);
            }

            @Override
            public ResultSet execute(String statement) {
                return newtsCassandraSession.execute(statement);
            }

            @Override
            public Future<Void> shutdown() {
                return newtsCassandraSession.shutdown().toCompletableFuture();
            }
        };
    }

    @Override
    public CassandraSession getSession() {
        return proxySession;
    }
}
