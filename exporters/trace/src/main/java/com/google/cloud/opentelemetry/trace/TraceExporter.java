package com.google.cloud.opentelemetry.trace;

import static com.google.api.client.util.Preconditions.checkNotNull;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.ServiceOptions;
import com.google.cloud.trace.v2.TraceServiceClient;
import com.google.cloud.trace.v2.TraceServiceSettings;
import com.google.devtools.cloudtrace.v1.Trace;
import com.google.devtools.cloudtrace.v2.AttributeValue;
import com.google.devtools.cloudtrace.v2.ProjectName;
import com.google.devtools.cloudtrace.v2.Span;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class TraceExporter implements SpanExporter {

  private final TraceServiceClient traceServiceClient;
  private final ProjectName projectName;
  private final String projectId;
  private final Map<String, AttributeValue> fixedAttributes;

  static TraceExporter createWithStub(String projectId, TraceServiceClient traceServiceClient,
      Map<String, AttributeValue> fixedAttributes) {
    return new TraceExporter(projectId, traceServiceClient, fixedAttributes);
  }

  static TraceExporter createWithCredentials(String projectId, Credentials credentials,
      Map<String, AttributeValue> fixedAttributes, Duration deadline) throws IOException {
    TraceServiceSettings.Builder builder = TraceServiceSettings.newBuilder()
        .setCredentialsProvider(FixedCredentialsProvider.create(checkNotNull(credentials, "credentials")));
    // We only use the batchWriteSpans API in this exporter.
    builder.batchWriteSpansSettings().setSimpleTimeoutNoRetries(org.threeten.bp.Duration.ofMillis(deadline.toMillis()));
    return new TraceExporter(projectId, TraceServiceClient.create(builder.build()), fixedAttributes);
  }

  public TraceExporter(String projectId, TraceServiceClient traceServiceClient,
      Map<String, AttributeValue> fixedAttributes) {
    this.projectId = projectId;
    this.traceServiceClient = traceServiceClient;
    this.projectName = ProjectName.of(projectId);
    this.fixedAttributes = fixedAttributes;
  }

  @Override
  public ResultCode export(Collection<SpanData> spanDataList) {
    List<Span> spans = new ArrayList<>(spanDataList.size());
    for (SpanData spanData : spanDataList) {
      spans.add(TraceTranslator.generateSpan(spanData, projectId, fixedAttributes));
    }

    traceServiceClient.batchWriteSpans(projectName, spans);
    return ResultCode.SUCCESS;
  }

  @Override
  public void shutdown() {
    throw new UnsupportedOperationException();
  }

}
