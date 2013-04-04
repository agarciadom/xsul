/**
 * DenHttpServerRequestImpl.java
 *
 * @author Liang Fang (lifang@cs.indiana.edu)
 * $Id: BytesHttpServerRequestImpl.java,v 1.2 2005/03/25 21:24:16 lifang Exp $
 */

package xsul.den.v1;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Enumeration;
import java.util.Hashtable;
import xsul.http_server.HttpServerException;
import xsul.http_server.HttpServerRequest;

public class BytesHttpServerRequestImpl implements HttpServerRequest {
    
    private String method;
    private String path;
    private String httpVersion;
    
    private String contentType;
    
    private Hashtable headers;
    
    private String charset;
    
    private InetAddress remotInetAddr;
    
    private int remotePort;
    
    private InputStream is;
    
    private boolean keepalive;
    
    private InputStream socketInputStream;
    private byte[] reqBytes;
    
    public BytesHttpServerRequestImpl(HttpServerRequest hsr) {
        method = hsr.getMethod();
        path = hsr.getPath();
        
//        httpVersion = hsr.getHttpVersion();
        contentType = hsr.getContentType();
        charset = hsr.getCharset();
        keepalive = hsr.isKeepAlive();
        // copy header hashtable
        headers = new Hashtable();
        for(Enumeration _enum = hsr.getHeaderNames();_enum.hasMoreElements();) {
            String hkey = (String)_enum.nextElement();
            String hval  = hsr.getHeader(hkey);
            headers.put(hkey, hval);
        }
        socketInputStream = hsr.getInputStream();
        is = socketInputStream;
    }
    
    public String getPath() {
        return path;
    }
    
    public String getMethod() {
        return method;
    }
    
    /**
     * Return value of header with given name or null if such header does not exist.
     */
    public String getHeader(String name) throws HttpServerException {
        name = name.toLowerCase();
        return (String)headers.get(name);
    }
    
    public InetAddress getRemotInetAddr() {
        return remotInetAddr;
    }
    
    public String getCharset() {
        return charset;
    }
    
    public Enumeration getHeaderNames() throws HttpServerException {
        return headers.keys();
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public InputStream getInputStream() {
        return is;
    }
    
    public boolean isKeepAlive() {
        return keepalive;
    }
    
    public int getHeaderCount() throws HttpServerException {
        return headers.size();
    }
    
    public void reset() {
        is = new ByteArrayInputStream(reqBytes);
    }
    
    public byte[] getRequestBytes()
        throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(8*1024);
        byte[] buf = new byte[4*1024];
        //copy input stream into byte array!
        while(true) {
            int received = is.read(buf);
            if(received < 0) {
                break;
            }
            if(received > 0) {
                baos.write(buf, 0, received);
            }
        }

        reqBytes = baos.toByteArray();
        
        return reqBytes;
    }
    
}

