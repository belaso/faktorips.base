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

package org.faktorips.devtools.core.ui.editors.pctype;

import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;



public class RelationLabelProvider extends DefaultLabelProvider {
    
    /** 
     * Overridden method.
     * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
     */
    public String getText(Object element) {
        if (!(element instanceof IRelation)) {
            return super.getText(element);    
        }
        IRelation relation = (IRelation)element;
        String targetName = relation.getTarget();
        int pos = targetName.lastIndexOf('.');
        if (pos>0) {
            targetName = targetName.substring(pos+1);
        }
        
        String maxC;
        if (relation.getMaxCardinality() == Integer.MAX_VALUE) {
        	maxC = "*"; //$NON-NLS-1$
        }
        else {
        	maxC = ""+relation.getMaxCardinality(); //$NON-NLS-1$
        }
        
        return relation.getTargetRoleSingular() +
            " : " + targetName +  //$NON-NLS-1$
        	" [" + relation.getMinCardinality() + //$NON-NLS-1$
        	".." + maxC + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }
}