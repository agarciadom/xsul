/* -*-             c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
// for license see accompanying LICENSE_TESTS.txt file (available also at http://www.xmlpull.org)

package xsul.ws_addressing;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import xsul.http_client.TestKeepAlive;
import xsul.msg_box.TestMsgBox;
import xsul.util.TestUtil;
import xsul.ws_addressing.TestEndpointReference;
import xsul.ws_addressing.TestMessageInformationHeaders;
import xsul.ws_addressing.TestWsaOperations;

/**
 * Test XSUL implementation of WS-Addressing.
 *
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class WsaTests extends TestRunner {
    //private static boolean runAll;

    //public static boolean runnigAllTests() { return runAll; }

    public static void main (String[] args) {
        junit.textui.TestRunner.run (WsaTests.suite());
    }
    
    public WsaTests() {
        super();
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("WSUL/XSUL WS-Addressing unit tests");

        suite.addTestSuite(TestEndpointReference.class);
        suite.addTestSuite(TestMessageInformationHeaders.class);
        suite.addTestSuite(TestWsaOperations.class);
        suite.addTestSuite(TestDetectMessageIdDuplicates.class);
        suite.addTestSuite(TestWsaConversion.class);
        
        return suite;
    }

}


