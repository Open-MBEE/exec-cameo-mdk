package gov.nasa.jpl.mbee.mdk.actions;

import com.nomagic.datahub.datamodel.Relation;
import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.ui.dialogs.MDDialogParentProvider;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
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
import java.net.URL;
import java.util.*;
import java.util.List;
 
/*******************************************************************************
* Copyright (c) <2013>, California Institute of Technology ("Caltech"). 
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
        targetElements = new ArrayList<Element>();
        targetElements.add(element);
    }
 
    @Override
    public void actionPerformed(ActionEvent e) {
        List<String> viewURLs = new ArrayList<>();
        for (Element element: targetElements) {
            Project project = Project.getProject(element);
            // build url
            boolean showPane = false;
            int numDocuments = 0;
            URI link = null;
            JPanel display = new JPanel();
            GridLayout column = new GridLayout(0,1);
            display.setLayout(column);
 
            // can't use URIBuilder, it converts the ‘#’ in the url and breaks things
            String uriBasePath = MMSUtils.getServiceUri(project).setPath("").clearParameters().toString()
                    + "/alfresco/mmsapp/mms.html#/workspaces/" + MDUtils.getWorkspace(project)
                    + "/sites/" + MMSUtils.getSiteName(project);
            try {
                Set<Element> documents = new HashSet<>();
                if (StereotypesHelper.hasStereotype(element, Utils.getDocumentStereotype())) {
                    documents.add(element);
//                    URI link = new URI(uriBasePath + "/documents/" + element.getID());
//                    SwingLink viewLinkLabel = new SwingLink(link.toString(), link);
//                    display.add(viewLinkLabel);
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
                if (!documents.isEmpty()) {
                    display.add(new JLabel("This element may be shown in the following views:"));

                    for (Element doc : documents) {

                        if (doc.equals(element)) {
                            link = new URI(uriBasePath + "/documents/" + element.getID());
                        } else {
                            link = new URI(uriBasePath + "/documents/" + doc.getID() + "/views/" + element.getID());
                        }
                        String documentName = "(empty name)";
                        if(doc instanceof NamedElement && ((NamedElement) doc).getName()!=null &! ((NamedElement) doc).getName().isEmpty()) {
                        documentName = ((NamedElement) doc).getName();
                        }
                        SwingLink viewLinkLabel = new SwingLink(link.toString(), link);
                        display.add(new JLabel("In document "+documentName + ": "));
                        display.add(viewLinkLabel);
                        numDocuments++;
                    }
                }
                else {
                    display.add(new JLabel("This view is not associated with any documents."));
                }
            }
            catch (URISyntaxException se) {
                display.add(new JLabel("[ERROR] Unable to generate view links for this element."));
            }

            if(numDocuments>1){
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
                JOptionPane.showMessageDialog(MDDialogParentProvider.getProvider().getDialogParent(), display);
            }
        }
    }
 
 
    public class SwingLink extends JLabel {
        private static final long serialVersionUID = 8273875024682878518L;
        private String text;
        private URI uri;
 
        public SwingLink(String text, URI uri){
            super();
            setup(text,uri);
        }
 
        public SwingLink(String text, String uri){
            super();
            setup(text,URI.create(uri));
        }
 
        public void setup(String t, URI u){
            text = t;
            uri = u;
            setText(text);
            setToolTipText(uri.toString());
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    open(uri);
                }
                public void mouseEntered(MouseEvent e) {
                    setText(text,false);
                }
                public void mouseExited(MouseEvent e) {
                    setText(text,true);
                }
            });
        }
 
        @Override
        public void setText(String text){
            setText(text,true);
        }
 
        public void setText(String text, boolean ul){
            String link = ul ? "<u>"+text+"</u>" : text;
            super.setText("<html><span style=\"color: #000099;\">"+
                    link+"</span></html>");
            this.text = text;
        }
 
        public String getRawText(){
            return text;
        }
 
        private void open(URI uri) {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                try {
                    desktop.browse(uri);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null,
                            "Failed to launch the link, your computer is likely misconfigured.",
                            "Cannot Launch Link",JOptionPane.WARNING_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null,
                        "Java is not able to launch links on your computer.",
                        "Cannot Launch Link", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
 
}
 