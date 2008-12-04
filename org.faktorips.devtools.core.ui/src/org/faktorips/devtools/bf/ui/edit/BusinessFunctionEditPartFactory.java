package org.faktorips.devtools.bf.ui.edit;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.faktorips.devtools.core.model.bf.BFElementType;
import org.faktorips.devtools.core.model.bf.IBFElement;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.bf.IControlFlow;

public class BusinessFunctionEditPartFactory implements EditPartFactory {

    public EditPart createEditPart(EditPart context, Object model) {
        EditPart child = null;

        if (model instanceof IBusinessFunction) {
            child = new BusinessFunctionEditPart();
            
        } else if (model instanceof IBFElement) {

            IBFElement element = (IBFElement)model;
            if (element.getType().equals(BFElementType.ACTION_BUSINESSFUNCTIONCALL)) {
                child = new CallBusinessFunctionActionEditPart();
            } else if(element.getType().equals(BFElementType.ACTION_INLINE)){
                child = new OpaqueActionEditPart();
            } else if(element.getType().equals(BFElementType.ACTION_METHODCALL)){
                child = new CallMethodActionEditPart();
            } else if (element.getType().equals(BFElementType.DECISION)) {
                child = new DecisionEditPart();
            } else if (element.getType().equals(BFElementType.MERGE)) {
                child = new MergeEditPart();
            } else if (element.getType().equals(BFElementType.START)) {
                child = new StartEditPart();
            } else if (element.getType().equals(BFElementType.END)) {
                child = new EndEditPart();
            } else if (element.getType().equals(BFElementType.PARAMETER)) {
                return null;
            }
        } else if (model instanceof IControlFlow) {
            child = new ControlFlowEditPart();
        } else {
            throw new IllegalArgumentException("No EditPart can be created for the provided model object: " + model);
        }
        child.setModel(model);
        return child;
    }

}
