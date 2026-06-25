package test.eclipse.serializer.communication.tls;

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


import org.eclipse.serializer.com.ComException;
import org.eclipse.serializer.communication.binarydynamic.ComBinaryDynamic;
import org.eclipse.serializer.communication.tls.*;
import org.eclipse.serializer.communication.types.ComChannel;
import org.eclipse.serializer.communication.types.ComClient;
import org.eclipse.serializer.communication.types.ComHost;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.eclipse.serializer.fixtures.TypeRegister;

import java.nio.ByteOrder;
import java.nio.file.Paths;

public class ClientWithoutTLSTest extends AbstractSecurityComTest {

    private Thread t;
    private final String pksPath = super.findPksPath();
    private ComHost<?> host;
    private final int port = 51_005;

    @Test
    public void clientTlsClientTest() {

        final ComClient<?> client = ComBinaryDynamic.Client(port);

        // create a channel by connecting the client
        final ComChannel channel;
        Assertions.assertThrows(ComException.class, client::connect, "https://github.com/microstream-one/microstream-private/issues/314");
        //client.connect();
    }


    @BeforeEach
    public void setupServer() throws InterruptedException {
        final String largeString = createLargeString(10);
        host = ComBinaryDynamic.Foundation()
                .setHostByteOrder(ByteOrder.BIG_ENDIAN)
                .setPort(port)
                .setConnectionHandler(ComTLSConnectionHandler.New(
                        new TLSKeyManagerProvider.PKCS12(Paths.get(pksPath), password),
                        new TLSTrustManagerProvider.PKCS12(Paths.get(pksPath), password),
                        new TLSParametersProvider.Default(),
                        new SecureRandomProvider.Default()
                ))
                .setHostChannelAcceptor(hostChannel -> {
                    hostChannel.send(new TypeRegister());

                    hostChannel.send(largeString);

                })
                .createHost();
        t = new Thread(() -> {
            host.run();
        });
        t.start();
        Thread.sleep(1000);
    }

    @AfterEach
    public void stopServer() throws InterruptedException {
        //System.out.println("is running: " +  host.isRunning());
        host.stop();
        t.join(1000);
    }

    private static String createLargeString(final int lines) {
        StringBuilder largeString = new StringBuilder();

        for (int i = 1; i <= lines; i++) {
            largeString.append(i).append(" a large String with ").append(lines).append(" lines. This is line no. ").append(i).append(" of ").append(lines).append("\n");
        }

        return largeString.toString();
    }
}
