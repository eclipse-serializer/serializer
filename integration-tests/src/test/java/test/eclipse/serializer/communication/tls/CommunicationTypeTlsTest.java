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
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;

import org.eclipse.serializer.communication.binarydynamic.ComBinaryDynamic;
import org.eclipse.serializer.communication.tls.*;
import org.eclipse.serializer.communication.types.ComChannel;
import org.eclipse.serializer.communication.types.ComClient;
import org.eclipse.serializer.communication.types.ComHost;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import test.eclipse.serializer.fixtures.TypeEnum;
import test.eclipse.serializer.fixtures.TypeRegister;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CommunicationTypeTlsTest extends AbstractSecurityComTest
{

    private ComHost<?> host;
    private Thread t;
    private final String original = "Some String to transfer";

    protected char[] password = new char[]{'m', 'i', 'c', 'r', 'o', 's', 't', 'r', 'e', 'a', 'm'};


    private static boolean firstTestSuccessful = false;

//    @Test
//    @Order(1)
    public void receiveTypeRegisterTest()
    {
        Integer port = findFreePort();
        TypeRegister typeRegister = new TypeRegister();

        startServer(typeRegister, port);

        final ComClient<?> client = prepareClient(findPksPath(), port);

        // create a channel by connecting the client
        final ComChannel channel = client.connect(20, Duration.ofMillis(250));

        final Object o = channel.receive();
        TypeRegister transferedTypeRegister = (TypeRegister) o;

        channel.close();

        Assertions.assertNotNull(o);
        typeRegister.proveData(transferedTypeRegister,"hashTableMSData", "identityHashMapData", "lazyData");

        firstTestSuccessful = true;
    }

//	@Test
//	@Order(2)
//	@EnabledForJreRange(min = JRE.JAVA_26)
//	public void receiveTypeRegisterTestJava26()
//	{
//		Integer port = findFreePort();
//		TypeRegister typeRegister = new TypeRegister();
//
//		startServer(typeRegister, port);
//
//		final ComClient<?> client = prepareClient(findPksPath(), port);
//
//		// create a channel by connecting the client
//		final ComChannel channel = client.connect(20, Duration.ofMillis(250));
//
//		final Object o = channel.receive();
//		TypeRegister transferedTypeRegister = (TypeRegister) o;
//
//		channel.close();
//
//		Assertions.assertNotNull(o);
//		typeRegister.proveData(transferedTypeRegister,"hashTableMSData", "identityHashMapData", "lazyData", "localDateData",
//				"localDateTimeData", "calendarApiData"
//		);
//		firstTestSuccessful = true;
//	}



	@ParameterizedTest
    @EnumSource(value = TypeEnum.class)
    public void receiveTypeTest(TypeEnum type)
    {
        Integer port = findFreePort();
        startServer(type.getOriginal(), port);

        final ComClient<?> client = prepareClient(findPksPath(), port);

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

    private ComClient<?> prepareClient(String pksPath, Integer port)
    {
        return ComBinaryDynamic.Foundation()
                .setConnectionHandler(ComTLSConnectionHandler.New(
                        new TLSKeyManagerProvider.PKCS12(Paths.get(pksPath), password),
                        new TLSTrustManagerProvider.PKCS12(Paths.get(pksPath), password),
                        new TLSParametersProvider.Default(),
                        new SecureRandomProvider.Default()
                ))
                .setPort(port)
                .createClient();
    }


    public void startServer(Object type, Integer port)
    {

        String pksPath = findPksPath();

        t = new Thread(() -> {
            host = ComBinaryDynamic.Foundation()
                    .setHostByteOrder(ByteOrder.BIG_ENDIAN)
                    .setPort(port)
                    .setConnectionHandler(ComTLSConnectionHandler.New(
                            new TLSKeyManagerProvider.PKCS12(Paths.get(pksPath), password),
                            new TLSTrustManagerProvider.PKCS12(Paths.get(pksPath), password),
                            new TLSParametersProvider.Default(),
                            new SecureRandomProvider.Default()
                    ))
                    .setHostChannelAcceptor(hostChannel ->
                    {
                        hostChannel.send(type);

                        host.stop();

                    })
                    .createHost()
            ;

            // run the host, making it constantly listen for new connections and relaying them to the logic
            //System.out.println("Started server for type " + type.getOriginal().getClass().getName() + " for port: " + port);
            host.run();
        });
        t.setUncaughtExceptionHandler((t, e) -> e.printStackTrace());
        t.start();

    }

    @AfterEach
    public void stopServer() throws InterruptedException
    {
        try {
            host.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
        t.join(1000);
    }

}
