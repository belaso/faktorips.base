/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.stdbuilder.xpand.model.AbstractGeneratorModelNode;

public class SinceVersionJavaDocTagGenerator implements IAnnotationGenerator {

    @Override
    public AnnotatedJavaElementType getAnnotatedJavaElementType() {
        return AnnotatedJavaElementType.ELEMENT_JAVA_DOC;
    }

    @Override
    public JavaCodeFragment createAnnotation(AbstractGeneratorModelNode modelNode) {
        return new JavaCodeFragment("@since " + modelNode.getSinceVersion());
    }

    @Override
    public boolean isGenerateAnnotationFor(AbstractGeneratorModelNode modelNode) {
        return modelNode.hasSinceVersion();
    }

    public static class Factory implements IAnnotationGeneratorFactory {

        @Override
        public boolean isRequiredFor(IIpsProject ipsProject) {
            return ipsProject.getVersionProvider().getProjectVersion() != null;
        }

        @Override
        public IAnnotationGenerator createAnnotationGenerator(AnnotatedJavaElementType type) {
            if (type == AnnotatedJavaElementType.ELEMENT_JAVA_DOC) {
                return new SinceVersionJavaDocTagGenerator();
            } else {
                return null;
            }
        }

    }

}
