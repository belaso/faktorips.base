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

package org.faktorips.devtools.core.ui.controlfactories;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.DateControlField;
import org.faktorips.devtools.core.ui.controls.AbstractDateTimeControl;
import org.faktorips.devtools.core.ui.inputformat.AbstractInputFormat;
import org.faktorips.devtools.core.ui.table.FormattingTextCellEditor;
import org.faktorips.devtools.core.ui.table.IpsCellEditor;
import org.faktorips.devtools.core.ui.table.TableViewerTraversalStrategy;
import org.faktorips.devtools.core.ui.table.TextCellEditor;

/**
 * A factory for edit fields/controls for date/time datatypes.
 * 
 * @since 3.7
 */
public abstract class AbstractDateTimeControlFactory extends ValueDatatypeControlFactory {

    public AbstractDateTimeControlFactory() {
        super();
    }

    @Override
    public EditField<String> createEditField(UIToolkit toolkit,
            Composite parent,
            ValueDatatype datatype,
            IValueSet valueSet,
            IIpsProject ipsProject) {

        AbstractDateTimeControl dateControl = createDateTimeControl(parent, toolkit);
        DateControlField<String> formatField = new DateControlField<String>(dateControl, getFormat());
        return formatField;
    }

    protected abstract AbstractDateTimeControl createDateTimeControl(Composite parent, UIToolkit toolkit);

    protected abstract AbstractInputFormat<String> getFormat();

    @Override
    public Control createControl(UIToolkit toolkit,
            Composite parent,
            ValueDatatype datatype,
            IValueSet valueSet,
            IIpsProject ipsProject) {
        Text text = toolkit.createTextAppendStyle(parent, getDefaultAlignment());
        return text;
    }

    protected Button createButton(UIToolkit toolkit, Composite calendarComposite) {
        GridData buttonGridData = new GridData(SWT.FILL, SWT.FILL, false, false);
        Button button = toolkit.createButton(calendarComposite, ""); //$NON-NLS-1$
        button.setLayoutData(buttonGridData);
        button.setImage(IpsUIPlugin.getImageHandling().getSharedImage("Calendar.png", true)); //$NON-NLS-1$
        return button;
    }

    /**
     * @deprecated use
     *             {@link #createTableCellEditor(UIToolkit, ValueDatatype, IValueSet, TableViewer, int, IIpsProject)}
     *             instead.
     */
    @Deprecated
    @Override
    public IpsCellEditor createCellEditor(UIToolkit toolkit,
            ValueDatatype dataType,
            IValueSet valueSet,
            TableViewer tableViewer,
            int columnIndex,
            IIpsProject ipsProject) {

        return createTableCellEditor(toolkit, dataType, valueSet, tableViewer, columnIndex, ipsProject);
    }

    /**
     * Creates a {@link TextCellEditor} containing a {@link Text} control and configures it with a
     * {@link TableViewerTraversalStrategy}.
     */
    @Override
    public IpsCellEditor createTableCellEditor(UIToolkit toolkit,
            ValueDatatype dataType,
            IValueSet valueSet,
            TableViewer tableViewer,
            int columnIndex,
            IIpsProject ipsProject) {

        IpsCellEditor cellEditor = createTextCellEditor(toolkit, dataType, valueSet, tableViewer.getTable(), ipsProject);
        TableViewerTraversalStrategy strat = new TableViewerTraversalStrategy(cellEditor, tableViewer, columnIndex);
        strat.setRowCreating(true);
        cellEditor.setTraversalStrategy(strat);
        return cellEditor;
    }

    private IpsCellEditor createTextCellEditor(UIToolkit toolkit,
            ValueDatatype dataType,
            IValueSet valueSet,
            Composite parent,
            IIpsProject ipsProject) {

        Text textControl = (Text)createControl(toolkit, parent, dataType, valueSet, ipsProject);
        IpsCellEditor tableCellEditor = new FormattingTextCellEditor<String>(textControl, getFormat());
        return tableCellEditor;
    }

    @Override
    public int getDefaultAlignment() {
        return SWT.RIGHT;
    }

}
