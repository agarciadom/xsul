/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package xsul.dsig.globus.security.authentication.wssec;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Map;
import javax.xml.namespace.QName;
import xsul.dsig.globus.security.authentication.wssec.X509SecurityToken;
import org.w3c.dom.Element;


public class BinarySecurityTokenFactory {
    private static final Class[] constructorType =
    { org.w3c.dom.Element.class };
    private static BinarySecurityTokenFactory factory;
    private Map tokenImpl;

    public BinarySecurityTokenFactory() {
        tokenImpl = new Hashtable();

        // register default impls
        tokenImpl.put(PKIPathSecurityToken.TYPE, PKIPathSecurityToken.class);

        tokenImpl.put(X509SecurityToken.TYPE, X509SecurityToken.class);
    }

    public static synchronized BinarySecurityTokenFactory getInstance() {
        if (factory == null) {
            factory = new BinarySecurityTokenFactory();
        }

        return factory;
    }

    public BinarySecurityToken createSecurityToken(Element element)
        throws WSSecurityException {
        BinarySecurityToken token = new BinarySecurityToken(element);
        QName type = token.getValueType();

        Class clazz = (Class) tokenImpl.get(type);

        if (clazz == null) {
            throw new WSSecurityException(
                WSSecurityException.UNSUPPORTED_SECURITY_TOKEN,
                "unsupportedBinaryTokenType", new Object[] { type }
            );
        }

        try {
            Constructor constructor = clazz.getConstructor(constructorType);

            if (constructor == null) {
                throw new WSSecurityException(
                    WSSecurityException.FAILURE, "invalidConstructor",
                    new Object[] { clazz }
                );
            }

            return (BinarySecurityToken) constructor.newInstance(
                new Object[] { element }
            );
        } catch (InvocationTargetException e) {
            Throwable ee = e.getTargetException();

            if (ee instanceof WSSecurityException) {
                throw (WSSecurityException) ee;
            } else {
                throw new WSSecurityException(
                    WSSecurityException.FAILURE, null, null, e
                );
            }
        } catch (NoSuchMethodException e) {
            throw new WSSecurityException(
                WSSecurityException.FAILURE, null, null, e
            );
        } catch (InstantiationException e) {
            throw new WSSecurityException(
                WSSecurityException.FAILURE, null, null, e
            );
        } catch (IllegalAccessException e) {
            throw new WSSecurityException(
                WSSecurityException.FAILURE, null, null, e
            );
        }
    }
}
