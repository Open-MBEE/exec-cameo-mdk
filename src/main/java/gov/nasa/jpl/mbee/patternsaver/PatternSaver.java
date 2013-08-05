package gov.nasa.jpl.mbee.patternsaver;

import gov.nasa.jpl.mbee.stylesaver.ViewSaver;

import java.util.HashSet;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;

public class PatternSaver {
	private JSONObject pattern;

	public void savePattern(Project proj, DiagramPresentationElement diag) {
    	// get the style string
		String styleStr = ViewSaver.save(proj, diag, false);
		if(styleStr == null) {
			return;
		}
		
		setPattern(diag, styleStr);
	}
	
	@SuppressWarnings("unchecked")
	private void setPattern(DiagramPresentationElement diag, String styleStr) {
		List<PresentationElement> elemList = diag.getPresentationElements();

		// parse the style string
		JSONParser parser = new JSONParser();
		Object parsedStyle = null;
		try {
			parsedStyle = parser.parse(styleStr);
		} catch(ParseException e) {
			e.printStackTrace();
			Application.getInstance().getGUILog().log("Error parsing pattern. Pattern save cancelled.");
			
			return;
		}

		JSONObject styleObj = (JSONObject) parsedStyle;
		
		HashSet<String> typesSaved = new HashSet<String>();		// a Set to store the type names saved throughout the process
		pattern = new JSONObject();								// a HashMap that will store the style pattern of the diagram
		
		for(PresentationElement elem : elemList) {
			String typeKey = elem.getHumanType();
			
			// check that the type style hasn't been saved yet
			if(!typesSaved.contains(typeKey)) {
				String typeValue = getElementPatternString(elem, styleObj);
				
				// add the key/value style pair to the JSON object
				pattern.put(typeKey, typeValue);
				
				typesSaved.add(elem.getHumanType());
				styleObj.remove(elem.getID());
			}
		}
	}
	
	public JSONObject getPattern() {
		return pattern;
	}
	
	private static String getElementPatternString(PresentationElement elem, JSONObject styleObj) {
		// get the value associated with the element's ID
		String elemStyleStr;
		try {
			elemStyleStr = (String) styleObj.get(elem.getID());
		} catch(NullPointerException e) {
			return null;
		}
		
		// element has not had its style saved
		if(elemStyleStr == null) {
			return null;
		}
		
		return elemStyleStr;
	}
}