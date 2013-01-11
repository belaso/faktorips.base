/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype;

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPartSite;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IDeleteListener;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.type.AttributesSection;

public class PolicyCmptTypeAttributesSection extends AttributesSection {

    private PolicyCmptTypeAttributesComposite attributesComposite;

    public PolicyCmptTypeAttributesSection(IPolicyCmptType policyCmptType, Composite parent, IWorkbenchPartSite site,
            UIToolkit toolkit) {

        super(policyCmptType, parent, site, toolkit);
    }

    @Override
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        attributesComposite = new PolicyCmptTypeAttributesComposite(getPolicyCmptType(), parent, toolkit);
        return attributesComposite;
    }

    @Override
    protected void performRefresh() {
        super.performRefresh();
        attributesComposite.updateOverrideButtonEnabledState();
    }

    private IPolicyCmptType getPolicyCmptType() {
        return (IPolicyCmptType)getType();
    }

    private class PolicyCmptTypeAttributesComposite extends AttributesComposite {

        public PolicyCmptTypeAttributesComposite(IPolicyCmptType policyCmptType, Composite parent, UIToolkit toolkit) {
            super(policyCmptType, parent, toolkit);
            addDeleteListener();
        }

        private void addDeleteListener() {
            super.addDeleteListener(new IDeleteListener() {
                @Override
                public boolean aboutToDelete(IIpsObjectPart part) {
                    IValidationRule rule = findValidationRule(part);
                    if (rule == null) {
                        // Nothing to do if no special rule is defined.
                        return true;
                    }
                    String msg = Messages.AttributesSection_deleteMessage;
                    boolean delete = MessageDialog
                            .openQuestion(getShell(), Messages.AttributesSection_deleteTitle, msg);
                    if (delete) {
                        rule.delete();
                    } else if (!delete) {
                        rule.setCheckValueAgainstValueSetRule(false);
                    }
                    return true;
                }

                private IValidationRule findValidationRule(IIpsObjectPart part) {
                    String name = part.getName();
                    List<IValidationRule> rules = getPolicyCmptType().getValidationRules();
                    for (IValidationRule rule : rules) {
                        if (!rule.isCheckValueAgainstValueSetRule()) {
                            continue;
                        }
                        String[] attributes = rule.getValidatedAttributes();
                        if (attributes.length == 1 && attributes[0].equals(name)) {
                            return rule;
                        }
                    }
                    return null;
                }

                @Override
                public void deleted(IIpsObjectPart part) {
                    // Nothing to do.
                }
            });
        }

        @Override
        protected void updateOverrideButtonEnabledState() {
            super.updateOverrideButtonEnabledState();
        }

        @Override
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) {
            return new AttributeEditDialog((IPolicyCmptTypeAttribute)part, shell);
        }

    }

}
