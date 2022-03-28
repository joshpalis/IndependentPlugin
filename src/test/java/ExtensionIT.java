import org.junit.jupiter.api.Test;
import org.opensearch.common.component.Lifecycle;
import org.opensearch.common.network.NetworkAddress;
import org.opensearch.common.settings.Settings;
import org.opensearch.common.transport.TransportAddress;
import org.opensearch.test.OpenSearchIntegTestCase;
import org.opensearch.transport.TransportSettings;

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

    private String address = "127.0.0.1";
    private int port = 9301;

    @Test
    public void testThatInfosAreExposed() {

        RunPlugin runPlugin = new RunPlugin();
        ThreadPool threadPool = new TestThreadPool("test");
        Settings settings = Settings.builder()
            .put("node.name", "IntegrationTests")
            .put(TransportSettings.BIND_HOST.getKey(), address)
            .put(TransportSettings.PORT.getKey(), port)
            .build();

        // start netty transport and ensure that address info is exposed
        try (Netty4Transport transport = startNettyTransport(runPlugin.getNetty(settings, threadPool))) {

            // check bound addresses
            for (TransportAddress transportAddress : transport.boundAddress().boundAddresses()) {
                assert (transportAddress instanceof TransportAddress);
                assertEquals(address, transportAddress.getAddress());
                assertEquals(port, transportAddress.getPort());
            }

            // check publish addresses
            assert (transport.boundAddress().publishAddress() instanceof TransportAddress);
            TransportAddress publishAddress = transport.boundAddress().publishAddress();
            assertEquals(address, NetworkAddress.format(publishAddress.address().getAddress()));
            assertEquals(port, publishAddress.address().getPort());

        } finally {
            terminate(threadPool);
        }
    }

    @Test
    public void testHandshakeRequestRecieved() {

        // questions :
        // How to send a handshake request
        // How to check recieved message

        // what do I need to begin writing tests :
        // 1) How do I send a handshake

        // start action listener to recieve request
        // create simple tcp client to send in a handshake request message
        // assert that action listener recieved the same request message

        // start action listener to recieve request
        // ActionListener actionListener = new ActionListener();
        // actionListener.runActionListener(true);

        // start tcp client and send handshake request message

    }

    @Test
    public void testHandshakeRequestAcknowledged() {

    }

    @Test
    public void testTcpHandshakeTimeout() {

    }

    // helper method to ensure netty transport was started
    private Netty4Transport startNettyTransport(Netty4Transport transport) {
        transport.start();
        assertEquals(transport.lifecycleState(), Lifecycle.State.STARTED);
        return transport;
    }

}
