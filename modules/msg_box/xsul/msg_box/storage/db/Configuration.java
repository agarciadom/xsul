/**
 * Software License, Version 1.0
 *
 * Copyright 2003 The Trustees of Indiana University.  All rights reserved.
 *
 *
 *Redistribution and use in source and binary forms, with or without
 *modification, are permitted provided that the following conditions are met:
 *
 *1) All redistributions of source code must retain the above copyright notice,
 * the list of authors in the original source code, this list of conditions and
 * the disclaimer listed in this license;
 *2) All redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the disclaimer listed in this license in
 * the documentation and/or other materials provided with the distribution;
 *3) Any documentation included with all redistributions must include the
 * following acknowledgement:
 *
 *"This product includes software developed by the Community Grids Lab. For
 * further information contact the Community Grids Lab at
 * http://communitygrids.iu.edu/."
 *
 * Alternatively, this acknowledgement may appear in the software itself, and
 * wherever such third-party acknowledgments normally appear.
 *
 *4) The name Indiana University or Community Grids Lab or NaradaBrokering,
 * shall not be used to endorse or promote products derived from this software
 * without prior written permission from Indiana University.  For written
 * permission, please contact the Advanced Research and Technology Institute
 * ("ARTI") at 351 West 10th Street, Indianapolis, Indiana 46202.
 *5) Products derived from this software may not be called NaradaBrokering,
 * nor may Indiana University or Community Grids Lab or NaradaBrokering appear
 * in their name, without prior written permission of ARTI.
 *
 *
 * Indiana University provides no reassurances that the source code provided
 * does not infringe the patent or any other intellectual property rights of
 * any other entity.  Indiana University disclaims any liability to any
 * recipient for claims brought by any other entity based on infringement of
 * intellectual property rights or otherwise.
 *
 *LICENSEE UNDERSTANDS THAT SOFTWARE IS PROVIDED "AS IS" FOR WHICH NO
 *WARRANTIES AS TO CAPABILITIES OR ACCURACY ARE MADE. INDIANA UNIVERSITY GIVES
 *NO WARRANTIES AND MAKES NO REPRESENTATION THAT SOFTWARE IS FREE OF
 *INFRINGEMENT OF THIRD PARTY PATENT, COPYRIGHT, OR OTHER PROPRIETARY RIGHTS.
 *INDIANA UNIVERSITY MAKES NO WARRANTIES THAT SOFTWARE IS FREE FROM "BUGS",
 *"VIRUSES", "TROJAN HORSES", "TRAP DOORS", "WORMS", OR OTHER HARMFUL CODE.
 *LICENSEE ASSUMES THE ENTIRE RISK AS TO THE PERFORMANCE OF SOFTWARE AND/OR
 *ASSOCIATED MATERIALS, AND TO THE PERFORMANCE AND VALIDITY OF INFORMATION
 *GENERATED USING SOFTWARE.
 */

package xsul.msg_box.storage.db;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Vector;

/**
 * Configuration interface.
 * 
 */

public interface Configuration {

	/**
	 * Create an Configuration object that is a subset of this one. The new
	 * Configuration object contains every key from the current Configuration
	 * that starts with prefix. The prefix is removed from the keys in the
	 * subset.
	 * 
	 * @param prefix
	 *            The prefix used to select the properties.
	 */
	Configuration subset(String prefix);

	/**
	 * Check if the configuration is empty.
	 * 
	 * @return true is the configuration contains no key/value pair, false
	 *         otherwise
	 */
	boolean isEmpty();

	/**
	 * Check if the configuration contains the key.
	 * 
	 * @return true is the configuration contains a value for this key, false
	 *         otherwise
	 */
	boolean containsKey(String key);

	/**
	 * Add a property to the configuration. If it already exists then the value
	 * stated here will be added to the configuration entry. For example, if
	 * 
	 * resource.loader = file
	 * 
	 * is already present in the configuration and you
	 * 
	 * addProperty("resource.loader", "classpath")
	 * 
	 * Then you will end up with a Vector like the following:
	 * 
	 * ["file", "classpath"]
	 * 
	 * @param key
	 *            The Key to add the property to.
	 * @param token
	 *            The Value to add.
	 */
	void addProperty(String key, Object value);

	/**
	 * Set a property, this will replace any previously set values. Set values
	 * is implicitly a call to clearProperty(key), addProperty(key,value).
	 * 
	 * @param key
	 *            The key of the property to change
	 * @param value
	 *            The new value
	 */
	void setProperty(String key, Object value);

	/**
	 * Clear a property in the configuration.
	 * 
	 * @param key
	 *            the key to remove along with corresponding value.
	 */
	void clearProperty(String key);

	/**
	 * Gets a property from the configuration.
	 * 
	 * @param key
	 *            property to retrieve
	 * @return value as object. Will return user value if exists, if not then
	 *         default value if exists, otherwise null
	 */
	String getProperty(String key);

	/**
	 * Get the list of the keys contained in the configuration repository that
	 * match the specified prefix.
	 * 
	 * @param prefix
	 *            The prefix to test against.
	 * @return An Iterator of keys that match the prefix.
	 */
	Iterator getKeys(String prefix);

	/**
	 * Get the list of the keys contained in the configuration repository.
	 * 
	 * @return An Iterator.
	 */
	Iterator getKeys();

	/**
	 * Get a list of properties associated with the given configuration key.
	 * 
	 * @param key
	 *            The configuration key.
	 * @return The associated properties if key is found.
	 * @exception ClassCastException
	 *                is thrown if the key maps to an object that is not a
	 *                String/Vector.
	 * @exception IllegalArgumentException
	 *                if one of the tokens is malformed (does not contain an
	 *                equals sign).
	 */
	Properties getProperties(String key);

	/**
	 * Get a boolean associated with the given configuration key.
	 * 
	 * @param key
	 *            The configuration key.
	 * @return The associated boolean.
	 * @exception NoSuchElementException
	 *                is thrown if the key doesn't map to an existing object.
	 * @exception ClassCastException
	 *                is thrown if the key maps to an object that is not a
	 *                Boolean.
	 */
	boolean getBoolean(String key);

	/**
	 * Get a boolean associated with the given configuration key.
	 * 
	 * @param key
	 *            The configuration key.
	 * @param defaultValue
	 *            The default value.
	 * @return The associated boolean.
	 * @exception ClassCastException
	 *                is thrown if the key maps to an object that is not a
	 *                Boolean.
	 */
	boolean getBoolean(String key, boolean defaultValue);

	/**
	 * Get a boolean associated with the given configuration key.
	 * 
	 * @param key
	 *            The configuration key.
	 * @param defaultValue
	 *            The default value.
	 * @return The associated boolean if key is found and has valid format,
	 *         default value otherwise.
	 * @exception ClassCastException
	 *                is thrown if the key maps to an object that is not a
	 *                Boolean.
	 */
	Boolean getBoolean(String key, Boolean defaultValue);

	/**
	 * Get a byte associated with the given configuration key.
	 * 
	 * @param key
	 *            The configuration key.
	 * @return The associated byte.
	 * @exception NoSuchElementException
	 *                is thrown if the key doesn't map to an existing object.
	 * @exception ClassCastException
	 *                is thrown if the key maps to an object that is not a Byte.
	 * @exception NumberFormatException
	 *                is thrown if the value mapped by the key has not a valid
	 *                number format.
	 */
	byte getByte(String key);

	/**
	 * Get a byte associated with the given configuration key.
	 * 
	 * @param key
	 *            The configuration key.
	 * @param defaultValue
	 *            The default value.
	 * @return The associated byte.
	 * @exception ClassCastException
	 *                is thrown if the key maps to an object that is not a Byte.
	 * @exception NumberFormatException
	 *                is thrown if the value mapped by the key has not a valid
	 *                number format.
	 */
	byte getByte(String key, byte defaultValue);

	/**
	 * Get a byte associated with the given configuration key.
	 * 
	 * @param key
	 *            The configuration key.
	 * @param defaultValue
	 *            The default value.
	 * @return The associated byte if key is found and has valid format, default
	 *         value otherwise.
	 * @exception ClassCastException
	 *                is thrown if the key maps to an object that is not a Byte.
	 * @exception NumberFormatException
	 *                is thrown if the value mapped by the key has not a valid
	 *                number format.
	 */
	Byte getByte(String key, Byte defaultValue);

	/**
	 * Get a double associated with the given configuration key.
	 * 
	 * @param key
	 *            The configuration key.
	 * @return The associated double.
	 * @exception NoSuchElementException
	 *                is thrown if the key doesn't map to an existing object.
	 * @exception ClassCastException
	 *                is thrown if the key maps to an object that is not a
	 *                Double.
	 * @exception NumberFormatException
	 *                is thrown if the value mapped by the key has not a valid
	 *                number format.
	 */
	double getDouble(String key);

	/**
	 * Get a double associated with the given configuration key.
	 * 
	 * @param key
	 *            The configuration key.
	 * @param defaultValue
	 *            The default value.
	 * @return The associated double.
	 * @exception ClassCastException
	 *                is thrown if the key maps to an object that is not a
	 *                Double.
	 * @exception NumberFormatException
	 *                is thrown if the value mapped by the key has not a valid
	 *                number format.
	 */
	double getDouble(String key, double defaultValue);

	/**
	 * Get a double associated with the given configuration key.
	 * 
	 * @param key
	 *            The configuration key.
	 * @param defaultValue
	 *            The default value.
	 * @return The associated double if key is found and has valid format,
	 *         default value otherwise.
	 * @exception ClassCastException
	 *                is thrown if the key maps to an object that is not a
	 *                Double.
	 * @exception NumberFormatException
	 *                is thrown if the value mapped by the key has not a valid
	 *                number format.
	 */
	Double getDouble(String key, Double defaultValue);

	/**
	 * Get a float associated with the given configuration key.
	 * 
	 * @param key
	 *            The configuration key.
	 * @return The associated float.
	 * @exception NoSuchElementException
	 *                is thrown if the key doesn't map to an existing object.
	 * @exception ClassCastException
	 *                is thrown if the key maps to an object that is not a
	 *                Float.
	 * @exception NumberFormatException
	 *                is thrown if the value mapped by the key has not a valid
	 *                number format.
	 */
	float getFloat(String key);

	/**
	 * Get a float associated with the given configuration key.
	 * 
	 * @param key
	 *            The configuration key.
	 * @param defaultValue
	 *            The default value.
	 * @return The associated float.
	 * @exception ClassCastException
	 *                is thrown if the key maps to an object that is not a
	 *                Float.
	 * @exception NumberFormatException
	 *                is thrown if the value mapped by the key has not a valid
	 *                number format.
	 */
	float getFloat(String key, float defaultValue);

	/**
	 * Get a float associated with the given configuration key.
	 * 
	 * @param key
	 *            The configuration key.
	 * @param defaultValue
	 *            The default value.
	 * @return The associated float if key is found and has valid format,
	 *         default value otherwise.
	 * @exception ClassCastException
	 *                is thrown if the key maps to an object that is not a
	 *                Float.
	 * @exception NumberFormatException
	 *                is thrown if the value mapped by the key has not a valid
	 *                number format.
	 */
	Float getFloat(String key, Float defaultValue);

	/**
	 * Get a int associated with the given configuration key.
	 * 
	 * @param key
	 *            The configuration key.
	 * @return The associated int.
	 * @exception NoSuchElementException
	 *                is thrown if the key doesn't map to an existing object.
	 * @exception ClassCastException
	 *                is thrown if the key maps to an object that is not a
	 *                Integer.
	 * @exception NumberFormatException
	 *                is thrown if the value mapped by the key has not a valid
	 *                number format.
	 */
	int getInt(String key);

	/**
	 * Get a int associated with the given configuration key.
	 * 
	 * @param key
	 *            The configuration key.
	 * @param defaultValue
	 *            The default value.
	 * @return The associated int.
	 * @exception ClassCastException
	 *                is thrown if the key maps to an object that is not a
	 *                Integer.
	 * @exception NumberFormatException
	 *                is thrown if the value mapped by the key has not a valid
	 *                number format.
	 */
	int getInt(String key, int defaultValue);

	/**
	 * Get a int associated with the given configuration key.
	 * 
	 * @param key
	 *            The configuration key.
	 * @param defaultValue
	 *            The default value.
	 * @return The associated int if key is found and has valid format, default
	 *         value otherwise.
	 * @exception ClassCastException
	 *                is thrown if the key maps to an object that is not a
	 *                Integer.
	 * @exception NumberFormatException
	 *                is thrown if the value mapped by the key has not a valid
	 *                number format.
	 */
	Integer getInteger(String key, Integer defaultValue);

	/**
	 * Get a long associated with the given configuration key.
	 * 
	 * @param key
	 *            The configuration key.
	 * @return The associated long.
	 * @exception NoSuchElementException
	 *                is thrown if the key doesn't map to an existing object.
	 * @exception ClassCastException
	 *                is thrown if the key maps to an object that is not a Long.
	 * @exception NumberFormatException
	 *                is thrown if the value mapped by the key has not a valid
	 *                number format.
	 */
	long getLong(String key);

	/**
	 * Get a long associated with the given configuration key.
	 * 
	 * @param key
	 *            The configuration key.
	 * @param defaultValue
	 *            The default value.
	 * @return The associated long.
	 * @exception ClassCastException
	 *                is thrown if the key maps to an object that is not a Long.
	 * @exception NumberFormatException
	 *                is thrown if the value mapped by the key has not a valid
	 *                number format.
	 */
	long getLong(String key, long defaultValue);

	/**
	 * Get a long associated with the given configuration key.
	 * 
	 * @param key
	 *            The configuration key.
	 * @param defaultValue
	 *            The default value.
	 * @return The associated long if key is found and has valid format, default
	 *         value otherwise.
	 * @exception ClassCastException
	 *                is thrown if the key maps to an object that is not a Long.
	 * @exception NumberFormatException
	 *                is thrown if the value mapped by the key has not a valid
	 *                number format.
	 */
	Long getLong(String key, Long defaultValue);

	/**
	 * Get a short associated with the given configuration key.
	 * 
	 * @param key
	 *            The configuration key.
	 * @return The associated short.
	 * @exception NoSuchElementException
	 *                is thrown if the key doesn't map to an existing object.
	 * @exception ClassCastException
	 *                is thrown if the key maps to an object that is not a
	 *                Short.
	 * @exception NumberFormatException
	 *                is thrown if the value mapped by the key has not a valid
	 *                number format.
	 */
	short getShort(String key);

	/**
	 * Get a short associated with the given configuration key.
	 * 
	 * @param key
	 *            The configuration key.
	 * @param defaultValue
	 *            The default value.
	 * @return The associated short.
	 * @exception ClassCastException
	 *                is thrown if the key maps to an object that is not a
	 *                Short.
	 * @exception NumberFormatException
	 *                is thrown if the value mapped by the key has not a valid
	 *                number format.
	 */
	short getShort(String key, short defaultValue);

	/**
	 * Get a short associated with the given configuration key.
	 * 
	 * @param key
	 *            The configuration key.
	 * @param defaultValue
	 *            The default value.
	 * @return The associated short if key is found and has valid format,
	 *         default value otherwise.
	 * @exception ClassCastException
	 *                is thrown if the key maps to an object that is not a
	 *                Short.
	 * @exception NumberFormatException
	 *                is thrown if the value mapped by the key has not a valid
	 *                number format.
	 */
	Short getShort(String key, Short defaultValue);

	/**
	 * Get a string associated with the given configuration key.
	 * 
	 * @param key
	 *            The configuration key.
	 * @return The associated string.
	 * @exception ClassCastException
	 *                is thrown if the key maps to an object that is not a
	 *                String.
	 */
	String getString(String key);

	/**
	 * Get a string associated with the given configuration key.
	 * 
	 * @param key
	 *            The configuration key.
	 * @param defaultValue
	 *            The default value.
	 * @return The associated string if key is found and has valid format,
	 *         default value otherwise.
	 * @exception ClassCastException
	 *                is thrown if the key maps to an object that is not a
	 *                String.
	 */
	String getString(String key, String defaultValue);

	/**
	 * Get an array of strings associated with the given configuration key.
	 * 
	 * @param key
	 *            The configuration key.
	 * @return The associated string array if key is found.
	 * @exception ClassCastException
	 *                is thrown if the key maps to an object that is not a
	 *                String/Vector of Strings.
	 */
	String[] getStringArray(String key);

	/**
	 * Get a Vector of strings associated with the given configuration key.
	 * 
	 * @param key
	 *            The configuration key.
	 * @return The associated Vector.
	 * @exception ClassCastException
	 *                is thrown if the key maps to an object that is not a
	 *                Vector.
	 */
	Vector getVector(String key);

	/**
	 * Get a Vector of strings associated with the given configuration key.
	 * 
	 * @param key
	 *            The configuration key.
	 * @param defaultValue
	 *            The default value.
	 * @return The associated Vector.
	 * @exception ClassCastException
	 *                is thrown if the key maps to an object that is not a
	 *                Vector.
	 */
	Vector getVector(String key, Vector defaultValue);
}
