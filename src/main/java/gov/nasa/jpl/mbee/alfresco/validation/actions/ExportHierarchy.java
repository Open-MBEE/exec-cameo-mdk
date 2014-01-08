package gov.nasa.jpl.mbee.alfresco.validation.actions;

import gov.nasa.jpl.mbee.DocGen3Profile;
import gov.nasa.jpl.mbee.alfresco.ExportUtility;
import gov.nasa.jpl.mbee.generator.DocumentGenerator;
import gov.nasa.jpl.mbee.generator.PostProcessor;
import gov.nasa.jpl.mbee.model.DocBookOutputVisitor;
import gov.nasa.jpl.mbee.model.Document;
import gov.nasa.jpl.mbee.viewedit.DBAlfrescoVisitor;
import gov.nasa.jpl.mbee.viewedit.ViewEditUtils;
import gov.nasa.jpl.mbee.viewedit.ViewHierarchyVisitor;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBBook;
import gov.nasa.jpl.mgss.mbee.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mgss.mbee.docgen.validation.RuleViolationAction;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

public class ExportHierarchy extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {
    private static final long serialVersionUID = 1L;
    private Element view;
    private GUILog gl = Application.getInstance().getGUILog();
    
    public ExportHierarchy(Element e) {
        super("ExportHierarchy", "Export Hierarchy", null, null);
        this.view = e;
    }
    
    @Override
    public boolean canExecute(Collection<Annotation> arg0) {
        return true;
    }

    @Override
    public void execute(Collection<Annotation> annos) {
        Collection<Annotation> toremove = new ArrayList<Annotation>();
        for (Annotation anno: annos) {
            Element e = (Element)anno.getTarget();
            if (exportHierarchy(e)) {
                toremove.add(anno);
            }
        }
        if (!toremove.isEmpty()) {
            this.removeViolationsAndUpdateWindow(toremove);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (exportHierarchy(view)) {
            this.removeViolationAndUpdateWindow();
        }
    }
    
    private boolean exportHierarchy(Element view) {
        DocumentGenerator dg = new DocumentGenerator(view, null, null);
        Document dge = dg.parseDocument(true, true);
        ViewHierarchyVisitor vhv = new ViewHierarchyVisitor();
        dge.accept(vhv);
        String url = ViewEditUtils.getUrl(false);
        boolean document = false;
        Stereotype documentView = StereotypesHelper.getStereotype(Application.getInstance().getProject(),
                DocGen3Profile.documentViewStereotype, "Document Profile");
        if (StereotypesHelper.hasStereotypeOrDerived(view, documentView))
            document = true;
        
        JSONObject view2view = vhv.getView2View();
        if (document) {
            String docurl = url + "/javawebscripts/documents";
            
            JSONObject send = new JSONObject();
            JSONArray documents = new JSONArray();
            JSONObject doc = new JSONObject();
            doc.put("view2view", view2view);
            doc.put("noSections", vhv.getNosections());
            doc.put("id", view.getID());
            documents.add(doc);
            send.put("documents", documents);
            if (!ExportUtility.send(docurl, send.toJSONString()))
                return false;
        } else {
            JSONArray views = new JSONArray();
            for (Object viewid: view2view.keySet()) {
                JSONObject viewinfo = new JSONObject();
                viewinfo.put("id", viewid);
                viewinfo.put("childrenViews", view2view.get(viewid));
                views.add(viewinfo);
            }
            JSONObject send  = new JSONObject();
            send.put("views", views);
            if (!ExportUtility.send(url + "/javawebscripts/newviews", send.toJSONString()))
                return false;
        }
        return true;
        
    }
    
}
