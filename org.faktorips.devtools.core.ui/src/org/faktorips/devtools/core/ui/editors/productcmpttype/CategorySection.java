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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory.Position;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.LocalizedLabelProvider;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.ControlPropertyBinding;
import org.faktorips.devtools.core.ui.controller.fields.SectionEditField;
import org.faktorips.devtools.core.ui.dialogs.DialogMementoHelper;
import org.faktorips.devtools.core.ui.dnd.IpsObjectPartContainerByteArrayTransfer;
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

    private final IAction deleteAction;

    private final IAction editAction;

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
        deleteAction = new DeleteCategoryAction(contextType, category, categoryCompositionSection);
        editAction = new EditCategoryAction(contextType, category, categoryCompositionSection);

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

        getBindingContext().add(
                new ControlPropertyBinding(getSectionControl(), category, IProductCmptCategory.PROPERTY_NAME,
                        String.class) {

                    @Override
                    public void updateUiIfNotDisposed(String nameOfChangedProperty) {
                        updateSectionTitle();
                    }
                });
        SectionEditField sectionEditField = new SectionEditField(getSectionControl());
        getBindingContext().bindProblemMarker(sectionEditField, category, IProductCmptCategory.PROPERTY_NAME);
        getBindingContext().bindProblemMarker(sectionEditField, category,
                IProductCmptCategory.PROPERTY_DEFAULT_FOR_FORMULA_SIGNATURE_DEFINITIONS);
        getBindingContext().bindProblemMarker(sectionEditField, category,
                IProductCmptCategory.PROPERTY_DEFAULT_FOR_POLICY_CMPT_TYPE_ATTRIBUTES);
        getBindingContext().bindProblemMarker(sectionEditField, category,
                IProductCmptCategory.PROPERTY_DEFAULT_FOR_PRODUCT_CMPT_TYPE_ATTRIBUTES);
        getBindingContext().bindProblemMarker(sectionEditField, category,
                IProductCmptCategory.PROPERTY_DEFAULT_FOR_TABLE_STRUCTURE_USAGES);
        getBindingContext().bindProblemMarker(sectionEditField, category,
                IProductCmptCategory.PROPERTY_DEFAULT_FOR_VALIDATION_RULES);
    }

    private void setLayout(Composite parent) {
        GridLayout layout = new GridLayout(1, true);
        layout.marginWidth = 1;
        layout.marginHeight = 2;
        parent.setLayout(layout);
    }

    @Override
    protected void populateToolBar(IToolBarManager toolBarManager) {
        addMoveActions(toolBarManager);
        toolBarManager.add(new Separator());
        toolBarManager.add(editAction);
        toolBarManager.add(deleteAction);
    }

    private void addMoveActions(IToolBarManager toolBarManager) {
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
        moveUpAction.setEnabled(!contextType.isFirstCategory(category) && contextType.isDefining(category));
        moveDownAction.setEnabled(!contextType.isLastCategory(category) && contextType.isDefining(category));
        moveLeftAction.setEnabled(contextType.isDefining(category));
        moveRightAction.setEnabled(contextType.isDefining(category));

        editAction.setEnabled(contextType.isDefining(category));
        deleteAction.setEnabled(contextType.isDefining(category));
    }

    private static class CategoryComposite extends ViewerButtonComposite {

        private final ContentsChangeListener contentsChangeListener;

        private final IProductCmptCategory category;

        private final IProductCmptType contextType;

        private final CategoryCompositionSection categoryCompositionSection;

        private final Color disabledColor = new Color(getDisplay(), 100, 100, 100);

        private Button moveUpButton;

        private Button moveDownButton;

        private Button changeCategoryButton;

        private PropertyContentProvider contentProvider;

        public CategoryComposite(IProductCmptCategory category, IProductCmptType contextType,
                CategoryCompositionSection categoryCompositionSection, Composite parent, UIToolkit toolkit) {

            super(parent);

            this.category = category;
            this.contextType = contextType;
            this.categoryCompositionSection = categoryCompositionSection;

            initControls(toolkit);

            contentsChangeListener = createContentsChangeListener(contextType);
            addContentsChangeListener();
            addDisposeListener();
        }

        private void addContentsChangeListener() {
            contextType.getIpsModel().addChangeListener(contentsChangeListener);
        }

        private void addDisposeListener() {
            addDisposeListener(new DisposeListener() {
                @Override
                public void widgetDisposed(DisposeEvent e) {
                    contextType.getIpsModel().removeChangeListener(contentsChangeListener);
                    disabledColor.dispose();
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
                        throw new CoreRuntimeException(e);
                    }
                }
            };
        }

        @Override
        protected void refreshThis() {
            updateTreeItemEnabledStates();
        }

        /**
         * Sets the foreground color of all tree items representing properties assigned by
         * supertypes to the disabled color.
         */
        private void updateTreeItemEnabledStates() {
            for (int i = 0; i < contentProvider.properties.size(); i++) {
                if (!isPropertyOfContextType(contentProvider.properties.get(i))) {
                    getTree().getItem(i).setForeground(disabledColor);
                }
            }
        }

        @Override
        protected ContentViewer createViewer(Composite parent, UIToolkit toolkit) {
            TreeViewer viewer = new TreeViewer(toolkit.getFormToolkit().createTree(parent, SWT.NONE));

            setLabelProvider(viewer);
            setContentProvider(viewer);
            addDoubleClickChangeCategoryListener(viewer);
            addDragSupport(viewer);
            addDropSupport(viewer);

            viewer.setInput(category);

            return viewer;
        }

        private void setLabelProvider(TreeViewer viewer) {
            viewer.setLabelProvider(new LocalizedLabelProvider() {
                @Override
                public Image getImage(Object element) {
                    // Returns the default image of the corresponding property value
                    if (element instanceof IProductCmptProperty) {
                        IProductCmptProperty property = (IProductCmptProperty)element;
                        return IpsUIPlugin.getImageHandling().getDefaultImage(
                                property.getProductCmptPropertyType().getValueImplementationClass());
                    }
                    return super.getImage(element);
                }
            });
        }

        private void setContentProvider(TreeViewer viewer) {
            contentProvider = new PropertyContentProvider(contextType, category);
            viewer.setContentProvider(contentProvider);
        }

        private void addDoubleClickChangeCategoryListener(TreeViewer viewer) {
            viewer.getTree().addMouseListener(new MouseAdapter() {
                @Override
                public void mouseDoubleClick(MouseEvent e) {
                    openChangeCategoryDialog();
                }
            });
        }

        private void addDragSupport(final TreeViewer viewer) {
            viewer.addDragSupport(DND.DROP_MOVE, new Transfer[] { ProductCmptPropertyTransfer.getInstance() },
                    new PropertyDragListener(this));
        }

        private void addDropSupport(TreeViewer viewer) {
            viewer.addDropSupport(DND.DROP_MOVE, new Transfer[] { ProductCmptPropertyTransfer.getInstance() },
                    new PropertyDropAdapter(this, viewer));
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
            int[] selectionIndices = getSelectionIndices();
            int[] newSelectionIndices = Arrays.copyOf(selectionIndices, selectionIndices.length);
            try {
                newSelectionIndices = category.moveProductCmptProperties(selectionIndices, up, contextType);
            } catch (CoreException e) {
                // The elements could not be moved so the new selection equals the old selection
                IpsPlugin.log(e);
                newSelectionIndices = Arrays.copyOf(selectionIndices, selectionIndices.length);
            }
            setSelection(newSelectionIndices);
        }

        private int[] getSelectionIndices() {
            TreeItem[] selection = getTree().getSelection();
            int[] selectionIndices = new int[selection.length];
            for (int i = 0; i < selection.length; i++) {
                selectionIndices[i] = getTree().indexOf(selection[i]);
            }
            return selectionIndices;
        }

        private void setSelection(int[] selectionIndices) {
            for (int index : selectionIndices) {
                getTree().select(getTree().getItem(index));
            }
            getTree().setFocus();
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
            if (selectedProperty == null || !isSelectedPropertyOfContextType()) {
                return;
            }

            ChangeCategoryDialog dialog = new ChangeCategoryDialog(contextType, selectedProperty, category, getShell());
            int returnCode = dialog.open();
            if (returnCode == Window.OK) {
                // Set the selection to the selected property in the target category
                IProductCmptCategory selectedCategory = dialog.getSelectedCategory();
                CategorySection targetCategorySection = categoryCompositionSection.getCategorySection(selectedCategory);
                targetCategorySection.getViewerButtonComposite().setSelectedObject(selectedProperty);
            }
        }

        @Override
        protected void updateButtonEnabledStates() {
            moveUpButton.setEnabled(isPropertySelected() && !isFirstElementSelected());
            moveDownButton.setEnabled(isPropertySelected() && !isLastElementSelected());
            changeCategoryButton.setEnabled(isPropertySelected() && isSelectedPropertyOfContextType());
        }

        private boolean isSelectedPropertyOfContextType() {
            return isPropertyOfContextType(getSelectedProperty());
        }

        private boolean isPropertyOfContextType(IProductCmptProperty property) {
            IProductCmptType productCmptType;
            try {
                productCmptType = property.findProductCmptType(property.getIpsProject());
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
            return contextType.equals(productCmptType);
        }

        private IProductCmptProperty getSelectedProperty() {
            return (IProductCmptProperty)getSelectedObject();
        }

        private boolean isPropertySelected() {
            return getTree().getSelectionCount() > 0;
        }

        private Tree getTree() {
            return getTreeViewer().getTree();
        }

        private TreeViewer getTreeViewer() {
            return (TreeViewer)getViewer();
        }

        private static class PropertyContentProvider implements ITreeContentProvider {

            private final List<IProductCmptProperty> properties = new ArrayList<IProductCmptProperty>();

            private final IProductCmptType contextType;

            private final IProductCmptCategory category;

            private PropertyContentProvider(IProductCmptType contextType, IProductCmptCategory category) {
                this.contextType = contextType;
                this.category = category;
            }

            @Override
            public Object[] getElements(Object inputElement) {
                properties.clear();
                try {
                    properties
                            .addAll(category.findProductCmptProperties(contextType, true, contextType.getIpsProject()));
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

            @Override
            public Object[] getChildren(Object parentElement) {
                // Not really a tree, there are never any children
                return new Object[0];
            }

            @Override
            public Object getParent(Object element) {
                // As there are no children, there are no parents as well
                return null;
            }

            @Override
            public boolean hasChildren(Object element) {
                // Not really a tree, there are never any children
                return false;
            }

        }

        private static class PropertyDragListener implements DragSourceListener {

            private final CategoryComposite categoryComposite;

            private PropertyDragListener(CategoryComposite categoryComposite) {
                this.categoryComposite = categoryComposite;
            }

            @Override
            public void dragStart(DragSourceEvent event) {
                event.doit = categoryComposite.isPropertySelected();
                if (event.doit) {
                    PropertyDropAdapter.sourceTree = categoryComposite.getTree();
                }
            }

            @Override
            public void dragSetData(DragSourceEvent event) {
                event.data = new IProductCmptProperty[] { categoryComposite.getSelectedProperty() };
            }

            @Override
            public void dragFinished(DragSourceEvent event) {
                PropertyDropAdapter.sourceTree = null;
            }

        }

        private static class PropertyDropAdapter extends ViewerDropAdapter {

            /**
             * The {@link Tree} widget that started the drag and drop operation.
             * <p>
             * This reference is used by the method {@link #validateDrop(Object, int, TransferData)}
             * to obtain the {@link IProductCmptProperty} the user wants to transfer and to deduce
             * the source {@link IProductCmptCategory}. The field is set by the
             * {@link PropertyDragListener} that starts the drag and drop operation.
             * <p>
             * We know that this is not a "nice" solution but see no other way as
             * {@code getCurrentEvent().data} is null in
             * {@link #validateDrop(Object, int, TransferData)}.
             */
            private static Tree sourceTree;

            private final CategoryComposite categoryComposite;

            private PropertyDropAdapter(CategoryComposite categoryComposite, Viewer viewer) {
                super(viewer);
                this.categoryComposite = categoryComposite;
            }

            @Override
            public boolean validateDrop(Object target, int operation, TransferData transferType) {
                // Cannot drop directly onto items
                if (getCurrentLocation() == LOCATION_ON) {
                    return false;
                }

                // Cannot drop properties from supertypes into another category
                if (isDropSupertypePropertyIntoDifferentCategory()) {
                    return false;
                }

                return ProductCmptPropertyTransfer.getInstance().isSupportedType(transferType);
            }

            private boolean isDropSupertypePropertyIntoDifferentCategory() {
                return !categoryComposite.isPropertyOfContextType(getDroppedPropertyFromSourceTree())
                        && !categoryComposite.getTree().equals(sourceTree);
            }

            private IProductCmptProperty getDroppedPropertyFromSourceTree() {
                return (IProductCmptProperty)sourceTree.getSelection()[0].getData();
            }

            @Override
            public boolean performDrop(Object data) {
                IProductCmptProperty droppedProperty = getDroppedProperty(data);
                try {
                    if (getCurrentLocation() == LOCATION_BEFORE) {
                        getTargetCategory().insertProductCmptProperty(droppedProperty, getTargetProperty(), true,
                                droppedProperty.getIpsProject());
                    } else if (getCurrentLocation() == LOCATION_AFTER) {
                        getTargetCategory().insertProductCmptProperty(droppedProperty, getTargetProperty(), false,
                                droppedProperty.getIpsProject());
                    } else if (getCurrentLocation() == LOCATION_NONE) {
                        getTargetCategory().insertProductCmptProperty(droppedProperty, null, false,
                                droppedProperty.getIpsProject());
                    }
                } catch (CoreException e) {
                    return false;
                }

                return true;
            }

            private IProductCmptProperty getDroppedProperty(Object data) {
                return (IProductCmptProperty)((Object[])data)[0];
            }

            @Override
            protected int determineLocation(DropTargetEvent event) {
                if (!(event.item instanceof Item)) {
                    return LOCATION_NONE;
                }

                Item item = (Item)event.item;
                Rectangle bounds = getBounds(item);
                if (bounds == null) {
                    return LOCATION_NONE;
                }

                /*
                 * When the mouse is on an item, return LOCATION_BEFORE or LOCATION_AFTER instead,
                 * depending on the distance to the respective location.
                 */
                Point coordinates = categoryComposite.getTree().toControl(new Point(event.x, event.y));
                if ((coordinates.y - bounds.y) < bounds.height / 2) {
                    return LOCATION_BEFORE;
                } else if ((bounds.y + bounds.height - coordinates.y) < bounds.height / 2) {
                    return LOCATION_AFTER;
                } else {
                    return LOCATION_ON;
                }
            }

            private IProductCmptProperty getTargetProperty() {
                return (IProductCmptProperty)getCurrentTarget();
            }

            private IProductCmptCategory getTargetCategory() {
                return (IProductCmptCategory)getViewer().getInput();
            }

        }

        private static class ProductCmptPropertyTransfer extends
                IpsObjectPartContainerByteArrayTransfer<IProductCmptProperty> {

            private static final String TYPE_NAME = "ProductCmptProperty"; //$NON-NLS-1$

            private static final int TYPE_ID = registerType(TYPE_NAME);

            private static final ProductCmptPropertyTransfer instance = new ProductCmptPropertyTransfer();

            private ProductCmptPropertyTransfer() {
                super(IProductCmptProperty.class);
            }

            private static ProductCmptPropertyTransfer getInstance() {
                return instance;
            }

            @Override
            protected void writePartContainer(IProductCmptProperty part, DataOutputStream outputStream) {
                writeString(part.getIpsProject().getName(), outputStream);
                writeString(part.getType().getQualifiedName(), outputStream);
                writeString(part.getType().getIpsObjectType().getId(), outputStream);
                writeString(part.getId(), outputStream);
            }

            @Override
            protected IProductCmptProperty readPartContainer(DataInputStream readIn) {
                String projectName = readString(readIn);
                String typeQualifiedName = readString(readIn);
                IpsObjectType typeObjectType = IpsObjectType.getTypeForName(readString(readIn));
                String partId = readString(readIn);

                IIpsProject ipsProject = IpsPlugin.getDefault().getIpsModel().getIpsProject(projectName);
                try {
                    IType type = (IType)ipsProject.findIpsObject(typeObjectType, typeQualifiedName);
                    IProductCmptProperty property = null;
                    for (IIpsElement child : type.getChildren()) {
                        if (!(child instanceof IProductCmptProperty)) {
                            continue;
                        }
                        IProductCmptProperty potentialProperty = (IProductCmptProperty)child;
                        if (partId.equals(potentialProperty.getId())) {
                            property = potentialProperty;
                            break;
                        }
                    }
                    return property;
                } catch (CoreException e) {
                    throw new CoreRuntimeException(e);
                }
            }

            @Override
            protected String[] getTypeNames() {
                return new String[] { TYPE_NAME };
            }

            @Override
            protected int[] getTypeIds() {
                return new int[] { TYPE_ID };
            }

        }

    }

    private static abstract class CategoryAction extends Action {

        private final IProductCmptType productCmptType;

        private final IProductCmptCategory category;

        private final CategoryCompositionSection categoryCompositionSection;

        private CategoryAction(IProductCmptType productCmptType, IProductCmptCategory category,
                CategoryCompositionSection categoryCompositionSection) {

            this.productCmptType = productCmptType;
            this.category = category;
            this.categoryCompositionSection = categoryCompositionSection;
        }

        protected IProductCmptType getProductCmptType() {
            return productCmptType;
        }

        protected IProductCmptCategory getCategory() {
            return category;
        }

        protected CategoryCompositionSection getCategoryCompositionSection() {
            return categoryCompositionSection;
        }

        protected Shell getShell() {
            return categoryCompositionSection.getShell();
        }

    }

    private static abstract class MoveCategoryAction extends CategoryAction {

        private MoveCategoryAction(IProductCmptType productCmptType, IProductCmptCategory category,
                CategoryCompositionSection categoryCompositionSection) {

            super(productCmptType, category, categoryCompositionSection);
        }

        @Override
        public void run() {
            boolean moved = move();
            if (moved) {
                getCategoryCompositionSection().recreateCategorySections(getCategory());
            }
        }

        protected abstract boolean move();

    }

    private static class MoveCategoryUpAction extends MoveCategoryAction {

        private static final String IMAGE_FILENAME = "ArrowUp.gif"; //$NON-NLS-1$

        private MoveCategoryUpAction(IProductCmptType productCmptType, IProductCmptCategory category,
                CategoryCompositionSection categoryCompositionSection) {

            super(productCmptType, category, categoryCompositionSection);

            setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(IMAGE_FILENAME));
            setText(Messages.MoveCategoryUpAction_label);
            setToolTipText(Messages.MoveCategoryUpAction_tooltip);
        }

        @Override
        protected boolean move() {
            return getProductCmptType().moveCategories(Arrays.asList(getCategory()), true);
        }

    }

    private static class MoveCategoryDownAction extends MoveCategoryAction {

        private static final String IMAGE_FILENAME = "ArrowDown.gif"; //$NON-NLS-1$

        private MoveCategoryDownAction(IProductCmptType productCmptType, IProductCmptCategory category,
                CategoryCompositionSection categoryCompositionSection) {

            super(productCmptType, category, categoryCompositionSection);

            setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(IMAGE_FILENAME));
            setText(Messages.MoveCategoryDownAction_label);
            setToolTipText(Messages.MoveCategoryDownAction_tooltip);
        }

        @Override
        protected boolean move() {
            return getProductCmptType().moveCategories(Arrays.asList(getCategory()), false);
        }

    }

    private static class MoveCategoryLeftAction extends MoveCategoryAction {

        private static final String IMAGE_FILENAME = "ArrowLeft.gif"; //$NON-NLS-1$

        private MoveCategoryLeftAction(IProductCmptType productCmptType, IProductCmptCategory category,
                CategoryCompositionSection categoryCompositionSection) {

            super(productCmptType, category, categoryCompositionSection);

            setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(IMAGE_FILENAME));
            setText(Messages.MoveCategoryLeftAction_label);
            setToolTipText(Messages.MoveCategoryLeftAction_tooltip);
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

        private static final String IMAGE_FILENAME = "ArrowRight.gif"; //$NON-NLS-1$

        private MoveCategoryRightAction(IProductCmptType productCmptType, IProductCmptCategory category,
                CategoryCompositionSection categoryCompositionSection) {

            super(productCmptType, category, categoryCompositionSection);

            setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(IMAGE_FILENAME));
            setText(Messages.MoveCategoryRightAction_label);
            setToolTipText(Messages.MoveCategoryRightAction_tooltip);
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

    private static class DeleteCategoryAction extends CategoryAction {

        private static final String IMAGE_FILENAME = "Delete.gif"; //$NON-NLS-1$

        private DeleteCategoryAction(IProductCmptType productCmptType, IProductCmptCategory category,
                CategoryCompositionSection categoryCompositionSection) {

            super(productCmptType, category, categoryCompositionSection);

            setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(IMAGE_FILENAME));
            setText(Messages.DeleteCategoryAction_label);
            setToolTipText(Messages.DeleteCategoryAction_tooltip);
        }

        @Override
        public void run() {
            boolean oldDeleted = getCategory().isDeleted();
            getCategory().delete();
            if (oldDeleted != getCategory().isDeleted()) {
                getCategoryCompositionSection().deleteCategorySection(getCategory());
            }
        }

    }

    private static class EditCategoryAction extends CategoryAction {

        private static final String IMAGE_FILENAME = "Edit.gif"; //$NON-NLS-1$

        private EditCategoryAction(IProductCmptType productCmptType, IProductCmptCategory category,
                CategoryCompositionSection categoryCompositionSection) {

            super(productCmptType, category, categoryCompositionSection);

            setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(IMAGE_FILENAME));
            setText(Messages.EditCategoryAction_label);
            setToolTipText(Messages.EditCategoryAction_tooltip);
        }

        @Override
        public void run() {
            DialogMementoHelper dialogHelper = new DialogMementoHelper() {
                @Override
                protected Dialog createDialog() {
                    return new CategoryEditDialog(getCategory(), getShell());
                }
            };

            Position oldPosition = getCategory().getPosition();
            int returnCode = dialogHelper.openDialogWithMemento(getCategory());
            // Recreate the category sections if the category's position has changed (= move)
            if (returnCode == Window.OK && !oldPosition.equals(getCategory().getPosition())) {
                getCategoryCompositionSection().recreateCategorySections(getCategory());
            }
        }

    }

}
