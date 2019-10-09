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
package tech.pegasys.pantheon.ethereum.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import tech.pegasys.pantheon.ethereum.core.Transaction;
import tech.pegasys.pantheon.ethereum.mainnet.TransactionProcessor.Result;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TransactionSimulatorResultTest {

  private TransactionSimulatorResult transactionSimulatorResult;

  @Mock private Transaction transaction;
  @Mock private Result result;

  @Before
  public void before() {
    this.transactionSimulatorResult = new TransactionSimulatorResult(transaction, result);
  }

  @Test
  public void shouldDelegateToTransactionProcessorResultWhenOutputIsCalled() {
    transactionSimulatorResult.getOutput();

    verify(result).getOutput();
  }

  @Test
  public void shouldDelegateToTransactionProcessorResultWhenIsSuccessfulIsCalled() {
    transactionSimulatorResult.isSuccessful();

    verify(result).isSuccessful();
  }

  @Test
  public void shouldUseTransactionProcessorResultAndTransactionToCalculateGasEstimate() {
    transactionSimulatorResult.getGasEstimate();

    verify(transaction).getGasLimit();
    verify(result).getGasRemaining();
  }

  @Test
  public void shouldCalculateCorrectGasEstimateWhenConsumedAllGas() {
    when(transaction.getGasLimit()).thenReturn(5L);
    when(result.getGasRemaining()).thenReturn(0L);

    assertThat(transactionSimulatorResult.getGasEstimate()).isEqualTo(5L);
  }

  @Test
  public void shouldCalculateCorrectGasEstimateWhenGasWasInsufficient() {
    when(transaction.getGasLimit()).thenReturn(1L);
    when(result.getGasRemaining()).thenReturn(-5L);

    assertThat(transactionSimulatorResult.getGasEstimate()).isEqualTo(6L);
  }

  @Test
  public void shouldCalculateCorrectGasEstimateWhenGasLimitWasSufficient() {
    when(transaction.getGasLimit()).thenReturn(10L);
    when(result.getGasRemaining()).thenReturn(3L);

    assertThat(transactionSimulatorResult.getGasEstimate()).isEqualTo(7L);
  }
}
