/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.search.aggregations.support;

import org.elasticsearch.search.aggregations.Aggregator;
import org.elasticsearch.search.aggregations.AggregatorFactories;
import org.elasticsearch.search.aggregations.AggregatorFactory;
import org.elasticsearch.search.aggregations.InternalAggregation.Type;
import org.elasticsearch.search.aggregations.pipeline.PipelineAggregator;
import org.elasticsearch.search.internal.SearchContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class MultiValuesSourceAggregatorFactory<VS extends ValuesSource, AF extends MultiValuesSourceAggregatorFactory<VS, AF>>
        extends AggregatorFactory<AF> {

    protected Map<String, ValuesSourceConfig<VS>> configs;

    public MultiValuesSourceAggregatorFactory(String name, Type type, Map<String, ValuesSourceConfig<VS>> configs,
            SearchContext context, AggregatorFactory<?> parent, AggregatorFactories.Builder subFactoriesBuilder,
            Map<String, Object> metaData) throws IOException {
        super(name, type, context, parent, subFactoriesBuilder, metaData);
        this.configs = configs;
    }

    @Override
    public Aggregator createInternal(Aggregator parent, boolean collectsFromSingleBucket, List<PipelineAggregator> pipelineAggregators,
                                     Map<String, Object> metaData) throws IOException {
        HashMap<String, VS> valuesSources = new HashMap<>();

        for (Map.Entry<String, ValuesSourceConfig<VS>> config : configs.entrySet()) {
            VS vs = config.getValue().toValuesSource(context);
            if (vs != null) {
                valuesSources.put(config.getKey(), vs);
            }
        }
        if (valuesSources.isEmpty()) {
            return createUnmapped(parent, pipelineAggregators, metaData);
        }
        return doCreateInternal(valuesSources, parent, collectsFromSingleBucket, pipelineAggregators, metaData);
    }

    protected abstract Aggregator createUnmapped(Aggregator parent, List<PipelineAggregator> pipelineAggregators,
                                                 Map<String, Object> metaData) throws IOException;

    protected abstract Aggregator doCreateInternal(Map<String, VS> valuesSources, Aggregator parent, boolean collectsFromSingleBucket,
        List<PipelineAggregator> pipelineAggregators, Map<String, Object> metaData) throws IOException;

}
