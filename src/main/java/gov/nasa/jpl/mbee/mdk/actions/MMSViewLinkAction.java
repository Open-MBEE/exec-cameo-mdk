package gov.nasa.jpl.mbee.mdk.actions;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import gov.nasa.jpl.mbee.mdk.actions.ui.MMSViewLinkForm;
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
        for (Element element: targetElements) {
            Project project = Project.getProject(element);

            // build url
            // can't use URIBuilder, it converts the ‘#’ in the url and breaks things
            URIBuilder uriBase = MMSUtils.getServiceUri(project);
            if (uriBase == null) {
                return;
            }
            String uriBasePath = uriBase.setPath("").clearParameters().toString()
                    + "/alfresco/mmsapp/mms.html#/workspaces/" + MDUtils.getWorkspace(project)
                    + "/sites/" + MMSUtils.getSiteName(project);
            Set<Element> documents = new HashSet<>();
            if (StereotypesHelper.hasStereotype(element, Utils.getDocumentStereotype())) {
                documents.add(element);
            }

            List<Relationship> relationships = new ArrayList<>();
            Set<Element> viewChain = new HashSet<>();
            viewChain.add(element);
            relationships.addAll(element.get_relationshipOfRelatedElement());
            for (int i = 0; i < relationships.size(); i++) {
                if (!(relationships.get(i) instanceof Association)) {
                    continue;
                }
                Element hierarchyParent = ((Association) relationships.get(i)).getMemberEnd().get(0).getOwner();
                if (StereotypesHelper.hasStereotype(hierarchyParent, Utils.getDocumentStereotype())) {
                    documents.add(hierarchyParent);
                }
                if (!viewChain.contains(hierarchyParent)) {
                    viewChain.add(hierarchyParent);
                    relationships.addAll(hierarchyParent.get_relationshipOfRelatedElement());
                }
            }

            // build links
            URI link = null;

            if(documents.size() > 1){
                // build multiple links
                String label = "";
                List<JButton> linkButtons = new ArrayList<>();
                try {
                    if (!documents.isEmpty()) {
                        label = "Documents containing " + element.getHumanName() + ":";
                        for (Element doc : documents) {
                            if (doc.equals(element)) {
                                link = new URI(uriBasePath + "/documents/" + element.getID());
                            } else {
                                link = new URI(uriBasePath + "/documents/" + doc.getID() + "/views/" + element.getID());
                            }
                            JButton button = new ViewButton(doc.getHumanName(), link);
                            linkButtons.add(button);
                        }
                    }
                }
                catch (URISyntaxException se) {
                    Application.getInstance().getGUILog().log("[ERROR] Exception occurred while generating VE links for " + element.getHumanName() + ". Unable to proceed.");
                    return;
                }
                // and display
                MMSViewLinkForm viewLinkForm = new MMSViewLinkForm(label, linkButtons);
                viewLinkForm.setVisible(true);
            }else {
                // build single link
                try {
                    link = new URI(uriBasePath + "/documents/" + element.getID() + "/views/" + element.getID());
                }
                catch (URISyntaxException se) {
                    Application.getInstance().getGUILog().log("[ERROR] Exception occurred while generating VE links for " + element.getHumanName() + ". Unable to proceed.");
                    return;
                }
                // just open it if possible
                if (Desktop.isDesktopSupported()) {
                    try {
                        Application.getInstance().getGUILog().log("[INFO] " + element.getHumanName()
                                + "does not belong to a document hierarchy. Opening view in VE without document context.");
                        Desktop.getDesktop().browse(link);
                    }
                    catch (IOException e1) {
                        Application.getInstance().getGUILog().log("[ERROR] Exception occurred while opening the VE page. Link: " + link.toString());
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
                    Application.getInstance().getGUILog().log("[ERROR] Exception occurred while opening the VE page. Link: " + uri.toString());
                }
            } else {
                Application.getInstance().getGUILog().log("[WARNING] Java is unable to open links on your computer. Link: " + uri.toString());
            }
        }
    }


}

