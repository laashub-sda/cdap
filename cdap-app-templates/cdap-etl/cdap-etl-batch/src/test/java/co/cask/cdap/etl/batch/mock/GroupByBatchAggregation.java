/*
 * Copyright © 2016 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package co.cask.cdap.etl.batch.mock;

import co.cask.cdap.api.annotation.Name;
import co.cask.cdap.api.annotation.Plugin;
import co.cask.cdap.api.data.format.StructuredRecord;
import co.cask.cdap.api.plugin.PluginConfig;
import co.cask.cdap.etl.api.Emitter;
import co.cask.cdap.etl.api.PipelineConfigurer;
import co.cask.cdap.etl.api.batch.BatchAggregation;
import co.cask.cdap.etl.api.batch.BatchRuntimeContext;
import co.cask.cdap.etl.api.batch.BatchSourceContext;
import co.cask.cdap.etl.batch.FunctionConfig;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Plugin(type = "aggregation")
@Name("GroupByBatchAggregation")
public class GroupByBatchAggregation extends BatchAggregation<StructuredRecord, StructuredRecord, StructuredRecord> {

  private static final Type LIST_FUNCTION_CONFIG_TYPE = new TypeToken<Map<String, FunctionConfig>>() { }.getType();
  private static final Gson GSON = new Gson();

  private final AggregatorConfig config;

  public GroupByBatchAggregation(AggregatorConfig config) {
    this.config = config;
  }

  @Override
  public void initialize(BatchRuntimeContext context) throws Exception {
    Map<String, FunctionConfig> functions = GSON.fromJson(config.functions, LIST_FUNCTION_CONFIG_TYPE);
    for (Map.Entry<String, FunctionConfig> entry : functions.entrySet()) {
      // initialize plugin
      String field = entry.getKey();
      FunctionConfig function = entry.getValue();
      Object plugin = context.newPluginInstance(function.getPlugin().getName());
    }
  }

  @Override
  public void destroy() {

  }

  public static co.cask.cdap.etl.proto.v1.Plugin getPlugin(String groupBy, Map<String, FunctionConfig> functions) {
    Map<String, String> properties = new HashMap<>();
    properties.put(PROP_GROUP_BY, groupBy);
    properties.put(PROP_FUNCTIONS, GSON.toJson(functions));
    return new co.cask.cdap.etl.proto.v1.Plugin("GroupByBatchAggregation", properties);
  }

  @Override
  public void transform(StructuredRecord input, Emitter<StructuredRecord> emitter) throws Exception {
    // TODO: ??
    emitter.emit(input);
  }

  public static class AggregatorConfig extends PluginConfig {
    private String groupBy;

    // ideally this would be Map<String, FunctionConfig> functions
    private String functions;
  }

  @Override
  public void configurePipeline(PipelineConfigurer configurer) {
//    for each function:
//    usePlugin(id, type, name, properties);
  }

  @Override
  public void prepareRun(BatchSourceContext context) throws Exception {

  }

  @Override
  public void groupBy(StructuredRecord input, Emitter<StructuredRecord> emitter) {
    // key = new record from input with only fields in config.groupBy
    Set<String> fields = ImmutableSet.copyOf(config.groupBy.split(","));
//    emitter.emit(recordSubset(input, fields));
  }

  public void aggregate(StructuredRecord groupKey, Iterable<StructuredRecord> groupRecords, Emitter<StructuredRecord> emitter) {
    // reset all functions
    for (StructuredRecord record : groupRecords) {
//      foreach function:
//      function.update(record);
    }
    // build record from group key and function values
//    for each function:
//    val = function.aggregate();
    // emit record
  }

}
