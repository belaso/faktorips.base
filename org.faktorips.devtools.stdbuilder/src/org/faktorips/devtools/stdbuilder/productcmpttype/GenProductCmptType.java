/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.productcmpttype;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.productcmpttype.association.GenProdAssociation;
import org.faktorips.devtools.stdbuilder.productcmpttype.association.GenProdAssociationTo1;
import org.faktorips.devtools.stdbuilder.productcmpttype.association.GenProdAssociationToMany;
import org.faktorips.devtools.stdbuilder.productcmpttype.attribute.GenProductCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.productcmpttype.method.GenProductCmptTypeMethod;
import org.faktorips.devtools.stdbuilder.productcmpttype.tableusage.GenTableStructureUsage;
import org.faktorips.devtools.stdbuilder.type.GenType;
import org.faktorips.devtools.stdbuilder.type.GenTypePart;
import org.faktorips.runtime.IllegalRepositoryModificationException;
import org.faktorips.runtime.internal.MethodNames;
import org.faktorips.util.LocalizedStringsSet;

/**
 * A generator for <code>IProductCmptType</code>s. It provides access to generators for attributes,
 * methods and associations of the product component type. Typically when the generator is created
 * all the generators of its parts are also created except the ones in the super type hierarchy.
 * These are created on demand since it is expected that only a few of them will be overridden. It
 * is necessary to provide an own generator instance for those overridden parts in this generator
 * and not to delegate to the generator of the super class since otherwise it would not be possible
 * to determine if code has to be generated with respect to the super type.
 * 
 * @author Peter Erzberger
 */
public class GenProductCmptType extends GenType {

    private List<GenProductCmptTypeAttribute> genProductCmptTypeAttributes = new ArrayList<GenProductCmptTypeAttribute>();
    private List<GenProdAssociation> genProdAssociations = new ArrayList<GenProdAssociation>();
    private List<GenProductCmptTypeMethod> genMethods = new ArrayList<GenProductCmptTypeMethod>();
    private List<GenTableStructureUsage> genTableStructureUsages = new ArrayList<GenTableStructureUsage>();

    public GenProductCmptType(IProductCmptType productCmptType, StandardBuilderSet builderSet) throws CoreException {
        super(productCmptType, builderSet, new LocalizedStringsSet(GenProductCmptType.class));
        createGeneratorsForProdAttributes();
        createGeneratorsForProdAssociations();
        createGeneratorsForMethods();
        createGeneratorsForTableStructureUsages();
    }

    public IProductCmptType getProductCmptType() {
        return (IProductCmptType)getType();
    }

    public String getUnqualifiedClassNameForProductCmptTypeGen(boolean forInterface) {
        if (forInterface) {
            String name = getType().getName() + getAbbreviationForGenerationConcept();
            return getJavaNamingConvention().getPublishedInterfaceName(name);
        }
        String generationAbb = getAbbreviationForGenerationConcept();
        return getJavaNamingConvention().getImplementationClassName(getType().getName() + generationAbb);
    }

    /**
     * Code sample:
     * 
     * <pre>
     * [Javadoc]
     * public IMotorProductGen getMotorProductGen();
     * </pre>
     */
    public void generateMethodGetProductCmptGeneration(IIpsProject ipsProject, JavaCodeFragmentBuilder methodsBuilder)
            throws CoreException {
        IPolicyCmptType pcType = getProductCmptType().findPolicyCmptType(ipsProject);
        String[] replacements = new String[] { getNameForGenerationConcept(true), getType().getName(),
                pcType != null ? pcType.getName() : "missing" };
        appendLocalizedJavaDoc("METHOD_GET_PRODUCTCMPT_GENERATION", replacements, methodsBuilder);
        generateSignatureGetProductCmptGeneration(methodsBuilder);
        methodsBuilder.appendln(";");
    }

    /**
     * Returns the name of the method to access the product component generation, e.g.
     * getMotorProductGen
     */
    public String getMethodNameGetProductCmptGeneration() {
        String[] replacements = new String[] { getType().getName(), getAbbreviationForGenerationConcept(),
                getNameForGenerationConcept(true) };
        return getLocalizedText("METHOD_GET_PRODUCTCMPT_GENERATION_NAME", replacements);
    }

    /**
     * Returns the name of the method to set the product component, e.g. setMotorProduct
     */
    public String getMethodNameSetProductCmpt() {
        return getLocalizedText("METHOD_SET_PRODUCTCMPT_NAME", getType().getName());
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public IMotorProductGen getMotorProductGen()
     * </pre>
     */
    public void generateSignatureGetProductCmptGeneration(JavaCodeFragmentBuilder methodsBuilder) {
        String methodName = getMethodNameGetProductCmptGeneration();
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, getQualifiedClassNameForProductCmptTypeGen(true),
                methodName, new String[0], new String[0]);
    }

    /**
     * Returns the name of the method to access the product component, e.g. getMotorProduct
     */
    public String getMethodNameGetProductCmpt() {
        return getLocalizedText("METHOD_GET_PRODUCTCMPT_NAME", getType().getName());
    }

    private void createGeneratorsForProdAttributes() throws CoreException {
        List<IProductCmptTypeAttribute> attrs = getProductCmptType().getProductCmptTypeAttributes();
        for (IProductCmptTypeAttribute attr : attrs) {
            if (attr.isValid(attr.getIpsProject())) {
                GenProductCmptTypeAttribute generator = new GenProductCmptTypeAttribute(this, attr);
                genProductCmptTypeAttributes.add(generator);
                getGeneratorsByPart().put(attr, generator);
            }
        }
    }

    private void createGeneratorsForProdAssociations() throws CoreException {
        List<IProductCmptTypeAssociation> ass = getProductCmptType().getProductCmptTypeAssociations();
        for (IProductCmptTypeAssociation as : ass) {
            if (as.isValid(as.getIpsProject())) {
                GenProdAssociation generator = createGenerator(as);
                genProdAssociations.add(generator);
                getGeneratorsByPart().put(as, generator);
            }
        }
    }

    private void createGeneratorsForMethods() throws CoreException {
        List<IProductCmptTypeMethod> methods = getProductCmptType().getProductCmptTypeMethods();
        for (IProductCmptTypeMethod method : methods) {
            if (method.isValid(method.getIpsProject())) {
                GenProductCmptTypeMethod generator = new GenProductCmptTypeMethod(this, method);
                genMethods.add(generator);
                getGeneratorsByPart().put(method, generator);
            }
        }
    }

    private void createGeneratorsForTableStructureUsages() throws CoreException {
        List<ITableStructureUsage> tsus = getProductCmptType().getTableStructureUsages();
        for (ITableStructureUsage tsu : tsus) {
            if (tsu.isValid(tsu.getIpsProject())) {
                GenTableStructureUsage generator = new GenTableStructureUsage(this, tsu);
                genTableStructureUsages.add(generator);
                getGeneratorsByPart().put(tsu, generator);
            }
        }
    }

    private GenProdAssociation createGenerator(IProductCmptTypeAssociation association) throws CoreException {
        if (association.is1ToMany()) {
            return new GenProdAssociationToMany(this, association);
        }
        return new GenProdAssociationTo1(this, association);
    }

    public GenProductCmptTypeAttribute getGenerator(IProductCmptTypeAttribute a) throws CoreException {
        GenProductCmptTypeAttribute generator = (GenProductCmptTypeAttribute)getGeneratorsByPart().get(a);
        if (generator == null && a.isValid(a.getIpsProject())) {
            // generators for supertype attributes will be created on demand since it is expected
            // that only a few exist. It will not be checked if the provided attribute is actually a
            // supertype attribute because of performance reasons.
            generator = new GenProductCmptTypeAttribute(this, a);
            genProductCmptTypeAttributes.add(generator);
            getGeneratorsByPart().put(a, generator);
        }

        return generator;
    }

    public GenProductCmptTypeMethod getGenerator(IProductCmptTypeMethod method) throws CoreException {
        GenProductCmptTypeMethod generator = (GenProductCmptTypeMethod)getGeneratorsByPart().get(method);
        if (generator == null && method.isValid(method.getIpsProject())) {
            // generators for supertype methods will be created on demand since it is expected that
            // only a few exit. It will not be checked if the provided method is actually a
            // supertype
            // method because of performance reasons.
            generator = new GenProductCmptTypeMethod(this, method);
            genMethods.add(generator);
            getGeneratorsByPart().put(method, generator);
        }
        return generator;
    }

    public GenProdAssociation getGenerator(IProductCmptTypeAssociation a) throws CoreException {
        GenProdAssociation generator = (GenProdAssociation)getGeneratorsByPart().get(a);
        if (generator == null && a.isValid(a.getIpsProject())) {
            // generators for supertype associations will be created on demand since it is expected
            // that
            // only a few exit. It will not be checked if the provided association is actually a
            // supertype
            // assocation because of performance reasons.
            generator = createGenerator(a);
            genProdAssociations.add(generator);
            getGeneratorsByPart().put(a, generator);
        }
        return generator;
    }

    public GenTableStructureUsage getGenerator(ITableStructureUsage tsu) {
        return (GenTableStructureUsage)getGeneratorsByPart().get(tsu);
    }

    public List<GenProductCmptTypeAttribute> getGenProdAttributes() {
        return genProductCmptTypeAttributes;
    }

    public JavaCodeFragment generateFragmentCheckIfRepositoryIsModifiable() {
        JavaCodeFragment frag = new JavaCodeFragment();
        frag.appendln("if (" + MethodNames.GET_REPOSITORY + "()!=null && !" + MethodNames.GET_REPOSITORY + "()."
                + MethodNames.IS_MODIFIABLE + "()) {");
        frag.append("throw new ");
        frag.appendClassName(IllegalRepositoryModificationException.class);
        frag.appendln("();");
        frag.appendln("}");
        return frag;
    }

    public String getMethodNameGetGeneration() {
        IChangesOverTimeNamingConvention convention = getProductCmptType().getIpsProject()
                .getChangesInTimeNamingConventionForGeneratedCode();
        Locale locale = getLanguageUsedInGeneratedSourceCode();
        String generationConceptName = convention.getGenerationConceptNameSingular(locale);
        String generationConceptAbbreviation = convention.getGenerationConceptNameAbbreviation(locale);
        return getLocalizedText("METHOD_GET_GENERATION_NAME", new String[] { getProductCmptType().getName(),
                generationConceptAbbreviation, generationConceptName });
    }

    public String getQualifiedClassNameForProductCmptTypeGen(boolean forInterface) {
        return getQualifiedName(forInterface)
                + getChangesInTimeNamingConvention().getGenerationConceptNameAbbreviation(
                        getLanguageUsedInGeneratedSourceCode());
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public IProductGen getGeneration(Calendar effectiveDate)
     * </pre>
     */
    void generateSignatureGetGeneration(JavaCodeFragmentBuilder methodsBuilder) {
        String generationInterface = getQualifiedClassNameForProductCmptTypeGen(true);
        String methodName = getMethodNameGetGeneration();
        String paramName = getVarNameEffectiveDate();
        methodsBuilder.signature(Modifier.PUBLIC, generationInterface, methodName, new String[] { paramName },
                new String[] { Calendar.class.getName() });
    }

    /**
     * Returns the variable or parameter name for the effetiveDate.
     * 
     */
    public String getVarNameEffectiveDate() {
        IChangesOverTimeNamingConvention convention = getChangesInTimeNamingConvention();
        Locale locale = getLanguageUsedInGeneratedSourceCode();
        String conceptName = convention.getEffectiveDateConceptName(locale);
        return StringUtils.uncapitalize(conceptName);
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public IMotorProduct getMotorProduct()
     * </pre>
     */
    public void generateSignatureGetProductCmpt(JavaCodeFragmentBuilder methodsBuilder) {
        String returnType = getQualifiedName(true);
        String methodName = getMethodNameGetProductCmpt();
        methodsBuilder.signature(Modifier.PUBLIC, returnType, methodName, EMPTY_STRING_ARRAY, EMPTY_STRING_ARRAY);
    }

    /**
     * Code sample:
     * 
     * <pre>
     * public void setMotorProduct(IMotorProduct motorProduct, boolean initPropertiesWithConfiguratedDefaults)
     * </pre>
     */
    public void generateSignatureSetProductComponent(JavaCodeFragmentBuilder methodsBuilder) {
        String methodName = getMethodNameSetProductCmpt();
        String[] paramTypes = new String[] { getQualifiedName(true), "boolean" };
        methodsBuilder.signature(java.lang.reflect.Modifier.PUBLIC, "void", methodName,
                getMethodParamNamesSetProductCmpt(), paramTypes);
    }

    /** Returns the method parameters for the method: setProductCmpt. */
    public String[] getMethodParamNamesSetProductCmpt() {
        return new String[] { StringUtils.uncapitalize(getType().getName()), "initPropertiesWithConfiguratedDefaults" };
    }

    @Override
    protected void getGeneratedJavaElementsForType(List<IJavaElement> javaElements,
            IType generatedJavaType,
            boolean forInterface) {

        if (getProductCmptType().isConfigurationForPolicyCmptType()) {
            IType javaTypePolicyCmpt = null;
            try {
                javaTypePolicyCmpt = findGeneratedJavaTypeForPolicyCmptType(forInterface);
            } catch (CoreException e) {
                throw new RuntimeException(e);
            }

            if (javaTypePolicyCmpt != null) {
                IMethod getProductCmptMethod = javaTypePolicyCmpt.getMethod(getMethodNameGetProductCmpt(),
                        new String[] {});
                javaElements.add(getProductCmptMethod);
                IMethod getProductCmptGenMethod = javaTypePolicyCmpt.getMethod(getMethodNameGetProductCmptGeneration(),
                        new String[] {});
                javaElements.add(getProductCmptGenMethod);
                IMethod setProductCmptMethod = javaTypePolicyCmpt.getMethod(getMethodNameSetProductCmpt(),
                        new String[] {
                                "Q" + getJavaNamingConvention().getPublishedInterfaceName(getType().getName()) + ";",
                                "Z" });
                javaElements.add(setProductCmptMethod);
            }
        }
    }

    @Override
    protected GenTypePart getGenTypePart(IIpsObjectPart ipsObjectPart) throws CoreException {
        GenTypePart genTypePart = null;
        if (ipsObjectPart instanceof IProductCmptTypeAssociation) {
            genTypePart = getGenerator((IProductCmptTypeAssociation)ipsObjectPart);
        }
        if (ipsObjectPart instanceof IProductCmptTypeAttribute) {
            genTypePart = getGenerator((IProductCmptTypeAttribute)ipsObjectPart);
        }
        if (ipsObjectPart instanceof IProductCmptTypeMethod) {
            genTypePart = getGenerator((IProductCmptTypeMethod)ipsObjectPart);
        }
        if (ipsObjectPart instanceof ITableStructureUsage) {
            genTypePart = getGenerator((ITableStructureUsage)ipsObjectPart);
        }
        return genTypePart;
    }

    /**
     * Searches and returns the Java type generated for the <tt>IPolicyCmptType</tt> that is
     * configured by the <tt>IProductCmptType</tt> this generator is configured for.
     * <p>
     * Returns <tt>null</tt> if the <tt>IPolicyCmptType</tt> cannot be found.
     * 
     * @param forInterface Flag indicating whether to search for the published interface of the
     *            <tt>IPolicyCmptType</tt> (<tt>true</tt>) or for it's implementation (
     *            <tt>false</tt>).
     * 
     * @throws CoreException If an error occurs while searching for the <tt>IPolicyCmptType</tt>.
     */
    public IType findGeneratedJavaTypeForPolicyCmptType(boolean forInterface) throws CoreException {
        // JavaSourceFileBuilder policyCmptTypeBuilder = forInterface ?
        // getBuilderSet().getPolicyCmptInterfaceBuilder()
        // : getBuilderSet().getPolicyCmptImplClassBuilder();
        //
        // IPolicyCmptType policyCmptType =
        // getProductCmptType().findPolicyCmptType(getProductCmptType().getIpsProject());
        // if (policyCmptType == null) {
        // return null;
        // }
        // return policyCmptTypeBuilder.getGeneratedJavaTypes(policyCmptType).get(0);
        return null;
    }

}
