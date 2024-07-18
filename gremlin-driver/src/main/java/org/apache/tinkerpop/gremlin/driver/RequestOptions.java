/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tinkerpop.gremlin.driver;

import org.apache.tinkerpop.gremlin.process.traversal.GremlinLang;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.decoration.OptionsStrategy;
import org.apache.tinkerpop.gremlin.util.message.RequestMessageV4;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import static org.apache.tinkerpop.gremlin.util.TokensV4.ARGS_BATCH_SIZE;
import static org.apache.tinkerpop.gremlin.util.TokensV4.ARGS_BULKING;
import static org.apache.tinkerpop.gremlin.util.TokensV4.ARGS_EVAL_TIMEOUT;
import static org.apache.tinkerpop.gremlin.util.TokensV4.ARGS_G;
import static org.apache.tinkerpop.gremlin.util.TokensV4.ARGS_LANGUAGE;
import static org.apache.tinkerpop.gremlin.util.TokensV4.ARGS_MATERIALIZE_PROPERTIES;

/**
 * Options that can be supplied on a per request basis.
 *
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public final class RequestOptions {

    public static final RequestOptions EMPTY = RequestOptions.build().create();

    private final String graphOrTraversalSource;
    private final Map<String, Object> parameters;
    private final Integer batchSize;
    private final Long timeout;
    private final String language;
    private final String materializeProperties;
    private final boolean bulking;

    private RequestOptions(final Builder builder) {
        this.graphOrTraversalSource = builder.graphOrTraversalSource;
        this.parameters = builder.parameters;
        this.batchSize = builder.batchSize;
        this.timeout = builder.timeout;
        this.language = builder.language;
        this.materializeProperties = builder.materializeProperties;
        this.bulking = builder.bulking;
    }

    public Optional<String> getG() {
        return Optional.ofNullable(graphOrTraversalSource);
    }

    public Optional<Map<String, Object>> getParameters() {
        return Optional.ofNullable(parameters);
    }

    public Optional<Integer> getBatchSize() {
        return Optional.ofNullable(batchSize);
    }

    public Optional<Long> getTimeout() {
        return Optional.ofNullable(timeout);
    }

    public Optional<String> getLanguage() {
        return Optional.ofNullable(language);
    }

    public Optional<String> getMaterializeProperties() { return Optional.ofNullable(materializeProperties); }

    public boolean isBulking() { return bulking; }

    public static Builder build() {
        return new Builder();
    }

    public static RequestOptions getRequestOptions(final GremlinLang gremlinLang) {
        final Iterator<OptionsStrategy> itty = gremlinLang.getOptionsStrategies().iterator();
        final RequestOptions.Builder builder = RequestOptions.build().withBulking(true);
        while (itty.hasNext()) {
            final OptionsStrategy optionsStrategy = itty.next();
            final Map<String, Object> options = optionsStrategy.getOptions();
            if (options.containsKey(ARGS_EVAL_TIMEOUT))
                builder.timeout(((Number) options.get(ARGS_EVAL_TIMEOUT)).longValue());
            if (options.containsKey(ARGS_BATCH_SIZE))
                builder.batchSize(((Number) options.get(ARGS_BATCH_SIZE)).intValue());
            if (options.containsKey(ARGS_MATERIALIZE_PROPERTIES))
                builder.materializeProperties((String) options.get(ARGS_MATERIALIZE_PROPERTIES));
            if (options.containsKey(ARGS_LANGUAGE))
                builder.language((String) options.get(ARGS_LANGUAGE));
            if (options.containsKey(ARGS_BULKING))
                builder.withBulking((boolean) options.get(ARGS_BULKING));
        }

        final Map<String, Object> parameters = gremlinLang.getParameters();
        if (parameters != null && !parameters.isEmpty()) {
            parameters.forEach(builder::addParameter);
        }
        return builder.create();
    }

    public static final class Builder {
        private String graphOrTraversalSource = null;
        private Map<String, Object> parameters = null;
        private Integer batchSize = null;
        private Long timeout = null;
        private String materializeProperties = null;
        private String language = null;
        private boolean bulking = false;

        /**
         * The aliases to set on the request.
         */
        public Builder addG(final String graphOrTraversalSource) {
            this.graphOrTraversalSource = graphOrTraversalSource;
            return this;
        }

        /**
         * The parameters to pass on the request.
         */
        public Builder addParameter(final String name, final Object value) {
            if (null == parameters)
                parameters = new HashMap<>();

            if (ARGS_G.equals(name)) {
                this.graphOrTraversalSource = (String) value;
            }

            if (ARGS_LANGUAGE.equals(name)) {
                this.language = (String) value;
            }

            parameters.put(name, value);
            return this;
        }

        /**
         * The per client request override for the client and server configured {@code resultIterationBatchSize}. If
         * this value is not set, then the configuration for the {@link Cluster} is used unless the
         * {@link RequestMessageV4} is configured completely by the user.
         */
        public Builder batchSize(final int batchSize) {
            this.batchSize = batchSize;
            return this;
        }

        /**
         * Enable or disable bulking on server.
         * In some cases, bulking can significantly improve performance. It is better to enable when
         * the server response may contain duplicates, in other cases a small overhead will be added.
         */
        public Builder withBulking(final boolean bulking) {
            this.bulking = bulking;
            return this;
        }

        /**
         * The per client request override in milliseconds for the server configured {@code evaluationTimeout}.
         * If this value is not set, then the configuration for the server is used.
         */
        public Builder timeout(final long timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * Sets the language identifier to be sent on the request.
         */
        public Builder language(final String language) {
            this.language = language;
            return this;
        }

        /**
         * Sets the materializeProperties identifier to be sent on the request.
         */
        public Builder materializeProperties(final String materializeProperties) {
            this.materializeProperties = materializeProperties;
            return this;
        }

        public RequestOptions create() {
            return new RequestOptions(this);
        }
    }
}
