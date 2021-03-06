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
package org.hyperledger.besu.ethereum.unitrie;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.hyperledger.besu.crypto.Hash;
import org.hyperledger.besu.plugin.data.Address;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt256;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UniTrieKeyMapperTest {

  private static final Bytes mockAddress =
      Bytes.fromHexString("0x71c7656ec7ab88b098defb751b7401b5f6d8976f");

  @Mock private Address address;

  private UniTrieKeyMapper mapper;

  @Before
  public void setup() {
    mapper = new UniTrieKeyMapper();
  }

  @Test
  public void testAccountKey() {
    when(address.toArrayUnsafe()).thenReturn(mockAddress.toArrayUnsafe());
    Bytes key = mapper.getAccountKey(address);

    assertThat(key.get(0)).isEqualTo((byte) 0);
    assertThat(key.slice(1, 10).toHexString())
        .isEqualTo(Hash.keccak256(mockAddress).slice(0, 10).toHexString());
    assertThat(key.slice(11)).isEqualTo(mockAddress);
  }

  @Test
  public void testAccountCodeKey() {
    when(address.toArrayUnsafe()).thenReturn(mockAddress.toArrayUnsafe());
    Bytes key = mapper.getAccountCodeKey(address);

    assertThat(key.get(0)).isEqualTo((byte) 0);
    assertThat(key.slice(1, 10).toHexString())
        .isEqualTo(Hash.keccak256(mockAddress).slice(0, 10).toHexString());
    assertThat(key.slice(11, 20)).isEqualTo(mockAddress);
    assertThat(key.get(31)).isEqualTo((byte) 0x80);
  }

  @Test
  public void testAccountStoragePrefixKey() {
    when(address.toArrayUnsafe()).thenReturn(mockAddress.toArrayUnsafe());
    Bytes key = mapper.getAccountStoragePrefixKey(address);

    assertThat(key.get(0)).isEqualTo((byte) 0);
    assertThat(key.slice(1, 10).toHexString())
        .isEqualTo(Hash.keccak256(mockAddress).slice(0, 10).toHexString());
    assertThat(key.slice(11, 20)).isEqualTo(mockAddress);
    assertThat(key.get(31)).isEqualTo((byte) 0);
  }

  @Test
  public void testAccountStorageKey_allZeros() {
    UInt256 subkey = UInt256.ZERO;
    when(address.toArrayUnsafe()).thenReturn(mockAddress.toArrayUnsafe());

    Bytes key = mapper.getAccountStorageKey(address, subkey);

    assertThat(key.size()).isEqualTo(43);
    assertThat(key.get(0)).isEqualTo((byte) 0);
    assertThat(key.slice(1, 10).toHexString())
        .isEqualTo(Hash.keccak256(mockAddress).slice(0, 10).toHexString());
    assertThat(key.slice(11, 20)).isEqualTo(mockAddress);
    assertThat(key.get(31)).isEqualTo((byte) 0);
    assertThat(key.slice(32, 10).toHexString())
        .isEqualTo(Hash.keccak256(subkey.toBytes()).slice(0, 10).toHexString());
    assertThat(key.get(42)).isEqualTo((byte) 0);
  }

  @Test
  public void testAccountStorageKey_leadingZeros() {
    UInt256 subkey = UInt256.valueOf(1234);
    when(address.toArrayUnsafe()).thenReturn(mockAddress.toArrayUnsafe());

    Bytes key = mapper.getAccountStorageKey(address, subkey);

    assertThat(key.size()).isEqualTo(44);
    assertThat(key.get(0)).isEqualTo((byte) 0);
    assertThat(key.slice(1, 10).toHexString())
        .isEqualTo(Hash.keccak256(mockAddress).slice(0, 10).toHexString());
    assertThat(key.slice(11, 20)).isEqualTo(mockAddress);
    assertThat(key.get(31)).isEqualTo((byte) 0);
    assertThat(key.slice(32, 10).toHexString())
        .isEqualTo(Hash.keccak256(subkey.toBytes()).slice(0, 10).toHexString());
    assertThat(key.slice(42)).isEqualTo(subkey.toBytes().slice(30));
  }

  @Test
  public void testAccountStorageKey_noZeros() {
    UInt256 subkey = UInt256.MAX_VALUE;
    when(address.toArrayUnsafe()).thenReturn(mockAddress.toArrayUnsafe());

    Bytes key = mapper.getAccountStorageKey(address, subkey);

    assertThat(key.size()).isEqualTo(74);
    assertThat(key.get(0)).isEqualTo((byte) 0);
    assertThat(key.slice(1, 10).toHexString())
        .isEqualTo(Hash.keccak256(mockAddress).slice(0, 10).toHexString());
    assertThat(key.slice(11, 20)).isEqualTo(mockAddress);
    assertThat(key.get(31)).isEqualTo((byte) 0);
    assertThat(key.slice(32, 10).toHexString())
        .isEqualTo(Hash.keccak256(subkey.toBytes()).slice(0, 10).toHexString());
    assertThat(key.slice(42)).isEqualTo(subkey.toBytes());
  }
}
