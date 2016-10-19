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

package gov.nasa.jpl.mbee.mdk.test;

import com.nomagic.magicdraw.copypaste.CopyPasting;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.project.ProjectDescriptor;
import com.nomagic.magicdraw.core.project.ProjectDescriptorsFactory;
import com.nomagic.magicdraw.core.project.ProjectsManager;
import com.nomagic.magicdraw.esi.EsiUtils;
import com.nomagic.magicdraw.merge.MergeUtil;
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.teamwork.application.TeamworkUtils;
import com.nomagic.magicdraw.teamwork2.locks.ILockProjectService;
import com.nomagic.magicdraw.uml.DiagramTypeConstants;
import com.nomagic.magicdraw.uml.Finder;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Dependency;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;

import com.nomagic.uml2.ext.magicdraw.components.mdbasiccomponents.Component;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.impl.ElementsFactory;
import com.nomagic.utils.ErrorHandler;
import gov.nasa.jpl.mbee.mdk.api.ElementFinder;
import gov.nasa.jpl.mbee.mdk.ems.ImportUtility;
import gov.nasa.jpl.mbee.mdk.lib.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class has functions that uses the MDK api to test the MDK actions.
 * 
 */
public class MagicDrawHelper {

	private static Project project;
	private static ElementsFactory ef;

	private static void createSession() {
		if (!SessionManager.getInstance().isSessionCreated()) {
			SessionManager.getInstance().createSession("Automated changes");
		}
	}

	private static void cancelSession() {
		if (SessionManager.getInstance().isSessionCreated()) {
			SessionManager.getInstance().cancelSession();
		}
	}

	private static void closeSession() {
		if (SessionManager.getInstance().isSessionCreated()) {
			SessionManager.getInstance().closeSession();
		}
	}

	private static void initializeFactory() {
		if (ef != null)
			return;
		project = Application.getInstance().getProject();
		ef = project.getElementsFactory();
	}
	
	/**
	 * Prints a message to console and MD log 
	 */
	
	public static void generalMessage(String s) {
		Application instance = Application.getInstance();
		instance.getGUILog().log(s);
		System.out.println(s);
	}


	public static Element finishElement(Element newElement, String name, Element owner) {
		if (newElement instanceof NamedElement && !(name == null || name.isEmpty() )) {
			((NamedElement)newElement).setName(name);
		}
		newElement.setOwner(owner);
		return newElement;
	}

	/**
	 * Returns the user who holds a lock on the element, or null if there is no
	 * lock holder
	 * 
	 * @param target
	 *            The element you want the lock information from
	 */

	public static boolean confirmElementLocked(Element target) {
		List<Element> lockedElements = new ArrayList<Element>();
		lockedElements.addAll(TeamworkUtils.getLockedElement(Application.getInstance().getProject(), null));
		return lockedElements.contains(target);
	}

	/**
	 * Copies specific elements to a location
	 * 
	 * @param elementToCopy
	 * @param copyTarget
	 */
	public static Element copyAndPaste(Element elementToCopy, Element copyTarget) throws Exception {
		Element newCopy = null;

		if (elementToCopy != null && copyTarget != null) {
			try {
				createSession();
				newCopy = CopyPasting.copyPasteElement(elementToCopy, copyTarget, true);
				closeSession();
			} catch (Exception e) {
				cancelSession();
				throw e;
			}
		}
		return newCopy;
	}
	
    public static void prepareMMS(String url, String site) {
		createSession();
		String s;
		s = "Model Management System";
		if (StereotypesHelper.hasStereotype(ElementFinder.getModelRoot(), s)) {
        	System.out.println("Found " + s);
        }
        Stereotype mms = StereotypesHelper.getStereotype(project, s);
		if (mms != null) {
			System.out.println("Stereotype found " + s);
		}
		s = "ModelManagementSystem";
		if (StereotypesHelper.hasStereotype(ElementFinder.getModelRoot(), s)) {
        	System.out.println("Found " + s);
        }
		mms = StereotypesHelper.getStereotype(project, s);
		if (mms != null) {
			System.out.println("Stereotype found " + s);
		}

		
		System.out.println(StereotypesHelper.canApplyStereotype(ElementFinder.getModelRoot(), mms));
		if (!StereotypesHelper.hasStereotype(ElementFinder.getModelRoot(), "ModelManagementSystem")) {
			StereotypesHelper.addStereotype(ElementFinder.getModelRoot(), mms);
		}
		StereotypesHelper.setStereotypePropertyValue(ElementFinder.getModelRoot(), mms, "MMS Site", site);
		StereotypesHelper.setStereotypePropertyValue(ElementFinder.getModelRoot(), mms, "MMS URL", url);
		
		closeSession();
	}
	
	private static Property getTagProperty(String name, Stereotype ster) {
		List<Element> ste = StereotypesHelper.getExtendedElements(ster);
		for (Element elem : ste) {
			if (ster.hasOwnedAttribute()) {
				List<Property> attribs = ster.getOwnedAttribute();
				for (Property tag : attribs) {
					System.out.println(tag.getName());
					if (tag.getName().equals(name)) {
						List<?> value = StereotypesHelper.getStereotypePropertyValue(elem, ster, tag.getName());
						for (Object val :value) {
							if (val instanceof LiteralString) {
								System.out.println(((LiteralString)val).getValue() + " " + ((LiteralString)val).isEditable());
							}
						}
						return tag;
					}
				}
			}
		}
		System.out.println(name + " not found");
		return null;
	}
	
	public static Package createPackage(String name, Element owner) {
		createSession();
		Package newPackage = createPackageNoSession(name, owner);
		closeSession();
		return newPackage;
	}
	
	protected static Package createPackageNoSession(String name, Element owner) {
		initializeFactory();
		Package newPackage = ef.createPackageInstance();
		finishElement(newPackage, name, owner);
		return newPackage;
	}

	public static Class createView(String name, Element owner) {
		createSession();
		Class newView = createViewNoSession(name, owner);
		closeSession();
		return newView;
	}

	protected static Class createViewNoSession(String name, Element owner) {
		initializeFactory();
		Class newView = ef.createClassInstance();
		Stereotype sysmlView = Utils.getViewClassStereotype();
		StereotypesHelper.addStereotype(newView, sysmlView);
		finishElement(newView, name, owner);
		return newView;
	}

	public static Class createDocument(String name, Element owner) {
		createSession();
		Class newDocument = createDocumentNoSession(name, owner);
		closeSession();
		return newDocument;
	}

	protected static Class createDocumentNoSession(String name, Element owner) {
		initializeFactory();
		Class newDocument = ef.createClassInstance();
		Stereotype sysmlDocument = Utils.getDocumentStereotype();
		ImportUtility.setOrCreateAsi(sysmlDocument, newDocument);
		finishElement(newDocument, name, owner);
		return newDocument;
	}

	public static Association createDirectedComposition(Element document, Element view) {
		createSession();
		initializeFactory();
		Association assoc = ef.createAssociationInstance();
		finishElement(assoc, null, document.getOwner());
		Property source = createPropertyNoSession(((NamedElement)view).getName(), document, null, view, "composite", "1", "1");
        Property target = createPropertyNoSession("", assoc, null, document, "none", "1", "1");
		assoc.getMemberEnd().clear();
		assoc.getMemberEnd().add(0, source);
		assoc.getMemberEnd().add(target);
		assoc.getOwnedEnd().add(0,target);
		closeSession();
		return assoc;
	}
	
	public static Component createSiteCharComponent(String name, Element owner) {
		Component comp = createComponent(name, owner);
		Element genTarget = ElementFinder.getElementByID("_17_0_5_1_8660276_1415063844134_132446_18688");
		createGeneralization("", comp, comp, genTarget);
		createDependency("", comp, comp, owner);
		return comp;
	}
	
	public static Component createComponent(String name, Element owner) {
		createSession();
		Component comp = createComponentNoSession(name, owner);
		closeSession();
		return comp;
	}
	
	public static Component createComponentNoSession(String name, Element owner) {
		initializeFactory();
		Component comp = ef.createComponentInstance();
		finishElement(comp, name, owner);
		return comp;
	}
	
	public static Generalization createGeneralization(String name, Element owner, Element source, Element target) {
		createSession();
		Generalization genr = createGeneralizationNoSession(name, owner, source, target);
		closeSession();
		return genr;
	}
	
	protected static Generalization createGeneralizationNoSession(String name, Element owner, Element source, Element target) {
		initializeFactory();
		Generalization genr = ef.createGeneralizationInstance();
		setRelationshipEnds(genr, source, target);
		finishElement(genr, null, owner);
		return genr;
	}
	
	public static Dependency createDependency(String name, Element owner, Element source, Element target) {
		createSession();
		Dependency depd = createDependencyNoSession(name, owner, source, target);
		closeSession();
		return depd;
	}
	
	protected static Dependency createDependencyNoSession(String name, Element owner, Element source, Element target) {
		initializeFactory();
		Dependency depd = ef.createDependencyInstance();
		setRelationshipEnds(depd, source, target);
		finishElement(depd, null, owner);
		return depd;
	}
	
	public static void setRelationshipEnds(DirectedRelationship dr, Element source, Element target) {
		ModelHelper.setClientElement(dr, source);
		ModelHelper.setSupplierElement(dr, target);
	}
	
	public static Property createProperty(String name, Element owner, ValueSpecification defaultValue, 
			Element typeElement, String aggregation, String multMin, String multMax) {
		createSession();
		Property prop = createPropertyNoSession(name, owner, defaultValue, typeElement, aggregation, multMin, multMax);
		closeSession();
		return prop;
	}
	
	protected static Property createPropertyNoSession(String name, Element owner, ValueSpecification defaultValue, 
			Element typeElement, String aggregation, String multMin, String multMax) {
		initializeFactory();
		Property prop = ef.createPropertyInstance();
		
		prop.setDefaultValue(defaultValue);
		
		if (typeElement != null)
			prop.setType((Type) typeElement);
		
		if (aggregation != null)
			prop.setAggregation(AggregationKindEnum.getByName(aggregation));
		
		if (multMin != null) {
			try{
				Long spmin = new Long(multMin);
	    	    ValueSpecification pmin = prop.getLowerValue();
	    	    if (pmin == null)
	    	        pmin = ef.createLiteralIntegerInstance();
	    	    if (pmin instanceof LiteralInteger)
	    	        ((LiteralInteger)pmin).setValue(spmin.intValue());
	    	    if (pmin instanceof LiteralUnlimitedNatural)
	    	        ((LiteralUnlimitedNatural)pmin).setValue(spmin.intValue());
	    	    prop.setLowerValue(pmin);
	    	}
	    	catch (NumberFormatException en){}
		}
		
		if (multMax != null) {
			try{
				Long spmax = new Long(multMax);
	    	    ValueSpecification pmin = prop.getLowerValue();
	    	    if (pmin == null)
	    	        pmin = ef.createLiteralIntegerInstance();
	    	    if (pmin instanceof LiteralInteger)
	    	        ((LiteralInteger)pmin).setValue(spmax.intValue());
	    	    if (pmin instanceof LiteralUnlimitedNatural)
	    	        ((LiteralUnlimitedNatural)pmin).setValue(spmax.intValue());
	    	    prop.setLowerValue(pmin);
	    	}
	    	catch (NumberFormatException en){}
		}
		
		finishElement(prop, null, owner);
		return prop;
	}

	/**
	 * Creates a specified number of diagrams in the open project
	 * 
	 * @param num
	 *            number of diagrams to be created.
	 */
	public static void createDiagrams(int num) {
		createSession();
		for (int i = 0; i < num; i++) {
			try {
				ModelElementsManager.getInstance().createDiagram(DiagramTypeConstants.UML_CLASS_DIAGRAM,
						(Namespace) ElementFinder.getModelRoot());
			} catch (ReadOnlyElementException e) {
				e.printStackTrace();
				cancelSession();
			}
		}
		closeSession();
	}

    public static void createDiagrams(int num, Package owner) {
        createSession();
        for (int i = 0; i < num; i++) {
            try {
                ModelElementsManager.getInstance().createDiagram(DiagramTypeConstants.UML_CLASS_DIAGRAM,
                        (Namespace) owner);
            } catch (ReadOnlyElementException e) {
                e.printStackTrace();
                cancelSession();
            }
        }
        closeSession();
    }

	/**
	 * Deletes all elements in a project under the indicated parent.
	 * 
	 * @param parent
	 *            Parent element under which you want to delete elements.
	 */
	public static void deleteEditableContainerChildren(Element parent) {
		Collection<Element> elements = parent.getOwnedElement();
		ArrayList<Element> eList = new ArrayList<Element>();
		createSession();

		for (Element e : elements) {
			if (e.getHumanName().startsWith("Package") && e.isEditable()) {
				eList.add(e);
			}
			if (e.getHumanName().startsWith("Diagram") && e.isEditable()) {
				eList.add(e);
			}
		}

		ModelElementsManager mem = ModelElementsManager.getInstance();
		for (Element elem : eList) {
			try {
				mem.removeElement(elem);
			} catch (Exception e) {
				Application.getInstance().getGUILog()
						.log("Exception occurred in delete all editable elements: " + e.toString());
				cancelSession();
			}
		}
		closeSession();
	}

	/**
	 * Deletes all editable container elements in a project.
	 */
	public static void deleteLocalMDElements() {
		deleteEditableContainerChildren(ElementFinder.getModelRoot());
	}

	/**
	 * Deletes selected element from Magic Draw, including children
	 * 
	 * @param ele
	 *            selected element to be deleted.
	 */
	public static void deleteMDElement(Element ele) throws Exception {
		createSession();
		try {
			ModelElementsManager.getInstance().removeElement(ele);
		} catch (Exception e) {
			cancelSession();
			throw e;
		}
		closeSession();
	}

	/**
	 * Returns the comments of the associated elements, stripped of html wrapper
	 * 
	 * @param target
	 *            The level element whose comment you want to return
	 * 
	 */
	public static String getElementDocumentation(Element target) {
		return Utils.stripHtmlWrapper(ModelHelper.getComment(target));
	}

	/**
	 * Returns the value of the passed property
	 * 
	 * @param target
	 *            The property whose value you wish to inspect
	 */
	public static String getPropertyValue(Element target) {
		String value = null;
		if (target instanceof Property) {
			ValueSpecification vs = ((Property) target).getDefaultValue();
			if (vs instanceof LiteralBoolean) {
				value = Boolean.toString(((LiteralBoolean) vs).isValue());
			} else if (vs instanceof LiteralInteger) {
				value = Long.toString(((LiteralInteger) vs).getValue());
			} else if (vs instanceof LiteralNull) {
				value = null;
			} else if (vs instanceof LiteralReal) {
				value = Double.toString(((LiteralReal) vs).getValue());
			} else if (vs instanceof LiteralString) {
				value = Utils.stripHtmlWrapper(((LiteralString) vs).getValue());
			} else if (vs instanceof LiteralUnlimitedNatural) {
				value = Long.toString(((LiteralUnlimitedNatural) vs).getValue());
			}
		}
		return value;
	}

	/**
	 * Renames the passed NamedElement
	 * 
	 * @param target
	 *            The NamedElement to rename
	 * @param newName
	 *            The new name for the element
	 */
	public static void renameElement(NamedElement target, String newName) {
		createSession();
		ImportUtility.setName(target, newName);
		closeSession();
	}

	/**
	 * Updates the comments of the target element, adding the necessary html
	 * wrapper
	 * 
	 * @param target
	 *            The level element whose comment you want to change
	 * @param documentation
	 *            The new comments for the target element
	 * 
	 */
	public static void setElementDocumentation(Element target, String documentation) {
		createSession();
		ImportUtility.setDocumentation(target, documentation);
		closeSession();
	}

	/**
	 * 3-way merge - merge branch changes to trunk.
	 *
	 * @param projectName
	 *            remote project name.
	 * @param branchName
	 *            branch name.
	 * @return merged project.
	 * @throws java.rmi.RemoteException
	 *             remote exception.
	 */

	public static void mergeToTrunk(String projectName, String branchName) throws Exception {
		final ProjectDescriptor trunkDescriptor = TeamworkUtils.getRemoteProjectDescriptorByQualifiedName(projectName);

		// load target project (trunk head)
		final ProjectsManager projectsManager = Application.getInstance().getProjectsManager();
		projectsManager.loadProject(trunkDescriptor, true);
		final Project targetProject = projectsManager.getActiveProject();
		projectsManager.closeProject();

		// ancestor is 1st version from trunk
		final ProjectDescriptor ancestor = createProjectDescriptor(projectName, trunkDescriptor, 1);

		// source is 2nd version from branch
		String branchedProjectName = TeamworkUtils.generateProjectQualifiedName(projectName,
				new String[] { branchName });
		final ProjectDescriptor branchDescriptor = TeamworkUtils
				.getRemoteProjectDescriptorByQualifiedName(branchedProjectName);
		final ProjectDescriptor source = createProjectDescriptor(branchedProjectName, branchDescriptor, 2);

		// merge project (prefer low memory usage to performance)
		MergeUtil.merge(targetProject, source, ancestor, MergeUtil.ConflictResolution.TARGET_PREFERRED,
				new SimpleErrorHandler(), MergeUtil.Optimization.MEMORY);
	}

	public static ProjectDescriptor createProjectDescriptor(String projectName, ProjectDescriptor projectDescriptor,
			int version) {
		final String remoteID = ProjectDescriptorsFactory.getRemoteID(projectDescriptor.getURI());
		return ProjectDescriptorsFactory.createRemoteProjectDescriptor(remoteID, projectName, version);
	}

	/**
	 * Commits to teamwork and releases locks in a robust manner.
	 * 
	 * Note - this is a time consuming operation when elements are created outside of the __MMSSync__ folder
	 * 
	 * @param user
	 * @param commitMessage
	 * @return
	 */
	public static boolean teamworkCommitReleaseLocks(String user, String commitMessage) {
		Project prj = Application.getInstance().getProject();
		Collection<Element> lockedElements = TeamworkUtils.getLockedElement(prj, user);
		Element syncpkg = ElementFinder.getElement("Package", "__MMSSync__");
		if (syncpkg != null)
			lockedElements.addAll(ElementFinder.findElements(syncpkg));
		boolean success = TeamworkUtils.commitProject(prj, commitMessage, null, lockedElements, null);
		lockedElements = TeamworkUtils.getLockedElement(prj, user);
		if (syncpkg == null || !lockedElements.isEmpty())
			TeamworkUtils.unlockElement(Application.getInstance().getProject(), ElementFinder.getModelRoot(), true, true, true);
		return success;
	}

    public static void twcCommitReleaseLocks(String user, String commitMessage) {
        Project prj = Application.getInstance().getProject();

        ILockProjectService twcLocks =  EsiUtils.getLockService(prj);
        Collection<Element> lockedElements = twcLocks.getLockedByMe();


        Element syncpkg = ElementFinder.getElement("Package", "__MMSSync__");
        if (syncpkg != null) {
            lockedElements.addAll(ElementFinder.findElements(syncpkg));
        }
        EsiUtils.commitProject(prj, commitMessage, lockedElements, null, false, null);
        //lockedElements = TeamworkUtils.getLockedElement(prj, user);

        if (syncpkg == null || !lockedElements.isEmpty()) {
            Collection<Element> entireModel = new ArrayList<Element>(1);
            entireModel.add(ElementFinder.getModelRoot());
            twcLocks.unlockElements(entireModel, true, null);
        }
    }

    private static class SimpleErrorHandler implements ErrorHandler<Exception> {
        @Override
        public void error(Exception ex) throws Exception {
            // just print stack trace
            ex.printStackTrace();
        }
    }

    public static Class createBlock(String name, Element owner) {
        createSession();
        Class block = createBlockNoSession(name, owner);
        closeSession();
        return block;
    }

    public static Class createBlock(String name) {
        Project prj = Application.getInstance().getProject();
        return createBlock(name, prj.getPrimaryModel());
    }

    protected static Class createBlockNoSession(String name, Element owner) {
        initializeFactory();
        //Package newPackage = ef.createPackageInstance();

        Class block = ef.createClassInstance();
        //Stereotype blockstp = (Stereotype)Utils.getElementByQualifiedName("Data [SysMLSysML::Blocks::Block" );
        //Stereotype blockstp = StereotypesHelper.getStereotype(Application.getInstance().getProject(), "Block");
        Finder f1 = new Finder();
        Stereotype blockstp = (Stereotype) Finder.byQualifiedName().find(Application.getInstance().getProject(), "SysML::Blocks::Block");

        //System.out.println("Sterotype human name is - " + blockstp.getHumanName());
        if (blockstp == null) {
            System.out.println("ERROR - STEROTYPE IS NULL");
        }
        StereotypesHelper.addStereotype(block, blockstp);
        block.setName(name);
        block.setOwner(owner);

        //finishElement(newPackage, name, owner);
        return block;
    }

}