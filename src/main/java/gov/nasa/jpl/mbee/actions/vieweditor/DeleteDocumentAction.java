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
package gov.nasa.jpl.mbee.actions.vieweditor;

import gov.nasa.jpl.mbee.ViewEditorProfile;
import gov.nasa.jpl.mbee.viewedit.ViewEditUtils;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class DeleteDocumentAction extends MDAction {
    private static final long serialVersionUID = 1L;
    private Element            proj;
    public static final String actionid = "DeleteDocument";

    public DeleteDocumentAction(Element e) {
        super(actionid, "Remove From View Editor", null, null);
        proj = e;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GUILog gl = Application.getInstance().getGUILog();
        String docid = proj.getID();
        if (StereotypesHelper.hasStereotypeOrDerived(proj, ViewEditorProfile.document)) {
            docid = (String)StereotypesHelper.getStereotypePropertyFirst(proj, ViewEditorProfile.document,
                    "documentId");
        }
        String url = ViewEditUtils.getUrl();
        if (url == null || url.equals(""))
            return;
        url += "/rest/projects/document/" + docid + "/delete";
        PostMethod pm = new PostMethod(url);
        try {
            // pm.setRequestHeader("Content-Type", "text/json");
            // pm.setRequestEntity(JsonRequestEntity.create(vol.getID()));
            // Protocol easyhttps = new Protocol("https", new
            // EasySSLProtocolSocketFactory(), 443);
            // Protocol.registerProtocol("https", easyhttps);
            HttpClient client = new HttpClient();
            ViewEditUtils.setCredentials(client, url, pm);
            int code = client.executeMethod(pm);
            if (ViewEditUtils.showErrorMessage(code))
                return;
            String response = pm.getResponseBodyAsString();
            if (response.equals("ok"))
                gl.log("[INFO] Remove Successful.");
            else if (response.equals("NotFound"))
                gl.log("[ERROR] Document not found.");
            else
                gl.log(response);
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            pm.releaseConnection();
        }
    }

}
