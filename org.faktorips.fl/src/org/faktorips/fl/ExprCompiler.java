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

package org.faktorips.fl;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.faktorips.codegen.CodeGenUtil;
import org.faktorips.codegen.ConversionCodeGenerator;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.fl.operations.AddDecimalDecimal;
import org.faktorips.fl.operations.AddDecimalInt;
import org.faktorips.fl.operations.AddDecimalInteger;
import org.faktorips.fl.operations.AddIntDecimal;
import org.faktorips.fl.operations.AddIntInt;
import org.faktorips.fl.operations.AddIntegerDecimal;
import org.faktorips.fl.operations.AddMoneyMoney;
import org.faktorips.fl.operations.AddStringString;
import org.faktorips.fl.operations.DivideDecimalDecimal;
import org.faktorips.fl.operations.DivideMoneyDecimal;
import org.faktorips.fl.operations.EqualsDecimalDecimal;
import org.faktorips.fl.operations.EqualsMoneyMoney;
import org.faktorips.fl.operations.EqualsStringString;
import org.faktorips.fl.operations.GreaterThanDecimalDecimal;
import org.faktorips.fl.operations.GreaterThanMoneyMoney;
import org.faktorips.fl.operations.GreaterThanOrEqualDecimalDecimal;
import org.faktorips.fl.operations.GreaterThanOrEqualMoneyMoney;
import org.faktorips.fl.operations.LessThanDecimalDecimal;
import org.faktorips.fl.operations.LessThanMoneyMoney;
import org.faktorips.fl.operations.LessThanOrEqualDecimalDecimal;
import org.faktorips.fl.operations.LessThanOrEqualMoneyMoney;
import org.faktorips.fl.operations.MinusDecimal;
import org.faktorips.fl.operations.MinusInteger;
import org.faktorips.fl.operations.MinusMoney;
import org.faktorips.fl.operations.MinusPrimitiveInt;
import org.faktorips.fl.operations.MultiplyDecimalDecimal;
import org.faktorips.fl.operations.MultiplyDecimalMoney;
import org.faktorips.fl.operations.MultiplyIntInt;
import org.faktorips.fl.operations.MultiplyIntegerMoney;
import org.faktorips.fl.operations.MultiplyMoneyDecimal;
import org.faktorips.fl.operations.NotEqualsDecimalDecimal;
import org.faktorips.fl.operations.NotEqualsMoneyMoney;
import org.faktorips.fl.operations.PlusDecimal;
import org.faktorips.fl.operations.PlusInteger;
import org.faktorips.fl.operations.PlusMoney;
import org.faktorips.fl.operations.PlusPrimitiveInt;
import org.faktorips.fl.operations.SubtractDecimalDecimal;
import org.faktorips.fl.operations.SubtractIntInt;
import org.faktorips.fl.operations.SubtractMoneyMoney;
import org.faktorips.fl.parser.FlParser;
import org.faktorips.fl.parser.ParseException;
import org.faktorips.fl.parser.SimpleNode;
import org.faktorips.fl.parser.TokenMgrError;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.message.Message;

/**
 * A compiler to compile expressions.
 * <p>
 * This class is not threadsafe!
 */
public class ExprCompiler {

    /**
     * The prefix for all compiler messages.
     */
    public final static String PREFIX = "FLC-"; //$NON-NLS-1$

    /**
     * An internal compiler error occured during compilation. This message is generated if the
     * compiler fails because of a bug, there is nothing wrong with the expression.
     */
    public final static String INTERNAL_ERROR = PREFIX + "InternalError"; //$NON-NLS-1$

    /**
     * The expression has a syntax error, it does not confirm to the grammar.
     * <p>
     * Example: 2 + + b does not conform to the grammar.
     */
    public final static String SYNTAX_ERROR = PREFIX + "SyntaxError"; //$NON-NLS-1$

    /**
     * The expression has a lexical error. 
     */
    public final static String LEXICAL_ERROR = PREFIX + "LexicalError"; //$NON-NLS-1$

    /**
     * The operation like +, *, - can't be done on the provided types.
     * <p>
     * Example: You can't multiply (*) two money values.
     */
    public final static String UNDEFINED_OPERATOR = PREFIX + "UndefinedOperator"; //$NON-NLS-1$

    /**
     * An identifier can't be resolved.
     * <p>
     * Example: a * 2
     * <p>
     * In the expression a is an identifier and it is possible that it can't be resolved by the
     * {IdentifierResolver} the compiler uses.
     */
    public final static String UNDEFINED_IDENTIFIER = PREFIX + "UndefinedIdentifier"; //$NON-NLS-1$

    /**
     * The expression contains a call to an undefined function.
     */
    public final static String UNDEFINED_FUNCTION = PREFIX + "UndefinedFunction"; //$NON-NLS-1$

    /**
     * The expression contains a function call to a function with wrong argument types.
     */
    public final static String WRONG_ARGUMENT_TYPES = PREFIX + "WrongArgumentTypes"; //$NON-NLS-1$

    /**
     * The expression contains a the expression <code>null</code>.
     */
    public final static String NULL_NOT_ALLOWED = PREFIX + "NullNotAllowed"; //$NON-NLS-1$

    final static LocalizedStringsSet localizedStrings = new LocalizedStringsSet(
            "org.faktorips.fl.Messages", ExprCompiler.class.getClassLoader());

    // locale that is used for the locale dependant messages generated by the compiler
    private Locale locale;

    // Resolver for identifiers
    private IdentifierResolver identifierResolver = new DefaultIdentifierResolver();

    // list of function resolvers
    private List functionResolvers = new ArrayList(2);

    // ConversionCodeGenerator that defines the implizit datatype conversion performed
    // by the compiler and can generate the appropriate Java sourcecode.
    private ConversionCodeGenerator conversionCg = ConversionCodeGenerator.getDefault();

    // Map containing a list of available binary operations per operator.
    private Map binaryOperations = new HashMap();

    // Map containing a list of available unary operations per operator.
    private Map unaryOperations = new HashMap();

    // the parser (generated by JavaCC)
    private FlParser parser;

    // true, if the expression's type should always be an object and not a primitive.
    private boolean ensureResultIsObject = true;

    /**
     * Creates a new compiler. Messages returned by the compiler are generated using the default
     * locale.
     */
    public ExprCompiler() {
        this(Locale.getDefault());
    }

    /**
     * Creates a new compiler.
     * 
     * @param locale The locale that is used to generate locale dependant messages.
     */
    public ExprCompiler(Locale locale) {
        this.locale = locale;
        parser = new FlParser(new ByteArrayInputStream("".getBytes()));
        registerDefaults();
    }

    /**
     * Registers the default operations.
     */
    protected void registerDefaults() {

        // plus operation
        register(new PlusDecimal());
        register(new PlusPrimitiveInt());
        register(new PlusInteger());
        register(new PlusMoney());

        // minus operation
        register(new MinusDecimal());
        register(new MinusInteger());
        register(new MinusPrimitiveInt());
        register(new MinusMoney());

        // add operation
        register(new AddDecimalDecimal());
        register(new AddDecimalInt());
        register(new AddDecimalInteger());
        register(new AddIntDecimal());
        register(new AddIntegerDecimal());
        register(new AddIntInt());
        register(new AddMoneyMoney());
        register(new AddStringString());

        // subtract operation
        register(new SubtractDecimalDecimal());
        register(new SubtractIntInt());
        register(new SubtractMoneyMoney());

        // multiply operation
        register(new MultiplyDecimalDecimal());
        register(new MultiplyIntInt());
        register(new MultiplyDecimalMoney());
        register(new MultiplyMoneyDecimal());
        register(new MultiplyIntegerMoney());

        // divide operation
        register(new DivideDecimalDecimal());
        register(new DivideMoneyDecimal());

        // greater than operation
        register(new GreaterThanDecimalDecimal());
        register(new GreaterThanMoneyMoney());

        // greater than or equal operation
        register(new GreaterThanOrEqualDecimalDecimal());
        register(new GreaterThanOrEqualMoneyMoney());

        // less than operation
        register(new LessThanDecimalDecimal());
        register(new LessThanMoneyMoney());

        // less than or equal operation
        register(new LessThanOrEqualDecimalDecimal());
        register(new LessThanOrEqualMoneyMoney());

        // equals operation
        register(new EqualsDecimalDecimal());
        register(new EqualsMoneyMoney());
        register(new EqualsStringString());

        // not equals operation
        register(new NotEqualsDecimalDecimal());
        register(new NotEqualsMoneyMoney());
    }

    /**
     * Registers the binary operation.
     */
    public void register(BinaryOperation op) {
        List operatorOperations = (List)binaryOperations.get(op.getOperator());
        if (operatorOperations == null) {
            operatorOperations = new ArrayList(20);
            binaryOperations.put(op.getOperator(), operatorOperations);
        }
        operatorOperations.add(op);
    }

    /**
     * Registers the unary operation.
     */
    public void register(UnaryOperation op) {
        List operatorOperations = (List)unaryOperations.get(op.getOperator());
        if (operatorOperations == null) {
            operatorOperations = new ArrayList(20);
            unaryOperations.put(op.getOperator(), operatorOperations);
        }
        operatorOperations.add(op);
    }

    /**
     * Sets the <code>BinaryOperation</code>s the compiler uses. Overwrites all operations
     * previously registered.
     * 
     * @throws IllegalArgumentException if operations is null.
     */
    public void setBinaryOperations(BinaryOperation[] operations) {
        ArgumentCheck.notNull(operations);
        binaryOperations = new HashMap();
        for (int i = 0; i < operations.length; i++) {
            register(operations[i]);
        }
    }

    /**
     * Sets the <code>UnaryOperation</code>s the compiler uses. Overwrites all operations
     * previously registered.
     * 
     * @throws IllegalArgumentException if operations is null.
     */
    public void setUnaryOperations(UnaryOperation[] operations) {
        ArgumentCheck.notNull(operations);
        unaryOperations = new HashMap();
        for (int i = 0; i < operations.length; i++) {
            register(operations[i]);
        }
    }

    /**
     * Returns the locale the compiler uses for it's messages.
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Returns the compiler's EnsureResultIsObject property.
     * 
     * @see #setEnsureResultIsObject(boolean)
     */
    public boolean getEnsureResultIsObject() {
        return this.ensureResultIsObject;
    }

    /**
     * Sets the compiler's EnsureResultIsObject property. If set to true, the compiler will check if
     * an expression's type is a Java primitive before returning the result. If the type is a
     * primitive the compiler will convert it to the appropriate wrapper class. E.g. the expresison
     * <code>2+4</code> is of type primitive int. If this property is set to true the compiler
     * would wrap the resulting source code with a <code>Integer(..)</code>.
     */
    public void setEnsureResultIsObject(boolean newValue) {
        this.ensureResultIsObject = newValue;
    }

    /**
     * Returns the resover the compiler uses to resolve identifiers.
     */
    public IdentifierResolver getIdentifierResolver() {
        return identifierResolver;
    }

    /**
     * Sets the <code>IdentifierResolver</code> the compiler uses to resolve identifiers.
     * 
     * @throws IllegalArgumentException if resolver is null.
     */
    public void setIdentifierResolver(IdentifierResolver resolver) {
        ArgumentCheck.notNull(resolver);
        this.identifierResolver = resolver;
    }

    /**
     * Returns the <code>ConversionCodeGenerator </code> that defines the compiler's implicit
     * conversions, e.g. convert a primtive int to an Integer.
     */
    public ConversionCodeGenerator getConversionCodeGenerator() {
        return conversionCg;
    }

    /**
     * Sets the <code>ConversionCodeGenerator</code> that the compiler uses for implicit
     * conversions, e.g. convert a primtive int to an Integer.
     * 
     * @throws IllegalArgumentException if ccg is null.
     */
    public void setConversionCodeGenerator(ConversionCodeGenerator ccg) {
        ArgumentCheck.notNull(ccg);
        conversionCg = ccg;
    }

    /**
     * Sets the <code>Locale</code> the compiler uses to generate it's messages.
     * 
     * @throws IllegalArgumentException if locale is null.
     */
    public void setLocale(Locale locale) {
        ArgumentCheck.notNull(locale);
        this.locale = locale;
    }

    /**
     * Adds the function resolver to the ones used by the compiler to resolve function calls in
     * expressions.
     * 
     * @throws IllegalArgumentException if fctResolver is null.
     */
    public void add(FunctionResolver fctResolver) {
        ArgumentCheck.notNull(fctResolver);
        functionResolvers.add(fctResolver);
        FlFunction[] functions = fctResolver.getFunctions();
        for (int i = 0; i < functions.length; i++) {
            functions[i].setCompiler(this);
        }
    }

    /**
     * Removes the function resolver from the ones used by the compiler to resolve function calls.
     * If the resolver hasn't been added before this method does nothing.
     * 
     * @throws IllegalArgumentException if fctResolver is null.
     */
    public void remove(FunctionResolver fctResolver) {
        ArgumentCheck.notNull(fctResolver);
        functionResolvers.remove(fctResolver);
    }

    /**
     * Returns an iterator to access the added function resolvers.
     */
    Iterator getFunctionResolvers() {
        return functionResolvers.iterator();
    }

    /**
     * Return the functions supported by the compiler.
     */
    public FlFunction[] getFunctions() {
        List functions = new ArrayList();
        for (Iterator it = getFunctionResolvers(); it.hasNext();) {
            FunctionResolver resolver = (FunctionResolver)it.next();
            FlFunction[] resolverFunctions = resolver.getFunctions();
            for (int i = 0; i < resolverFunctions.length; i++) {
                functions.add(resolverFunctions[i]);
            }
        }
        return (FlFunction[])functions.toArray(new FlFunction[functions.size()]);
    }

    /**
     * Compiles the given expression string into Java sourcecode. If the compilation is not
     * successfull, the result contains messages that describe the error/problem that has occured.
     * If the compilation is successfull, the result contains Java sourcecode that represents the
     * expression along wih the expression's datatype. In this case the result does not contain any
     * error messages, but may contain warnings or informations.
     */
    public CompilationResult compile(String expr) {
        SimpleNode rootNode;
        // parse the expression
        try {
            rootNode = parse(expr);
        } catch (ParseException pe) {
            return parseExceptionToResult(pe);
        } catch (Exception pe) {
            return new CompilationResultImpl(Message.newError(INTERNAL_ERROR, localizedStrings
                    .getString(INTERNAL_ERROR, locale)));
        } catch (TokenMgrError e) {
            String text = localizedStrings.getString(LEXICAL_ERROR, locale, new String[]{e.getMessage()});
            return new CompilationResultImpl(Message.newError(LEXICAL_ERROR, text));
        }
        // parse ok, generate the sourcecode via the visitor visiting the parse tree
        CompilationResult result;
        try {
            ParseTreeVisitor visitor = new ParseTreeVisitor(this);
            result = (CompilationResult)rootNode.jjtAccept(visitor, null);
        } catch (Exception e) {
            return new CompilationResultImpl(Message.newError(INTERNAL_ERROR, localizedStrings
                    .getString(INTERNAL_ERROR, locale)));
        }
        if (result.failed()) {
            return result;
        }
        try {
            Datatype resultType = result.getDatatype();
            if (!ensureResultIsObject || !resultType.isPrimitive()) {
                return result;
            }
            // convert primitive to wrapper object
            JavaCodeFragment converted = CodeGenUtil.convertPrimitiveToWrapper(resultType, result
                    .getCodeFragment());
            return new CompilationResultImpl(converted, ((ValueDatatype)resultType)
                    .getWrapperType());
        } catch (Exception e) {
            return new CompilationResultImpl(Message.newError(INTERNAL_ERROR, localizedStrings
                    .getString(INTERNAL_ERROR, locale)));
        }
    }

    private SimpleNode parse(String expr) throws ParseException {
        parser.ReInit(new ByteArrayInputStream(expr.getBytes()));
        return parser.start();
    }

    private CompilationResult parseExceptionToResult(ParseException e) {
        String expected = "";
        for (int i = 0; i < e.expectedTokenSequences.length; i++) {
            expected += e.tokenImage[e.expectedTokenSequences[i][0]] + " ";
        }
        Object[] replacements = new Object[] { e.currentToken.next.toString(),
                new Integer(e.currentToken.next.beginLine),
                new Integer(e.currentToken.next.beginColumn), expected };
        return new CompilationResultImpl(Message.newError(SYNTAX_ERROR, localizedStrings.getString(
            SYNTAX_ERROR, locale, replacements)));
    }

    BinaryOperation[] getBinaryOperations(String operator) {
        List operatorOperations = (List)binaryOperations.get(operator);
        if (operatorOperations == null) {
            return new BinaryOperation[0];
        }
        return (BinaryOperation[])operatorOperations.toArray(new BinaryOperation[operatorOperations
                .size()]);
    }

    UnaryOperation[] getUnaryOperations(String operator) {
        List operatorOperations = (List)unaryOperations.get(operator);
        if (operatorOperations == null) {
            return new UnaryOperation[0];
        }
        return (UnaryOperation[])operatorOperations.toArray(new UnaryOperation[operatorOperations
                .size()]);
    }

}
