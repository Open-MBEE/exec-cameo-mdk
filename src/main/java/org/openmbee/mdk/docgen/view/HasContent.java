package org.openmbee.mdk.docgen.view;

import org.eclipse.emf.common.util.EList;

/**
 * @author dlam
 * @model abstract="true"
 */
public interface HasContent extends ViewElement {

    /**
     * @return
     * @model containment="true"
     */
    EList<ViewElement> getChildren();
}
