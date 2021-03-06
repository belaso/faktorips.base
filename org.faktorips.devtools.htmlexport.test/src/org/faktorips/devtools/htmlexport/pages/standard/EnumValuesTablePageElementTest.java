/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.standard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.enums.EnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.value.IValue;
import org.faktorips.devtools.core.ui.UIDatatypeFormatter;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EnumValuesTablePageElementTest {

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private IEnumType enumType;

    @Mock
    private DocumentationContext doc;

    @Mock
    private IEnumValue enumValue1;

    @Mock
    private IEnumAttribute enumAttribute1;

    @Mock
    private EnumAttributeValue enumAttributeValue1;

    @Mock
    private IValue<String> stringValue;

    @Mock
    private ValueDatatype valueDatatype;

    @Mock
    private UIDatatypeFormatter datatypeFormatter;

    private EnumValuesTablePageElement pageElement;

    @Before
    public void setup() {
        List<IEnumValue> valuelist = new ArrayList<IEnumValue>();
        List<IEnumAttribute> attributeList = new ArrayList<IEnumAttribute>();
        valuelist.add(enumValue1);
        attributeList.add(enumAttribute1);

        doReturn(valuelist).when(enumType).getEnumValues();
        doReturn(ipsProject).when(enumType).getIpsProject();
        doReturn(ipsProject).when(enumAttribute1).getIpsProject();
        doReturn(ipsProject).when(doc).getIpsProject();
        doReturn(valuelist).when(enumType).getEnumValues();
        doReturn(attributeList).when(enumType).findAllEnumAttributes(true, ipsProject);

    }

    @Test
    public void testCreateRowWithIpsObjectPart_EmptyResult() {
        pageElement = new EnumValuesTablePageElement(enumType, doc);

        List<IPageElement> resultList = pageElement.createRowWithIpsObjectPart(enumValue1);

        assertNotNull(resultList);
        assertEquals(0, resultList.size());
    }

    @Test
    public void testCreateRowWithIpsObjectPart_filledResult() throws CoreException {
        doReturn(enumAttributeValue1).when(enumValue1).getEnumAttributeValue(enumAttribute1);
        doReturn(stringValue).when(enumAttributeValue1).getValue();
        doReturn("toBeAdded").when(stringValue).getDefaultLocalizedContent(ipsProject);
        doReturn(valueDatatype).when(enumAttribute1).findDatatype(ipsProject);
        doReturn(datatypeFormatter).when(doc).getDatatypeFormatter();
        doReturn("addedFormatedValue").when(datatypeFormatter).formatValue(valueDatatype, "toBeAdded");

        pageElement = new EnumValuesTablePageElement(enumType, doc);
        List<IPageElement> resultList = pageElement.createRowWithIpsObjectPart(enumValue1);

        assertNotNull(resultList);
        assertEquals(1, resultList.size());
        assertTrue(resultList.get(0).toString().contains("addedFormatedValue"));
    }

    @Test
    public void testCreateRowWithIpsObjectPart_filledResultThrowException() throws CoreException {
        doReturn(enumAttributeValue1).when(enumValue1).getEnumAttributeValue(enumAttribute1);
        doReturn(stringValue).when(enumAttributeValue1).getValue();
        doReturn("enumAttributeValue1Name").when(enumAttributeValue1).getName();
        doReturn("toBeAdded").when(stringValue).getDefaultLocalizedContent(ipsProject);
        when(enumAttribute1.findDatatype(ipsProject)).thenThrow(new CoreException(new IpsStatus("Test")));

        pageElement = new EnumValuesTablePageElement(enumType, doc);
        List<IPageElement> resultList = pageElement.createRowWithIpsObjectPart(enumValue1);

        assertNotNull(resultList);
        assertEquals(1, resultList.size());
        assertTrue(resultList.get(0).toString().contains("toBeAdded"));
    }

    @Test
    public void testCreateRowWithIpsObjectPart_filledResultNoEnumAttributeValue() {
        doReturn(enumAttributeValue1).when(enumValue1).getEnumAttributeValue(null);

        pageElement = new EnumValuesTablePageElement(enumType, doc);
        List<IPageElement> resultList = pageElement.createRowWithIpsObjectPart(enumValue1);

        assertNotNull(resultList);
        assertEquals(0, resultList.size());
    }

    @Test
    public void testCreateRowWithIpsObjectPart_filledResultNoEnumAttributeValueValue() {
        doReturn(enumAttributeValue1).when(enumValue1).getEnumAttributeValue(enumAttribute1);
        doReturn(null).when(enumAttributeValue1).getValue();

        pageElement = new EnumValuesTablePageElement(enumType, doc);
        List<IPageElement> resultList = pageElement.createRowWithIpsObjectPart(enumValue1);

        assertNotNull(resultList);
        assertEquals(0, resultList.size());
    }
}