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

import static tech.pegasys.pantheon.util.uint.UInt256s.greaterThanOrEqualTo256;

import tech.pegasys.pantheon.ethereum.core.Gas;
import tech.pegasys.pantheon.ethereum.vm.AbstractOperation;
import tech.pegasys.pantheon.ethereum.vm.GasCalculator;
import tech.pegasys.pantheon.ethereum.vm.MessageFrame;
import tech.pegasys.pantheon.util.bytes.Bytes32;
import tech.pegasys.pantheon.util.bytes.Bytes32s;
import tech.pegasys.pantheon.util.uint.UInt256;

public class SarOperation extends AbstractOperation {

  private static final Bytes32 ALL_BITS =
      Bytes32.fromHexString("0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff");

  public SarOperation(final GasCalculator gasCalculator) {
    super(0x1d, "SAR", 2, 1, false, 1, gasCalculator);
  }

  @Override
  public Gas cost(final MessageFrame frame) {
    return gasCalculator().getVeryLowTierGasCost();
  }

  @Override
  public void execute(final MessageFrame frame) {
    final UInt256 shiftAmount = frame.popStackItem().asUInt256();
    Bytes32 value = frame.popStackItem();

    final boolean negativeNumber = value.get(0) < 0;

    // short circuit result if we are shifting more than the width of the data.
    if (greaterThanOrEqualTo256(shiftAmount)) {
      final Bytes32 overflow = negativeNumber ? ALL_BITS : Bytes32.ZERO;
      frame.pushStackItem(overflow);
      return;
    }

    // first perform standard shift right.
    value = Bytes32s.shiftRight(value, shiftAmount.toInt());

    // if a negative number, carry through the sign.
    if (negativeNumber) {
      final Bytes32 significantBits = Bytes32s.shiftLeft(ALL_BITS, 256 - shiftAmount.toInt());
      value = Bytes32s.or(value, significantBits);
    }
    frame.pushStackItem(value);
  }
}
