/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.htmlexport.pages.elements.core.table;

import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;

/**
 * <p>
 * The {@link RegexTablePageElementLayout} uses a regular expression to layout a cell.
 * </p>
 * <p>
 * Set given {@link Style}s, if
 * <ol>
 * <li>{@link TableCellPageElement} consists just of one {@link TextPageElement}</li>
 * <li>the text of the {@link TextPageElement} matches the given regular expression</li>
 * </ol>
 * </p>
 * <p>
 * e.g. RegexTablePageElementLayout(".{1,3}", Style.CENTER) centers every cell with maximum 3
 * characters
 * </p>
 * 
 * @author dicker
 * 
 */
public class RegexTablePageElementLayout extends DefaultTablePageElementLayout {
    private String regex;
    private Style[] styles;

    /**
     * creates a {@link RegexTablePageElementLayout} with the pattern regex and adds the given
     * {@link Style}s to the matching cells
     * 
     */
    public RegexTablePageElementLayout(String regex, Style... styles) {
        super();
        this.regex = regex;
        this.styles = styles;
    }

    @Override
    public void layoutCell(int row, int column, TableCellPageElement cellPageElement) {
        if (cellMatches(cellPageElement)) {
            cellPageElement.addStyles(styles);
        }
    }

    private boolean cellMatches(TableCellPageElement cellPageElement) {
        if (cellPageElement.size() != 1) {
            return false;
        }

        if (!(cellPageElement.getSubElement(0) instanceof TextPageElement)) {
            return false;
        }

        TextPageElement element = (TextPageElement)cellPageElement.getSubElement(0);

        if (element.getText() == null) {
            return false;
        }

        return (element.getText().matches(regex));
    }
}
