package com.continuuity.internal.app.runtime.flow;

import com.continuuity.api.data.DataSet;
import com.continuuity.api.flow.flowlet.FlowletContext;
import com.continuuity.api.flow.flowlet.FlowletSpecification;
import com.continuuity.api.metrics.Metrics;
import com.continuuity.app.logging.FlowletLoggingContext;
import com.continuuity.app.metrics.FlowletMetrics;
import com.continuuity.app.program.Program;
import com.continuuity.common.logging.LoggingContext;
import com.continuuity.common.metrics.CMetrics;
import com.continuuity.common.metrics.MetricType;
import com.continuuity.data.operation.ttqueue.QueueProducer;
import com.continuuity.internal.app.runtime.ProgramRuntimeContext;
import com.continuuity.weave.api.RunId;
import com.google.common.base.Objects;

import java.util.Map;

/**
 * Internal implementation of {@link FlowletContext}.
 */
final class BasicFlowletContext extends ProgramRuntimeContext implements FlowletContext {

  private final String flowId;
  private final String flowletId;
  private final int instanceId;
  private final FlowletSpecification flowletSpec;

  private volatile int instanceCount;
  private final QueueProducer queueProducer;
  private final boolean asyncMode;
  private final CMetrics systemMetrics;
  private final FlowletMetrics flowletMetrics;

  BasicFlowletContext(Program program, String flowletId, int instanceId, RunId runId, int instanceCount,
                      Map<String, DataSet> datasets, FlowletSpecification flowletSpec, boolean asyncMode) {
    super(program, runId, datasets);
    this.flowId = program.getProgramName();
    this.flowletId = flowletId;
    this.instanceId = instanceId;
    this.instanceCount = instanceCount;
    this.flowletSpec = flowletSpec;
    this.asyncMode = asyncMode;

    this.instanceCount = program.getSpecification().getFlows().get(flowId).getFlowlets().get(flowletId).getInstances();
    this.queueProducer = new QueueProducer(getMetricName());

    this.systemMetrics = new CMetrics(MetricType.FlowSystem, getMetricName());
    this.flowletMetrics = new FlowletMetrics(getAccountId(), getApplicationId(),
                                             flowId, flowletId, runId.toString(), instanceId);
  }

  @Override
  public String toString() {
    return String.format("flowlet=%s, instance=%d, groupsize=%s, %s",
                         getFlowletId(), getInstanceId(), getInstanceCount(), super.toString());
  }

  @Override
  public int getInstanceCount() {
    return instanceCount;
  }

  @Override
  public String getName() {
    return getFlowletId();
  }

  @Override
  public FlowletSpecification getSpecification() {
    return flowletSpec;
  }

  public CMetrics getSystemMetrics() {
    return systemMetrics;
  }

  public void setInstanceCount(int count) {
    instanceCount = count;
  }

  public boolean isAsyncMode() {
    return asyncMode;
  }

  public String getFlowId() {
    return flowId;
  }

  public String getFlowletId() {
    return flowletId;
  }

  public int getInstanceId() {
    return instanceId;
  }

  public QueueProducer getQueueProducer() {
    return queueProducer;
  }

  public LoggingContext getLoggingContext() {
    return new FlowletLoggingContext(getAccountId(), getApplicationId(), getFlowId(), getFlowletId());
  }

  public Metrics getMetrics() {
    return flowletMetrics;
  }

  public long getGroupId() {
    int gid = 100000 + Objects.hashCode(getAccountId(), getApplicationId(), getFlowId(), getFlowletId());
    return   0xffffffffL & gid;
  }

  public String getMetricName() {
    return String.format("%s.%s.%s.%s.%s.%d",
                         getAccountId(),
                         getApplicationId(),
                         getFlowId(),
                         getRunId(),
                         getFlowletId(),
                         getInstanceId());
  }
}
