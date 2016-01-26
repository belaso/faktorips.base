/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.productcmpt;

import static org.faktorips.abstracttest.matcher.Matchers.hasInvalidObject;
import static org.faktorips.abstracttest.matcher.Matchers.hasMessageCode;
import static org.faktorips.abstracttest.matcher.Matchers.isEmpty;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CardinalityTest {

    @Mock
    private IProductCmptLink link;

    @Test
    public void testValidate_Ok() throws Exception {
        assertThat(new Cardinality(0, 1, 0).validate(link), isEmpty());
        assertThat(new Cardinality(1, 1, 1).validate(link), isEmpty());
        assertThat(new Cardinality(0, Cardinality.CARDINALITY_MANY, 0).validate(link), isEmpty());
        assertThat(new Cardinality(0, Cardinality.CARDINALITY_MANY, 1).validate(link), isEmpty());
        assertThat(new Cardinality(1, Cardinality.CARDINALITY_MANY, 1).validate(link), isEmpty());
        assertThat(new Cardinality(100, Cardinality.CARDINALITY_MANY, 500).validate(link), isEmpty());
    }

    @Test
    public void testValidate_Min_Bewlow_Zero() throws Exception {
        Cardinality cardinality = new Cardinality(-1, 1, 0);

        assertThat(cardinality.validate(link), hasMessageCode(Cardinality.MSGCODE_MIN_CARDINALITY_IS_LESS_THAN_0));
        assertThat(cardinality.validate(link).size(), is(1));
        assertThat(cardinality.validate(link).getMessage(0), hasInvalidObject(link));
    }

    @Test
    public void testValidate_Max_Bewlow_1() throws Exception {
        Cardinality cardinality = new Cardinality(0, 0, 0);

        assertThat(cardinality.validate(link), hasMessageCode(Cardinality.MSGCODE_MAX_CARDINALITY_IS_LESS_THAN_1));
        assertThat(cardinality.validate(link).size(), is(1));
        assertThat(cardinality.validate(link).getMessage(0), hasInvalidObject(link));
    }

    @Test
    public void testValidate_Max_Lt_Min() throws Exception {
        Cardinality cardinality = new Cardinality(2, 1, 2);

        assertThat(cardinality.validate(link), hasMessageCode(Cardinality.MSGCODE_MAX_CARDINALITY_IS_LESS_THAN_MIN));
        assertThat(cardinality.validate(link).size(), is(1));
        assertThat(cardinality.validate(link).getMessage(0), hasInvalidObject(link));
    }

    @Test
    public void testValidate_Default_Lt_Min() throws Exception {
        Cardinality cardinality = new Cardinality(1, 5, 0);

        assertThat(cardinality.validate(link), hasMessageCode(Cardinality.MSGCODE_DEFAULT_CARDINALITY_OUT_OF_RANGE));
        assertThat(cardinality.validate(link).size(), is(1));
        assertThat(cardinality.validate(link).getMessage(0), hasInvalidObject(link));
    }

    @Test
    public void testValidate_Default_Gt_Max() throws Exception {
        Cardinality cardinality = new Cardinality(1, 5, 6);

        assertThat(cardinality.validate(link), hasMessageCode(Cardinality.MSGCODE_DEFAULT_CARDINALITY_OUT_OF_RANGE));
        assertThat(cardinality.validate(link).size(), is(1));
        assertThat(cardinality.validate(link).getMessage(0), hasInvalidObject(link));
    }

    @Test
    public void testValidate_Default_Too_High() throws Exception {
        Cardinality cardinality = new Cardinality(1, Cardinality.CARDINALITY_MANY, Cardinality.CARDINALITY_MANY);

        assertThat(cardinality.validate(link), hasMessageCode(Cardinality.MSGCODE_DEFAULT_CARDINALITY_OUT_OF_RANGE));
        assertThat(cardinality.validate(link).size(), is(1));
        assertThat(cardinality.validate(link).getMessage(0), hasInvalidObject(link));
    }

}
