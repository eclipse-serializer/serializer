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


import java.net.InetSocketAddress;
import java.time.Duration;

import org.eclipse.serializer.communication.binarydynamic.ComBinaryDynamic;
import org.eclipse.serializer.communication.types.ComChannel;
import org.eclipse.serializer.communication.types.ComClient;

public class Client
{

    public static void main(String[] args)
    {

        int port = 30_000;
        String serverAddress = "192.168.1.104";
        final ComClient<?> client = ComBinaryDynamic.Client(new InetSocketAddress(serverAddress, port));
        // create a channel by connecting the client
        final ComChannel channel = client.connect(10, Duration.ofMillis(500));

        final Object o = channel.receive();

        channel.close();

        String copy = (String) o;

        System.out.println(copy);

    }
}
