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

package org.faktorips.codegen.conversion;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;


/**
 *
 */
public class PrimitiveIntToIntegerCg extends AbstractSingleConversionCg {

    /**
     * @param from
     * @param to
     */
    public PrimitiveIntToIntegerCg() {
        super(Datatype.PRIMITIVE_INT, Datatype.INTEGER);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.codegen.SingleConversionCg#getConversionCode()
     */
    public JavaCodeFragment getConversionCode(JavaCodeFragment fromValue) {
        JavaCodeFragment fragment = new JavaCodeFragment();
        fragment.append("new ");
        fragment.appendClassName(Integer.class);
        fragment.append('(');
        fragment.append(fromValue);
        fragment.append(')');
        return fragment;
    }

}
