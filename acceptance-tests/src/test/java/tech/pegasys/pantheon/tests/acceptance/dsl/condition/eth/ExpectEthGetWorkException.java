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
package tech.pegasys.pantheon.tests.acceptance.dsl.condition.eth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import tech.pegasys.pantheon.tests.acceptance.dsl.condition.Condition;
import tech.pegasys.pantheon.tests.acceptance.dsl.node.Node;
import tech.pegasys.pantheon.tests.acceptance.dsl.transaction.eth.EthGetWorkTransaction;

import org.web3j.protocol.exceptions.ClientConnectionException;

public class ExpectEthGetWorkException implements Condition {

  private final EthGetWorkTransaction transaction;
  private final String expectedMessage;

  public ExpectEthGetWorkException(
      final EthGetWorkTransaction transaction, final String expectedMessage) {
    this.transaction = transaction;
    this.expectedMessage = expectedMessage;
  }

  @Override
  public void verify(final Node node) {
    final Throwable thrown = catchThrowable(() -> node.execute(transaction));
    assertThat(thrown).isInstanceOf(ClientConnectionException.class);
    assertThat(thrown.getMessage()).contains(expectedMessage);
  }
}
