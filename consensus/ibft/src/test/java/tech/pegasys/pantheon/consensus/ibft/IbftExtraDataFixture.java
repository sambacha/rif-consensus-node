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
package tech.pegasys.pantheon.consensus.ibft;

import static java.util.Collections.emptyList;

import tech.pegasys.pantheon.crypto.SECP256K1;
import tech.pegasys.pantheon.crypto.SECP256K1.KeyPair;
import tech.pegasys.pantheon.crypto.SECP256K1.Signature;
import tech.pegasys.pantheon.ethereum.core.Address;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.core.Hash;
import tech.pegasys.pantheon.util.bytes.BytesValue;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class IbftExtraDataFixture {

  public static IbftExtraData createExtraData(
      final BlockHeader header,
      final BytesValue vanityData,
      final Optional<Vote> vote,
      final List<Address> validators,
      final List<KeyPair> committerKeyPairs) {

    return createExtraData(header, vanityData, vote, validators, committerKeyPairs, 0);
  }

  public static IbftExtraData createExtraData(
      final BlockHeader header,
      final BytesValue vanityData,
      final Optional<Vote> vote,
      final List<Address> validators,
      final List<KeyPair> committerKeyPairs,
      final int roundNumber) {

    return createExtraData(
        header, vanityData, vote, validators, committerKeyPairs, roundNumber, false);
  }

  public static IbftExtraData createExtraData(
      final BlockHeader header,
      final BytesValue vanityData,
      final Optional<Vote> vote,
      final List<Address> validators,
      final List<KeyPair> committerKeyPairs,
      final int baseRoundNumber,
      final boolean useDifferentRoundNumbersForCommittedSeals) {

    final IbftExtraData ibftExtraDataNoCommittedSeals =
        new IbftExtraData(vanityData, emptyList(), vote, baseRoundNumber, validators);

    // if useDifferentRoundNumbersForCommittedSeals is true then each committed seal will be
    // calculated for an extraData field with a different round number
    List<Signature> commitSeals =
        IntStream.range(0, committerKeyPairs.size())
            .mapToObj(
                i -> {
                  final int round =
                      useDifferentRoundNumbersForCommittedSeals
                          ? ibftExtraDataNoCommittedSeals.getRound() + i
                          : ibftExtraDataNoCommittedSeals.getRound();

                  IbftExtraData extraDataForCommittedSealCalculation =
                      new IbftExtraData(
                          ibftExtraDataNoCommittedSeals.getVanityData(),
                          emptyList(),
                          ibftExtraDataNoCommittedSeals.getVote(),
                          round,
                          ibftExtraDataNoCommittedSeals.getValidators());

                  final Hash headerHashForCommitters =
                      IbftBlockHashing.calculateDataHashForCommittedSeal(
                          header, extraDataForCommittedSealCalculation);

                  return SECP256K1.sign(headerHashForCommitters, committerKeyPairs.get(i));
                })
            .collect(Collectors.toList());

    return new IbftExtraData(
        ibftExtraDataNoCommittedSeals.getVanityData(),
        commitSeals,
        ibftExtraDataNoCommittedSeals.getVote(),
        ibftExtraDataNoCommittedSeals.getRound(),
        ibftExtraDataNoCommittedSeals.getValidators());
  }
}
