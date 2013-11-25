package gov.nasa.jpl.mgss.mbee.docgen.dgview;

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
