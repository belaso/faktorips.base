/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.binding;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.Viewer;

public class ViewerRefreshBinding extends ControlPropertyBinding {

    private final Viewer viewer;
    private boolean expandAll;

    public ViewerRefreshBinding(Viewer viewer, Object object, String propertyName, boolean expandAll) {
        super(viewer.getControl(), object, propertyName, null);
        this.viewer = viewer;
        this.expandAll = expandAll;
    }

    public static ViewerRefreshBinding refresh(Viewer viewer, Object object) {
        return new ViewerRefreshBinding(viewer, object, null, false);
    }

    public static ViewerRefreshBinding refreshAndExpand(Viewer viewer, Object object) {
        return new ViewerRefreshBinding(viewer, object, null, true);
    }

    public static ViewerRefreshBinding refresh(Viewer viewer, Object object, String propertyName) {
        return new ViewerRefreshBinding(viewer, object, propertyName, false);
    }

    public static ViewerRefreshBinding refreshAndExpand(Viewer viewer, Object object, String propertyName) {
        return new ViewerRefreshBinding(viewer, object, propertyName, true);
    }

    @Override
    public void updateUiIfNotDisposed(String nameOfChangedProperty) {
        if (StringUtils.isEmpty(nameOfChangedProperty) || getProperty() == null
                || getPropertyName().equals(nameOfChangedProperty)) {
            viewer.refresh();
            if (expandAll && viewer instanceof AbstractTreeViewer) {
                ((AbstractTreeViewer)viewer).expandAll();
            }
        }
    }

}
