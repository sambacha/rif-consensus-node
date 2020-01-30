/*
 * Copyright ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.besu.ethereum.eth.sync.worldstate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hyperledger.besu.metrics.noop.NoOpMetricsSystem.NO_OP_COUNTER;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.hyperledger.besu.ethereum.core.Hash;
import org.hyperledger.besu.ethereum.worldstate.WorldStateStorage;
import org.hyperledger.besu.metrics.noop.NoOpMetricsSystem;
import org.hyperledger.besu.services.pipeline.Pipe;
import org.hyperledger.besu.services.tasks.Task;
import org.hyperledger.besu.util.bytes.BytesValue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class LoadLocalDataStepTest {

  private static final BytesValue DATA = BytesValue.of(1, 2, 3);
  private static final Hash HASH = Hash.hash(DATA);
  private final WorldStateStorage worldStateStorage = mock(WorldStateStorage.class);
  private final WorldStateStorage.Updater updater = mock(WorldStateStorage.Updater.class);

  private final Pipe<Task<NodeDataRequest>> completedTasks =
      new Pipe<>(10, NO_OP_COUNTER, NO_OP_COUNTER, NO_OP_COUNTER);
  private final LoadLocalDataStep loadLocalDataStep =
      new LoadLocalDataStep(worldStateStorage, new NoOpMetricsSystem());

  private static final CodeNodeDataRequest request = NodeDataRequest.createCodeRequest(HASH);
  private static final Task<NodeDataRequest> task = new StubTask(request);

  private static final UniNodeValueDataRequest uniRequest =
      NodeDataRequest.createUniNodeValueDataRequest(HASH);
  private static final Task<NodeDataRequest> uniTask = new StubTask(uniRequest);

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {request, task},
          {uniRequest, uniTask}
        });
  }

  @Parameter public NodeDataRequest currentRequest;

  @Parameter(value = 1)
  public Task<NodeDataRequest> currentTask;

  @Test
  public void shouldReturnStreamWithUnchangedTaskWhenDataNotPresent() {
    final Stream<Task<NodeDataRequest>> output =
        loadLocalDataStep.loadLocalData(currentTask, completedTasks);

    assertThat(completedTasks.poll()).isNull();
    assertThat(output).containsExactly(currentTask);
  }

  @Test
  public void shouldReturnEmptyStreamAndSendTaskToCompletedPipeWhenDataIsPresent() {
    when(worldStateStorage.getCode(HASH)).thenReturn(Optional.of(DATA));
    when(worldStateStorage.getAccountStateTrieNode(HASH)).thenReturn(Optional.of(DATA));

    final Stream<Task<NodeDataRequest>> output =
        loadLocalDataStep.loadLocalData(currentTask, completedTasks);

    assertThat(completedTasks.poll()).isSameAs(currentTask);
    assertThat(currentRequest.getData()).isEqualTo(DATA);
    assertThat(output).isEmpty();

    // Should not require persisting.
    request.persist(updater);
    verifyZeroInteractions(updater);
  }
}
