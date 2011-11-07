/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory.Position;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.dialogs.DialogHelper;
import org.faktorips.devtools.core.ui.editors.ViewerButtonComposite;
import org.faktorips.devtools.core.ui.editors.productcmpttype.CategoryPage.CategoryCompositionSection;
import org.faktorips.devtools.core.ui.forms.IpsSection;

/**
 * A section allowing the user to edit an {@link IProductCmptCategory}.
 * <p>
 * By means of this section, the user is able to change the order of the
 * {@link IProductCmptProperty}s assigned to the {@link IProductCmptCategory} that is being edited.
 * Furthermore, the {@link IProductCmptCategory} itself can be moved up, down, left or right.
 * <p>
 * A separate dialog enables the user to change the properties of an {@link IProductCmptCategory},
 * for example it's name and whether the {@link IProductCmptCategory} is marked as default for a
 * specific kind {@link IProductCmptProperty}.
 * <p>
 * Yet another dialog allows to change the {@link IProductCmptCategory} of an
 * {@link IProductCmptProperty}.
 * 
 * @author Alexander Weickmann
 */
public class CategorySection extends IpsSection {

    private final IProductCmptCategory category;

    private final IProductCmptType contextType;

    private final CategoryCompositionSection categoryCompositionSection;

    private final IAction moveUpAction;

    private final IAction moveDownAction;

    private final IAction moveLeftAction;

    private final IAction moveRightAction;

    private ViewerButtonComposite viewerButtonComposite;

    public CategorySection(IProductCmptCategory category, IProductCmptType contextType,
            CategoryCompositionSection categoryCompositionSection, Composite parent, UIToolkit toolkit) {

        super(parent, Section.TITLE_BAR, GridData.FILL_BOTH, toolkit);

        this.category = category;
        this.contextType = contextType;
        this.categoryCompositionSection = categoryCompositionSection;

        moveUpAction = new MoveCategoryUpAction(contextType, category, categoryCompositionSection);
        moveDownAction = new MoveCategoryDownAction(contextType, category, categoryCompositionSection);
        moveLeftAction = new MoveCategoryLeftAction(contextType, category, categoryCompositionSection);
        moveRightAction = new MoveCategoryRightAction(contextType, category, categoryCompositionSection);

        initControls();
    }

    @Override
    protected String getSectionTitle() {
        return IpsPlugin.getMultiLanguageSupport().getLocalizedLabel(category);
    }

    @Override
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        setLayout(client);
        viewerButtonComposite = new CategoryComposite(category, contextType, categoryCompositionSection, client,
                toolkit);
    }

    private void setLayout(Composite parent) {
        GridLayout layout = new GridLayout(1, true);
        layout.marginWidth = 1;
        layout.marginHeight = 2;
        parent.setLayout(layout);
    }

    @Override
    protected void populateToolBar(IToolBarManager toolBarManager) {
        if (category.isAtRightPosition()) {
            toolBarManager.add(moveLeftAction);
        }

        toolBarManager.add(moveUpAction);
        toolBarManager.add(moveDownAction);

        if (category.isAtLeftPosition()) {
            toolBarManager.add(moveRightAction);
        }
    }

    @Override
    protected void performRefresh() {
        viewerButtonComposite.refresh();
        updateToolBarEnabledStates();
    }

    private ViewerButtonComposite getViewerButtonComposite() {
        return viewerButtonComposite;
    }

    private void updateToolBarEnabledStates() {
        moveUpAction.setEnabled(contextType.canMoveCategoryUp(category));
        moveDownAction.setEnabled(contextType.canMoveCategoryDown(category));
        moveLeftAction.setEnabled(contextType.canMoveCategoryLeft(category));
        moveRightAction.setEnabled(contextType.canMoveCategoryRight(category));
    }

    private static class CategoryComposite extends ViewerButtonComposite {

        private final ContentsChangeListener contentsChangeListener;

        private final IProductCmptCategory category;

        private final IProductCmptType contextType;

        private final CategoryCompositionSection categoryCompositionSection;

        private Button moveUpButton;

        private Button moveDownButton;

        private Button changeCategoryButton;

        public CategoryComposite(IProductCmptCategory category, IProductCmptType contextType,
                CategoryCompositionSection categoryCompositionSection, Composite parent, UIToolkit toolkit) {

            super(parent);

            this.category = category;
            this.contextType = contextType;
            this.categoryCompositionSection = categoryCompositionSection;

            initControls(toolkit);

            contentsChangeListener = createContentsChangeListener(contextType);
            addContentsChangeListener(contextType.getIpsModel());
        }

        private void addContentsChangeListener(final IIpsModel ipsModel) {
            ipsModel.addChangeListener(contentsChangeListener);
            addDisposeListener(new DisposeListener() {
                @Override
                public void widgetDisposed(DisposeEvent e) {
                    ipsModel.removeChangeListener(contentsChangeListener);
                }
            });
        }

        /**
         * Creates a {@link ContentsChangeListener} that refreshes this section if the
         * {@link IPolicyCmptType} configured by the context {@link IProductCmptType} has changed.
         * <p>
         * The reason for this listener is the whole-content-changed-event that is fired when
         * changes done to an {@link IIpsSrcFile} are discarded. The section needs to react to this
         * event accordingly by updating the list of it's {@link IProductCmptProperty}s.
         */
        private ContentsChangeListener createContentsChangeListener(final IProductCmptType contextType) {
            return new ContentsChangeListener() {
                @Override
                public void contentsChanged(ContentChangeEvent event) {
                    try {
                        if (event.isAffected(contextType.findPolicyCmptType(contextType.getIpsProject()))) {
                            refresh();
                        }
                    } catch (CoreException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        }

        @Override
        protected ContentViewer createViewer(Composite parent, UIToolkit toolkit) {
            TableViewer tableViewer = new TableViewer(toolkit.createTable(parent, SWT.NONE));

            setLabelProvider(tableViewer);
            setContentProvider(tableViewer);
            addDoubleClickChangeCategoryListenerToTableViewer(tableViewer);

            tableViewer.setInput(category);

            return tableViewer;
        }

        private void setLabelProvider(TableViewer tableViewer) {
            tableViewer.setLabelProvider(new DefaultLabelProvider());
        }

        private void setContentProvider(ContentViewer tableViewer) {
            tableViewer.setContentProvider(new IStructuredContentProvider() {
                @Override
                public Object[] getElements(Object inputElement) {
                    List<IProductCmptProperty> properties = new ArrayList<IProductCmptProperty>();
                    try {
                        properties.addAll(category.findProductCmptProperties(contextType, false,
                                contextType.getIpsProject()));
                    } catch (CoreException e) {
                        // Recover by not displaying any properties
                        IpsPlugin.log(e);
                    }
                    return properties.toArray();
                }

                @Override
                public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                    // Nothing to do
                }

                @Override
                public void dispose() {
                    // Nothing to do
                }
            });
        }

        private void addDoubleClickChangeCategoryListenerToTableViewer(TableViewer tableViewer) {
            tableViewer.getTable().addMouseListener(new MouseAdapter() {
                @Override
                public void mouseDoubleClick(MouseEvent e) {
                    openChangeCategoryDialog();
                }
            });
        }

        @Override
        protected boolean createButtons(Composite buttonComposite, UIToolkit toolkit) {
            createMoveUpButton(buttonComposite, toolkit);
            createMoveDownButton(buttonComposite, toolkit);
            createChangeCategoryButton(buttonComposite, toolkit);
            return true;
        }

        private void createMoveUpButton(Composite buttonComposite, UIToolkit toolkit) {
            moveUpButton = toolkit.createButton(buttonComposite, Messages.CategorySection_buttonUp);
            moveUpButton
                    .setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
            moveUpButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    moveParts(true);
                }
            });
        }

        private void createMoveDownButton(Composite buttonComposite, UIToolkit toolkit) {
            moveDownButton = toolkit.createButton(buttonComposite, Messages.CategorySection_buttonDown);
            moveDownButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
                    | GridData.VERTICAL_ALIGN_BEGINNING));
            moveDownButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    moveParts(false);
                }
            });
        }

        private void moveParts(boolean up) {
            int[] selection = getTable().getSelectionIndices();
            int[] newSelection = Arrays.copyOf(selection, selection.length);
            try {
                newSelection = category.moveProductCmptProperties(selection, up, contextType);
            } catch (CoreException e) {
                // The elements could not be moved so the new selection equals the old selection
                IpsPlugin.log(e);
                newSelection = Arrays.copyOf(selection, selection.length);
            }

            getTable().setSelection(newSelection);
            getTable().setFocus();

            refresh();
        }

        private void createChangeCategoryButton(Composite buttonComposite, UIToolkit toolkit) {
            changeCategoryButton = toolkit.createButton(buttonComposite, Messages.CategorySection_buttonChangeCategory);
            changeCategoryButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
                    | GridData.VERTICAL_ALIGN_BEGINNING));
            changeCategoryButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    openChangeCategoryDialog();
                }
            });
        }

        private void openChangeCategoryDialog() {
            IProductCmptProperty selectedProperty = getSelectedProperty();
            if (selectedProperty == null) {
                return;
            }

            ChangeCategoryDialog dialog = new ChangeCategoryDialog(contextType, selectedProperty, category, getShell());
            DialogHelper dialogHelper = new DialogHelper();

            int returnCode = dialogHelper.openDialogWithMemento(dialog, selectedProperty);
            if (returnCode == Window.OK) {
                // Set the selection to the selected property in the target category
                IProductCmptCategory selectedCategory = dialog.getSelectedCategory();
                CategorySection targetCategorySection = categoryCompositionSection.getCategorySection(selectedCategory);
                targetCategorySection.getViewerButtonComposite().setSelectedObject(selectedProperty);
            }
        }

        private IProductCmptProperty getSelectedProperty() {
            return (IProductCmptProperty)getSelectedObject();
        }

        private Table getTable() {
            return getTableViewer().getTable();
        }

        private TableViewer getTableViewer() {
            return (TableViewer)getViewer();
        }

        @Override
        protected void updateButtonEnabledStates() {
            moveUpButton.setEnabled(isPropertySelected() && !isFirstElementSelected());
            moveDownButton.setEnabled(isPropertySelected() && !isLastElementSelected());
            changeCategoryButton.setEnabled(isPropertySelected());
        }

        private boolean isPropertySelected() {
            return !getViewer().getSelection().isEmpty();
        }

    }

    private static abstract class MoveCategoryAction extends Action {

        private final IProductCmptType productCmptType;

        private final IProductCmptCategory category;

        private final CategoryCompositionSection categoryCompositionSection;

        private MoveCategoryAction(IProductCmptType productCmptType, IProductCmptCategory category,
                CategoryCompositionSection categoryCompositionSection) {

            this.productCmptType = productCmptType;
            this.category = category;
            this.categoryCompositionSection = categoryCompositionSection;

            setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(getImageFilename()));
            setText(getLabel());
            setToolTipText(getToolTip());
        }

        protected abstract String getImageFilename();

        protected abstract String getLabel();

        protected abstract String getToolTip();

        @Override
        public void run() {
            boolean moved = move();
            if (moved) {
                categoryCompositionSection.recreateCategorySections();
                categoryCompositionSection.relayout();
                categoryCompositionSection.getCategorySection(getCategory()).setFocus();
            }
        }

        protected abstract boolean move();

        protected IProductCmptType getProductCmptType() {
            return productCmptType;
        }

        protected IProductCmptCategory getCategory() {
            return category;
        }

    }

    private static class MoveCategoryUpAction extends MoveCategoryAction {

        private MoveCategoryUpAction(IProductCmptType productCmptType, IProductCmptCategory category,
                CategoryCompositionSection categoryCompositionSection) {

            super(productCmptType, category, categoryCompositionSection);
        }

        @Override
        protected String getImageFilename() {
            return "ArrowUp.gif"; //$NON-NLS-1$
        }

        @Override
        protected String getLabel() {
            return Messages.MoveCategoryUpAction_label;
        }

        @Override
        protected String getToolTip() {
            return Messages.MoveCategoryUpAction_tooltip;
        }

        @Override
        protected boolean move() {
            return getProductCmptType().moveCategories(Arrays.asList(getCategory()), true);
        }

    }

    private static class MoveCategoryDownAction extends MoveCategoryAction {

        private MoveCategoryDownAction(IProductCmptType productCmptType, IProductCmptCategory category,
                CategoryCompositionSection categoryCompositionSection) {

            super(productCmptType, category, categoryCompositionSection);
        }

        @Override
        protected String getImageFilename() {
            return "ArrowDown.gif"; //$NON-NLS-1$
        }

        @Override
        protected String getLabel() {
            return Messages.MoveCategoryDownAction_label;
        }

        @Override
        protected String getToolTip() {
            return Messages.MoveCategoryDownAction_tooltip;
        }

        @Override
        protected boolean move() {
            return getProductCmptType().moveCategories(Arrays.asList(getCategory()), false);
        }

    }

    private static class MoveCategoryLeftAction extends MoveCategoryAction {

        private MoveCategoryLeftAction(IProductCmptType productCmptType, IProductCmptCategory category,
                CategoryCompositionSection categoryCompositionSection) {

            super(productCmptType, category, categoryCompositionSection);
        }

        @Override
        protected String getImageFilename() {
            return "ArrowLeft.gif"; //$NON-NLS-1$
        }

        @Override
        protected String getLabel() {
            return Messages.MoveCategoryLeftAction_label;
        }

        @Override
        protected String getToolTip() {
            return Messages.MoveCategoryLeftAction_tooltip;
        }

        @Override
        protected boolean move() {
            if (!getCategory().isAtLeftPosition()) {
                getCategory().setPosition(Position.LEFT);
                return true;
            }
            return false;
        }

    }

    private static class MoveCategoryRightAction extends MoveCategoryAction {

        private MoveCategoryRightAction(IProductCmptType productCmptType, IProductCmptCategory category,
                CategoryCompositionSection categoryCompositionSection) {

            super(productCmptType, category, categoryCompositionSection);
        }

        @Override
        protected String getImageFilename() {
            return "ArrowRight.gif"; //$NON-NLS-1$
        }

        @Override
        protected String getLabel() {
            return Messages.MoveCategoryRightAction_label;
        }

        @Override
        protected String getToolTip() {
            return Messages.MoveCategoryRightAction_tooltip;
        }

        @Override
        protected boolean move() {
            if (!getCategory().isAtRightPosition()) {
                getCategory().setPosition(Position.RIGHT);
                return true;
            }
            return false;
        }

    }

}
