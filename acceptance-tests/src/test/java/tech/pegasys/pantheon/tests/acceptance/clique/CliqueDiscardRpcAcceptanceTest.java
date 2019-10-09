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

public class CliqueDiscardRpcAcceptanceTest extends AcceptanceTestBase {

  @Test
  public void shouldDiscardVotes() throws IOException {
    final String[] validators = {"validator1", "validator3"};
    final PantheonNode validator1 =
        pantheon.createCliqueNodeWithValidators("validator1", validators);
    final PantheonNode validator2 =
        pantheon.createCliqueNodeWithValidators("validator2", validators);
    final PantheonNode validator3 =
        pantheon.createCliqueNodeWithValidators("validator3", validators);
    cluster.start(validator1, validator2, validator3);

    validator1.execute(cliqueTransactions.createAddProposal(validator2));
    validator1.execute(cliqueTransactions.createRemoveProposal(validator3));
    validator1.verify(
        clique.proposalsEqual().addProposal(validator2).removeProposal(validator3).build());

    validator1.execute(cliqueTransactions.createDiscardProposal(validator2));
    validator1.verify(clique.proposalsEqual().removeProposal(validator3).build());

    validator1.execute(cliqueTransactions.createDiscardProposal(validator3));
    cluster.verify(clique.noProposals());
  }
}
