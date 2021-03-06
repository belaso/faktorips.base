/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.dialogs;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.util.QNameUtil;

/**
 * Dialog for changing the sort order of IIpsPackageFragments.
 * 
 * @author Markus Blum
 */
public class IpsPackageSortDefDialog extends TrayDialog {

    private static final String SETTINGS_SECTION_SIZE = "size"; //$NON-NLS-1$
    private static final String SETTINGS_SIZE_X = "x"; //$NON-NLS-1$
    private static final String SETTINGS_SIZE_Y = "y"; //$NON-NLS-1$
    private static final int SETTINGS_DEFAULT_HEIGTH = 480;
    private static final int SETTINGS_DEFAULT_WIDTH = 640;

    private String title;
    private IIpsProject project;
    private IpsProjectSortOrdersPM sortOrderPM;

    private UIToolkit toolkit;
    private TreeViewer treeViewer;
    private Button up;
    private Button down;
    private Button restore;
    private Composite container;

    private DialogSettings settings;
    private static String settingsFilename;

    /**
     * New instance.
     * 
     * @param parentShell The active shell.
     * @param title Title of the dialog.
     * @param project The selected IIpsProject.
     */
    public IpsPackageSortDefDialog(Shell parentShell, String title, IIpsProject project) {
        super(parentShell);

        this.title = title;
        this.project = project;
        sortOrderPM = new IpsProjectSortOrdersPM(project);

        toolkit = new UIToolkit(null);

        int shellStyle = getShellStyle();
        setShellStyle(shellStyle | SWT.RESIZE | SWT.MAX);

        loadDialogSettings();
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        getShell().setText(title);

        container = (Composite)super.createDialogArea(parent);
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);

        // restore size
        int width = Math.max(settings.getInt(SETTINGS_SIZE_X), layoutData.heightHint);
        int height = Math.max(settings.getInt(SETTINGS_SIZE_Y), layoutData.widthHint);
        layoutData.widthHint = Math.max(width, layoutData.minimumWidth);
        layoutData.heightHint = Math.max(height, layoutData.minimumHeight);

        container.setLayoutData(layoutData);

        GridLayout layout = new GridLayout();
        container.setLayout(layout);

        createHeadline(container);
        createSortArea(container);
        createRestoreButton(container);

        Dialog.applyDialogFont(parent);

        return container;
    }

    /**
     * Create a headline with the chosen IpsProject name.
     */
    private void createHeadline(Composite parent) {

        Composite headline = toolkit.createComposite(parent);

        headline.setLayoutData(new GridData());

        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        headline.setLayout(layout);

        toolkit.createLabel(headline, Messages.IpsPackageSortDefDialog_headlineText);
        toolkit.createLabel(headline, project.getName());
    }

    private void createRestoreButton(Composite parent) {
        Composite restoreComposite = toolkit.createComposite(parent);

        GridLayout layout = new GridLayout();
        restoreComposite.setLayout(layout);

        restore = toolkit.createButton(restoreComposite, Messages.IpsPackageSortDefDialog_restore);
        restore.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                restorePressed();
                treeViewer.refresh();
            }
        });

        restoreComposite.setLayoutData(new GridData(SWT.TRAIL, SWT.DEFAULT, false, false));
    }

    /**
     * Create component for shifting IpsPackageFragments
     */
    private void createSortArea(Composite parent) {
        Composite sortComposite = toolkit.createComposite(parent);

        sortComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        sortComposite.setLayout(layout);

        createTreeViewer(sortComposite);
        createUpDownButtons(sortComposite);
        udpateButtonEnablement();
        treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                udpateButtonEnablement();
            }
        });
    }

    /**
     * Enables or disables the buttons {@link #up} and {@link #down} according to the
     * {@link #treeViewer}'s current selection.
     * <p>
     * Both buttons are disabled if no element is selected or if the element represents a source
     * folder or packageFragmentRoot respectively.
     * <p>
     * For movable elements (PackageFragments) the button {@link #up} is disabled if the fragment is
     * the first in its parent and cannot be moved further upwards, #down analogous.
     */
    private void udpateButtonEnablement() {
        Object selectedElement = getFirstSelectedElementFromTreeViewer();
        if (selectedElement != null && selectedElement instanceof IIpsPackageFragment) {
            IIpsPackageFragment fragment = (IIpsPackageFragment)selectedElement;
            if (fragment.isDefaultPackage()) {
                /*
                 * These are the sourceFolders or pckgFragmentRoots displayed in the dialog. Disable
                 * buttons, as reordering is not possible.
                 */
                up.setEnabled(false);
                down.setEnabled(false);
            } else {
                up.setEnabled(!sortOrderPM.isFirstInParent(fragment));
                down.setEnabled(!sortOrderPM.isLastInParent(fragment));
            }
        } else {
            // nothing or no movable element selected, disable buttons
            up.setEnabled(false);
            down.setEnabled(false);
        }
    }

    /**
     * Create the treeviewer.
     */
    private void createTreeViewer(Composite sortComposite) {
        treeViewer = new TreeViewer(sortComposite);
        treeViewer.setLabelProvider(new IpsPackageSortDefLabelProvider());
        treeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        treeViewer.setContentProvider(sortOrderPM);
        treeViewer.setInput(sortOrderPM);
        // expand roots
        treeViewer.expandToLevel(2);
    }

    /**
     * Create composite with up/buttons.
     */
    private void createUpDownButtons(Composite parent) {
        Composite upDownComposite = toolkit.createComposite(parent);

        upDownComposite.setLayoutData(new GridData());

        GridLayout layout = new GridLayout();
        upDownComposite.setLayout(layout);

        up = toolkit.createButton(upDownComposite, Messages.IpsPackageSortDefDialog_up);
        up.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                upPressed();
            }
        });
        setButtonLayoutData(up);

        down = toolkit.createButton(upDownComposite, Messages.IpsPackageSortDefDialog_down);
        down.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                downPressed();
            }
        });
        setButtonLayoutData(down);
    }

    /**
     * Handle Button <code>restore</code>.
     */
    protected void restorePressed() {
        sortOrderPM.restore();
        treeViewer.refresh();
    }

    /**
     * Handle Button <code>down</code>.
     */
    protected void downPressed() {
        Object element = getFirstSelectedElementFromTreeViewer();

        if (element instanceof IIpsPackageFragment) {
            IIpsPackageFragment fragment = (IIpsPackageFragment)element;
            sortOrderPM.moveOneDown(fragment);
            treeViewer.refresh(false);
            udpateButtonEnablement();
        }
    }

    /**
     * Handle Button <code>up</code>.
     */
    protected void upPressed() {
        Object element = getFirstSelectedElementFromTreeViewer();

        if (element instanceof IIpsPackageFragment) {
            IIpsPackageFragment fragment = (IIpsPackageFragment)element;
            sortOrderPM.moveOneUp(fragment);
            treeViewer.refresh(false);
            udpateButtonEnablement();
        }
    }

    protected Object getFirstSelectedElementFromTreeViewer() {
        return ((IStructuredSelection)treeViewer.getSelection()).getFirstElement();
    }

    @Override
    protected void okPressed() {
        // write changes to filesystem.
        try {
            sortOrderPM.saveSortDefDelta();
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }

        super.okPressed();
    }

    @Override
    public boolean close() {
        saveDialogSettings();

        if (toolkit != null) {
            toolkit.dispose();
        }

        return super.close();
    }

    /**
     * save dialog settings to file.
     */
    private void saveDialogSettings() {
        Point size = container.getSize();
        settings.put(SETTINGS_SIZE_X, size.x);
        settings.put(SETTINGS_SIZE_Y, size.y);

        try {
            settings.save(settingsFilename);
        } catch (IOException e) {
            // cant save - use defaults the next time
            IpsPlugin.log(e);
        }
    }

    /**
     * load dialog settings from file.
     */
    private void loadDialogSettings() {
        IPath path = IpsPlugin.getDefault().getStateLocation();
        settingsFilename = path.append("sortDefDialog.settings").toOSString(); //$NON-NLS-1$

        settings = new DialogSettings(SETTINGS_SECTION_SIZE);
        // set default size if no settings exists
        settings.put(SETTINGS_SIZE_X, SETTINGS_DEFAULT_WIDTH);
        settings.put(SETTINGS_SIZE_Y, SETTINGS_DEFAULT_HEIGTH);

        try {
            settings.load(settingsFilename);
        } catch (IOException e) {
            // cant read the settings, use defaults.
            // do not log the error - could be the first time we read the settings.
            /* IpsPlugin.log(e); */
        }
    }

    /**
     * New LabelProvider for the TreeViewer <code>treeViewer</code>.
     * 
     * @author Markus Blum
     */
    private class IpsPackageSortDefLabelProvider extends LabelProvider {

        @Override
        public Image getImage(Object element) {
            IIpsPackageFragment fragment = (IIpsPackageFragment)element;

            Image image;
            if (fragment.isDefaultPackage()) {
                image = IpsUIPlugin.getImageHandling().getImage(fragment.getRoot());
            } else {
                image = IpsUIPlugin.getImageHandling().getImage(fragment);
            }

            return image;
        }

        @Override
        public String getText(Object element) {
            IIpsPackageFragment fragment = (IIpsPackageFragment)element;
            String name;

            if (fragment.isDefaultPackage()) {
                name = fragment.getRoot().getName();
            } else {
                name = fragment.getName();
            }

            return QNameUtil.getUnqualifiedName(name);
        }
    }
}
