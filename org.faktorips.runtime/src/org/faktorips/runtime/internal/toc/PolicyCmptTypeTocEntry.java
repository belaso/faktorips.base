/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal.toc;

/**
 * This special kind of {@link ModelTypeTocEntry} represents an entry for policy component types
 * 
 * @author dirmeier
 */
public class PolicyCmptTypeTocEntry extends ModelTypeTocEntry {

    public static final String XML_TAG = "PolicyCmptType";

    public PolicyCmptTypeTocEntry(String ipsObjectId, String ipsObjectQualifiedName, String xmlResourceName,
            String implementationClassName) {
        super(ipsObjectId, ipsObjectQualifiedName, xmlResourceName, implementationClassName);
    }

    @Override
    protected String getXmlElementTag() {
        return XML_TAG;
    }

}
