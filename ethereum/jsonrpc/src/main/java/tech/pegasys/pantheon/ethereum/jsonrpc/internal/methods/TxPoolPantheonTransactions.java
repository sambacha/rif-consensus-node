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

import tech.pegasys.pantheon.ethereum.eth.transactions.PendingTransactions;
import tech.pegasys.pantheon.ethereum.jsonrpc.RpcMethod;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.JsonRpcRequest;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcSuccessResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.results.PendingTransactionsResult;

public class TxPoolPantheonTransactions implements JsonRpcMethod {

  private final PendingTransactions pendingTransactions;

  public TxPoolPantheonTransactions(final PendingTransactions pendingTransactions) {
    this.pendingTransactions = pendingTransactions;
  }

  @Override
  public String getName() {
    return RpcMethod.TX_POOL_PANTHEON_TRANSACTIONS.getMethodName();
  }

  @Override
  public JsonRpcResponse response(final JsonRpcRequest request) {
    final JsonRpcSuccessResponse jsonRpcSuccessResponse =
        new JsonRpcSuccessResponse(
            request.getId(),
            new PendingTransactionsResult(pendingTransactions.getTransactionInfo()));
    return jsonRpcSuccessResponse;
  }
}
