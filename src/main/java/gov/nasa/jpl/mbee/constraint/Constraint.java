/**
 * 
 */
package gov.nasa.jpl.mbee.constraint;

import java.util.Collection;
import java.util.Set;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

/**
 * A constraint in the context of a model.
 *
 */
public interface Constraint {
////  public abstract class Constraint {
////    public Constraint( Element constrainingElement, Element constrainedElement ) {
////        addConstrainedElement( constrainedElement );
////        addConstrainingElement( constrainingElement );
////    }
//    public Set< Element > getConstrainedElements();

    /**
     * @return the constrained elements and any other other constrained objects.
     */
    public Set< Object > getConstrainedObjects();
    public Set< Element > getConstrainingElements();
//    public void addConstrainedElements( Collection< Element > elements );
//    public void addConstrainedElement( Element element );
    public void addConstrainedObjects( Collection< Object > objects );
    public void addConstrainedObject( Object object );
    public void addConstrainingElements( Collection< Element > elements );
    public void addConstrainingElement( Element element );
    public Element getViolatedConstraintElement();
    
    /**
     * @return a text expression of the constraint
     */
    public String getExpression();
//    public void setExpression();

    public Boolean evaluate();
    
    /**
     * @return whether the constraint is properly specified and not self-contradictory
     */
    public boolean isConsistent();
//    public Boolean isGrounded();
//    public static class Helper {
//        public static Constraint makeConstraint( Element constraint, Object constrainedObject ) {
//            
//        }
//        public static void evaluateAgainst( Element constraint, Object constrainedObject ) {
//            
//        }
//    }
}
