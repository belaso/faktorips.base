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

package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.faktorips.devtools.core.internal.model.ipsobject.IpsObject;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.helper.filter.IpsElementFilter;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextType;

/**
 * Lists and links the packages of the given {@link IpsObject}s in a page
 * 
 * @author dicker
 * 
 */
public class IpsPackagesListPageElement extends AbstractListPageElement {

    /**
     * Comparator, which support the sorting of packages by name
     */
    private Comparator<IIpsSrcFile> packagesComparator = new Comparator<IIpsSrcFile>() {
        @Override
        public int compare(IIpsSrcFile arg0, IIpsSrcFile arg1) {
            return arg0.getIpsPackageFragment().getName().compareTo(arg1.getIpsPackageFragment().getName());
        }
    };

    /**
     * @see AbstractListPageElement
     */
    public IpsPackagesListPageElement(IIpsElement baseIpsElement, List<IIpsSrcFile> srcFiles, IpsElementFilter filter,
            DocumentationContext context) {
        super(baseIpsElement, srcFiles, filter, context);
        setTitle(Messages.IpsPackagesListPageElement_allPackages);
    }

    /**
     * @see AbstractListPageElement
     */
    public IpsPackagesListPageElement(IIpsElement baseIpsElement, List<IIpsSrcFile> srcFiles,
            DocumentationContext context) {
        this(baseIpsElement, srcFiles, ALL_FILTER, context);
    }

    @Override
    public void build() {
        super.build();
        addPageElements(new TextPageElement(getTitle(), TextType.HEADING_2));

        List<PageElement> list = createPackageList();

        addPageElements(new TextPageElement(
                list.size() + " " + Messages.IpsPackagesListPageElement_packages, TextType.BLOCK)); //$NON-NLS-1$

        if (list.size() > 0) {
            addPageElements(new ListPageElement(list));
        }
    }

    private List<PageElement> createPackageList() {

        Collections.sort(srcFiles, packagesComparator);

        Set<IIpsPackageFragment> packageFragments = getRelatedPackageFragments();

        List<PageElement> packageLinks = new ArrayList<PageElement>();
        Set<String> linkedPackagesNames = new HashSet<String>();

        for (IIpsPackageFragment packageFragment : packageFragments) {
            if (!filter.accept(packageFragment)) {
                continue;
            }
            if (linkedPackagesNames.contains(packageFragment.getName())) {
                continue;
            }

            linkedPackagesNames.add(packageFragment.getName());
            packageLinks.add(PageElementUtils.createLinkPageElement(getContext(), packageFragment, getLinkTarget(),
                    IpsUIPlugin.getLabel(packageFragment), true));
        }

        return packageLinks;
    }
}
