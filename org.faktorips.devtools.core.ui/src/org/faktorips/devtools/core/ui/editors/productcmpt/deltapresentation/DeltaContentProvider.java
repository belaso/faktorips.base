/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.productcmpt.deltapresentation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.core.model.productcmpt.DeltaType;
import org.faktorips.devtools.core.model.productcmpt.IDeltaEntry;
import org.faktorips.devtools.core.model.productcmpt.IGenerationToTypeDelta;

/**
 * 
 * @author Jan Ortmann
 */
public class DeltaContentProvider implements ITreeContentProvider {

    private IGenerationToTypeDelta delta;

    public DeltaContentProvider() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        delta = (IGenerationToTypeDelta)newInput;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object[] getElements(Object inputElement) {
        List<DeltaType> elements = new ArrayList<DeltaType>();
        for (DeltaType element : DeltaType.ALL_TYPES) {
            if (delta.getEntries(element).length > 0) {
                elements.add(element);
            }
        }
        return elements.toArray();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasChildren(Object element) {
        return element instanceof DeltaType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object[] getChildren(Object parentElement) {
        if (!(parentElement instanceof DeltaType)) {
            return new Object[0];
        }
        return delta.getEntries((DeltaType)parentElement);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getParent(Object element) {
        if (element instanceof IDeltaEntry) {
            return ((IDeltaEntry)element).getDeltaType();
        }
        return delta;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {

    }

}
