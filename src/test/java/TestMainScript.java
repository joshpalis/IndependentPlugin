import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.opensearch.test.OpenSearchTestCase;

import transportservice.RunPlugin;
import transportservice.netty4.Netty;

public class TestMainScript extends OpenSearchTestCase{


    // test considerations :
    // - transport service is set up correctly
    // - test that connection is up
    // - mock a call to the transport services


    // runplugin has no methods besides main, what should i test?
    // 1) Mock node creation
    // 2) check local ephemeral
    // 3) check socket
    // 4) test : once node is connected, check "Tcp transport channel accepted"
    // - test main script debug output
    // -- check for bound address comnfirmation

    // mock external class
    @Mock private Netty nettyTranport;

    @Test
    public void unitTest(){
        System.out.println("TEST : Unit Test is envoked");
        String[] args = null;

        try(MockedStatic<RunPlugin> mockMain = Mockito.mockStatic(RunPlugin.class)) {
            mockMain.when(() -> RunPlugin.main(args)).thenReturn("THIS IS A TEST");
            RunPlugin.main(args);
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
       


    }
}