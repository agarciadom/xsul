/* -*-             c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
// for license see accompanying LICENSE_TESTS.txt file (available also at http://www.xmlpull.org)

package xsul;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import xsul.http_client.TestKeepAlive;
import xsul.lead.TestXmlNameValueList;
import xsul.msg_box.TestMsgBox;
import xsul.util.TestUtil;
import xsul.ws_addressing.TestDetectMessageIdDuplicates;
import xsul.ws_addressing.TestEndpointReference;
import xsul.ws_addressing.TestMessageInformationHeaders;
import xsul.ws_addressing.TestWsaConversion;
import xsul.ws_addressing.TestWsaOperations;
import xsul.ws_addressing.WsaTests;

/**
 * Test XSUL.
 *
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class AllTests extends TestRunner {
    //private static boolean runAll;

    //public static boolean runnigAllTests() { return runAll; }

    public static void main (String[] args) {
        junit.textui.TestRunner.run (AllTests.suite());
    }
    
    public AllTests() {
        super();
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("WSUL/XSUL unit tests");

        suite.addTestSuite(TestUtil.class);
        suite.addTestSuite(TestXmlNameValueList.class);
        suite.addTestSuite(TestRPC.class);
        suite.addTestSuite(TestKeepAlive.class);
        suite.addTest(WsaTests.suite());
        suite.addTestSuite(TestOneWayMessaging.class);
        suite.addTestSuite(TestMsgBox.class);
        return suite;
    }

}


