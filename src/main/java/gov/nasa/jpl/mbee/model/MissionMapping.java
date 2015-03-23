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
import gov.nasa.jpl.mbee.actions.MapMissionAction;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.lib.Utils2;
import gov.nasa.jpl.mbee.model.ui.CharacterizationChooserUI;
import gov.nasa.jpl.mbee.model.ui.LibraryComponent;
import gov.nasa.jpl.mbee.model.ui.MissionCharacterization;
import gov.nasa.jpl.mbee.model.ui.MissionComponent;
import gov.nasa.jpl.mbee.tree.Node;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBTable;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBText;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.AggregationKindEnum;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Association;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Generalization;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Relationship;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Type;
import com.nomagic.uml2.impl.ElementsFactory;

public class MissionMapping extends Query {

    private Package                                         missionComponentPackage;
    private Package                                         libraryComponentPackage;
    private Package                                         libraryCharPackage;

    private Map<Element, LibraryComponent>                  libraryComponentMapping;
    private Map<Element, MissionCharacterization>           missionCharMapping;
    private Set<NamedElement>                               libraryChars;                                         // from
                                                                                                                   // imported
                                                                                                                   // scope

    private ElementsFactory                                 ef;
    private SessionManager                                  sm;
    private static final String                             CHAR                = "Characterization";
    private static final String                             COMPONENT           = "Component";
    private static final String                             IMCECOMPONENT       = "mission:Component";
    private static final String                             IMCECHAR            = "analysis:Characterization";
    private static final String                             IMCECHARACTERIZABLE = "analysis:CharacterizedElement";
    private static final String                             IMCECHARACTERIZES   = "analysis:characterizes";
    private static final String                             CHARACTERIZES       = "Characterizes";
    private static final String                             DEPPREFIX           = "zz_";
    private boolean                                         IMCEPresent         = false;

    private Node<String, MissionComponent>                  tree;
    private Set<LibraryComponent>                           libraryComponents;
    private Map<NamedElement, Set<MissionCharacterization>> library2missionChars;
    private Set<NamedElement>                               chars;                                                // actual
                                                                                                                   // possible
                                                                                                                   // chars
                                                                                                                   // given
                                                                                                                   // library
                                                                                                                   // components
                                                                                                                   // available
                                                                                                                   // from
                                                                                                                   // scope

    private boolean hasCharacterizesDependency(Element e) {
        for (Relationship s: e.get_relationshipOfRelatedElement()) {
            if (StereotypesHelper.hasStereotypeOrDerived(s, CHARACTERIZES))
                return true;
            else if (StereotypesHelper.hasStereotypeOrDerived(s, IMCECHARACTERIZES)) {
                IMCEPresent = true;
                return true;
            }
        }

        return false;
    }

    public boolean init() {
        ef = Application.getInstance().getProject().getElementsFactory();
        sm = SessionManager.getInstance();
        libraryComponents = new HashSet<LibraryComponent>();
        library2missionChars = new HashMap<NamedElement, Set<MissionCharacterization>>();

        libraryComponentMapping = new HashMap<Element, LibraryComponent>();
        missionCharMapping = new HashMap<Element, MissionCharacterization>();
        libraryChars = new HashSet<NamedElement>();
        chars = new HashSet<NamedElement>();

        for (Object e: this.targets) {
            if (e instanceof Package) {
                for (Element ee: Utils.collectOwnedElements((Package)e, 0)) {
                    if (ee instanceof Classifier && hasCharacterizesDependency(ee)) {
                        if (StereotypesHelper.hasStereotypeOrDerived(ee, CHAR)
                                || StereotypesHelper.hasStereotypeOrDerived(ee, IMCECHAR)) {
                            libraryCharPackage = (Package)e;
                            break;
                        } else if (StereotypesHelper.hasStereotypeOrDerived(ee, COMPONENT)
                                || StereotypesHelper.hasStereotypeOrDerived(ee, IMCECOMPONENT)
                                || StereotypesHelper.hasStereotypeOrDerived(ee, IMCECHARACTERIZABLE)) {
                            libraryComponentPackage = (Package)e;
                            break;
                        }
                    }
                }
                if (libraryComponentPackage != null && libraryCharPackage != null)
                    break;
            }
        }
        for (Object e: this.targets) {
            if (e instanceof Package && e != libraryComponentPackage && e != libraryCharPackage) {
                missionComponentPackage = (Package)e;
            }
        }
        if (missionComponentPackage == null)
            return false;

        fillLibrary();
        fillMissionChars();
        tree = fillMission(missionComponentPackage);
        tree.setComparator(new Comparator<Node<String, MissionComponent>>() {
            @Override
            public int compare(Node<String, MissionComponent> o1, Node<String, MissionComponent> o2) {
                if (o1.getData().isPackage() && !o2.getData().isPackage())
                    return -1;
                if (o2.getData().isPackage() && !o1.getData().isPackage())
                    return 1;
                return o1.getData().getName().compareTo(o2.getData().getName());
            }
        });
        tree.sortAllChildren();
        return true;
    }

    public void dump() {
        GUILog log = Application.getInstance().getGUILog();
        log.log("Library Components:");
        for (LibraryComponent lc: this.libraryComponents) {
            log.log("\t\t" + lc.getName());
        }
        log.log("Library Characterizations:");
        for (NamedElement chars: this.chars) {
            log.log("\t\t" + chars.getName());
        }
        log.log("Mission Components:");
        for (MissionComponent lc: tree.getAllData()) {
            if (!lc.isPackage()) {
                if (lc.getElement() != null) {
                    log.log("\t\t" + lc.getElement().getName());
                } else {
                    log.log("\t\tnew " + lc.getName());
                }
                log.log("\t\t\tSpecializes:");
                for (LibraryComponent cc: lc.getLibraryComponents()) {
                    if (libraryComponents.contains(cc))
                        log.log("\t\t\t\t" + cc.getName());
                    else
                        log.log("\t\t\t\t" + cc.getName() + " (Out of scope)");
                    log.log("\t\t\t\tCharacterizations:");
                    for (NamedElement chars: cc.getCharacterizations()) {
                        log.log("\t\t\t\t\t" + chars.getHumanName());
                    }
                }
                log.log("\t\t\tCharacterizations:");
                for (MissionCharacterization e: lc.getMissionCharacterizations()) {
                    NamedElement ac = e.getLibraryCharacterization();
                    log.log("\t\t\t\t" + e.getName() + ": " + ac.getName());
                }
            }
        }
    }

    public Node<String, MissionComponent> getRoot() {
        return tree;
    }

    public void showChooser() {
        CharacterizationChooserUI chooserUI = new CharacterizationChooserUI(this);
        chooserUI.getFrame().setVisible(true);
    }

    public void apply() {
        GUILog log = Application.getInstance().getGUILog();
        try {
            sm.createSession("apply mission mapping");
            applyInternal();
            sm.closeSession();
            log.log("saved");
        } catch (Exception e) {
            log.log("Save failed, make sure you have all necessary things locked");
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            log.log(sw.toString());
            sm.cancelSession();
        }
    }

    private void applyInternal() throws Exception {
        for (Node<String, MissionComponent> mc: tree.getAllNodes()) {
            NamedElement e = mc.getData().getElement();
            if (e == null) {
                e = ef.createClassInstance();
                if (IMCEPresent) {
                    StereotypesHelper.addStereotypeByString(e, IMCECOMPONENT);
                } else {
                    StereotypesHelper.addStereotypeByString(e, COMPONENT);
                }

                e.setOwner(mc.getParent().getData().getElement());
            }
            if (!e.getName().equals(mc.getData().getName()))
                e.setName(mc.getData().getName());
            if (mc.getData().isPackage())
                continue;
            for (LibraryComponent lc: mc.getData().getAddedLib()) {
                addSpecialization((Classifier)e, (Classifier)lc.getElement());
            }
            for (LibraryComponent lc: mc.getData().getRemovedLib()) {
                removeSpecialization((Classifier)e, (Classifier)lc.getElement());
            }
            for (MissionCharacterization mmc: mc.getData().getAddedChar()) {
                Classifier c = (Classifier)mmc.getElement();
                if (existsProperty(e, c))
                    continue;
                if (c == null) {

                    if (IMCEPresent) {
                        c = ef.createComponentInstance();
                        StereotypesHelper.addStereotypeByString(c, IMCECHAR);
                    } else {
                        c = ef.createClassInstance();
                        StereotypesHelper.addStereotypeByString(c, CHAR);
                    }
                    c.setName(mmc.getName());
                    c.setOwner(e);

                    Generalization g = ef.createGeneralizationInstance();
                    g.setOwner(c);
                    g.setSpecific(c);
                    g.setGeneral((Classifier)mmc.getLibraryCharacterization());
                    Utils.copyStereotypes(mmc.getLibraryCharacterization(), c);
                    bst((Class)c);
                }
                Association a = ef.createAssociationInstance();
                a.setOwner(e.getOwner());
                Property p1 = a.getMemberEnd().get(0);
                Property p2 = a.getMemberEnd().get(1);
                p1.setName(mmc.getName());
                p1.setType(c);
                p1.setOwner(e);
                p1.setAggregation(AggregationKindEnum.COMPOSITE);
                StereotypesHelper.addStereotypeByString(p1, "PartProperty");
                p2.setType((Type)e);
            }
            for (MissionCharacterization mmc: mc.getData().getRemovedChar()) {
                for (Property p: new HashSet<Property>(((Classifier)e).getAttribute())) {
                    if (p.getType() == mmc.getElement()) {
                        ModelElementsManager.getInstance().removeElement(p.getType());
                        ModelElementsManager.getInstance().removeElement(p);
                    }
                }
            }
        }

    }

    public void refactor() {
        GUILog log = Application.getInstance().getGUILog();

        try {
            sm.createSession("refactor mission mappings");
            applyInternal();

            for (MissionCharacterization mc: missionCharMapping.values()) {
                MappingUtil.refactorProperties(mc.getLibraryCharacterization(), mc.getElement(), ef);
            }

            for (MissionComponent lc: tree.getAllData()) {
                if (!lc.isPackage()) {
                    Set<String> libChars = new HashSet<String>();
                    Set<String> missionChars = new HashSet<String>();
                    for (LibraryComponent cc: lc.getLibraryComponents()) {
                        for (NamedElement c: cc.getCharacterizations()) {
                            libChars.add(c.getName());
                        }
                    }
                    Element elem = lc.getElement();
                    // find any characterizations that arent in library
                    // characterizations
                    for (Element c: elem.getOwnedElement()) {
                        if (StereotypesHelper.hasStereotypeOrDerived(c, IMCECHAR)
                                || StereotypesHelper.hasStereotypeOrDerived(c, CHAR)) {
                            NamedElement ne = (NamedElement)c;
                            missionChars.add(ne.getName());
                            if (!libChars.contains(ne.getName())) {
                                deprecateName(ne);
                            }
                        }
                    }
                    // find any part properties that aren't in library
                    // characterizations
                    for (Element c: elem.getOwnedElement()) {
                        if (StereotypesHelper.hasStereotypeOrDerived(c, "PartProperty")) {
                            NamedElement ne = (NamedElement)c;
                            if (missionChars.contains(ne.getName())) {
                                if (!libChars.contains(ne.getName())) {
                                    deprecateName(ne);
                                }
                            }
                        }
                    }

                    // undeprecate if something was added back in
                    for (String libChar: libChars) {
                        if (!missionChars.contains(libChar)) {
                            for (Element c: elem.getOwnedElement()) {
                                if (c instanceof NamedElement) {
                                    NamedElement ne = (NamedElement)c;
                                    if (ne.getName().replace(DEPPREFIX, "").equals(libChar)) {
                                        ne.setName(ne.getName().replace(DEPPREFIX, ""));
                                        log.log("Undeprecated referenced characterization: "
                                                + ne.getHumanName());
                                    }
                                }
                            }
                        }
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

    private void deprecateName(Element e) {
        GUILog log = Application.getInstance().getGUILog();
        if (e instanceof NamedElement) {
            NamedElement ne = (NamedElement)e;
            if (!ne.getName().startsWith(DEPPREFIX)) {
                ne.setName(DEPPREFIX + ne.getName());
                log.log("Deprecated unreferenced characterization: " + ne.getHumanName());
            }
        }
    }

    private boolean existsProperty(NamedElement e, Classifier c) {
        for (NamedElement p: ((Classifier)e).getOwnedMember()) {
            if (p instanceof Property && ((Property)p).getType() == c)
                return true;
        }
        return false;
    }

    private void addSpecialization(Classifier from, Classifier to) {
        if (from.getGeneral().contains(to))
            return;
        Generalization g = ef.createGeneralizationInstance();
        g.setGeneral(to);
        g.setSpecific(from);
        g.setOwner(from);
    }

    private void removeSpecialization(Classifier from, Classifier to) throws ReadOnlyElementException {
        for (Generalization g: new HashSet<Generalization>(from.getGeneralization())) {
            if (g.getGeneral() == to) {
                ModelElementsManager.getInstance().removeElement(g);
            }
        }
    }

    private void bst(Class c) {
        for (NamedElement p: c.getInheritedMember()) {
            if (p instanceof Property) {
                Property np = null;
                if (((Property)p).getAssociation() != null) {
                    Association asso = ef.createAssociationInstance();
                    asso.setOwner(c.getOwner());
                    np = asso.getMemberEnd().get(0);
                    asso.getMemberEnd().get(1).setOwner(asso);
                    asso.getMemberEnd().get(1).setType(c);
                    Generalization gen = ef.createGeneralizationInstance();
                    gen.setOwner(asso);
                    gen.setGeneral(((Property)p).getAssociation());
                    gen.setSpecific(asso);
                } else
                    np = ef.createPropertyInstance();
                np.setName(p.getName());
                np.setOwner(c);
                np.setType(((Property)p).getType());
                np.setAggregation(((Property)p).getAggregation());
                np.getRedefinedProperty().add((Property)p);
                Utils.copyStereotypes(p, np);
            }
        }
    }

    public boolean missingInformation() {
        return (missionComponentPackage == null);
    }

    private void fillLibrary() {
        if (libraryCharPackage != null) {
            for (Element e: Utils.collectOwnedElements(libraryCharPackage, 0)) {
                if (e instanceof Classifier && (StereotypesHelper.hasStereotypeOrDerived(e, CHAR))
                        || StereotypesHelper.hasStereotypeOrDerived(e, IMCECHAR)) {
                    libraryChars.add((NamedElement)e);
                }
            }
        }
        if (libraryComponentPackage != null) {
            for (Element e: Utils.collectOwnedElements(libraryComponentPackage, 0)) {
                if (e instanceof Classifier
                        && (StereotypesHelper.hasStereotypeOrDerived(e, COMPONENT)
                                || StereotypesHelper.hasStereotypeOrDerived(e, IMCECOMPONENT) || StereotypesHelper
                                    .hasStereotypeOrDerived(e, IMCECHARACTERIZABLE))) {
                    addLibraryComponent(e, true);
                }
            }
        }
        for (Element e: Utils.collectOwnedElements(missionComponentPackage, 0)) {
            if (e instanceof Classifier
                    && (StereotypesHelper.hasStereotypeOrDerived(e, COMPONENT)
                            || StereotypesHelper.hasStereotypeOrDerived(e, IMCECOMPONENT) || StereotypesHelper
                                .hasStereotypeOrDerived(e, IMCECHARACTERIZABLE))) {
                for (Element g: ((Classifier)e).getGeneral()) {
                    if (libraryComponentMapping.containsKey(g))
                        continue;
                    if (g instanceof Classifier
                            && (StereotypesHelper.hasStereotypeOrDerived(g, COMPONENT)
                                    || StereotypesHelper.hasStereotypeOrDerived(g, IMCECOMPONENT) || StereotypesHelper
                                        .hasStereotypeOrDerived(g, IMCECHARACTERIZABLE))) {
                        if (libraryComponentPackage == null)
                            addLibraryComponent(g, true);
                        else
                            addLibraryComponent(g, false);
                    }
                }
            }
        }

    }

    private void addLibraryComponent(Element e, boolean inScope) {
        LibraryComponent lc = new LibraryComponent(((NamedElement)e).getName(), (NamedElement)e);
        if (IMCEPresent) {
            for (Element c: Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(e,
                    IMCECHARACTERIZES, 2, true, 1)) {
                if (StereotypesHelper.hasStereotypeOrDerived(c, IMCECHAR)) {
                    lc.getCharacterizations().add((NamedElement)c);
                    if ((libraryChars.contains(c) || libraryCharPackage == null) && inScope) {
                        chars.add((NamedElement)c);
                    }
                }
            }
        } else {
            for (Element c: Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(e,
                    CHARACTERIZES, 2, true, 1)) {
                if (StereotypesHelper.hasStereotypeOrDerived(c, CHAR)) {
                    lc.getCharacterizations().add((NamedElement)c);
                    if ((libraryChars.contains(c) || libraryCharPackage == null) && inScope) {
                        chars.add((NamedElement)c);
                    }
                }
            }
        }
        libraryComponentMapping.put(e, lc);
        if (inScope)
            libraryComponents.add(lc);
    }

    private Node<String, MissionComponent> fillMission(NamedElement cur) {
        Node<String, MissionComponent> node = new Node<String, MissionComponent>(cur.getID(),
                new MissionComponent(cur.getName(), cur));
        if (cur instanceof Package) {
            if (IMCEPresent) {
                for (Element e: cur.getOwnedElement()) {
                    if (e instanceof Package
                            || (e instanceof Classifier && (StereotypesHelper.hasStereotypeOrDerived(e,
                                    IMCECOMPONENT) || StereotypesHelper.hasStereotypeOrDerived(e,
                                    IMCECHARACTERIZABLE)))) {
                        node.addChild(fillMission((NamedElement)e));
                    }
                }
            } else {
                for (Element e: cur.getOwnedElement()) {
                    if (e instanceof Package
                            || (e instanceof Classifier && StereotypesHelper.hasStereotypeOrDerived(e,
                                    COMPONENT))) {
                        node.addChild(fillMission((NamedElement)e));
                    }
                }
            }

        } else if (StereotypesHelper.hasStereotypeOrDerived(cur, COMPONENT)
                || StereotypesHelper.hasStereotypeOrDerived(cur, IMCECOMPONENT)
                || StereotypesHelper.hasStereotypeOrDerived(cur, IMCECHARACTERIZABLE)) {
            fillComponentChars(cur, node.getData());
            fillComponentLib(cur, node.getData());
            node.getData().updateLibrary2MissionCharMapping();
        }
        return node;
    }

    private void fillComponentLib(NamedElement component, MissionComponent com) {
        for (Classifier c: ((Classifier)component).getGeneral()) {
            if (libraryComponentMapping.containsKey(c)) {
                com.getLibraryComponents().add(libraryComponentMapping.get(c));
            }
        }
    }

    private void fillComponentChars(NamedElement component, MissionComponent com) {
        for (Property p: ((Class)component).getOwnedAttribute()) {
            Type t = p.getType();
            if (t != null
                    && (StereotypesHelper.hasStereotypeOrDerived(t, CHAR) || StereotypesHelper
                            .hasStereotypeOrDerived(t, IMCECHAR))) {
                if (missionCharMapping.containsKey(t)) {
                    com.getMissionCharacterizations().add(missionCharMapping.get(t));
                    continue;
                }
                for (Element e: ((Classifier)t).getGeneral()) {
                    if (chars.contains(e)) {// show actual possible chars in
                                            // scope
                        MissionCharacterization mc = addMissionChar(t, (NamedElement)e);
                        com.getMissionCharacterizations().add(mc);
                        break;
                    }
                }
            }
        }
    }

    private MissionCharacterization addMissionChar(NamedElement mission, NamedElement library) {
        MissionCharacterization mc = new MissionCharacterization(mission.getName(), mission);
        mc.setLibraryCharacterization(library);
        if (!library2missionChars.containsKey(library)) {
            library2missionChars.put(library, new HashSet<MissionCharacterization>());
        }
        library2missionChars.get(library).add(mc);
        missionCharMapping.put(mission, mc);
        return mc;
    }

    private void fillMissionChars() {
        for (Element e: Utils.collectOwnedElements(missionComponentPackage, 0)) {
            if (e instanceof Classifier
                    && (StereotypesHelper.hasStereotypeOrDerived(e, CHAR) || StereotypesHelper
                            .hasStereotypeOrDerived(e, IMCECHAR))) {
                for (Classifier general: ((Classifier)e).getGeneral()) {
                    if (chars.contains(general)) {
                        addMissionChar((NamedElement)e, general);
                    }
                }
            }
        }
    }

    public Set<LibraryComponent> getLibraryComponents() {
        return libraryComponents;
    }

    public Map<NamedElement, Set<MissionCharacterization>> getLibrary2MissionChars() {
        return library2missionChars;
    }

    public Set<NamedElement> getLibraryCharacterizations() {
        return chars;
    }

    @Override
    public List<DocumentElement> visit(boolean forViewEditor, String outputDir) {
        List<DocumentElement> res = new ArrayList<DocumentElement>();
        if (!init())
            return res;
        DBTable table = new DBTable();
        Node<String, MissionComponent> root = getRoot();
        List<Element> chars = Utils.sortByName(Utils2.asList(getLibraryCharacterizations(), Element.class));
        List<List<DocumentElement>> grid = new ArrayList<List<DocumentElement>>();
        List<List<DocumentElement>> headers = new ArrayList<List<DocumentElement>>();
        addMissionRows(root, chars, grid, 1);
        table.setBody(grid);
        List<DocumentElement> headerrow = new ArrayList<DocumentElement>();
        headerrow.add(new DBText("Component"));
        headerrow.add(new DBText("Inherits From"));
        for (Element charr: chars) {
            headerrow.add(new DBText(((NamedElement)charr).getName()));
        }
        headers.add(headerrow);
        table.setHeaders(headers);
        table.setCols(headerrow.size());
        table.setTitle("Component Characterizations");
        res.add(table);
        return res;
    }

    private void addMissionRows(Node<String, MissionComponent> cur, List<Element> chars,
            List<List<DocumentElement>> grid, int depth) {
        MissionComponent curc = cur.getData();
        List<DocumentElement> row = new ArrayList<DocumentElement>();
        row.add(new DBText(DocGenUtils.getIndented(curc.getName(), depth)));
        if (curc.isPackage()) {
            for (@SuppressWarnings("unused") Element charr: chars) {
                row.add(new DBText(""));
            }
            row.add(new DBText(""));
        } else {
            String inherits = "";
            int i = 0;
            for (LibraryComponent lc: curc.getLibraryComponents()) {
                if (i == 0)
                    inherits = inherits + lc.getName();
                else
                    inherits = ", " + inherits + lc.getName();
            }
            row.add(new DBText(inherits));
            for (Element charr: chars) {
                if (curc.hasLibraryCharacterization((NamedElement)charr))
                    row.add(new DBText("X"));
                else
                    row.add(new DBText(""));
            }
        }
        grid.add(row);
        for (Node<String, MissionComponent> child: cur.getChildrenAsList()) {
            addMissionRows(child, chars, grid, depth + 1);
        }
    }

    @Override
    public List<MDAction> getActions() {
        List<MDAction> res = new ArrayList<MDAction>();
        //res.add(new MapMissionAction(this));
        return res;
    }

}
