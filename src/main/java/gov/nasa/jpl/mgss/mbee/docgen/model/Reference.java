package gov.nasa.jpl.mgss.mbee.docgen.model;

import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.From;

import java.util.Collection;
import java.util.List;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Slot;

/**
 * A Reference encapsulates an Element, an attribute type, the resulting
 * value of the reference, and the corresponding DocumentElement in order to
 * treat the attribute as a variable in the context of the model and the
 * output document/view.
 * 
 */
public class Reference {

	public Element element = null;
    public From from = null;
    public Object result = null;

    /**
     * Create a Reference and assign all of its members.
     * 
     * @param element
     *            the source of the entry
     * @param from
     *            what aspect of the source to show
     * @param result
     *            the value to be shown
     */
    public Reference(Element element, From from, Object result) {
        this.element = element;
        this.from = from;
        this.result = result;
    }

    /**
     * Create a reference to the input object based on its type. 
     * this is very error prone since it's inferring what the element should be and what the From attribute should be, avoid using
     * @param element
     */
    public Reference(Element element) {
    	if (element instanceof Property) {
    		this.element = element;
    		this.result = ((Property)element).getDefaultValue();
    		this.from = From.DVALUE;
    	} else if (element instanceof Slot) {
    		this.element = element;
    		this.result = ((Slot)element).getValue();
    		this.from = From.DVALUE;
    	} else if (element instanceof NamedElement) {
    		this.element = element;
    		this.result = ((NamedElement)element).getName();
    		this.from = From.NAME;
    	} else
    		this.result = element.getHumanName();
    }
    
    /**
     * Create a reference to a result 
     * @param object
     *            the result referenced
     */
    public Reference(Object object) { // TODO -- is this function called (reused) where it could be?
    	this.result = object;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("(");
        sb.append("elem="
                + (element == null ? "null" : element.getHumanName())
                + ", ");
        sb.append("from=" + from + ", ");
        sb.append("result=" + result + ", ");
        sb.append(")");
        return sb.toString();
    }

    /**
     * Create reference to values of the property owned by the input
     * element that matches the input property.
     * 
     * @param element
     *            owner of the property
     * @param property
     *            a property similarly named to the property of element to
     *            be referenced
     * @return a list of references to matching properties for each of their
     *         values or an empty list if no such values exist.
     */
    public static Reference getPropertyReference(Element element,
                                                        Property property) {
        List<Object> v;
        List<Element> props;
        // need to get actual properties
        Element prop = Utils.getElementProperty(element, property);
        v = Utils.getElementPropertyValues(element, property, true);
        if (prop == null) {
            return new Reference(v); 
        } else {
            return new Reference(prop, From.DVALUE, v);
        }
    }

    /**
     * Determines whether result should be editable based on current view editor, a result is not editable if:
     * <ul><li>any of the result/element/from properties are null</li>
     * <li>the source element is a slot and its corresponding property has multiplicity > 1</li>
     * <li>the result is not a literal or made up of literals (string, number, boolean)</li>
     * </ul>
     * @return
     */
    public boolean isResultEditable() {
    	if (result == null || element == null || from == null || 
    			(element instanceof Slot && ((Slot)element).getDefiningFeature().getUpper() > 1) || 
    			(result instanceof Collection && ((Collection)result).size() > 1) || !Utils.isLiteral(result))
    		return false;
    	return true;
    }
    
    
    /**
     * Create references to an attribute of an element for each of its
     * values.
     * 
     * @param element
     *            owner of the property
     * @param from
     *            the attribute type of the element (name, value, ...)
     * @param results
     *            the values of the attribute
     * @return a list of references to the element's attribute for each of
     *         its values or an empty list if no such values exist.
     */
  /*  there shouldn't be any use case for expanding multiple results associated with one element into multiple references
 	public static List<Reference> getReferences(Element element, From from,
                                                List<Object> results) {
        List<Reference> entries = new ArrayList<Reference>();
        for (Object result : results) {
            List<Reference> c;
            List<Object> v;
            // null Element with ValueSpecification is meant to be
            // disconnected from an Element
            if (element == null && result instanceof ValueSpecification) {
                Reference ref = new Reference(null, from, result);
                entries.add(ref);
            } else if (result instanceof Slot) {
                Slot slot = (Slot) result;
                List<ValueSpecification> vals = slot.getValue(); // Utils.getSlotValues(
                                                                 // slot );
                if (!Utils2.isNullOrEmpty(vals)) {
                    v = new ArrayList<Object>(vals);
                    c = Common.Reference.getReferences(slot, From.DVALUE, v);
                    entries.addAll(c);
                }
            } else if (result instanceof Property) {
                Property property = (Property) result;
                c = getPropertyReference(element, property);
                entries.addAll(c);
            } else {
                Reference ref = new Reference(element, from, result);
                entries.add(ref);
            }
        }
        return entries;
    }
    */
}
