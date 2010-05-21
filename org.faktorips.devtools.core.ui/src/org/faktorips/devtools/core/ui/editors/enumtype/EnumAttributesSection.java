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

package org.faktorips.devtools.core.ui.editors.enumtype;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.actions.RenameHandler;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;
import org.faktorips.util.ArgumentCheck;

/**
 * The UI section for the <tt>EnumTypeStructurePage</tt> that contains the <tt>IEnumAttribute</tt>s
 * of the <tt>IEnumType</tt> to be edited.
 * 
 * @see EnumTypeStructurePage
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumAttributesSection extends SimpleIpsPartsSection {

    /** The UI composite for the section. */
    EnumAttributesComposite enumAttributesComposite;

    private IpsObjectEditorPage page;

    /**
     * Creates a new <tt>EnumAttributesSection</tt> containing the <tt>IEnumAttribute</tt>s of the
     * given <tt>IEnumType</tt>.
     * 
     * @param page The <tt>IpsObjectEditorPage</tt> this section belongs to.
     * @param enumType The <tt>IEnumType</tt> to show the <tt>IEnumAttribute</tt>s from.
     * @param parent The parent UI composite.
     * @param toolkit The UI toolkit that shall be used to create UI elements.
     */
    public EnumAttributesSection(IpsObjectEditorPage page, IEnumType enumType, Composite parent, UIToolkit toolkit) {
        super(enumType, parent, Messages.EnumAttributesSection_title, toolkit);
        ArgumentCheck.notNull(page);
        this.page = page;
        ((EnumAttributesComposite)getPartsComposite()).createContextMenu();
    }

    @Override
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        enumAttributesComposite = new EnumAttributesComposite(getEnumType(), parent, toolkit);
        return enumAttributesComposite;
    }

    private IEnumType getEnumType() {
        return (IEnumType)getIpsObject();
    }

    @Override
    protected void performRefresh() {
        super.performRefresh();
        enumAttributesComposite.updateInheritButtonEnabledState();
        try {
            enumAttributesComposite.setCanDelete(!(getEnumType().isCapableOfContainingValues()));
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * A composite that shows an <tt>IEnumType</tt>'s attributes in a viewer and allows to edit
     * these attributes in a dialog, to create new attributes, move attributes and to delete
     * attributes.
     */
    private class EnumAttributesComposite extends IpsPartsComposite implements ISelectionChangedListener {

        /** The <tt>IEnumType</tt> being edited by the editor. */
        private IEnumType enumType;

        /**
         * Button that opens a dialog that enables the user to inherit attributes from the supertype
         * hierarchy.
         */
        private Button inheritButton;

        /**
         * Creates a new <tt>EnumAttributesComposite</tt> based upon the attributes of the given
         * <tt>IEnumType</tt>.
         * 
         * @param enumType The <tt>IEnumType</tt> to show the <tt>IEnumAttribute</tt>s of.
         * @param parent The parent UI composite.
         * @param toolkit The UI toolkit to create new UI elements with.
         * 
         * @throws NullPointerException If <tt>enumType</tt> is <tt>null</tt>.
         */
        public EnumAttributesComposite(IEnumType enumType, Composite parent, UIToolkit toolkit) {
            super(enumType, parent, toolkit);
            ArgumentCheck.notNull(enumType);
            this.enumType = enumType;
            addSelectionChangedListener(this);
        }

        private void createContextMenu() {
            MenuManager manager = new MenuManager();
            MenuManager refactorSubmenu = new MenuManager(Messages.EnumAttributesSection_submenuRefactor);

            manager.add(refactorSubmenu);
            manager.add(new Separator());

            refactorSubmenu.add(RenameHandler.getContributionItem());
            Menu contextMenu = manager.createContextMenu(getViewer().getControl());
            getViewer().getControl().setMenu(contextMenu);
        }

        @Override
        protected IStructuredContentProvider createContentProvider() {
            return new IStructuredContentProvider() {

                @Override
                public Object[] getElements(Object inputElement) {
                    return enumType.getEnumAttributesIncludeSupertypeCopies(true).toArray();
                }

                @Override
                public void dispose() {

                }

                @Override
                public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

                }

            };
        }

        @Override
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) throws CoreException {
            return new EnumAttributeEditDialog((IEnumAttribute)part, shell);
        }

        @Override
        protected IIpsObjectPart newIpsPart() throws CoreException {
            IEnumAttribute newEnumAttribute = enumType.newEnumAttribute();

            /*
             * If this is the first attribute to be created and the values are being defined in the
             * EnumType then make sure that there will be one EnumValue available for editing.
             */
            if (enumType.getEnumAttributesCountIncludeSupertypeCopies(true) == 1) {
                if (enumType.isContainingValues() && !(enumType.isAbstract())) {
                    if (enumType.getEnumValuesCount() == 0) {
                        enumType.newEnumValue();
                    }
                }
            }

            return newEnumAttribute;
        }

        @Override
        protected void deleteIpsPart(IIpsObjectPart partToDelete) throws CoreException {
            IEnumAttribute enumAttributeToDelete = (IEnumAttribute)partToDelete;
            enumType.deleteEnumAttributeWithValues(enumAttributeToDelete);

            // Delete all EnumValues if there are no more EnumAttributes.
            if (enumType.getEnumAttributesCountIncludeSupertypeCopies(enumType.isCapableOfContainingValues()) == 0) {
                for (IEnumValue currentEnumValue : enumType.getEnumValues()) {
                    currentEnumValue.delete();
                }
            }
        }

        @Override
        protected int[] moveParts(int[] indexes, boolean up) {
            int newIndex = indexes[0];
            List<IEnumAttribute> enumAttributes = enumType.getEnumAttributesIncludeSupertypeCopies(true);

            IEnumAttribute enumAttributeToMove = enumAttributes.get(newIndex);
            try {
                newIndex = enumType.moveEnumAttribute(enumAttributeToMove, up);
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }

            return new int[] { newIndex };
        }

        @Override
        protected boolean createButtons(Composite buttons, UIToolkit toolkit) {
            super.createButtons(buttons, toolkit);
            createButtonSpace(buttons, toolkit);

            inheritButton = toolkit.createButton(buttons, Messages.EnumAttributessection_buttonInherit);
            inheritButton
                    .setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
            inheritButton.addSelectionListener(new SelectionListener() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    try {
                        inheritClicked();
                    } catch (CoreException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent event) {

                }
            });
            updateInheritButtonEnabledState();

            return true;
        }

        private void updateInheritButtonEnabledState() {
            try {
                IEnumType enumType = (IEnumType)getIpsObject();
                boolean superEnumTypeExists = enumType.hasExistingSuperEnumType(enumType.getIpsProject());
                inheritButton.setEnabled(superEnumTypeExists);
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * {@inheritDoc}
         * <p>
         * Disables the "Delete" - Button if the selected <tt>IIpsObjectPart</tt> is an
         * <tt>IEnumLiteralNameAttribute</tt> while the <tt>IEnumType</tt> is needing to use it.
         */
        @Override
        public void selectionChanged(SelectionChangedEvent event) {
            try {
                setCanDelete(true);
                if (getSelectedPart() instanceof IEnumLiteralNameAttribute && enumType.isCapableOfContainingValues()) {
                    setCanDelete(false);
                }
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Opens a dialog enabling the user to inherit <tt>IEnumAttribute</tt>s from the supertype
         * hierarchy in a comfortable way.
         */
        private void inheritClicked() throws CoreException {
            InheritEnumAttributesDialog dialog = new InheritEnumAttributesDialog(enumType, getShell());
            if (dialog.open() == Window.OK) {
                IEnumAttribute[] attributesToOverwrite = dialog.getSelectedAttributes();
                enumType.inheritEnumAttributes(Arrays.asList(attributesToOverwrite));
                refresh();
            }
        }

    }

}
