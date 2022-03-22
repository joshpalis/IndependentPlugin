import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.hamcrest.Matchers.is;

import java.util.Collections;

import org.apache.logging.log4j.core.LifeCycle;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opensearch.common.io.stream.NamedWriteableRegistry;
import org.opensearch.common.network.NetworkService;
import org.opensearch.common.settings.Settings;
import org.opensearch.common.util.PageCacheRecycler;
import org.opensearch.indices.breaker.NoneCircuitBreakerService;
import org.opensearch.search.SearchModule;
import org.opensearch.test.OpenSearchTestCase;
import org.opensearch.threadpool.ThreadPool;
import org.opensearch.transport.TransportSettings;

import org.opensearch.Version;
import transportservice.transport.*;
import transportservice.RunPlugin;
import transportservice.SharedGroupFactory;
import transportservice.netty4.Netty;
import transportservice.TestThreadPool;
import transportservice.TransportService;

@RunWith(MockitoJUnitRunner.class)
public class TestMainScript extends OpenSearchTestCase{

    @Mock 
    private TransportService transportService;

    // test transport service was started
    //@Test
    public void testTransportServiceStarted() {

        try (MockedStatic<RunPlugin> rp = Mockito.mockStatic(RunPlugin.class)) {
            rp.verify(() -> RunPlugin.startTransportService(transportService));
        }

    }




    // // TESTS : Constructor method parameter types 

    // @Test
    // public void testTransportServiceCreation(){
        
    //     RunPlugin mockedRunPlugin = mock(RunPlugin.class);
    //     Netty mockedNetty = mock(Netty.class);
    //     ConnectionManager mockedConnectionManager = mock(ConnectionManager.class);
    //     ThreadPool mockThreadPool = mock(ThreadPool.class);

    //     makeTransportService(mockedNetty, mockedConnectionManager, mockThreadPool);
    //     verify(mockedRunPlugin, times(1)).makeTransportService(any(), any(), any());
    // }

    // // dummy test
    // @Test
    // public void testConnectionManagerCreation(){
    //     RunPlugin mockedRunPlugin = mock(RunPlugin.class);
    //     Netty mockedNetty = mock(Netty.class);

    //     mockedRunPlugin.makeConnectionManager(Settings.builder().build(), mockedNetty);
    //     verify(mockedRunPlugin, times(1)).makeConnectionManager(any(), any());
    // }



    // test : connection exception
    // test : Transport response on HTTP requests
    // test : Nothing is Returned for other invalid packets

    // TODO : Test netty creation, currently cannot mock network service since it is final class

    // @Test
    // public void testNettyCreation(){
    //     System.out.println("TEST : Netty");
    //     mockedRunPlugin.makeNettyTransport(
    //         mockedSettings, 
    //         mockedVersion, 
    //         mockThreadPool, 
    //         mockedNetworkService, 
    //         mockedPageCacheRecycler, 
    //         mockNamedWriteableRegistry, 
    //         mockedCircuitBreakerService, 
    //         mockeSharedGroupFactory);

    //     verify(mockedRunPlugin, times(1)).makeNettyTransport(any(), any(), any(), any(), any(), any(), any(), any());
    // }
}

