package gov.nasa.jpl.mbee.mdk.actions;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.ui.dialogs.MDDialogParentProvider;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import gov.nasa.jpl.mbee.mdk.actions.ui.MMSViewLinkForm;
import gov.nasa.jpl.mbee.mdk.ems.MMSUtils;
import gov.nasa.jpl.mbee.mdk.lib.MDUtils;
import gov.nasa.jpl.mbee.mdk.lib.Utils;

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
            String viewName = "(empty name)";
            if(element instanceof NamedElement && ((NamedElement) element).getName()!=null
                    && !((NamedElement) element).getName().isEmpty()) {
                viewName = ((NamedElement)element).getName();
            }

            Project project = Project.getProject(element);
            // build url
            boolean showPane = false;
            int numDocuments = 0;
            URI link = null;

            // can't use URIBuilder, it converts the ‘#’ in the url and breaks things
            String uriBasePath = MMSUtils.getServiceUri(project).setPath("").clearParameters().toString()
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

            String label;
            List<JButton> linkButtons = new ArrayList<>();
            try {
                if (!documents.isEmpty()) {
                    label = "Documents containing " + viewName + ":";
                    for (Element doc : documents) {
                        if (doc.equals(element)) {
                            link = new URI(uriBasePath + "/documents/" + element.getID());
                        } else {
                            link = new URI(uriBasePath + "/documents/" + doc.getID() + "/views/" + element.getID());
                        }
                        String documentName = "(empty name)";
                        if(doc instanceof NamedElement && ((NamedElement) doc).getName()!=null &! ((NamedElement) doc).getName().isEmpty()) {
                            documentName = ((NamedElement)doc).getName();
                        }
                        JButton button = new ViewButton(documentName, link);
                        linkButtons.add(button);
                        numDocuments++;
                    }
                }
                else {
                    label = "No documents contain " + viewName + ". <br>Direct view link:";
                    link = new URI(uriBasePath + "/documents/" + element.getID() + "/views/" + element.getID());
                    JButton button = new ViewButton(viewName, link);
                    linkButtons.add(button);
                }
            }
            catch (URISyntaxException se) {
                label = "[ERROR] Unable to generate view links for this element.";
            }

            MMSViewLinkForm viewLinkForm = new MMSViewLinkForm(label, linkButtons);
            if(numDocuments > 1 || numDocuments == 0){
                showPane = true;
            }else {
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(link);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                } else {
                    showPane = true;
                }
            }
            if(showPane) {
                viewLinkForm.setVisible(true);
            }
        }
    }

    public class ViewButton extends JButton {
//        private String text;
        private URI uri;

        public ViewButton(String text, URI uri){
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
                    JOptionPane.showMessageDialog(null,
                            "Failed to launch the link, possibly due to a configuration issue.\nLink: " + uri.toString(),
                            "Cannot Launch Link", JOptionPane.WARNING_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null,
                        "Java is not able to launch links on your computer.\nLink: " + uri.toString(),
                        "Cannot Launch Link", JOptionPane.WARNING_MESSAGE);
            }
        }
    }


}

