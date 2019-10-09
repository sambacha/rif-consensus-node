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
package tech.pegasys.pantheon.ethereum.core;

import tech.pegasys.pantheon.ethereum.mainnet.MainnetBlockHeaderFunctions;
import tech.pegasys.pantheon.ethereum.util.RawBlockIterator;
import tech.pegasys.pantheon.testutil.BlockTestUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.rules.TemporaryFolder;

public final class BlockSyncTestUtils {

  private BlockSyncTestUtils() {
    // Utility Class
  }

  public static List<Block> firstBlocks(final int count) {
    final List<Block> result = new ArrayList<>(count);
    final TemporaryFolder temp = new TemporaryFolder();
    try {
      temp.create();
      final Path blocks = temp.newFile().toPath();
      BlockTestUtil.write1000Blocks(blocks);
      try (final RawBlockIterator iterator =
          new RawBlockIterator(
              blocks, rlp -> BlockHeader.readFrom(rlp, new MainnetBlockHeaderFunctions()))) {
        for (int i = 0; i < count; ++i) {
          result.add(iterator.next());
        }
      }
    } catch (final IOException ex) {
      throw new IllegalStateException(ex);
    } finally {
      temp.delete();
    }
    return result;
  }
}
