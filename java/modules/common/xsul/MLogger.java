/* -*-             c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
/*
 * Copyright (c) 2003 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the licence.
 *
 * $Id: MLogger.java,v 1.8 2005/02/16 05:52:51 aslom Exp $
 */

package xsul;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

/**
 * This is very small implementation of logging for JDK 1.2 (or better)
 * that is self-contained (<b>it is just one class!</b>) and is both easy to use
 * and simple to configurable from command line by using system properties (-Dlog=...)
 * or from inside of your application (see setCmdNames() method).
 * To use in your application simply copy this file to your source tree,
 * chnage package name and in your code use:
 * <pre>
 *   private static final MLogger logger = Mlogger.getLogger();
 *   ...
 *   logger.fine("hellp from logger");
 * </pre>
 * <p>Following system properties are supported:<ul>
 * <li>log=:LEVEL,name.name,name.name:LEVEL
 * </li>
 * <li>debug - equivalent to -Dlog=:ALL
 * </li>
 * <li>showtime [default is true] - should GML time be show for each log entry
 * </li>
 * <li>log.multiline [default is false] - print log entry context and message on seaprate lines
 * </li>
 * <li>log.wrapcol=N [0 = no wrapping] - wrap output that is longer than N column
 * </li>
 * </ul>
 *
 * <p>NOTE: the API is modelled after Log4J and JDK 1.4 logger so should be easy to switch.
 * </p>
 * @version $Revision: 1.8 $ $Date: 2005/02/16 05:52:51 $ (GMT)
 * @author Aleksander Slominski [http://www.extreme.indiana.edu/~aslom]
 */

public class MLogger {
    public static final MLogger global = new MLogger("global", null);
    public static final String PROPERTY_PREFIX = ""; //"logger.prefix"
    public static final String PROPERTY_LOG = PROPERTY_PREFIX+"log";
    public static final String PROPERTY_SHOWTIME = PROPERTY_PREFIX+"showtime";
    public static final String PROPERTY_DEBUG = PROPERTY_PREFIX+"debug";
    public static final String PROPERTY_WRAPCOL = PROPERTY_PREFIX+"log.wrapcol";
    public static final String PROPERTY_MULTILINE = PROPERTY_PREFIX+"log.multiline";
    public static final int DEFAULT_NESTING_LEVEL = 4;
    private static final boolean QUIET = false; // no configuration messages
    private static final boolean DEBUG = false;
    private static final boolean WARN = false;
    
    // global state
    private static PrintStream sink = System.err;
    private static boolean guessFailed;
    //private static List anchoredLoggers = new LinkedList();
    private static boolean showTime = true;
    private static int wrapCol;
    private static boolean multiline;
    
    //private static List cmdLoggers;
    
    // per instance states
    private Level myLevel;
    private String myName;
    
    
    
    
    
    // do magic reading -Dlog property, ex:
    // -Dlog=wombat:INFO,tests:ALL,xpp:OFF
    // -Dlog=:OFF
    // -Ddebug=true
    static {
        if(Log.ON) {
            //cmdLoggers = new ArrayList();
            try {
                
                //String names = System.getProperty("log");
                String names = null;
                try {
                    names = (String) AccessController.doPrivileged(new PrivilegedAction() {
                                public Object run() {
                                    return System.getProperty(PROPERTY_LOG);
                                }
                            });
                } catch(AccessControlException ace) {
                    if(DEBUG) System.err.println("no access to log sytem property for log");
                    if(DEBUG) ace.printStackTrace();
                }
                if(DEBUG) System.err.println("MLogger log="+names);
                // last resort
                String debug = System.getProperty(PROPERTY_DEBUG);
                if(names == null &&  debug != null) {
                    String s = debug.toLowerCase();
                    if(DEBUG) System.err.println("MLogger debug="+s);
                    if(!"false".equals(s) && !"off".equals(s))
                        names = ":ALL";
                }
                setCmdNames(names); //set list of loggers and levels
                String requestedShowTime = System.getProperty(PROPERTY_SHOWTIME);
                if(requestedShowTime != null) {
                    if(requestedShowTime.length() > 0) {
                        showTime = Boolean.getBoolean(requestedShowTime);
                    } else {
                        showTime = true; //when property is present and empty it is ON
                    }
                }
                String multilineProperty = System.getProperty(PROPERTY_MULTILINE);
                if(multilineProperty != null) {
                    if(multilineProperty.length() > 0) {
                        multiline = Boolean.getBoolean(multilineProperty);
                    } else {
                        multiline = true; //when property is present and empty it is ON
                    }
                }
                String wrapColProperty = System.getProperty(PROPERTY_WRAPCOL);
                if(wrapColProperty != null) {
                    wrapCol = Integer.parseInt(wrapColProperty);
                }
            } catch(IllegalArgumentException ex) {
                // this is user error and that is why printing of it is not suppressed
                System.err.println("can't set logging "+ex);
                ex.printStackTrace();
            } catch(java.security.AccessControlException ex) {
                if(WARN ||DEBUG) {
                    System.err.println("can't set logging "+ex);
                    ex.printStackTrace();
                }
            }
            
        }
    }
    
    protected MLogger(String name, String resourceBundleName) {
        this.myName = name;
    }
    
    public static MLogger getAnonymousMLogger() {
        return new MLogger(null, null);
    }
    
    public static MLogger getLogger() {
        // determine log klass dynamically
        String name = "";
        Location loc = new Location();
        if(guessLocation(loc, 2)) {
            name =  loc.klass;
        }
        if(DEBUG) System.err.println("MLogger.getLogger loc.klass="+loc.klass);
        return getLogger(name);
    }
    
    public static MLogger getLogger(java.lang.String name) {
        MLogger logger = LogManager.getLogManager().getLogger(name);
        //anchorLogger(logger);
        return logger;
    }
    
    public String getName() {
        return myName;
    }
    
    public boolean isLoggable(Level level) {
        return level.intValue() >= myLevel.intValue();
    }
    
    public final boolean isSevereEnabled() { return isLoggable(Level.SEVERE); }
    public final boolean isWarningEnabled() { return isLoggable(Level.WARNING); }
    public final boolean isInfoEnabled() { return isLoggable(Level.INFO); }
    public final boolean isConfigEnabled() { return isLoggable(Level.CONFIG); }
    public final boolean isFineEnabled() { return isLoggable(Level.FINE); }
    public final boolean isFinerEnabled() { return isLoggable(Level.FINER); }
    public final boolean isFinestEnabled() { return isLoggable(Level.FINEST); }
    
    public void setLevel(Level newLevel) {
        myLevel = newLevel;
        if(DEBUG) {
            boolean enabled = myLevel != Level.OFF;
            System.err.println("MLogger.setLevel name='"+myName+"' level="+myLevel+" enabled="+enabled);
            //if(DEBUG) System.err.println("SLogger name="+name+" is not enabled");
        }
    }
    
    public Level getLevel() {
        return myLevel;
    }
    
    
    public static String parametersToList(Object[] params) {
        if(params == null) return " array with parameters is null";
        StringBuffer sb = new StringBuffer();
        sb.append('{');
        for (int i = 0; i < params.length; i++)
        {
            sb.append(params[i]);
            if(i < params.length - 1) sb.append(",");
        }
        sb.append('}');
        return sb.toString();
    }
    
    
    
    /** Log a method entry with Level.FINER and message "ENTRY" */
    public void entering() {
        logg(Level.FINER, "ENTRY"); }
    public void entering(String sourceClass, String sourceMethod) {
        logg(Level.FINER, sourceClass+":"+sourceMethod+" ENTRY"); }
    
    /** Log a method entry with Level.FINER and message "ENTRY" and parametr content appended */
    public void entering(Object param1) {
        logg(Level.FINER, "ENTRY "+param1); }
    public void entering(String sourceClass, String sourceMethod, Object param1) {
        logg(Level.FINER, sourceClass+":"+sourceMethod+" ENTRY "+param1); }
    
    /** Log a method entry with Level.FINER and message "ENTRY" and parametrs content appended */
    public void entering(Object[] params) {
        logg(Level.FINER, "ENTRY "+parametersToList(params));
    }
    public void entering(String sourceClass, String sourceMethod, Object[] params) {
        logg(Level.FINER, sourceClass+":"+sourceMethod+" ENTRY "+parametersToList(params));
    }
    
    
    
    /** Log a method return with Level.FINER and message "RETURN" */
    public void exiting() {
        logg(Level.FINER, "RETURN");
    }
    public void exiting(String sourceClass, String sourceMethod) {
        logg(Level.FINER, sourceClass+":"+sourceMethod+" RETURN");
    }
    
    /** Log a method return with Level.FINER and message "RETURN" and return value appended */
    public Object exiting(Object result) {
        logg(Level.FINER, "RETURN "+result);
        return result;
    }
    public Object exiting(String sourceClass, String sourceMethod, Object result) {
        logg(Level.FINER, sourceClass+":"+sourceMethod+" RETURN "+result);
        return result;
    }
    
    /** Log an exception thrown with Level.FINER and message "THROW" and exception appended*/
    public Throwable throwing(Throwable thrown) {
        logg(Level.SEVERE, "THROW", thrown);
        return thrown;
    }
    
    public Throwable throwing(String sourceClass,
                              String sourceMethod,
                              Throwable thrown)
    {
        logg(Level.SEVERE, sourceClass+":"+sourceMethod+" THROW", thrown);
        return thrown;
    }
    
    /** report that exception was caught with log message at FINER level and "CAUGHT" message */
    public Throwable caught(Throwable thrown)
    {
        logg(Level.SEVERE, "CAUGHT", thrown);
        return thrown;
    }
    public Throwable caught(String sourceClass,
                            String sourceMethod,
                            Throwable thrown)
    {
        logg(Level.SEVERE, sourceClass+":"+sourceMethod+" CAUGHT", thrown);
        return thrown;
        
    }
    
    public void severe(String msg) { logg(Level.SEVERE, msg); }
    public void warning(String msg) { logg(Level.WARNING, msg); }
    public void info(String msg) { logg(Level.INFO, msg); }
    public void config(String msg) { logg(Level.CONFIG, msg); }
    public void fine(String msg) { logg(Level.FINE, msg); }
    public void finer(String msg) { logg(Level.FINER, msg); }
    public void finest(String msg) { logg(Level.FINEST, msg); }
    
    
    public void log(Level level, String msg) { logg(level, msg); }
    public void log(Level level, String msg, Throwable thrown)
    { logg(level, msg, thrown); }
    public void log(Level level, String msg, Object param1)
    { logg(level, msg+" "+param1); }
    public void log(Level level, String msg, Object[] params)
    { logg(level, msg+" "+parametersToList(params)); }
    
    public void severe(String msg, Throwable thrown)
    { logg(Level.SEVERE, msg, thrown); }
    public void warning(String msg, Throwable thrown)
    { logg(Level.WARNING, msg, thrown); }
    public void info(String msg, Throwable thrown)
    { logg(Level.INFO, msg, thrown); }
    public void config(String msg, Throwable thrown)
    { logg(Level.CONFIG, msg, thrown); }
    public void fine(String msg, Throwable thrown)
    { logg(Level.FINE, msg, thrown); }
    public void finer(String msg, Throwable thrown)
    { logg(Level.FINER, msg, thrown); }
    public void finest(String msg, Throwable thrown)
    { logg(Level.FINEST, msg, thrown); }
    
    
    public static synchronized PrintStream getSink() {
        return sink;
    }
    
    public static synchronized void setSink(PrintStream ps) {
        if(!QUIET) System.err.println("MLogger sink="+ps);
        if(sink != System.err) {
            String s = global.queryPrefix(Level.ALL, DEFAULT_NESTING_LEVEL)
                + "MLogger.setSink() can only be called once";
            ps.println(s);
            throw new IllegalArgumentException("sink can be only set once");
            
        }
        sink = ps;
    }
    
    // --- internal state
    
    public void logg(Level level, String msg) {
        //        if(!isLoggable(level)) {
        //          return;
        //        }
        //        String s = queryPrefix(DEFAULT_NESTING_LEVEL) + msg;
        //        //"\n\tat zoo.Zoo.sayMoo(Zoo.java:15) "
        //        synchronized(getClass()) {
        //          sink.println(s);
        //        }
        //        if(!Log.ON) {
        //          throw new RuntimeException(
        //              "For efficiency reasons use code: if(Log.ON) log(..)");
        //        }
        logg(level, msg, null, DEFAULT_NESTING_LEVEL + 1);
    }
    
    public void logg(Level level, String msg, Throwable thrown) {
        logg(level, msg, thrown, DEFAULT_NESTING_LEVEL + 1);
    }
    
    public void logg(Level level, String msg, Throwable thrown, int nestingLevel) {
        if(!isLoggable(level)) return;
        String s = queryPrefix(level, nestingLevel) + (multiline ? "\n" : "") + msg;
        if(wrapCol > 0) {
            s = wrap(s, wrapCol);
        }
        synchronized(getClass()) {
            if(thrown == null) {
                sink.println(s);
            } else {
                sink.print(s+" exception: ");
                thrown.printStackTrace(sink);
            }
        }
        if(!Log.ON) {
            throw new RuntimeException(
                "For efficiency reasons use code: if(Log.ON) log(..)");
        }
    }
    
    private static String wrap(String s, int wrapCol) {
        int len = s.length();
        if(len < wrapCol) {
            return s;
        }
        int extraLines = (len / wrapCol) + 1;
        StringBuffer buf = new StringBuffer(len + 2 * extraLines);
        
        //wrapCol -= 8; //leave 8 characters off for tabulators
        int count = 0; //-8; //first line has no tabs
        int start = 0;
        int pos = 0;
        while(pos < len) {
            char c = s.charAt(pos);
            //buf.append(c);
            if(c == '\n' || c == '\r') {
                count = 0;
                //buf.append('\t');
                //count = 8;
                //} else if(c == '\r') {
                //    count = 0;
            } else if(c == '\t' ) {
                count += 8;
            } else if(count > wrapCol) {
                int i = pos;
                while(i >  start) {
                    char backC = s.charAt(i);
                    if(  Character.isWhitespace(backC) ) {
                        break;
                    }
                    --i;
                }
                if(i > start) {
                    buf.append(s.substring(start, i));
                    start = pos = i + 1; //pass over whitespace
                } else {
                    buf.append(s.substring(start, pos));
                    start = pos;
                }
                buf.append("\n");
                count = 0;
                //buf.append("\n\t");
                //count = 8;
            } else {
                ++count;
            }
            ++pos;
        }
        buf.append(s.substring(start));
        return buf.toString();
    }
    
    private String queryPrefix(Level level, int nestingLevel) {
        //TODO: is it faster than shared myLoc and synchronizing(myLoc) (considering printing...)?
        Location myLoc = new Location();
        StringBuffer buf = new StringBuffer(80);
        buf.append("[ ");
        if(guessLocation(myLoc, nestingLevel)) {
            
            if(showTime) {
                long now = System.currentTimeMillis();
                formatTime(now, buf);
                buf.append(' ');
            }
            
            
            String tname = Thread.currentThread().getName(); //toString();
            //long ltid = Thread.currentThread().hashCode();
            //String tid = Long.toString(ltid, 16);
            buf.append(tname).append(": ");
            //e.printStackTrace(System.out);
            
            boolean addSpaceAfterLoggerName = true;
            String loggerNameToPrint = myName;
            if(myLoc.fileName != null) {
                int i = myLoc.fileName.lastIndexOf(".java");
                if(i > 0) {
                    String s = myLoc.fileName.substring(0, i);
                    if(myName.endsWith(s)) {
                        int j = myName.length() - s.length();
                        loggerNameToPrint = myName.substring(0, j); //+"%"+ myName.substring(j);
                        addSpaceAfterLoggerName = false;
                    }
                }
            }
            
            buf.append(loggerNameToPrint);
            if(addSpaceAfterLoggerName) {
                buf.append(" ");
                if(myLoc.packageName != null && false == myLoc.packageName.equals(myName)) {
                    buf.append(myLoc.packageName).append('.');
                }
            }
            
            
            if(myLoc.line != -1) {
                buf.append(myLoc.fileName).append(':')
                    .append(myLoc.line).append(' ');
            } else {
                buf.append(myLoc.fileName).append(' ')
                    .append(myLoc.klass).append(' ');
                
            }
            //buf.append(level);
            buf.append(myLoc.method).append(' ');
            if(Level.ALL.equals(level) || Level.FINEST.equals(level)) {
                buf.append('1');
            } else if(Level.FINER.equals(level)) {
                buf.append('2');
            }  else if(Level.FINE.equals(level)) {
                buf.append("3");
            } else if(Level.CONFIG.equals(level)) {
                buf.append('4');
            } else if(Level.INFO.equals(level)) {
                buf.append('5');
            } else if(Level.WARNING.equals(level)) {
                buf.append('6');
            } else if(Level.SEVERE.equals(level)) {
                buf.append("7");
            } else {
                buf.append(level);
            }
            
            //            if(Level.ALL.equals(level) || Level.FINEST.equals(level)) {
            //            } else if(Level.FINER.equals(level)) {
            //                buf.append('+');
            //            }  else if(Level.FINE.equals(level)) {
            //                buf.append("#");
            //            } else if(Level.CONFIG.equals(level)) {
            //                buf.append('%');
            //            } else if(Level.INFO.equals(level)) {
            //                buf.append('?');
            //            } else if(Level.SEVERE.equals(level)) {
            //                buf.append("!");
            //            } else {
            //                buf.append(level);
            //            }
        } else {
            buf.append(myName).append(' ');
        }
        buf.append("] ");
        //}
        return buf.toString();
    }
    
    //private static boolean guessLocation(Location loc, int nestingLevel) {
    //    return guessLocation(loc, nestingLevel);
    //}
    
    private static boolean guessLocation(Location loc, int nestingLevel) {
        if(guessFailed) return false;
        Throwable throwable = new Throwable();
        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        String s = sw.toString()+"\n";
        String line = null;
        try {
            // skip two lines
            int i = s.indexOf('\n');
            for(int lines = 0; lines < nestingLevel; ++lines)
                i = s.indexOf('\n', i+1);
            int j = s.indexOf('\n', i+1);
            // extract info from line ex.:
            // "        at soaprmi.soapenc.SoapEnc.readObject(SoapEnc.java:116)");
            ///System.err.println("s="+s+" i="+i+" j="+j);
            line = s.substring(i+1, j);
            if(DEBUG) System.err.println("line="+line);
            i = line.indexOf('(');
            if(i < 0) {
                if(DEBUG) System.err.println("problem trace="+s+" line="+line);
                line = "at <Unknown>.<undetermined>(<Unknown>.java:-1)";
                i = line.indexOf('(');
            }
            loc.method = line.substring(0, i);
            j = loc.method.lastIndexOf('.');
            if(j < 0) {
                loc.klass = "";
                int k = loc.method.lastIndexOf("at ");
                loc.method = loc.method.substring(k+3);
            } else {
                int k = loc.method.lastIndexOf("at ");
                loc.klass = loc.method.substring(k+3, j); //!
                loc.method = loc.method.substring(j+1);
            }
            int packagePos = loc.klass.lastIndexOf('.');
            if(packagePos > 0) {
                loc.packageName = loc.klass.substring(0, packagePos);
            }
            j = line.lastIndexOf(')');
            String pos = line.substring(i+1, j); //!
            loc.line = -1;
            loc.fileName = pos;
            i = pos.indexOf(':');
            if(i >= 0) {
                loc.fileName=pos.substring(0, i);
                String lineNo = "";
                try {
                    lineNo = pos.substring(i + 1);
                    if(DEBUG) System.err.println("lineNo="+lineNo);
                    {
                        // hacking for IRIX: '6, Compiled Code'
                        //   from line output such like that 'at A.<init>(A.java:6, Compiled Code)'
                        int posComma = lineNo.indexOf(',');
                        if(posComma != -1) {
                            lineNo = lineNo.substring(0, posComma);
                        }
                    }
                    {
                        // more special code for AIX JDK 1.4 "at xsul.XmlConstants.<clinit>(XmlConstants.java:25).null(Unknown Source)"
                        int posRightP = lineNo.indexOf(')');
                        if(posRightP != -1) {
                            lineNo = lineNo.substring(0, posRightP);
                        }
                    }
                    loc.line=Integer.parseInt(lineNo);
                } catch(NumberFormatException ex) {
                    System.err.println("This exception is just warning"
                                           +" (it is only affecting logging)"
                                           +""+MLogger.class.getName()
                                           +" is disabling advanced logging  "
                                           +" - can't parse"
                                           +" string: '"+line+"'"
                                           +" for line number '"+lineNo+"'"
                                           +" stack trace:---\n"+s+"\n---");
                    ex.printStackTrace();
                    guessFailed = true;
                }
            }
        } catch(IndexOutOfBoundsException ex) {
            System.err.println(MLogger.class.getName()+
                                   " disabling advanced logging - "+
                                   " location guess failed for stack trace"+
                                   "on line: '"+line+"'\n of stack trace:---\n"+s+"---");
            ex.printStackTrace();
            guessFailed = true;
        }
        return ! guessFailed;
    }
    
    
    /**
     * Enables list of loggers passed as string parameter.
     * Format: [logger_name:LEVEL][,...]
     *   logger_name is package[.class] or any name that was given
     *     when creating logger with MLogger.getMLogger("name");
     *   LEVEL -s execatly name of Level such as ALL, FINE, etc.
     *     (see Level class)
     */
    public static void setCmdNames(String names) {
        if(DEBUG) System.err.println("MLogger.setNames names="+names);
        if(!QUIET && names != null ) {
            System.err.println(
                "MLogger $Revision: 1.8 $ $Date: 2005/02/16 05:52:51 $ (GMT)"+
                    " configured as '"+names+"' "
                    +" (XSUL version compile-time:"+XsulVersion.IMPL_VERSION+" runtime:"
                    +XsulVersion.getImplementationVersion()+")");
        }
        if(names != null) {
            if(names.indexOf(',') == -1) {
                setCmdLogger(names);
            } else {
                StringTokenizer tok = new StringTokenizer(names, ",");
                while (tok.hasMoreTokens()) {
                    setCmdLogger(tok.nextToken());
                }
            }
        }
    }
    
    private static void setCmdLogger(String name) {
        Level level = Level.ALL; //default level
        int pos;
        if((pos = name.indexOf(':')) != -1) {
            String levelName = name.substring(pos + 1);
            level = Level.parse(levelName);
            name = name.substring(0, pos);
        }
        // DONE: investigate to change it to LogManager.getLogManager().getLogger()
        MLogger l = LogManager.getLogManager().getLogger(name, level);
        ////NOTE: must anchor or this SLogger will be GC as LogManger keeps only weak references
        //anchorLogger(l);
        //walk and update all logger levels
        LogManager.getLogManager().setLevel(name, level);
    }
    
    //    private static void anchorLogger(MLogger l) {
    //        synchronized(anchoredLoggers) {
    //            //if(anchoredLoggers.contains(l) == false) { //not good enough as depends on equals()
    //            boolean  found = false;
    //            // make sure that anchored list has no duplicates to avoid memory leak ...
    //            for (Iterator i = anchoredLoggers.listIterator() ; i.hasNext(); )
    //            {
    //                Object o = i.next();
    //                if(o == l) {
    //                    found = true;
    //                    break;
    //                }
    //            }
    //            if(!found) {
    //                anchoredLoggers.add(l);
    //            }
    //        }
    //    }
    
    public static boolean getMultiline() {
        return multiline;
    }
    
    public static void setMultiline(boolean enable) {
        multiline = enable;
    }
    
    public static int getWrapCol() {
        return wrapCol;
    }
    
    public static void setWrapCol(int i) {
        wrapCol = i;
    }
    
    public static boolean getShowTime() {
        return showTime;
    }
    
    public static void setShowTime(boolean enable) {
        showTime = enable;
    }
    
    public static void formatTime(long time, StringBuffer buf) {
        long ms = time % 1000;
        time /= 1000;
        long ss = time % 60;
        time /= 60;
        long mm = time  % 60;
        time /= 60;
        long hh = time % 24;
        
        if(hh < 10) buf.append('0');
        buf.append(hh).append(':');
        
        if(mm < 10) buf.append('0');
        buf.append(mm).append(':');
        
        if(ss < 10) buf.append('0');
        buf.append(ss).append('.');
        
        if(ms < 100) buf.append('0');
        if(ms < 10) buf.append('0');
        buf.append(ms);
        
    }
    
    
    // print logger state -- good for debugging
    public String toString() {
        StringBuffer buf = new StringBuffer(getClass().getName()+"={");
        buf.append(getName()+":"+getLevel());
        buf.append("}");
        return buf.toString();
    }
    
    //public String toString() {
    //   return "[SLogger name='"+name+"' level='"+level+"']";
    //}
    
    private static class Location  {
        String klass;
        String method;
        String fileName;
        int line;
        String packageName;
    };
    
    /**
     * Logging class that can be switched off completly to deliver
     * max performance if required (just set Log.ON to false)
     *
     * @version $Revision: 1.8 $ $Date: 2005/02/16 05:52:51 $ (GMT)
     * @author Aleksander Slominski [http://www.extreme.indiana.edu/~aslom]
     */
    
    
    public static final class Log  {
        
        public static final boolean ON = true; //false;
        
    }
    
    
    public static class Level implements java.io.Serializable {
        private static Map levels = new HashMap();
        
        public static final Level OFF =  new Level("OFF", 10000);
        //(highest value)
        public static final Level SEVERE =  new Level("SEVERE", 7777);
        public static final Level WARNING =  new Level("WARNING", 666);
        public static final Level INFO =  new Level("INFO", 55);
        public static final Level CONFIG =  new Level("CONFIG", 44);
        public static final Level FINE =  new Level("FINE", 33);
        public static final Level FINER =  new Level("FINER", 22);
        //(lowest value)
        public static final Level FINEST =  new Level("FINEST", 11);
        public static final Level ALL = new Level("ALL", 0);
        
        protected Level(java.lang.String name, int value) {
            this.name = name;
            this.value = value;
            levels.put(name, this);
        }
        
        public int hashCode() {
            return value; //+ name.hasCode()
        }
        
        public boolean equals(java.lang.Object ox) {
            if (ox == null) {
                return false;
            }
            Level other;
            try {
                other = (Level) ox;
            } catch (ClassCastException e) {
                return false;
            }
            return value == other.intValue();
        }
        
        public final int intValue() {
            return value;
        }
        
        public final java.lang.String toString() {
            return name;
        }
        
        public static Level parse(java.lang.String name) {
            Level l = (Level) levels.get(name);
            if(l == null) throw new IllegalArgumentException
                    ("unknown debug level: "+name);
            return l;
        }
        
        private int value;
        private String name;
    }
    
    public static class LogManager {
        private static final boolean DEBUG = false;
        private static LogManager instance = new LogManager();
        private SortedMap loggers = new TreeMap(); // loggers: name -> SLogger (name, level)
        
        
        LogManager() {
        }
        
        public static LogManager getLogManager() {
            return instance;
        }
        
        public boolean addLogger(MLogger l) {
            String name = l.getName();
            synchronized(loggers) {
                loggers.put(name, l); //new WeakReference(l));
            }
            Level level = l.getLevel();
            if(DEBUG) System.err.println("LogManager.addLogger() name='"+name+"' level="+level);
            //System.err.println("SLogger addLogger l="+l);
            return true; //TODO why?
        }
        
        public Level getLevel(java.lang.String name) {
            //obtain from levels guessing by taking off dots
            //Object o;
            Level level = Level.OFF;  // this is default level if no loggers available ...
            String origName = name;
            while(name != null){
                if(DEBUG) System.err.println("LogManager.getLevel() trying name='"+name+"'");
                //WeakReference wr;
                //synchronized(loggers) {
                //    wr =  (WeakReference) loggers.get(name);
                //}
                //if(wr != null) {
                //    if(DEBUG) System.err.println("LogManager.getLevel() found wr='"+wr+"'");
                //    MLogger logger;
                //    if((logger = (MLogger) wr.get()) != null) {
                //        level = logger.getLevel();
                //        if(DEBUG) System.err.println(
                //                "LogManager.getLevel()="+level+" for name="+origName
                //                    +" stripped name="+name);
                //        break; //return level;
                //    }
                //}
                MLogger logger;
                synchronized(loggers) {
                    logger = (MLogger) loggers.get(name);
                }
                if(logger != null) {
                    if(DEBUG) System.err.println("LogManager.getLevel() found wr='"+logger+"'");
                    
                    level = logger.getLevel();
                    if(DEBUG) System.err.println(
                            "LogManager.getLevel()="+level+" for name="+origName
                                +" stripped name="+name);
                    break; //return level;
                }
                
                int pos;
                if((pos = name.lastIndexOf('.')) > 0) {
                    name=name.substring(0, pos);
                } else if(name.length() > 0) {
                    name = "";
                } else {
                    name = null;
                }
            }
            if(DEBUG && name == null) System.err.println(
                    "LogManager.getLevel()="+level+" DEFAULT for name='"+origName+"'");
            return level;
        }
        
        public MLogger getLogger(java.lang.String name) {
            return getLogger(name, null);
        }
        
        MLogger getLogger(java.lang.String name, Level newLevel) {
            //WeakReference wr;
            //synchronized(loggers) {
            //      wr = (WeakReference) loggers.get(name);
            //}
            //MLogger logger = null;
            //if(wr != null) {
            //      logger = (MLogger) wr.get();
            //}
            MLogger logger;
            synchronized(loggers) {
                logger = (MLogger) loggers.get(name);
            }
            if(logger == null) {
                logger = new MLogger(name, null);
                if(newLevel == null) {
                    Level level = getLevel(name);
                    logger.setLevel(level);
                }
                addLogger(logger);
            }
            if(newLevel != null) {
                logger.setLevel(newLevel);
            }
            return logger;
        }
        
        public Enumeration getLoggerNames() {
            Enumeration en;
            synchronized(loggers) {
                en = Collections.enumeration(loggers.keySet());
            }
            return en;
        }
        
        
        /**
         * Set a log level for a given set of loggers.
         * Subsequently the target loggers will only log messages whose
         * types are greater than or equal to the given level.
         * The level value Level.OFF can be used to turn off logging.
         * The given log level applies to the named logger (if it exists), and
         * on any other named loggers below that name in the naming hierarchy.
         * The name and level are recorded, and will be applied to any
         * new loggers that are later created matching the given name.
         */
        public void setLevel(String name, Level level) {
            // it will create a logger if not existing as well
            MLogger l = getLogger(name);
            //l.setLevel(level);
            synchronized(loggers) {
                
                SortedMap tail = loggers.tailMap(name);
                // traverse map & set levels until you hit next category...
                Set entries = tail.entrySet();
                
                for(Iterator i = entries.iterator(); i.hasNext(); ) {
                    
                    //CodeWarrior compiler hates it and crashes with strange errors!!!
                    //Map.Entry me = (Map.Entry) i;
                    Map.Entry me = (Map.Entry) i.next();
                    
                    String keyName = (String) me.getKey();
                    if(!keyName.startsWith(name))
                        break;
                    //WeakReference wr = (WeakReference) me.getValue();
                    //if((l = (MLogger) wr.get()) != null) {
                    //      l.setLevel(level);
                    //}
                    l = (MLogger) me.getValue();
                    l.setLevel(level);
                }
            }
        }
        
        // print all loggers -- good for debugging
        public String toString() {
            StringBuffer buf = new StringBuffer(getClass().getName()+"={ ");
            SortedMap tail = loggers.tailMap("");
            // traverse map & set levels until you hit next category...
            Set entries = tail.entrySet();
            MLogger l = null;
            for(Iterator i = entries.iterator(); i.hasNext(); ) {
                
                Map.Entry me = (Map.Entry) i.next();
                
                String keyName = (String) me.getKey();
                Level level = null;
                //WeakReference wr = (WeakReference) me.getValue();
                
                //if((l = (MLogger) wr.get()) != null) {
                //   level = l.getLevel();
                //    buf.append(keyName+":"+level);
                //    if(i.hasNext()) buf.append(",");
                //}
                if((l = (MLogger) me.getValue()) != null) {
                    level = l.getLevel();
                    buf.append(keyName+":"+level);
                    if(i.hasNext()) buf.append(",");
                }
            }
            buf.append(" }");
            return buf.toString();
        }
        
    }
    
}

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 *
 * Copyright (C) 2003 The Trustees of Indiana University.
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



