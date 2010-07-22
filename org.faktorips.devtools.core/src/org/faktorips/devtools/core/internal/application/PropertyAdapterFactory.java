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

package org.faktorips.devtools.core.internal.application;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.views.properties.FilePropertySource;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.ResourcePropertySource;

/**
 * Adapter factory for adapting <code>IResource</code>s to <code>IPropertySource</code>s.
 * 
 * @author Thorsten Guenther
 */
class PropertyAdapterFactory implements IAdapterFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object getAdapter(Object o, Class adapterType) {
        // can suppress the warning as the Eclipse interface defineds the method signature
        if (adapterType.isInstance(o)) {
            return o;
        }
        if (adapterType == IPropertySource.class) {
            if (o instanceof IResource) {
                IResource resource = (IResource)o;
                if (resource.getType() == IResource.FILE) {
                    return new FilePropertySource((IFile)o);
                } else {
                    return new ResourcePropertySource((IResource)o);
                }
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Class[] getAdapterList() {
        // can suppress the warning as the Eclipse interface defineds the method signature
        return new Class[] { IPropertySource.class };
    }
}
