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
import static org.mockito.Mockito.when;

import tech.pegasys.pantheon.ethereum.jsonrpc.internal.JsonRpcRequest;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcSuccessResponse;
import tech.pegasys.pantheon.ethereum.p2p.network.P2PNetwork;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NetListeningTest {

  private NetListening method;

  @Mock private P2PNetwork p2PNetwork;

  @Before
  public void before() {
    this.method = new NetListening(p2PNetwork);
  }

  @Test
  public void shouldReturnTrueWhenNetworkIsListening() {
    when(p2PNetwork.isListening()).thenReturn(true);

    final JsonRpcRequest request = netListeningRequest();
    final JsonRpcResponse expectedResponse = new JsonRpcSuccessResponse(null, true);

    assertThat(method.response(request)).isEqualToComparingFieldByField(expectedResponse);
  }

  @Test
  public void shouldReturnFalseWhenNetworkIsNotListening() {
    when(p2PNetwork.isListening()).thenReturn(false);

    final JsonRpcRequest request = netListeningRequest();
    final JsonRpcResponse expectedResponse = new JsonRpcSuccessResponse(null, false);

    assertThat(method.response(request)).isEqualToComparingFieldByField(expectedResponse);
  }

  @Test
  public void getPermissions() {
    List<String> permissions = method.getPermissions();
    assertThat(permissions).containsExactlyInAnyOrder("net:*", "net:listening", "*:*");
  }

  private JsonRpcRequest netListeningRequest() {
    return new JsonRpcRequest("2.0", "net_listening", new Object[] {});
  }
}
