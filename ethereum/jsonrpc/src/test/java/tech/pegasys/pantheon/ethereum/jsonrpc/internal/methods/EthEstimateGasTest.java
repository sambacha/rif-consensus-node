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
package tech.pegasys.pantheon.ethereum.jsonrpc.internal.methods;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import tech.pegasys.pantheon.ethereum.chain.Blockchain;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.JsonRpcRequest;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.parameters.JsonCallParameter;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.parameters.JsonRpcParameter;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.queries.BlockchainQueries;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcError;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcErrorResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcSuccessResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.results.Quantity;
import tech.pegasys.pantheon.ethereum.transaction.CallParameter;
import tech.pegasys.pantheon.ethereum.transaction.TransactionSimulator;
import tech.pegasys.pantheon.ethereum.transaction.TransactionSimulatorResult;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EthEstimateGasTest {

  private EthEstimateGas method;

  @Mock private BlockHeader blockHeader;
  @Mock private Blockchain blockchain;
  @Mock private BlockchainQueries blockchainQueries;
  @Mock private TransactionSimulator transactionSimulator;

  @Before
  public void setUp() {
    when(blockchainQueries.headBlockNumber()).thenReturn(1L);
    when(blockchainQueries.getBlockchain()).thenReturn(blockchain);
    when(blockchain.getBlockHeader(eq(1L))).thenReturn(Optional.of(blockHeader));
    when(blockHeader.getGasLimit()).thenReturn(Long.MAX_VALUE);
    when(blockHeader.getNumber()).thenReturn(1L);

    method = new EthEstimateGas(blockchainQueries, transactionSimulator, new JsonRpcParameter());
  }

  @Test
  public void shouldReturnCorrectMethodName() {
    assertThat(method.getName()).isEqualTo("eth_estimateGas");
  }

  @Test
  public void shouldReturnErrorWhenTransientTransactionProcessorReturnsEmpty() {
    final JsonRpcRequest request = ethEstimateGasRequest(callParameter());
    when(transactionSimulator.process(eq(modifiedCallParameter()), eq(1L)))
        .thenReturn(Optional.empty());

    final JsonRpcResponse expectedResponse =
        new JsonRpcErrorResponse(null, JsonRpcError.INTERNAL_ERROR);

    assertThat(method.response(request)).isEqualToComparingFieldByField(expectedResponse);
  }

  @Test
  public void shouldReturnGasEstimateWhenTransientTransactionProcessorReturnsResult() {
    final JsonRpcRequest request = ethEstimateGasRequest(callParameter());
    mockTransientProcessorResultGasEstimate(1L);

    final JsonRpcResponse expectedResponse = new JsonRpcSuccessResponse(null, Quantity.create(1L));

    assertThat(method.response(request)).isEqualToComparingFieldByField(expectedResponse);
  }

  private void mockTransientProcessorResultGasEstimate(final long gasEstimate) {
    final TransactionSimulatorResult result = mock(TransactionSimulatorResult.class);
    when(result.getGasEstimate()).thenReturn(gasEstimate);
    when(transactionSimulator.process(eq(modifiedCallParameter()), eq(1L)))
        .thenReturn(Optional.of(result));
  }

  private JsonCallParameter callParameter() {
    return new JsonCallParameter("0x0", "0x0", "0x0", "0x0", "0x0", "");
  }

  private JsonCallParameter modifiedCallParameter() {
    return new JsonCallParameter("0x0", "0x0", Quantity.create(Long.MAX_VALUE), "0x0", "0x0", "");
  }

  private JsonRpcRequest ethEstimateGasRequest(final CallParameter callParameter) {
    return new JsonRpcRequest("2.0", "eth_estimateGas", new Object[] {callParameter});
  }
}
