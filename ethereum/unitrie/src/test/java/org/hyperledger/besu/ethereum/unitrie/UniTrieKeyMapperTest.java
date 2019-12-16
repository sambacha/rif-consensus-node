/*
 * Copyright ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 *  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 */
package org.hyperledger.besu.ethereum.unitrie;

import org.hyperledger.besu.crypto.Hash;
import org.hyperledger.besu.plugin.data.Address;
import org.hyperledger.besu.util.bytes.BytesValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class UniTrieKeyMapperTest {

    private static final BytesValue mockAddress =
            BytesValue.fromHexString("0x71c7656ec7ab88b098defb751b7401b5f6d8976f");

    @Mock private Address address;

    private UniTrieKeyMapper mapper;

    @Before
    public void setup() {
        mapper = new UniTrieKeyMapper();
    }

    @Test
    public void testAccountKey() {
        when(address.getByteArray()).thenReturn(mockAddress.getArrayUnsafe());
        BytesValue key = mapper.getAccountKey(address);

        assertThat(key.get(0)).isEqualTo((byte)0);
        assertThat(key.slice(1, 10).getHexString())
                .isEqualTo(Hash.keccak256(mockAddress).slice(0, 10).getHexString());
        assertThat(key.slice(11)).isEqualTo(mockAddress);
    }

    @Test
    public void testAccountCodeKey() {
        when(address.getByteArray()).thenReturn(mockAddress.getArrayUnsafe());
        BytesValue key = mapper.getAccountCodeKey(address);

        assertThat(key.get(0)).isEqualTo((byte)0);
        assertThat(key.slice(1, 10).getHexString())
                .isEqualTo(Hash.keccak256(mockAddress).slice(0, 10).getHexString());
        assertThat(key.slice(11, 20)).isEqualTo(mockAddress);
        assertThat(key.get(31)).isEqualTo((byte)0x80);
    }

    @Test
    public void testAccountStoragePrefixKey() {
        when(address.getByteArray()).thenReturn(mockAddress.getArrayUnsafe());
        BytesValue key = mapper.getAccountStoragePrefixKey(address);

        assertThat(key.get(0)).isEqualTo((byte)0);
        assertThat(key.slice(1, 10).getHexString())
                .isEqualTo(Hash.keccak256(mockAddress).slice(0, 10).getHexString());
        assertThat(key.slice(11, 20)).isEqualTo(mockAddress);
        assertThat(key.get(31)).isEqualTo((byte)0);
    }

    @Test
    public void testAccountStorageKey_allZeros() {
        BytesValue subkey = BytesValue.of(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        when(address.getByteArray()).thenReturn(mockAddress.getArrayUnsafe());

        BytesValue key = mapper.getAccountStorageKey(address, subkey);

        assertThat(key.size()).isEqualTo(43);
        assertThat(key.get(0)).isEqualTo((byte)0);
        assertThat(key.slice(1, 10).getHexString())
                .isEqualTo(Hash.keccak256(mockAddress).slice(0, 10).getHexString());
        assertThat(key.slice(11, 20)).isEqualTo(mockAddress);
        assertThat(key.get(31)).isEqualTo((byte)0);
        assertThat(key.slice(32, 10).getHexString())
                .isEqualTo(Hash.keccak256(subkey).slice(0, 10).getHexString());
        assertThat(key.get(42)).isEqualTo((byte)0);
    }

    @Test
    public void testAccountStorageKey_leadingZeros() {
        BytesValue subkey = BytesValue.of(0, 0, 0, 0, 1, 2, 3, 4, 5, 6);
        when(address.getByteArray()).thenReturn(mockAddress.getArrayUnsafe());

        BytesValue key = mapper.getAccountStorageKey(address, subkey);

        assertThat(key.size()).isEqualTo(48);
        assertThat(key.get(0)).isEqualTo((byte)0);
        assertThat(key.slice(1, 10).getHexString())
                .isEqualTo(Hash.keccak256(mockAddress).slice(0, 10).getHexString());
        assertThat(key.slice(11, 20)).isEqualTo(mockAddress);
        assertThat(key.get(31)).isEqualTo((byte)0);
        assertThat(key.slice(32, 10).getHexString())
                .isEqualTo(Hash.keccak256(subkey).slice(0, 10).getHexString());
        assertThat(key.slice(42)).isEqualTo(subkey.slice(4));
    }

    @Test
    public void testAccountStorageKey_noZeros() {
        BytesValue subkey = BytesValue.of(1, 2, 3, 4, 5, 6);
        when(address.getByteArray()).thenReturn(mockAddress.getArrayUnsafe());

        BytesValue key = mapper.getAccountStorageKey(address, subkey);

        assertThat(key.size()).isEqualTo(48);
        assertThat(key.get(0)).isEqualTo((byte)0);
        assertThat(key.slice(1, 10).getHexString())
                .isEqualTo(Hash.keccak256(mockAddress).slice(0, 10).getHexString());
        assertThat(key.slice(11, 20)).isEqualTo(mockAddress);
        assertThat(key.get(31)).isEqualTo((byte)0);
        assertThat(key.slice(32, 10).getHexString())
                .isEqualTo(Hash.keccak256(subkey).slice(0, 10).getHexString());
        assertThat(key.slice(42)).isEqualTo(subkey);
    }
}
