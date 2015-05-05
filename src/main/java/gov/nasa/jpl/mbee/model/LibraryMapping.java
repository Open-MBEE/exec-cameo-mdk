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
package gov.nasa.jpl.mbee.model;

import gov.nasa.jpl.mbee.DocGenUtils;
import gov.nasa.jpl.mbee.actions.MapLibraryAction;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.lib.Utils2;
import gov.nasa.jpl.mbee.model.ui.LibraryChooserUI;
import gov.nasa.jpl.mbee.model.ui.LibraryComponent;
import gov.nasa.jpl.mbee.tree.Node;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBTable;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBText;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Dependency;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DirectedRelationship;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Type;
import com.nomagic.uml2.impl.ElementsFactory;

public class LibraryMapping extends Query {

    private Package                        componentPackage;
    private Package                        charPackage;
    private Set<NamedElement>              chars;
    private ElementsFactory                ef;
    private SessionManager                 sm;
    private static final String            CHAR                = "Characterization";
    private static final String            CHARACTERIZES       = "Characterizes";
    private static final String            COMPONENT           = "Component";
    private static final String            IMCECOMPONENT       = "mission:Component";
    private static final String            IMCECHAR            = "analysis:Characterization";
    private static final String            IMCECHARACTERIZES   = "analysis:characterizes";
    private static final String            IMCECHARACTERIZABLE = "analysis:CharacterizedElement";
    private static final String            DEPPREFIX           = "zz_";
    private boolean                        IMCEPresent         = false;
    private Node<String, LibraryComponent> tree;

    private Set<NamedElement>              usedChars;

    public boolean init() {
        chars = new HashSet<NamedElement>();
        usedChars = new HashSet<NamedElement>();
        ef = Application.getInstance().getProject().getElementsFactory();
        sm = SessionManager.getInstance();

        // create a root tree node so we can add multiple component imports
        tree = new Node<String, LibraryComponent>("Library", new LibraryComponent("Library"));

        for (Object e: this.targets) {
            if (e instanceof Package) {
                for (Element ee: Utils.collectOwnedElements((Package)e, 0)) {
                    if (StereotypesHelper.hasStereotypeOrDerived(ee, IMCECHAR)
                            || StereotypesHelper.hasStereotypeOrDerived(ee, CHAR)) {
                        charPackage = (Package)e;
                        if (StereotypesHelper.hasStereotypeOrDerived(ee, IMCECHAR))
                            IMCEPresent = true;
                        fillChars();
                        break;
                    }
                }
            }
        }

        // fill in the tree after all the characterizations
        for (Object e: this.targets) {
            if (e instanceof Package) {
                for (Element ee: Utils.collectOwnedElements((Package)e, 0)) {
                    if (StereotypesHelper.hasStereotypeOrDerived(ee, IMCECOMPONENT)
                            || StereotypesHelper.hasStereotypeOrDerived(ee, COMPONENT)
                            || StereotypesHelper.hasStereotypeOrDerived(ee, IMCECHARACTERIZABLE)) {
                        componentPackage = (Package)e;
                        if (StereotypesHelper.hasStereotypeOrDerived(ee, IMCECOMPONENT)
                                || StereotypesHelper.hasStereotypeOrDerived(ee, IMCECHARACTERIZABLE))
                            IMCEPresent = true;
                        // create tree after IMCEPresent is tagged
                        tree.addChild(fillComponent(componentPackage));
                        break;
                    }
                }
            }
        }

        if (missingInformation())
            return false;

        tree.sortAllChildren(new Comparator<Node<String, LibraryComponent>>() {
            @Override
            public int compare(Node<String, LibraryComponent> o1, Node<String, LibraryComponent> o2) {
                if (o1.getData().isPackage() && !o2.getData().isPackage())
                    return -1;
                if (o2.getData().isPackage() && !o1.getData().isPackage())
                    return 1;
                return o1.getData().getName().compareTo(o2.getData().getName());
            }
        });
        return true;
    }

    public void dump() {
        GUILog log = Application.getInstance().getGUILog();
        log.log("Characterizations:");
        for (NamedElement e: chars) {
            log.log("\t\t" + e.getName());
        }
        log.log("Components:");
        for (LibraryComponent lc: tree.getAllData()) {
            if (!lc.isPackage()) {
                if (lc.getElement() != null) {
                    log.log("\t\t" + lc.getElement().getName());
                } else {
                    log.log("\t\tnew " + lc.getName());
                }
                for (NamedElement e: lc.getCharacterizations()) {
                    log.log("\t\t\t\t" + e.getName());
                }
            }
        }
    }

    public Node<String, LibraryComponent> getRoot() {
        return tree;
    }

    public Set<NamedElement> getCharacterizations() {
        return chars;
    }

    public Set<NamedElement> getUsedChars() {
        return usedChars;
    }

    public void showChooser() {
        LibraryChooserUI chooserUI = new LibraryChooserUI(this);
        chooserUI.getFrame().setVisible(true);

    }

    public void apply() {
        GUILog log = Application.getInstance().getGUILog();
        try {
            sm.createSession("apply library mapping");
            applyInternal();
            sm.closeSession();
        } catch (Exception ex) {
            log.log("Save failed, make sure you have all necessary things locked");
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            log.log(sw.toString());
            sm.cancelSession();
        }
    }

    private void applyInternal() throws Exception {
        GUILog log = Application.getInstance().getGUILog();
        for (Node<String, LibraryComponent> lc: tree.getAllNodes()) {
            if (lc.getData().isPackage())
                continue;
            NamedElement e = lc.getData().getElement();
            if (e == null) {
                e = ef.createClassInstance();
                if (IMCEPresent)
                    StereotypesHelper.addStereotypeByString(e, IMCECOMPONENT);
                else
                    StereotypesHelper.addStereotypeByString(e, COMPONENT);
                e.setOwner(lc.getParent().getData().getElement());

            }
            if (!e.getName().equals(lc.getData().getName()))
                e.setName(lc.getData().getName());
            for (NamedElement addedChar: lc.getData().getAdded()) {
                addDependency(addedChar, e);
            }
            for (NamedElement removedChar: lc.getData().getRemoved()) {
                removeDependency(removedChar, e);
            }
        }
        log.log("Changes applied");
    }

    public boolean missingInformation() {
        return (charPackage == null || componentPackage == null);
    }

    /**
     * Refactor is really save, then refactor based on the library
     * characterizations
     */
    public void refactor() {
        GUILog log = Application.getInstance().getGUILog();

        try {
            sm.createSession("refactor library mappings");
            // apply all changes before doing the refactoring
            applyInternal();

            // start the refactoring of both characterizations and properties
            for (Node<String, LibraryComponent> lc: tree.getAllNodes()) {
                if (lc.getData().isPackage())
                    continue;
                NamedElement e = lc.getData().getElement();
                Set<NamedElement> characterizations = lc.getData().getCharacterizations();
                Collection<Classifier> derived = ModelHelper.getDerivedClassifiers((Classifier)e);
                if (!derived.isEmpty()) {
                    log.log("Refactoring instances of: " + e.getName());
                    for (Classifier c: derived) {
                        refactorCharacterizations(characterizations, c);
                        refactorCharacterizationProperties(characterizations, c);
                    }
                }
            }
            sm.closeSession();
            log.log("Refactor changes successfully applied");
        } catch (Exception ex) {
            log.log("Refactor failed, make sure you have all necessary things locked");
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            log.log(sw.toString());
            sm.cancelSession();
        }
    }

    private void refactorCharacterizations(Set<NamedElement> characterizations, Classifier classifier) {
        GUILog log = Application.getInstance().getGUILog();
        for (Element e: classifier.getOwnedElement()) {
            boolean missing = true;

            if (!(e instanceof Property || StereotypesHelper.hasStereotypeOrDerived(e, IMCECHAR) || StereotypesHelper
                    .hasStereotypeOrDerived(e, CHAR))) {
                continue;
            }

            // fchar is matching characterization
            NamedElement fchar = hasCharacterization(characterizations, e);
            if (fchar != null) {
                missing = false;
            }

            NamedElement ne = (NamedElement)e;
            if (missing) {
                if (!ne.getName().startsWith(DEPPREFIX)) {
                    log.log("Deprecated unreferenced characterization: " + e.getHumanName());
                    ne.setName(DEPPREFIX + ne.getName());
                }
            } else {
                if (ne.getName().startsWith(DEPPREFIX)) {
                    log.log("Undeprecating re-referenced characterization: " + e.getHumanName());
                    ne.setName(ne.getName().replace(DEPPREFIX, ""));
                }
            }
        }
    }

    private NamedElement hasCharacterization(Set<NamedElement> characterizations, Element e) {
        for (NamedElement c: characterizations) {
            if (e instanceof Property) {
                Type type = ((Property)e).getType();
                if (type.getName().equals(c.getName())) {
                    return c;
                }
            } else if (e instanceof Classifier) {
                Classifier ec = (Classifier)e;
                for (Classifier general: ec.getGeneral()) {
                    if (general == c) {
                        return c;
                    }
                }
            }
        }
        return null;
    }

    private void refactorCharacterizationProperties(Set<NamedElement> characterizations, Classifier classifier) {
        for (Element e: classifier.getOwnedElement()) {
            if (StereotypesHelper.hasStereotypeOrDerived(e, IMCECHAR)
                    || StereotypesHelper.hasStereotypeOrDerived(e, CHAR)) {
                NamedElement ne = hasCharacterization(characterizations, e);
                if (ne != null) {
                    MappingUtil.refactorProperties(ne, e, ef);
                }
            }
        }
    }

    private void fillChars() {
        for (Element e: Utils.collectOwnedElements(charPackage, 0)) {
            if (e instanceof Classifier
                    && (StereotypesHelper.hasStereotypeOrDerived(e, IMCECHAR) || StereotypesHelper
                            .hasStereotypeOrDerived(e, CHAR))) {
                chars.add((NamedElement)e);
            }
        }
    }

    private Node<String, LibraryComponent> fillComponent(NamedElement cur) {
        Node<String, LibraryComponent> node = new Node<String, LibraryComponent>(cur.getID(),
                new LibraryComponent(cur.getName(), cur));
        if (cur instanceof Package) {
            for (Element e: cur.getOwnedElement()) {
                if (e instanceof Package || StereotypesHelper.hasStereotypeOrDerived(e, IMCECOMPONENT)
                        || StereotypesHelper.hasStereotypeOrDerived(e, IMCECHARACTERIZABLE)
                        || StereotypesHelper.hasStereotypeOrDerived(e, COMPONENT)) {
                    node.addChild(fillComponent((NamedElement)e));
                }
            }
        }
        if (StereotypesHelper.hasStereotypeOrDerived(cur, IMCECOMPONENT)
                || StereotypesHelper.hasStereotypeOrDerived(cur, IMCECHARACTERIZABLE)
                || StereotypesHelper.hasStereotypeOrDerived(cur, COMPONENT)) {
            fillComponentChars(cur, node.getData());
        }
        return node;
    }

    private void fillComponentChars(NamedElement component, LibraryComponent com) {
        List<Element> directedRelatedElements;

        if (IMCEPresent) {
            directedRelatedElements = Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(
                    component, IMCECHARACTERIZES, 2, true, 1);
        } else {
            directedRelatedElements = Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(
                    component, CHARACTERIZES, 2, true, 1);
        }

        for (Element e: directedRelatedElements) {
            if (chars.contains(e)) {
                com.getCharacterizations().add((NamedElement)e);
                usedChars.add((NamedElement)e);
            }

        }
    }

    private void removeDependency(Element from, Element to) throws ReadOnlyElementException {
        ModelElementsManager mem = ModelElementsManager.getInstance();
        for (DirectedRelationship r: new HashSet<DirectedRelationship>(
                from.get_directedRelationshipOfSource())) {
            if (ModelHelper.getSupplierElement(r) == to && r instanceof Dependency) {
                mem.removeElement(r);
            }
        }
    }

    private void addDependency(Element from, Element to) throws ReadOnlyElementException {
        Dependency d = ef.createDependencyInstance();
        d.setOwner(to.getOwner());
        ModelHelper.setSupplierElement(d, to);
        ModelHelper.setClientElement(d, from);
        if (IMCEPresent)
            StereotypesHelper.addStereotypeByString(d, IMCECHARACTERIZES);
        else
            StereotypesHelper.addStereotypeByString(d, CHARACTERIZES);

    }

    @Override
    public List<DocumentElement> visit(boolean forViewEditor, String outputDir) {
        List<DocumentElement> res = new ArrayList<DocumentElement>();
        if (!init())
            return res;
        DBTable table = new DBTable();
        Node<String, LibraryComponent> root = getRoot();
        List<Element> chars = Utils.sortByName( Utils2.asList( getUsedChars(), Element.class ) );
        List<List<DocumentElement>> grid = new ArrayList<List<DocumentElement>>();
        List<List<DocumentElement>> headers = new ArrayList<List<DocumentElement>>();
        addLibraryRows(root, chars, grid, 1);
        table.setBody(grid);
        List<DocumentElement> headerrow = new ArrayList<DocumentElement>();
        headerrow.add(new DBText("Component"));
        for (Element charr: chars) {
            headerrow.add(new DBText(((NamedElement)charr).getName()));
        }
        headers.add(headerrow);
        table.setHeaders(headers);
        table.setCols(headerrow.size());
        table.setTitle("Possible Component Characterizations");
        res.add(table);
        return res;
    }

    private void addLibraryRows(Node<String, LibraryComponent> cur, List<Element> chars,
            List<List<DocumentElement>> grid, int depth) {
        LibraryComponent curc = cur.getData();
        List<DocumentElement> row = new ArrayList<DocumentElement>();
        row.add(new DBText(DocGenUtils.getIndented(curc.getName(), depth)));
        if (curc.isPackage()) {
            for (@SuppressWarnings("unused") Element charr: chars) {
                row.add(new DBText(""));
            }
        } else {
            for (Element charr: chars) {
                if (curc.hasCharacterization((NamedElement)charr))
                    row.add(new DBText("X"));
                else
                    row.add(new DBText(""));
            }
        }
        grid.add(row);
        for (Node<String, LibraryComponent> child: cur.getChildrenAsList()) {
            addLibraryRows(child, chars, grid, depth + 1);
        }
    }

    @Override
    public List<MDAction> getActions() {
        List<MDAction> res = new ArrayList<MDAction>();
        //res.add(new MapLibraryAction(this));
        return res;
    }
}
