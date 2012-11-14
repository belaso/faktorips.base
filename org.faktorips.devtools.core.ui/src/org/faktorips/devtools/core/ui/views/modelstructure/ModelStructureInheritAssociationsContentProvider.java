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

package org.faktorips.devtools.core.ui.views.modelstructure;

import static org.faktorips.devtools.core.ui.views.modelstructure.AssociationComponentNode.newAssociationComponentNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;

public final class ModelStructureInheritAssociationsContentProvider extends AbstractModelStructureContentProvider {

    private List<ComponentNode> storedRootElements;
    private final AssociationType[] ASSOCIATION_TYPES = { AssociationType.AGGREGATION,
            AssociationType.COMPOSITION_MASTER_TO_DETAIL };

    @Override
    public Object getParent(Object element) {
        return ((ComponentNode)element).getParent();
    }

    @Override
    public boolean hasChildren(Object element) {
        Object[] children = this.getChildren(element);
        return children != null && children.length > 0;
    }

    @Override
    protected String getWaitingLabel() {
        return Messages.ModelStructure_waitingLabel;
    }

    @Override
    protected Object[] collectElements(Object inputElement, IProgressMonitor monitor) {
        // if input is an IType, alter it to the corresponding IIpsProject
        IIpsProject inputProject = null;
        if (inputElement instanceof IType) {
            monitor.beginTask(getWaitingLabel(), 2);
            inputProject = ((IType)inputElement).getIpsProject();
            List<IType> projectTypes = getProjectITypes(inputProject, ((IType)inputElement).getIpsObjectType());
            monitor.worked(1);
            Collection<IType> rootElementsForIType = getRootElementsForIType((IType)inputElement, projectTypes,
                    ToChildAssociationType.SELF, new ArrayList<IType>(), new ArrayList<PathElement>());
            storedRootElements = ComponentNode.encapsulateComponentTypes(rootElementsForIType, null, inputProject);
            Object[] rootElements = storedRootElements.toArray();
            monitor.worked(1);
            monitor.done();
            return rootElements;
        } else if (inputElement instanceof IIpsProject) {
            inputProject = (IIpsProject)inputElement;
            IIpsProject project = inputProject;

            monitor.beginTask(getWaitingLabel(), 3);
            List<IType> projectComponents;
            if (getShowTypeState() == ShowTypeState.SHOW_POLICIES) {
                projectComponents = getProjectITypes(project, IpsObjectType.POLICY_CMPT_TYPE);
            } else {
                projectComponents = getProjectITypes(project, IpsObjectType.PRODUCT_CMPT_TYPE);
            }
            monitor.worked(1);

            List<IType> derivedRootElements = computeDerivedRootElements(
                    getProjectSpecificITypes(projectComponents, project), projectComponents, project);
            monitor.worked(1);
            storedRootElements = ComponentNode.encapsulateComponentTypes(derivedRootElements, null, project);
            Object[] rootElements = storedRootElements.toArray();
            monitor.worked(1);
            monitor.done();
            return rootElements;
        } else {
            return null;
        }
    }

    /**
     * Computes the root-nodes for an {@link IType} element. The root-nodes are exactly those nodes
     * which have an outgoing association to the given element. An association is of Type
     * {@link AssociationType#COMPOSITION_MASTER_TO_DETAIL} or {@link AssociationType#AGGREGATION}.
     * A run of this method with an empty rootCandidates parameter will return a list of
     * root-candidates. A second run of this method with the previously obtained rootCandidates as
     * parameter, will remove false candidates from the list. The method calls itself recursively.
     * 
     * @param element the starting point
     * @param componentList the list of all concerned elements
     * @param association the
     *            {@link org.faktorips.devtools.core.ui.views.modelstructure.AbstractModelStructureContentProvider.ToChildAssociationType
     *            ToChildAssociationType} of the parent element to this element
     * @param rootCandidates a {@link Collection} of {@link IType}. elements
     * @param callHierarchy a {@link List} which contains the path from the current element to the
     *            source element
     */
    Collection<IType> getRootElementsForIType(IType element,
            List<IType> componentList,
            ToChildAssociationType association,
            Collection<IType> rootCandidates,
            List<PathElement> callHierarchy) {

        List<PathElement> callHierarchyTemp = new LinkedList<PathElement>(callHierarchy);

        PathElement pathElement = new PathElement(element, association);
        // If the element is already contained in the callHierarchy we have detected a cycle
        if (callHierarchy.contains(pathElement)) {
            return new HashSet<IType>();
        } else {
            callHierarchyTemp.add(0, pathElement);
        }

        Set<IType> rootElements = new HashSet<IType>();
        List<IType> associatingTypes = getAssociatingTypes(element, componentList, ASSOCIATION_TYPES);
        // Breaking condition
        if (associatingTypes.isEmpty()
                && (association == ToChildAssociationType.SELF || association == ToChildAssociationType.ASSOCIATION)) {
            rootElements.add(element);
        }

        // recursive call for all child elements
        for (IType associations : associatingTypes) {
            Collection<IType> rootElementsForIType = getRootElementsForIType(associations, componentList,
                    ToChildAssociationType.ASSOCIATION, rootCandidates, callHierarchyTemp);
            rootElements.addAll(rootElementsForIType);
        }

        // Add supertype if it has any aggregation or composition
        if (rootElements.isEmpty()
                && association == ToChildAssociationType.SUPERTYPE
                && !element.getAssociations(AssociationType.AGGREGATION, AssociationType.COMPOSITION_MASTER_TO_DETAIL)
                        .isEmpty()) {
            rootElements.add(element);
        }

        // None of the child elements is a root element, therefore check the association type of the
        // current element
        if (rootElements.isEmpty() && association == ToChildAssociationType.ASSOCIATION) {
            rootElements.add(element);
        }

        // If the hierarchy is solely build with supertypes, we must add the source element itself
        if (rootElements.isEmpty() && association == ToChildAssociationType.SELF) {
            rootElements.add(element);
        }

        return rootElements;
    }

    private List<IType> computeDerivedRootElements(List<IType> projectSpecificITypes,
            List<IType> allComponentITypes,
            IIpsProject project) {
        List<IType> rootCandidates = new ArrayList<IType>();

        for (IType projectType : projectSpecificITypes) {
            try {
                IType supertype = projectType.findSupertype(project);

                if ((supertype == null || !supertype.getIpsProject().equals(project))
                        && !isAssociated(projectType, projectSpecificITypes, allComponentITypes, project,
                                ASSOCIATION_TYPES)) {
                    rootCandidates.add(projectType);
                }
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        }

        // remove superfluous root elements
        List<IType> notRootElements = new ArrayList<IType>();
        for (IType rootCandidate : rootCandidates) {
            // remove element if it is associated by another project specific element
            if (isAssociated(rootCandidate, projectSpecificITypes, allComponentITypes, project)) {
                notRootElements.add(rootCandidate);
            }
        }
        rootCandidates.removeAll(notRootElements);
        return rootCandidates;
    }

    private List<IType> findProjectSpecificSubtypes(IType parent, IIpsProject project) {
        List<IType> rootElements = new ArrayList<IType>();
        List<IType> subtypes = parent.findSubtypes(false, false, project);
        boolean foundNothing = true;
        for (IType subtype : subtypes) {
            if (subtype.getIpsProject().equals(project)) { // root-element found
                rootElements.add(subtype);
                foundNothing = false;
            }
        }

        // perform a breadth-first-search on the subtype-hierarchy
        List<IType> sameLevelSubtypes = new ArrayList<IType>(subtypes);
        while (foundNothing) {
            List<IType> newSameLevelSubtypes = new ArrayList<IType>();
            for (IType subtype : sameLevelSubtypes) {
                newSameLevelSubtypes.addAll(subtype.findSubtypes(false, false, project));
            }
            for (IType sameLevelSubtype : sameLevelSubtypes) {
                if (sameLevelSubtype.getIpsProject().equals(project)) {
                    rootElements.add(sameLevelSubtype);
                    foundNothing = false;
                }
            }
            if (foundNothing && newSameLevelSubtypes.isEmpty()) {
                return newSameLevelSubtypes;
            } else {
                sameLevelSubtypes = newSameLevelSubtypes;
            }
        }
        return rootElements;
    }

    @Override
    protected List<SubtypeComponentNode> getComponentNodeSubtypeChildren(ComponentNode parent) {
        List<IType> subtypes = findProjectSpecificSubtypes(parent.getValue(), parent.getValue().getIpsProject());
        return SubtypeComponentNode.encapsulateSubtypeComponentTypes(subtypes, parent, parent.getValue()
                .getIpsProject());
    }

    @Override
    protected List<AssociationComponentNode> getComponentNodeAssociationChildren(ComponentNode parent) {

        List<AssociationComponentNode> associationNodes = new ArrayList<AssociationComponentNode>();

        IType parentType = parent.getValue();
        IIpsProject project = parent.getSourceIpsProject();

        // add direct associations (from the same project)
        associationNodes.addAll(getDirectAssociationComponentNodes(parent, project));

        try {
            /*
             * general root element or supertype is from the same project, therefore we have no
             * derived associations
             */
            if (parentType.findSupertype(project) == null) {
                if (!associationNodes.isEmpty()) {
                    return associationNodes;
                } else {
                    return null;
                }
            } else {
                // compute the derived associations -> go up in the inheritance hierarchy
                IType supertype = parentType.findSupertype(project);
                List<IAssociation> supertypeAssociations = new ArrayList<IAssociation>();
                // collect all relevant supertype-associations
                while (supertype != null) {
                    supertypeAssociations.addAll(0, supertype.getAssociations(ASSOCIATION_TYPES));
                    supertype = supertype.findSupertype(project);
                }

                ArrayList<AssociationComponentNode> superAssociationNodes = new ArrayList<AssociationComponentNode>();
                for (IAssociation supertypeAssociation : supertypeAssociations) {
                    if (!supertypeAssociation.isDerivedUnion()) {
                        IType target = supertypeAssociation.findTarget(project);
                        AssociationComponentNode associationComponentNode = newAssociationComponentNode(target,
                                supertypeAssociation, parent, project);
                        associationComponentNode.setInherited(true);
                        superAssociationNodes.add(associationComponentNode);
                    }
                }
                associationNodes.addAll(0, superAssociationNodes);
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }

        if (!associationNodes.isEmpty()) {
            return associationNodes;
        }
        return null;
    }

    private List<AssociationComponentNode> getDirectAssociationComponentNodes(ComponentNode parent, IIpsProject project) {
        IType parentValue = parent.getValue();
        List<IAssociation> directAssociations = new ArrayList<IAssociation>();
        for (IAssociation directAssociation : parentValue.getAssociations(ASSOCIATION_TYPES)) {
            try {
                if (directAssociation.findTarget(project).getIpsProject().equals(project)) {
                    directAssociations.add(directAssociation);
                }
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        }

        List<AssociationComponentNode> componentNodes = new ArrayList<AssociationComponentNode>();
        for (IAssociation association : directAssociations) {
            componentNodes.add(AssociationComponentNode.newAssociationComponentNode(association, parent, project));
        }
        return componentNodes;
    }

    @Override
    protected List<ComponentNode> getStoredRootElements() {
        return storedRootElements;
    }
}
