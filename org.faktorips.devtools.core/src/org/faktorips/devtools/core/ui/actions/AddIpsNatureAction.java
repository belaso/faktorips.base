/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) dürfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1
 * (vor Gründung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation
 * 
 **************************************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionDelegate;
import org.faktorips.devtools.core.FaktorIpsClasspathVariableInitializer;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.IpsObjectPath;
import org.faktorips.devtools.core.internal.model.product.DateBasedProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsProjectProperties;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.Radiobutton;
import org.faktorips.devtools.core.ui.controls.RadiobuttonGroup;

/**
 * An action that adds the ips nature to a project.
 * 
 * @author Jan Ortmann, Daniel Hohenberger
 */
public class AddIpsNatureAction extends ActionDelegate {

    private IStructuredSelection selection = StructuredSelection.EMPTY;

    private String sourceFolderName = Messages.AddIpsNatureAction_defaultSourceFolderName;
    private String basePackageName = Messages.AddIpsNatureAction_basePackage_default;
    private String runtimeIdPrefix = Messages.AddIpsNatureAction_defaultRuntimeIdPrefix;
    private boolean isModelProject = true;
    private boolean isProductDefinitionProject = false;

    /**
     * {@inheritDoc}
     */
    public void selectionChanged(IAction action, ISelection newSelection) {
        if (newSelection instanceof IStructuredSelection) {
            selection = (IStructuredSelection)newSelection;
        } else {
            selection = StructuredSelection.EMPTY;
        }
    }

    private IJavaProject getJavaProject() {
        if (selection.size() != 1) {
            return null;
        }
        if (selection.getFirstElement() instanceof IJavaProject) {
            return (IJavaProject)selection.getFirstElement();
        }
        return null;
    }

    public void runWithEvent(IAction action, Event event) {
        IJavaProject javaProject = getJavaProject();
        if (javaProject == null) {
            IpsStatus status = new IpsStatus(IpsStatus.WARNING, 0, NLS.bind(Messages.AddIpsNatureAction_noJavaProject,
                    selection), null);
            ErrorDialog.openError(getShell(), Messages.AddIpsNatureAction_errorTitle, null, status);
            return;
        }
        IProjectDescription description;
        try {
            description = javaProject.getProject().getDescription();
            String[] natures = description.getNatureIds();
            for (int i = 0; i < natures.length; i++) {
                if (natures[i].equals(IIpsProject.NATURE_ID)) {
                    MessageDialog.openInformation(getShell(), Messages.AddIpsNatureAction_titleAddFaktorIpsNature,
                            Messages.AddIpsNatureAction_msgIPSNatureAlreadySet);
                    return;
                }
            }
        } catch (CoreException e1) {
            IpsPlugin.log(e1);
            return;
        }
        try {
            AddIpsNatureDialog dialog = new AddIpsNatureDialog(getShell());
            if (dialog.open() == Window.CANCEL) {
                return;
            }
            IFolder javaSrcFolder = javaProject.getProject().getFolder("src"); //$NON-NLS-1$
            IPackageFragmentRoot[] roots = javaProject.getPackageFragmentRoots();
            for (int i = 0; i < roots.length; i++) {
                if (roots[i].getKind() == IPackageFragmentRoot.K_SOURCE) {
                    if (roots[i].getCorrespondingResource() instanceof IProject) {
                        IpsStatus status = new IpsStatus(Messages.AddIpsNatureAction_msgSourceInProjectImpossible);
                        ErrorDialog.openError(getShell(), Messages.AddIpsNatureAction_titleAddFaktorIpsNature, null,
                                status);
                        return;
                    }
                    javaSrcFolder = (IFolder)roots[i].getCorrespondingResource();
                    break;
                }
            }
            addIpsRuntimeLibraries(javaProject);
            IIpsProject ipsProject = IpsPlugin.getDefault().getIpsModel().createIpsProject(javaProject);
            IIpsProjectProperties props = ipsProject.getProperties();
            props.setRuntimeIdPrefix(runtimeIdPrefix);
            props.setProductDefinitionProject(isProductDefinitionProject);
            props.setModelProject(isModelProject);
            props.setPredefinedDatatypesUsed(IpsPlugin.getDefault().getIpsModel().getPredefinedValueDatatypes());
            DateBasedProductCmptNamingStrategy namingStrategy = new DateBasedProductCmptNamingStrategy(
                    " ", "yyyy-MM", true); //$NON-NLS-1$ //$NON-NLS-2$
            props.setProductCmptNamingStrategy(namingStrategy);
            ipsProject.setProperties(props);
            IFolder ipsModelFolder = ipsProject.getProject().getFolder(sourceFolderName);
            if (!ipsModelFolder.exists()) {
                ipsModelFolder.create(true, true, null);
            }
            IpsObjectPath path = new IpsObjectPath();
            path.setOutputDefinedPerSrcFolder(false);
            path.setBasePackageNameForGeneratedJavaClasses(basePackageName);
            path.setOutputFolderForGeneratedJavaFiles(javaSrcFolder);
            path.setBasePackageNameForExtensionJavaClasses(basePackageName);
            path.newSourceFolderEntry(ipsModelFolder);
            ipsProject.setIpsObjectPath(path);

        } catch (CoreException e) {
            IpsStatus status = new IpsStatus(Messages.AddIpsNatureAction_msgErrorCreatingIPSProject + javaProject, e);
            ErrorDialog.openError(getShell(), Messages.AddIpsNatureAction_titleAddFaktorIpsNature, null, status);
            IpsPlugin.log(e);
        }
    }

    private void addIpsRuntimeLibraries(IJavaProject javaProject) throws JavaModelException {
        IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
        int numOfJars = FaktorIpsClasspathVariableInitializer.IPS_VARIABLES_BIN.length;
        IClasspathEntry[] entries = new IClasspathEntry[oldEntries.length + numOfJars];
        System.arraycopy(oldEntries, 0, entries, 0, oldEntries.length);
        for (int i = 0; i < numOfJars; i++) {
            Path jarPath = new Path(FaktorIpsClasspathVariableInitializer.IPS_VARIABLES_BIN[i]);
            Path srcZipPath = null;
            if (StringUtils.isNotEmpty(FaktorIpsClasspathVariableInitializer.IPS_VARIABLES_SRC[i])) {
                srcZipPath = new Path(FaktorIpsClasspathVariableInitializer.IPS_VARIABLES_SRC[i]);
            }
            entries[oldEntries.length + i] = JavaCore.newVariableEntry(jarPath, srcZipPath, null);
        }
        javaProject.setRawClasspath(entries, null);
    }

    /**
     * Returns the active shell.
     */
    protected Shell getShell() {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
    }

    private class AddIpsNatureDialog extends TitleAreaDialog {
        private Text errorMessageText;
        private String errorMessage;

        private Text sourceFolderText;
        private Text basePackageText;
        private Text runtimeIdText;

        private Radiobutton modelProjectButton;
        private Radiobutton productDefinitionProjectButton;
        private Radiobutton fullProjectButton;

        /**
         * Image for title area
         */
        private Image dlgTitleImage = null;
        private Button okButton;

        /**
         * The current validation status. Its value can be one of the following:
         * <ul>
         * <li><code>IMessageProvider.NONE</code> (default);</li>
         * <li><code>IMessageProvider.WARNING</code>;</li>
         * <li><code>IMessageProvider.ERROR</code>;</li>
         * </ul>
         * Used when validating the user input.
         */
        private int validationStatus;

        public AddIpsNatureDialog(Shell parentShell) {
            super(parentShell);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
         */
        protected void configureShell(Shell shell) {
            super.configureShell(shell);
            shell.setText(Messages.AddIpsNatureAction_dialogTitle);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
         */
        protected void createButtonsForButtonBar(Composite parent) {
            // create OK and Cancel buttons by default
            okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
            createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
            sourceFolderText.setFocus();
        }

        /*
         * (non-Javadoc) Method declared on Dialog.
         */
        protected Control createDialogArea(Composite parent) {
            Composite composite0 = (Composite)super.createDialogArea(parent);
            UIToolkit kit = new UIToolkit(null);
            Group composite = kit.createGroup(composite0, SWT.SHADOW_NONE, null);

            RadiobuttonGroup radiobuttonGroup = kit.createRadiobuttonGroup(composite, SWT.SHADOW_IN,
                    Messages.AddIpsNatureAction_ProjectType);

            modelProjectButton = radiobuttonGroup.addRadiobutton(Messages.AddIpsNatureAction_modelProject);
            modelProjectButton.setChecked(isModelProject && !isProductDefinitionProject);

            productDefinitionProjectButton = radiobuttonGroup
                    .addRadiobutton(Messages.AddIpsNatureAction_productDefinitionProject);
            productDefinitionProjectButton.setChecked(isProductDefinitionProject && !isModelProject);

            fullProjectButton = radiobuttonGroup.addRadiobutton(Messages.AddIpsNatureAction_fullProject);
            fullProjectButton.setChecked(isModelProject && isProductDefinitionProject);

            kit.createVerticalSpacer(composite, 5);
            Composite textComposite = kit.createLabelEditColumnComposite(composite);

            kit.createLabel(textComposite, Messages.AddIpsNatureAction_sourceFolderName, false);
            sourceFolderText = kit.createText(textComposite, SWT.BORDER);
            sourceFolderText.setText(sourceFolderName);
            sourceFolderText.addModifyListener(new ModifyListener() {
                public void modifyText(ModifyEvent event) {
                    sourceFolderModified();
                }
            });
            kit.createVerticalSpacer(composite, 5);

            kit.createLabel(textComposite, Messages.AddIpsNatureAction_basePackageName, false);
            basePackageText = kit.createText(textComposite, SWT.BORDER);
            basePackageText.setText(basePackageName);
            basePackageText.addModifyListener(new ModifyListener() {
                public void modifyText(ModifyEvent event) {
                    basePackageModified();
                }
            });
            kit.createVerticalSpacer(composite, 5);

            kit.createLabel(textComposite, Messages.AddIpsNatureAction_runtimeIdPrefix, false);
            runtimeIdText = kit.createText(textComposite, SWT.BORDER);
            runtimeIdText.setText(runtimeIdPrefix);

            errorMessageText = new Text(composite, SWT.READ_ONLY);
            errorMessageText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
            errorMessageText.setBackground(errorMessageText.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
            setErrorMessage(errorMessage);

            applyDialogFont(composite);
            return composite;
        }

        /**
         * Fires validations (variable name first) and updates enabled state for the "Ok" button
         * accordingly.
         */
        private void sourceFolderModified() {
            validationStatus = IMessageProvider.NONE;
            okButton.setEnabled(validateSourceFolder());
        }

        /**
         * Fires validations (variable name first) and updates enabled state for the "Ok" button
         * accordingly.
         */
        private void basePackageModified() {
            validationStatus = IMessageProvider.NONE;
            okButton.setEnabled(validateBasePackage());
        }

        /**
         * Validates the current variable name, and updates this dialog's message.
         * 
         * @return true if the name is valid, false otherwise
         */
        private boolean validateSourceFolder() {
            boolean allowFinish = false;

            // if the current validationStatus is ERROR, no additional validation applies
            if (validationStatus == IMessageProvider.ERROR) {
                return false;
            }

            // assumes everything will be ok
            String message = Messages.AddIpsNatureAction_dialogMessage;
            int newValidationStatus = IMessageProvider.NONE;

            String sourceFolderName = sourceFolderText.getText();

            if (sourceFolderName.length() == 0) {
                // the source folder name is empty
                newValidationStatus = IMessageProvider.ERROR;
                message = Messages.AddIpsNatureAction_ErrorNoSourceFolderName;
            } else {
                allowFinish = true;
            }
            // overwrite the current validation status / message only if everything is ok (clearing
            // them)
            // or if we have a more serious problem than the current one
            if (validationStatus == IMessageProvider.NONE || newValidationStatus == IMessageProvider.ERROR) {
                validationStatus = newValidationStatus;
            }
            // only set the message here if it is not going to be set in
            // validateVariableValue to avoid flashing.
            setMessage(message, validationStatus);
            return allowFinish;
        }

        /**
         * Validates the current variable name, and updates this dialog's message.
         * 
         * @return true if the name is valid, false otherwise
         */
        private boolean validateBasePackage() {
            boolean allowFinish = false;

            // if the current validationStatus is ERROR, no additional validation applies
            if (validationStatus == IMessageProvider.ERROR) {
                return false;
            }

            // assumes everything will be ok
            String message = Messages.AddIpsNatureAction_dialogMessage;
            int newValidationStatus = IMessageProvider.NONE;

            String basePackageName = basePackageText.getText();

            if (!JavaConventions.validatePackageName(basePackageName).isOK()) {
                newValidationStatus = IMessageProvider.ERROR;
                message = Messages.AddIpsNatureAction_basePackageNameNotValid;
            } else {
                allowFinish = true;
            }
            // overwrite the current validation status / message only if everything is ok (clearing
            // them)
            // or if we have a more serious problem than the current one
            if (validationStatus == IMessageProvider.NONE || newValidationStatus == IMessageProvider.ERROR) {
                validationStatus = newValidationStatus;
            }
            // only set the message here if it is not going to be set in
            // validateVariableValue to avoid flashing.
            setMessage(message, validationStatus);
            return allowFinish;
        }

        /**
         * Sets or clears the error message. If not <code>null</code>, the OK button is disabled.
         * 
         * @param errorMessage the error message, or <code>null</code> to clear
         * @since 3.0
         */
        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            if (errorMessageText != null && !errorMessageText.isDisposed()) {
                errorMessageText.setText(errorMessage == null ? "" : errorMessage); //$NON-NLS-1$
                errorMessageText.getParent().update();
                // Access the ok button by id, in case clients have overridden button creation.
                // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=113643
                Control button = getButton(IDialogConstants.OK_ID);
                if (button != null) {
                    button.setEnabled(errorMessage == null);
                }
            }
        }

        /*
         * (non-Javadoc) Method declared on Dialog.
         */
        protected void buttonPressed(int buttonId) {
            if (buttonId == IDialogConstants.OK_ID) {
                sourceFolderName = sourceFolderText.getText();
                runtimeIdPrefix = runtimeIdText.getText();
                isModelProject = modelProjectButton.isChecked() || fullProjectButton.isChecked();
                isProductDefinitionProject = productDefinitionProjectButton.isChecked()
                        || fullProjectButton.isChecked();
            }
            super.buttonPressed(buttonId);
        }

        /*
         * (non-Javadoc) Method declared in Window.
         */
        protected Control createContents(Composite parent) {

            Control contents = super.createContents(parent);
            setTitle(Messages.AddIpsNatureAction_dialogTitle);
            dlgTitleImage = IpsPlugin.getDefault().getImageDescriptor("wizards/AddIpsNatureWizard.png").createImage(); //$NON-NLS-1$
            setTitleImage(dlgTitleImage);
            setMessage(Messages.AddIpsNatureAction_dialogMessage);

            return contents;
        }

        /**
         * This implementation of this <code>Window</code> method disposes of the banner image
         * when the dialog is closed.
         */
        public boolean close() {
            if (dlgTitleImage != null) {
                dlgTitleImage.dispose();
            }
            return super.close();
        }

    }

}
