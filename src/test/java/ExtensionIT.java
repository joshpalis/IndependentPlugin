import org.junit.jupiter.api.Test;
import org.opensearch.common.component.Lifecycle;
import org.opensearch.common.network.NetworkAddress;
import org.opensearch.common.settings.Settings;
import org.opensearch.common.transport.TransportAddress;
import org.opensearch.test.OpenSearchIntegTestCase;
import org.opensearch.transport.TransportService;
import org.opensearch.transport.TransportSettings;
import java.net.*;
import java.io.*;

import transportservice.RunPlugin;
import transportservice.netty4.Netty4Transport;
import org.opensearch.threadpool.ThreadPool;

public class ExtensionIT extends OpenSearchIntegTestCase {

    // integration tests
    // - test transport information is exposed
    // - test transport request / response
    // - (SDK transport) test handshake request recieved (message "internal:tcp/handshake recieved request")
    // - (SDK transport) test handshake request response sent (message : internal:tcp/handshake sent response)
    // - (SDK action listener) test action listener work

    private volatile String clientResult;
    private String host = "127.0.0.1";

    @Test
    public void testThatInfosAreExposed() {

        int port = 9301;

        RunPlugin runPlugin = new RunPlugin();
        ThreadPool threadPool = new TestThreadPool("test");
        Settings settings = Settings.builder()
            .put("node.name", "node_extension")
            .put(TransportSettings.BIND_HOST.getKey(), host)
            .put(TransportSettings.PORT.getKey(), port)
            .build();

        Netty4Transport transport = runPlugin.getNetty4Transport(settings, threadPool);

        // start netty transport and ensure that address info is exposed
        try {
            transport.start();
            assertEquals(transport.lifecycleState(), Lifecycle.State.STARTED);

            // check bound addresses
            for (TransportAddress transportAddress : transport.boundAddress().boundAddresses()) {
                assert (transportAddress instanceof TransportAddress);
                assertEquals(host, transportAddress.getAddress());
                assertEquals(port, transportAddress.getPort());
            }

            // check publish addresses
            assert (transport.boundAddress().publishAddress() instanceof TransportAddress);
            TransportAddress publishAddress = transport.boundAddress().publishAddress();
            assertEquals(host, NetworkAddress.format(publishAddress.address().getAddress()));
            assertEquals(port, publishAddress.address().getPort());

        } finally {
            terminate(threadPool);
        }
    }

    @Test
    public void testInvalidMessageFormat() throws UnknownHostException, InterruptedException {

        // configure transport service
        int port = 9302;

        Settings settings = Settings.builder()
            .put("node.name", "node_extension_test")
            .put(TransportSettings.BIND_HOST.getKey(), host)
            .put(TransportSettings.PORT.getKey(), port)
            .build();

        Thread client = new Thread() {
            @Override
            public void run() {
                try {

                    // Connect to the server
                    Socket socket = new Socket(host, port);

                    // Create input/output stream to read/write to server
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintStream out = new PrintStream(socket.getOutputStream());

                    // server logs MESSAGE RECIEVED: TESTTT
                    // note : message validation is only done if message length >= 6 bytes
                    // character is 1 byte
                    out.print("TESTTT");

                    // Exception will originate from org.opensearch.transport.TcpTransport
                    // - invalid internal transport message format
                    // Expected behavior : transport service will close connection to client
                    // disconnection by foreign host indicated by a return value of -1
                    // only way to check if connection was closed by foreign host is to attempt to read
                    clientResult = String.valueOf(in.read());

                    // Close stream and socket connection
                    out.close();
                    socket.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        // start transport service and attempt tcp client connection
        startTransportandClient(settings, client);

        // expecting -1 from client attempt to read from server, indicating connection closed by host
        assertEquals("-1", clientResult);
    }

    @Test
    public void testMismatchingPort() throws UnknownHostException, InterruptedException {

        // configure transport service settings with correct port
        int port = 9303;

        Settings settings = Settings.builder()
            .put("node.name", "node_extension")
            .put(TransportSettings.BIND_HOST.getKey(), host)
            .put(TransportSettings.PORT.getKey(), port)
            .build();

        Thread client = new Thread() {
            @Override
            public void run() {
                try {
                    // Connect to the server using the wrong socket, will throw exception
                    Socket socket = new Socket(host, 0);
                    socket.close();
                } catch (Exception e) {
                    clientResult = e.getMessage();
                }

            }
        };

        // start transport service and attempt client connection
        startTransportandClient(settings, client);

        // expecting server response "Connection refused"
        assertEquals("Connection refused", clientResult);
    }

    // test extension handshake recieved
    @Test
    public void testHandshakeRequestRecieved() {

    }

    // test exstension handshake response
    @Test
    public void testHandshakeRequestAcknowledged() {

    }

    private void startTransportandClient(Settings settings, Thread client) throws UnknownHostException, InterruptedException {

        // retrieve transport service
        RunPlugin runPlugin = new RunPlugin();
        TransportService transportService = runPlugin.getTransportService(settings);

        // start transport service
        runPlugin.startTransportService(transportService);
        assertEquals(transportService.lifecycleState(), Lifecycle.State.STARTED);

        // connect client server to transport service
        client.start();

        // listen for messages, set timeout to close server socket connection
        runPlugin.startActionListener(1000);

        // wait for client thread to finish execution
        client.join();
    }

}
