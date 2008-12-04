/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.faktorips.devtools.bf.ui.model.commands;

import org.eclipse.draw2d.AbsoluteBendpoint;
import org.eclipse.draw2d.geometry.Point;
import org.faktorips.devtools.core.model.bf.IControlFlow;

public class MoveBendpointCommand extends BendpointCommand {

    public MoveBendpointCommand(int index, Point location, IControlFlow controlFlow) {
        super(index, location, controlFlow);
    }
    @Override
    protected void executeInternal() {
        getControlFlow().setBendpoint(getIndex(), new AbsoluteBendpoint(getLocation()));
    }

}
