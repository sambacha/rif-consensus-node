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

import org.hyperledger.besu.util.bytes.BytesValue;

import com.google.common.base.Preconditions;

/**
 * Visitor implementing put operation on a Unitrie.
 *
 * @author ppedemon
 */
public class PutVisitor implements UniPathVisitor {

  private final BytesValue value;
  private final UniNodeFactory nodeFactory;

  public PutVisitor(final BytesValue value, final UniNodeFactory nodeFactory) {
    Preconditions.checkNotNull(value, "Value to insert can't be null");
    this.value = value;
    this.nodeFactory = nodeFactory;
  }

  @Override
  public UniNode visit(final NullUniNode node, final BytesValue path) {
    return nodeFactory.createLeaf(path, ValueWrapper.fromValue(value));
  }

  @Override
  public UniNode visit(final BranchUniNode node, final BytesValue path) {
    BytesValue nodePath = node.getPath();
    BytesValue commonPath = path.commonPrefix(nodePath);

    if (commonPath.size() == path.size() && commonPath.size() == nodePath.size()) {
      return node.replaceValue(value);
    }

    if (commonPath.size() < nodePath.size()) {
      BytesValue updatedNodePath = nodePath.slice(commonPath.size() + 1);

      UniNode updatedNode = node.replacePath(updatedNodePath);
      byte updatedNodePos = nodePath.get(commonPath.size());

      if (commonPath.size() == path.size()) {
        return splitWithoutNewLeaf(commonPath, value, updatedNode, updatedNodePos);
      } else {
        BytesValue newLeafPath = path.slice(commonPath.size() + 1);
        return splitWithNewLeaf(commonPath, value, updatedNode, updatedNodePos, newLeafPath);
      }
    }

    // If we get here then commonPath.size() == nodePath.size() and
    // commonPath.size() < path.size().This is the recursive case.
    byte pos = path.get(commonPath.size());
    BytesValue newPath = path.slice(commonPath.size() + 1);
    if (pos == 0) {
      return node.replaceChild(pos, node.getLeftChild().accept(this, newPath));
    } else {
      return node.replaceChild(pos, node.getRightChild().accept(this, newPath));
    }
  }

  /**
   * Produce a split without a new leaf. This will happen because the common path is equal to the
   * new leaf path, so the split root must hold the new value.
   *
   * @param commonPath split's common path (will be associated to the split's root)
   * @param value new value to insert
   * @param updatedNode original node, now holding the suffix of the common path
   * @param updatedNodePos byte signaling position of the original node under the split root
   * @return root node of the split, holding the new value and the updated node as the only child
   */
  private UniNode splitWithoutNewLeaf(
      final BytesValue commonPath,
      final BytesValue value,
      final UniNode updatedNode,
      final byte updatedNodePos) {

    if (updatedNodePos == 0) {
      return nodeFactory.createBranch(
          commonPath, ValueWrapper.fromValue(value), updatedNode, NullUniNode.instance());
    } else {
      return nodeFactory.createBranch(
          commonPath, ValueWrapper.fromValue(value), NullUniNode.instance(), updatedNode);
    }
  }

  /**
   * Produce a split with a new leaf. This will happen because the new leaf path is larger than the
   * common path, so the split root must have as children the updated node and the a new leaf
   * holding the value to insert.
   *
   * @param commonPath split's common path (will be associated to the split's root)
   * @param value new value to insert
   * @param updatedNode original node, now holding the suffix of the common path
   * @param updatedNodePos byte signaling position of the original node under the split root
   * @param newLeafPath path to new leaf to be put under the split root, sibling of the updated node
   * @return root node of the split, having as children the updated node and the new leaf
   */
  private UniNode splitWithNewLeaf(
      final BytesValue commonPath,
      final BytesValue value,
      final UniNode updatedNode,
      final byte updatedNodePos,
      final BytesValue newLeafPath) {

    UniNode newLeaf = nodeFactory.createLeaf(newLeafPath, ValueWrapper.fromValue(value));
    if (updatedNodePos == 0) {
      return nodeFactory.createBranch(commonPath, ValueWrapper.EMPTY, updatedNode, newLeaf);
    } else {
      return nodeFactory.createBranch(commonPath, ValueWrapper.EMPTY, newLeaf, updatedNode);
    }
  }
}
