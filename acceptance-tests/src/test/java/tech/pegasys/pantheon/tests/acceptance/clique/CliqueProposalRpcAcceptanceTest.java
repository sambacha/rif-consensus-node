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
package tech.pegasys.pantheon.tests.acceptance.clique;

import tech.pegasys.pantheon.tests.acceptance.dsl.AcceptanceTestBase;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.PantheonNode;

import java.io.IOException;

import org.junit.Test;

public class CliqueProposalRpcAcceptanceTest extends AcceptanceTestBase {

  @Test
  public void shouldReturnProposals() throws IOException {
    final String[] initialValidators = {"miner1", "miner2"};
    final PantheonNode minerNode1 =
        pantheon.createCliqueNodeWithValidators("miner1", initialValidators);
    final PantheonNode minerNode2 =
        pantheon.createCliqueNodeWithValidators("miner2", initialValidators);
    final PantheonNode minerNode3 =
        pantheon.createCliqueNodeWithValidators("miner3", initialValidators);
    cluster.start(minerNode1, minerNode2, minerNode3);

    cluster.verify(clique.noProposals());
    minerNode1.execute(cliqueTransactions.createAddProposal(minerNode3));
    minerNode1.execute(cliqueTransactions.createRemoveProposal(minerNode2));
    minerNode2.execute(cliqueTransactions.createRemoveProposal(minerNode3));

    minerNode1.verify(
        clique.proposalsEqual().addProposal(minerNode3).removeProposal(minerNode2).build());
    minerNode2.verify(clique.proposalsEqual().removeProposal(minerNode3).build());
    minerNode3.verify(clique.noProposals());
  }
}
