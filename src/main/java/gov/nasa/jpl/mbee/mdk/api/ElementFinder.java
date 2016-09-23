/*******************************************************************************
 * Copyright (c) <2016>, California Institute of Technology ("Caltech").  
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

package gov.nasa.jpl.mbee.mdk.api;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ElementFinder {

    /************************************************************
     *
     * MD Element Finding Methods
     *
     ************************************************************/

    /**
     * Returns a list of elements under the indicated parent, inclusive of the
     * parent (the first element in the list). No guarantee is made as to the
     * editability of these elements.
     *
     * @param parent The top level element whose owned elements you want to select
     */
    public static List<Element> findElements(Element parent) {
        List<Element> elements = new ArrayList<Element>();
        elements.add(parent);
        elements.addAll(parent.getOwnedElement());
        Element current;
        for (int i = 0; i < elements.size(); i++) {
            current = elements.get(i);
            if (current.hasOwnedElement()) {
                Collection<Element> owned = current.getOwnedElement();
                for (Element elm : owned) {
                    if (!elements.contains(elm)) {
                        elements.add(elm);
                    }
                }
            }
        }
        return elements;
    }

    /**
     * Returns a list of all elements in the model, inclusive of the data
     * element (the first element in the list). No guarantee is made as to the
     * editability of these elements.
     */
    public static List<Element> findElements() {
        return findElements(getModelRoot());
    }

    /**
     * Retrieves all elements in the project with the specified human name that are
     * owned by the specified element, inclusive of the parent (the first
     * element in the list). No guarantee is made as to the editability of these
     * elements.
     *
     * @param elementHumanName HumanName of element, i.e. "Package New Container" or (type + " " + name)
     * @param parent           The top level element whose owned elements you want to search
     *                         through
     */
    public static List<Element> findElementsByHumanName(String elementHumanName, Element parent) {
        List<Element> elements = findElements(parent);
        if (elements == null) {
            return null;
        }
        List<Element> retrievedElements = new ArrayList<Element>();
        for (Element elem : elements) {
            if (elem.getHumanName().contains(elementHumanName)) {
                retrievedElements.add(elem);
            }
        }
        System.out.println("Found " + retrievedElements.size() + " Element(s) containing human name " + elementHumanName);
        if (retrievedElements.size() > 0) {
            return retrievedElements;
        }
        else {
            return null;
        }
    }

    /**
     * Retrieves all elements in the project with the specified human name in the
     * model inclusive of the root (the first element in the list). No guarantee
     * is made as to the editability of these elements.
     *
     * @param elementHumanName HumanName of element, i.e. "Package New Container" or (type + " " + name)
     */
    public static List<Element> findElementsByHumanName(String elementHumanName) {
        return findElementsByHumanName(elementHumanName, getModelRoot());
    }

    /**
     * Retrieves all named elements in the project with the specified name that are
     * owned by the specified element, inclusive of the parent (the first
     * element in the list). No guarantee is made as to the editability of these
     * elements.
     *
     * @param elementType Type of element. ie. Package or Document.
     * @param parent      The top level element whose owned elements you want to search
     *                    through
     */
    public static List<NamedElement> findElementsByName(String elementName, Element parent) {
        List<Element> elements = findElements(parent);
        if (elements == null) {
            return null;
        }
        List<NamedElement> retrievedElements = new ArrayList<NamedElement>();
        for (Element elem : elements) {
            if (elem instanceof NamedElement && ((NamedElement) elem).getName().equals(elementName)) {
                retrievedElements.add((NamedElement) elem);
            }
        }
        System.out.println("Found " + retrievedElements.size() + " NamedElement(s) conaining name " + elementName);
        if (retrievedElements.size() > 0) {
            return retrievedElements;
        }
        else {
            return null;
        }
    }

    public static Element findOwnedElementByName(Element owner, String name) {
        for (Element e : owner.getOwnedElement()) {
            if (e instanceof NamedElement) {
                if (((NamedElement) e).getName().equals(name)) {
                    return e;
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all elements in the project with the specified name in the
     * model inclusive of the root (the first element in the list). No guarantee
     * is made as to the editability of these elements.
     *
     * @param elementName Name of element.
     */
    public static List<NamedElement> findElementsByName(String elementName) {
        return findElementsByName(elementName, getModelRoot());
    }

    /**
     * Retrieves all elements in the project with the specified type that are
     * owned by the specified parent, inclusive of parent (the first element in
     * the list). No guarantee is made as to the editability of these elements.
     *
     * @param elementType Type of element. ie. Package or Document.
     * @param parent      The top level element whose owned elements you want to search
     *                    through
     */
    public static List<Element> findElementsByType(String elementType, Element parent) {
        List<Element> elements = findElements(parent);
        if (elements == null) {
            return null;
        }
        List<Element> retrievedElements = new ArrayList<Element>();
        for (Element elem : elements) {
            if (elem.getHumanType().equals(elementType)) {
                retrievedElements.add(elem);
            }
        }
        System.out.println("Found " + retrievedElements.size() + " Element(s) of type " + elementType);
        if (retrievedElements.size() > 0) {
            return retrievedElements;
        }
        else {
            return null;
        }
    }

    /**
     * Retrieves all elements in the project with the specified type, inclusive
     * of root (the first element in the list). No guarantee is made as to the
     * editability of these elements.
     *
     * @param elementType Type of element. ie. Package or Document.
     */
    public static List<Element> findElementsByType(String elementType) {
        return findElementsByType(elementType, getModelRoot());
    }

    /**
     * Find the first occurrence of the listed element under the supplied owner,
     * inclusive of the owner
     *
     * @param type  Type of desired element
     * @param name  Name of desired element
     * @param owner Element under which you search for the indicated element
     */
    public static Element getElement(String type, String name, Element owner) {
        if (owner == null) {
            owner = getModelRoot();
        }
        ArrayList<Element> elements = new ArrayList<Element>();
        elements.add(owner);
        elements.addAll(owner.getOwnedElement());
        Element current;
        for (int i = 0; i < elements.size(); i++) {
            current = elements.get(i);
            if (current.getHumanName().equals(type + (name.equals("") ? "" : " " + name))) {
                return current;
            }
            if (current.hasOwnedElement()) {
                elements.addAll(current.getOwnedElement());
            }
        }
        return null;
    }

    /**
     * Find the first occurrence of the listed element anywhere in the model,
     * inclusive of model root
     *
     * @param type Type of desired element
     * @param name Name of desired element
     */
    public static Element getElement(String type, String name) {
        return getElement(type, name, getModelRoot());
    }

    /**
     * Finds the indicated element based on the ID
     *
     * @param targetID String containing target ID in project
     * @return
     */
    public static Element getElementByID(String targetID) {
        Element target = (Element) Application.getInstance().getProject().getElementByID(targetID);
        return target;
    }

    /**
     * @param qualifiedName in the format of magicdraw's qualified name: ex "Package::hello::world
     * @return
     */
    public static Element getElementByQualifiedName(String qualifiedName, Project project) {
        String[] path = qualifiedName.split("::");
        Element curElement = project.getModel();
        for (int i = 0; i < path.length; i++) {
            curElement = findOwnedElementByName(curElement, path[i]);
            if (curElement == null) {
                return null;
            }
        }
        return curElement;
    }

    /**
     * Retrieves the model root element.
     */
    public static Element getModelRoot() {
        Element mdl = Application.getInstance().getProject().getModel();
        if (mdl.getHumanName().equals("Model Data")) {
            return mdl;
        }
        return null;
    }

    /**
     * Finds the first package that contains the target element
     * Generally used to find a container that holds an element known directly,
     * or in conjunction with getElementByID to find it's container.
     *
     * @param target Element contained within the returned package
     * @return Element
     */
    public static Element getPackageContainer(Element target) {
        Element pkg = target.getOwner();
        while (!pkg.getHumanType().equals("Package")) {
            target = pkg;
            pkg = target.getOwner();
        }
        return pkg;
    }

    /**
     * @return
     */
    public static int getEmptyDiagramCount() {
        List<Element> diagrams = findElementsByType("Diagram");
        List<Element> temp = new ArrayList<Element>();
        for (Element e : diagrams) {
            if (!e.isEditable()) {
                temp.add(e);
            }
        }
        for (Element e : temp) {
            if (!e.isEditable()) {
                diagrams.remove(e);
            }
        }
        return diagrams.size();
    }

}
