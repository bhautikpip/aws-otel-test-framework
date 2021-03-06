/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.amazon.opentelemetry.load.generator.emitter;

import com.amazon.opentelemetry.load.generator.factory.AwsTracerProviderFactory;
import com.amazon.opentelemetry.load.generator.model.Parameter;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.exporter.otlp.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.TracerSdkManagement;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class OtlpTraceEmitter extends TraceEmitter {

  Tracer tracer;

  public OtlpTraceEmitter(Parameter param) {
    super();
    this.param = param;
  }

  @Override
  public void emitDataLoad() throws Exception {
    this.setupProvider();
    this.start(() -> nextDataPoint());
  }

  @Override
  public void setupProvider() throws Exception {
    OpenTelemetrySdk otelSdk = new AwsTracerProviderFactory().create();
    TracerSdkManagement tracerProvider = otelSdk.getTracerManagement();
    tracer =
        otelSdk.getTracerProvider().get("aws-otel-load-generator-trace", "semver:0.1.0");
    OtlpGrpcSpanExporter spanExporter = OtlpGrpcSpanExporter.builder()
        .setChannel(
            ManagedChannelBuilder.forTarget(param.getEndpoint()).usePlaintext().build())
        .setDeadlineMs(TimeUnit.SECONDS.toMillis(10))
        .build();

    tracerProvider.addSpanProcessor(
        SimpleSpanProcessor.builder(spanExporter).build());
  }

  @Override
  public void nextDataPoint() {
    Span exampleSpan = tracer.spanBuilder("Example Span").setSpanKind(Span.Kind.SERVER).startSpan();

    exampleSpan.setAttribute("good", "true");
    exampleSpan.setAttribute("exampleNumber", UUID.randomUUID().toString());
    exampleSpan.end();
  }
}
