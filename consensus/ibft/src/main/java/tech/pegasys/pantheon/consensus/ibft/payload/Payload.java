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
package tech.pegasys.pantheon.consensus.ibft.payload;

import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.rlp.BytesValueRLPOutput;
import tech.pegasys.pantheon.ethereum.rlp.RLPInput;
import tech.pegasys.pantheon.ethereum.rlp.RLPOutput;
import tech.pegasys.pantheon.util.bytes.BytesValue;

public interface Payload extends RoundSpecific {

  void writeTo(final RLPOutput rlpOutput);

  default BytesValue encoded() {
    BytesValueRLPOutput rlpOutput = new BytesValueRLPOutput();
    writeTo(rlpOutput);

    return rlpOutput.encoded();
  }

  int getMessageType();

  static Hash readDigest(final RLPInput ibftMessageData) {
    return Hash.wrap(ibftMessageData.readBytes32());
  }
}
