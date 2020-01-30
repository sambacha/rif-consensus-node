/*
 * Copyright ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */
package org.hyperledger.besu.ethereum.vm;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hyperledger.besu.ethereum.core.BlockHeaderMock;

@JsonIgnoreProperties({"_info", "callcreates", "logs"})
public class UniTrieVMReferenceTestCaseSpec extends AbstractVMReferenceTestCaseSpec {

  @JsonCreator
  public UniTrieVMReferenceTestCaseSpec(
      @JsonProperty("exec") final EnvironmentInformation exec,
      @JsonProperty("env") final BlockHeaderMock env,
      @JsonProperty("gas") final String finalGas,
      @JsonProperty("out") final String out,
      @JsonProperty("pre") final UniTrieWorldStateMock initialWorldState,
      @JsonProperty("post") final UniTrieWorldStateMock finalWorldState) {
    super(exec, env, finalGas, out, initialWorldState, finalWorldState);
  }
}
