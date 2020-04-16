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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Optional;
import org.hyperledger.besu.crypto.Hash;
import org.hyperledger.besu.util.bytes.BytesValue;

/**
 * Abstract UniNode, factoring out common node functionality.
 *
 * @author ppedemon
 */
public abstract class AbstractUniNode implements UniNode {

  static final int MAX_INLINED_NODE_SIZE = 44;
  static final UniNodeEncoding encodingHelper = new UniNodeEncoding();

  private ValueWrapper longValueWrapper;
  private SoftReferenceWrapper pathWeakReference;
  private boolean dirty = false;
  private byte path0;
  private byte path0Len;

  AbstractUniNode(final byte[] path, final ValueWrapper valueWrapper) {
    Preconditions.checkNotNull(path);
    Preconditions.checkNotNull(valueWrapper);

    if (valueWrapper.isLong()) {
      this.longValueWrapper = valueWrapper;
    }

    if (path.length>8) {
      this.pathWeakReference = new SoftReferenceWrapper(path);
      path0Len = 127; // we just put some value not to leave it at zero, which is tested later.
    } else {
      // El path viene expandido
      if (path.length>0) {
        BytesValue encodedPath = PathEncoding.encodePath(BytesValue.wrap(path));
        path0 = encodedPath.get(0);
      }
      path0Len = (byte) path.length;
    }
  }

  @VisibleForTesting
  void clearWeakReferences() {
    if (pathWeakReference!=null)
      pathWeakReference.value.clear();
  }

  @Override
  public byte[] getPath() {

    if (path0Len==0) {
      return new byte[]{};
    }

    if (path0Len<=8) {
      return encodingHelper.decodeSingleBytePath(path0,path0Len);
    }

    if (pathWeakReference != null) {
      byte[] v = pathWeakReference.value.get();
      if (v != null) {
        return v;
      }
    }

    byte[] path = encodingHelper.decodePathFromFullEncoding(ByteBuffer.wrap(getEncoding()));
    pathWeakReference = new SoftReferenceWrapper(path);
    return path;
  }

  @Override
  public ValueWrapper getValueWrapper() {
    if (longValueWrapper != null) {
      return longValueWrapper;
    }

    return encodingHelper.decodeValueWrapperFromFullEncoding(ByteBuffer.wrap(getEncoding()));
  }

  @Override
  public Optional<byte[]> getValue(final DataLoader loader) {
    return getValueWrapper().solveValue(loader);
  }

  @Override
  public Optional<byte[]> getValueHash() {
    return getValueWrapper().getHash();
  }

  @Override
  public Optional<Integer> getValueLength() {
    return getValueWrapper().getLength();
  }

  @Override
  public String print(final int indent) {
    BytesValue path = PathEncoding.encodePath(BytesValue.wrap(getPath()));
    long pathLength = getPath().length;
    ValueWrapper vr = getValueWrapper();
    return String.format(
        "%s%s%s%s",
        Strings.repeat(" ", indent),
        String.format(
            "(key = %s (%d), cs = %d, val = %s)", path, pathLength, getChildrenSize(), vr),
        String.format("\n%s", getLeftChild().print(indent + 2)),
        String.format("\n%s", getRightChild().print(indent + 2)));
  }

  @Override
  public String toString() {
    return print(0);
  }

  @Override
  public byte[] getHash() {
    BytesValue encoding = BytesValue.of(getEncoding());
    return Hash.keccak256(encoding).getArrayUnsafe();
  }

  @Override
  public boolean isDirty() {
    return dirty;
  }

  @Override
  public void markDirty() {
    dirty = true;
  }

  UniNode removeValue(final UniNodeFactory nodeFactory) {
    // By removing this node's value we might have a chance to coalesce
    return coalesce(
        nodeFactory.createBranch(getPath(), ValueWrapper.EMPTY, getLeftChild(), getRightChild()),
        nodeFactory);
  }

  UniNode replaceValue(final byte[] newValue, final UniNodeFactory nodeFactory) {
    Preconditions.checkNotNull(
        newValue, "Can't call replaceValue with null, call removeValue instead");
    if (getValueWrapper().wrappedValueIs(newValue)) {
      return this;
    }
    return nodeFactory.createBranch(
        getPath(), ValueWrapper.fromValue(newValue), getLeftChild(), getRightChild());
  }

  UniNode replacePath(final byte[] newPath, final UniNodeFactory nodeFactory) {
    if (Arrays.equals(newPath, getPath())) {
      return this;
    }
    return nodeFactory.createBranch(newPath, getValueWrapper(), getLeftChild(), getRightChild());
  }

  UniNode replaceChild(final byte pos, final UniNode newChild, final UniNodeFactory nodeFactory) {
    if (pos == 0) {
      if (newChild == getLeftChild()) {
        return this;
      }
      return coalesce(
          nodeFactory.createBranch(getPath(), getValueWrapper(), newChild, getRightChild()),
          nodeFactory);
    } else {
      if (newChild == getRightChild()) {
        return this;
      }
      return coalesce(
          nodeFactory.createBranch(getPath(), getValueWrapper(), getLeftChild(), newChild),
          nodeFactory);
    }
  }

  /**
   * Possibly coalesce a node. Rules are:
   *
   * <ul>
   *   <li>Node has a value: keep node as it is.
   *   <li>Node has no value and two children: keep node as it is.
   *   <li>Node has no value and no children: turn it into a {@link NullUniNode}.
   *   <li>Node has no value and a single child: replace it with the child (enlarging its path with
   *       the parent path and pos bit).
   * </ul>
   *
   * <p>So, for a branch with path p, no value and a left child with path q, return the left child
   * with path p:0:q. Conversely, if the branch has no value and a right child with path q, return
   * the right child with path p:1:q.
   *
   * @param node node to possible coalesce
   * @param nodeFactory node factory used to create a new node in case of coalescing
   * @return original node, or coalesced one.
   */
  private static UniNode coalesce(final UniNode node, final UniNodeFactory nodeFactory) {
    if (!node.getValueWrapper().isEmpty()) {
      return node;
    }

    boolean hasLeftChild = node.getLeftChild() != NullUniNode.instance();
    boolean hasRightChild = node.getRightChild() != NullUniNode.instance();

    // No value, both children: this is a branch node with no associated value, must keep
    if (hasLeftChild && hasRightChild) {
      return node;
    }

    // No value, no children: this is the NullUniNode
    if (!hasLeftChild && !hasRightChild) {
      return NullUniNode.instance();
    }

    // No value, only one child: we can actually coalesce
    byte pos;
    UniNode child;
    if (hasLeftChild) {
      pos = 0;
      child = node.getLeftChild();
    } else {
      pos = 1;
      child = node.getRightChild();
    }

    byte[] nodePath = node.getPath();
    byte[] childPath = child.getPath();
    byte[] newPath = Arrays.copyOf(nodePath, nodePath.length + 1 + childPath.length);
    newPath[nodePath.length] = pos;
    System.arraycopy(childPath, 0, newPath, nodePath.length + 1, childPath.length);

    return nodeFactory.createBranch(
        newPath, child.getValueWrapper(), child.getLeftChild(), child.getRightChild());
  }
}
