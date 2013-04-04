/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/* Copyright (c) 2002-2006 Extreme! Lab, Indiana University. All rights reserved.
 * This software is open source. See the bottom of this file for the licence.
 * $Id: XsulVersion.java,v 1.89 2006/08/29 18:25:57 aslom Exp $ */
package xsul;

/**
 * One place to put version number.
 *
 * @version $Revision: 1.89 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class XsulVersion {
    public final static String SPEC_VERSION = "2.7.4";
    private final static String BUILD = ""; //"_dev"; //"_f2"; //"_SPECIAL_EDITION";//"_b4";

    private final static String PROJECT_NAME = "XSUL";
    public  final static String IMPL_VERSION = SPEC_VERSION+BUILD;
    private final static String USER_AGENT = PROJECT_NAME+"/"+IMPL_VERSION;

    private static int VERSION_MAJOR = -1;
    private static int VERSION_MINOR = -1;
    private static int VERSION_INCREMENT = -1;

    public static String getUserAgent() {
        return USER_AGENT;
    }

    public static String getSpecVersion() {
        return SPEC_VERSION;
    }

    public static String getImplementationVersion() {
        return IMPL_VERSION;
    }

    /**
     * Print version when exxecuted from command line.
     */
    public static void main(String[] args)
    {
        String SPEC_OPT = "-spec";
        String IMPL_OPT = "-impl";
        if(SPEC_OPT.equals(args[0])) {
            System.out.println(SPEC_VERSION);
        } else if(IMPL_OPT.equals(args[0])) {
            System.out.println(IMPL_VERSION);
        } else {
            System.err.println(XsulVersion.class.getName()+" Error: "+SPEC_OPT+" or "+IMPL_OPT+" is required");
            System.exit(1);
        }

    }

    public static void exitIfRequiredVersionMissing(String version) {
        try {
            requireVersion(version);
        } catch(Exception ex) {
            ex.printStackTrace(System.err);
            System.err.println("Error: could not find required version "+version+" of "+PROJECT_NAME+": "+ex.getMessage());
            System.err.println("Please make sure that JAR file with "+PROJECT_NAME+" with version "+version+" (or higher) is available.");
            System.err.println("Please make sure there is no more than one JAR file with "+PROJECT_NAME);
            System.err.println("Exiting");
            System.exit(1);
        }
    }


    /**
     * Version must be of form M.N[.K] where M is major version,
     * N is minor version and K is increment.
     * This method returns true if current major version is the same
     * and minor is bigger or equal to current minor version.
     * If provided major and minor versions are equals to current version
     * then increment is also checked and check is passed when increment
     * is bigger or equal to current increment version.
     */
    public static void requireVersion(String version)
        throws IllegalStateException
    {
        // NOTE: this is safe as int operations are atomic ...
        if(VERSION_MAJOR <0) extractCurrentVersion();
        int[] parsed;
        try {
            parsed = parseVersion(version);
        } catch(NumberFormatException ex) {
            throw new IllegalArgumentException(
                "could not parse "+PROJECT_NAME+" version string "+version+": "+ex);
        }
        int major = parsed[0];
        int minor = parsed[1];
        int increment = parsed[2];

        if(major != VERSION_MAJOR) {
            throw new IllegalStateException("required "+PROJECT_NAME+" "+version
                                                       +" has different major version"
                                                       +" from current "+SPEC_VERSION);
        }
        if(minor > VERSION_MINOR) {
            throw new IllegalStateException("required "+PROJECT_NAME+" "+version
                                                       +" has too big minor version"
                                                       +" when compared to current "+SPEC_VERSION);
        }
        if(minor == VERSION_MINOR) {
            if(increment > VERSION_INCREMENT) {
                throw new IllegalStateException("required "+PROJECT_NAME+" "+version
                                                           +" is too old"
                                                           +" when compared to current "+SPEC_VERSION);
            }
        }
    }

    /**
     * Parse verion string N.M[.K] into thre subcomponents (M=major,N=minor,K=increment)
     * that are returned in array with three elements.
     * M and N must be non negative, and K if present must be positive integer.
     * Increment K is optional and if not present in verion strig it is returned as zero.
     */
    public static int[] parseVersion(String version)
        throws NumberFormatException
    {
        int[] parsed = new int[3];
        int firstDot = version.indexOf('.');
        if(firstDot == -1) {
            throw new NumberFormatException(
                "expected version string N.M but there is no dot in "+version);
        }
        String majorVersion = version.substring(0, firstDot);
        parsed[0] = Integer.parseInt(majorVersion);
        if(parsed[0] < 0) {
            throw new NumberFormatException(
                "major N version number in N.M can not be negative in "+version);
        }
        int secondDot = version.indexOf('.', firstDot+1);
        String minorVersion;
        if(secondDot >= 0) {
            minorVersion = version.substring(firstDot+1, secondDot);
        } else {
            minorVersion = version.substring(firstDot+1);
        }
        parsed[1] = Integer.parseInt(minorVersion);
        if(parsed[1] < 0) {
            throw new NumberFormatException(
                "minor M version number in N.M can not be negative in "+version);
        }
        if(secondDot >= 0) {
            String incrementVersion = version.substring(secondDot+1);
            parsed[2] = Integer.parseInt(incrementVersion);
            if(parsed[2] < 0) {
                throw new NumberFormatException(
                    "increment K version number in N.M.K must be positive number in "+version);
            }
        }
        return parsed;
    }

    private static synchronized void extractCurrentVersion() throws IllegalStateException
    {
        int[] parsed;
        try {
            parsed = parseVersion(SPEC_VERSION);
        } catch(NumberFormatException ex) {
            throw new IllegalStateException(
                "internal problem: could not parse current "+PROJECT_NAME+" version string "+SPEC_VERSION);
        }
        VERSION_MAJOR = parsed[0];
        VERSION_MINOR = parsed[1];
        VERSION_INCREMENT = parsed[2];
    }

}


/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 *
 * Copyright (c) 2002-2006 The Trustees of Indiana University.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1) All redistributions of source code must retain the above
 *    copyright notice, the list of authors in the original source
 *    code, this list of conditions and the disclaimer listed in this
 *    license;
 *
 * 2) All redistributions in binary form must reproduce the above
 *    copyright notice, this list of conditions and the disclaimer
 *    listed in this license in the documentation and/or other
 *    materials provided with the distribution;
 *
 * 3) Any documentation included with all redistributions must include
 *    the following acknowledgement:
 *
 *      "This product includes software developed by the Indiana
 *      University Extreme! Lab.  For further information please visit
 *      http://www.extreme.indiana.edu/"
 *
 *    Alternatively, this acknowledgment may appear in the software
 *    itself, and wherever such third-party acknowledgments normally
 *    appear.
 *
 * 4) The name "Indiana University" or "Indiana University
 *    Extreme! Lab" shall not be used to endorse or promote
 *    products derived from this software without prior written
 *    permission from Indiana University.  For written permission,
 *    please contact http://www.extreme.indiana.edu/.
 *
 * 5) Products derived from this software may not use "Indiana
 *    University" name nor may "Indiana University" appear in their name,
 *    without prior written permission of the Indiana University.
 *
 * Indiana University provides no reassurances that the source code
 * provided does not infringe the patent or any other intellectual
 * property rights of any other entity.  Indiana University disclaims any
 * liability to any recipient for claims brought by any other entity
 * based on infringement of intellectual property rights or otherwise.
 *
 * LICENSEE UNDERSTANDS THAT SOFTWARE IS PROVIDED "AS IS" FOR WHICH
 * NO WARRANTIES AS TO CAPABILITIES OR ACCURACY ARE MADE. INDIANA
 * UNIVERSITY GIVES NO WARRANTIES AND MAKES NO REPRESENTATION THAT
 * SOFTWARE IS FREE OF INFRINGEMENT OF THIRD PARTY PATENT, COPYRIGHT, OR
 * OTHER PROPRIETARY RIGHTS.  INDIANA UNIVERSITY MAKES NO WARRANTIES THAT
 * SOFTWARE IS FREE FROM "BUGS", "VIRUSES", "TROJAN HORSES", "TRAP
 * DOORS", "WORMS", OR OTHER HARMFUL CODE.  LICENSEE ASSUMES THE ENTIRE
 * RISK AS TO THE PERFORMANCE OF SOFTWARE AND/OR ASSOCIATED MATERIALS,
 * AND TO THE PERFORMANCE AND VALIDITY OF INFORMATION GENERATED USING
 * SOFTWARE.
 */

        




