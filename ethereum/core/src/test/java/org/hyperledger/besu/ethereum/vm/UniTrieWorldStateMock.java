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
 *
 */
package org.hyperledger.besu.ethereum.vm;

import org.hyperledger.besu.ethereum.core.Address;
import org.hyperledger.besu.ethereum.core.WorldUpdater;
import org.hyperledger.besu.ethereum.storage.keyvalue.WorldStateKeyValueStorage;
import org.hyperledger.besu.ethereum.worldstate.UniTrieMutableWorldState;
import org.hyperledger.besu.services.kvstore.InMemoryKeyValueStorage;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;

public class UniTrieWorldStateMock extends UniTrieMutableWorldState {

  @JsonCreator
  public static UniTrieWorldStateMock create(final Map<String, AccountMock> accounts) {
    final UniTrieWorldStateMock worldState = new UniTrieWorldStateMock();
    final WorldUpdater updater = worldState.updater();

    for (final Map.Entry<String, AccountMock> entry : accounts.entrySet()) {
      AccountMock.insertAccount(updater, Address.fromHexString(entry.getKey()), entry.getValue());
    }

    updater.commit();
    return worldState;
  }

  private UniTrieWorldStateMock() {
    super(new WorldStateKeyValueStorage(new InMemoryKeyValueStorage()));
  }
}