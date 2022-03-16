import org.junit.jupiter.api.Test;
import org.opensearch.test.OpenSearchTestCase;

import transportservice.RunPlugin;

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

    @Test
    public void unitTest(){
        System.out.println("TEST : Unit Test is envoked");


        // invoke main script for Transport service set up
        // String[] args = null;
        // RunPlugin.main(args);
        

    }
}