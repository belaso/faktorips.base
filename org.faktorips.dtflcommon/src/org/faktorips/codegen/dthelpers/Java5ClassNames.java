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

package org.faktorips.codegen.dthelpers;

/**
 * Qualified and unqualified class names for classes in Java 5 projects. They can not be retrieved
 * via <code>getClass().getName()</code> because a reference from old code to those projects is not
 * desired.
 * 
 * @author Daniel Hohenberger
 */
public interface Java5ClassNames {

    public static final String ValuesetPackage = "org.faktorips.valueset"; //$NON-NLS-1$
    public static final String ValueSet_UnqualifiedName = "ValueSet"; //$NON-NLS-1$
    public static final String ValueSet_QualifiedName = ValuesetPackage + "." + ValueSet_UnqualifiedName; //$NON-NLS-1$
    public static final String OrderedValueSet_UnqualifiedName = "OrderedValueSet"; //$NON-NLS-1$
    public static final String OrderedValueSet_QualifiedName = ValuesetPackage + "." + OrderedValueSet_UnqualifiedName; //$NON-NLS-1$
    public static final String DefaultRange_UnqualifiedName = "DefaultRange"; //$NON-NLS-1$
    public static final String DefaultRange_QualifiedName = ValuesetPackage + "." + DefaultRange_UnqualifiedName; //$NON-NLS-1$

    public static final String RuntimePackage = "org.faktorips.runtime"; //$NON-NLS-1$
    public static final String ILink_UnqualifiedName = "IProductComponentLink"; //$NON-NLS-1$
    public static final String ILink_QualifiedName = RuntimePackage + "." + ILink_UnqualifiedName; //$NON-NLS-1$

    public static final String RuntimePackageInternal = "org.faktorips.runtime.internal"; //$NON-NLS-1$
    public static final String ReadOnlyBinaryRangeTreeKeyType_UnqualifiedName = "ReadOnlyBinaryRangeTree.KeyType"; //$NON-NLS-1$
    public static final String ReadOnlyBinaryRangeTreeKeyType_QualifiedName = RuntimePackageInternal + "." //$NON-NLS-1$
            + ReadOnlyBinaryRangeTreeKeyType_UnqualifiedName;
    public static final String Link_UnqualifiedName = "ProductComponentLink"; //$NON-NLS-1$
    public static final String Link_QualifiedName = RuntimePackageInternal + "." + Link_UnqualifiedName; //$NON-NLS-1$

}
