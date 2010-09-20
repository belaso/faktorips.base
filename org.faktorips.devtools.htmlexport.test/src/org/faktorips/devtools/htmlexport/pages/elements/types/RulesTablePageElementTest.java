/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.htmlexport.pages.elements.types;

import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.pctype.MessageSeverity;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.standard.AbstractXmlUnitHtmlExportTest;
import org.faktorips.devtools.htmlexport.pages.standard.ContentPageUtil;

public class RulesTablePageElementTest extends AbstractXmlUnitHtmlExportTest {

    private PolicyCmptType policy;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        policy = newPolicyCmptType(ipsProject, "Vertrag");
    }

    private void assertXPathFromTable(PageElement objectContentPage, String subXPath) throws Exception {
        assertXPathExists(objectContentPage, getXPathMethodTable() + subXPath);
    }

    private String getXPathMethodTable() {
        return "//table[@id= '" + policy.getName() + "_validationrules" + "']";
    }

    public void testMethodsTableVorhanden() throws Exception {

        IValidationRule methodString = createRuleWithAttributes();
        IValidationRule methodInteger = createRuleWithoutAttributes();

        PageElement objectContentPage = ContentPageUtil.createObjectContentPageElement(policy.getIpsSrcFile(), config);

        assertXPathExists(objectContentPage, getXPathMethodTable());

        assertXPathFromTable(objectContentPage, "[count(.//tr)=3]");

        assertXPathFromTable(objectContentPage, "//tr[2][td='" + methodString.getName() + "']");

        assertXPathFromTable(objectContentPage, "//tr[3][td='" + methodInteger.getName() + "']");
    }

    public void testMethodsTableNichtVorhandenOhneAttribute() throws Exception {

        PageElement objectContentPage = ContentPageUtil.createObjectContentPageElement(policy.getIpsSrcFile(), config);

        assertXPathNotExists(objectContentPage, getXPathMethodTable());
    }

    public void testMethodsTableAufbau() throws Exception {
        createRuleWithoutAttributes();
        createRuleWithAttributes();
        PageElement objectContentPage = ContentPageUtil.createObjectContentPageElement(policy.getIpsSrcFile(), config);

        int row = 2;

        IValidationRule[] rules = policy.getRules();
        for (IValidationRule rule : rules) {
            assertXPathFromTable(objectContentPage, "//tr[" + row + "][td='" + rule.getName() + "']");
            assertXPathFromTable(objectContentPage, "//tr[" + row + "][td='" + rule.getMessageCode() + "']");
            assertXPathFromTable(objectContentPage, "//tr[" + row + "][td='" + rule.getMessageSeverity().getName()
                    + "']");
            assertXPathFromTable(objectContentPage, "//tr[" + row + "][td='" + rule.getMessageText() + "']");

            String[] validatedAttributes = rule.getValidatedAttributes();
            for (int i = 0; i < validatedAttributes.length; i++) {
                assertXPathFromTable(objectContentPage, "//tr[" + row + "]/td[contains(., '" + validatedAttributes[i]
                        + "')]");
            }

            row++;
        }
    }

    private IValidationRule createRuleWithoutAttributes() {
        IValidationRule rule = policy.newRule();
        rule.setName("RuleWithoutAttributes");
        rule.setMessageCode("CODE_WITHOUT_ATTRIBUTES");
        rule.setMessageSeverity(MessageSeverity.ERROR);
        rule.setMessageText("blubber");
        rule.addValidatedAttribute("Attribut_1");
        rule.addValidatedAttribute("Attribut_2");
        return rule;
    }

    private IValidationRule createRuleWithAttributes() {
        IValidationRule rule = policy.newRule();
        rule.setName("RuleWithAttributes");
        rule.setMessageCode("CODE_WITH_ATTRIBUTES");
        rule.setMessageSeverity(MessageSeverity.WARNING);
        rule.setMessageText("blabla");
        return rule;
    }
}
