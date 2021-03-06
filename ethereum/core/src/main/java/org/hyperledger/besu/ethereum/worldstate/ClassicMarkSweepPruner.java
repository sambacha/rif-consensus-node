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
package org.hyperledger.besu.ethereum.worldstate;

import org.hyperledger.besu.ethereum.chain.MutableBlockchain;
import org.hyperledger.besu.ethereum.core.Hash;
import org.hyperledger.besu.ethereum.rlp.RLP;
import org.hyperledger.besu.ethereum.trie.MerklePatriciaTrie;
import org.hyperledger.besu.ethereum.trie.StoredMerklePatriciaTrie;
import org.hyperledger.besu.metrics.ObservableMetricsSystem;
import org.hyperledger.besu.plugin.services.storage.KeyValueStorage;

import java.util.function.Function;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;

public class ClassicMarkSweepPruner extends AbstractMarkSweepPruner {

  public ClassicMarkSweepPruner(
      final WorldStateStorage worldStateStorage,
      final MutableBlockchain blockchain,
      final KeyValueStorage markStorage,
      final ObservableMetricsSystem metricsSystem) {
    super(worldStateStorage, blockchain, markStorage, metricsSystem);
  }

  public ClassicMarkSweepPruner(
      final WorldStateStorage worldStateStorage,
      final MutableBlockchain blockchain,
      final KeyValueStorage markStorage,
      final ObservableMetricsSystem metricsSystem,
      final int operationsPerTransaction) {

    super(worldStateStorage, blockchain, markStorage, metricsSystem, operationsPerTransaction);
  }

  @Override
  public void mark(final Hash rootHash) {
    getMarkOperationCounter().inc();
    createStateTrie(rootHash)
        .visitAll(
            node -> {
              if (Thread.interrupted()) {
                // Since we don't expect to abort marking ourselves,
                // our abort process consists only of handling interrupts
                throw new RuntimeException("Interrupted while marking");
              }
              markNode(node.getHash());
              node.getValue().ifPresent(this::processAccountState);
            });
    LOG.debug("Completed marking used nodes for pruning");
  }

  private MerklePatriciaTrie<Bytes32, Bytes> createStateTrie(final Bytes32 rootHash) {
    WorldStateStorage worldStateStorage = getWorldStateStorage();
    return new StoredMerklePatriciaTrie<>(
        worldStateStorage::getAccountStateTrieNode,
        rootHash,
        Function.identity(),
        Function.identity());
  }

  private MerklePatriciaTrie<Bytes32, Bytes> createStorageTrie(final Bytes32 rootHash) {
    WorldStateStorage worldStateStorage = getWorldStateStorage();
    return new StoredMerklePatriciaTrie<>(
        worldStateStorage::getAccountStorageTrieNode,
        rootHash,
        Function.identity(),
        Function.identity());
  }

  private void processAccountState(final Bytes value) {
    final StateTrieAccountValue accountValue = StateTrieAccountValue.readFrom(RLP.input(value));
    markNode(accountValue.getCodeHash());

    createStorageTrie(accountValue.getStorageRoot())
        .visitAll(storageNode -> markNode(storageNode.getHash()));
  }
}
