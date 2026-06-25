package test.eclipse.serializer.com.basic;

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
import org.eclipse.serializer.communication.types.ComHost;

import java.nio.ByteOrder;

public class Server {

    private static int port = 30_000;
    private static ComHost<?> host;

    public static void main(String[] args) {
        String value = "ahoj";
        startServer(value);
    }


    public static void startServer(Object original) {

        host = ComBinaryDynamic.Foundation()
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

    }
}
