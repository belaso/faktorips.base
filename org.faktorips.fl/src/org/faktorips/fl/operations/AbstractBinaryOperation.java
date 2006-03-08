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

package org.faktorips.fl.operations;

import org.faktorips.datatype.Datatype;
import org.faktorips.fl.BinaryOperation;
import org.faktorips.util.ArgumentCheck;


/**
 * Abstract implementation of BinaryOperation.
 */
public abstract class AbstractBinaryOperation implements BinaryOperation {
    
    private String operator;
    private Datatype lhsDatatype;
    private Datatype rhsDatatype;

    /**
     * Creates a new binary operation for the indicated left-hand-side and
     * right hand side datatype.  
     */
    public AbstractBinaryOperation(String operator, Datatype lhs, Datatype rhs) {
        ArgumentCheck.notNull(operator);
        ArgumentCheck.notNull(lhs);
        ArgumentCheck.notNull(rhs);
        this.operator = operator;
        lhsDatatype = lhs;
        rhsDatatype = rhs;
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.fl.BinaryOperation#getOperator()
     */
    public String getOperator() {
        return operator;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.BinaryOperation#getLhsDatatype()
     */
    public Datatype getLhsDatatype() {
        return lhsDatatype;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.fl.BinaryOperation#getRhsDatatype()
     */
    public Datatype getRhsDatatype() {
        return rhsDatatype;
    }

}
