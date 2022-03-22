import org.opensearch.common.io.stream.NamedWriteableRegistry;
import org.opensearch.common.network.NetworkService;
import org.opensearch.common.settings.Settings;
import org.opensearch.common.transport.BoundTransportAddress;
import org.opensearch.common.transport.TransportAddress;
import org.opensearch.common.unit.TimeValue;
import org.opensearch.common.util.PageCacheRecycler;
import org.opensearch.indices.breaker.NoneCircuitBreakerService;
import org.opensearch.search.SearchModule;
import org.opensearch.test.OpenSearchTestCase;
import org.opensearch.threadpool.TestThreadPool;
import org.opensearch.threadpool.ThreadPool;
import transportservice.transport.ConnectionManager;
import org.opensearch.transport.TransportRequestOptions;
import transportservice.TransportService;
import org.opensearch.transport.TransportSettings;
import org.opensearch.Version;
import org.opensearch.cluster.node.DiscoveryNode;
import org.opensearch.common.SuppressForbidden;

import transportservice.SharedGroupFactory;
import transportservice.TransportInterceptor;
import transportservice.netty4.Netty;
import transportservice.transport.ClusterConnectionManager;
import transportservice.transport.ConnectTransportException;
import transportservice.transport.ConnectionProfile;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.function.Function;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.mockito.ArgumentMatchers.startsWith;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.apache.lucene.util.SetOnce;
import org.junit.Before;
import org.junit.Assert.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestActionListener extends OpenSearchTestCase {

    public static final TransportInterceptor NOOP_TRANSPORT_INTERCEPTOR = new TransportInterceptor() {
    };
    private static final Version CURRENT_VERSION = Version.fromString(String.valueOf(Version.CURRENT.major) + ".0.0");
    protected static final Version version0 = CURRENT_VERSION.minimumCompatibilityVersion();

    public TransportService startTransportService() {
        Settings settings = Settings.builder()
        .put("node.name", "NettySizeHeaderFrameDecoderTests")
        .put(TransportSettings.BIND_HOST.getKey(), "127.0.0.1")
        .put(TransportSettings.PORT.getKey(), "0")
        .build();
        ThreadPool threadPool = new TestThreadPool("test");
        Netty transport = createNetty(settings, threadPool);
        ConnectionManager connectionManager = new ClusterConnectionManager(settings, transport);
        TransportService transportService = new TransportService(transport, connectionManager, transport.getResponseHandlers(), threadPool, new LocalNodeFactory(settings, "5"), NOOP_TRANSPORT_INTERCEPTOR);

        transportService.start();
        transportService.acceptIncomingRequests();

        return transportService;
    }

    
    // @Test
    // public void testMessageRecieved() throws IOException, InterruptedException{
        
    //     TransportService transportService = startTransportService();
    
    //     try (ServerSocket socket = new ServerSocket()) {
    //         socket.bind(getLocalEphemeral(), 1);
    //         socket.setReuseAddress(true);
    //         DiscoveryNode dummy = new DiscoveryNode(
    //             "TEST",
    //             new TransportAddress(socket.getInetAddress(), socket.getLocalPort()),
    //             emptyMap(),
    //             emptySet(),
    //             version0
    //         );
    //         Thread t = new Thread() {
    //             @Override
    //             public void run() {
    //                 try (Socket accept = socket.accept()) {
    //                     if (randomBoolean()) { // sometimes wait until the other side sends the message
    //                         accept.getInputStream().read();
    //                     }
    //                 } catch (IOException e) {
    //                     throw new UncheckedIOException(e);
    //                 }
    //             }
    //         };
    //         t.start();
    //         ConnectionProfile.Builder builder = new ConnectionProfile.Builder();
    //         builder.addConnections(
    //             1,
    //             TransportRequestOptions.Type.BULK,
    //             TransportRequestOptions.Type.PING,
    //             TransportRequestOptions.Type.RECOVERY,
    //             TransportRequestOptions.Type.REG,
    //             TransportRequestOptions.Type.STATE
    //         );
    //         builder.setHandshakeTimeout(TimeValue.timeValueHours(1));
    //         transportService.connectToNode(dummy, builder.build());

    //         // TODO : figure out how to send message and assert that message sent was the same


    //         t.join();
    //     }
    // }

    // configure and start netty
    private Netty createNetty(Settings settings, ThreadPool threadpool){
        PageCacheRecycler pageCacheRecycler = new PageCacheRecycler(settings);
        Netty transport = new Netty(
            settings,
            Version.CURRENT,
            threadpool,
            new NetworkService(Collections.emptyList()),
            pageCacheRecycler,
            new NamedWriteableRegistry(new SearchModule(Settings.EMPTY, Collections.emptyList()).getNamedWriteables()),
            new NoneCircuitBreakerService(),
            new SharedGroupFactory(settings)
        );

        transport.start();
        return transport;
    }

    // test : Default profile inherits from transport settings
    @Test
    public void testNettyDefaultProfileOverrideGeneralConfiguration() throws Exception{
        Settings settings = Settings.builder()
        .put("node.name", "NettySizeHeaderFrameDecoderTests")
        .put(TransportSettings.BIND_HOST.getKey(), "127.0.0.1")
        .put(TransportSettings.PORT.getKey(), "0")
        .put("transport.profiles.default.port", 1) // will get overrided by default settings 
        .build();
        
        ThreadPool threadPool = new TestThreadPool("default");
        try (Netty transport = createNetty(settings, threadPool)) {
            
            // profile should be 1, bound addresses should be [::1] (IPV6 loopback address) && [127.0.0.1] (IPV4)
            System.out.println(transport.profileBoundAddresses());
            assertEquals(1, transport.profileBoundAddresses().size());
            assertEquals(2, transport.boundAddress().boundAddresses().length);

        } finally {
            terminate(threadPool);
        }
    }

    // test : Transport Service Num bound addresses
    @Test
    public void testNettyBoundAddresses() throws Exception{
        Settings settings = Settings.builder()
        .put("node.name", "NettySizeHeaderFrameDecoderTests")
        .put(TransportSettings.BIND_HOST.getKey(), "127.0.0.1")
        .put(TransportSettings.PORT.getKey(), "0")
        .build();
        ThreadPool threadPool = new TestThreadPool("test");
        try (Netty transport = createNetty(settings, threadPool)) {
            
            // profile should be 1, bound addresses should be [::1] (IPV6 loopback address) && [127.0.0.1] (IPV4)
            assertEquals(1, transport.profileBoundAddresses().size());
            assertEquals(2, transport.boundAddress().boundAddresses().length);

        } finally {
            terminate(threadPool);
        }
    }

    @SuppressForbidden(reason = "need local ephemeral port")
    protected InetSocketAddress getLocalEphemeral() throws UnknownHostException {
        return new InetSocketAddress(InetAddress.getLocalHost(), 0);
    }

    private static class LocalNodeFactory implements Function<BoundTransportAddress, DiscoveryNode> {
        private final SetOnce<DiscoveryNode> localNode = new SetOnce<>();
        private final String persistentNodeId;
        private final Settings settings;

        private LocalNodeFactory(Settings settings, String persistentNodeId) {
            this.persistentNodeId = persistentNodeId;
            this.settings = settings;
        }

        @Override
        public DiscoveryNode apply(BoundTransportAddress boundTransportAddress) {
            localNode.set(DiscoveryNode.createLocal(settings, boundTransportAddress.publishAddress(), persistentNodeId));
            return localNode.get();
        }

        DiscoveryNode getNode() {
            assert localNode.get() != null;
            return localNode.get();
        }
    }

}