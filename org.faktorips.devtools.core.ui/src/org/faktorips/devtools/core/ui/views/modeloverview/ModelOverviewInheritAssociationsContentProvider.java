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

package org.faktorips.devtools.core.ui.views.modeloverview;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;

public class ModelOverviewInheritAssociationsContentProvider extends AbstractModelOverviewContentProvider {

    private final AssociationType[] ASSOCIATION_TYPES = { AssociationType.AGGREGATION,
            AssociationType.COMPOSITION_MASTER_TO_DETAIL };

    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof ComponentNode) {
            return getComponentNodeChildren((ComponentNode)parentElement).toArray();
        } else if (parentElement instanceof AbstractStructureNode) {
            return ((AbstractStructureNode)parentElement).getChildren().toArray();
        }
        return new Object[0];
    }

    @Override
    public Object getParent(Object element) {
        return ((IModelOverviewNode)element).getParent();
    }

    @Override
    public boolean hasChildren(Object element) {
        if (element instanceof AbstractStructureNode) {
            return !((AbstractStructureNode)element).getChildren().isEmpty();
        } else if (element instanceof ComponentNode) {
            return !getComponentNodeChildren((ComponentNode)element).isEmpty();
        }
        return false;
    }

    @Override
    protected String getWaitingLabel() {
        return Messages.IpsModelOverview_waitingLabel;
    }

    @Override
    protected Object[] collectElements(Object inputElement, IProgressMonitor monitor) {
        // only accept project input
        if (inputElement instanceof IIpsProject) {
            IIpsProject project = (IIpsProject)inputElement;

            List<IType> overallRootElements;
            if (showState == ShowTypeState.SHOW_POLICIES) {
                overallRootElements = getProjectRootElementsFromComponentList(
                        getProjectITypes(project, IpsObjectType.POLICY_CMPT_TYPE), project, ASSOCIATION_TYPES);
            } else {
                overallRootElements = getProjectRootElementsFromComponentList(
                        getProjectITypes(project, IpsObjectType.PRODUCT_CMPT_TYPE), project, ASSOCIATION_TYPES);
            }

            List<IType> derivedRootElements = computeDerivedRootElements(overallRootElements, project);
            return ComponentNode.encapsulateComponentTypes(derivedRootElements, project).toArray();
        } else {
            return null;
        }
    }

    private List<IType> computeDerivedRootElements(List<IType> overallRootElements, IIpsProject project) {
        List<IType> rootElements = new ArrayList<IType>();

        // at first check the overallRootElements themselves
        for (IType overallRootElement : overallRootElements) {
            if (overallRootElement.getIpsProject().equals(project)) {
                rootElements.add(overallRootElement);
            } else {
                // dig through the supertype hierarchy and construct the new root elements
                rootElements.addAll(findProjectSpecificSubtypes(overallRootElement, project));
            }
        }
        return rootElements;
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

    /**
     * Takes a {@link List} of {@link IType} and extracts all elements which are contained in the
     * provided {@link IIpsProject} into a new list.
     * 
     * @param components a list of {@link IType}
     * @param project the project for which the {@link IType}s should be retrieved
     * @return a {@link List} of {@link IType}, or an empty list if no provided elements are
     *         contained in this project
     */
    protected static List<IType> getProjectSpecificITypes(List<IType> components, IIpsProject project) {
        List<IType> projectComponents = new ArrayList<IType>();
        for (IType iType : components) {
            if (iType.getIpsProject().getName().equals(project.getName())) {
                projectComponents.add(iType);
            }
        }
        return projectComponents;
    }

    @Override
    List<AbstractStructureNode> getComponentNodeChildren(ComponentNode parent) {
        List<AbstractStructureNode> children = new ArrayList<AbstractStructureNode>();

        SubtypeNode subtypeNode = getComponentNodeSubtypeChild(parent);
        if (subtypeNode != null) {
            children.add(subtypeNode);
        }

        CompositeNode compositeNode = getComponentNodeCompositeChild(parent);
        if (compositeNode != null) {
            children.add(compositeNode);
        }

        return children;
    }

    @Override
    SubtypeNode getComponentNodeSubtypeChild(ComponentNode parent) {
        List<IType> subtypes = findProjectSpecificSubtypes(parent.getValue(), parent.getValue().getIpsProject());
        if (!subtypes.isEmpty()) {
            return new SubtypeNode(parent, ComponentNode.encapsulateComponentTypes(subtypes, parent.getValue()
                    .getIpsProject()));
        }
        return null;
    }

    @Override
    CompositeNode getComponentNodeCompositeChild(ComponentNode parent) {

        List<IType> derivedAssociations = new ArrayList<IType>();
        List<ComponentNode> associationNodes = new ArrayList<ComponentNode>();

        IType parentValue = parent.getValue();
        IIpsProject project = parentValue.getIpsProject();
        // add direct associations (from the same project)
        associationNodes.addAll(getDirectAssociationComponentNodes(parentValue, project));

        // general root element or supertype is from the same project, therefore we have no derived
        // associations
        try {
            if (parentValue.findSupertype(project) == null
                    || parentValue.findSupertype(project).getIpsProject().equals(project)) {
                if (!associationNodes.isEmpty()) {
                    return new CompositeNode(parent, associationNodes);
                } else {
                    return null;
                }
            } else {
                // compute the derived associations -> go up in the inheritance hierarchy
                IType supertype = parentValue.findSupertype(project);
                List<IType> supertypeAssociations = new ArrayList<IType>();
                // collect all relevant supertype-associations
                while (supertype != null && !supertype.getIpsProject().equals(project)) {
                    supertypeAssociations.addAll(getAssociationsForAssociationTypes(supertype, ASSOCIATION_TYPES));
                    supertype = supertype.findSupertype(project);
                }

                for (IType supertypeAssociation : supertypeAssociations) {
                    derivedAssociations.addAll(findProjectSpecificSubtypes(supertypeAssociation, project));
                }
                associationNodes.addAll(InheritedAssociationComponentNode
                        .encapsulateInheritedAssociationComponentTypes(derivedAssociations, project));
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }

        if (!associationNodes.isEmpty()) {
            return new CompositeNode(parent, associationNodes);
        }
        return null;
    }

    private List<AssociationComponentNode> getDirectAssociationComponentNodes(IType parentValue, IIpsProject project) {
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
        return AssociationComponentNode.encapsulateAssociationComponentTypes(directAssociations, project);
    }
}
