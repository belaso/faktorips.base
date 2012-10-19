/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.productcmpt.link;

import org.eclipse.core.runtime.IAdapterFactory;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathContainer;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;

/**
 * AdapterFactory to adapt a {@link LinkViewItem LinkViewItem} to an {@link IProductCmptLink}.
 * 
 * @author widmaier
 */
public class LinkViewItemAdapterFactory implements IAdapterFactory {

    @SuppressWarnings("rawtypes")
    // IAdaptable forces raw type upon implementing classes
    @Override
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (IIpsObjectPathContainer.class.equals(adapterType)) {
            return ((LinkViewItem)adaptableObject).getLink();
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    // IAdaptable forces raw type upon implementing classes
    @Override
    public Class[] getAdapterList() {
        return new Class[] { IIpsObjectPathContainer.class };
    }

}
