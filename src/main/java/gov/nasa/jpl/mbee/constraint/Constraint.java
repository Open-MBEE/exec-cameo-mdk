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
//  public abstract class Constraint {
//    public Constraint( Element constrainingElement, Element constrainedElement ) {
//        addConstrainedElement( constrainedElement );
//        addConstrainingElement( constrainingElement );
//    }
    public Set< Element > getConstrainedElements();
    public Set< Element > getConstrainingElements();
    public void addConstrainedElements( Collection< Element > elements );
    public void addConstrainedElement( Element element );
    public void addConstrainingElements( Collection< Element > elements );
    public void addConstrainingElement( Element element );
    public Element getViolatedConstraintElement();
    public String getExpression();
//    public void setExpression();
    public Boolean evaluate();
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
