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

package org.faktorips.devtools.core.ui.editors;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.ui.controller.IpsPartUIController;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.util.memento.Memento;


/**
 *
 */
public abstract class IpsPartEditDialog extends EditDialog {
    
    protected IpsPartUIController uiController;
    private TextField descriptionField;
    private Memento oldState;
    private boolean dirty = false;

    public IpsPartEditDialog(
            IIpsObjectPart part, 
            Shell parentShell, 
            String windowTitle) {
        this(part, parentShell, windowTitle, false, true);
    }
    
    public IpsPartEditDialog(
            IIpsObjectPart part, 
            Shell parentShell, 
            String windowTitle,
            boolean useTabFolder) {
        this(part, parentShell, windowTitle, useTabFolder, true);
    }

        public IpsPartEditDialog(
            IIpsObjectPart part, 
            Shell parentShell, 
            String windowTitle,
            boolean useTabFolder,
            boolean dataChangeable) {
        super(parentShell, windowTitle, useTabFolder, dataChangeable);
        uiController = createUIController(part);
        oldState = part.getIpsObject().newMemento();
        dirty = part.getIpsObject().getIpsSrcFile().isDirty();
    }
    
    // overwritten to be sure to get the cancel-button as soon as possible...
    protected void createButtonsForButtonBar(Composite parent) {
    	super.createButtonsForButtonBar(parent);
        super.getButton(Window.CANCEL).addSelectionListener(new SelectionListener() {
		
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		
			public void widgetSelected(SelectionEvent e) {
				handleAbortion();
			}
		});
    }
    
    protected void handleShellCloseEvent() {
		handleAbortion();
    	super.handleShellCloseEvent();
    }
    
    private void handleAbortion() {
		uiController.getIpsObjectPart().getIpsObject().setState(oldState);
		if (!dirty) {
			uiController.getIpsObjectPart().getIpsObject().getIpsSrcFile().markAsClean();
		}
    }
    
	protected Control createContents(Composite parent) {
	    Control control = super.createContents(parent);
        connectToModel();
        uiController.updateUI();
        setTitle(buildTitle());
        return control;
	}
    
	protected TabItem createDescriptionTabItem(TabFolder folder) {
	    Composite c = createTabItemComposite(folder, 1, false);
	    Text text = uiToolkit.createMultilineText(c);
	    descriptionField = new TextField(text);
	    TabItem item = new TabItem(folder, SWT.NONE);
	    item.setText(Messages.IpsPartEditDialog_description);
	    item.setControl(c);
	    return item;
	}
    
    protected IpsPartUIController createUIController(IIpsObjectPart part) {
        IpsPartUIController controller = new IpsPartUIController(part) {
            public void valueChanged(FieldValueChangedEvent e) {
                try {
                    super.valueChanged(e);
                    setTitle(buildTitle());
                    // showValidationResult(getPdPart().validate());
                } catch (Exception ex) {
                    IpsPlugin.logAndShowErrorDialog(ex);
                }
            }
        };
        return controller;
    }

    /**
     * Returns the part being edited.
     */
    public IIpsObjectPart getIpsPart() {
        return uiController.getIpsObjectPart();
    }
    
    protected String buildTitle() {
        IIpsObjectPart part = getIpsPart();
        if (part.getParent() instanceof IIpsObjectGeneration) {
            return part.getIpsObject().getName() + " "  //$NON-NLS-1$
            	+ part.getParent().getName() + "." + part.getName(); //$NON-NLS-1$
        }
        return part.getIpsObject().getName() + "." + part.getName(); //$NON-NLS-1$
    }
    
    protected void connectToModel() {
        if (descriptionField!=null) {
            uiController.add(descriptionField, getIpsPart(), IIpsObjectPart.PROPERTY_DESCRIPTION);
        }
    }

    protected void setEnabledDescription(boolean enabled) {
    	if (descriptionField != null) {
    		descriptionField.getControl().setEnabled(enabled);
    	}
    }
}
