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

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.core.BlockHeaderTestFixture;
import org.hyperledger.besu.ethereum.core.Hash;
import org.hyperledger.besu.ethereum.eth.manager.task.EthTask;
import org.hyperledger.besu.services.tasks.Task;
import org.hyperledger.besu.util.bytes.BytesValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class RequestDataStepTest {

  private static final long BLOCK_NUMBER = 492L;
  private static final BytesValue DATA1 = BytesValue.of(1, 1, 1, 1);
  private static final BytesValue DATA2 = BytesValue.of(2, 2, 2, 2);
  private static final Hash HASH1 = Hash.hash(DATA1);
  private static final Hash HASH2 = Hash.hash(DATA2);

  @SuppressWarnings("unchecked")
  private final BiFunction<List<Hash>, Long, EthTask<Map<Hash, BytesValue>>>
      getNodeDataTaskFactory = mock(BiFunction.class);

  @SuppressWarnings("unchecked")
  private final EthTask<Map<Hash, BytesValue>> ethTask = mock(EthTask.class);

  private final WorldDownloadState downloadState = mock(WorldDownloadState.class);
  private final BlockHeader blockHeader =
      new BlockHeaderTestFixture().number(BLOCK_NUMBER).buildHeader();
  private final CompletableFuture<Map<Hash, BytesValue>> getDataFuture = new CompletableFuture<>();

  private final RequestDataStep requestDataStep = new RequestDataStep(getNodeDataTaskFactory);

  @Parameters(name="use unitrie={0}")
  public static Object[] data() {
    // Use or not UniNodeDataRequests as opposed to classic NodeDataRequests
    return new Object[]{false, true};
  }

  @Parameter
  public boolean useClassicalRequest;

  @Before
  public void setUp() {
    when(ethTask.run()).thenReturn(getDataFuture);
  }

  @Test
  public void shouldRequestDistinctHashesForTasks() {
    final StubTask task1 = stubTaskForHash(HASH1);
    final StubTask task2 = stubTaskForHash(HASH2);
    final StubTask task3 = stubTaskForHash(HASH1);
    final List<Task<NodeDataRequest>> tasks = asList(task1, task2, task3);

    when(getNodeDataTaskFactory.apply(asList(HASH1, HASH2), BLOCK_NUMBER)).thenReturn(ethTask);

    final CompletableFuture<List<Task<NodeDataRequest>>> result =
        requestDataStep.requestData(tasks, blockHeader, downloadState);

    assertThat(result).isNotDone();

    getDataFuture.complete(ImmutableMap.of(HASH1, DATA1, HASH2, DATA2));

    assertThat(result).isDone();
    assertThat(result).isCompletedWithValue(tasks);

    assertThat(task1.getData().getData()).isEqualTo(DATA1);
    assertThat(task2.getData().getData()).isEqualTo(DATA2);
    assertThat(task3.getData().getData()).isEqualTo(DATA1);

    verify(downloadState).requestComplete(true);
  }

  @Test
  public void shouldReportNoProgressWhenRequestCompletesWithNoData() {
    final StubTask task1 = stubTaskForHash(HASH1);
    final List<Task<NodeDataRequest>> tasks = singletonList(task1);

    when(getNodeDataTaskFactory.apply(singletonList(HASH1), BLOCK_NUMBER)).thenReturn(ethTask);

    final CompletableFuture<List<Task<NodeDataRequest>>> result =
        requestDataStep.requestData(tasks, blockHeader, downloadState);

    assertThat(result).isNotDone();

    getDataFuture.complete(emptyMap());

    assertThat(result).isDone();
    assertThat(result).isCompletedWithValue(tasks);

    verify(downloadState).requestComplete(false);
  }

  @Test
  public void shouldNotReportNoProgressWhenTaskFails() {
    final StubTask task1 = stubTaskForHash(HASH1);
    final List<Task<NodeDataRequest>> tasks = singletonList(task1);

    when(getNodeDataTaskFactory.apply(singletonList(HASH1), BLOCK_NUMBER)).thenReturn(ethTask);

    final CompletableFuture<List<Task<NodeDataRequest>>> result =
        requestDataStep.requestData(tasks, blockHeader, downloadState);

    assertThat(result).isNotDone();

    getDataFuture.completeExceptionally(new RuntimeException());

    assertThat(result).isDone();
    assertThat(result).isCompletedWithValue(tasks);

    verify(downloadState, never()).requestComplete(anyBoolean());
  }

  @Test
  public void shouldTrackOutstandingTasks() {
    final StubTask task1 = stubTaskForHash(HASH1);
    final List<Task<NodeDataRequest>> tasks = singletonList(task1);

    when(getNodeDataTaskFactory.apply(singletonList(HASH1), BLOCK_NUMBER)).thenReturn(ethTask);

    requestDataStep.requestData(tasks, blockHeader, downloadState);
    verify(downloadState).addOutstandingTask(ethTask);

    getDataFuture.complete(emptyMap());
    verify(downloadState).removeOutstandingTask(ethTask);
  }

  private StubTask stubTaskForHash(final Hash hash) {
    return useClassicalRequest
        ? StubTask.forHash(hash)
        : new StubTask(NodeDataRequest.createUniNodeValueDataRequest(hash));
  }
}
