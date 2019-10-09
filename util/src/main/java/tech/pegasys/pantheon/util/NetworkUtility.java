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

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;

public class NetworkUtility {

  private NetworkUtility() {}

  private static final Supplier<Boolean> ipv6Available =
      Suppliers.memoize(NetworkUtility::checkIpv6Availability);

  /**
   * Is IPv6 available?
   *
   * @return Returns true if the machine reports having any IPv6 addresses.
   */
  public static boolean isIPv6Available() {
    return ipv6Available.get();
  }

  /**
   * The standard for IPv6 availability is if the machine has any IPv6 addresses.
   *
   * @return Returns true if any IPv6 addresses are iterable via {@link NetworkInterface}.
   */
  private static Boolean checkIpv6Availability() {
    try {
      final Enumeration<NetworkInterface> networkInterfaces =
          NetworkInterface.getNetworkInterfaces();
      while (networkInterfaces.hasMoreElements()) {
        final Enumeration<InetAddress> addresses =
            networkInterfaces.nextElement().getInetAddresses();
        while (addresses.hasMoreElements()) {
          if (addresses.nextElement() instanceof Inet6Address) {
            // Found an IPv6 address, hence the IPv6 stack is available.
            return true;
          }
        }
      }
    } catch (final Exception ignore) {
      // Any exception means we treat it as not available.
    }
    return false;
  }

  /**
   * Checks the port is not null and is in the valid range port (1-65536).
   *
   * @param port The port to check.
   * @return True if the port is valid, false otherwise.
   */
  public static boolean isValidPort(final int port) {
    return port > 0 && port < 65536;
  }

  public static String urlForSocketAddress(final String scheme, final InetSocketAddress address) {
    String hostName = address.getHostName();
    if ("0.0.0.0".equals(hostName)) {
      hostName = InetAddress.getLoopbackAddress().getHostName();
    }
    if ("0:0:0:0:0:0:0:0".equals(hostName)) {
      hostName = InetAddress.getLoopbackAddress().getHostName();
    }
    if (hostName.contains(":")) {
      hostName = "[" + hostName + "]";
    }
    return scheme + "://" + hostName + ":" + address.getPort();
  }
}
