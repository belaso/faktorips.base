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
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.util.memento.Memento;


/**
 * Base class for dialogs that allows to edit an ips object part.
 * In contrast to the original IpsPartEditDialog this version uses the new databinding. 
 * 
 * @since 2.0
 *  
 * @see BindingContext
 * 
 * @author Jan Ortmann
 */
public abstract class IpsPartEditDialog2 extends EditDialog implements ContentsChangeListener {
    
    protected BindingContext bindingContext = new BindingContext();;
    private IIpsObjectPart part;
    private TextField descriptionField;
    private Memento oldState;
    private boolean dirty = false;

    public IpsPartEditDialog2(
            IIpsObjectPart part, 
            Shell parentShell, 
            String windowTitle) {
        this(part, parentShell, windowTitle, false);
    }
    
    public IpsPartEditDialog2(
            IIpsObjectPart part, 
            Shell parentShell, 
            String windowTitle,
            boolean useTabFolder) {
        super(parentShell, windowTitle, useTabFolder);
        this.part = part;
        oldState = part.getIpsObject().newMemento();
        dirty = part.getIpsObject().getIpsSrcFile().isDirty();
        IpsPlugin.getDefault().getIpsModel().addChangeListener(this);
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
    
    public boolean close() {
        if (bindingContext!=null) {
            bindingContext.dispose();
        }
        IpsPlugin.getDefault().getIpsModel().removeChangeListener(this);
        return super.close();
    }
    
    private void handleAbortion() {
		part.getIpsObject().setState(oldState);
		if (!dirty) {
			part.getIpsObject().getIpsSrcFile().markAsClean();
		}
    }
    
	protected Control createContents(Composite parent) {
	    Control control = super.createContents(parent);
        bindingContext.updateUI();
        setTitle(buildTitle());
        return control;
	}
    
	protected TabItem createDescriptionTabItem(TabFolder folder) {
	    Composite c = createTabItemComposite(folder, 1, false);
	    Text text = uiToolkit.createMultilineText(c);
        bindingContext.bindContent(text, getIpsPart(), IIpsObjectPart.PROPERTY_DESCRIPTION);
	    TabItem item = new TabItem(folder, SWT.NONE);
	    item.setText(Messages.IpsPartEditDialog_description);
	    item.setControl(c);
	    return item;
	}
    
    /**
     * Returns the part being edited.
     */
    public IIpsObjectPart getIpsPart() {
        return part;
    }
    
    protected void updateTitleInTitleArea() {
        setTitle(buildTitle());
    }
    
    protected String buildTitle() {
        IIpsObjectPart part = getIpsPart();
        if (part.getParent() instanceof IIpsObjectGeneration) {
            return part.getIpsObject().getName() + " "  //$NON-NLS-1$
            	+ part.getParent().getName() + "." + part.getName(); //$NON-NLS-1$
        }
        return part.getIpsObject().getName() + "." + part.getName(); //$NON-NLS-1$
    }
    
    protected void setEnabledDescription(boolean enabled) {
    	if (descriptionField != null) {
    		descriptionField.getControl().setEnabled(enabled);
    	}
    }

    /**
     * {@inheritDoc}
     */
    public void contentsChanged(ContentChangeEvent event) {
        if (event.getIpsSrcFile().equals(getIpsPart().getIpsSrcFile())) {
            updateTitleInTitleArea();
        }
    }

    
}
