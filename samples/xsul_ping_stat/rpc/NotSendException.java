/*
 * Created on 29 nov. 2004
 *
 */
package xsul_ping_stat.rpc;

/**
 * @author Alexandre di Costanzo
 *
 */
public class NotSendException extends Exception {

    /**
     * 
     */
    public NotSendException() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     */
    public NotSendException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     * @param cause
     */
    public NotSendException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param cause
     */
    public NotSendException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

}
