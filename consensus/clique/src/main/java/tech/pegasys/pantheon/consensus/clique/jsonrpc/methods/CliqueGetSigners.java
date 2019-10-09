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
package tech.pegasys.pantheon.consensus.clique.jsonrpc.methods;

import tech.pegasys.pantheon.consensus.common.VoteTallyCache;
import tech.pegasys.pantheon.ethereum.core.BlockHeader;
import tech.pegasys.pantheon.ethereum.jsonrpc.RpcMethod;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.JsonRpcRequest;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.methods.JsonRpcMethod;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.parameters.BlockParameter;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.parameters.JsonRpcParameter;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.queries.BlockWithMetadata;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.queries.BlockchainQueries;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcError;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcErrorResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcSuccessResponse;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class CliqueGetSigners implements JsonRpcMethod {
  private final BlockchainQueries blockchainQueries;
  private final VoteTallyCache voteTallyCache;
  private final JsonRpcParameter parameters;

  public CliqueGetSigners(
      final BlockchainQueries blockchainQueries,
      final VoteTallyCache voteTallyCache,
      final JsonRpcParameter parameter) {
    this.blockchainQueries = blockchainQueries;
    this.voteTallyCache = voteTallyCache;
    this.parameters = parameter;
  }

  @Override
  public String getName() {
    return RpcMethod.CLIQUE_GET_SIGNERS.getMethodName();
  }

  @Override
  public JsonRpcResponse response(final JsonRpcRequest request) {
    final Optional<BlockHeader> blockHeader = determineBlockHeader(request);
    return blockHeader
        .map(bh -> voteTallyCache.getVoteTallyAfterBlock(bh).getValidators())
        .map(addresses -> addresses.stream().map(Objects::toString).collect(Collectors.toList()))
        .<JsonRpcResponse>map(addresses -> new JsonRpcSuccessResponse(request.getId(), addresses))
        .orElse(new JsonRpcErrorResponse(request.getId(), JsonRpcError.INTERNAL_ERROR));
  }

  private Optional<BlockHeader> determineBlockHeader(final JsonRpcRequest request) {
    final Optional<BlockParameter> blockParameter =
        parameters.optional(request.getParams(), 0, BlockParameter.class);
    final long latest = blockchainQueries.headBlockNumber();
    final long blockNumber = blockParameter.map(b -> b.getNumber().orElse(latest)).orElse(latest);
    return blockchainQueries.blockByNumber(blockNumber).map(BlockWithMetadata::getHeader);
  }
}
