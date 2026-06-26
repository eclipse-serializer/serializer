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


import java.nio.ByteOrder;
import java.nio.file.Paths;
import java.time.Duration;

import org.eclipse.serializer.communication.binarydynamic.ComBinaryDynamic;
import org.eclipse.serializer.communication.tls.*;
import org.eclipse.serializer.communication.types.ComChannel;
import org.eclipse.serializer.communication.types.ComClient;
import org.eclipse.serializer.communication.types.ComHost;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CommunicationTlsTest extends AbstractSecurityComTest
{

    private ComHost<?> host;
    private Thread t;
    private final String original = "Some String to transfer";
    private final int port = findFreePort();

    protected char[] password = new char[]{'m', 'i', 'c', 'r', 'o', 's', 't', 'r', 'e', 'a', 'm'};


    @Test
    public void receiveBasicObjectTest()
    {

        String pksPath = findPksPath();

        final ComClient<?> client = prepareClient(pksPath);

        // create a channel by connecting the client
        final ComChannel channel = client.connect(10, Duration.ofMillis(1_000));

        final Object o = channel.receive();

        channel.close();

        String copy = (String) o;

        System.out.println(copy);
        Assertions.assertNotNull(o);

    }

    private ComClient<?> prepareClient(String pksPath)
    {
        return ComBinaryDynamic.Foundation()
                .setConnectionHandler(ComTLSConnectionHandler.New(
                        new TLSKeyManagerProvider.PKCS12(Paths.get(pksPath), password),
                        new TLSTrustManagerProvider.PKCS12(Paths.get(pksPath), password),
                        new TLSParametersProvider.Default(),
                        new SecureRandomProvider.Default()
                ))
                .setPort(this.port)
                .createClient();
    }


    @BeforeEach
    public void startServer()
    {

        String pksPath = findPksPath();

        t = new Thread(() -> {
            host = ComBinaryDynamic.Foundation()
                    .setHostByteOrder(ByteOrder.BIG_ENDIAN)
                    .setPort(this.port)
                    .setConnectionHandler(ComTLSConnectionHandler.New(
                            new TLSKeyManagerProvider.PKCS12(Paths.get(pksPath), password),
                            new TLSTrustManagerProvider.PKCS12(Paths.get(pksPath), password),
                            new TLSParametersProvider.Default(),
                            new SecureRandomProvider.Default()
                    ))
                    .setHostChannelAcceptor(hostChannel ->
                    {
                        //hostChannel.send(original);
                        hostChannel.send(original);

                        //final Object o = hostChannel.receive();
                        //wrXDebug.println("HOST RECEIVED: " + o.toString());
                        host.stop();

                    })
                    .createHost()
            ;

            // run the host, making it constantly listen for new connections and relaying them to the logic
            host.run();
        });
        t.start();

    }

    @AfterEach
    public void stopServer() throws InterruptedException
    {
        //System.out.println("is running: " +  host.isRunning());
        try {
            host.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Thread.sleep(500);
        //t.interrupt();
        t.join(1000);
        //Thread.sleep(500);
    }


}
