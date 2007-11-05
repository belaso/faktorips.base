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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.fields.CardinalityField;

public class InverseRelationPropertyPage extends WizardPage implements IBlockedValidationWizardPage, IHiddenWizardPage {
    
    private NewPcTypeAssociationWizard wizard;
    private UIToolkit toolkit;
    private BindingContext bindingContext;
    
    private ArrayList visibleProperties = new ArrayList(10);
    protected IPolicyCmptTypeAssociation association;
    private Text targetRoleSingularText;
    private Text targetRolePluralText;
    private CardinalityField cardinalityFieldMin;
    private CardinalityField cardinalityFieldMax;
    private Text targetText;
    private Text typeText;
    private Combo existingRelCombo;
    private Label existingRelLabel;

    private String prevSelExistingRelation;
    private boolean displayedBefore;
    private Text description;
    
    private Composite dynamicComposite;
    private Composite mainComposite;
    
    public InverseRelationPropertyPage(NewPcTypeAssociationWizard wizard, UIToolkit toolkit, BindingContext bindingContext) {
        super("InverseRelationPropertyPage", "Inverse relation properties", null);
        setDescription("Define new inverse relation");
        this.wizard = wizard;
        this.toolkit = toolkit;
        this.bindingContext = bindingContext;
        
        setPageComplete(true);
    }

    public void createControl(Composite parent) {
        mainComposite = toolkit.createLabelEditColumnComposite(parent);
        mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        // target
        toolkit.createFormLabel(mainComposite, "Target");
        targetText = toolkit.createText(mainComposite);
        targetText.setEnabled(false);

        // type
        toolkit.createFormLabel(mainComposite, "Type");
        typeText = toolkit.createText(mainComposite);
        typeText.setEnabled(false);

        // existing association
        existingRelLabel = toolkit.createFormLabel(mainComposite, "Existing association");
        existingRelCombo = toolkit.createCombo(mainComposite);
        existingRelCombo.addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event ev) {
                existingRelationSelectionChanged();
            }
        });
        
        dynamicComposite = createGeneralControls(mainComposite);
        setControl(mainComposite);
    }

    private Composite createGeneralControls(Composite root) {
        Composite parent = toolkit.createLabelEditColumnComposite(root);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan = 2;
        parent.setLayoutData(gd);
        
        Group group = toolkit.createGroup(parent, "Properties");
        gd.grabExcessVerticalSpace = false;
        
        Composite workArea = toolkit.createLabelEditColumnComposite(group);
        workArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false ));

        // top extensions
        wizard.getExtFactoryInverseAssociation().createControls(workArea, toolkit, association, IExtensionPropertyDefinition.POSITION_TOP);
        
        // role singular
        toolkit.createFormLabel(workArea, "Target role (singular):");
        targetRoleSingularText = toolkit.createText(workArea);
        visibleProperties.add(IPolicyCmptTypeAssociation.PROPERTY_TARGET_ROLE_SINGULAR);
        targetRoleSingularText.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (StringUtils.isEmpty(association.getTargetRoleSingular())) {
                    association.setTargetRoleSingular(association.getDefaultTargetRoleSingular());
                }
            }
        });
        
        // role plural
        toolkit.createFormLabel(workArea, "Target role (plural):");
        targetRolePluralText = toolkit.createText(workArea);
        visibleProperties.add(IPolicyCmptTypeAssociation.PROPERTY_TARGET_ROLE_PLURAL);
        targetRolePluralText.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (StringUtils.isEmpty(targetRolePluralText.getText()) && association.isTargetRolePluralRequired()) {
                    association.setTargetRolePlural(association.getDefaultTargetRolePlural());
                }
            }
        });
        
        // min cardinality
        toolkit.createFormLabel(workArea, "Minimum cardinality:");
        Text minCardinalityText = toolkit.createText(workArea);
        cardinalityFieldMin = new CardinalityField(minCardinalityText);
        cardinalityFieldMin.setSupportsNull(false);
        visibleProperties.add(IPolicyCmptTypeAssociation.PROPERTY_MIN_CARDINALITY);
        
        // max cardinality
        toolkit.createFormLabel(workArea, "Maximum cardinality:");
        Text maxCardinalityText = toolkit.createText(workArea);
        cardinalityFieldMax = new CardinalityField(maxCardinalityText);
        cardinalityFieldMax.setSupportsNull(false);
        visibleProperties.add(IPolicyCmptTypeAssociation.PROPERTY_MAX_CARDINALITY);
        
        // bottom extensions
        wizard.getExtFactoryInverseAssociation().createControls(workArea, toolkit, association, IExtensionPropertyDefinition.POSITION_BOTTOM);
        
        description = wizard.createDescriptionText(parent);
        visibleProperties.add(IPolicyCmptTypeAssociation.PROPERTY_DESCRIPTION);
        
        return parent;
    }

    public void setAssociationAndUpdatePage(IPolicyCmptTypeAssociation association) {
        this.association = association;
        prevSelExistingRelation = association==null?"":association.getName();
        bindingContext.removeBindings(targetRoleSingularText);
        bindingContext.removeBindings(targetRolePluralText);
        bindingContext.removeBindings(cardinalityFieldMin.getControl());
        bindingContext.removeBindings(cardinalityFieldMax.getControl());
        bindingContext.removeBindings(description);
 
        wizard.getExtFactoryAssociation().removeBinding(bindingContext);
        
        if (association != null){
            dynamicComposite.dispose();
            dynamicComposite = createGeneralControls(mainComposite);
            
            bindingContext.bindContent(targetRoleSingularText, association, IPolicyCmptTypeAssociation.PROPERTY_TARGET_ROLE_SINGULAR);
            bindingContext.bindContent(targetRolePluralText, association, IPolicyCmptTypeAssociation.PROPERTY_TARGET_ROLE_PLURAL);
            bindingContext.bindContent((Text)cardinalityFieldMin.getControl(), association, IPolicyCmptTypeAssociation.PROPERTY_MIN_CARDINALITY);
            bindingContext.bindContent((Text)cardinalityFieldMax.getControl(), association, IPolicyCmptTypeAssociation.PROPERTY_MAX_CARDINALITY);
            bindingContext.bindContent(description, association, IPolicyCmptTypeAssociation.PROPERTY_DESCRIPTION);
            
            targetText.setText(association.getTarget());
            typeText.setText(association.getAssociationType().getName());
            
            wizard.getExtFactoryInverseAssociation().bind(bindingContext);
            
            bindingContext.updateUI();
            
            mainComposite.pack(true);
            mainComposite.getParent().pack(true);
            mainComposite.getParent().layout();
        } else {
            targetRoleSingularText.setText("");
            targetRolePluralText.setText("");
            cardinalityFieldMin.setText("");
            cardinalityFieldMax.setText("");
            targetText.setText("");
            typeText.setText("");
            description.setText("");
        }
    }
    
    public List getProperties() {
        return visibleProperties;
    }

    public void setShowExistingRelationDropDown(boolean showExisting) {
        existingRelCombo.setVisible(showExisting);
        existingRelLabel.setVisible(showExisting);
    }

    public void setExistingAssociations(String[] associations) {
        existingRelCombo.setItems(associations);
        existingRelCombo.select(0);
    }

    public void refreshControls() {
        setControlChangeable(!wizard.isExistingReverseRelation());
    }
    
    private void setControlChangeable(boolean changeable){
        toolkit.setDataChangeable(targetRoleSingularText, changeable);
        toolkit.setDataChangeable(targetRolePluralText, changeable);
        toolkit.setDataChangeable(cardinalityFieldMin.getControl(), changeable);
        toolkit.setDataChangeable(cardinalityFieldMax.getControl(), changeable);
    }
    
    /**
     * Event function to indicate a change of the existing relation.
     */
    private void existingRelationSelectionChanged() {
        int selIdx = existingRelCombo.getSelectionIndex();
        if (selIdx>=0){
            String selExistingRelation = existingRelCombo.getItem(selIdx);
            if (!selExistingRelation.equals(prevSelExistingRelation)){
                wizard.storeExistingInverseRelation(selExistingRelation);
                wizard.contentsChanged(this);
            }
        }
    }
    
    /**
     * @return <code>false</code> if no inverse association should be created or no existing
     *         relation exists otherwise <code>true</code>.
     */
    public boolean isPageVisible() {
        boolean visible = false;
        if (wizard.isNoneReverseRelation()) {
            return false;
        } else if (wizard.isExistingReverseRelation()) {
            try {
                if (NewPcTypeAssociationWizard.getCorrespondingTargetRelations(wizard.getAssociation(),
                        wizard.getTargetPolicyCmptType()).size() == 0) {
                    visible = false;
                } else {
                    visible = true;
                }
            } catch (CoreException e) {
                wizard.showAndLogError(e);
            }
        } else {
            visible = true;
        }
        if (visible) {
            if (!displayedBefore) {
                displayedBefore = true;
                setPageComplete(false);
            }
        }
        return visible;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean canFlipToNextPage() {
        setErrorMessage(null);
        boolean valid = wizard.validatePage(this, false);
        
        if (getNextPage() == null){
            return false;
        }
        
        return valid;
    }
    
    /**
     * {@inheritDoc}
     */
    public void setVisible(boolean visible) {
        if (visible){
            wizard.handleInverseAssociationSelectionState();
            wizard.validatePage(this, true);
        }
        super.setVisible(visible);
    }
}
