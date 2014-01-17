package gov.nasa.jpl.mbee.ems.validation.actions;

import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mgss.mbee.docgen.validation.RuleViolationAction;

import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.impl.ElementsFactory;

public class ImportElementComments extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {

    private static final long serialVersionUID = 1L;
    private Element element;
    private JSONObject result;
    
    public ImportElementComments(Element e, JSONObject result) {
        super("ImportElementComments", "Import element comments", null, null);
        this.element = e;
        this.result = result;
    }
    
    @Override
    public boolean canExecute(Collection<Annotation> arg0) {
        return false;
    }

    @Override
    public void execute(Collection<Annotation> annos) {
        
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        SessionManager.getInstance().createSession("import comments");
        try {
            Set<String> modelComments = new HashSet<String>();
            JSONArray comments = (JSONArray)result.get("elements");
            Set<String> webComments = new HashSet<String>();
            Map<String, JSONObject> webCommentsMap = new HashMap<String, JSONObject>();
            for (Object elinfo: comments) {
                String id = (String)((JSONObject)elinfo).get("id");
                webComments.add(id);
                webCommentsMap.put(id, (JSONObject)elinfo);
            }
            for (Comment el: element.get_commentOfAnnotatedElement()) {
                if (!ExportUtility.isElementDocumentation(el))
                    modelComments.add(el.getID());
            }
            ElementsFactory ef = Application.getInstance().getProject().getElementsFactory();
            for (String webid: webComments) {
                if (!modelComments.contains(webid)) {
                    Comment newcomment = ef.createCommentInstance();
                    JSONObject commentObject = webCommentsMap.get(webid);
                    newcomment.setBody(Utils.addHtmlWrapper((String)commentObject.get("body")));
                    newcomment.setID(webid);
                    newcomment.setOwner(element.getOwner());
                    newcomment.getAnnotatedElement().add(element);
                }
            }
            SessionManager.getInstance().closeSession();
            //AnnotationManager.getInstance().remove(annotation);
            //AnnotationManager.getInstance().update();
            this.removeViolationAndUpdateWindow();
        } catch (Exception ex) {
            SessionManager.getInstance().cancelSession();
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            Application.getInstance().getGUILog().log(sw.toString()); // stack trace as a string
            ex.printStackTrace();
        }
    }

}
