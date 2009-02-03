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

package org.faktorips.devtools.core.ui.wizards.enumtype;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.enumtype.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controls.IpsObjectRefControl;
import org.faktorips.devtools.core.ui.wizards.IpsObjectPage;

/**
 * The wizard page for the new enum type wizard.
 * 
 * @author Alexander Weickmann
 */
public class EnumTypePage extends IpsObjectPage {

    private TextButtonField supertypeField;
    private CheckboxField abstractField;
    private CheckboxField valuesArePartOfModelField;

    /**
     * Creates the enum type page.
     * 
     * @param selection
     */
    public EnumTypePage(IStructuredSelection selection) {
        super(IpsObjectType.ENUM_TYPE, selection, "PAGE NAME"); //$NON-NLS-1$
        setImageDescriptor(IpsUIPlugin.getDefault().getImageDescriptor("wizards/NewEnumTypeWizard.png")); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void fillNameComposite(Composite nameComposite, UIToolkit toolkit) {
        super.fillNameComposite(nameComposite, toolkit);

        // Super type
        toolkit.createFormLabel(nameComposite, Messages.Fields_SuperEnumType + ':');
        IpsObjectRefControl superTypeControl = toolkit.createEnumTypeRefControl(null, nameComposite);
        supertypeField = new TextButtonField(superTypeControl);
        supertypeField.addChangeListener(this);

        // Abstract
        toolkit.createLabel(nameComposite, ""); //$NON-NLS-1$
        abstractField = new CheckboxField(toolkit.createCheckbox(nameComposite, Messages.Fields_Abstract));
        abstractField.addChangeListener(this);

        // Values are part of model
        toolkit.createLabel(nameComposite, ""); //$NON-NLS-1$
        valuesArePartOfModelField = new CheckboxField(toolkit.createCheckbox(nameComposite,
                Messages.Fields_ValuesArePartOfModel));
        valuesArePartOfModelField.addChangeListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void sourceFolderChanged() {
        super.sourceFolderChanged();
        IIpsPackageFragmentRoot root = getIpsPackageFragmentRoot();
        if (root != null) {
            ((IpsObjectRefControl)supertypeField.getControl()).setIpsProject(root.getIpsProject());
        } else {
            ((IpsObjectRefControl)supertypeField.getControl()).setIpsProject(null);
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void finishIpsObjects(IIpsObject newIpsObject, List modifiedIpsObjects) throws CoreException {
        super.finishIpsObjects(newIpsObject, modifiedIpsObjects);

        IEnumType newEnumType = (IEnumType)newIpsObject;
        newEnumType.setIsAbstract((Boolean)abstractField.getValue());
        newEnumType.setValuesArePartOfModel((Boolean)valuesArePartOfModelField.getValue());
        newEnumType.setSuperEnumType(supertypeField.getText());

        modifiedIpsObjects.add(newEnumType);
        newEnumType.getIpsSrcFile().markAsDirty();
    }

}
