package org.faktorips.devtools.core.model.product;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;


/**
 *
 */
public interface IProductCmptRelation extends IIpsObjectPart {
    
    public final static String PROPERTY_TARGET = "target"; //$NON-NLS-1$
    public final static String PROPERTY_PCTYPE_RELATION = "pcTypeRelation"; //$NON-NLS-1$
    public final static String PROPERTY_MIN_CARDINALITY = "minCardinality"; //$NON-NLS-1$
    public final static String PROPERTY_MAX_CARDINALITY = "maxCardinality"; //$NON-NLS-1$
    
    public final static int CARDINALITY_MANY = IRelation.CARDINALITY_MANY;
    
    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "PRODUCTCMPT_RELATION-"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that the relation type in the model can't be found.
     */
    public final static String MSGCODE_UNKNWON_RELATIONTYPE = MSGCODE_PREFIX + "UnknownRelationType"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that the target product component does not exist.
     */
    public final static String MSGCODE_UNKNWON_TARGET = MSGCODE_PREFIX + "UnknownTarget"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the maximum cardinality is missing.
     */
    public final static String MSGCODE_MISSING_MAX_CARDINALITY = MSGCODE_PREFIX + "MissingMaxCardinality"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the maximum cardinality is less than 1.
     */
    public final static String MSGCODE_MAX_CARDINALITY_IS_LESS_THAN_1 = MSGCODE_PREFIX + "MaxCardinalityIsLessThan1"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the maximum cardinality exceeds the 
     * maximum cardinality defined in the model.
     */
    public final static String MSGCODE_MAX_CARDINALITY_EXCEEDS_MODEL_MAX = MSGCODE_PREFIX + "MaxCardinalityExceedsModelMax"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the minimum cardinality is less than 
     * the model defined min cardinality.
     */
    public final static String MSGCODE_MIN_CARDINALITY_IS_LESS_THAN_MODEL_MIN = MSGCODE_PREFIX + "MinCardinalityIsLessThanModelMin"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the maximum cardinality is less than 
     * the min cardinality.
     */
    public final static String MSGCODE_MAX_CARDINALITY_IS_LESS_THAN_MIN = MSGCODE_PREFIX + "MaxCardinalityIsLessThanMin"; //$NON-NLS-1$
    
    /**
     * Returns the product component generation this config element belongs to.
     */
    public IProductCmpt getProductCmpt();
    
    /**
     * Returns the product component generation this config element belongs to.
     */
    public IProductCmptGeneration getProductCmptGeneration();
    
    /**
     * Returns the name of the product component type relation this
     * relation is based on.
     */
    public String getProductCmptTypeRelation();
    
    /**
     * Returns the target product component.
     */
    public String getTarget();
    
    /**
     * Sets the target product component.
     */
    public void setTarget(String newTarget);

    /**
     * Returns the minmum number of target instances required in this relation.   
     */
    public int getMinCardinality();
    
    /**
     * Sets the minmum number of target instances required in this relation.   
     */
    public void setMinCardinality(int newValue);
    
    /**
     * Returns the maxmium number of target instances allowed in this relation.
     * If the number is not limited CARDINALITY_MANY is returned. 
     */
    public int getMaxCardinality();
    
    /**
     * Sets the maxmium number of target instances allowed in this relation.
     * An unlimited number is represented by CARDINALITY_MANY. 
     */
    public void setMaxCardinality(int newValue);
    
    /**
     * Finds the corresponding relation in the product component type this
     * product component is based on. Note the method searches not only the direct
     * product component type this product component is based on, but also it's 
     * super type hierarchy hierarchy.
     * 
     * @return the corresponding relation or <code>null</code> if no such
     * relation exists.
     * 
     * @throws CoreException if an exception occurs while searching the relation. 
     */
    public IProductCmptTypeRelation findProductCmptTypeRelation() throws CoreException;
    
}
