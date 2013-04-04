/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: BenchDriver.java,v 1.2 2005/02/14 03:43:21 aslom Exp $
 */

package soap_bench;

public class BenchDriver {
    
    public static void main(String[] args) {
        try {
            String location = "http://localhost:34321";
            int count = 10;
            if(args.length > 0) {
                location = args[0];
                if(args.length  > 1) {
                    count = Integer.parseInt(args[1]);
                }
            }
            System.out.println("Running self contained test for "+location+" "+count+" times");
            BenchServer.main(args);
            BenchClient.main(args);
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            BenchServer.shutdown();
        }
    }
}

