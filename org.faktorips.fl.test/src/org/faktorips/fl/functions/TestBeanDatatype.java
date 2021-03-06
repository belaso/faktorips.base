/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl.functions;

import java.util.HashMap;

import org.faktorips.datatype.AbstractDatatype;
import org.faktorips.fl.BeanDatatype;
import org.faktorips.fl.PropertyDatatype;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.StringUtil;

/**
 * Implementation of BeanDatatype for testing purposes.
 * 
 * @author Jan Ortmann
 */
public class TestBeanDatatype extends AbstractDatatype implements BeanDatatype {

    private String name;
    private HashMap<String, PropertyDatatype> properties = new HashMap<String, PropertyDatatype>();

    public TestBeanDatatype(String javaClassName) {
        ArgumentCheck.notNull(javaClassName);
        this.name = StringUtil.unqualifiedName(javaClassName);
    }

    public void add(PropertyDatatype property) {
        properties.put(property.getName(), property);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PropertyDatatype getProperty(String name) {
        return properties.get(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getQualifiedName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPrimitive() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAbstract() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValueDatatype() {
        return false;
    }

}
