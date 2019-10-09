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
package tech.pegasys.pantheon.ethereum.vm.operations;

import tech.pegasys.pantheon.ethereum.core.Gas;
import tech.pegasys.pantheon.ethereum.vm.AbstractOperation;
import tech.pegasys.pantheon.ethereum.vm.GasCalculator;
import tech.pegasys.pantheon.ethereum.vm.MessageFrame;
import tech.pegasys.pantheon.util.bytes.Bytes32;

public class SwapOperation extends AbstractOperation {

  private final int index;

  public SwapOperation(final int index, final GasCalculator gasCalculator) {
    super(0x90 + index - 1, "SWAP" + index, index + 1, index + 1, false, 1, gasCalculator);
    this.index = index;
  }

  @Override
  public Gas cost(final MessageFrame frame) {
    return gasCalculator().getVeryLowTierGasCost();
  }

  @Override
  public void execute(final MessageFrame frame) {
    final Bytes32 tmp = frame.getStackItem(0);
    frame.setStackItem(0, frame.getStackItem(index));
    frame.setStackItem(index, tmp);
  }
}
