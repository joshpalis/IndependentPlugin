import org.junit.jupiter.api.Test;
import org.opensearch.common.settings.Settings;
import org.opensearch.test.OpenSearchIntegTestCase;
import org.opensearch.transport.TransportSettings;

import transportservice.RunPlugin;
import transportservice.netty4.Netty4Transport;
import org.opensearch.threadpool.ThreadPool;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ExtensionIT extends OpenSearchIntegTestCase {

    // integration tests
    // - test transport request / response
    // - (SDK transport) test handshake request recieved (message "internal:tcp/handshake recieved request")
    // - (SDK transport) test handshake request response sent (message : internal:tcp/handshake sent response)
    // - (SDK action listener) test action listener work
    // - test tcp handshake connection reset
    // - test tcp handshake timeout
    // - test profiles
    // - test profiles include default
    // - test

    private Settings settings = Settings.builder()
        .put("node.name", "NettySizeHeaderFrameDecoderTests")
        .put(TransportSettings.BIND_HOST.getKey(), "127.0.0.1")
        .put(TransportSettings.PORT.getKey(), "9301")
        .build();


    @Test
    public void testThatInfosAreExposed() {

        RunPlugin runPlugin = new RunPlugin();
        ThreadPool threadPool = new TestThreadPool("test");

        System.out.println(TransportSettings.PORT.getKey());

        // ensure that profile addresses are exposed
        try (Netty4Transport transport = startNettyTransport(runPlugin.getNetty(settings, threadPool))) {
            
            assertEquals(1, transport.profileBoundAddresses().size());
            assertEquals(1, transport.boundAddress().boundAddresses().length);
        } finally {
            terminate(threadPool);
        }

        // ensure that bound transport addresses
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

}
