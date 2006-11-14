/**
 * 
 */
package org.faktorips.devtools.core.internal.model.versionmanager;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.versionmanager.AbstractMigrationOperation;
import org.faktorips.util.message.MessageList;

/**
 * Empty Migration 
 * 
 * @author Peter Erzberger
 */
public class Migration_0_9_39 extends AbstractMigrationOperation {

    public Migration_0_9_39(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId);
    }

    /**
     * {@inheritDoc}
     */
    public String getDescription() {
        return Messages.Migration_0_9_39_Description;
    }

    /**
     * {@inheritDoc}
     */
    public String getTargetVersion() {
        return "0.9.40"; //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public MessageList migrate(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
        return new MessageList();
    }
}
