/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.cloud.opentelemetry.metric;

import com.google.api.MetricDescriptor;
import com.google.monitoring.v3.CreateMetricDescriptorRequest;
import com.google.monitoring.v3.ProjectName;
import com.google.monitoring.v3.TimeSeries;
import java.util.List;

/** Wrapper interface for writing to Google Cloud Monitoring. */
public interface CloudMetricClient {
  /**
   * Construct a metric descriptor.
   *
   * <p>This informs Cloud Monitoring of label descriptions, metric descriptions, units, etc.
   */
  MetricDescriptor createMetricDescriptor(CreateMetricDescriptorRequest request);

  /**
   * Send a time series to Cloud Monitoring.
   *
   * @param name The name of the project where we write the time series.
   * @param timeSeries The list of time series to write.
   *     <p>Note: This can only take one point at per time series.
   */
  void createTimeSeries(ProjectName name, List<TimeSeries> timeSeries);

  /**
   * Send a service time series to Cloud Monitoring. A service time series is a time series for a
   * metric from a Google Cloud service. This method should not be used for sending custom metrics.
   *
   * @param name The name of the project where we write the time series.
   * @param timeSeries The list of time series to write.
   *     <p>Note: This can only take one point at per time series.
   */
  void createServiceTimeSeries(ProjectName name, List<TimeSeries> timeSeries);

  /** Shutdown this client, cleaning up any resources. */
  void shutdown();
}
