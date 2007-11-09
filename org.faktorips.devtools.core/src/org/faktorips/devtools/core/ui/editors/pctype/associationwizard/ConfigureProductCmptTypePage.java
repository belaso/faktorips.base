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

package org.faktorips.devtools.core.ui.editors.pctype.associationwizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * Page to define on of the following option:<ul>
 * <li>create a new product component type association
 * <li>don't create a new product component type association
 * </ul>
 * 
 * @author Joerg Ortmann
 */
public class ConfigureProductCmptTypePage extends WizardPage {
    
    private NewPcTypeAssociationWizard wizard;
    private UIToolkit toolkit;

    private Button noAssociationOnProductCmptType;
    private Button newAssociationOnProductCmptType;
    private Button prevSelection;

    public ConfigureProductCmptTypePage(NewPcTypeAssociationWizard wizard, UIToolkit toolkit) {
        super("InverseRelationPage", "Inverse relation", null);
        setDescription("Define association on product component type");
        this.wizard = wizard;
        this.toolkit = toolkit;
        
        setPageComplete(true);
    }
    
    public void createControl(Composite parent) {
        Composite pageComposite = wizard.createPageComposite(parent);
        
        InverseRelationSelectionListener listener = new InverseRelationSelectionListener();
        
        newAssociationOnProductCmptType = toolkit.createRadioButton(pageComposite, "Create a new product component type association");
        newAssociationOnProductCmptType.addSelectionListener(listener);
        
        toolkit.createVerticalSpacer(pageComposite, 1);
        
        noAssociationOnProductCmptType = toolkit.createRadioButton(pageComposite, "Do not create association on product component type");
        noAssociationOnProductCmptType.addSelectionListener(listener);
        
        // set the default selection
        newAssociationOnProductCmptType.setSelection(true);
        prevSelection = noAssociationOnProductCmptType;

        setControl(pageComposite);
    }

    /**
     * Listener for the radio buttons.
     */
    private class InverseRelationSelectionListener implements SelectionListener {
        public void widgetSelected(SelectionEvent e) {
            if (prevSelection != e.getSource()){
                prevSelection = (Button) e.getSource();
                if (e.getSource() == newAssociationOnProductCmptType) {
                    wizard.setConfigureProductCmptType(true);
                }else if(e.getSource() == noAssociationOnProductCmptType){
                    wizard.setConfigureProductCmptType(false);
                }
                wizard.pageHasChanged();
            }
        }
        public void widgetDefaultSelected(SelectionEvent e) {
            widgetSelected(e);
        }
    }
}
