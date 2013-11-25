package gov.nasa.jpl.mbee.dgview;

import org.eclipse.emf.common.util.EList;

/**
 * @model abstract="true"
 * @author dlam
 * 
 */
public interface HasContent extends ViewElement {

    /**
     * @model containment="true"
     * @return
     */
    EList<ViewElement> getChildren();
}
