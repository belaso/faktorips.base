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

package org.faktorips.fl.functions;

import org.faktorips.codegen.CodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.FunctionSignatures;

/**
 * An extension of the {@link AbstractBaseFlFunction} that provides base functionality for variable
 * argument functions.
 */
// Should be renamed to AbstractVarArgFunction, but that might break the API because old code relies
// on that class handling JavaCodeFragments
public abstract class AbstractBaseVarArgFunction<T extends CodeFragment> extends AbstractBaseFlFunction<T> {

    public final static String ERROR_MESSAGE_CODE = ExprCompiler.PREFIX + "VARARG"; //$NON-NLS-1$

    /**
     * Creates a new {@link AbstractBaseVarArgFunction}.
     * 
     * @see AbstractBaseFlFunction the super class constructor parameter descripton for more details.
     */
    public AbstractBaseVarArgFunction(String name, String description, FunctionSignatures signature) {
        super(name, description, signature);
    }

    /**
     * Creates a new {@link AbstractBaseVarArgFunction}.
     * 
     * @see AbstractBaseFlFunction the super class constructor parameter descripton for more details.
     */
    public AbstractBaseVarArgFunction(String name, String description, Datatype type, Datatype argType) {
        super(name, description, type, argType);
    }

    /**
     * Returns the expected {@link Datatype} used to convert arguments in a varargs statement to a
     * common {@link Datatype}. This default implementation returns the {@link Datatype} defined for
     * the first argument in the expression signature.
     * 
     * @param argResults the results of the compilation of the individual parameters; may be used by
     *            subclasses to infer a {@link Datatype}.
     * @return the expected {@link Datatype}
     */
    protected Datatype getExpectedDatatypeForArgResultConversion(CompilationResult<T>[] argResults) {
        return getArgTypes()[0];
    }

    /**
     * The actual compile logic for this function has to be implemented within this method. The
     * called provides the {@link CompilationResult} that will be returned by this function, an
     * array of already converted arguments and a {@link CodeFragment} into which the code is to be
     * generated. The compilation result is provided to this method to write error messages that may
     * occur during the code generation or to get status information. Implementations don't need to
     * care about shoveling messages from the argument {@link CompilationResult} object to the
     * returned CompilationResult object. This is already handled by the caller.
     */
    protected abstract void compileInternal(CompilationResult<T> returnValue,
            CompilationResult<T>[] convertedArgs,
            T fragment);
}
