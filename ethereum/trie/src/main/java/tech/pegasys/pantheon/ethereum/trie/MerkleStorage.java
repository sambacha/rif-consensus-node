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
package tech.pegasys.pantheon.ethereum.trie;

import tech.pegasys.pantheon.util.bytes.Bytes32;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.util.Optional;

/** Storage for use in a {@link StoredMerklePatriciaTrie}. */
public interface MerkleStorage {

  /**
   * Returns an {@code Optional} of the content mapped to the hash if it exists; otherwise empty.
   *
   * @param hash The hash for the content.
   * @return an {@code Optional} of the content mapped to the hash if it exists; otherwise empty
   */
  Optional<BytesValue> get(Bytes32 hash);

  /**
   * Updates the content mapped to the specified hash, creating the mapping if one does not already
   * exist.
   *
   * <p>Note: if the storage implementation already contains content for the given hash, it will
   * replace the existing content.
   *
   * @param hash The hash for the content.
   * @param content The content to store.
   */
  void put(Bytes32 hash, BytesValue content);

  /** Persist accumulated changes to underlying storage. */
  void commit();

  /** Throws away any changes accumulated by this store. */
  void rollback();
}
