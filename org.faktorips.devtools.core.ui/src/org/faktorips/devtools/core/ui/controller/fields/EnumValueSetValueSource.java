/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.controller.fields;

import java.util.List;

import org.faktorips.devtools.core.internal.model.valueset.EnumValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;

/**
 * This implementation of {@link IValueSource} considers all {@link IValueSet}s of
 * <code>IValueSetType.ENUM</code>. The datatype of the {@link IValueSet} is non specific.
 */
public class EnumValueSetValueSource implements IValueSource {

    private IValueSetOwner owner;

    public EnumValueSetValueSource(IValueSetOwner owner) {
        this.owner = owner;
    }

    @Override
    public List<String> getValues() {
        IValueSet valueSet = owner.getValueSet();
        EnumValueSet enumValueSet = (EnumValueSet)valueSet;
        return enumValueSet.getValuesAsList();
    }

    @Override
    public boolean isApplicable() {
        IValueSet valueSet = owner.getValueSet();
        return valueSet.isEnum();
    }

}
