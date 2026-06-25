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


import java.nio.ByteOrder;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;

import org.eclipse.serializer.communication.binarydynamic.ComBinaryDynamic;
import org.eclipse.serializer.communication.types.ComChannel;
import org.eclipse.serializer.communication.types.ComClient;
import org.eclipse.serializer.communication.types.ComHost;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import test.eclipse.serializer.fixtures.TypeEnum;
import test.eclipse.serializer.fixtures.TypeRegister;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CommunicationTypeTest extends AbstractCommunicationTest {

    private ComHost<?> host;
    private Thread t;
    private final String original = "Some String to transfer";

    private static boolean firstTestSuccessful = false;

    //@Test
    public void receiveSimpleTypeTest() {
        Integer port = findPort();
        System.out.println(port);
        startServer(TypeEnum.Java_util_Calendar, port);

        final ComClient<?> client = ComBinaryDynamic.Client(port);

        // create a channel by connecting the client
        final ComChannel channel = client.connect(10, Duration.ofMillis(500));

        final Object o = channel.receive();

        channel.close();

        Assertions.assertNotNull(o);
        Assertions.assertInstanceOf(TypeEnum.Java_util_Calendar.getOriginal().getClass(), o);


    }



    //@Test
    //@Order(1)
    //@RepeatedTest(1000)

    public void receiveTypeRegisterTest() {

        Integer port = findFreePort();
        //System.out.println(port);
        TypeRegister typeRegister = new TypeRegister();
        typeRegister.fillSampleDate();
        startServer(typeRegister, port);

        final ComClient<?> client = ComBinaryDynamic.Client(port);

        // create a channel by connecting the client
        final ComChannel channel = client.connect(20, Duration.ofMillis(250));

        final Object o = channel.receive();
        TypeRegister transferedTypeRegister = (TypeRegister) o;

        channel.close();

        Assertions.assertNotNull(o);
        typeRegister.proveData(transferedTypeRegister,
				"hashTableMSData", "identityHashMapData", "lazyData", "calendarApiData");

        firstTestSuccessful = true;
    }

    @ParameterizedTest
    @EnumSource(value = TypeEnum.class)
    //@Order(2)
    //@DisabledIf("firstTestSuccessful")
    public void receiveTypeTest(TypeEnum type) {

        Integer port = findFreePort();
        //System.out.println(port);
        startServer(type.getOriginal(), port);

        final ComClient<?> client = ComBinaryDynamic.Client(port);

        // create a channel by connecting the client
        final ComChannel channel = client.connect(20, Duration.ofMillis(250));

        final Object o = channel.receive();

        channel.close();

        Assertions.assertNotNull(o);
        Assertions.assertInstanceOf(type.getOriginal().getClass(), o);

    }

    static boolean firstTestSuccessful()
    {
        DayOfWeek day = LocalDate.now().getDayOfWeek();
        return firstTestSuccessful && !(day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY);
    }

    public void startServer(Object type, Integer port) {

        t = new Thread(() -> {
            host = ComBinaryDynamic.Foundation()
                    .setHostByteOrder(ByteOrder.BIG_ENDIAN)
                    .setPort(port)
                    .setHostChannelAcceptor(hostChannel ->
                    {
                        hostChannel.send(type);

                        host.stop();

                    })
                    .createHost()
            ;

            //System.out.println("Started server for type " + type.getOriginal().getClass().getName() + " for port: " + port);
            host.run();
        });
        t.setUncaughtExceptionHandler((t, e) -> e.printStackTrace());
        t.start();

    }

    @AfterEach
    public void stopServer() throws InterruptedException {
        try {
            host.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
        t.join(1000);
    }

}
