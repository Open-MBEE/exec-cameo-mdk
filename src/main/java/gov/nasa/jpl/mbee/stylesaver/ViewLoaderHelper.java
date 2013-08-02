package gov.nasa.jpl.mbee.stylesaver;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;

import org.json.simple.JSONObject;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.openapi.uml.PresentationElementsManager;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.properties.BooleanProperty;
import com.nomagic.magicdraw.properties.ColorProperty;
import com.nomagic.magicdraw.properties.FontProperty;
import com.nomagic.magicdraw.properties.Property;
import com.nomagic.magicdraw.properties.PropertyManager;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.magicdraw.uml.symbols.shapes.ShapeElement;

/**
 * Helper class containing methods for parsing and loading style information.
 * 
 * @author Benjamin Inada, JPL/Caltech
 */
public class ViewLoaderHelper {
	/**
	 * Helper function that should be used to parse color information to load into the property
	 * argument.
	 * 
	 * @param prop	the property to load color information into.
	 * @param value	the string to parse.
	 * @param elem	the element to load style into.
	 */
	public static void setColor(Property prop, String value, PresentationElement elem) {
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
		
		// create the color
		Color newColor = new Color(red, green, blue);
		
		// set the property
		PropertyManager properties = new PropertyManager();
		properties.addProperty(new ColorProperty(prop.getID(), newColor));
		
		try {
			PresentationElementsManager.getInstance().setPresentationElementProperties(elem, properties);
		} catch (ReadOnlyElementException e) {
			e.printStackTrace();
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Helper function that should be used to parse font information to load into the property
	 * argument.
	 * 
	 * @param prop	the property to load font information into.
	 * @param value	the string to parse.
	 * @param elem	the element to load style into.
	 */
	public static void setFont(Property prop, String value, PresentationElement elem) {		
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
		int style = Font.PLAIN;	// set plain style as default
		int size = 11;			// set size 11 as default
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
		
		// create the font
		Font newFont = new Font(name, style, size);

		// set the property
		PropertyManager properties = new PropertyManager();
		properties.addProperty(new FontProperty(prop.getID(), newFont));
		
		try {
			PresentationElementsManager.getInstance().setPresentationElementProperties(elem, properties);
		} catch (ReadOnlyElementException e) {
			e.printStackTrace();
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Helper function that should be used to parse boolean styling.
	 * 
	 * @param prop	the property to load font information into.
	 * @param value	the string to parse.
	 * @param elem	the element to load style into.
	 */
	public static void setBoolean(Property prop, String value, PresentationElement elem) {
		if(value == null) {
			Application.getInstance().getGUILog().log(elem.getName());
			return;
		}
		
		// go to the letter after the third ' ', this is where the boolean string is located
		int firstSpace = value.indexOf(' ');
		value = value.substring(firstSpace + 1);
		int secondSpace = value.indexOf(' ');
		value = value.substring(secondSpace + 1);
		int thirdSpace = value.indexOf(' ');
		value = value.substring(thirdSpace + 1);
		
		// find the fourth space - cut off here
		int fourthSpace = value.indexOf(' ');
		value = value.substring(0, fourthSpace);
		
		// set the property
		PropertyManager properties = new PropertyManager();
		
		if(value.equals("true")) {
			properties.addProperty(new BooleanProperty(prop.getID(), true));
		} else if(value.equals("false")) {
			properties.addProperty(new BooleanProperty(prop.getID(), false));
		} else {
			// some error in parsing
			Application.getInstance().getGUILog().log("A true/false property failed to load.");
			return;
		}
		
		try {
			PresentationElementsManager.getInstance().setPresentationElementProperties(elem, properties);
		} catch (ReadOnlyElementException e) {
			e.printStackTrace();
		} catch (ClassCastException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
		}
	}
	
	/**
	 * Helper function that should be used to parse bounds information to reshape
	 * an element.
	 * 
	 * @param elem		the element to reshape.
	 * @param jsonObj	the JSON object to parse.
	 */
	public static void setBounds(ShapeElement elem, JSONObject jsonObj) {
		Double x = (Double) jsonObj.get("rect_x");
		Double y = (Double) jsonObj.get("rect_y");
		Double width = (Double) jsonObj.get("rect_width");
		Double height = (Double) jsonObj.get("rect_height");
		
		// the restored bounds
		Rectangle rect = new Rectangle(x.intValue(), y.intValue(), width.intValue(), height.intValue());
		
		try {
			PresentationElementsManager.getInstance().reshapeShapeElement(elem, rect);
		} catch(ReadOnlyElementException e) {
			e.printStackTrace();
		}
	}
}
