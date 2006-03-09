/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.datatype;

/**
 *
 */
public interface ValueDatatype extends Datatype {

    /**
     * If this datatype represents a primitive type, this method returns the
     * datatype that represents the wrapper class. Returns <code>null</code> if this datatype
     * does not represent a primitive. 
     */
    public Datatype getWrapperType();
    
	/**
	 * Returns <code>true</code> if the given string can be parsed to a value of this datatype.
	 * Returns <code>false</code> otherwise.
	 */
	public abstract boolean isParsable(String value);
	
	/**
	 * Parses the given String and returns the appropriate datatype's value.
	 * Returns <code>null</code> or a NullObject, if the given String value is <code>null</code>.
	 * 
	 * @throws IllegalArgumentException if the string value can't be parsed.
	 */
	public abstract Object getValue(String value);
	
    /**
	 * Converts the given value into a String that can be parsed back to the value 
	 * via the <code>getValue()</code> method.
	 * <p> 
	 * If the value is null, the method returns null.
	 * 
	 * @throws IllegalArgumentException if the value is not a value of this datatype.
	 */
	public abstract String valueToString(Object value);
    
	/**
	 * Returns <code>true</code> if the given object is <code>null</code> or the NullObject (if the
	 * datatype value class makes use of the null object pattern.
	 * Returns <code>false</code> otherwise.
	 */
	public abstract boolean isNull(Object value);

}
