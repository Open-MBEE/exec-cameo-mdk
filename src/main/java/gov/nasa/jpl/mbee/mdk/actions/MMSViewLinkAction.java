package gov.nasa.jpl.mbee.mdk.actions;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import gov.nasa.jpl.mbee.mdk.actions.ui.MMSViewLinkForm;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.ems.MMSUtils;
import gov.nasa.jpl.mbee.mdk.lib.MDUtils;
import gov.nasa.jpl.mbee.mdk.lib.Utils;
import org.apache.http.client.utils.URIBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;

public class MMSViewLinkAction extends MDAction {

    private static final long serialVersionUID = 1L;
    private Collection<Element> targetElements;
    public static final String DEFAULT_ID = "ViewLink";

    public MMSViewLinkAction(Collection<Element> elements) {
        super(DEFAULT_ID, "Open in View Editor", null, null);
        targetElements = elements;
    }

    public MMSViewLinkAction(Element element) {
        super(DEFAULT_ID, "Open in View Editor", null, null);
        targetElements = new ArrayList<>();
        targetElements.add(element);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Stereotype documentStereotype = Utils.getDocumentStereotype();
        Stereotype viewStereotype = Utils.getViewStereotype();

        for (Element element: targetElements) {
            if (!StereotypesHelper.hasStereotypeOrDerived(element, viewStereotype)
                    && !StereotypesHelper.hasStereotypeOrDerived(element, documentStereotype)) {
                continue;
            }
            Project project = Project.getProject(element);

            // build url
            // can't use URIBuilder, it converts the ‘#’ in the url and breaks things
            URIBuilder uriBase = MMSUtils.getServiceUri(project);
            if (uriBase == null) {
                Application.getInstance().getGUILog().log("[ERROR] Unable to retrieve MMS information from model stereotype. Cancelling view open.");
                return;
            }
            String uriBasePath = uriBase.setPath("").clearParameters().toString()
                    + "/alfresco/mmsapp/mms.html#/workspaces/" + MDUtils.getWorkspace(project)
                    + "/sites/" + MMSUtils.getSiteName(project);
            Set<Element> documents = new HashSet<>();

            // collect document parents from hierarchy
            ArrayList<Element> viewChain = new ArrayList<>();
            viewChain.add(element);
            for (int i = 0; i <  viewChain.size(); i++) {
                if (StereotypesHelper.hasStereotype(viewChain.get(i), documentStereotype)) {
                    documents.add(viewChain.get(i));
                }
                // create set of hierarchy children so we can ignore those ends and only climb the hierarchy
                Set<Element> childViews = new HashSet<>();
                for (Property prop : ((Class) viewChain.get(i)).getOwnedAttribute()) {
                    if (!(prop.getType() instanceof Class)) {
                        continue;
                    }
                    Class type = (Class) prop.getType();
                    if (type == null || !(StereotypesHelper.hasStereotypeOrDerived(type, viewStereotype)
                            || StereotypesHelper.hasStereotypeOrDerived(type, documentStereotype))) {
                        continue;
                    }
                    childViews.add(type);
                }
                // check each association end, if it's a non-child view/document then add it to chain for further processing
                for (Relationship relation : viewChain.get(i).get_relationshipOfRelatedElement()) {
                    if (!(relation instanceof Association)) {
                        continue;
                    }
                    Element assocEnd = ((Association) relation).getMemberEnd().get(0).getOwner();
                    if (!StereotypesHelper.hasStereotypeOrDerived(assocEnd, viewStereotype)
                            && !StereotypesHelper.hasStereotypeOrDerived(assocEnd, documentStereotype)) {
                        continue;
                    }
                    if (!childViews.contains(assocEnd) && !viewChain.contains(assocEnd)) {
                        viewChain.add(assocEnd);
                    }
                }

            }

            // build links
            URI link;
            if(documents.size() > 1){
                // build multiple links
                String label = "";
                List<JButton> linkButtons = new ArrayList<>();
                try {
                    if (!documents.isEmpty()) {
                        label = "Documents containing " + element.getHumanName() + ":";
                        for (Element doc : documents) {
                            if (doc.equals(element)) {
                                link = new URI(uriBasePath + "/documents/" + Converters.getElementToIdConverter().apply(element));
                            } else {
                                link = new URI(uriBasePath + "/documents/" + Converters.getElementToIdConverter().apply(doc) + "/views/" + Converters.getElementToIdConverter().apply(element));
                            }
                            JButton button = new ViewButton(doc.getHumanName(), link);
                            linkButtons.add(button);
                        }
                    }
                }
                catch (URISyntaxException se) {
                    Application.getInstance().getGUILog().log("[ERROR] Exception occurred while generating View Editor links for " + element.getHumanName() + ". Unable to proceed.");
                    return;
                }
                // and display
                MMSViewLinkForm viewLinkForm = new MMSViewLinkForm(label, linkButtons);
                viewLinkForm.setVisible(true);
            }else {
                // build single link
                try {
                    link = new URI(uriBasePath + "/documents/" + Converters.getElementToIdConverter().apply(element) + "/views/" + Converters.getElementToIdConverter().apply(element));
                }
                catch (URISyntaxException se) {
                    Application.getInstance().getGUILog().log("[ERROR] Exception occurred while generating View Editor links for " + element.getHumanName() + ". Unable to proceed.");
                    return;
                }
                // just open it if possible
                if (Desktop.isDesktopSupported()) {
                    try {
                        if (documents.size() == 0) {
                            Application.getInstance().getGUILog().log("[INFO] " + element.getHumanName()
                                    + " does not belong to a document hierarchy. Opening view in View Editor without document context.");
                        }
                        Desktop.getDesktop().browse(link);
                    }
                    catch (IOException e1) {
                        Application.getInstance().getGUILog().log("[ERROR] Exception occurred while opening the View Editor page. Link: " + link.toString());
                        e1.printStackTrace();
                    }
                } else {
                    Application.getInstance().getGUILog().log("[WARNING] Java is unable to open links on your computer. Link: " + link.toString());
                }
            }
        }
    }

    private class ViewButton extends JButton {
        private URI uri;

        ViewButton(String text, URI uri){
            super(text);
            setup(uri);
            this.setMaximumSize(new Dimension(280, 18));
        }

        public void setup(URI u){
            uri = u;
            setToolTipText(uri.toString());
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    open(uri);
                }
            });
        }

        private void open(URI uri) {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                try {
                    desktop.browse(uri);
                } catch (IOException e) {
                    Application.getInstance().getGUILog().log("[ERROR] Exception occurred while opening the View Editor page. Link: " + uri.toString());
                }
            } else {
                Application.getInstance().getGUILog().log("[WARNING] Java is unable to open links on your computer. Link: " + uri.toString());
            }
        }
    }


}

