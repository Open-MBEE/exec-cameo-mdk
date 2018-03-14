package gov.nasa.jpl.mbee.mdk.actions;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.mms.MMSUtils;
import gov.nasa.jpl.mbee.mdk.ui.ViewEditorLinkForm;
import gov.nasa.jpl.mbee.mdk.util.MDUtils;
import gov.nasa.jpl.mbee.mdk.util.Utils;
import org.apache.http.client.utils.URIBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;

public class MMSViewLinkAction extends MDAction {

    private static final long serialVersionUID = 1L;
    private Collection<Element> targetElements;
    private Project project;
    public static final String DEFAULT_ID = "ViewLink";

    public MMSViewLinkAction(Collection<Element> elements) {
        super(DEFAULT_ID, "Open in View Editor", null, null);
        this.targetElements = elements;
        this.project = Project.getProject(elements.iterator().next());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Stereotype documentStereotype = Utils.getDocumentStereotype(project);
        Stereotype viewStereotype = Utils.getViewStereotype(project);

        for (Element element : targetElements) {
            if (!StereotypesHelper.hasStereotypeOrDerived(element, viewStereotype)
                    && !StereotypesHelper.hasStereotypeOrDerived(element, documentStereotype)) {
                continue;
            }

            // build url
            URIBuilder uriBase = MMSUtils.getServiceUri(project);
            if (uriBase == null) {
                Application.getInstance().getGUILog().log("[ERROR] Unable to retrieve MMS information from model stereotype. Cancelling view open.");
                return;
            }
            //projects/PROJECT-ID_5_17_16_1_31_54_PM_5fc737b6_154bba92ecd_4cc1_cae_tw_jpl_nasa_gov_127_0_0_1/master/documents/_18_5_83a025f_1491339810716_846504_4332/views/_18_5_83a025f_1491339810716_846504_4332

            // include this in the host portion of the uri. not technically correct, but it prevents the # from being converted and breaking things
            uriBase.setHost(uriBase.getHost() + "/alfresco/mmsapp/mms.html#");
            uriBase.setPath("");

            String uriPath;
            try {
                uriPath = "/projects/" + Converters.getIProjectToIdConverter().apply(project.getPrimaryProject()) + "/" + MDUtils.getBranchId(project);
            } catch (RuntimeException re) {
                re.printStackTrace();
                Application.getInstance().getGUILog().log("[ERROR] Unable to get TWC branch. Cancelling view open. Reason: " + re.getMessage());
                return;
            }
            // collect document parents from hierarchy
            Set<Element> documents = new HashSet<>();
            ArrayList<Element> viewChain = new ArrayList<>();
            viewChain.add(element);
            for (int i = 0; i < viewChain.size(); i++) {
                Element currentView = viewChain.get(i);
                if (StereotypesHelper.hasStereotype(currentView, documentStereotype)) {
                    documents.add(currentView);
                }
                // create set of hierarchy children so we can ignore those ends and only climb the hierarchy
                Set<Element> childViews = new HashSet<>();
                for (Property prop : ((Class) currentView).getOwnedAttribute()) {
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
                for (Relationship relation : currentView.get_relationshipOfRelatedElement()) {
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
            if (documents.size() > 1) {
                // build multiple links
                String label = "";
                List<JButton> linkButtons = new ArrayList<>();
                try {
                    if (!documents.isEmpty()) {
                        label = "Documents containing " + element.getHumanName() + ":";
                        for (Element doc : documents) {
                            if (doc.equals(element)) {
                                uriPath += "/documents/" + Converters.getElementToIdConverter().apply(element);
                            }
                            else {
                                uriPath += "/documents/" + Converters.getElementToIdConverter().apply(doc) + "/views/" + Converters.getElementToIdConverter().apply(element);
                            }
                            JButton button = new ViewButton(doc.getHumanName(), uriBase.setPath(uriPath).build());
                            linkButtons.add(button);
                        }
                    }
                } catch (URISyntaxException se) {
                    Application.getInstance().getGUILog().log("[ERROR] An error occurred while generating View Editor links for " + element.getHumanName() + ". Unable to proceed.");
                    return;
                }
                // and display
                ViewEditorLinkForm viewLinkForm = new ViewEditorLinkForm(label, linkButtons);
                viewLinkForm.setVisible(true);
            }
            else {
                // build single link
                if (documents.isEmpty()) {
                    uriPath += "/documents/" + Converters.getElementToIdConverter().apply(element) + "/views/" + Converters.getElementToIdConverter().apply(element);
                }
                else {
                    uriPath += "/documents/" + Converters.getElementToIdConverter().apply(documents.iterator().next()) + "/views/" + Converters.getElementToIdConverter().apply(element);
                }
                // just open it if possible
                if (Desktop.isDesktopSupported()) {
                    try {
                        if (documents.size() == 0) {
                            Application.getInstance().getGUILog().log("[INFO] " + element.getHumanName()
                                    + " does not belong to a document hierarchy. Opening view in View Editor without document context.");
                        }
                        Desktop.getDesktop().browse(uriBase.setPath(uriPath).build());
                    } catch (URISyntaxException | IOException e1) {
                        Application.getInstance().getGUILog().log("[ERROR] An error occurred while opening the View Editor page. Link: " + uriBase.toString());
                        e1.printStackTrace();
                    }
                }
                else {
                    Application.getInstance().getGUILog().log("[WARNING] Java is unable to open links on your computer. Link: " + uriBase.toString());
                }
            }
        }
    }

    private class ViewButton extends JButton {
        private URI uri;

        ViewButton(String text, URI uri) {
            super(text);
            setup(uri);
            this.setMaximumSize(new Dimension(280, 18));
        }

        void setup(URI u) {
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
                    Application.getInstance().getGUILog().log("[ERROR] An error occurred while opening the View Editor page. Link: " + uri.toString());
                }
            }
            else {
                Application.getInstance().getGUILog().log("[WARNING] Java is unable to open links on your computer. Link: " + uri.toString());
            }
        }
    }


}

