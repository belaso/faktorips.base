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

package org.faktorips.devtools.core.builder;

import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfig;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.ClassToInstancesMap;

/**
 * Abstract implementation that can be used as a base class for real builder sets.
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractBuilderSet implements IIpsArtefactBuilderSet {

    /**
     * Configuration property for this builder. IIpsArtefactBuilderSets that use this builder can
     * provide values of this property via the IIpsArtefactBuilderSetConfig object that is provided
     * by the initialize method of an IIpsArtefactBuilderSet.
     */
    public final static String CONFIG_PROPERTY_GENERATOR_LOCALE = "generatorLocale"; //$NON-NLS-1$

    /**
     * Configuration property setting that none mergeable filed should be marked as derived filed.
     */
    public final static String CONFIG_MARK_NONE_MERGEABLE_RESOURCES_AS_DERIVED = "markNoneMergeableResourcesAsDerived"; //$NON-NLS-1$

    private String id;
    private String label;
    private IIpsProject ipsProject;
    private IIpsArtefactBuilderSetConfig config;
    private ClassToInstancesMap<IIpsArtefactBuilder> builders;

    public AbstractBuilderSet() {
        super();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public IIpsProject getIpsProject() {
        return ipsProject;
    }

    @Override
    public void setIpsProject(IIpsProject ipsProject) {
        this.ipsProject = ipsProject;
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public boolean containsAggregateRootBuilder() {
        return false;
    }

    /**
     * Default implementation returns <code>false</code>. {@inheritDoc}
     */
    @Override
    public boolean isInverseRelationLinkRequiredFor2WayCompositions() {
        return false;
    }

    /**
     * Default implementation returns <code>false</code>. {@inheritDoc}
     */
    @Override
    public boolean isRoleNamePluralRequiredForTo1Relations() {
        return false;
    }

    @Override
    public IIpsArtefactBuilder[] getArtefactBuilders() {
        return builders.values().toArray(new IIpsArtefactBuilder[builders.values().size()]);
    }

    @Override
    public IIpsArtefactBuilderSetConfig getConfig() {
        return config;
    }

    @Override
    public void initialize(IIpsArtefactBuilderSetConfig config) throws CoreException {
        ArgumentCheck.notNull(config);
        this.config = config;
        builders = createBuilders();
    }

    @Override
    public Locale getLanguageUsedInGeneratedSourceCode() {
        String localeString = getConfig().getPropertyValueAsString(CONFIG_PROPERTY_GENERATOR_LOCALE);
        if (localeString == null) {
            return Locale.ENGLISH;
        }
        return getLocale(localeString);
    }

    public static Locale getLocale(String s) {
        StringTokenizer tokenzier = new StringTokenizer(s, "_"); //$NON-NLS-1$
        if (!tokenzier.hasMoreTokens()) {
            return Locale.ENGLISH;
        }
        String language = tokenzier.nextToken();
        if (!tokenzier.hasMoreTokens()) {
            return new Locale(language);
        }
        String country = tokenzier.nextToken();
        if (!tokenzier.hasMoreTokens()) {
            return new Locale(language, country);
        }
        String variant = tokenzier.nextToken();
        return new Locale(language, country, variant);
    }

    /**
     * Template method to create the set's builders.
     */
    protected abstract ClassToInstancesMap<IIpsArtefactBuilder> createBuilders() throws CoreException;

    @Override
    public void afterBuildProcess(int buildKind) throws CoreException {
        // could be implemented in subclass
    }

    @Override
    public void beforeBuildProcess(int buildKind) throws CoreException {
        // could be implemented in subclass
    }

    @Override
    public <T extends IIpsArtefactBuilder> T getBuilderByClass(Class<T> builderClass) {
        if (builders == null) {
            throw new IllegalStateException("No builders initialized yet"); //$NON-NLS-1$
        }
        List<T> buildersByClass = builders.get(builderClass);
        if (buildersByClass.size() == 1) {
            return buildersByClass.get(0);
        } else if (buildersByClass.size() > 1) {
            throw new RuntimeException(
                    "Only one builder per class is allowed by this builder set: " + builderClass.getName() + " is " + buildersByClass.size() + " times registered"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        } else {
            throw new RuntimeException("No builder registerd for: " + builderClass.getName()); //$NON-NLS-1$
        }
    }

    @Override
    public boolean isTableBasedEnumValidationRequired() {
        return true;
    }

    @Override
    public boolean isPersistentProviderSupportConverter() {
        return false;
    }

    @Override
    public boolean isPersistentProviderSupportOrphanRemoval() {
        return false;
    }

    @Override
    public boolean isMarkNoneMergableResourcesAsDerived() {
        Boolean propertyValueAsBoolean = getConfig().getPropertyValueAsBoolean(
                CONFIG_MARK_NONE_MERGEABLE_RESOURCES_AS_DERIVED);
        if (propertyValueAsBoolean != null) {
            return propertyValueAsBoolean;
        } else {
            return true;
        }
    }

    @Override
    public void clean(IProgressMonitor monitor) {
        // default implementation does nothing
    }

}
