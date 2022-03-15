import org.junit.jupiter.api.Test;
import org.opensearch.test.OpenSearchTestCase;

public class TestMainScript extends OpenSearchTestCase{


    // test considerations :
    // - transport service is set up correctly
    // - test that connection is up
    // - mock a call to the transport services

    @Test
    public void unitTest(){
        System.out.println("TEST : Unit Test is envoked");
    }
}