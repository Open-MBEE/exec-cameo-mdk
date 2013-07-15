package gov.nasa.jpl.mgss.mbee.docgen.dgview;

import org.eclipse.emf.common.util.EList;
import java.util.List;

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
