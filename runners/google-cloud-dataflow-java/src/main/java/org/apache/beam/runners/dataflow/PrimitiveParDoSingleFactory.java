/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.beam.runners.dataflow;

import java.util.List;
import org.apache.beam.runners.core.construction.ForwardingPTransform;
import org.apache.beam.runners.core.construction.SingleInputOutputOverrideFactory;
import org.apache.beam.sdk.common.runner.v1.RunnerApi.DisplayData;
import org.apache.beam.sdk.runners.PTransformOverrideFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.ParDo.Bound;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionView;

/**
 * A {@link PTransformOverrideFactory} that produces {@link ParDoSingle} instances from
 * {@link ParDo.Bound} instances. {@link ParDoSingle} is a primitive {@link PTransform}, to ensure
 * that {@link DisplayData} appears on all {@link ParDo ParDos} in the {@link DataflowRunner}.
 */
public class PrimitiveParDoSingleFactory<InputT, OutputT>
    extends SingleInputOutputOverrideFactory<
        PCollection<? extends InputT>, PCollection<OutputT>, ParDo.Bound<InputT, OutputT>> {
  @Override
  public PTransform<PCollection<? extends InputT>, PCollection<OutputT>> getReplacementTransform(
      ParDo.Bound<InputT, OutputT> transform) {
    return new ParDoSingle<>(transform);
  }

  /**
   * A single-output primitive {@link ParDo}.
   */
  public static class ParDoSingle<InputT, OutputT>
      extends ForwardingPTransform<PCollection<? extends InputT>, PCollection<OutputT>> {
    private final ParDo.Bound<InputT, OutputT> original;

    private ParDoSingle(Bound<InputT, OutputT> original) {
      this.original = original;
    }

    @Override
    public PCollection<OutputT> expand(PCollection<? extends InputT> input) {
      return PCollection.createPrimitiveOutputInternal(
          input.getPipeline(), input.getWindowingStrategy(), input.isBounded());
    }

    public DoFn<InputT, OutputT> getFn() {
      return original.getFn();
    }

    public List<PCollectionView<?>> getSideInputs() {
      return original.getSideInputs();
    }

    @Override
    protected PTransform<PCollection<? extends InputT>, PCollection<OutputT>> delegate() {
      return original;
    }
  }
}
