package com.continuuity;

import com.continuuity.test.ApplicationManager;
import com.continuuity.test.FlowManager;
import com.continuuity.test.ProcedureManager;
import com.continuuity.test.ReactorTestBase;
import com.continuuity.test.RuntimeMetrics;
import com.continuuity.test.RuntimeStats;
import com.continuuity.test.StreamWriter;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class WordCountTest extends ReactorTestBase {

  @Test
  public void test() throws Exception {
    try {
      ApplicationManager appManager = deployApplication(WordCountApp.class);

      // Starts a flow
      FlowManager flowManager = appManager.startFlow("WordCountFlow");

      // Write a message to stream
      StreamWriter streamWriter = appManager.getStreamWriter("text");
      streamWriter.send("A testing message message");

      // Wait for the last flowlet processed all tokens.
      RuntimeMetrics countMetrics = RuntimeStats.getFlowletMetrics("WordCountApp", "WordCountFlow", "CountByField");
      countMetrics.waitForProcessed(4, 2, TimeUnit.SECONDS);

      flowManager.stop();

      // Start procedure and query for word frequency.
      ProcedureManager procedureManager = appManager.startProcedure("WordFrequency");
      String response = procedureManager.getClient().query("wordfreq", ImmutableMap.of("word", "text:message"));

      // Verify the frequency.
      Map<String, Integer> result = new Gson().fromJson(response, new TypeToken<Map<String, Integer>>(){}.getType());
      Assert.assertEquals(2, result.get("text:message").intValue());

      procedureManager.stop();

      TimeUnit.SECONDS.sleep(1);

    } finally {
      clear();
    }
  }
}
