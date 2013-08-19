package gov.nasa.jpl.mgss.mbee.docgen.model;

import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.tree.Node;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mgss.mbee.docgen.model.ui.CharacterizationChooserUI;
import gov.nasa.jpl.mgss.mbee.docgen.model.ui.LibraryComponent;
import gov.nasa.jpl.mgss.mbee.docgen.model.ui.MissionCharacterization;
import gov.nasa.jpl.mgss.mbee.docgen.model.ui.MissionComponent;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.ActivityNode;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Dependency;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.AggregationKind;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.AggregationKindEnum;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Association;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DirectedRelationship;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Generalization;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Relationship;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Type;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

import com.nomagic.uml2.impl.ElementsFactory;

public class MissionMapping extends Query {

	private Package missionComponentPackage;
	private Package libraryComponentPackage;
	private Package libraryCharPackage;
	
	private Map<Element, LibraryComponent> libraryComponentMapping;
	private Map<Element, MissionCharacterization> missionCharMapping;
	private Set<NamedElement> libraryChars; //from imported scope
	
	private ElementsFactory ef;
	private SessionManager sm;
	private static final String CHAR = "Characterization";
	private static final String COMPONENT = "Component";
	private static final String IMCECOMPONENT= "mission:Component";
	private static final String IMCECHAR = "analysis:Characterization";
	private boolean IMCEPresent=false;
	
	private Node<String, MissionComponent> tree;
	private Set<LibraryComponent> libraryComponents;
	private Map<NamedElement, Set<MissionCharacterization>> library2missionChars;
	private Set<NamedElement> chars; //actual possible chars given library components available from scope
	
	@Override
	public void accept(IModelVisitor v) {	
		v.visit(this);
	}
	
	private boolean hasCharacterizesDependency(Element e) {
		for (Relationship s: e.get_relationshipOfRelatedElement()) {
			if (StereotypesHelper.hasStereotypeOrDerived(s, "Characterizes") )
				return true;
			else if (StereotypesHelper.hasStereotypeOrDerived(s, "analysis:characterizes")){
				IMCEPresent=true;
				return true;
			}
		}
		
		return false;
	}
	
	

	public boolean initialize() {
		ef = Application.getInstance().getProject().getElementsFactory();
		sm = SessionManager.getInstance();
		libraryComponents = new HashSet<LibraryComponent>();
		library2missionChars = new HashMap<NamedElement, Set<MissionCharacterization>>();
		
		libraryComponentMapping = new HashMap<Element, LibraryComponent>();
		missionCharMapping = new HashMap<Element, MissionCharacterization>();
		libraryChars = new HashSet<NamedElement>();
		chars = new HashSet<NamedElement>();
		
		
		for (Element e: this.targets) {
			if (e instanceof Package) {
				for (Element ee: Utils.collectOwnedElements(e, 0)) {
					if (ee instanceof Classifier && hasCharacterizesDependency(ee)) {
						if (StereotypesHelper.hasStereotypeOrDerived(ee, CHAR) || StereotypesHelper.hasStereotypeOrDerived(ee, IMCECHAR)) {
							libraryCharPackage = (Package)e;
							break;
						} else if (StereotypesHelper.hasStereotypeOrDerived(ee, COMPONENT) || StereotypesHelper.hasStereotypeOrDerived(ee, IMCECOMPONENT)) {
							libraryComponentPackage = (Package)e;
							break;
						}
					} 
				}
				if (libraryComponentPackage != null && libraryCharPackage != null)
					break;
			}
		}
		for (Element e: this.targets) {
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
			public int compare(Node<String, MissionComponent> o1,
					Node<String, MissionComponent> o2) {
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
			log.log("\t\t" + chars.getName() );
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
			for (Node<String, MissionComponent> mc: tree.getAllNodes()) {
				NamedElement e = mc.getData().getElement();
				if (e == null) {
					e = ef.createClassInstance();
					if (IMCEPresent){
						StereotypesHelper.addStereotypeByString(e, IMCECOMPONENT);
					}
					else{
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
						c = ef.createClassInstance();
						c.setName(mmc.getName());
						c.setOwner(e);
						if(IMCEPresent){
							StereotypesHelper.addStereotypeByString(c, IMCECHAR);
						}
						else {
							StereotypesHelper.addStereotypeByString(c, CHAR);
						}
						
						Generalization g = ef.createGeneralizationInstance();
						g.setOwner(c);
						g.setSpecific(c);
						g.setGeneral((Classifier)mmc.getLibraryCharacterization());
						Utils.copyStereotypes((Classifier)mmc.getLibraryCharacterization(), c);
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
			sm.closeSession();
			log.log("saved");
		} catch(Exception e) {
			log.log("Save failed, make sure you have all necessary things locked");
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			log.log(sw.toString());
			sm.cancelSession();
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
				if (((Property) p).getAssociation() != null) {
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
				if (e instanceof Classifier && (StereotypesHelper.hasStereotypeOrDerived(e, CHAR)) || StereotypesHelper.hasStereotypeOrDerived(e, IMCECHAR)) {
					libraryChars.add((NamedElement)e);
				}
			}
		} 
		if (libraryComponentPackage != null) {
			for (Element e: Utils.collectOwnedElements(libraryComponentPackage, 0)) {
				if (e instanceof Classifier && (StereotypesHelper.hasStereotypeOrDerived(e, COMPONENT)||StereotypesHelper.hasStereotypeOrDerived(e, IMCECOMPONENT))) {
					addLibraryComponent(e, true);
				}
			}
		} 
		for (Element e: Utils.collectOwnedElements(missionComponentPackage, 0)) {
			if (e instanceof Classifier && (StereotypesHelper.hasStereotypeOrDerived(e, COMPONENT) || StereotypesHelper.hasStereotypeOrDerived(e, IMCECOMPONENT))) {
				for (Element g: ((Classifier)e).getGeneral()) {
					if (libraryComponentMapping.containsKey(g))
						continue;
					if (g instanceof Classifier && (StereotypesHelper.hasStereotypeOrDerived(g, COMPONENT)||StereotypesHelper.hasStereotypeOrDerived(g, IMCECOMPONENT))) {
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
		if (IMCEPresent){
			for (Element c: Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(e, "analysis:characterizes", 2, true, 1)) {
				if (StereotypesHelper.hasStereotypeOrDerived(c, IMCECHAR)) {
					lc.getCharacterizations().add((NamedElement)c);
					if  ((libraryChars.contains(c) || libraryCharPackage == null) && inScope) {
						chars.add((NamedElement)c);
					}
				}
			}
		}
		else{
			
		for (Element c: Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(e, "Characterizes", 2, true, 1)) {
			if (StereotypesHelper.hasStereotypeOrDerived(c, CHAR)) {
				lc.getCharacterizations().add((NamedElement)c);
				if  ((libraryChars.contains(c) || libraryCharPackage == null) && inScope) {
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
		Node<String, MissionComponent> node = new Node<String, MissionComponent>(cur.getID(), new MissionComponent(cur.getName(), cur));
		if (cur instanceof Package) {
			if(IMCEPresent){
				for (Element e: cur.getOwnedElement()) {
					if (e instanceof Package || (e instanceof Classifier && StereotypesHelper.hasStereotypeOrDerived(e, IMCECOMPONENT))) {
						node.addChild(fillMission((NamedElement)e));
					}
				}
			}
			else{
				for (Element e: cur.getOwnedElement()) {
					if (e instanceof Package || (e instanceof Classifier && StereotypesHelper.hasStereotypeOrDerived(e, COMPONENT))) {
						node.addChild(fillMission((NamedElement)e));
					}
				}
			}
				
		} else if (StereotypesHelper.hasStereotypeOrDerived(cur, COMPONENT)||StereotypesHelper.hasStereotypeOrDerived(cur, IMCECOMPONENT)) {
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
			if (t != null && (StereotypesHelper.hasStereotypeOrDerived(t, CHAR)||StereotypesHelper.hasStereotypeOrDerived(t, IMCECHAR))) {
				if (missionCharMapping.containsKey(t)) {
					com.getMissionCharacterizations().add(missionCharMapping.get(t));
					continue;
				}
				for (Element e: ((Classifier)t).getGeneral()) {
					if (chars.contains(e)) {//show actual possible chars in scope
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
			if (e instanceof Classifier && (StereotypesHelper.hasStereotypeOrDerived(e, CHAR)||StereotypesHelper.hasStereotypeOrDerived(e, IMCECHAR))) {
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
