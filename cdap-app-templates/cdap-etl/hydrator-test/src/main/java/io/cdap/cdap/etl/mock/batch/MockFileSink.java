/*
 * Copyright © 2015-2016 Cask Data, Inc.
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

package io.cdap.cdap.etl.mock.batch;

import io.cdap.cdap.api.annotation.Macro;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.cdap.api.common.Bytes;
import io.cdap.cdap.api.data.batch.Output;
import io.cdap.cdap.api.data.batch.OutputFormatProvider;
import io.cdap.cdap.api.data.format.StructuredRecord;
import io.cdap.cdap.api.data.schema.Schema;
import io.cdap.cdap.api.dataset.lib.KeyValue;
import io.cdap.cdap.api.dataset.table.Row;
import io.cdap.cdap.api.dataset.table.Scanner;
import io.cdap.cdap.api.dataset.table.Table;
import io.cdap.cdap.api.plugin.PluginClass;
import io.cdap.cdap.api.plugin.PluginConfig;
import io.cdap.cdap.api.plugin.PluginPropertyField;
import io.cdap.cdap.etl.api.Emitter;
import io.cdap.cdap.etl.api.batch.BatchRuntimeContext;
import io.cdap.cdap.etl.api.batch.BatchSink;
import io.cdap.cdap.etl.api.batch.BatchSinkContext;
import io.cdap.cdap.etl.proto.v2.ETLPlugin;
import io.cdap.cdap.format.StructuredRecordStringConverter;
import io.cdap.cdap.test.DataSetManager;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileAsBinaryOutputFormat;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mock sink that writes records to files and has a utility method for getting all records written.
 */
@Plugin(type = BatchSink.PLUGIN_TYPE)
@Name(MockFileSink.NAME)
public class MockFileSink extends BatchSink<StructuredRecord, BytesWritable, BytesWritable> {
  public static final String NAME = "MockFile";
  public static final String INITIALIZED_COUNT_METRIC = "initialized.count";
  public static final PluginClass PLUGIN_CLASS = getPluginClass();
  private final Config config;
  private BytesWritable key = new BytesWritable();
  private BytesWritable val = new BytesWritable();

  public MockFileSink(Config config) {
    this.config = config;
  }

  /**
   * Config for the sink.
   */
  public static class Config extends PluginConfig {
    @Macro
    private String outputDir;
  }

  @Override
  public void prepareRun(BatchSinkContext context) {
    Map<String, String> outputProperties = new HashMap<>();
    outputProperties.put(FileOutputFormat.OUTDIR, config.outputDir);
    context.addOutput(Output.of(context.getStageName(),
                                new OutputFormatProvider() {
                                  @Override
                                  public String getOutputFormatClassName() {
                                    return SequenceFileAsBinaryOutputFormat.class.getName();
                                  }

                                  @Override
                                  public Map<String, String> getOutputFormatConfiguration() {
                                    return outputProperties;
                                  }
                                }));
  }

  @Override
  public void initialize(BatchRuntimeContext context) throws Exception {
    super.initialize(context);
    context.getMetrics().count(INITIALIZED_COUNT_METRIC, 1);
  }

  @Override
  public void transform(StructuredRecord input,
                        Emitter<KeyValue<BytesWritable, BytesWritable>> emitter) throws Exception {
    byte[] schemaBytes = Bytes.toBytes(input.getSchema().toString());
    byte[] recordBytes = Bytes.toBytes(StructuredRecordStringConverter.toJsonString(input));
    key.set(schemaBytes, 0, schemaBytes.length);
    val.set(recordBytes, 0, recordBytes.length);
    emitter.emit(new KeyValue<>(key, val));
  }

  public static ETLPlugin getPlugin(String outputDir) {
    Map<String, String> properties = new HashMap<>();
    properties.put("outputDir", outputDir);
    return new ETLPlugin(NAME, BatchSink.PLUGIN_TYPE, properties, null);
  }

  /**
   * Used to read the records written by this sink.
   */
  public static List<StructuredRecord> readOutput(File outputDir) throws Exception {
    List<StructuredRecord> output = new ArrayList<>();
    BytesWritable schemaBytes = new BytesWritable();
    BytesWritable recordBytes = new BytesWritable();
    for (File outputFile : outputDir.listFiles()) {
      if (!outputFile.isFile()) {
        continue;
      }

      Path path = new Path(outputFile.getAbsolutePath());
      try (SequenceFile.Reader reader = new SequenceFile.Reader(new Configuration(), SequenceFile.Reader.file(path))) {
        while (reader.next(schemaBytes, recordBytes)) {
          Schema schema = Schema.parseJson(Bytes.toString(schemaBytes.getBytes()));
          String recordStr = Bytes.toString(recordBytes.getBytes());
          StructuredRecord record = StructuredRecordStringConverter.fromJsonString(recordStr, schema);
          output.add(record);
        }
      }
    }
    return output;
  }

  private static PluginClass getPluginClass() {
    Map<String, PluginPropertyField> properties = new HashMap<>();
    properties.put("outputDir", new PluginPropertyField("outputDir", "", "string", true, true));
    return new PluginClass(BatchSink.PLUGIN_TYPE, NAME, "", MockFileSink.class.getName(), "config", properties);
  }
}
