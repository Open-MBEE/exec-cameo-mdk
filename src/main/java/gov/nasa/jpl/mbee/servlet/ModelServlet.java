package gov.nasa.jpl.mbee.servlet;

import gov.nasa.jpl.ocl.CallOperation;
import gov.nasa.jpl.ocl.DgEnvironmentFactory;
import gov.nasa.jpl.ocl.DgOperationInstance;
import gov.nasa.jpl.ocl.OclEvaluator;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EParameter;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.ocl.OCL;
import org.eclipse.ocl.ecore.Constraint;
import org.eclipse.ocl.ecore.EcoreEnvironmentFactory;
import org.eclipse.ocl.helper.OCLHelper;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Relationship;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Slot;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

/**
 * Simple servlet to expose containment tree or specification information).
 * 
 * This servlet is loaded from {@link gov.nasa.jpl.mgss.mbee.docgen.DocGenEmbeddedTomcatServer#setup}
 * @author cinyoung
 *
 */
@SuppressWarnings("serial")
public class ModelServlet extends HttpServlet {
	/**
	 * Global buffer that can be manipulated to return servlet response
	 */
	StringBuffer	sb;
	/**
	 * The root EObject specified in the Servlet request
	 */
	EObject			rootEObject;
	Project			project;

	/**
	 * Handles the servlet request 
	 */
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
		sb = new StringBuffer();
		project = Application.getInstance().getProject();

		if (project == null) {
			resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
		} else {
			// lets parse the request and serve as appropriate
			serveRequest(req);
			
			Writer w = resp.getWriter();
			w.write(sb.toString());
			w.flush();
		}
	}
	
	/**
	 * Utility function that builds up the servlet response based on the request
	 * 
	 * Request looks something like:
	 * 
	 * http://127.0.0.1:8080/{MD_ID}{?specification}
	 *   {MD_ID} is the MD ID of the element to query, defaults to root model if not specified
	 *   {?specification} is query, if not specified, containment is rendered, 
	 *                    otherwise dumps out specification information 
	 * 
	 * @param req
	 */
	private void serveRequest(HttpServletRequest req) {
		boolean isSpecification = false;
		
		// grab the MD ID and the queries from request
		String mdId = req.getRequestURI().replaceFirst("/", "");
		String queries[] = {};
		if (req.getQueryString() != null) {
			queries = req.getQueryString().split("&");
		}
		for (String query: queries) {
			if (query.startsWith("specification")) {
				isSpecification = true;
			}
		}

		// if no mdId specified, root is project model
		if (mdId.length() == 0) {
			rootEObject = project.getModel();
		} else {
			rootEObject = (EObject)project.getElementByID(mdId);
		}

		if (isSpecification) {
			renderSpecification();
		} else {
			renderContainment();
		}
		
		// wrap response with html tags
		sb.insert(0, "<html><body>\n");
		sb.append("</body></html>\n");

		// lets play with OCL
		// TODO: need to move this out into its own test class
		boolean verbose = true;

		// set up the customized environment
  		// create custom environment factory
  		DgEnvironmentFactory	envFactory 	= new DgEnvironmentFactory();
  		
  		// create custom operation
		DgOperationInstance		doi 		= new DgOperationInstance();
		doi.setName("regexMatch");
		doi.setAnnotationName("DocGenEnvironment");
		EParameter parm = EcoreFactory.eINSTANCE.createEParameter();
		parm.setName("pattern");
		doi.addParameter(parm);
		
		// essentially set the actual operation as function pointer
		doi.setOperation(new CallOperation() {
			@Override
			public Object callOperation(Object source, Object[] args) {
				Pattern pattern = Pattern.compile((String) args[0]);
				Matcher matcher = pattern.matcher((String) source);
	
				return matcher.matches() ? matcher.group() : null;
			}
		});
		
		// add custom operation to environment and evaluation environment
		envFactory.getDgEnvironment().addDgOperation(doi);
		envFactory.getDgEvaluationEnvironment().addDgOperation(doi);
				
		// create the ocl evaluator
		OclEvaluator.createOclInstance(envFactory);

		// create query and evaluate
		String oclquery = "name.regexMatch('DocGen Templating') <> null";
//		oclquery = "name <> 'DocGen Templating'"; //"ownedType->asSet()";
//		oclquery = "self.appliedStereotypeInstance.slot";
//		oclquery = "self.appliedStereotypeInstance.classifier.attribute";
//		oclquery = "name.regexMatch('DocGen Templating') <> null";

		Object result = OclEvaluator.evaluateQuery(rootEObject, oclquery, verbose);
		if (result != null) {
			System.out.println(result.getClass() + ": " + result.toString());
			if (result instanceof Set) {
				for (Stereotype key: (Set<Stereotype>)result) {
					System.out.println("\t" + key.getHumanName());
				}
			} else if (result instanceof List) {
				for (Property prop: (List<Property>) result) {
					System.out.println("\t" + prop.getHumanName());
				}
			}
		}
	}
	
	
	/**
	 * Render containment hierarchy one level deep 
	 */
	private void renderContainment() {
		/**
		 * Map of child name to its MD ID (for hyperlinks are available)
		 * 
		 * Use TreeMap so they are sorted when displayed
		 */
		Map<String, String> children = new TreeMap<String, String>();
		/**
		 * Set of relations that describes the source and target
		 */
		Set<String> relations = new TreeSet<String>();
		
		// just look one level deep
		for (EObject eobject: rootEObject.eContents()) {
			EClass eclass = eobject.eClass();
			String id = (String) eobject.eGet(getEAttribute(eclass, "ID"));
			if (getEAttribute(eclass, "name") != null) {
				String name = (String) eobject.eGet(getEAttribute(eclass, "name"));
				if (name.length() > 0) {
					children.put(name, id);
				} else {
					// TODO: Need to genericize for all relationships to show source and target
					if (eobject instanceof Relationship) {
						relations.add(eclass.getName() + "[" + rootEObject.eGet(getEAttribute(rootEObject.eClass(), "qualifiedName")) + "]");
					}
				}
			}
		}
		
		// create the HTML 
		sb.append("<ul>\n");
		if (relations.size() > 0) {
			sb.append("<li>Relations<ul>\n");
			for (String key: relations) {
				sb.append("<li>" + key + "</li>\n");
			}
			sb.append("</ul></li>\n");
		}

		for (String key: children.keySet()) {
			sb.append("<li><a href=\"" + children.get(key) + "\">" + key + "</a></li>\n");
		}
		sb.append("</ul>\n");
	}

	
	/**
	 * Helper method to render structural features for an EObject
	 * @param eobject
	 * @return
	 */
	private StringBuffer renderEObjectStructuralFeatures(EObject eobject) {
		EClass eclass = eobject.eClass();
		StringBuffer result = new StringBuffer();
		if (getEAttribute(eclass, "name") != null) {
			String name = (String) eobject.eGet(getEAttribute(eclass, "name"));
		}
		
		result.append("<ul>\n");
		for (EStructuralFeature loopvar: eclass.getEAllStructuralFeatures()) {
			result.append("<li>" + loopvar.getName() + "</li>\n");
		}
		result.append("</ul>\n");
		
		return result;
	}

	/**
	 * Render the specification
	 */
	private void renderSpecification() {
		EClass rootEclass = rootEObject.eClass();
		sb.append("<h2>" + rootEObject.eGet(getEAttribute(rootEclass, "name")) + "</h2>\n");
		
		sb.append("<h3>MetaClass</h3>\n");
		sb.append("<ul><li>" + rootEclass.getName() + "</ul></li>\n");
		
		sb.append("<h3>Attributes</h3>\n");
		sb.append("<ul>\n");
		for (EAttribute loopvar: rootEclass.getEAllAttributes()) {
			sb.append("<li>" + loopvar.getName() + "</li>\n");
			sb.append("<ul><li>" + rootEObject.eGet(loopvar) + "</li></ul>\n");
		}
		sb.append("</ul>\n");

		// Super Types and Generic Super Types are the same for us
//		sb.append("<h3>Super Types</h3>\n");
//		sb.append("<ul>\n");
//		for (EClass loopvar: rootEclass.getEAllSuperTypes()) {
//			sb.append("<li>" + loopvar.getName() + "</li>\n");
//		}
//		sb.append("</ul>\n");

		// just gets MD Extension
//		sb.append("<h3>Operations</h3>\n");
//		sb.append("<ul>\n");
//		for (EOperation loopvar: rootEclass.getEAllOperations()) {
//			sb.append("<li>" + loopvar.getName() + "</li>\n");
//		}
//		sb.append("</ul>\n");
	
//		sb.append("<h3>Cross References</h3>\n");
//		sb.append("<ul>\n");
//		for (EObject loopvar: rootEclass.eCrossReferences()) {
//			sb.append("<li>" + loopvar + "</li>\n");
//		}
//		sb.append("</ul>\n");

		
		Set<String> references = new HashSet<String>();

		sb.append("<h3>References</h3>\n");
		sb.append("<ul>\n");
		for (EReference loopvar: rootEclass.getEAllReferences()) {
			// add the Reference to the list
			sb.append("<li>" + loopvar.getName() + "</li>\n");
			if (loopvar.getName().equals("appliedStereotypeInstance")) {
				EList<Slot> slots = null;
				
				// lets get the instance specification (TODO: do this recursively in case there are recursive Stereotypes)
				EObject asiEobject = (EObject) rootEObject.eGet(loopvar);
				if (asiEobject != null) {
					EClass  asiEclass = asiEobject.eClass();
					System.out.println("References for Applied Stereotype Instance");
					for (EReference eref: asiEclass.getEAllReferences()) {
						System.out.println("\t" + eref.getName());
						if (eref.getName() == "slot") {
							// TODO: Slots are empty until instanced - need to be abel to get slots from Stereotype metadata
							slots = (EList<Slot>) asiEobject.eGet(eref);
							break;
						} else if (eref.getName() == "_instanceValueOfInstance") {
							Object eobject = asiEobject.eGet(eref);
							System.out.println(eobject);
							break;
						}
					}
				}
			}
			references.add(loopvar.getName()); // keeps a set of added references so can see what strctural features doesn't include (should just be attributes)
			
			// print out the actual value
			if (rootEObject.eGet(loopvar) instanceof NamedElement) {
				sb.append("<ul><li>" + rootEObject.eGet(loopvar) + ": " + ((NamedElement)rootEObject.eGet(loopvar)).getName() + "</li>\n");
				sb.append("</ul>\n");
			} else {
				sb.append("<ul><li>" + rootEObject.eGet(loopvar) + "</li></ul>\n");
			}
		}
		sb.append("</ul>\n");

	}
	
	/**
	 * Utility function get the EAttribute of the specified name from an EClass
	 * @param eclass
	 * @param attrname
	 * @return
	 */
	private EAttribute getEAttribute(EClass eclass, String attrname) {
		for (EAttribute attr: eclass.getEAllAttributes()) {
			if (attr.getName() == attrname) {
				return attr;
			}
		}
		return null;
	}
}
