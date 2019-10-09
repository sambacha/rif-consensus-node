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
package tech.pegasys.pantheon.ethereum.mainnet;

import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.BlockHeaderFunctions;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.ethereum.core.ParsedExtraData;
import tech.pegasys.pantheon.ethereum.core.SealableBlockHeader;

/**
 * Looks up the correct {@link BlockHeaderFunctions} to use based on a {@link ProtocolSchedule} to
 * ensure that the correct hash is created given the block number.
 */
public class ScheduleBasedBlockHeaderFunctions<C> implements BlockHeaderFunctions {

  private final ProtocolSchedule<C> protocolSchedule;

  private ScheduleBasedBlockHeaderFunctions(final ProtocolSchedule<C> protocolSchedule) {
    this.protocolSchedule = protocolSchedule;
  }

  public static <C> BlockHeaderFunctions create(final ProtocolSchedule<C> protocolSchedule) {
    return new ScheduleBasedBlockHeaderFunctions<>(protocolSchedule);
  }

  @Override
  public Hash hash(final BlockHeader header) {
    return getBlockHeaderFunctions(header).hash(header);
  }

  @Override
  public ParsedExtraData parseExtraData(final BlockHeader header) {
    return getBlockHeaderFunctions(header).parseExtraData(header);
  }

  private BlockHeaderFunctions getBlockHeaderFunctions(final SealableBlockHeader header) {
    return protocolSchedule.getByBlockNumber(header.getNumber()).getBlockHeaderFunctions();
  }
}
