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
package tech.pegasys.pantheon.crypto;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

public class QuickEntropy {

  public byte[] getQuickEntropy() {
    final byte[] nanoTimeBytes = Longs.toByteArray(System.nanoTime());
    final byte[] objectHashBytes = Ints.toByteArray(new Object().hashCode());
    return Bytes.concat(nanoTimeBytes, objectHashBytes);
  }
}
