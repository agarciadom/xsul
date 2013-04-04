/**
 * DenRPC.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 */

package xsul.den.v1;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import xsul.MLogger;
import xsul.den.DenRoutingTable;
import xsul.den.processor.DistributedProcessor;
import xsul.http_server.HttpMiniServer;
import xsul.http_server.HttpMiniServlet;
import xsul.http_server.HttpServerException;
import xsul.http_server.HttpServerRequest;

public class DenRPC {
    private final static MLogger logger = MLogger.getLogger();
    
    private final static String DEFAULT_CONF_FILE_PATH = "default.properties";
    
    public static Properties config;
    
    private HttpMiniServer httpServer;
    
    private static HttpMiniServlet poolServlet;
    
    private static DenRoutingTable routingTable;
    
    private DenRPC() {
        // Launch HTTP Server
        int port = Integer.parseInt(config.getProperty("server.port"));
        this.httpServer = new HttpMiniServer(port);
        
        // Launch Routing Table
        routingTable = new DenRoutingTable();
        
        // Launch Servlets Pool
        poolServlet = new DistributedProcessor(routingTable) {
            protected HttpServerRequest process(HttpServerRequest req)
                throws HttpServerException {
                return processBydefault(req);
            }
        };
        this.httpServer.useServlet(poolServlet);
    }
    
    private void start() {
        // Start HTTP Server
        this.httpServer.startServer();
    }
    
    private static void loadConfiguration() {
        logger.finest("Loading default configuration");
        
        // create and load default properties
        Properties defaultProps = new Properties();
        FileInputStream in = null;
        try {
            in = new FileInputStream(DEFAULT_CONF_FILE_PATH);
            defaultProps.load(in);
            in.close();
        } catch (FileNotFoundException e) {
            logger.finest("Couldn't find the default configuration file", e);
        } catch (IOException e) {
            logger.finest("Couldn't read the default configuration file", e);
        }
        
        // create program properties with default
        config = new Properties(defaultProps);
        
        logger.finest("Configuration loaded");
    }
    
    public static void main(String[] args) {
        // Load Configuration
        DenRPC.loadConfiguration();
        
        // DispatcherRPC Creation
        DenRPC gateway = new DenRPC();
        
        // Start DispatcherRPC with the mini server
        gateway.start();
    }
    
}

