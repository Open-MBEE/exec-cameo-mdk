/*******************************************************************************
 * Copyright (c) <2013>, California Institute of Technology ("Caltech").  
 * U.S. Government sponsorship acknowledged.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are 
 * permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice, this list of 
 *    conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice, this list 
 *    of conditions and the following disclaimer in the documentation and/or other materials 
 *    provided with the distribution.
 *  - Neither the name of Caltech nor its operating division, the Jet Propulsion Laboratory, 
 *    nor the names of its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER  
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package gov.nasa.jpl.mbee.patternloader;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import gov.nasa.jpl.mbee.stylesaver.ViewSaver;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.HashSet;
import java.util.List;

/**
 * A class used to store a JSON style pattern of a diagram.
 *
 * @author Benjamin Inada, JPL/Caltech
 */
public class PatternSaver {
    /**
     * Stores the style pattern of a diagram in JSON object form.
     */
    private JSONObject pattern;

    /**
     * Stores the types saved in the pattern.
     */
    private HashSet<String> typesSaved;

    /**
     * Sets the pattern property by getting a style string representing the
     * styles on the parameter diagram.
     *
     * @param proj the project that the diagram is stored in.
     * @param diag the diagram to save.
     */
    public void savePattern(Project proj, DiagramPresentationElement diag) {
        // get the style string
        String styleStr = ViewSaver.save(proj, diag, true);
        if (styleStr == null) {
            return;
        }

        setPattern(diag, styleStr);
    }

    /**
     * A method for parsing the pattern style string and loading the pattern
     * property.
     *
     * @param diag     the diagram to save.
     * @param styleStr the JSON style pattern string.
     */
    private void setPattern(DiagramPresentationElement diag, String styleStr) {
        List<PresentationElement> elemList = diag.getPresentationElements();

        // parse the style string
        JSONParser parser = new JSONParser();
        Object parsedStyle = null;
        try {
            parsedStyle = parser.parse(styleStr);
        } catch (ParseException e) {
            e.printStackTrace();
            Application.getInstance().getGUILog().log("Error parsing pattern. Pattern save cancelled.");

            return;
        }

        JSONObject styleObj = (JSONObject) parsedStyle;

        pattern = new JSONObject(); // a HashMap that will store the style
        // pattern of the diagram
        typesSaved = new HashSet<String>(); // a Set to store the type names
        // saved throughout the process

        for (PresentationElement parent : elemList) {
            setPatternHelper(parent, styleObj);
        }
    }

    /**
     * A helper method for setting the style of a single element into the
     * pattern property.
     *
     * @param elem     the element to set.
     * @param styleObj the JSON style pattern object.
     */
    @SuppressWarnings("unchecked")
    private void setPatternHelper(PresentationElement elem, JSONObject styleObj) {
        // recursively set the pattern property
        setPatternHelperChildren(elem, styleObj);

        String typeKey = elem.getHumanType();

        // check that the type style hasn't been saved yet
        if (!typesSaved.contains(typeKey)) {
            String typeValue = getElementPatternString(elem, styleObj);

            // do not save element style into pattern string if its style was
            // not saved
            if (typeValue == null) {
                return;
            }

            // add the key/value style pair to the JSON object
            pattern.put(typeKey, typeValue);

            typesSaved.add(elem.getHumanType());
            styleObj.remove(elem.getID());
        }
    }

    /**
     * Recursive helper method to assist the setPatternHelper() method.
     *
     * @param parent   the parent of the possibly nested owned elements to save.
     * @param styleObj the JSON style pattern object.
     */
    private void setPatternHelperChildren(PresentationElement parent, JSONObject styleObj) {
        // get the parent element's children
        List<PresentationElement> children = parent.getPresentationElements();

        // base case -- no children
        if (children.isEmpty()) {
            return;
        }

        // iterate over each element storing style properties
        for (PresentationElement child : children) {
            setPatternHelper(child, styleObj);
        }
    }

    /**
     * Gets the pattern string for a specific presentation element.
     *
     * @param elem     the element to get the pattern string for.
     * @param styleObj the JSON style pattern object.
     * @return the pattern string for the parameter element, null if not found
     */
    private static String getElementPatternString(PresentationElement elem, JSONObject styleObj) {
        // get the style string associated with the element's ID
        String elemStyleStr;
        try {
            elemStyleStr = styleObj.get(elem.getID()).toString();
        } catch (NullPointerException e) {
            return null;
        }

        String patternString = PatternLoaderUtils.removeUnnecessaryProperties(elemStyleStr);

        return patternString;
    }

    /**
     * Getter for the pattern property.
     *
     * @return the pattern property.
     */
    public JSONObject getPattern() {
        return pattern;
    }
}
