/*
 * Copyright 2019 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package tech.pegasys.pantheon.ethereum.jsonrpc.websocket.subscription.syncing;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import tech.pegasys.pantheon.ethereum.core.SyncStatus;
import tech.pegasys.pantheon.ethereum.core.Synchronizer;
import tech.pegasys.pantheon.ethereum.core.Synchronizer.SyncStatusListener;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.results.SyncingResult;
import tech.pegasys.pantheon.ethereum.jsonrpc.websocket.subscription.SubscriptionManager;
import tech.pegasys.pantheon.ethereum.jsonrpc.websocket.subscription.request.SubscriptionType;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SyncingSubscriptionServiceTest {

  @Mock private SubscriptionManager subscriptionManager;
  @Mock private Synchronizer synchronizer;
  private SyncStatusListener syncStatusListener;

  @Before
  public void before() {
    final ArgumentCaptor<SyncStatusListener> captor =
        ArgumentCaptor.forClass(SyncStatusListener.class);
    when(synchronizer.observeSyncStatus(captor.capture())).thenReturn(1L);
    new SyncingSubscriptionService(subscriptionManager, synchronizer);
    syncStatusListener = captor.getValue();
  }

  @Test
  public void shouldSendSyncStatusWhenReceiveSyncStatus() {
    final SyncingSubscription subscription =
        new SyncingSubscription(9L, "conn", SubscriptionType.SYNCING);
    final List<SyncingSubscription> subscriptions = Collections.singletonList(subscription);
    final SyncStatus syncStatus = new SyncStatus(0L, 1L, 3L);
    final SyncingResult expectedSyncingResult = new SyncingResult(syncStatus);

    doAnswer(
            invocation -> {
              Consumer<List<SyncingSubscription>> consumer = invocation.getArgument(2);
              consumer.accept(subscriptions);
              return null;
            })
        .when(subscriptionManager)
        .notifySubscribersOnWorkerThread(any(), any(), any());

    syncStatusListener.onSyncStatus(syncStatus);

    verify(subscriptionManager)
        .sendMessage(eq(subscription.getSubscriptionId()), eq(expectedSyncingResult));
  }

  @Test
  public void shouldSendNotSyncingStatusWhenReceiveSyncStatusAtHead() {
    final SyncingSubscription subscription =
        new SyncingSubscription(9L, "conn", SubscriptionType.SYNCING);
    final List<SyncingSubscription> subscriptions = Collections.singletonList(subscription);
    final SyncStatus syncStatus = new SyncStatus(0L, 1L, 1L);

    doAnswer(
            invocation -> {
              Consumer<List<SyncingSubscription>> consumer = invocation.getArgument(2);
              consumer.accept(subscriptions);
              return null;
            })
        .when(subscriptionManager)
        .notifySubscribersOnWorkerThread(any(), any(), any());

    syncStatusListener.onSyncStatus(syncStatus);

    verify(subscriptionManager)
        .sendMessage(eq(subscription.getSubscriptionId()), any(NotSynchronisingResult.class));
  }
}
