/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.pctype;

import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsObjectPath;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IMethod;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.pctype.ITableStructureUsage;
import org.faktorips.devtools.core.model.pctype.ITypeHierarchy;


/**
 *
 */
public class TypeHierarchyTest extends AbstractIpsPluginTest {

    private IIpsProject pdProject;
    private IIpsPackageFragmentRoot pdRootFolder;
    private IIpsPackageFragment pdFolder;
    private IIpsSrcFile pdSrcFile;
    private PolicyCmptType pcType;
    private IPolicyCmptType supertype;
    private IPolicyCmptType supersupertype;

    /*
     * @see PluginTest#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        pdProject = this.newIpsProject("TestProject");
        pdRootFolder = pdProject.getIpsPackageFragmentRoots()[0];
        pdFolder = pdRootFolder.createPackageFragment("products.folder", true, null);
        pdSrcFile = pdFolder.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "TestPolicy", true, null);
        pcType = (PolicyCmptType)pdSrcFile.getIpsObject();

        // create two more types that act as supertype and supertype's supertype 
        IIpsSrcFile file1 = pdFolder.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "Supertype", true, null);
        supertype = (PolicyCmptType)file1.getIpsObject();
        IIpsSrcFile file2 = pdFolder.createIpsFile(IpsObjectType.POLICY_CMPT_TYPE, "Supersupertype", true, null);
        supersupertype = (PolicyCmptType)file2.getIpsObject();
    }

    public void testGetSubtypeHierarchy() throws Exception {
        pcType.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());

        ITypeHierarchy hierarchy = TypeHierarchy.getSubtypeHierarchy(supersupertype);
        assertFalse(hierarchy.containsCycle());
        
        assertNull(hierarchy.getSupertype(supersupertype));
        assertEquals(1, hierarchy.getSubtypes(supersupertype).length);
        assertEquals(supertype, hierarchy.getSubtypes(supersupertype)[0]);
        
        assertEquals(supersupertype, hierarchy.getSupertype(supertype));
        assertEquals(1, hierarchy.getSubtypes(supertype).length);
        assertEquals(pcType, hierarchy.getSubtypes(supertype)[0]);

        assertEquals(supertype, hierarchy.getSupertype(pcType));
        assertEquals(0, hierarchy.getSubtypes(pcType).length);
        
        // test if it works if the subtypes are in different projects
        IIpsProject project2 = newIpsProject("Project2");
        IPolicyCmptType newSubType = newPolicyCmptType(project2, "NewSubType");
        newSubType.setSupertype(pcType.getQualifiedName());
        
        IIpsProject project3 = newIpsProject("Project3");
        IPolicyCmptType newSubSubType = newPolicyCmptType(project3, "NewSubSubType");
        newSubSubType.setSupertype(newSubType.getQualifiedName());
        
        // no project dependencies => nothing could be found
        hierarchy = TypeHierarchy.getSubtypeHierarchy(pcType);
        assertEquals(0, hierarchy.getSubtypes(pcType).length);

        // setup project dependencies
        IIpsObjectPath path = project2.getIpsObjectPath();
        path.newIpsProjectRefEntry(pdProject);
        project2.setIpsObjectPath(path);
        path = project3.getIpsObjectPath();
        path.newIpsProjectRefEntry(project2);
        project3.setIpsObjectPath(path);
        
        hierarchy = TypeHierarchy.getSubtypeHierarchy(pcType);
        assertEquals(1, hierarchy.getSubtypes(pcType).length);
        assertEquals(newSubType, hierarchy.getSubtypes(pcType)[0]);
        assertEquals(1, hierarchy.getSubtypes(newSubType).length);
        assertEquals(newSubSubType, hierarchy.getSubtypes(newSubType)[0]);
    }

    public void testIsSubtypeOf_SupertypeHierarchy() throws Exception {
        pcType.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());

        ITypeHierarchy hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        assertTrue(hierarchy.isSubtypeOf(supertype, supersupertype));
        assertTrue(hierarchy.isSubtypeOf(pcType, supersupertype));
        assertFalse(hierarchy.isSubtypeOf(supertype, pcType));
        assertFalse(hierarchy.isSubtypeOf(supertype, null));
        assertFalse(hierarchy.isSubtypeOf(null, supertype));
        
        // now same with cycle in hierarchy 
        supersupertype.setSupertype(pcType.getQualifiedName());
        hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        assertTrue(hierarchy.isSubtypeOf(supertype, supersupertype));
        assertTrue(hierarchy.isSubtypeOf(pcType, supersupertype));
        assertFalse(hierarchy.isSubtypeOf(supertype, pcType));
        assertFalse(hierarchy.isSubtypeOf(supertype, null));
        assertFalse(hierarchy.isSubtypeOf(null, supertype));
    }
        
    public void testIsSubtypeOf_SubtypeHierarchy() throws Exception {
        pcType.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());

        ITypeHierarchy hierarchy = TypeHierarchy.getSubtypeHierarchy(supersupertype);
        assertTrue(hierarchy.isSubtypeOf(supertype, supersupertype));
        assertTrue(hierarchy.isSubtypeOf(pcType, supersupertype));
        assertFalse(hierarchy.isSubtypeOf(supertype, pcType));
        assertFalse(hierarchy.isSubtypeOf(supertype, null));
        assertFalse(hierarchy.isSubtypeOf(null, supertype));
        
        // now same with cycle in hierarchy 
        supersupertype.setSupertype(pcType.getQualifiedName());
        hierarchy = TypeHierarchy.getSubtypeHierarchy(supersupertype);
        assertTrue(hierarchy.isSubtypeOf(supertype, supersupertype));
        assertTrue(hierarchy.isSubtypeOf(pcType, supersupertype));
        assertFalse(hierarchy.isSubtypeOf(supertype, pcType));
        assertFalse(hierarchy.isSubtypeOf(supertype, null));
        assertFalse(hierarchy.isSubtypeOf(null, supertype));
    }
        

    public void testGetSupertype() throws Exception {

        ITypeHierarchy hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        assertNull(hierarchy.getSupertype(pcType)); // supertype relationship hasn't been established
        assertNull(hierarchy.getSupertype(supertype));
        assertEquals(0, hierarchy.getSubtypes(pcType).length);
        
        // create the supertype relations
        pcType.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());
        
        hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        assertEquals(supertype, hierarchy.getSupertype(pcType));
        assertEquals(supersupertype, hierarchy.getSupertype(supertype));
        assertNull(hierarchy.getSupertype(supersupertype));
        assertEquals(0, hierarchy.getSubtypes(pcType).length);
        
        // now same with cycle in hierarchy 
        supersupertype.setSupertype(pcType.getQualifiedName());
        hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        assertEquals(supertype, hierarchy.getSupertype(pcType));
        assertEquals(supersupertype, hierarchy.getSupertype(supertype));
        assertNull(hierarchy.getSupertype(supersupertype));
        assertEquals(0, hierarchy.getSubtypes(pcType).length);
        
    }

    public void testGetAllSupertypes() throws Exception {
        ITypeHierarchy hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        assertEquals(0, hierarchy.getAllSupertypes(pcType).length); // supertype relationship hasn't been established
        
        // create the supetype relations
        pcType.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());
        
        hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        IPolicyCmptType[] supertypes = hierarchy.getAllSupertypes(pcType); 
        assertEquals(2, supertypes.length);
        assertEquals(supertype, supertypes[0]);
        assertEquals(supersupertype, supertypes[1]);
        
        // now same with cycle in hierarchy 
        supersupertype.setSupertype(pcType.getQualifiedName());
        hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        supertypes = hierarchy.getAllSupertypes(pcType); 
        assertEquals(2, supertypes.length);
        assertEquals(supertype, supertypes[0]);
        assertEquals(supersupertype, supertypes[1]);
    }

    public void testGetAllSupertypesInclSelf() throws Exception {
        ITypeHierarchy hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        assertEquals(1, hierarchy.getAllSupertypesInclSelf(pcType).length); // supertype relationship hasn't been established
        assertEquals(pcType, hierarchy.getAllSupertypesInclSelf(pcType)[0]);
        
        // create the supetype relations
        pcType.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());
        
        hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        IPolicyCmptType[] supertypes = hierarchy.getAllSupertypesInclSelf(pcType); 
        assertEquals(3, supertypes.length);
        assertEquals(pcType, supertypes[0]);
        assertEquals(supertype, supertypes[1]);
        assertEquals(supersupertype, supertypes[2]);

        // now same with cycle in hierarchy 
        supersupertype.setSupertype(pcType.getQualifiedName());
        hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        supertypes = hierarchy.getAllSupertypesInclSelf(pcType); 
        assertEquals(3, supertypes.length);
        assertEquals(pcType, supertypes[0]);
        assertEquals(supertype, supertypes[1]);
        assertEquals(supersupertype, supertypes[2]);
    }
    
    public void testGetSubtypes() throws Exception {
        ITypeHierarchy hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        assertEquals(0, hierarchy.getSubtypes(pcType).length);
        
    }
    
    public void testIsSupertypeOf() throws Exception {
        ITypeHierarchy hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        assertFalse(hierarchy.isSupertypeOf(supertype, pcType)); // supertype relationship hasn't been established
        
        // create the supetype relations
        pcType.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());
        
        hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        assertTrue(hierarchy.isSupertypeOf(supertype, pcType));
        assertTrue(hierarchy.isSupertypeOf(supersupertype, pcType));

        assertFalse(hierarchy.isSupertypeOf(supertype, null));
        assertFalse(hierarchy.isSupertypeOf(null, pcType));
        
        // now same with cycle in hierarchy 
        supersupertype.setSupertype(pcType.getQualifiedName());
        hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        assertTrue(hierarchy.isSupertypeOf(supertype, pcType));
        assertTrue(hierarchy.isSupertypeOf(supersupertype, pcType));

        assertFalse(hierarchy.isSupertypeOf(supertype, null));
        assertFalse(hierarchy.isSupertypeOf(null, pcType));

        
    }
    
    public void testGetAllAttributes() throws Exception {
        // create the supetype relations
        pcType.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());

        IAttribute a1 = pcType.newAttribute();
        IAttribute a2 = supertype.newAttribute();
        IAttribute a3 = supersupertype.newAttribute();
        TypeHierarchy hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        IAttribute[] attributes = hierarchy.getAllAttributes(pcType);
        assertEquals(3, attributes.length);
        assertEquals(a1, attributes[0]);
        assertEquals(a2, attributes[1]);
        assertEquals(a3, attributes[2]);
        
        // now same with cycle in hierarchy 
        supersupertype.setSupertype(pcType.getQualifiedName());
        hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        attributes = hierarchy.getAllAttributes(pcType);
        assertEquals(3, attributes.length);
        assertEquals(a1, attributes[0]);
        assertEquals(a2, attributes[1]);
        assertEquals(a3, attributes[2]);
        
    }

    public void testGetAllAttributesRespectingOverride() throws Exception {
        pcType.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());

        // no attribute overridden
        IAttribute a1 = pcType.newAttribute();
        a1.setName("a");
        IAttribute a2 = supertype.newAttribute();
        a2.setName("b");
        IAttribute a3 = supersupertype.newAttribute();
        a3.setName("c");
        
        TypeHierarchy hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        IAttribute[] attributes = hierarchy.getAllAttributesRespectingOverride(pcType);
        assertEquals(3, attributes.length);
        
        // a1 overrides a2, a3 not overridden
        a1.setName("b");
        a1.setOverwrites(true);

        attributes = hierarchy.getAllAttributesRespectingOverride(pcType);
        assertEquals(2, attributes.length);
        assertEquals(a1, attributes[0]);
        assertEquals(a3, attributes[1]);
        
        // a1 overrides a2, a2 overrides a3
        a3.setName("b");
        a2.setOverwrites(true);

        attributes = hierarchy.getAllAttributesRespectingOverride(pcType);
        assertEquals(1, attributes.length);
        assertEquals(a1, attributes[0]);

        // a2 overrides a3, a1 not overridden nor overriding.
        a1.setName("x");
        a1.setOverwrites(false);
        
        attributes = hierarchy.getAllAttributesRespectingOverride(pcType);
        assertEquals(2, attributes.length);
        assertEquals(a1, attributes[0]);
        assertEquals(a2, attributes[1]);
        
        // now same with cycle in hierarchy 
        supersupertype.setSupertype(pcType.getQualifiedName());
        hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        attributes = hierarchy.getAllAttributesRespectingOverride(pcType);
        assertEquals(2, attributes.length);
        assertEquals(a1, attributes[0]);
        assertEquals(a2, attributes[1]);
    }
    
    public void testGetAllMethods() throws Exception {
        // create the supetype relations
        pcType.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());

        IMethod m1 = pcType.newMethod();
        IMethod m2 = supertype.newMethod();
        IMethod m3 = supersupertype.newMethod();
        TypeHierarchy hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        IMethod[] methods = hierarchy.getAllMethods(pcType);
        assertEquals(3, methods.length);
        assertEquals(m1, methods[0]);
        assertEquals(m2, methods[1]);
        assertEquals(m3, methods[2]);
        
        // now same with cycle in hierarchy 
        supersupertype.setSupertype(pcType.getQualifiedName());
        hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        methods = hierarchy.getAllMethods(pcType);
        assertEquals(3, methods.length);
        assertEquals(m1, methods[0]);
        assertEquals(m2, methods[1]);
        assertEquals(m3, methods[2]);
        
    }
    
    public void testGetTableStructureUsages() throws Exception {
        // create the supertype 
        pcType.setConfigurableByProductCmptType(true);
        supertype.setConfigurableByProductCmptType(true);
        supersupertype.setConfigurableByProductCmptType(true);
        pcType.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());

        ITableStructureUsage tsu1 = pcType.newTableStructureUsage();
        tsu1.setRoleName("role1");
        ITableStructureUsage tsu2 = supertype.newTableStructureUsage();
        tsu2.setRoleName("role2");
        ITableStructureUsage tsu3 = supersupertype.newTableStructureUsage();
        tsu3.setRoleName("role3");
        
        TypeHierarchy hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        ITableStructureUsage[] tableStructureUsages = hierarchy.getAllTableStructureUsages(pcType);
        assertEquals(3, tableStructureUsages.length);
        assertEquals(tsu1, tableStructureUsages[0]);
        assertEquals(tsu2, tableStructureUsages[1]);
        assertEquals(tsu3, tableStructureUsages[2]);
        
        // now same with cycle in hierarchy 
        supersupertype.setSupertype(pcType.getQualifiedName());
        hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        tableStructureUsages = hierarchy.getAllTableStructureUsages(pcType);
        assertEquals(3, tableStructureUsages.length);
        assertEquals(tsu1, tableStructureUsages[0]);
        assertEquals(tsu2, tableStructureUsages[1]);
        assertEquals(tsu3, tableStructureUsages[2]);
        
    }

    public void testFindAttribute() throws Exception {
        // create the supetype relations
        pcType.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());

        IAttribute a1 = pcType.newAttribute();
        a1.setName("a1");
        IAttribute a2 = supersupertype.newAttribute();
        a2.setName("a2");
        
        TypeHierarchy hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        assertEquals(a1, hierarchy.findAttribute(pcType, "a1"));
        assertEquals(a2, hierarchy.findAttribute(pcType, "a2"));
        assertEquals(a2, hierarchy.findAttribute(supertype, "a2"));
        assertNull(hierarchy.findAttribute(pcType, "unkown"));
        
        // now same with cycle in hierarchy 
        supersupertype.setSupertype(pcType.getQualifiedName());
        hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        assertEquals(a1, hierarchy.findAttribute(pcType, "a1"));
        assertEquals(a2, hierarchy.findAttribute(pcType, "a2"));
        assertEquals(a2, hierarchy.findAttribute(supertype, "a2"));
        assertNull(hierarchy.findAttribute(pcType, "unkown"));
        
    }
    
    public void testFindRelation() throws Exception {
        // create the supetype relations
        pcType.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());

        IRelation r1 = pcType.newRelation();
        r1.setTargetRoleSingular("r1");
        IRelation r2 = supersupertype.newRelation();
        r2.setTargetRoleSingular("r2");
        
        TypeHierarchy hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        assertEquals(r1, hierarchy.findRelation(pcType, "r1"));
        assertEquals(r2, hierarchy.findRelation(pcType, "r2"));
        assertEquals(r2, hierarchy.findRelation(supertype, "r2"));
        assertNull(hierarchy.findRelation(pcType, "unkown"));
        
        // now same with cycle in hierarchy 
        supersupertype.setSupertype(pcType.getQualifiedName());
        hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        assertEquals(r1, hierarchy.findRelation(pcType, "r1"));
        assertEquals(r2, hierarchy.findRelation(pcType, "r2"));
        assertEquals(r2, hierarchy.findRelation(supertype, "r2"));
        assertNull(hierarchy.findRelation(pcType, "unkown"));
        
    }
    
    public void testFindTableStructureUsage() throws Exception {
        // create the supertype
        pcType.setConfigurableByProductCmptType(true);
        supertype.setConfigurableByProductCmptType(true);
        supersupertype.setConfigurableByProductCmptType(true);
        pcType.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());

        ITableStructureUsage tsu1 = pcType.newTableStructureUsage();
        tsu1.setRoleName("role1");
        ITableStructureUsage tsu2 = supertype.newTableStructureUsage();
        tsu2.setRoleName("role2");
        ITableStructureUsage tsu3 = supersupertype.newTableStructureUsage();
        tsu3.setRoleName("role3");
        
        TypeHierarchy hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        assertEquals(tsu3, hierarchy.findTableStructureUsage(pcType, "role3"));
        assertEquals(tsu2, hierarchy.findTableStructureUsage(pcType, "role2"));
        assertEquals(tsu1, hierarchy.findTableStructureUsage(pcType, "role1"));
        assertNull(hierarchy.findTableStructureUsage(supersupertype, "role1"));
        
        // now same with cycle in hierarchy 
        supersupertype.setSupertype(pcType.getQualifiedName());
        hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        assertEquals(tsu3, hierarchy.findTableStructureUsage(pcType, "role3"));
        assertEquals(tsu2, hierarchy.findTableStructureUsage(pcType, "role2"));
        assertEquals(tsu1, hierarchy.findTableStructureUsage(pcType, "role1"));
        assertNull(hierarchy.findTableStructureUsage(supersupertype, "role1"));
        
    }
    
    public void testGetAllRelations() throws Exception {
        // create the supetype relations
        pcType.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());

        IRelation r1 = pcType.newRelation();
        IRelation r2 = supertype.newRelation();
        IRelation r3 = supersupertype.newRelation();
        TypeHierarchy hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        IRelation[] relations = hierarchy.getAllRelations(pcType);
        assertEquals(3, relations.length);
        assertEquals(r1, relations[0]);
        assertEquals(r2, relations[1]);
        assertEquals(r3, relations[2]);
        
        // now same with cycle in hierarchy 
        supersupertype.setSupertype(pcType.getQualifiedName());
        hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        relations = hierarchy.getAllRelations(pcType);
        assertEquals(3, relations.length);
        assertEquals(r1, relations[0]);
        assertEquals(r2, relations[1]);
        assertEquals(r3, relations[2]);
        
    }

    public void testCycleDetection() throws Exception {
        pcType.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());

    	ITypeHierarchy hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
    	assertFalse(hierarchy.containsCycle());
        
        hierarchy = TypeHierarchy.getSubtypeHierarchy(supersupertype);    	
        assertFalse(hierarchy.containsCycle());
        
    	supersupertype.setSupertype(pcType.getQualifiedName());
        
        hierarchy = TypeHierarchy.getSupertypeHierarchy(pcType);
        assertTrue(hierarchy.containsCycle());

        hierarchy = TypeHierarchy.getSubtypeHierarchy(supersupertype);
        assertTrue(hierarchy.containsCycle());
    }
    
    public void testGetAllSubtypes() throws Exception {
        pcType.setSupertype(supertype.getQualifiedName());
        supertype.setSupertype(supersupertype.getQualifiedName());

        TypeHierarchy hierarchy = TypeHierarchy.getSubtypeHierarchy(supersupertype);
        
        IPolicyCmptType[] types = hierarchy.getAllSubtypes(supersupertype);
        assertEquals(2, types.length);
        assertSame(supertype, types[0]);
        assertSame(pcType, types[1]);
        
        types = hierarchy.getAllSubtypes(supertype);
        assertEquals(1, types.length);
        assertSame(pcType, types[0]);

        types = hierarchy.getAllSubtypes(pcType);
        assertEquals(0, types.length);
        
        // now same with cycle in hierarchy 
        supersupertype.setSupertype(pcType.getQualifiedName());
        hierarchy = TypeHierarchy.getSubtypeHierarchy(supersupertype);
        assertTrue(hierarchy.containsCycle());
        types = hierarchy.getAllSubtypes(supersupertype);
        assertEquals(2, types.length);
        assertSame(supertype, types[0]);
        assertSame(pcType, types[1]);
    }
}
