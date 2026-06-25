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


import org.eclipse.serializer.communication.binarydynamic.ComBinaryDynamic;
import org.eclipse.serializer.communication.types.ComChannel;
import org.eclipse.serializer.communication.types.ComClient;
import org.eclipse.serializer.communication.types.ComHost;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteOrder;
import java.time.Duration;

public class CommunicationTest extends AbstractCommunicationTest{

    private ComHost<?> host;
    private Thread t;
    private final String original = "Some String to transfer";

    private int port;

    @Test
    public void receiveBasicObjectTest() {
        final ComClient<?> client = ComBinaryDynamic.Client(port);
        // create a channel by connecting the client
        final ComChannel channel = client.connect(10, Duration.ofMillis(500));

        final Object o = channel.receive();

        channel.close();

        String copy = (String) o;

        System.out.println(copy);
        Assertions.assertNotNull(o);

    }

    @Test
    public void basicTestWithPortCheck() throws IOException, InterruptedException {
        waitForPortToBeAvailable(port);
        System.out.println("Port Available, i can continue");
        final ComClient<?> client = ComBinaryDynamic.Client(port);
        // create a channel by connecting the client
        final ComChannel channel = client.connect(100, Duration.ofMillis(500));

        final Object o = channel.receive();

        channel.close();

        String copy = (String) o;

        System.out.println(copy);
        Assertions.assertNotNull(o);

    }

    @BeforeEach
    public void startServer() throws InterruptedException {
        port = findFreePort();
        t = new Thread(() -> {
            host = ComBinaryDynamic.Foundation()
                    .setHostByteOrder(ByteOrder.BIG_ENDIAN)
                    .setPort(port)
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
    public void stopServer() throws InterruptedException {
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
