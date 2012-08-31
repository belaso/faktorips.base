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

package org.faktorips.devtools.stdbuilder.xpand.productcmpt;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xpand.TypeBuilder;
import org.faktorips.devtools.stdbuilder.xpand.model.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductCmptGenerationClass;
import org.faktorips.util.LocalizedStringsSet;

public class ProductCmptGenerationInterfaceBuilder extends TypeBuilder {

    public ProductCmptGenerationInterfaceBuilder(StandardBuilderSet builderSet, GeneratorModelContext modelContext,
            ModelService modelService) {
        super(builderSet, modelContext, modelService, new LocalizedStringsSet(
                ProductCmptGenerationInterfaceBuilder.class));
    }

    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
        return IpsObjectType.PRODUCT_CMPT_TYPE.equals(ipsSrcFile.getIpsObjectType());
    }

    @Override
    protected Class<XProductCmptGenerationClass> getGeneratorModelNodeClass() {
        return XProductCmptGenerationClass.class;
    }

    @Override
    public boolean isBuildingPublishedSourceFile() {
        // TODO
        return false;
    }

    @Override
    public String getTemplate() {
        return "org::faktorips::devtools::stdbuilder::xpand::productcmpt::template::IProductComponentGeneration::main";
    }

    @Override
    protected boolean generatesInterface() {
        return true;
    }

}
