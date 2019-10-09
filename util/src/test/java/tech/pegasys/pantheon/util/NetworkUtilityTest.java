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
package tech.pegasys.pantheon.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.InetSocketAddress;

import org.junit.Test;

public class NetworkUtilityTest {

  @Test
  public void urlForSocketAddressHandlesIPv6() {
    final InetSocketAddress ipv6All = new InetSocketAddress("::", 80);
    assertFalse(NetworkUtility.urlForSocketAddress("http", ipv6All).contains("::"));
    assertFalse(NetworkUtility.urlForSocketAddress("http", ipv6All).contains("0:0:0:0:0:0:0:0"));
    final InetSocketAddress ipv6 = new InetSocketAddress("1:2:3:4:5:6:7:8", 80);
    assertTrue(NetworkUtility.urlForSocketAddress("http", ipv6).contains("[1:2:3:4:5:6:7:8]"));
  }
}
