package gov.nasa.jpl.mgss.mbee.docgen.model;

import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.tree.Node;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mgss.mbee.docgen.model.ui.CharacterizationChooserUI;
import gov.nasa.jpl.mgss.mbee.docgen.model.ui.LibraryChooserUI;
import gov.nasa.jpl.mgss.mbee.docgen.model.ui.LibraryComponent;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.ActivityNode;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Dependency;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DirectedRelationship;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Type;
import com.nomagic.uml2.impl.ElementsFactory;

public class LibraryMapping extends Query {

	private Package componentPackage;
	private Package charPackage;
	private Set<NamedElement> chars;
	private ElementsFactory ef;
	private SessionManager sm;
	private static final String CHAR = "Characterization";
	private static final String COMPONENT = "Component";
	private static final String IMCECOMPONENT= "mission:Component";
	private static final String IMCECHAR = "analysis:Characterization";
	private boolean IMCEPresent=false;
	private Node<String, LibraryComponent> tree;
	
	private Set<NamedElement> usedChars;

	@Override
	
	public void accept(IModelVisitor v) {	
		v.visit(this);
	}

	public boolean initialize() {
		chars = new HashSet<NamedElement>();
		usedChars = new HashSet<NamedElement>();
		ef = Application.getInstance().getProject().getElementsFactory();
		sm = SessionManager.getInstance();
		for (Element e: this.targets) {
			if (e instanceof Package) {
				for (Element ee: Utils.collectOwnedElements(e, 0)) {
					if (StereotypesHelper.hasStereotypeOrDerived(ee, IMCECHAR)||StereotypesHelper.hasStereotypeOrDerived(ee, CHAR)) {
						charPackage = (Package)e;
						if (StereotypesHelper.hasStereotypeOrDerived(ee, IMCECHAR))
								IMCEPresent=true;
						break;
					}
						
					 else if (StereotypesHelper.hasStereotypeOrDerived(ee, IMCECOMPONENT)||StereotypesHelper.hasStereotypeOrDerived(ee, COMPONENT)) {
						componentPackage = (Package)e;
						if(StereotypesHelper.hasStereotypeOrDerived(ee, IMCECOMPONENT))
							IMCEPresent=true;
						break;
					}
				}
			}
		}
		if (missingInformation())
			return false;
		fillChars();
		tree = fillComponent(componentPackage);
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
			for (Node<String, LibraryComponent> lc: tree.getAllNodes()) {
				if (lc.getData().isPackage())
					continue;
				NamedElement e = lc.getData().getElement();
				if (e == null) {
					e = ef.createClassInstance();
					if(IMCEPresent)
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
			sm.closeSession();
		} catch(Exception ex) {
			log.log("Save failed, make sure you have all necessary things locked");
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			ex.printStackTrace(pw);
			log.log(sw.toString());
			sm.cancelSession();
		}
	}
	
	
	public boolean missingInformation() {
		return (charPackage == null || componentPackage == null);
	}
	
	private void fillChars() {
		for (Element e: Utils.collectOwnedElements(charPackage, 0)) {
			if (e instanceof Classifier && (StereotypesHelper.hasStereotypeOrDerived(e, IMCECHAR)||StereotypesHelper.hasStereotypeOrDerived(e, CHAR))) {
				chars.add((NamedElement)e);
			}
		}
	}
	
	private Node<String, LibraryComponent> fillComponent(NamedElement cur) {
		Node<String, LibraryComponent> node = new Node<String, LibraryComponent>(cur.getID(), new LibraryComponent(cur.getName(), cur));
		if (cur instanceof Package) {
			for (Element e: cur.getOwnedElement()) {
				if (e instanceof Package || StereotypesHelper.hasStereotypeOrDerived(e,  IMCECOMPONENT)||StereotypesHelper.hasStereotypeOrDerived(e,  COMPONENT)) {
					node.addChild(fillComponent((NamedElement)e));
				}
			}
		}
		if (StereotypesHelper.hasStereotypeOrDerived(cur, IMCECOMPONENT)||StereotypesHelper.hasStereotypeOrDerived(cur, COMPONENT)){
			fillComponentChars(cur, node.getData());
		}
		return node;
	}
	
	private void fillComponentChars(NamedElement component, LibraryComponent com) {	
		GUILog log = Application.getInstance().getGUILog();
		if (IMCEPresent){
			for (Element e: Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(component, "analysis:characterizes", 2, true, 1)) {
				if (chars.contains(e)) {
					com.getCharacterizations().add((NamedElement)e);
					usedChars.add((NamedElement)e);
				}
			
			}
		}
		else
		{
			for (Element e: Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(component, "Characterizes", 2, true, 1)) {
				if (chars.contains(e)) {
					com.getCharacterizations().add((NamedElement)e);
					usedChars.add((NamedElement)e);
				}
				
			}
		}
	}
	
	private void removeDependency(Element from, Element to) throws ReadOnlyElementException {
		ModelElementsManager mem = ModelElementsManager.getInstance();
		for (DirectedRelationship r: new HashSet<DirectedRelationship>(from.get_directedRelationshipOfSource())) {
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
		if(IMCEPresent)
			StereotypesHelper.addStereotypeByString(d, "analysis:characterizes");
		else
			StereotypesHelper.addStereotypeByString(d, "Characterizes");

	}

	@Override
	public void initialize(ActivityNode an, List<Element> in) {
		
	}

	@Override
	public void parse() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public DocumentElement visit(boolean forViewEditor) {
		// TODO Auto-generated method stub
		return null;
	}
}
