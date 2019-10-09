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
package tech.pegasys.pantheon.consensus.clique.headervalidationrules;

import tech.pegasys.pantheon.consensus.clique.CliqueBlockInterface;
import tech.pegasys.pantheon.consensus.common.EpochManager;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.mainnet.DetachedBlockHeaderValidationRule;

public class CoinbaseHeaderValidationRule implements DetachedBlockHeaderValidationRule {

  private final EpochManager epochManager;

  public CoinbaseHeaderValidationRule(final EpochManager epochManager) {
    this.epochManager = epochManager;
  }

  @Override
  // The coinbase field is used for voting nodes in/out of the validator group. However, no votes
  // are allowed to be cast on epoch blocks
  public boolean validate(final BlockHeader header, final BlockHeader parent) {
    if (epochManager.isEpochBlock(header.getNumber())) {
      return header.getCoinbase().equals(CliqueBlockInterface.NO_VOTE_SUBJECT);
    }
    return true;
  }
}
