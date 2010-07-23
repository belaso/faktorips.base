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

package org.faktorips.devtools.core.model;

import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;

/**
 * An event that signals the change of an IPS source file's modification status from modifier to
 * unmodified or vice versa.
 * 
 * @author Jan Ortmann
 */
public class ModificationStatusChangedEvent {

    private IIpsSrcFile file;

    public ModificationStatusChangedEvent(IIpsSrcFile file) {
        this.file = file;
    }

    /**
     * Returns the file which modification status has changed.
     */
    public IIpsSrcFile getIpsSrcFile() {
        return file;
    }

}
