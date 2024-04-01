package org.openmbee.mdk.actions;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.sysml.util.SysMLProfile;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import org.openmbee.mdk.SysMLExtensions;
import org.openmbee.mdk.api.incubating.convert.Converters;
import org.openmbee.mdk.options.MDKProjectOptions;
import org.openmbee.mdk.ui.ViewEditorLinkForm;
import org.openmbee.mdk.util.MDUtils;
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
        Stereotype documentStereotype = SysMLExtensions.getInstanceByProject(project).document().getStereotype();
        Stereotype viewStereotype = SysMLProfile.getInstanceByProject(project).view().getStereotype();

        for (Element element : targetElements) {
            if (!StereotypesHelper.hasStereotypeOrDerived(element, viewStereotype)
                    && !StereotypesHelper.hasStereotypeOrDerived(element, documentStereotype)) {
                continue;
            }

            // build url
            URIBuilder uriBase = MDKProjectOptions.getVeUrl(project);
            if (uriBase == null)
                return;


            String viewFragment;
            try {
                viewFragment = "/projects/" + Converters.getIProjectToIdConverter().apply(project.getPrimaryProject()) + "/" + MDUtils.getBranchId(project);
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
                                viewFragment += "/" + Converters.getElementToIdConverter().apply(element) + "/present?viewId=" + Converters.getElementToIdConverter().apply(element);
                            }
                            else {
                                viewFragment += "/" + Converters.getElementToIdConverter().apply(doc) + "/present?viewId=" + Converters.getElementToIdConverter().apply(element);
                            }
                            JButton button = new ViewButton(doc.getHumanName(), uriBase.setFragment(viewFragment).build());
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
                    viewFragment += "/" + Converters.getElementToIdConverter().apply(element) + "/present?viewId=" + Converters.getElementToIdConverter().apply(element);
                }
                else {
                    viewFragment += "/" + Converters.getElementToIdConverter().apply(documents.iterator().next()) + "/present?viewId=" + Converters.getElementToIdConverter().apply(element);
                }
                // just open it if possible
                if (Desktop.isDesktopSupported()) {
                    try {
                        if (documents.size() == 0) {
                            Application.getInstance().getGUILog().log("[INFO] " + element.getHumanName()
                                    + " does not belong to a document hierarchy. Opening view in View Editor without document context.");
                        }
                        Desktop.getDesktop().browse(uriBase.setFragment(viewFragment).build());
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

