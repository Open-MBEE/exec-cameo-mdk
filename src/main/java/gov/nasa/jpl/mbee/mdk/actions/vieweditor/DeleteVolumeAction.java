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
package gov.nasa.jpl.mbee.mdk.actions.vieweditor;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.ViewEditorProfile;
import gov.nasa.jpl.mbee.mdk.lib.Utils;
import gov.nasa.jpl.mbee.mdk.viewedit.ViewEditUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

@Deprecated
public class DeleteVolumeAction extends MDAction {
    private static final long serialVersionUID = 1L;
    private Element proj;
    public static final String actionid = "DeleteVolume";

    public DeleteVolumeAction(Element e) {
        super(actionid, "Remove From View Editor", null, null);
        proj = e;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GUILog gl = Application.getInstance().getGUILog();
        String volid = proj.getID();
        List<Element> projects = Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(proj,
                ViewEditorProfile.hasVolume, 2, false, 1);
        boolean root = false;

        for (Element p : projects) {
            if (StereotypesHelper.hasStereotype(p, ViewEditorProfile.project)) {
                root = true;
            }
        }

        if (!root) {
            Utils.showPopupMessage("You cannot remove a non-root volume from view editor directly");
            return;
        }
        String url = ViewEditUtils.getUrl();
        if (url == null || url.equals("")) {
            return;
        }
        url += "/rest/projects/volume/" + volid + "/delete";
        PostMethod pm = new PostMethod(url);
        try {
            HttpClient client = new HttpClient();
            ViewEditUtils.setCredentials(client, url, pm);
            int code = client.executeMethod(pm);
            if (ViewEditUtils.showErrorMessage(code)) {
                return;
            }
            String response = pm.getResponseBodyAsString();
            if (response.equals("ok")) {
                gl.log("[INFO] Remove Successful.");
            }
            else if (response.equals("NotFound")) {
                gl.log("[ERROR] Volume not found.");
            }
            else {
                gl.log(response);
            }
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            pm.releaseConnection();
        }
    }

}
