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
package tech.pegasys.pantheon.tests.acceptance.dsl.node.configuration;

import tech.pegasys.pantheon.tests.acceptance.dsl.node.configuration.genesis.GenesisConfigurationProvider;

import java.net.URI;
import java.util.List;
import java.util.Optional;

public interface NodeConfiguration {

  void setBootnodes(List<URI> bootnodes);

  List<URI> getBootnodes();

  void useWebSocketsForJsonRpc();

  void useAuthenticationTokenInHeaderForJsonRpc(String token);

  Optional<Integer> getJsonRpcWebSocketPort();

  String getHostName();

  boolean isJsonRpcEnabled();

  GenesisConfigurationProvider getGenesisConfigProvider();

  Optional<String> getGenesisConfig();

  void setGenesisConfig(final String config);

  boolean isP2pEnabled();

  boolean isDiscoveryEnabled();

  boolean isBootnodeEligible();

  List<String> getExtraCLIOptions();
}
