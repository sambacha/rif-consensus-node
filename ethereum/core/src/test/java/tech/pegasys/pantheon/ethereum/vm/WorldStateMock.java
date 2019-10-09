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
package tech.pegasys.pantheon.ethereum.vm;

import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.MutableAccount;
import tech.pegasys.pantheon.ethereum.core.Wei;
import tech.pegasys.pantheon.ethereum.core.WorldUpdater;
import tech.pegasys.pantheon.ethereum.worldstate.DebuggableMutableWorldState;
import tech.pegasys.pantheon.util.bytes.BytesValue;
import tech.pegasys.pantheon.util.uint.UInt256;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Represent a mock worldState for testing. */
public class WorldStateMock extends DebuggableMutableWorldState {

  public static class AccountMock {
    private final long nonce;
    private final Wei balance;
    private final BytesValue code;
    private final Map<UInt256, UInt256> storage;

    private static final Map<UInt256, UInt256> parseStorage(final Map<String, String> values) {
      final Map<UInt256, UInt256> storage = new HashMap<>();
      for (final Map.Entry<String, String> entry : values.entrySet()) {
        storage.put(UInt256.fromHexString(entry.getKey()), UInt256.fromHexString(entry.getValue()));
      }
      return storage;
    }

    public AccountMock(
        @JsonProperty("nonce") final String nonce,
        @JsonProperty("balance") final String balance,
        @JsonProperty("storage") final Map<String, String> storage,
        @JsonProperty("code") final String code) {
      this.nonce = Long.decode(nonce);
      this.balance = Wei.fromHexString(balance);
      this.code = BytesValue.fromHexString(code);
      this.storage = parseStorage(storage);
    }

    public long nonce() {
      return nonce;
    }

    public Wei balance() {
      return balance;
    }

    public BytesValue code() {
      return code;
    }

    public Map<UInt256, UInt256> storage() {
      return storage;
    }
  }

  public static void insertAccount(
      final WorldUpdater updater, final Address address, final AccountMock toCopy) {
    final MutableAccount account = updater.getOrCreate(address);
    account.setNonce(toCopy.nonce());
    account.setBalance(toCopy.balance());
    account.setCode(toCopy.code());
    for (final Map.Entry<UInt256, UInt256> entry : toCopy.storage().entrySet()) {
      account.setStorageValue(entry.getKey(), entry.getValue());
    }
  }

  @JsonCreator
  public static WorldStateMock create(final Map<String, AccountMock> accounts) {
    final WorldStateMock worldState = new WorldStateMock();
    final WorldUpdater updater = worldState.updater();

    for (final Map.Entry<String, AccountMock> entry : accounts.entrySet()) {
      insertAccount(updater, Address.fromHexString(entry.getKey()), entry.getValue());
    }

    updater.commit();
    return worldState;
  }

  private WorldStateMock() {
    super();
  }
}
