package test.eclipse.serializer.communication;

/*-
 * #%L
 * Eclipse Serializer Integration Tests
 * %%
 * Copyright (C) 2023 - 2026 MicroStream Software
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Assertions;

public abstract class AbstractCommunicationTest
{

    private static final AtomicInteger port = new AtomicInteger(49_500);

    protected static synchronized Integer findPort()
    {
        return port.addAndGet(3);
    }


    public static int findFreePort()
    {
        Integer port = null;
        try (ServerSocket serverSocket = new ServerSocket(0)) {

            port = serverSocket.getLocalPort();
            // setReuseAddress(false) is required only on OSX,
            // otherwise the code will not work correctly on that platform
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Assertions.assertNotNull(port);
        return port;
    }

    public static boolean isTcpPortAvailable(int port)
    {
        try (ServerSocket serverSocket = new ServerSocket()) {
            // setReuseAddress(false) is required only on OSX,
            // otherwise the code will not work correctly on that platform
            serverSocket.setReuseAddress(false);
            serverSocket.bind(new InetSocketAddress(InetAddress.getByName("localhost"), port), 1);
            return true;
        } catch (Exception ex) {
            System.err.println("Port not available" + port);
            System.err.println("XXXXXXXX");
            return false;
        }
    }

    protected void waitForPortToBeAvailable(int port) throws IOException
    {
        boolean scanning = true;
        int counter = 0;
        SocketChannel channel = null;
        while (scanning) {
            try {
                channel = SocketChannel.open(new InetSocketAddress(InetAddress.getLocalHost(), port));
                scanning = false;
            } catch (Exception e) {
                counter++;
                if (counter > 50) {
                    throw new RuntimeException("Cannot connect to web server on port:." + port);
                }
                System.out.println("Connect failed, waiting and trying again: " + counter);
                try {
                    Thread.sleep(500);//2 seconds
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            } finally {
                if (channel != null) {
                    channel.close();
                }
            }
        }
    }

    /**
     * Utility for testing purpose only, make a connection and do not close the socket after.
     *
     * @param host
     * @param port
     */
    protected static void waitForPortToBeAvailableWithoutClose(String host, int port)
    {
        boolean scanning = true;
        int counter = 0;
        while (scanning) {
            try {
                SocketChannel.open(new InetSocketAddress(host, port));
                scanning = false;
            } catch (Exception e) {
                counter++;
                if (counter > 50) {
                    throw new RuntimeException("Cannot connect to web server on port:." + port);
                }
                System.out.println("Connect failed, waiting and trying again: " + counter);
                try {
                    Thread.sleep(500);//2 seconds
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }
    }

}
