/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.flidentifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.devtools.core.builder.flidentifier.IdentifierNodeGeneratorFactory;
import org.faktorips.devtools.core.builder.flidentifier.ast.AttributeNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNodeFactory;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.stdbuilder.GeneratorRuntimeException;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyAttribute;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyCmptClass;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductAttribute;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductCmptClass;
import org.faktorips.fl.CompilationResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AttributeNodeGeneratorTest {

    @Mock
    private IdentifierNodeGeneratorFactory<JavaCodeFragment> factory;

    @Mock
    private StandardBuilderSet builderSet;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private CompilationResult<JavaCodeFragment> contextCompilationResult;

    private AttributeNodeGenerator attributeNodeGenerator;

    private AttributeNode attributeNode;

    private IAttribute attribute;

    @Before
    public void createAttributeNodeGenerator() {
        attributeNodeGenerator = new AttributeNodeGenerator(factory, builderSet);
    }

    private void createAttributeNode(boolean isDefault) throws CoreException {
        when(attribute.findDatatype(ipsProject)).thenReturn(Datatype.STRING);
        attributeNode = createAttributeNode(isDefault, false);
        when(contextCompilationResult.getDatatype()).thenReturn(Datatype.INTEGER);
    }

    @Test
    public void testGetCompilationResult_PolicyCmptTypeAttribute() throws Exception {
        attribute = mock(IPolicyCmptTypeAttribute.class);
        createAttributeNode(false);
        XPolicyAttribute xPolicyAttribute = mock(XPolicyAttribute.class);
        when(xPolicyAttribute.getMethodNameGetter()).thenReturn("getAttribute");
        when(builderSet.getModelNode(attribute, XPolicyAttribute.class)).thenReturn(xPolicyAttribute);

        CompilationResult<JavaCodeFragment> compilationResult = attributeNodeGenerator.getCompilationResult(
                attributeNode, contextCompilationResult);

        assertFalse(compilationResult.failed());
        assertEquals(".getAttribute()", compilationResult.getCodeFragment().getSourcecode());
        assertEquals(Datatype.STRING, compilationResult.getDatatype());
    }

    @Test
    public void testGetCompilationResult_PolicyCmptTypeAttributeDefault() throws Exception {
        attribute = mock(IPolicyCmptTypeAttribute.class);
        createAttributeNode(true);
        IType type = mock(IType.class);
        when(attribute.getType()).thenReturn(type);
        XPolicyCmptClass xPolicyCmptClass = mock(XPolicyCmptClass.class);
        XPolicyAttribute xPolicyAttribute = mock(XPolicyAttribute.class);
        when(builderSet.getModelNode(attribute, XPolicyAttribute.class)).thenReturn(xPolicyAttribute);
        when(builderSet.getModelNode(type, XPolicyCmptClass.class)).thenReturn(xPolicyCmptClass);
        when(xPolicyCmptClass.getMethodNameGetProductCmptGeneration()).thenReturn("getProductCmptGen");
        when(xPolicyAttribute.getMethodNameGetDefaultValue()).thenReturn("getAttribute");

        CompilationResult<JavaCodeFragment> compilationResult = attributeNodeGenerator.getCompilationResult(
                attributeNode, contextCompilationResult);

        assertFalse(compilationResult.failed());
        assertEquals(".getProductCmptGen().getAttribute()", compilationResult.getCodeFragment().getSourcecode());
        assertEquals(Datatype.STRING, compilationResult.getDatatype());
    }

    @Test
    public void testGetCompilationResult_ProductCmptTypeAttributeChangingOverTime() throws Exception {
        attribute = mock(IProductCmptTypeAttribute.class);
        createAttributeNode(false);
        XProductAttribute xProductAttribute = mock(XProductAttribute.class);
        when(builderSet.getModelNode(attribute, XProductAttribute.class)).thenReturn(xProductAttribute);
        when(xProductAttribute.isChangingOverTime()).thenReturn(true);
        when(xProductAttribute.getMethodNameGetter()).thenReturn("getAttribute");
        CompilationResult<JavaCodeFragment> compilationResult = attributeNodeGenerator.getCompilationResult(
                attributeNode, contextCompilationResult);

        assertFalse(compilationResult.failed());
        assertEquals(".getAttribute()", compilationResult.getCodeFragment().getSourcecode());
        assertEquals(Datatype.STRING, compilationResult.getDatatype());
    }

    @Test
    public void testGetCompilationResult_ProductCmptTypeAttribute() throws Exception {
        attribute = mock(IProductCmptTypeAttribute.class);
        createAttributeNode(false);
        IType type = mock(IType.class);
        when(attribute.getType()).thenReturn(type);
        XProductAttribute xProductAttribute = mock(XProductAttribute.class);
        XProductCmptClass xProductCmptClass = mock(XProductCmptClass.class);

        when(builderSet.getModelNode(attribute, XProductAttribute.class)).thenReturn(xProductAttribute);
        when(builderSet.getModelNode(type, XProductCmptClass.class)).thenReturn(xProductCmptClass);
        when(xProductAttribute.isChangingOverTime()).thenReturn(false);
        when(xProductAttribute.getMethodNameGetter()).thenReturn("getAttribute");
        when(xProductCmptClass.getMethodNameGetProductCmpt()).thenReturn("getProductCmpt");

        CompilationResult<JavaCodeFragment> compilationResult = attributeNodeGenerator.getCompilationResult(
                attributeNode, contextCompilationResult);

        assertFalse(compilationResult.failed());
        assertEquals(".getProductCmpt().getAttribute()", compilationResult.getCodeFragment().getSourcecode());
        assertEquals(Datatype.STRING, compilationResult.getDatatype());
    }

    @Test
    public void testGetCompilationResult_ListOfTypeDatatype() throws Exception {
        attribute = mock(IPolicyCmptTypeAttribute.class);
        IPolicyCmptType type = mock(IPolicyCmptType.class);
        when(attribute.findDatatype(ipsProject)).thenReturn(Datatype.INTEGER);
        when(attribute.getDatatype()).thenReturn("Integer");
        attributeNode = createAttributeNode(false, true);

        JavaCodeFragment javaCodeFragment = new JavaCodeFragment("hsVertrag.getDeckungen()");
        when(contextCompilationResult.getCodeFragment()).thenReturn(javaCodeFragment);
        ListOfTypeDatatype listofTypeDatatype = mock(ListOfTypeDatatype.class);
        when(listofTypeDatatype.getBasicDatatype()).thenReturn(type);
        when(type.getJavaClassName()).thenReturn("HausratVertrag");
        when(contextCompilationResult.getDatatype()).thenReturn(listofTypeDatatype);
        XPolicyAttribute xPolicyAttribute = mock(XPolicyAttribute.class);
        when(xPolicyAttribute.getMethodNameGetter()).thenReturn("getWohnflaeche");
        when(builderSet.getModelNode(attribute, XPolicyAttribute.class)).thenReturn(xPolicyAttribute);
        CompilationResult<JavaCodeFragment> compilationResult = attributeNodeGenerator.getCompilationResult(
                attributeNode, contextCompilationResult);
        assertFalse(compilationResult.failed());
        System.out.println(compilationResult.getCodeFragment().getSourcecode());
    }

    @Test(expected = GeneratorRuntimeException.class)
    public void testGetCompilationResult_NoListOfTypeDatatype() throws Exception {
        attribute = mock(IPolicyCmptTypeAttribute.class);
        when(attribute.findDatatype(ipsProject)).thenReturn(Datatype.INTEGER);
        when(attribute.getDatatype()).thenReturn("Integer");
        attributeNode = createAttributeNode(false, false);
        ListOfTypeDatatype listofTypeDatatype = mock(ListOfTypeDatatype.class);
        when(contextCompilationResult.getDatatype()).thenReturn(listofTypeDatatype);
        attributeNodeGenerator.getCompilationResult(attributeNode, contextCompilationResult);
    }

    private AttributeNode createAttributeNode(boolean defaultAccess, boolean listOfType) {
        return (AttributeNode)new IdentifierNodeFactory(attribute.getName(), ipsProject).createAttributeNode(attribute,
                defaultAccess, listOfType);
    }

}
