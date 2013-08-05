package gov.nasa.jpl.mbee.stylesaver;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.properties.Property;
import com.nomagic.magicdraw.properties.PropertyManager;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralString;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

/**
 * A class that loads style information corresponding to stereotyped elements.
 * 
 * @author Benjamin Inada, JPL/Caltech
 */
public class Loader extends MDAction {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Initializes the Loader.
	 * 
	 * @param id 		The ID of the action.
	 * @param value 	The name of the action.
	 * @param elem  	The element to be "saved."
	 * @param mnemonic	The mnemonic key of the action.
	 * @param group		The name of the related commands group.
	 */
	public Loader(String id, String value, int mnemonic, String group) {
		super(id, value, null, null);
	}
	
	/**
	 * Loads the style of elements on the active diagram by gathering
	 * relevant style information from a JSON string in a tag named "style" from
	 * the active diagram's stereotype.
	 * 
	 * @param e The ActionEvent that fired this method.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		Project proj = Application.getInstance().getProject();	// get the project
		GUILog gl = Application.getInstance().getGUILog();

    	DiagramPresentationElement diagram;
    	
    	// try to load the active diagram
    	try {
        	diagram = proj.getActiveDiagram();
    	} catch (NullPointerException ex) {
			gl.log("Plugin usage invalid -- Please open a diagram first.");
			return;
    	}

    	// get all the elements in the active diagram and store them into a list
    	List<PresentationElement> list;
    	try {
    		list = diagram.getPresentationElements();
    	} catch(NullPointerException ex) {
			gl.log("Plugin usage invalid -- There are no elements on this diagram.");
			return;
    	}
       	
    	diagram.ensureLoaded();
    	
    	// find the style block
		String blockName = diagram.getName() + "." + diagram.getElement().getID() + ".Style";
		
    	Collection<? extends Element> collection = ModelHelper.getElementsOfType(diagram.getElement().getOwner(), new java.lang.Class[]{com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class.class}, false);
    	
    	// find the block in the collection returned
    	Class block = null;
    	for(Element elem : collection) {
    		if(elem instanceof NamedElement) {
    			// search by name
    			if(((NamedElement) elem).getName().equals(blockName)) {
    				block = (Class) elem;
    				break;
    			}
    		}
    	}
    	
    	// TODO if a relative block was not found, alert the user
    	if(block == null) {
    		
    	}
		
    	// get a list of all the block's properties
		List<com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property> props = block.getOwnedAttribute();
		
		// find the style property
		com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property styleProp = null;
		for(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property p : props) {
			if(p.getName().equals("Style")) {
				styleProp = p;
				break;
			}
		}
		
		// TODO if the style property was not found, alert the user
		if(styleProp == null) {
			
		}
		
		// the default value field is where the JSON-string is located
		LiteralString styleVS = (LiteralString) styleProp.getDefaultValue();
		String style = styleVS.getValue();
				
    	for(PresentationElement diagElem : list) {
    		// parse the style string for the correct style for each element
    		String elemStyle = getStyleString(diagElem, style);
    		
    		// set the style of each element and repaint it
    		setStyle(diagElem, elemStyle);
    		diagElem.getDiagramSurface().repaint();
       	}
	}
	
	/**
	 * Get the specific style string for an element from the main style string.
	 * 
	 * @param elem The element the returned style string is for.
	 * @param style The main style string associated with the active diagram.
	 * 
	 * @return The style string associated with the PresentationElement argument.
	 */
	private static String getStyleString(PresentationElement elem, String style) {
		JSONParser parser = new JSONParser();

		// parse the style string
		Object obj = null;
		try {
			obj = parser.parse(style);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		JSONObject jsonObj = (JSONObject) obj;
		
		// get the value associated with the element's ID
		String styleStr = (String) jsonObj.get(elem.getID());
		
		return styleStr;
	}
	
	/**
	 * Sets style properties in the parameterized PresentationElement according to the
	 * PresentationElement's style property.
	 * 
	 * @param elem The element to set style properties in.
	 * @param style The style string to set.
	 */
	private static void setStyle(PresentationElement elem, String style) {
		PropertyManager propMan = elem.getPropertyManager();
		
		JSONParser parser = new JSONParser();

		// parse the style string
		Object obj = null;
		try {
			obj = parser.parse(style);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		JSONObject jsonObj = (JSONObject) obj;

		List<Property> propList = propMan.getProperties();
		Iterator<Property> iter = propList.iterator();

		// iterate over all the current properties
		while(iter.hasNext()) {
			Property currProp = iter.next();
			String currPropID = currProp.getID();

			// generate the new property
			String newPropValue = (String) jsonObj.get(currPropID);
			
			// if property takes a value of type java.awt.Color, call setColorHelper()
			if(currProp.getValue() instanceof java.awt.Color) {
				setColorHelper(currProp, newPropValue);
			}
			
			// if property takes a value of type java.awt.Font, call setFontHelper()
			if(currProp.getValue() instanceof java.awt.Font) {
				setFontHelper(currProp, newPropValue);
			}
		}
	}
	
	/**
	 * Helper function that should be used to parse color information to load into the property
	 * argument.
	 * 
	 * @param prop The property to load color information into.
	 * @param value The string to parse.
	 */
	private static void setColorHelper(Property prop, String value) {
		// trim the RGB values
		int ltBracket = value.indexOf("[");
		int rtBracket = value.indexOf("]");
		String rgb = value.substring(ltBracket + 1, rtBracket);
		
		// parse the RGB comma-separated string
		String redStr = rgb.substring(0, rgb.indexOf(','));
		rgb = rgb.substring(rgb.indexOf(',') + 2);
		String greenStr = rgb.substring(0, rgb.indexOf(','));
		rgb = rgb.substring(rgb.indexOf(',') + 2);
		String blueStr = rgb.substring(0);
		
		// get the integer representations of the color values
		int red = Integer.parseInt(redStr);
		int green = Integer.parseInt(greenStr);
		int blue = Integer.parseInt(blueStr);
				
		Color newColor = new Color(red, green, blue);
		prop.setValue(newColor);
	}
	
	/**
	 * Helper function that should be used to parse font information to load into the property
	 * argument.
	 * 
	 * @param prop The property to load font information into.
	 * @param value The string to parse.
	 */
	private static void setFontHelper(Property prop, String value) {		
		// go to the letter after the third ' ', this is where the font info begins
		int firstSpace = value.indexOf(' ');
		value = value.substring(firstSpace + 1);
		int secondSpace = value.indexOf(' ');
		value = value.substring(secondSpace + 1);
		int thirdSpace = value.indexOf(' ');
		int end = value.indexOf('(');
		
		// trim the string to parsable format e.g. Arial bold 11
		value = value.substring(thirdSpace + 1, end);
		value = value.replace(',', '\0');
				
		String name = "";
		int style = Font.PLAIN;	// plain should be the default
		int size = 11;
		int nextSpace = value.indexOf(' ');
		String word = value.substring(0, nextSpace);
		value = value.substring(nextSpace + 1);
		boolean styleFlag = false;
		
		while(true) {
			// try to parse the word as an integer
			// if parsable, we have reached "size" and the end of the string
			try {
				size = Integer.parseInt(word);
				break;
			} catch(NumberFormatException e) {
				// read in the next word (style can be made up of two words e.g. italic bold)
				String nextWord = value.substring(0, value.indexOf(' '));
				String italicBoldCandidate = word + " " + nextWord;
				
				// note: case matters
				if(word.equals("bold\0")) {
					style = Font.BOLD;
					styleFlag = true;
				} else if(word.equals("italic\0")) {
					style = Font.ITALIC;
					styleFlag = true;
				} else if(italicBoldCandidate.equals("italic bold\0")) {
					style = Font.ITALIC | Font.BOLD;
					styleFlag = true;
				}
			}
			
			if(!styleFlag) {
				name = name.concat(word + " ");
			}
			
			// read up to the next ' ', and trim the string
			nextSpace = value.indexOf(' ');
			word = value.substring(0, nextSpace);
			value = value.substring(nextSpace + 1);
		}

		// trim extra space from name
		name = name.substring(0, name.lastIndexOf(' '));
		
		Font newFont = new Font(name, style, size);
		prop.setValue(newFont);
	}
}