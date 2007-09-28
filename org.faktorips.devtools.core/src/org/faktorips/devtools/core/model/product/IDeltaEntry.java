/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.model.product;

/**
 * A delta entry desribes a single difference between a product component generation and the type
 * it is based on. For example the type might contain a new attribute but the product component
 * has not matching attribute value. 
 *  
 * @author Jan Ortmann
 */
public interface IDeltaEntry {

    /**
     * Fixes the difference between the type and the produt component.
     * <p>
     * For example if the type contains a new attribute but the product component generation.
     * has not matching attribute value, this method creates the attribute vlaue.
     */
    public void fix();
}
