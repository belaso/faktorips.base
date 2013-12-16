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

package org.faktorips.devtools.core.ui.search.product;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.search.product.conditions.table.ProductSearchConditionPresentationModel;
import org.faktorips.devtools.core.ui.search.scope.IIpsSearchScope;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProductSearchQueryTest {

    private static final String PRODUCT_CMPT_TYPE_NAME = "ProductCmptType";

    @Mock
    private ProductSearchPresentationModel model;

    @Mock
    private ProductSearchConditionPresentationModel validCondition;

    @Mock
    private ProductSearchConditionPresentationModel invalidCondition;

    @Mock
    private IIpsModel ipsModel;

    @Mock
    private IIpsSearchScope scope;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private IIpsProject ipsProject2;

    @Mock
    private IProductCmptType productCmptType;

    private ProductSearchQuery query;

    @Before
    public void setUp() throws CoreException {
        when(validCondition.isValid()).thenReturn(true);
        when(invalidCondition.isValid()).thenReturn(false);

        when(model.getSearchScope()).thenReturn(scope);
        when(model.getProductCmptType()).thenReturn(productCmptType);

        when(productCmptType.getQualifiedName()).thenReturn(PRODUCT_CMPT_TYPE_NAME);

        when(ipsModel.getIpsProductDefinitionProjects()).thenReturn(new IIpsProject[] { ipsProject, ipsProject2 });

        when(ipsProject.findProductCmptType(PRODUCT_CMPT_TYPE_NAME)).thenReturn(productCmptType);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testIsOnlyTypeNameSearch() {
        query = new ProductSearchQuery(model, ipsModel);

        when(model.getProductSearchConditionPresentationModels()).thenReturn(
                new ArrayList<ProductSearchConditionPresentationModel>(), Arrays.asList(invalidCondition),
                Arrays.asList(invalidCondition, validCondition));

        assertTrue(query.isOnlyTypeNameSearch());
        assertTrue(query.isOnlyTypeNameSearch());
        assertFalse(query.isOnlyTypeNameSearch());
    }

    @Test
    public void testGetSelectedSrcFiles() throws CoreException {
        query = new ProductSearchQuery(model, ipsModel);

        IIpsSrcFile wrongObjectType = mock(IIpsSrcFile.class);
        when(wrongObjectType.getIpsObjectType()).thenReturn(IpsObjectType.POLICY_CMPT_TYPE);

        IIpsSrcFile wrongProductCmptType = mock(IIpsSrcFile.class);
        when(wrongProductCmptType.getIpsObjectType()).thenReturn(IpsObjectType.PRODUCT_CMPT);

        IIpsSrcFile selectedSrcFile = mock(IIpsSrcFile.class);
        when(selectedSrcFile.getIpsObjectType()).thenReturn(IpsObjectType.PRODUCT_CMPT);

        Set<IIpsSrcFile> selectedFiles = new HashSet<IIpsSrcFile>(Arrays.asList(wrongObjectType, wrongProductCmptType,
                selectedSrcFile));

        when(scope.getSelectedIpsSrcFiles()).thenReturn(selectedFiles);
        when(ipsProject.findAllProductCmptSrcFiles(productCmptType, true)).thenReturn(
                new IIpsSrcFile[] { selectedSrcFile });

        Set<IIpsSrcFile> selectedSrcFiles = query.getSelectedSrcFiles();

        assertEquals(1, selectedSrcFiles.size());
        assertTrue(selectedSrcFiles.contains(selectedSrcFile));
    }

    @Test
    public void testGetSelectedSrcFilesNoSelectedFiles() throws CoreException {
        query = new ProductSearchQuery(model, ipsModel);

        IIpsSrcFile wrongObjectType = mock(IIpsSrcFile.class);
        when(wrongObjectType.getIpsObjectType()).thenReturn(IpsObjectType.POLICY_CMPT_TYPE);

        IIpsSrcFile wrongProductCmptType = mock(IIpsSrcFile.class);
        when(wrongProductCmptType.getIpsObjectType()).thenReturn(IpsObjectType.PRODUCT_CMPT);

        IIpsSrcFile selectedSrcFile = mock(IIpsSrcFile.class);
        when(selectedSrcFile.getIpsObjectType()).thenReturn(IpsObjectType.PRODUCT_CMPT);

        when(scope.getSelectedIpsSrcFiles()).thenReturn(new HashSet<IIpsSrcFile>());

        Set<IIpsSrcFile> selectedSrcFiles = query.getSelectedSrcFiles();

        assertTrue(selectedSrcFiles.isEmpty());
    }
}
