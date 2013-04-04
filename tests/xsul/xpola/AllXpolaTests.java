/* -*-             c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/

package xsul.xpola;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import xsul.http_client.TestKeepAlive;
import xsul.msg_box.TestMsgBox;
import xsul.util.TestUtil;
import xsul.ws_addressing.WsaTests;

/**
 * Run all XPola tests.
 */
public class AllXpolaTests extends TestRunner {
    public static void main (String[] args) {
        junit.textui.TestRunner.run (AllXpolaTests.suite());
    }
    
    public AllXpolaTests() {
        super();
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("WSUL/XSUL Xpola unit tests");

        suite.addTestSuite(TestCapman.class);
        suite.addTestSuite(TestCapmanService.class);
        suite.addTestSuite(TestGeneral.class);
        suite.addTestSuite(TestGroupman.class);
        suite.addTestSuite(TestGroupmanService.class);
        return suite;
    }

}


