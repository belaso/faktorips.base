/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.builder.DefaultJavaSourceFileBuilder;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.util.LocalizedStringsSet;

/**
 * Abstract Java source file builder that belongs to the Faktor-IPS standard code generator and
 * implements the {@link IIpsStandardArtefactBuilder} interface. All builders of the standard
 * builder set generating Java source code should inherit from this class.
 * 
 * @author Jan Ortmann
 */
public abstract class StandardJavaSourceFileBuilder extends DefaultJavaSourceFileBuilder implements
        IIpsStandardArtefactBuilder {

    private boolean buildsPublishedArtefacts;

    public StandardJavaSourceFileBuilder(IIpsArtefactBuilderSet builderSet, boolean buildsPublishedArtefacts,
            LocalizedStringsSet localizedStringsSet) {
        super(builderSet, "", localizedStringsSet);
        this.buildsPublishedArtefacts = buildsPublishedArtefacts;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean buildsPublishedArtefacts() {
        return buildsPublishedArtefacts;
    }

    @Override
    public boolean buildsDerivedArtefacts() {
        return !isMergeEnabled();
    }

    /**
     * Returns the qualified name of the Java class generated by this builder for the ips object
     * stored in the given ips source file.
     * 
     * @param ipsSrcFile the ips source file.
     * @return the qualified class name
     * 
     * @throws CoreException is delegated from calls to other methods
     */
    @Override
    public String getQualifiedClassName(IIpsSrcFile ipsSrcFile) throws CoreException {
        if (getBuilderSet() instanceof StandardBuilderSet) {
            StandardBuilderSet standardBuilderSet = (StandardBuilderSet)getBuilderSet();
            return standardBuilderSet.getPackageNameForGeneratedArtefacts(this, ipsSrcFile);
        }
        throw new CoreException(
                new IpsStatus(
                        "The standard builders can only be used with the standard builder set, or with a build set that inherits from the standard builder set!"));
    }

}
