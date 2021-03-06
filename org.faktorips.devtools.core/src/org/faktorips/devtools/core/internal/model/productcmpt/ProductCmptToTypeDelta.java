/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.productcmpt.deltaentries.InvalidGenerationsDeltaEntry;
import org.faktorips.devtools.core.internal.model.productcmpt.deltaentries.MissingPropertyValueEntry;
import org.faktorips.devtools.core.internal.model.productcmpt.deltaentries.ValueWithoutPropertyEntry;
import org.faktorips.devtools.core.model.ipsobject.IFixDifferencesComposite;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IDeltaEntry;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainerToTypeDelta;

public class ProductCmptToTypeDelta extends PropertyValueContainerToTypeDelta {

    public ProductCmptToTypeDelta(IProductCmpt productCmpt, IIpsProject ipsProject) throws CoreException {
        super(productCmpt, productCmpt, ipsProject);
    }

    @Override
    public IProductCmpt getPropertyValueContainer() {
        return (IProductCmpt)super.getPropertyValueContainer();
    }

    @Override
    protected void createAdditionalEntriesAndChildren() throws CoreException {
        checkInvalidGenerations();
        for (IIpsObjectGeneration generation : getPropertyValueContainer().getGenerationsOrderedByValidDate()) {
            ProductCmptGeneration productCmptGen = (ProductCmptGeneration)generation;
            IPropertyValueContainerToTypeDelta computeDeltaToModel = productCmptGen
                    .computeDeltaToModel(getIpsProject());
            addChild(computeDeltaToModel);
        }
        findAndSetPredecessors();
    }

    private void checkInvalidGenerations() {
        if (!getPropertyValueContainer().allowGenerations() && getPropertyValueContainer().getGenerations().size() > 1) {
            addEntry(new InvalidGenerationsDeltaEntry(getPropertyValueContainer()));
        }
    }

    private void findAndSetPredecessors() {
        List<ValueWithoutPropertyEntry> valueWithoutPropertyEntries = new ArrayList<ValueWithoutPropertyEntry>();
        List<MissingPropertyValueEntry> missingPropertyValueEntries = new ArrayList<MissingPropertyValueEntry>();

        addEntries(getEntries(), valueWithoutPropertyEntries, missingPropertyValueEntries);
        for (IFixDifferencesComposite fixDifferencesComposite : getChildren()) {
            PropertyValueContainerToTypeDelta propertyValueContainerToTypeDelta = (PropertyValueContainerToTypeDelta)fixDifferencesComposite;
            addEntries(propertyValueContainerToTypeDelta.getEntries(), valueWithoutPropertyEntries,
                    missingPropertyValueEntries);
        }

        for (ValueWithoutPropertyEntry valueWithoutPropertyEntry : valueWithoutPropertyEntries) {
            for (MissingPropertyValueEntry missingPropertyValueEntry : missingPropertyValueEntries) {
                if (valueWithoutPropertyEntry.getPropertyName().equals(missingPropertyValueEntry.getPropertyName())) {
                    missingPropertyValueEntry.setPredecessor(valueWithoutPropertyEntry);
                }
            }
        }

    }

    protected void addEntries(IDeltaEntry[] entries,
            List<ValueWithoutPropertyEntry> valueWithoutPropertyEntries,
            List<MissingPropertyValueEntry> missingPropertyEntries) {
        for (IDeltaEntry entry : entries) {
            if (entry instanceof ValueWithoutPropertyEntry) {
                valueWithoutPropertyEntries.add((ValueWithoutPropertyEntry)entry);
            }
            if (entry instanceof MissingPropertyValueEntry) {
                missingPropertyEntries.add((MissingPropertyValueEntry)entry);
            }
        }
    }

}
