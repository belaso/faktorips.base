/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.tableconversion.excel;

import junit.framework.TestCase;

import org.faktorips.datatype.Datatype;
import org.faktorips.util.message.MessageList;

/**
 * 
 * @author Thorsten Guenther
 */
public class DoubleValueConverterTest extends TestCase {

    public void testGetIpsValue() {
        MessageList ml = new MessageList();
        DoubleValueConverter converter = new DoubleValueConverter();
        String value = converter.getIpsValue(new Double(1234), ml);
        assertTrue(Datatype.DOUBLE.isParsable(value));
        assertTrue(ml.isEmpty());

        value = converter.getIpsValue(new Double(Double.MAX_VALUE), ml);
        assertTrue(Datatype.DOUBLE.isParsable(value));
        assertTrue(ml.isEmpty());

        value = converter.getIpsValue(new Double(Double.MIN_VALUE), ml);
        assertTrue(Datatype.DOUBLE.isParsable(value));
        assertTrue(ml.isEmpty());

        value = converter.getIpsValue(new Integer(0), ml);
        assertFalse(ml.isEmpty());
        assertEquals(new Integer(0).toString(), value);
    }

    public void testGetExternalDataValue() {
        MessageList ml = new MessageList();
        DoubleValueConverter converter = new DoubleValueConverter();
        final String VALID = "1234";
        final String INVALID = "invalid";

        assertTrue(Datatype.DOUBLE.isParsable(VALID));
        assertFalse(Datatype.DOUBLE.isParsable(INVALID));

        Object value = converter.getExternalDataValue(VALID, ml);
        assertEquals(new Double(1234), value);
        assertTrue(ml.isEmpty());

        value = converter.getExternalDataValue(null, ml);
        assertNull(value);
        assertTrue(ml.isEmpty());

        value = converter.getExternalDataValue(INVALID, ml);
        assertFalse(ml.isEmpty());
        assertEquals(INVALID, value);
    }

}
