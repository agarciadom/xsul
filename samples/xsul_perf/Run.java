/* -*- mode: Java; c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2004 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: Run.java,v 1.4 2004/03/30 19:42:45 aslom Exp $
 */

package xsul_perf;

public class Run {
    
    public static void main(String[] args)
    {
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
            EchoServer.main(new String[]{location});
            EchoClient.main(new String[]{location, ""+count});
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            EchoServer.shutdown();
        }
    }
}



