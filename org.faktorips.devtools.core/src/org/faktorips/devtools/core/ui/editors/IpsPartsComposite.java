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

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.MessageCueLabelProvider;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.util.memento.Memento;
import org.faktorips.util.message.MessageList;


/**
 * A composite that shows parts in a table viewer and provides an
 * area containing a new, edit and delete button.
 */
public abstract class IpsPartsComposite extends ViewerButtonComposite implements ISelectionProviderActivation{

    // the object the parts belong to.
    private IIpsObject ipsObject;

    // buttons
	protected Button newButton;
	protected Button editButton;
	protected Button deleteButton;
	protected Button upButton;
	protected Button downButton;
	
	
	// flag that controls if a new part can be created.
	private boolean canCreate;
	
	// flag that controls if a part can be edited.
	private boolean canEdit;
	
	// flag that controls if a part can be deleted.
	private boolean canDelete;
	
	// flag that controls if parts can be moved.
	private boolean canMove;
	
	// Table to show the content
	private Table table;
	
	// listener to start editing on double click.
	private MouseAdapter editDoubleClickListener;
	
	// flag that control if the edit button is shown (edit is also possible
	// via double click.
	private boolean showEditButton;
	
    // the table view of this composite 
    private TableViewer viewer;
    
    public IpsPartsComposite(
            IIpsObject pdObject, 
            Composite parent, 
            UIToolkit toolkit) {
        this(pdObject, parent, true, true, true, true, true, toolkit);
    }

    public IpsPartsComposite(
            IIpsObject pdObject, 
            Composite parent,
            boolean canCreate,
            boolean canEdit,
            boolean canDelete,
            boolean canMove,
            boolean showEditButton,
            UIToolkit toolkit) {
        super(parent);
        this.ipsObject = pdObject;
        this.canCreate = canCreate;
        this.canEdit = canEdit;
        this.canDelete = canDelete;
        this.canMove = canMove;
        this.showEditButton = showEditButton;
        initControls(toolkit);
        toolkit.getFormToolkit().adapt(this);
    }
    
    /**
     * Returns the IpsObject the parts belong to.
     */
    public IIpsObject getIpsObject() {
        return ipsObject;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.forms.ViewerButtonSection#createViewer(org.eclipse.swt.widgets.Composite, org.eclipse.ui.forms.widgets.FormToolkit)
     */
    protected Viewer createViewer(Composite parent, UIToolkit toolkit) {
		table = toolkit.getFormToolkit().createTable(parent, SWT.NONE);
		setEditDoubleClickListenerEnabled(true);

		viewer = new TableViewer(table);
		viewer.setContentProvider(createContentProvider());
		ILabelProvider lp = createLabelProvider();
		viewer.setLabelProvider(new MessageCueLabelProvider(lp));
		
		new TableMessageHoverService(viewer) {

            protected MessageList getMessagesFor(Object element) throws CoreException {
                return ((IIpsObjectPart)element).validate();
            }
		    
		};
		
		return viewer;
    }
    
    /**
     * Enable or disable the listener to start editing on double click.
     * 
     * @param enabled <code>true</code> to enable editing on double click.
     */
    protected void setEditDoubleClickListenerEnabled(boolean enabled) {
    	if (enabled) {
    		if (editDoubleClickListener == null) {
    			editDoubleClickListener = new MouseAdapter() {
    			    public void mouseDoubleClick(MouseEvent e) {
    			        editPart();
    			    }
    			};
    		}
    		table.addMouseListener(editDoubleClickListener);
    	}
    	else if (editDoubleClickListener != null) {
    		table.removeMouseListener(editDoubleClickListener);
    	}
    }
    
    /**
     * Creates the content provider for the table viewer.
     */
    protected abstract IStructuredContentProvider createContentProvider();
    
    /**
     * Creates the label provider for the table viewer. Returns the
     * default label provider by default
     */
    protected ILabelProvider createLabelProvider() {
        return new DefaultLabelProvider();
    }

    /**
     * Creates new, edit and delete buttons (if enabled). Can be overridden if other buttons are needed.
     * 
     * @see org.faktorips.devtools.core.ui.forms.ViewerButtonSection#createButtons(org.eclipse.swt.widgets.Composite, org.eclipse.ui.forms.widgets.FormToolkit)
     */
    protected boolean createButtons(Composite buttons, UIToolkit toolkit) {
        boolean buttonCreated = false;
        if (canCreate) {
            createNewButton(buttons, toolkit);
            buttonCreated = true;
        }
		if (canEdit && showEditButton) {
		    createEditButton(buttons, toolkit);    
            buttonCreated = true;
		}
		if (canDelete) {
		    createDeleteButton(buttons, toolkit);    
            buttonCreated = true;
		}
		if (canMove) {
		    if (buttonCreated) {
		        createButtonSpace(buttons, toolkit);
		    }
		    createMoveButtons(buttons, toolkit);
		    buttonCreated = true;
		}
		return buttonCreated;
    }
    
    protected final void createButtonSpace(Composite buttons, UIToolkit toolkit) {
		Label spacer = toolkit.createLabel(buttons, null);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING);
		data.heightHint = 5;
		spacer.setLayoutData(data);
    }
    
    protected final void createNewButton(Composite buttons, UIToolkit toolkit) {
		newButton = toolkit.createButton(buttons, Messages.IpsPartsComposite_buttonNew);
		newButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
		newButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				try {
					newPart();
					
				} catch (Exception ex) {
					IpsPlugin.logAndShowErrorDialog(ex);
				}
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
    }
    
    protected final void createEditButton(Composite buttons, UIToolkit toolkit) {
		editButton = toolkit.createButton(buttons, Messages.IpsPartsComposite_buttonEdit);
		editButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
		editButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				try {
					editPart();
				} catch (Exception ex) {
				    IpsPlugin.logAndShowErrorDialog(ex);
				}
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
    }
    
    protected final void createDeleteButton(Composite buttons, UIToolkit toolkit) {
		deleteButton = toolkit.createButton(buttons, Messages.IpsPartsComposite_buttonDelete);
		deleteButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
		deleteButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				try {
					deletePart();
				} catch (Exception ex) {
				    IpsPlugin.logAndShowErrorDialog(ex);
				}
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
    }
    
    protected final void createMoveButtons(Composite buttons, UIToolkit toolkit) {
		upButton = toolkit.createButton(buttons, Messages.IpsPartsComposite_buttonUp);
		upButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
		upButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				try {
					moveParts(true);
				} catch (Exception ex) {
				    IpsPlugin.logAndShowErrorDialog(ex);
				}
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		downButton = toolkit.createButton(buttons, Messages.IpsPartsComposite_buttonDown);
		downButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
		downButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				try {
					moveParts(false);
				} catch (Exception ex) {
				    IpsPlugin.logAndShowErrorDialog(ex);
				}
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
        
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.forms.ViewerButtonSection#updateButtonEnabledStates()
     */
    protected void updateButtonEnabledStates() {
		boolean itemSelected = false;
		if (getViewer().getSelection()!=null && !getViewer().getSelection().isEmpty()) {
			itemSelected = true;
		}
		if (newButton!=null) {
		    newButton.setEnabled(true);    
		}
		if (editButton!=null) {
		    editButton.setEnabled(itemSelected);    
		}
		if (deleteButton!=null) {
		    deleteButton.setEnabled(itemSelected);    
		}
		if (upButton!=null) {
		    upButton.setEnabled(itemSelected);    
		}
		if (downButton!=null) {
		    downButton.setEnabled(itemSelected);    
		}
    }
    
    public final IIpsObjectPart getSelectedPart() {
        return (IIpsObjectPart)getSelectedObject();
    }
    
    private void newPart() {
        try {
            IIpsSrcFile file = ipsObject.getIpsSrcFile();
            boolean dirty = file.isDirty();
            Memento memento = ipsObject.newMemento();
            IIpsObjectPart newPart = newIpsPart();
            EditDialog dialog = createEditDialog(newPart, getShell());
            dialog.open();
            if (dialog.getReturnCode()==Window.CANCEL) {
                ipsObject.setState(memento);
                if (!dirty) {
                    file.markAsClean();
                }
            }
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        refresh();
    }

    private void editPart() {
        TableViewer viewer = (TableViewer)getViewer();
        if (viewer.getSelection().isEmpty()) {
            return;
        }
        try {
            IIpsSrcFile file = ipsObject.getIpsSrcFile();
            boolean dirty = file.isDirty();
            IIpsObjectPart part = getSelectedPart();
            Memento memento = part.newMemento();
            EditDialog dialog = createEditDialog(part, getShell());
            if (dialog == null) {
            	return;
            }
            dialog.open();
            if (dialog.getReturnCode()==Window.CANCEL) {
                part.setState(memento);
                if (!dirty) {
                    file.markAsClean();
                }
            }
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        refresh();
    }

    private void deletePart() {
        TableViewer viewer = (TableViewer)getViewer();
        if (viewer.getSelection().isEmpty()) {
            return;
        }
        try {
            Table table = (Table)getViewer().getControl();
            int selectedIndexAfterDeletion = Math.min(table.getSelectionIndex(), table.getItemCount()-2);
            IIpsObjectPart part = getSelectedPart();
            fireAboutToDelete(part);
            part.delete();
            if (selectedIndexAfterDeletion>=0) {
                Object selected = viewer.getElementAt(selectedIndexAfterDeletion);
                viewer.setSelection(new StructuredSelection(selected), true);
            }
            fireDeleted(part);
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        refresh();
    }
    
    /**
     * Adds the listener as one being notified when the selected part changes.
     */
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        getViewer().addSelectionChangedListener(listener);
    }
    
    /**
     * Removes the listener as one being notified when the selected part changes.
     */
    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
        getViewer().removeSelectionChangedListener(listener);
    }
    
    private void moveParts(boolean up) {
        TableViewer viewer = (TableViewer)getViewer();
        if (viewer.getSelection().isEmpty()) {
            return;
        }
        Table table = viewer.getTable();
		int[] newSelection = moveParts(table.getSelectionIndices(), up);
		viewer.refresh();
		table.setSelection(newSelection);
		viewer.getControl().setFocus();
        refresh();
    }
    
    /**
     * Creates a new part.
     */
    protected abstract IIpsObjectPart newIpsPart();
    
    /**
     * Creates a dialog to edit the part. 
     */
    protected abstract EditDialog createEditDialog(IIpsObjectPart part, Shell shell) throws CoreException;
    
    /**
     * Moves the parts indentified by the indexes in the model object up or down.
     * 
     * @return the new indices of the moved parts.
     */
    protected int[] moveParts(int[] indexes, boolean up) {
        return indexes;
    }
    
    private ArrayList deleteListeners = new ArrayList();
    protected void addDeleteListener(IDeleteListener listener) {
    	deleteListeners.add(listener);
    }
    
    protected void removeDeleteListener(IDeleteListener listener) {
    	deleteListeners.remove(listener);
    }
    
    private void fireAboutToDelete(IIpsObjectPart part) {
    	for (Iterator iter = deleteListeners.iterator(); iter.hasNext();) {
			IDeleteListener listener = (IDeleteListener) iter.next();
			listener.aboutToDelete(part);
		}
    }

    private void fireDeleted(IIpsObjectPart part) {
    	for (Iterator iter = deleteListeners.iterator(); iter.hasNext();) {
			IDeleteListener listener = (IDeleteListener) iter.next();
			listener.deleted(part);
		}
    }

    /**
     * {@inheritDoc}
     */
    public ISelectionProvider getSelectionProvider() {
        return viewer;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isActivated() {
        
        if(viewer == null){
            return false;
        }
        return viewer.getTable().getDisplay().getCursorControl() == viewer.getTable() ? true : false;
    }
    
    
}
