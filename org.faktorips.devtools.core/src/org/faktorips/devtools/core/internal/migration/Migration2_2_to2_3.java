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

package org.faktorips.devtools.core.internal.migration;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.internal.model.tablestructure.TableStructureType;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.tablestructure.IUniqueKey;
import org.faktorips.util.ArgumentCheck;

/**
 * Provides a static method that performs the migration to version 2.3 featuring new
 * <tt>IEnumType</tt> ips objects.
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class Migration2_2_to2_3 {

    /** Prohibit instantiation. */
    private Migration2_2_to2_3() {

    }

    /**
     * Replaces all <tt>ITableStructure</tt>s that have been declared to be enumeration structures
     * with new, abstract <code>IEnumType</code>s.
     * <p>
     * Also, all <tt>ITableContents</tt> that are built upon an <tt>ITableStructure</tt> will become
     * <code>IEnumType</code>s containing the enum values. The referenced table structure will be
     * the super enum type.
     * 
     * @param ipsProject The ips project to migrate to version 2.3.
     * @param monitor The progress monitor to use to show progress to the user or <tt>null</tt> if
     *            none is available.
     * 
     * @throws CoreException If an error occurs while searching for the <tt>ITableStructure</tt> or
     *             <tt>ITableContents</tt> ips objects or while creating the new <tt>IEnumType</tt>
     *             ips objects.
     * @throws NullPointerException If <tt>ipsProject</tt> is <tt>null</tt>.
     */
    public static void migrate(IIpsProject ipsProject, IProgressMonitor monitor) throws CoreException {
        ArgumentCheck.notNull(ipsProject);

        // Find all enum type table structures.
        IIpsSrcFile[] tableStructureSrcFiles = ipsProject.findIpsSrcFiles(IpsObjectType.TABLE_STRUCTURE);
        List<ITableStructure> enumTableStructures = new ArrayList<ITableStructure>();
        for (IIpsSrcFile currentIpsSrcFile : tableStructureSrcFiles) {
            ITableStructure currentTableStructure = (ITableStructure)currentIpsSrcFile.getIpsObject();
            if (currentTableStructure.getTableStructureType().equals(TableStructureType.ENUMTYPE_MODEL)) {
                enumTableStructures.add(currentTableStructure);
            }
        }

        // Find all table contents that refer to enum type table structures.
        IIpsSrcFile[] tableContentsSrcFiles = ipsProject.findIpsSrcFiles(IpsObjectType.TABLE_CONTENTS);
        List<ITableContents> enumTableContents = new ArrayList<ITableContents>();
        for (IIpsSrcFile currentIpsSrcFile : tableContentsSrcFiles) {
            ITableContents currentTableContents = (ITableContents)currentIpsSrcFile.getIpsObject();
            if (currentTableContents.findTableStructure(currentTableContents.getIpsProject()).getTableStructureType()
                    .equals(TableStructureType.ENUMTYPE_MODEL)) {
                enumTableContents.add(currentTableContents);
            }
        }

        // Start the progress monitor if available (now we know how much work needs to be done).
        if (monitor != null) {
            monitor.beginTask("Migration", enumTableStructures.size() + enumTableContents.size());
        }

        // Replace the table structures and table contents.
        replaceTableStructures(enumTableStructures, monitor);
        replaceTableContents(enumTableContents, ipsProject, monitor);

        // Finish the monitor if available.
        if (monitor != null) {
            monitor.done();
        }
    }

    /** Replaces the given enum table structures with new enum types. */
    private static void replaceTableStructures(List<ITableStructure> enumTableStructures, IProgressMonitor monitor)
            throws CoreException {

        /*
         * Create a new enum type object for each of the found enum type table structures and delete
         * the old table structures.
         */
        for (ITableStructure currentTableStructure : enumTableStructures) {
            // Create the new enum type.
            IIpsSrcFile newFile = currentTableStructure.getIpsPackageFragment().createIpsFile(IpsObjectType.ENUM_TYPE,
                    currentTableStructure.getName(), true, null);
            IEnumType newEnumType = (IEnumType)newFile.getIpsObject();
            newEnumType.setAbstract(true);
            newEnumType.setContainingValues(false);
            newEnumType.setDescription(currentTableStructure.getDescription());

            // Create enum attributes.
            // 1. key is the id, 2. key is the java literal name.
            IUniqueKey[] uniqueKeys = currentTableStructure.getUniqueKeys();
            String id = uniqueKeys[0].getKeyItemAt(0).getName();
            String literalName = uniqueKeys[1].getKeyItemAt(0).getName();
            for (IColumn currentColumn : currentTableStructure.getColumns()) {
                IEnumAttribute newEnumAttribute = newEnumType.newEnumAttribute();
                String currentColumnName = currentColumn.getName();
                newEnumAttribute.setName(currentColumnName);
                newEnumAttribute.setDatatype(currentColumn.getDatatype());
                boolean isLiteralName = literalName.equals(currentColumnName);
                if (isLiteralName) {
                    newEnumAttribute.setLiteralName(true);
                    newEnumAttribute.setUniqueIdentifier(true);
                    newEnumAttribute.setUsedAsNameInFaktorIpsUi(true);
                }
                boolean isId = id.equals(currentColumnName);
                if (isId) {
                    newEnumAttribute.setUniqueIdentifier(true);
                    newEnumAttribute.setUsedAsIdInFaktorIpsUi(true);
                }
                newEnumAttribute.setInherited(false);
                newEnumAttribute.setDescription(currentColumn.getDescription());
            }

            // Delete the old table structure.
            currentTableStructure.getIpsSrcFile().getCorrespondingResource().delete(true, null);

            // Update monitor if available.
            if (monitor != null) {
                monitor.worked(1);
            }
        }
    }

    /**
     * Replaces the given table contents referring to enum table structures with new enum types
     * containing the enum values.
     */
    private static void replaceTableContents(List<ITableContents> enumTableContents,
            IIpsProject ipsProject,
            IProgressMonitor monitor) throws CoreException {

        /*
         * Create a new enum type object for each of the found table contents and delete the old
         * table contents.
         */
        for (ITableContents currentTableContents : enumTableContents) {
            // Create the new enum content.
            IIpsSrcFile newFile = currentTableContents.getIpsPackageFragment().createIpsFile(IpsObjectType.ENUM_TYPE,
                    currentTableContents.getName(), true, null);
            IEnumType newEnumType = (IEnumType)newFile.getIpsObject();
            newEnumType.setSuperEnumType(currentTableContents.getTableStructure());
            newEnumType.setAbstract(false);
            newEnumType.setContainingValues(true);

            // Inherit the enum attributes.
            newEnumType.inheritEnumAttributes(newEnumType.findInheritEnumAttributeCandidates(ipsProject));

            // Create the enum values.
            for (IRow currentRow : ((ITableContentsGeneration)currentTableContents.getFirstGeneration()).getRows()) {
                IEnumValue newEnumValue = newEnumType.newEnumValue();
                List<IEnumAttributeValue> enumAttributeValues = newEnumValue.getEnumAttributeValues();
                for (int i = 0; i < enumAttributeValues.size(); i++) {
                    IEnumAttributeValue currentEnumAttributeValue = enumAttributeValues.get(i);
                    currentEnumAttributeValue.setValue(currentRow.getValue(i));
                }
            }

            // Delete the old table contents.
            currentTableContents.getIpsSrcFile().getCorrespondingResource().delete(true, null);

            // Update monitor if available.
            if (monitor != null) {
                monitor.worked(1);
            }
        }
    }

}
