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

import java.util.Optional;

import com.google.common.base.Preconditions;

/**
 * Class encompassing {@link UniNode} data to be encoded.
 *
 * @author ppedemon
 */
class UniNodeEncodingInput {

  private final byte[] path;
  private final ValueWrapper valueWrapper;
  private final UniNode leftChild;
  private final UniNode rightChild;

  UniNodeEncodingInput(final byte[] path, final ValueWrapper valueWrapper) {
    this(path, valueWrapper, NullUniNode.instance(), NullUniNode.instance());
  }

  UniNodeEncodingInput(
      final byte[] path,
      final ValueWrapper valueWrapper,
      final UniNode leftChild,
      final UniNode rightChild) {

    Preconditions.checkNotNull(path);
    Preconditions.checkNotNull(valueWrapper);
    Preconditions.checkNotNull(leftChild);
    Preconditions.checkNotNull(rightChild);

    this.path = path;
    this.valueWrapper = valueWrapper;
    this.leftChild = leftChild;
    this.rightChild = rightChild;
  }

  public byte[] getPath() {
    return path;
  }

  public ValueWrapper getValueWrapper() {
    return valueWrapper;
  }

  public Optional<Integer> getValueLength() {
    return valueWrapper.getLength();
  }

  public UniNode getLeftChild() {
    return leftChild;
  }

  public UniNode getRightChild() {
    return rightChild;
  }

  long getChildrenSize() {
    if (isLeaf()) {
      return 0;
    } else {
      return leftChild.intrinsicSize() + rightChild.intrinsicSize();
    }
  }

  boolean isLeaf() {
    return leftChild == NullUniNode.instance() && rightChild == NullUniNode.instance();
  }
}
