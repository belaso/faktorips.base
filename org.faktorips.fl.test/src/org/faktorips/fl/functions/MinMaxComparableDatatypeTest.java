/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.fl.functions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

import java.util.Set;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.joda.LocalDateDatatype;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.functions.joda.Date;
import org.junit.Test;

public class MinMaxComparableDatatypeTest extends FunctionAbstractTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        registerFunction(new MinMaxComparableDatatypes("MAX", "", true, Datatype.STRING));
        registerFunction(new MinMaxComparableDatatypes("MIN", "", false, Datatype.STRING));

        registerFunction(new MinMaxComparableDatatypes("MAX", "", true, LocalDateDatatype.DATATYPE));
        registerFunction(new MinMaxComparableDatatypes("MIN", "", false, LocalDateDatatype.DATATYPE));
        registerFunction(new Date("DATE", ""));
    }

    @Test
    public void testCompile_max() throws Exception {
        execAndTestSuccessfull("MAX(\"aaa\"; \"zzz\")", "zzz", Datatype.STRING);
        execAndTestSuccessfull("MAX(\"zzz\"; \"aaa\")", "zzz", Datatype.STRING);
        execAndTestSuccessfull("MAX(\"1\"; \"1\")", "1", Datatype.STRING);
    }

    @Test
    public void testCompile_min() throws Exception {
        execAndTestSuccessfull("MIN(\"aaa\"; \"zzz\")", "aaa", Datatype.STRING);
        execAndTestSuccessfull("MIN(\"zzz\"; \"aaa\")", "aaa", Datatype.STRING);
        execAndTestSuccessfull("MIN(\"1\"; \"1\")", "1", Datatype.STRING);
    }

    @Test
    public void testCompile_minDate() throws Exception {

        CompilationResult<JavaCodeFragment> compile = compiler.compile("MIN(DATE(2014; 02; 01); DATE(2014; 03; 08))");
        Set<String> imports = compile.getCodeFragment().getImportDeclaration().getImports();
        assertEquals(
                "(new LocalDate(2014, 02, 01).compareTo(new LocalDate(2014, 03, 08)) < 0 ? new LocalDate(2014, 02, 01) : new LocalDate(2014, 03, 08))",
                compile.getCodeFragment().getSourcecode());

        assertThat(imports, hasItem("org.joda.time.LocalDate"));
    }

    @Test
    public void testCompile_maxDate() throws Exception {

        CompilationResult<JavaCodeFragment> compile = compiler.compile("MAX(DATE(2014; 02; 01); DATE(2014; 03; 08))");
        Set<String> imports = compile.getCodeFragment().getImportDeclaration().getImports();
        assertEquals(
                "(new LocalDate(2014, 02, 01).compareTo(new LocalDate(2014, 03, 08)) > 0 ? new LocalDate(2014, 02, 01) : new LocalDate(2014, 03, 08))",
                compile.getCodeFragment().getSourcecode());

        assertThat(imports, hasItem("org.joda.time.LocalDate"));
    }

    @Test
    public void testFail() throws Exception {
        execAndTestFail("MAX(\"10\")", "FLC-WrongArgumentTypes");
        execAndTestFail("MIN(\"aaa\")", "FLC-WrongArgumentTypes");
    }
}
