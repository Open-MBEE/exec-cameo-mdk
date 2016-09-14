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
package gov.nasa.jpl.mbee.stylesaver;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.openapi.uml.PresentationElementsManager;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.properties.*;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.magicdraw.uml.symbols.paths.PathElement;
import com.nomagic.magicdraw.uml.symbols.shapes.ShapeElement;
import org.json.simple.JSONObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class containing methods for parsing and loading style information.
 *
 * @author Benjamin Inada, JPL/Caltech
 */
public class ViewLoaderHelper {
    /**
     * Helper function that should be used to parse color information to load
     * into the property argument.
     *
     * @param prop  the property to load color information into.
     * @param value the string to parse.
     * @param elem  the element to load style into.
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
     * Helper function that should be used to parse font information to load
     * into the property argument.
     *
     * @param prop  the property to load font information into.
     * @param value the string to parse.
     * @param elem  the element to load style into.
     */
    public static void setFont(Property prop, String value, PresentationElement elem) {
        // go to the letter after the third ' ', this is where the font info
        // begins
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
        int style = Font.PLAIN; // set plain style as default
        int size = 11; // set size 11 as default
        int nextSpace = value.indexOf(' ');
        String word = value.substring(0, nextSpace);
        value = value.substring(nextSpace + 1);
        boolean styleFlag = false;

        while (true) {
            // try to parse the word as an integer
            // if parsable, we have reached "size" and the end of the string
            try {
                size = Integer.parseInt(word);
                break;
            } catch (NumberFormatException e) {
                // read in the next word (style can be made up of two words e.g.
                // italic bold)
                String nextWord = value.substring(0, value.indexOf(' '));
                String italicBoldCandidate = word + " " + nextWord;

                // note: case matters
                if (word.equals("bold\0")) {
                    style = Font.BOLD;
                    styleFlag = true;
                }
                else if (word.equals("italic\0")) {
                    style = Font.ITALIC;
                    styleFlag = true;
                }
                else if (italicBoldCandidate.equals("italic bold\0")) {
                    style = Font.ITALIC | Font.BOLD;
                    styleFlag = true;
                }
            }

            if (!styleFlag) {
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
     * @param prop  the property to load font information into.
     * @param value the string to parse.
     * @param elem  the element to load style into.
     */
    public static void setBoolean(Property prop, String value, PresentationElement elem) {
        if (value == null) {
            return;
        }

        // go to the letter after the third ' ', this is where the boolean
        // string is located
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

        if (value.equals("true")) {
            properties.addProperty(new BooleanProperty(prop.getID(), true));
        }
        else if (value.equals("false")) {
            properties.addProperty(new BooleanProperty(prop.getID(), false));
        }
        else {
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
     * Helper function that should be used to parse various choice properties of
     * elements.
     *
     * @param prop  the property to load choice property information into.
     * @param value the string to parse.
     * @param elem  the element to load style into.
     */
    public static void setChoice(Property prop, String value, PresentationElement elem) {
        String origValue = value;

        // go to the letter after the second ' ', this is where the type of
        // choice is located
        int firstSpace = value.indexOf(' ');
        value = value.substring(firstSpace + 1);
        int secondSpace = value.indexOf(' ');
        value = value.substring(secondSpace + 1);

        // find the third space - cut off here
        int thirdSpace = value.indexOf(' ');
        value = value.substring(0, thirdSpace);

        if (value.equals("LINK_LINE_STYLE")) {
            setPathStyle(prop, origValue, elem);
        }
    }

    /**
     * Helper function that should be used to parse path style information of an
     * element.
     *
     * @param prop  the property to load path style information into.
     * @param value the string to parse.
     * @param elem  the element to load style into.
     */
    public static void setPathStyle(Property prop, String value, PresentationElement elem) {
        // go to the letter after the third ' ', this is where the line style
        // string is located
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

        if (value.equals(PathElement.RECTILINEAR)) {
            properties.addProperty(new ChoiceProperty(prop.getID(), PathElement.RECTILINEAR,
                    PathElement.LINE_STYLE));
        }
        else if (value.equals(PathElement.OBLIQUE)) {
            properties.addProperty(new ChoiceProperty(prop.getID(), PathElement.OBLIQUE,
                    PathElement.LINE_STYLE));
        }
        else if (value.equals(PathElement.BEZIER)) {
            properties.addProperty(new ChoiceProperty(prop.getID(), PathElement.BEZIER,
                    PathElement.LINE_STYLE));
        }
        else {
            // some error in parsing
            Application.getInstance().getGUILog().log("The path style property failed to load.");
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
     * Helper function that should be used to parse break point information.
     *
     * @param elem    the element to reshape.
     * @param jsonObj the JSON object to parse.
     */
    public static void setBreakPoints(PathElement elem, JSONObject jsonObj) {
        // get the break points in the saved style
        Long numBreakPts = (Long) jsonObj.get("num_break_points");

        if (numBreakPts == null) {
            return;
        }

        List<Point> breakPoints = new ArrayList<Point>(numBreakPts.intValue());

        // add each of the break points to a list
        for (int i = 0; i < numBreakPts.intValue(); i++) {
            Point breakPt = getBreakPoint(jsonObj, "break_point_" + i);
            breakPoints.add(breakPt);
        }

        Point supplierPoint = getBreakPoint(jsonObj, "supplier_point");
        Point clientPoint = getBreakPoint(jsonObj, "client_point");

        try {
            PresentationElementsManager.getInstance().changePathPoints(elem, supplierPoint, clientPoint,
                    breakPoints);
        } catch (ReadOnlyElementException e) {
            Application.getInstance().getGUILog().log("Break points failed to load");
            return;
        }
    }

    /**
     * Parses a JSONObject containing Point information and gets a new Point.
     *
     * @param jsonObj   the JSONObject with Point information to parse.
     * @param pointName the name of the Point in the corresponding JSON string e.g.
     *                  break_point_1
     * @return the Point represented by this JSONObject.
     */
    private static Point getBreakPoint(JSONObject jsonObj, String pointName) {
        String breakPtStr = (String) jsonObj.get(pointName);

        int firstEquals = breakPtStr.indexOf('=');
        breakPtStr = breakPtStr.substring(firstEquals + 1);
        int firstComma = breakPtStr.indexOf(',');

        String xStr = breakPtStr.substring(0, firstComma);

        int secondEquals = breakPtStr.indexOf('=');
        breakPtStr = breakPtStr.substring(secondEquals + 1);
        int bracket = breakPtStr.indexOf(']');

        String yStr = breakPtStr.substring(0, bracket);

        Point newPoint = new Point(Integer.parseInt(xStr), Integer.parseInt(yStr));

        return newPoint;
    }

    /**
     * Helper function that should be used to parse path line width information.
     *
     * @param elem    the PathElement to load with saved line width.
     * @param jsonObj the JSONObject with line width information to parse.
     */
    public static void setLineWidth(PathElement elem, JSONObject jsonObj) {
        String widthStr = (String) jsonObj.get("path_line_width");

        elem.setLineWidth(Integer.parseInt(widthStr));
    }

    /**
     * Helper function that should be used to parse bounds information to
     * reshape an element.
     *
     * @param elem    the element to reshape.
     * @param jsonObj the JSON object to parse.
     */
    public static void setBounds(ShapeElement elem, JSONObject jsonObj) {
        Double x = (Double) jsonObj.get("rect_x");
        Double y = (Double) jsonObj.get("rect_y");
        Double width = (Double) jsonObj.get("rect_width");
        Double height = (Double) jsonObj.get("rect_height");

        if ((x == null) || (y == null) || (width == null) || (height == null)) {
            return;
        }

        // the restored bounds
        Rectangle rect = new Rectangle(x.intValue(), y.intValue(), width.intValue(), height.intValue());

        try {
            PresentationElementsManager.getInstance().reshapeShapeElement(elem, rect);
        } catch (ReadOnlyElementException e) {
            e.printStackTrace();
        }
    }
}
