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
package gov.nasa.jpl.mbee;

import gov.nasa.jpl.mbee.dgview.FromProperty;
import gov.nasa.jpl.mbee.dgview.Table;
import gov.nasa.jpl.mbee.dgview.TableEntry;
import gov.nasa.jpl.mbee.dgview.TableRow;
import gov.nasa.jpl.mbee.dgview.ViewElement;
import gov.nasa.jpl.mbee.lib.HtmlManipulator;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBTable;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBTableEntry;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.From;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.export.image.ImageExporter;
import com.nomagic.magicdraw.properties.BooleanProperty;
import com.nomagic.magicdraw.properties.ElementProperty;
import com.nomagic.magicdraw.properties.NumberProperty;
import com.nomagic.magicdraw.properties.StringProperty;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ElementValue;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceValue;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralBoolean;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralInteger;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralReal;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralString;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralUnlimitedNatural;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Slot;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification;

public class DocGenUtils {

    /**
     * this was trying to use regex only to convert html tags to docbook tags,
     * should probably switch to just using jsoup
     */
    @SuppressWarnings("serial")
    public static final Map<String, String> html2docbookConvert = new HashMap<String, String>() {
                                                                    {
                                                                        put("<p>|<p [^>]*>", "<para>");
                                                                        put("</p>", "</para>");
                                                                        put("<ul>|<ul [^>]*>",
                                                                                "<itemizedlist spacing=\"compact\">");
                                                                        put("</ul>", "</itemizedlist>");
                                                                        put("<ol>|<ol [^>]*>",
                                                                                "<orderedlist spacing=\"compact\">");
                                                                        put("</ol>", "</orderedlist>");
                                                                        put("<li>|<li [^>]*>",
                                                                                "<listitem><para>");
                                                                        put("</li>", "</para></listitem>");
                                                                        put("<b>|<b [^>]*>|<em>|<em [^>]*>|<strong>|<strong [^>]*>",
                                                                                "<emphasis role=\"bold\">");
                                                                        put("<h[1-6]>|<h[1-6] [^>]*>", "<para><emphasis role=\"bold\">");
                                                                        put("<s>|<strike>|<s [^>]*>|<strike [^>]*>",
                                                                                "<emphasis role=\"strikethrough\">");
                                                                        put("<i>|<i [^>]*>", "<emphasis>");
                                                                        put("<u>|<u [^>]*>",
                                                                                "<emphasis role=\"underline\">");
                                                                        put("<span>|<span [^>]*>|</span>|<br>|<br/>|</br>|<br />",
                                                                                "");
                                                                        put("</b>|</i>|</u>|</strong>|</em>|</s>|</strike>",
                                                                                "</emphasis>");
                                                                        put("</h[1-6]>", "</emphasis></para>");
                                                                        put("<font [^>]*>|</font>", "");
                                                                        put("<sup>|<sup [^>]*>",
                                                                                "<superscript>");
                                                                        put("<sub>|<sub [^>]*>",
                                                                                "<subscript>");
                                                                        put("</sup>", "</superscript>");
                                                                        put("</sub>", "</subscript>");
                                                                        put("<a href=\"(http[^\"]+)\">([^<]*)</a>",
                                                                                "<link xl:href=\"$1\">$2</link>");
                                                                        put("<a href=\"(file[^\"]+)\">([^<]*)</a>",
                                                                                "<link xl:href=\"$1\">$2</link>");
                                                                        put("<a href=\"(mailto[^\"]+)\">([^<]*)</a>",
                                                                                "<link xl:href=\"$1\">$2</link>");
                                                                        put("<a href=\"mdel://([^\"&^\\?]+)(\\?[^\"]*)?\">([^<]*)</a>",
                                                                                "<link linkend=\"$1\">$3</link>");
                                                                        put("<img [^>]*src=\"([^>]+)\"[^>]*></img>", "<imageobject><imagedata fileref=\"$1\" scalefit=\"1\"/></imageobject>");
                                                                        put("<img [^>]*src=\"([^>]+)\"[^>]*/>", "<imageobject><imagedata fileref=\"$1\" scalefit=\"1\"/></imageobject>");
                                                                        put("<pre>|<pre [^>]*>", "<screen>");
                                                                        put("</pre>", "</screen>");
                                                                        put("<svg",
                                                                                "<mediaobject><imageobject><imagedata><svg");
                                                                        put("</svg>",
                                                                                "</svg></imagedata></imageobject></mediaobject>");
                                                                        put("&nbsp;", "&#160;");
                                                                        put("&sup2;",
                                                                                "<superscript>2</superscript>");
                                                                        put("&sup3;",
                                                                                "<superscript>3</superscript>");
                                                                    }
                                                                };

    /**
     * docbook ignores regular white space in table cells, this is to force
     * indentation in docbook, 1 indent is 4 spaces
     * 
     * @param name
     * @param depth
     * @return
     */
    public static String getIndented(String name, int depth) {
        String space = "";
        for (int i = 1; i < depth; i++)
            space += "&#xA0;&#xA0;&#xA0;&#xA0;";
        return space + name;
    }

    /**
     * given any object tries to return a string representation suitable for use
     * in docbook<br/>
     * 
     * @param s
     * @return
     */
    public static String fixString(Object s) {
        return fixString(s, true);
    }

    public static String fixString(Object s, boolean convertHtml) {
        // may want to look at
        // com.nomagic.magicdraw.uml.RepresentationTextCreator.getRepresentedText
        if (s instanceof String) {
            if (((String)s).contains("<html>"))
                if (convertHtml)
                    return HtmlManipulator.replaceHtmlEntities(html2docbook((String)s));
                else
                    return Utils.stripHtmlWrapper((String)s);
            else
                return HtmlManipulator.replaceHtmlEntities(((String)s)
                        .replaceAll("&(?![A-Za-z#0-9]+;)", "&amp;").replaceAll("<([>=\\s])", "&lt;$1")
                        .replaceAll("<<", "&lt;&lt;").replaceAll("<(?![^>]+>)", "&lt;"));
        } else if (s instanceof Integer)
            return Integer.toString((Integer)s);
        else if (s instanceof InstanceValue) {
            InstanceSpecification is = ((InstanceValue)s).getInstance();
            if (is != null)
                return fixString(is.getName());
        } else if (s instanceof ElementValue) {
            Element e = ((ElementValue)s).getElement();
            return fixString(e);
        } else if (s instanceof LiteralBoolean) {
            return Boolean.toString(((LiteralBoolean)s).isValue());
        } else if (s instanceof LiteralString) {
            return fixString(((LiteralString)s).getValue());
        } else if (s instanceof LiteralInteger) {
            return Integer.toString(((LiteralInteger)s).getValue());
        } else if (s instanceof LiteralUnlimitedNatural) {
            return Integer.toString(((LiteralUnlimitedNatural)s).getValue());
        } else if (s instanceof LiteralReal) {
            return Double.toString(((LiteralReal)s).getValue());
        } else if (s instanceof NamedElement) {
            return fixString(((NamedElement)s).getName());
        } else if (s instanceof Comment) {
            return fixString(((Comment)s).getBody());
        } else if (s instanceof StringProperty) {
            return fixString(((StringProperty)s).getString());
        } else if (s instanceof NumberProperty) {
            return ((NumberProperty)s).getValue().toString();
        } else if (s instanceof BooleanProperty) {
            return ((BooleanProperty)s).getBooleanObject().toString();
        } else if (s instanceof ElementProperty) {
            return fixString(((ElementProperty)s).getElement());
        } else if (s instanceof Slot) {
            return slot2String((Slot)s);
        } else if (s != null) {
            return fixString(s.toString());
        }
        return "";
    }
    
    public static Object getLiteralValue( Object s, boolean convertHtml ) {
        if (s instanceof String) {
            return fixString( s, convertHtml );
        } else if (s instanceof Integer) {
            return (Integer)s;
        } else if (s instanceof InstanceValue) {
            InstanceSpecification is = ((InstanceValue)s).getInstance();
            if (is != null)
                return getLiteralValue(is.getName(), convertHtml);
        } else if (s instanceof ElementValue) {
            Element e = ((ElementValue)s).getElement();
            return getLiteralValue(e, convertHtml);
        } else if (s instanceof LiteralBoolean) {
            return ((LiteralBoolean)s).isValue();
        } else if (s instanceof LiteralString) {
            return ((LiteralString)s).getValue();
        } else if (s instanceof LiteralInteger) {
            return ((LiteralInteger)s).getValue();
        } else if (s instanceof LiteralUnlimitedNatural) {
            return ((LiteralUnlimitedNatural)s).getValue();
        } else if (s instanceof LiteralReal) {
            return ((LiteralReal)s).getValue();
        } else if (s instanceof NamedElement) {
            return ((NamedElement)s).getName();
        } else if (s instanceof Comment) {
            return getLiteralValue(((Comment)s).getBody(), convertHtml);
        } else if (s instanceof StringProperty) {
            return getLiteralValue(((StringProperty)s).getString(), convertHtml);
        } else if (s instanceof NumberProperty) {
            return ((NumberProperty)s).getValue();
        } else if (s instanceof BooleanProperty) {
            return ((BooleanProperty)s).getBooleanObject();
        } else if (s instanceof ElementProperty) {
            return getLiteralValue(((ElementProperty)s).getElement(), convertHtml);
        } else if (s instanceof Slot) {
            return slot2String((Slot)s);
        }
        return s;
    }

    /**
     * gives sensible text representation for slot element
     * 
     * @param s
     * @return
     */
    public static String slot2String(Slot s) {
        return slot2String(s, true);
    }

    public static String slot2String(Slot s, boolean includeName) {
        String string = (includeName ? s.getDefiningFeature().getName() + " = " : "");
        List<String> values = new ArrayList<String>();
        for (ValueSpecification vs: s.getValue()) {
            values.add(fixString(vs));
        }
        return string + Utils.join(values, ", ");
    }

    /**
     * tries to make s into a docbook paragraph if not already one
     * 
     * @param s
     * @return
     */
    public static String addDocbook(String s) {
        String ss = html2docbook(s);
        if (ss.matches("(?s).*<para>.*")) // (s?) is the flag for DOTALL mode, .
                                          // doesn't match newlines by default
            // if (s.matches("(?s)\\s*<para>.*</para>\\s*"))
            return ss;
        return "<para>" + ss + "</para>";
    }

    /**
     * tries to make s into a html paragraph if not already one
     * 
     * @param s
     * @return
     */
    public static String addP(String s) {
        if (s.matches("(?s).*<p>.*")) // (s?) is the flag for DOTALL mode, .
                                      // doesn't match newlines by default
            // if (s.matches("(?s)\\s*<para>.*</para>\\s*"))
            return s;
        return "<p>" + s + "</p>";
    }

    /**
     * this is to help pdf transfrom be able to do wordwrap at non whitespace
     * chars, adds an invisible space to chars that should be able to break
     * probably should use some regex instead...
     * 
     * @param s
     * @return
     */
    public static String addInvisibleSpace(String s) {
        return s.replaceAll(";", ";&#x200B;").replaceAll("\\.", ".&#x200B;").replaceAll("\\(", "(&#x200B;")
                .replaceAll("\\)", ")&#x200B;").replaceAll(",", ",&#x200B;").replaceAll("/", "/&#x200B;")
                .replaceAll("_", "_&#x200B;").replaceAll("::", "::&#x200B;");
    }

    /**
     * if string contains html, converts it to docbook<br/>
     * also does some special processing if there's informal table elements,
     * removes width on tables so pdf transforms don't get cut off if width is
     * set too big should use jsoup processing to replace the regex map above
     * 
     * @param html
     * @return
     */
    public static String html2docbook(String html) {
        if (!html.contains("<html>"))
            return html;
        String s = null;
        Document d = Jsoup.parse(html);
        Elements tables = d.select("table.informal");
        if (!tables.isEmpty()) {
            tables.tagName("informaltable");
            tables.removeAttr("width");
        }
        tables = d.select("table");
        tables.removeAttr("width");

        Elements paragraphs = d.select("p");
        for (org.jsoup.nodes.Element e: paragraphs) {
            if (!e.hasText() || e.html().equals("&#160;") || e.html().equals("&nbsp;")) {
                e.remove();
            }
        }
        for (org.jsoup.nodes.Element e: d.select("span")) {
            if (e.hasAttr("style") && e.attr("style").startsWith("background-color")) {
                String style = e.attr("style");
                String color = style.substring(style.indexOf('#') + 1);
                e.tagName("phrase");
                e.removeAttr("style");
                e.attr("role", color);
            }
        }
        s = d.toString();
        int start = s.indexOf("<body>");
        int end = s.indexOf("</body>");
        if (start > -1 && end > -1)
            s = s.substring(start + 6, end);
        for (String key: html2docbookConvert.keySet()) {
            s = s.replaceAll(key, html2docbookConvert.get(key));
        }
        return s;
    }

    /**
     * Generates svg and png files of the diagram passed in, the diagram names
     * will be the diagram id in magicdraw
     * 
     * @param diagram
     *            the magicdraw diagram element
     * @param outputdir
     *            directory for docbook xml output (without trailing slash)
     * @param outputfilename
     *            name of the docbook xml output name (without extension, this
     *            will be used to generate a folder called outputfilename_files
     *            inside outputdir where the diagrams will be stored)
     * @param genNew
     *            true or false, if true, will always generate new image, if
     *            not, will check whether image is already there before
     *            generating
     * @param debug
     * @throws IOException
     */
    public static List<String> exportDiagram(Diagram d, File directory, boolean genNew) throws IOException {
        GUILog gl = Application.getInstance().getGUILog();
        List<String> res = new ArrayList<String>();
        DiagramPresentationElement diagram = Application.getInstance().getProject().getDiagram(d);

        String pngfilename = diagram.getID() + ".png";
        String svgfilename = diagram.getID() + ".svg";
        // String templateFolderName = outputdir + "/" + outputfilename +
        // "_files";
        // File directory = new File(templateFolderName);
        File pngdiagramFile = new File(directory, pngfilename);
        File svgdiagramFile = new File(directory, svgfilename);
        // String svgfname = outputfilename + "_files" + "/" + svgfilename;
        String svgfname = "images/" + svgfilename;// System.getProperty("file.separator")
                                                  // + svgfilename;
        res.add(svgfname);

        if (genNew || !pngdiagramFile.exists()) {
            try {
                gl.log("[DocGen] Exporting Diagram " + diagram.getName() + " " + svgfname);
                ImageExporter.export(diagram, ImageExporter.SVG, svgdiagramFile);
                ImageExporter.export(diagram, ImageExporter.PNG, pngdiagramFile);
            } catch (IOException e) {
                e.printStackTrace();
                return res;
            }
        } else {
            gl.log("[DocGen] Exporting diagram: Image file for " + diagram.getName()
                    + " exists. Using previously generated file.");
        }

        // whether to scale to width or not, in svg file width is specified in
        // inches, check it's less than width of pdf portrait paper
        String scale = "true";
        try {
            BufferedReader svg = new BufferedReader(new FileReader(svgdiagramFile));
            String line = svg.readLine();
            while (line != null) {
                if (line.startsWith("<svg")) {
                    int widthindex = line.indexOf("width");
                    if (widthindex > -1) {
                        int endindex = line.indexOf("\"", widthindex + 7);
                        String w = line.substring(widthindex + 7, endindex - 2);
                        double wd = Double.parseDouble(w);
                        if (wd < 5.5)
                            scale = "false";
                    }
                    break;
                }
                line = svg.readLine();
            }
            svg.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        res.add(scale);
        return res;
    }

    /**
     * return names of a collection of named elements
     * 
     * @param elements
     * @return
     */
    public static List<String> getElementNames(Collection<NamedElement> elements) {
        List<String> names = new ArrayList<String>();
        for (NamedElement e: elements) {
            names.add(e.getName());
        }
        return names;
    }

    public static DocumentElement ecoreTranslateView(ViewElement ve, boolean forViewEditor) {
        DocumentElement de = null;
        de = (new DgviewDBSwitch(forViewEditor)).doSwitch(ve);
        return de;
    }
}
