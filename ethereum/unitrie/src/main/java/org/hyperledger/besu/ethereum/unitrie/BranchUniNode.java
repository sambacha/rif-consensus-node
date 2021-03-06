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
package org.hyperledger.besu.ethereum.unitrie;

import java.nio.ByteBuffer;

import com.google.common.base.Preconditions;
import org.apache.tuweni.bytes.Bytes;

/**
 * An inner unitrie node, possibly with children. A leaf is comprised by an instance of this class
 * having two {@link NullUniNode} as children.
 *
 * @author ppedemon
 */
public class BranchUniNode extends AbstractUniNode {

  private final UniNode leftChild;
  private final UniNode rightChild;
  private final byte[] encoding;

  BranchUniNode(
      final byte[] path,
      final ValueWrapper valueWrapper,
      final UniNode leftChild,
      final UniNode rightChild) {

    super(path, valueWrapper);

    Preconditions.checkNotNull(leftChild);
    Preconditions.checkNotNull(rightChild);

    this.leftChild = leftChild;
    this.rightChild = rightChild;
    this.encoding = encode(path, valueWrapper);
  }

  BranchUniNode(final UniNodeEncodingOutput encodingOutput) {
    super(encodingOutput.getPath(), encodingOutput.getValueWrapper());

    Preconditions.checkNotNull(encodingOutput.getLeftChild());
    Preconditions.checkNotNull(encodingOutput.getRightChild());
    Preconditions.checkNotNull(encodingOutput.getEncoding());

    leftChild = encodingOutput.getLeftChild();
    rightChild = encodingOutput.getRightChild();
    encoding = encodingOutput.getEncoding();
  }

  @Override
  public UniNode accept(final UniPathVisitor visitor, final Bytes path) {
    return visitor.visit(this, path);
  }

  @Override
  public void accept(final UniNodeVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public UniNode getLeftChild() {
    return leftChild;
  }

  @Override
  public UniNode getRightChild() {
    return rightChild;
  }

  @Override
  public long getChildrenSize() {
    return encodingHelper.decodeChildrenSizeFromFullEncoding(ByteBuffer.wrap(encoding));
  }

  @Override
  public long intrinsicSize() {
    ValueWrapper valueWrapper = getValueWrapper();
    int valueSize = valueWrapper.isLong() ? valueWrapper.getLength().orElse(0) : 0;
    return valueSize + getChildrenSize() + getEncoding().length;
  }

  @Override
  public byte[] getEncoding() {
    return encoding;
  }

  @Override
  public boolean isReferencedByHash() {
    return true;
  }

  private byte[] encode(final byte[] path, final ValueWrapper valueWrapper) {
    UniNodeEncodingInput encData =
        new UniNodeEncodingInput(path, valueWrapper, leftChild, rightChild);
    return encodingHelper.encode(encData).toArrayUnsafe();
  }
}
