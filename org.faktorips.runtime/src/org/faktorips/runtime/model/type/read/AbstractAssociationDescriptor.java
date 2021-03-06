/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.model.type.read;

import java.lang.reflect.Method;

import org.faktorips.runtime.model.type.Association;
import org.faktorips.runtime.model.type.ModelElement;
import org.faktorips.runtime.model.type.Type;

abstract class AbstractAssociationDescriptor<P extends Association> extends PartDescriptor<P> {

    private Method annotatedElement;

    public boolean isValid() {
        return getAnnotatedElement() != null;
    }

    public Method getAnnotatedElement() {
        return annotatedElement;
    }

    public void setAnnotatedElement(Method annotatedElement) {
        this.annotatedElement = annotatedElement;
    }

    @Override
    public P create(ModelElement parentElement) {
        Type type = (Type)parentElement;
        if (isValid()) {
            return createValid(type);
        } else {
            // else it must be defined in a super type but overridden (with the same name and
            // target) in this type. That leads to a different implementation being generated
            // but not a new annotation.
            Type superType = type.getSuperType();
            if (superType != null) {
                Association association = superType.getAssociation(getName());
                if (association != null) {
                    @SuppressWarnings("unchecked")
                    P overwritingAssociationFor = (P)association.createOverwritingAssociationFor(type);
                    return overwritingAssociationFor;
                }
            }
            throw new IllegalArgumentException(type.getDeclarationClass() + " lists \"" + getName()
                    + "\" as one of it's @IpsAssociations but no matching @IpsAssociation could be found.");
        }
    }

    protected abstract P createValid(Type type);

}