/*******************************************************************************
 * Copyright (c) <2013>, California Institute of Technology ("Caltech").
 * U.S. Government sponsorship acknowledged.
 * <p>
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 * <p>
 * - Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 * - Neither the name of Caltech nor its operating division, the Jet Propulsion Laboratory,
 * nor the names of its contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 * <p>
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
package gov.nasa.jpl.mbee.mdk.ems;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.ci.persistence.IAttachedProject;
import com.nomagic.ci.persistence.IProject;
import com.nomagic.ci.persistence.versioning.IVersionDescriptor;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.core.project.ProjectDescriptorsFactory;
import com.nomagic.magicdraw.foundation.MDObject;
import com.nomagic.magicdraw.teamwork2.ProjectVersion;
import com.nomagic.magicdraw.teamwork2.TeamworkService;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.CallBehaviorAction;
import com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities.ActivityEdge;
import com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities.ActivityParameterNode;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdtemplates.StringExpression;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Dependency;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdbasicbehaviors.OpaqueBehavior;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdcommunications.CallEvent;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdcommunications.Event;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdcommunications.Trigger;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdsimpletime.Duration;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdsimpletime.DurationInterval;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdsimpletime.TimeExpression;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdsimpletime.TimeInterval;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.Connector;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.ConnectorEnd;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Extension;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.ProfileApplication;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.*;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdprotocolstatemachines.ProtocolConformance;
import gov.nasa.jpl.mbee.mdk.DocGen3Profile;
import gov.nasa.jpl.mbee.mdk.MDKPlugin;
import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.ems.jms.JMSUtils;
import gov.nasa.jpl.mbee.mdk.ems.sync.queue.OutputQueue;
import gov.nasa.jpl.mbee.mdk.ems.sync.queue.Request;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.lib.MDUtils;
import gov.nasa.jpl.mbee.mdk.lib.Utils;
import gov.nasa.jpl.mbee.mdk.options.MDKOptionsGroup;
import gov.nasa.jpl.mbee.mdk.viewedit.ViewEditUtils;
import gov.nasa.jpl.mbee.mdk.web.JsonRequestEntity;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.jms.*;
import javax.swing.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

@Deprecated
//TODO migrate usage of these methods to EMFExporter.java @donbot
public class ExportUtility {
    public static boolean justPostconditionIds = true;  // don't embed conditions

    public static Logger log = Logger.getLogger(ExportUtility.class);
    public static Map<String, Integer> mountedVersions;
    private static String developerUrl = "https://sheldon.jpl.nasa.gov";
    private static String developerSite = "europa";
    private static String developerWs = "master";
    public static boolean baselineNotSet = false;
    public static Map<String, Map<String, String>> wsIdMapping = new HashMap<String, Map<String, String>>();
    public static Map<String, Map<String, String>> sites = new HashMap<String, Map<String, String>>();

    public static void updateWorkspaceIdMapping() {
        String projId = Application.getInstance().getProject().getPrimaryProject().getProjectID();
        Map<String, String> idmapping = null;
        if (wsIdMapping.containsKey(projId)) {
            idmapping = wsIdMapping.get(projId);
        }
        else {
            idmapping = new HashMap<String, String>();
            wsIdMapping.put(projId, idmapping);
        }

        String url = getUrl(Application.getInstance().getProject());
        if (url == null) {
            return;
        }
        url += "/workspaces";
        String result = null;
        try {
            result = get(url, false);
        } catch (ServerException ex) {

        }
        if (result != null) {
            idmapping.clear();
            JSONObject ob = (JSONObject) JSONValue.parse(result);
            JSONArray array = (JSONArray) ob.get("workspaces");
            for (Object ws : array) {
                JSONObject workspace = (JSONObject) ws;
                String id = (String) workspace.get("id");
                String qname = (String) workspace.get("qualifiedName");
                idmapping.put(qname, id);
            }
        }
    }

    public static void updateMasterSites() {
        String projId = Application.getInstance().getProject().getPrimaryProject().getProjectID();
        Map<String, String> idmapping = null;
        if (sites.containsKey(projId)) {
            idmapping = sites.get(projId);
        }
        else {
            idmapping = new HashMap<String, String>();
            sites.put(projId, idmapping);
        }

        String url = getUrl(Application.getInstance().getProject());
        if (url == null) {
            return;
        }
        url += "/workspaces/master/sites";
        String result = null;
        try {
            result = get(url, false);
        } catch (ServerException ex) {

        }
        if (result != null) {
            idmapping.clear();
            JSONObject ob = (JSONObject) JSONValue.parse(result);
            JSONArray array = (JSONArray) ob.get("sites");
            for (Object ws : array) {
                JSONObject site = (JSONObject) ws;
                String id = (String) site.get(MDKConstants.SYSML_ID_KEY);
                idmapping.put((String) site.get("name"), id);
            }
        }
    }

    public static boolean siteExists(String site, boolean human) {
        String projId = Application.getInstance().getProject().getPrimaryProject().getProjectID();
        Map<String, String> idmapping = null;
        if (sites.containsKey(projId)) {
            idmapping = sites.get(projId);
        }
        else {
            idmapping = new HashMap<String, String>();
            sites.put(projId, idmapping);
        }
        if (human) {
            return idmapping.keySet().contains(site);
        }
        else {
            return idmapping.values().contains(site);
        }
    }

    public static final Set<String> IGNORE_SLOT_FEATURES = new HashSet<String>(Arrays.asList(
            "_17_0_2_3_e9f034d_1375396269655_665865_29411", // stylesaver
            "_17_0_2_2_ff3038a_1358222938684_513628_2513", // integrity
            "_17_0_2_2_ff3038a_1358666613056_344763_2540", // integrity
            "_17_0_2_3_407019f_1383165366792_59388_29094", // mms
            "_17_0_2_3_407019f_1389652520710_658839_29078", // mms
            "_17_0_2_3_407019f_1391466672868_698092_29164", // mms
            "_be00301_1073306188629_537791_2", // diagraminfo
            "_be00301_1077726770128_871366_1", // diagraminfo
            "_be00301_1073394345322_922552_1", // diagraminfo
            "_16_8beta_8ca0285_1257244649124_794756_344", //diagraminfo
            "_11_5EAPbeta_be00301_1147431377925_245593_1615", //propertyPath
            "_16_5beta1_8ba0276_1232443673774_531258_269", //diagramTable
            "_16_5beta1_8ba0276_1232443673774_745303_270",
            "_16_5beta1_8ba0276_1232443673774_935617_271",
            "_16_5beta1_8ba0276_1232443673774_678788_272",
            "_16_6beta_8ba0276_1244211637437_237626_366",
            "_16_6beta_9020291_1246357739330_848040_336",
            "_16_6_8e8028e_1254469858416_898766_344",
            "_16_8beta_8e8028e_1257349277183_113594_375",
            "_16_8beta_8ba0276_1257943223236_390587_970",
            "_16_9beta_8ba0276_1273470566124_805209_1268",
            "_17_0beta_641020e_1283429856637_842592_1509",
            "_17_0beta_641020e_1283429914629_129769_1512",
            "_17_0beta_8ba0276_1284990422899_274631_1524",
            "_17_0beta_641020e_1285323269972_481272_1555",
            "_18_0beta_8e10289_1385041001405_147345_3249",
            "_18_0beta_8e10289_1385043703949_249972_3255",
            "_18_0beta_8e10289_1386323880115_217238_3252",
            "_18_0beta_8e10289_1387266836276_325174_3258",
            "_17_0beta_641020e_1285584942604_770258_1555",
            "_18_0beta_8e8028e_1384177586257_166109_3250", //instance table
            "_17_0_3beta_8e10289_1348753189250_976469_2933", //dependency matrix
            "_17_0_3beta_8e10289_1348753189288_719322_3041",
            "_17_0_3beta_8e10289_1348753189308_306094_3073",
            "_17_0_3beta_8e10289_1348753189316_438832_3087",
            "_17_0_3beta_8e10289_1348753189264_979131_2971",
            "_17_0_3beta_8e10289_1348753189264_72200_2968",
            "_17_0_3beta_8e10289_1348753189283_64144_3030",
            "_17_0_4beta_8e10289_1359443478322_371106_3007",
            "_17_0_4beta_8e10289_1359443708558_424258_3014",
            "_17_0_4beta_8ca0285_1361197666671_94490_3026",
            "_17_0_4beta_8f90291_1361803516746_297162_3018",
            "_17_0_4beta_8f90291_1361805357951_82494_3264",
            "_17_0_4beta_8f90291_1361805429858_801243_3284",
            "_17_0_4beta_8f90291_1361805612723_667253_3304",
            "_17_0_4beta_8f90291_1361805739775_551814_3329",
            "_17_0_4beta_8f90291_1361805778266_117541_3346",
            "_17_0_4beta_8f90291_1361806174665_674570_3425",
            "_17_0_4beta_8f90291_1361806780185_829302_3479",
            "_17_0_4beta_8e10289_1361896019117_533508_3014",
            "_17_0_4beta_8e10289_1362067034050_524960_3347",
            "_17_0_5beta_8e10289_1370856956242_284758_3129",
            "_17_0_5beta_8e10289_1370857054963_105141_3132",
            "_17_0_3beta_8e10289_1348753189250_698438_2944", //matrix filter
            "_17_0_3beta_8e10289_1348753189269_689567_2994",
            "_17_0_3beta_8e10289_1348753189234_132241_2888",
            "_17_0_3beta_8e10289_1348753189236_123477_2907",
            "_17_0_3beta_8e10289_1348753189264_179517_2974",
            "_17_0_3beta_8e10289_1348753189250_349424_2939",
            "_17_0_3beta_8e10289_1348753189284_755266_3033",
            "_17_0_3beta_8e10289_1348753189235_302185_2895",
            "_17_0_3beta_8e10289_1348753189320_16317_3090",
            "_17_0_3beta_8e10289_1348753189269_548324_2992",
            "_16_8beta_9020291_1260453954350_740392_1196", //relation map
            "_16_8beta_9020291_1260453966240_198881_1206",
            "_16_8beta_9020291_1260453967146_76281_1208",
            "_16_8beta_9020291_1260453968333_553037_1212",
            "_16_8beta_9020291_1260453968865_442280_1214",
            "_16_8beta_9020291_1260453970130_871899_1216",
            "_16_8beta_9020291_1260453970505_464947_1218",
            "_16_8beta_9020291_1260453971021_882247_1220",
            "_16_8beta_9020291_1260453972114_74547_1226",
            "_16_8beta_9020291_1260453972411_254735_1228",
            "_16_8beta_9020291_1261404200708_645145_1058",
            "_16_8beta_9020291_1261404274486_811759_1095",
            "_16_8beta_9020291_1261404325436_538645_1109",
            "_16_8beta_9020291_1261404349386_293128_1122",
            "_16_8beta_9020291_1262175442698_661575_1141",
            "_16_8beta_9020291_1262175474107_293557_1151",
            "_16_8beta_9020291_1262778651547_617085_1106",
            "_16_8beta_9020291_1265099984697_425850_1246",
            "_17_0_4beta_8850271_1363160735759_359423_3337",
            "_17_0_4beta_8850271_1363684316441_131742_3334",
            "_17_0_5beta_8850271_1378111695683_872709_3379",
            "_17_0_3_85f027d_1362349793876_101885_3031", //specification table
            "_17_0_3_85f027d_1362349793876_376001_3032",
            "_17_0_3_85f027d_1362349793876_780075_3033",
            "_17_0_4beta_85f027d_1366953341699_324867_3761",
            "_18_0_2_407019f_1433361787467_278914_14410" //view elements dummy slot
    ));

    public static final Set<String> IGNORE_INSTANCE_CLASSIFIERS = new HashSet<String>(Arrays.asList(
            "_11_5EAPbeta_be00301_1147431307463_773225_1455", //nested connector end
            "_16_5beta1_8ba0276_1232443673758_573873_267", //diagram table
            "_9_0_be00301_1108044380615_150487_0", //diagram info
            "_18_0beta_8e8028e_1384177586203_506524_3245", //instance table
            "_17_0_3beta_8e10289_1348753189236_873942_2915", //dependency matrix
            "_17_0_3beta_8e10289_1348753189306_38038_3061", //matrix filter
            "_16_8beta_9020291_1260453936960_387965_1187", //relation map
            "_17_0_2_3_407019f_1383165357327_898985_29071", //mms
            "_17_0_3_85f027d_1362349793845_681432_2986" //specification table
    ));

    public static String getElementID(Element e) {
        if (e == null) {
            return null;
        }
        if (e instanceof Slot) {
            Slot slot = (Slot) e;
            if (slot.getOwningInstance() == null || slot.getDefiningFeature() == null) {
                return null;
            }
            return slot.getOwningInstance().getID() + "-slot-" + slot.getDefiningFeature().getID();
        }
        else if (e instanceof Model && e == Application.getInstance().getProject().getModel()) {
            return Application.getInstance().getProject().getPrimaryProject().getProjectID();
        }
        return e.getID();
    }

    public static Element getElementFromID(Project project, String id) {
        if (project == null || id == null) {
            return null;
        }
        String[] ids = id.split("-slot-");
        if (ids.length < 2) {
            if (id.equals(project.getPrimaryProject().getProjectID())) {
                return project.getModel();
            }
            return (Element) project.getElementByID(ids[0]);
        }
        else {
            Element instancespec = (Element) project.getElementByID(ids[0]);
            Element definingFeature = (Element) project.getElementByID(ids[1]);
            if (instancespec != null && definingFeature != null && instancespec instanceof InstanceSpecification) {
                for (Slot slot : ((InstanceSpecification) instancespec).getSlot()) {
                    if (slot.getDefiningFeature() == definingFeature) {
                        return slot;
                    }
                }
            }
            else {
                return null;
            }
        }
        return null;
    }

    /*
    public static String getUrl(Project project) {
        if (project == null || project.getModel() == null) {
            return null;
        }
        String url;
        if (StereotypesHelper.hasStereotype(project.getModel(), "ModelManagementSystem")) {
            url = (String) StereotypesHelper.getStereotypePropertyFirst(project.getModel(),
                    "ModelManagementSystem", "MMS URL");
            if (url == null || url.equals("")) {
                Utils.showPopupMessage("Your project root element doesn't have ModelManagementSystem MMS URL stereotype property set!");
                url = null;
            }
        }
        else {
            Utils.showPopupMessage("Your project root element doesn't have ModelManagementSystem MMS URL stereotype property set!");
            url = null;
        }
        if (url == null && MDUtils.isDeveloperMode()) {
            url = JOptionPane.showInputDialog("[DEVELOPER MODE] Enter the editor URL:", developerUrl);
        }
        if (url == null || url.equals("")) {
            return null;
        }
        developerUrl = url;
        url += "/alfresco/service";
        return url;
    }
    */

    /*
    public static String getSite() {
        Element model = Application.getInstance().getProject().getModel();
        String site = (String) StereotypesHelper.getStereotypePropertyFirst(model, "ModelManagementSystem", "MMS Site");
        if (site == null || site.equals("")) {
            Utils.showPopupMessage("Your project root element doesn't have ModelManagementSystem MMS Site stereotype property set!");
            site = null;
        }
        if (site == null && MDUtils.isDeveloperMode()) {
            site = JOptionPane.showInputDialog("[DEVELOPER MODE] Enter the site:", developerSite);
        }
        if (site == null || site.equals("")) {
            return null;
        }
        developerSite = site;
        // do switch here
        return site;
    }
    */

    public static String getSiteForProject(IProject prj) {
        String human = getHumanSiteForProject(prj);
        return sites.get(Application.getInstance().getProject().getPrimaryProject().getProjectID()).get(human);
    }


    public static String getHumanSiteForProject(IProject prj) {
        return prj.getName().toLowerCase().replace(' ', '-').replace('_', '-').replace('.', '-');
    }

    public static String getWorkspace() {
        Project project = Application.getInstance().getProject();
        String twbranch = getTeamworkBranch(project);
        if (twbranch == null) {
            return "master";
        }
        twbranch = "master/" + twbranch;
        String projId = Application.getInstance().getProject().getPrimaryProject().getProjectID();
        Map<String, String> wsmap = wsIdMapping.get(projId);
        if (wsmap == null) {
            updateWorkspaceIdMapping();
            wsmap = wsIdMapping.get(projId);
        }
        if (wsmap != null) {
            String id = wsmap.get(twbranch);
            if (id == null) {
                updateWorkspaceIdMapping();
                id = wsmap.get(twbranch);
            }
            if (id != null) {
                return id;
            }
        }
        Utils.guilog("[ERROR]: Cannot lookup workspace on server that corresponds to this project branch");
        return null;
    }

    /*
    @Deprecated
    // TODO Add project as parameter @donbot
    public static String getUrlWithWorkspace() {
        String url = getUrl(Application.getInstance().getProject());
        String workspace = getWorkspace();
        if (url != null && workspace != null) {
            return url + "/workspaces/" + workspace;
        }
        return null;
    }
    */

    /*
    @Deprecated
    // TODO Add project as parameter @donbot
    public static String getUrlWithWorkspaceAndSite() {
        String url = getUrl(Application.getInstance().getProject());
        String workspace = getWorkspace();
        String site = getSite();
        if (url != null && workspace != null && site != null) {
            return url + "/workspaces/" + workspace + "/sites/" + site;
        }
        return null;
    }
*/

    /*
    @Deprecated
    // TODO Add project as parameter @donbot
    public static String getUrlForProject() {
        String url = getUrl(Application.getInstance().getProject());
        String site = getSite();
        if (url == null || site == null) {
            return null;
        }
        return url + "/workspaces/master/sites/" + site + "/projects/" + Application.getInstance().getProject().getPrimaryProject().getProjectID();
        /*String url = getUrlWithWorkspaceAndSite();
        if (url != null)
            return url + "/projects/" + Application.getInstance().getProject().getPrimaryProject().getProjectID();
        return null;*/
    /*
    }
    */

    /*
    public static String getUrlForProject(IProject prj) {
        String url = getUrl(Application.getInstance().getProject());
        String site = getSiteForProject(prj);
        if (url != null && site != null) {
            return url + "/workspaces/master/sites/" + site + "/projects/" + prj.getProjectID();
        }
        return null;
    }
    */

    /*
    @Deprecated
    // TODO Add project as parameter @donbot
    public static String getPostElementsUrl() {
        String url = getUrlWithWorkspaceAndSite();
        if (url == null) {
            return null;
        }
        return url + "/elements";
    }
    */

    public static boolean showErrors(int code, String response, boolean showPopupErrors) {
        if (code != 200) {
            if (code >= 500) {
                if (showPopupErrors) {
                    Utils.showPopupMessage("Server Error. See message window for details.");
                }
                Utils.guilog(response);
            }
            else if (code == 401) {
                if (showPopupErrors) {
                    Utils.showPopupMessage("You are not authorized or don't have permission. You can login and try again.");
                }
                else {
                    Utils.guilog("You are not authorized or don't have permission. You can login and try again.");
                }
                ViewEditUtils.clearUsernameAndPassword();
            }
            else if (code == 403) {
                if (showPopupErrors) {
                    Utils.showPopupMessage("You do not have permission to do this.");
                }
                else {
                    Utils.guilog("You do not have permission to do this.");
                }
            }
            else {
                try {
                    Object o = JSONValue.parse(response);
                    if (o instanceof JSONObject && ((JSONObject) o).containsKey("message")) {
                        Utils.guilog("Server message: " + code + " " + ((JSONObject) o).get("message"));
                    }
                    else if (o instanceof JSONObject || o instanceof JSONArray) {
                        Utils.guilog("Server response: " + code + (MDKOptionsGroup.getMDKOptions().isLogJson() ? " " + response : ""));
                    }
                    else {
                        Utils.guilog("Server response: " + code + " " + response);
                    }
                } catch (Exception c) {
                    Utils.guilog("Server response: " + code + " " + response);
                }
                if (code == 400) {
                    return false;
                }
            }
            return true;
        }
        try {
            Object o = JSONValue.parse(response);
            if (o instanceof JSONObject && ((JSONObject) o).containsKey("message")) {
                Utils.guilog("Server message: 200 " + ((JSONObject) o).get("message"));
            }
        } catch (Exception c) {

        }
        return false;
    }

    public static String delete(String url, boolean feedback) {
        boolean print = MDKOptionsGroup.getMDKOptions().isLogJson();
        if (url == null) {
            return null;
        }
        checkAndResetTicket(url);
        url = addTicketToUrl(url);
        DeleteMethod gm = new DeleteMethod(url);
        try {
            HttpClient client = new HttpClient();
            ViewEditUtils.setCredentials(client, url, gm);
            if (print) {
                log.info("delete: " + url);
            }
            if (feedback) {
                Utils.guilog("[INFO] Deleting...");
            }
            int code = client.executeMethod(gm);
            String json = gm.getResponseBodyAsString();
            if (print) {
                log.info("delete response: " + json);
            }
            if (showErrors(code, json, false)) {
                return null;
            }
            if (feedback) {
                Utils.guilog("[INFO] Delete Successful");
            }
            return json;
        } catch (Exception ex) {
            Utils.printException(ex);
        } finally {
            gm.releaseConnection();
        }
        return null;

    }

    public static String send(String url, PostMethod pm) {
        boolean print = MDKOptionsGroup.getMDKOptions().isLogJson();
        if (url == null) {
            return null;
        }
        checkAndResetTicket(url);
        url = addTicketToUrl(url);
        try {
            // GUILog gl = Application.getInstance().getGUILog();
            Utils.guilog("[INFO] Sending file...");
            if (print) {
                log.info("send file: " + url);
            }
            HttpClient client = new HttpClient();
            ViewEditUtils.setCredentials(client, url, pm);
            int code = client.executeMethod(pm);
            String response = pm.getResponseBodyAsString();
            if (print) {
                log.info("send file response: " + code + " " + response);
            }
            if (showErrors(code, response, false)) {
                return null;
            }
            Utils.guilog("[INFO] Send File Successful.");
            return response;
        } catch (Exception ex) {
            Utils.printException(ex);
            return null;
        } finally {
            pm.releaseConnection();
        }
    }

    public static String send(String url, String json, /*String method,*/ boolean showPopupErrors, boolean suppressGuiLog) {
        return send(url, json, showPopupErrors, suppressGuiLog, "Send#?");
    }

    public static String send(String url, String json, /*String method,*/ boolean showPopupErrors, boolean suppressGuiLog, String _threadName) {
        boolean print = MDKOptionsGroup.getMDKOptions().isLogJson();
        if (url == null) {
            return null;
        }
        checkAndResetTicket(url);
        url = addTicketToUrl(url);
        EntityEnclosingMethod pm = null;
        //if (method == null)
        pm = new PostMethod(url);
        //else
        //pm = new PutMethod(url);
        //GUILog gl = Application.getInstance().getGUILog();
        try {
            if (!suppressGuiLog) {
                Utils.guilog("[INFO] Sending...");
            }
            // if (json.length() > 3000) {
            // System.out.println(json);
            //   log.info(_id + " send: " + url + ": " + json);
            //gl.log("(see md.log for what got sent - too big to show)");
            //} else
            log.info(_threadName + " send: " + url + ": " + json);// gl.log(json);
            pm.setRequestHeader("Content-Type", "application/json;charset=utf-8");
            pm.setRequestEntity(JsonRequestEntity.create(json));
            HttpClient client = new HttpClient();

            /*int timeout = 120; // seconds
            HttpParams httpParams = client.getParams();
            httpParams.setParameter(HttpConnectionParams.CONNECTION_TIMEOUT, timeout * 1000); //cause ConnectionTimeoutException
            httpParams.setParameter(HttpConnectionParams.SO_TIMEOUT, timeout * 1000); //casue SockteTimeoutException
            */

            ViewEditUtils.setCredentials(client, url, pm);
            if (print) {
                log.info(_threadName + " executing....");
            }
            int code = client.executeMethod(pm);
            if (print) {
                log.info(_threadName + " server returned: " + code);
            }
            String response = pm.getResponseBodyAsString();
            if (print) {
                log.info(_threadName + " response: " + code + " " + response);
            }
            if (showErrors(code, response, showPopupErrors)) {
                return null;
            }
            if (print) {
                log.info(_threadName + " Send Successful.");
            }
            if (!suppressGuiLog) {
                Utils.guilog("[INFO] Send Successful.");
            }
            return response;
        } catch (org.apache.commons.httpclient.ConnectTimeoutException ex) { //the time to establish the connection with the remote host
            Utils.printException(ex);
            return null;
        } catch (java.net.SocketTimeoutException ex) { //the time waiting for data after the connection was established; maximum time of inactivity between two data packets
            Utils.printException(ex);
            return null;
        } catch (Exception ex) { //java.net.SocketException: Software caused connection abort: recv failed
            Utils.printException(ex);
            return null;
        } finally {
            pm.releaseConnection();
        }
    }
    /*
     * public static String send(String url, String json, String method) { return send(url, json, method, true, false); }
	 */

    public static String send(String url, String json) {
        //return send(url, json, null); //method == null means POST
        return send(url, json/*, method*/, true, false);
    }

    public static String deleteWithBody(String url, String json, boolean feedback) {
        boolean print = MDKOptionsGroup.getMDKOptions().isLogJson();
        checkAndResetTicket(url);
        EntityEnclosingMethod pm = null;
        url = addTicketToUrl(url);
        pm = new DeleteMethodWithEntity(url);
        try {
            if (print) {
                log.info("deleteWithBody: " + url + ": " + json);// gl.log(json);
            }
            pm.setRequestHeader("Content-Type",
                    "application/json;charset=utf-8");
            pm.setRequestEntity(JsonRequestEntity.create(json));
            HttpClient client = new HttpClient();
            ViewEditUtils.setCredentials(client, url, pm);
            int code = client.executeMethod(pm);
            String response = pm.getResponseBodyAsString();
            if (print) {
                log.info("deleteWithBody Response: " + code + " " + response);
            }
            if (showErrors(code, response, false)) {
                return null;
            }
            if (feedback) {
                Utils.guilog("[INFO] Delete Successful");
            }
            return response;
        } catch (Exception ex) {
            Utils.printException(ex);
            return null;
        } finally {
            pm.releaseConnection();
        }
    }

    public static String getWithBody(String url, String json) throws ServerException {
        boolean print = MDKOptionsGroup.getMDKOptions().isLogJson();
        EntityEnclosingMethod pm = null;
        checkAndResetTicket(url);
        url = addTicketToUrl(url);
        pm = new GetMethodWithEntity(url);
        try {
            if (print) {
                log.info("getWithBody: " + url + ": " + json);// gl.log(json);
            }
            pm.setRequestHeader("Content-Type",
                    "application/json;charset=utf-8");
            pm.setRequestEntity(JsonRequestEntity.create(json));
            HttpClient client = new HttpClient();
            ViewEditUtils.setCredentials(client, url, pm);
            int code = client.executeMethod(pm);
            String response = pm.getResponseBodyAsString();
            if (print) {
                log.info("getWithBody Response: " + code + " " + response);
            }
            if (showErrors(code, response, false)) {
                throw new ServerException(json, code);
            }
            if (code == 400) {
                throw new ServerException(json, code);
            }
            return response;
        } catch (HttpException ex) {
            Utils.printException(ex);
            throw new ServerException("", 500);
        } catch (IOException ex) {
            Utils.printException(ex);
            throw new ServerException("", 500);
        } finally {
            pm.releaseConnection();
        }
    }

    //convert the view2view json object given by alfresco visitor to json server expects
    @SuppressWarnings("unchecked")
    public static JSONArray formatView2View(JSONObject vv) {
        JSONArray response = new JSONArray();
        for (Object viewid : vv.keySet()) {
            JSONArray children = (JSONArray) vv.get(viewid);
            JSONObject viewinfo = new JSONObject();
            viewinfo.put("id", viewid);
            viewinfo.put("childrenViews", children);
            response.add(viewinfo);
        }
        return response;
    }

    // convert view2view json array given by alfresco server to format created by alfresco visitor
    @SuppressWarnings("unchecked")
    public static JSONObject keyView2View(JSONArray vv) {
        JSONObject response = new JSONObject();
        for (Object viewinfo : vv) {
            String id = (String) ((JSONObject) viewinfo).get("id");
            JSONArray children = (JSONArray) ((JSONObject) viewinfo)
                    .get("childrenViews");
            if (response.containsKey(id) && !response.get(id).equals(children)) {
                //something is messed up
                Utils.log("[WARNING] Document hierarchy from MMS is inconsistent and will interfere with validation, please file a CAE Support jira at https://cae-jira.jpl.nasa.gov/projects/SSCAES/summary with component MD.MDK to request help to resolve.");
            }
            response.put(id, children);
        }
        return response;
    }

    // helper method for long for get() method. will trigger a login dialogue
    public static String get(String url) throws ServerException {
        return get(url, null, null, true);
    }

    // helper method for long for get() method. will trigger a login dialogue
    public static String get(String url, boolean showPopupErrors) throws ServerException {
        return get(url, null, null, showPopupErrors);
    }

    // helper method for long for get() method. will bypass the login dialogue if username is not null or empty ""
    public static String get(String url, String username, String password) throws ServerException {
        return get(url, username, password, true);
    }

    /*
    public static boolean checkAndResetTicket(String urlString) {
        // TODO Fix properly by passing project/site reference with the request so this sort of transformation isn't required
        URL url;
        String baseUrl;
        try {
            url = new URL(urlString);
            baseUrl = url.getProtocol() + "://" + url.getHost() + "/alfresco/service";
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }
        try {
            boolean validTicket = checkTicket(baseUrl);
            if (!validTicket) {
                String loggedIn = getTicket(baseUrl + "/api/login", ViewEditUtils.getUsername(), ViewEditUtils.getPassword(), false);
            }
            return true;
        } catch (ServerException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean checkTicket(String baseUrl) throws ServerException {
        boolean print = MDKOptionsGroup.getMDKOptions().isLogJson();
        String ticket = ViewEditUtils.getTicket();
        if (ticket == null || ticket.equals("")) {
            return false;
        }
        String url = baseUrl + "/mms/login/ticket/" + ViewEditUtils.getTicket();
        GetMethod gm = new GetMethod(url);
        try {
            HttpClient client = new HttpClient();
            if (print) {
                log.info("checkTicket: " + url);
            }
            int code = client.executeMethod(gm);
            String json = gm.getResponseBodyAsString();
            if (print) {
                log.info("checkTicket response: " + code + " " + json);
            }
            if (code != 404 && code != 200) {
                throw new ServerException(json, code); //?
            }
            //Application.getInstance().getGUILog().log("[INFO] Successful...");
            return code != 404;
        } catch (IOException | IllegalArgumentException ex) {
            //Utils.printException(ex);
            ex.printStackTrace();
            throw new ServerException("", 500);
        } finally {
            gm.releaseConnection();
        }
    }

    // long form get method allowing option of bypassing the login dialog if username is not null or empty ""
    public static String getTicket(String url, String username, String password, boolean showPopupErrors) throws ServerException {
        boolean print = MDKOptionsGroup.getMDKOptions().isLogJson();

        //curl -k https://cae-ems-origin.jpl.nasa.gov/alfresco/service/api/login -X POST -H Content-Type:application/json -d '{"username":"username", "password":"password"}'

        HttpClient client = new HttpClient();
        if (url == null) {
            return null;
        }
        // url = "https://cae-ems-origin.jpl.nasa.gov/alfresco/service/api/login";
        PostMethod postMethod = new PostMethod(url);
        String userpasswordJsonString = "";
        try {
            if (username != null && !username.equals("")) {
                ViewEditUtils.setUsernameAndPassword(username, password);
            }
            userpasswordJsonString = ViewEditUtils.getCredentialsString();
            //Application.getInstance().getGUILog().log("[INFO] Getting...");
            //Application.getInstance().getGUILog().log("url=" + url);

            // String JSON_STRING = "{\"username\":\"username\", \"password\":\"password\"}";

            StringRequestEntity requestEntity = new StringRequestEntity(
                    userpasswordJsonString,
                    "application/json",
                    "UTF-8");

            postMethod.setRequestEntity(requestEntity);
            int code = client.executeMethod(postMethod);
            String json = postMethod.getResponseBodyAsString();
            if (print) {
                log.info("get ticket response: " + code + " " + json);
            }
            if (showErrors(code, json, showPopupErrors)) {
                throw new ServerException(json, code);
            }
            if (code == 400) {
                throw new ServerException(json, code); //?
            }
            //Application.getInstance().getGUILog().log("[INFO] Successful...");

            JSONObject ob = (JSONObject) JSONValue.parse(json);
            if (ob != null) {
                JSONObject d = (JSONObject) ob.get("data");
                if (d != null && !d.isEmpty()) {
                    String ticket = (String) d.get("ticket");
                    ViewEditUtils.setTicket(ticket);
                }
                else {
                    return null;
                }
            }
            else {
                return null;
            }
            return json;
        } catch (HttpException ex) {
            Utils.printException(ex);
            throw new ServerException("", 500);
        } catch (IOException ex) {
            Utils.printException(ex);
            throw new ServerException("", 500);
        } catch (IllegalArgumentException ex) {
            Utils.showPopupMessage("URL is malformed");
            Utils.printException(ex);
            throw new ServerException("", 500);
        } finally {
            postMethod.releaseConnection();
        }
    }
    */

    // long form get method allowing option of bypassing the login dialog if username is not null or empty ""
    public static String get(String url, String username, String password, boolean showPopupErrors) throws ServerException {
        boolean print = MDKOptionsGroup.getMDKOptions().isLogJson();
        if (url == null) {
            return null;
        }
        // url = stuff
        checkAndResetTicket(url);
        url = addTicketToUrl(url);
        // url += alf_ticket=<ticket>
        GetMethod gm = new GetMethod(url);
        // get method for that stuff
        try {
            HttpClient client = new HttpClient();
            if (username == null || username.equals("")) {
                ViewEditUtils.setCredentials(client, url, gm);
            }
            else {
                ViewEditUtils.setCredentials(client, url, gm, username, password);
            }
            // get method has a new Header("Authorization", getAuthStringEnc()));
            if (print) {
                log.info("get: " + url);
            }
            // executes
            // parses response
            int code = client.executeMethod(gm);
            String json = gm.getResponseBodyAsString();
            // TODO Remove this hackery @donbot
            if (code == 404) {
                json = null;
            }
            if (print) {
                log.info("get response: " + code + " " + json);
            }
            if (showErrors(code, json, showPopupErrors)) {
                throw new ServerException(json, code);
            }
            if (code == 400) {
                throw new ServerException(json, code); //?
            }
            return json;
        } catch (IOException | IllegalArgumentException ex) {
            ex.printStackTrace();
            throw new ServerException("", 500);
        } finally {
            gm.releaseConnection();
        }
    }

    public static String addTicketToUrl(String r) {
        String ticket = ViewEditUtils.getTicket();
        if (ticket == null || ticket.equals("")) {
            return r;
        }
        String url = r;
        if (url.contains("?")) {
            url += "&alf_ticket=" + ticket;
        }
        else {
            url += "?alf_ticket=" + ticket;
        }
        return url;
    }

    //check if comment is actually the documentation of its owner
    public static boolean isElementDocumentation(Comment c) {
        if (c.getAnnotatedElement().size() > 1 || c.getAnnotatedElement().isEmpty()) {
            return false;
        }
        return c.getAnnotatedElement().iterator().next() == c.getOwner();
    }

    public static JSONObject fillValueSpecification(ValueSpecification vs,
                                                    JSONObject einfo) {
        return fillValueSpecification(vs, einfo, false);
    }

    //given value spec and value object, fill in stuff
    @SuppressWarnings("unchecked")
    public static JSONObject fillValueSpecification(ValueSpecification vs,
                                                    JSONObject einfo, boolean useLongForDouble) {
        if (vs == null) {
            return null;
        }
        JSONObject elementInfo = einfo;
        if (elementInfo == null) {
            elementInfo = new JSONObject();
        }
        elementInfo.put("valueExpression", null);
        // ValueSpecification expr = vs.getExpression();
        // if ( expr != null ) {
        // elementInfo.put( "valueExpression", expr.getID() );
        // }
        if (vs instanceof Duration) {
            elementInfo.put("type", "Duration");
        }
        else if (vs instanceof DurationInterval) {
            elementInfo.put("type", "DurationInterval");
            elementInfo.put("min", null);
            elementInfo.put("max", null);
            /*Duration maxD = ((DurationInterval) vs).getMax();
            if (maxD != null) {
                elementInfo.put("max", maxD.getID());
            Duration minD = ((DurationInterval) vs).getMin();
            if (minD != null)
                elementInfo.put("durationMin", minD.getID());*/
        }
        else if (vs instanceof ElementValue) {
            elementInfo.put("type", "ElementValue");
            Element elem = ((ElementValue) vs).getElement();
            elementInfo.put("elementId", ((elem != null) ? ExportUtility.getElementID(elem) : null));
        }
        else if (vs instanceof Expression) {
            elementInfo.put("type", "Expression");
            // if (((Expression) vs).getSymbol() != null) {
            // elementInfo.put("symbol", ((Expression) vs).getSymbol());
            // }
            List<ValueSpecification> vsl = ((Expression) vs).getOperand();
            if (vsl != null && vsl.size() > 0) {
                JSONArray operand = new JSONArray();
                for (ValueSpecification vs2 : vsl) {
                    JSONObject res = new JSONObject();
                    fillValueSpecification(vs2, res, useLongForDouble);
                    operand.add(res);
                }
                elementInfo.put("operand", operand);
            }
        }
        else if (vs instanceof InstanceValue) {
            elementInfo.put("type", "InstanceValue");
            InstanceValue iv = (InstanceValue) vs;
            InstanceSpecification i = iv.getInstance();
            elementInfo.put("instance", ((i != null) ? ExportUtility.getElementID(i) : null));
        }
        else if (vs instanceof LiteralSpecification) {
            if (vs instanceof LiteralBoolean) {
                elementInfo.put("type", "LiteralBoolean");
                elementInfo.put("boolean", ((LiteralBoolean) vs).isValue());
            }
            else if (vs instanceof LiteralInteger) {
                elementInfo.put("type", "LiteralInteger");
                elementInfo.put("integer", new Long(((LiteralInteger) vs).getValue()));
            }
            else if (vs instanceof LiteralNull) {
                elementInfo.put("type", "LiteralNull");
            }
            else if (vs instanceof LiteralReal) {
                elementInfo.put("type", "LiteralReal");
                double real = ((LiteralReal) vs).getValue();
                elementInfo.put("double", real);
                if (real % 1 == 0 && useLongForDouble) {
                    try {
                        elementInfo.put("double", (long) real);
                    } catch (Exception ex) {

                    }
                }
            }
            else if (vs instanceof LiteralString) {
                elementInfo.put("type", "LiteralString");
                elementInfo.put("string", Utils.stripHtmlWrapper(((LiteralString) vs).getValue()));
            }
            else if (vs instanceof LiteralUnlimitedNatural) {
                elementInfo.put("type", "LiteralUnlimitedNatural");
                elementInfo.put("naturalValue", new Long(((LiteralUnlimitedNatural) vs).getValue()));
            }
        }
        else if (vs instanceof OpaqueExpression) {
            elementInfo.put("type", "OpaqueExpression");
            List<String> body = ((OpaqueExpression) vs).getBody();
            elementInfo.put("expressionBody", ((body != null) ? makeJsonArray(body) : new JSONArray()));
        }
        else if (vs instanceof StringExpression) {
            elementInfo.put("type", "StringExpression");
        }
        else if (vs instanceof TimeExpression) {
            elementInfo.put("type", "TimeExpression");
        }
        else if (vs instanceof TimeInterval) {
            elementInfo.put("type", "TimeInterval");
            elementInfo.put("min", null);
            elementInfo.put("max", null);
            /*
             * TimeExpression maxD = ((TimeInterval) vs).getMax(); if (maxD != null) elementInfo.put("timeIntervalMax", maxD.getID()); TimeExpression minD = ((TimeInterval) vs).getMin(); if (minD != null) elementInfo.put("timeIntervalMin",
			 * minD.getID());
			 */
        }
        return elementInfo;
    }

    @SuppressWarnings("unchecked")
    protected static <T extends MDObject> JSONArray makeJsonArrayOfIDs(Collection<T> collection) {
        JSONArray ids = new JSONArray();
        for (T t : collection) {
            if (t != null) {
                ids.add(t.getID());
            }
        }
        return ids;
    }

    @SuppressWarnings("unchecked")
    protected static <T> JSONArray makeJsonArray(Collection<T> collection) {
        JSONArray arr = new JSONArray();
        for (T t : collection) {
            if (t != null) {
                arr.add(t);
            }
        }
        return arr;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject fillElement(Element e, JSONObject eInfo) {
        JSONObject elementInfo = eInfo;
        if (elementInfo == null) {
            elementInfo = new JSONObject();
        }
        // JSONObject specialization = new JSONObject();
        // elementInfo.put("specialization", specialization);
        //  Stereotype commentS = Utils.getCommentStereotype();
        if (e instanceof Package) {
            fillPackage((Package) e, elementInfo);
        }
        else if (e instanceof Property || e instanceof Slot) {
            fillPropertySpecialization(e, elementInfo, true, true);
        }
        else if (e instanceof DirectedRelationship) {
            fillDirectedRelationshipSpecialization((DirectedRelationship) e, elementInfo);
        }
        else if (e instanceof Connector) {
            fillConnectorSpecialization((Connector) e, elementInfo);
        }
        else if (e instanceof Operation) {
            fillOperationSpecialization((Operation) e, elementInfo);
        }
        else if (e instanceof Constraint) {
            fillConstraintSpecialization((Constraint) e, elementInfo);
        }
        else if (e instanceof InstanceSpecification) {
            elementInfo.put("type", "InstanceSpecification");
            fillInstanceSpecificationSpecialization((InstanceSpecification) e, elementInfo);
            /*
			 * ValueSpecification spec = ((InstanceSpecification) e) .getSpecification(); if (spec != null) specialization.put("instanceSpecificationSpecification", spec.getID());
			 */
        }
        else if (e instanceof Parameter) {
            fillParameterSpecialization((Parameter) e, elementInfo);
        }
        else if (e instanceof Comment || StereotypesHelper.hasStereotypeOrDerived(e, Utils.getCommentStereotype())) {
            elementInfo.put("type", "Comment");
        }
        else if (e instanceof Association) {
            fillAssociationSpecialization((Association) e, elementInfo);

        }
        else if (e.getClass().getSimpleName().equals("ClassImpl")) {
            Stereotype viewpoint = Utils.getViewpointStereotype();
            Stereotype view = Utils.getViewStereotype();
            Stereotype doc = Utils.getProductStereotype();
            // Stereotype view = Utils.getViewStereotype();
            if (viewpoint != null && StereotypesHelper.hasStereotypeOrDerived(e, viewpoint)) {
                elementInfo.put("type", "Viewpoint");
            }
            else if (view != null && StereotypesHelper.hasStereotypeOrDerived(e, view)) {
                if (StereotypesHelper.hasStereotypeOrDerived(e, doc)) {
                    elementInfo.put("type", "Product");
                }
                else {
                    elementInfo.put("type", "View");
                }
                fillViewContent(e, elementInfo);
            }
            else {
                elementInfo.put("type", "Element");
            }

        }
        else {
            String typeName = "Untyped"; // default

            Class baseClass = StereotypesHelper.getBaseClass(e);
            if (baseClass != null) {
                typeName = baseClass.getName();
            }
            elementInfo.put("type", typeName);

            if (e instanceof ActivityParameterNode) {
                fillActivityParameterNode((ActivityParameterNode) e, elementInfo);
            }
            else if (e instanceof Event) {
                fillEvent((Event) e, elementInfo);
            }
            else if (e instanceof Transition) {
                fillTransition((Transition) e, elementInfo);
            }
            else if (e instanceof ActivityEdge) // ControlFlow ObjectFlow
            {
                fillActivityEdge((ActivityEdge) e, elementInfo);
            }
            else if (e instanceof OpaqueBehavior) // OpaqueBehavior, FunctionBehavior
            {
                fillOpaqueBehavior((OpaqueBehavior) e, elementInfo);
            }
            else if (e instanceof CallBehaviorAction) {
                fillCallBehaviorAction((CallBehaviorAction) e, elementInfo);
            }
            else if (e instanceof Trigger) {
                fillTrigger((Trigger) e, elementInfo);
            }
            else if (e instanceof State) {
                fillState((State) e, elementInfo);
            }
            else if (e instanceof Pseudostate) {
                fillPseudostate((Pseudostate) e, elementInfo);
            }
        }
        fillOwnedAttribute(e, elementInfo);
        fillName(e, elementInfo);
        fillDoc(e, elementInfo);
        fillOwner(e, elementInfo);
        fillMetatype(e, elementInfo);
        elementInfo.put(MDKConstants.SYSML_ID_KEY, getElementID(e));
        return elementInfo;
    }

    @SuppressWarnings("unchecked")
    public static void fillPseudostate(Pseudostate e, JSONObject elementInfo) {
        PseudostateKind s;
        elementInfo.put("kind", ((s = e.getKind()) == null) ? null : s.toString());
    }

    @SuppressWarnings("unchecked")
    public static void fillState(State e, JSONObject elementInfo) {
        Element s;
        if (!(e instanceof FinalState)) {
            elementInfo.put("doActivityId", ((s = e.getDoActivity()) == null) ? null : s.getID());
            elementInfo.put("entryId", ((s = e.getEntry()) == null) ? null : s.getID());
            elementInfo.put("exitId", ((s = e.getExit()) == null) ? null : s.getID());
        }
    }

    @SuppressWarnings("unchecked")
    public static void fillEvent(Event e, JSONObject elementInfo) {
        Element s;
        elementInfo.put("behaviorId", ((s = e.getBehavior()) == null) ? null : s.getID());
        if (e instanceof CallEvent) {
            elementInfo.put("operationId", ((s = ((CallEvent) e).getOperation()) == null) ? null : s.getID());
        }
    }

    @SuppressWarnings("unchecked")
    public static void fillTrigger(Trigger e, JSONObject elementInfo) {
        Element s;
        elementInfo.put("eventId", ((s = e.getEvent()) == null) ? null : s.getID());
    }

    @SuppressWarnings("unchecked")
    public static void fillTransition(Transition e, JSONObject elementInfo) {
        Element s;
        elementInfo.put("clientId", ((s = ModelHelper.getClientElement(e)) == null) ? null : s.getID());
        elementInfo.put("supplierId", ((s = ModelHelper.getSupplierElement(e)) == null) ? null : s.getID());
        elementInfo.put("effectId", ((s = e.getEffect()) == null) ? null : s.getID());
        if (e.hasTrigger()) {
            for (Trigger t : e.getTrigger()) {// only one is allow to define in MD
                elementInfo.put("triggerId", t.getID());
            }
        }
        else {
            elementInfo.put("triggerId", null);
        }
    }

    @SuppressWarnings("unchecked")
    public static void fillActivityParameterNode(ActivityParameterNode e, JSONObject elementInfo) {
        Parameter s;
        elementInfo.put("parameterId", ((s = e.getParameter()) == null) ? null : s.getID());
    }

    @SuppressWarnings("unchecked")
    public static void fillOpaqueBehavior(OpaqueBehavior e, JSONObject elementInfo) {
        elementInfo.put("body", makeJsonArray(e.getBody()));
        elementInfo.put("language", makeJsonArray(e.getLanguage()));
    }

    @SuppressWarnings("unchecked")
    public static void fillActivityEdge(ActivityEdge e, JSONObject elementInfo) {

        Element s;
        elementInfo.put("sourceId", ((s = e.getSource()) == null) ? null : s.getID());
        elementInfo.put("targetId", ((s = e.getTarget()) == null) ? null : s.getID());

        ValueSpecification guard = e.getGuard();
        if (guard == null) {
            elementInfo.put("guard", null);
        }
        else {
            JSONObject vs = new JSONObject();
            fillValueSpecification(guard, vs);
            elementInfo.put("guard", vs);
        }
    }

    @SuppressWarnings("unchecked")
    public static void fillCallBehaviorAction(CallBehaviorAction e, JSONObject elementInfo) {
        Element s;
        elementInfo.put("behaviorId", ((s = e.getBehavior()) == null) ? null : s.getID());
    }

    public static JSONObject fillViewContent(Element e, JSONObject elementInfo) {
        Stereotype doc = Utils.getProductStereotype();
        if (elementInfo == null) {
            elementInfo = new JSONObject();
        }
        if (StereotypesHelper.hasStereotypeOrDerived(e, doc)) {
            elementInfo.put("type", "Product");
        }
        else {
            elementInfo.put("type", "View");
        }
        Constraint c = Utils.getViewConstraint(e);
        if (c != null) {
            JSONObject cob = fillConstraintSpecialization(c, null);
            if (cob.containsKey("specification")) {
                elementInfo.put("contents", cob.get("specification"));
                elementInfo.put("contains", new JSONArray());
            }
        }
        // Moved from stereotype to memory to not need a lock; see ViewPresentationGenerator
        /*Object o = StereotypesHelper.getStereotypePropertyFirst(e, Utils.getViewClassStereotype(), "elements");
        if (o != null && o instanceof String) {
            try {
                JSONArray a = (JSONArray) JSONValue.parse((String) o);
                        elementInfo.put("allowedElements", new JSONArray());
                        elementInfo.put("displayedElements", a);
                    } catch(Exception ex){
                    }
                }
        else {
            elementInfo.put("displayedElements", new JSONArray());
        }*/
        return elementInfo;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject fillPropertySpecialization(Element e, JSONObject elementInfo, boolean value,
                                                        boolean ptype) {
        if (elementInfo == null) {
            elementInfo = new JSONObject();
        }
        if (e instanceof Property) {
            elementInfo.put("aggregation", ((Property) e).getAggregation().toString().toUpperCase());
            elementInfo.put("type", "Property");
            elementInfo.put("isDerived", ((Property) e).isDerived());
            elementInfo.put("isSlot", false);
            if (value) {
                ValueSpecification vs = ((Property) e).getDefaultValue();
                JSONArray singleElementSpecVsArray = new JSONArray();
                if (vs != null) {
                    // Create a new JSONObject and a new JSONArray. Fill in
                    // the values to the new JSONObject and then insert
                    // that JSONObject into the array (NOTE: there will
                    // be single element in this array). Finally, insert
                    // the array into the specialization element as the
                    // value of the "value" property.
                    //

                    JSONObject newElement = new JSONObject();
                    fillValueSpecification(vs, newElement);
                    singleElementSpecVsArray.add(newElement);
                }
                elementInfo.put("value", singleElementSpecVsArray);
            }
            // specialization.put("upper", fillValueSpecification(((Property)e).getUpperValue(), null));
            // specialization.put("lower", fillValueSpecification(((Property)e).getLowerValue(), null));
            if (ptype) {
                Type type = ((Property) e).getType();
                elementInfo.put("propertyTypeId", (type == null) ? null : type.getID());

            }
            elementInfo.put("multiplicityMin", (long) ((Property) e).getLower());
            elementInfo.put("multiplicityMax", (long) ((Property) e).getUpper());

            Collection<Property> cps = ((Property) e).getRedefinedProperty();
            JSONArray redefinedProperties = new JSONArray();
            for (Property cp : cps) {
                redefinedProperties.add(getElementID(cp));
            }
            elementInfo.put("redefinesId", redefinedProperties);

        }
        else { // if (e instanceof Slot) {
            elementInfo.put("type", "Property");
            elementInfo.put("isDerived", false);
            elementInfo.put("isSlot", true);

            // Retrieve a list of ValueSpecification objects.
            // Loop through these objects, creating a new JSONObject
            // for each value spec. Fill in the new JSONObject and
            // insert them into a new JSONArray.
            // Finally, once you've looped through all the value
            // specifications, insert the JSONArray into the
            // new specialization element.
            //
            if (value) {
                List<ValueSpecification> vsl = ((Slot) e).getValue();
                JSONArray specVsArray = new JSONArray();
                if (vsl != null && vsl.size() > 0) {
                    for (ValueSpecification vs : vsl) {
                        JSONObject newElement = new JSONObject();
                        fillValueSpecification(vs, newElement);
                        specVsArray.add(newElement);
                    }
                }
                elementInfo.put("value", specVsArray);
            }
            if (ptype) {
                Element type = ((Slot) e).getDefiningFeature();
                elementInfo.put("propertyTypeId", (type == null) ? null : type.getID());
            }
        }
        return elementInfo;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject fillInstanceSpecificationSpecialization(InstanceSpecification e, JSONObject
            elementInfo) {
        if (elementInfo == null) {
            elementInfo = new JSONObject();
        }
        if (e.getSpecification() != null) {
            elementInfo.put("instanceSpecificationSpecification", fillValueSpecification(e.getSpecification(), null));
        }
        JSONArray classifiers = new JSONArray();
        for (Classifier c : e.getClassifier()) {
            classifiers.add(c.getID());
        }
        elementInfo.put("classifierId", classifiers);
        elementInfo.put("type", "InstanceSpecification");
        return elementInfo;
    }

    public static JSONObject sanitizeJSON(JSONObject spec) {
        List<Object> remKeys = new ArrayList<Object>();
        for (Object key : spec.keySet()) {
            // delete empty JSONArray
            if (spec.get(key) instanceof JSONArray && ((JSONArray) spec.get(key)).isEmpty()) {
                remKeys.add(key);
            }
        }
        for (Object key : remKeys) {
            spec.remove(key);
        }
        return spec;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject fillAssociationSpecialization(Association e, JSONObject elementInfo) {
        if (elementInfo == null) {
            elementInfo = new JSONObject();
        }
        int i = 0;
        for (Property p : e.getMemberEnd()) {
            if (i == 0) {
                elementInfo.put("sourceId", p.getID());
                // specialization.put("sourceAggregation", p.getAggregation().toString().toUpperCase());
            }
            else {
                elementInfo.put("targetId", p.getID());
                // specialization.put("targetAggregation", p.getAggregation().toString().toUpperCase());
            }
            i++;
        }
        JSONArray owned = new JSONArray();
        for (Property p : e.getOwnedEnd()) {
            owned.add(p.getID());
        }
        elementInfo.put("ownedEnd", owned);
        elementInfo.put("type", "Association");
        return elementInfo;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject fillPackage(Package e, JSONObject elementInfo) {
        if (elementInfo == null) {
            elementInfo = new JSONObject();
        }
        elementInfo.put("type", "Package");
        elementInfo.put("isSite", Utils.isSiteChar(e));
        return elementInfo;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject fillConstraintSpecialization(Constraint e, JSONObject elementInfo) {
        if (elementInfo != null) {
            elementInfo.put("type", "Constraint");
            ValueSpecification vspec = e.getSpecification();
            if (vspec != null) {
                JSONObject cspec = new JSONObject();
                fillValueSpecification(vspec, cspec);
                elementInfo.put("specification", cspec);
            }
        }
        return elementInfo;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject fillConnectorSpecialization(Connector e, JSONObject elementInfo) {
        if (elementInfo == null) {
            elementInfo = new JSONObject();
        }
        elementInfo.put("type", "Connector");
        int i = 0;
        if (e.getEnd() == null) {
            return elementInfo;
        }
        for (ConnectorEnd end : e.getEnd()) {
            JSONArray propertyPath = new JSONArray();
            if (end.getRole() != null) {
                if (StereotypesHelper.hasStereotype(end, "NestedConnectorEnd")) {
                    List<Element> ps = StereotypesHelper.getStereotypePropertyValue(end, "NestedConnectorEnd", "propertyPath");
                    for (Element path : ps) {
                        if (path instanceof ElementValue) {
                            propertyPath.add(((ElementValue) path).getElement().getID());
                        }
                        else if (path instanceof Property) {
                            propertyPath.add(path.getID());
                        }
                    }
                }
                propertyPath.add(end.getRole().getID());
            }
            if (i == 0) {
                // specialization.put("sourceUpper", fillValueSpecification(end.getUpperValue(), null));
                // specialization.put("sourceLower", fillValueSpecification(end.getLowerValue(), null));
                elementInfo.put("sourcePathId", propertyPath);
            }
            else {
                // specialization.put("targetUpper", fillValueSpecification(end.getUpperValue(), null));
                // specialization.put("targetLower", fillValueSpecification(end.getLowerValue(), null));
                elementInfo.put("targetPathId", propertyPath);
            }
            i++;
        }
        Association type = e.getType();
        elementInfo.put("connectorTypeId", (type == null) ? null : type.getID());
        elementInfo.put("connectorKind", (e.getKind() == null) ? null : e.getKind().toString());
        return elementInfo;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject fillOperationSpecialization(Operation e, JSONObject elementInfo) {
        if (elementInfo == null) {
            elementInfo = new JSONObject();
        }
        elementInfo.put("type", "Operation");
        List<Parameter> vsl = e.getOwnedParameter();
        if (vsl != null && vsl.size() > 0) {
            elementInfo.put("parametersId", makeJsonArrayOfIDs(vsl));
        }
        else {
            elementInfo.put("parametersId", new JSONArray());
        }
        return elementInfo;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject fillParameterSpecialization(Parameter e, JSONObject elementInfo) {
        if (elementInfo == null) {
            elementInfo = new JSONObject();
        }
        elementInfo.put("type", "Parameter");

        ParameterDirectionKind dir = e.getDirection();
        elementInfo.put("direction", (dir == null) ? null : dir.toString());

        Type type = e.getType();
        elementInfo.put("parameterTypeId", (type == null) ? null : type.getID());

        // ValueSpecification defaultValue = p.getDefaultValue();
        // if (defaultValue != null) {
        // specialization.put("parameterDefaultValue",
        // defaultValue.getID());
        // }
        return elementInfo;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject fillDirectedRelationshipSpecialization(DirectedRelationship
                                                                            e, JSONObject elementInfo) {
        if (elementInfo == null) {
            elementInfo = new JSONObject();
        }
        if (e instanceof Dependency) {
            if (StereotypesHelper.hasStereotype(e, "characterizes")) {
                elementInfo.put("type", "Characterizes");
            }
            else if (StereotypesHelper.hasStereotypeOrDerived(e, DocGen3Profile.queriesStereotype)) {
                elementInfo.put("type", "Expose");
            }
            else {
                elementInfo.put("type", "Dependency");
            }
        }
        else if (e instanceof Generalization) {
            Stereotype conforms = Utils.getSysML14ConformsStereotype();
            if (conforms != null && StereotypesHelper.hasStereotypeOrDerived(e, conforms)) {
                elementInfo.put("type", "Conform");
            }
            else {
                elementInfo.put("type", "Generalization");
            }
        }
        else if (e instanceof ProtocolConformance) { // StateMachine
            elementInfo.put("type", "ProtocolConformance");
        }
        else {
            elementInfo.put("type", "DirectedRelationship");
        }
        Element client = ModelHelper.getClientElement(e);
        Element supplier = ModelHelper.getSupplierElement(e);
        // (client != null) //this shouldn't happen
        elementInfo.put("sourceId", (client == null) ? null : getElementID(client));
        // (supplier != null) //this shouldn't happen
        elementInfo.put("targetId", (supplier == null) ? null : getElementID(supplier));
        return elementInfo;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject fillName(Element e, JSONObject einfo) {
        JSONObject info = einfo;
        if (info == null) {
            info = new JSONObject();
            info.put(MDKConstants.SYSML_ID_KEY, getElementID(e));
        }

        info.put("name", (e instanceof NamedElement) ? ((NamedElement) e).getName() : "");
        return info;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject fillDoc(Element e, JSONObject einfo) {
        JSONObject info = einfo;
        if (info == null) {
            info = new JSONObject();
            info.put(MDKConstants.SYSML_ID_KEY, getElementID(e));
        }
        info.put("documentation", Utils.stripHtmlWrapper(ModelHelper.getComment(e)));
        return info;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject fillOwnedAttribute(Element e, JSONObject einfo) {
        JSONObject info = einfo;
        if (info == null) {
            info = new JSONObject();
            info.put(MDKConstants.SYSML_ID_KEY, getElementID(e));
        }

        JSONArray propIDs = new JSONArray();
        if (e instanceof Class) {
            for (Property prop : ((Class) e).getOwnedAttribute()) {
                propIDs.add(getElementID(prop));
            }
            info.put("ownedAttributeId", propIDs);
        }
        return info;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject fillOwner(Element e, JSONObject einfo) {
        JSONObject info = einfo;
        if (info == null) {
            info = new JSONObject();
            info.put(MDKConstants.SYSML_ID_KEY, getElementID(e));
        }

        info.put("ownerId", (e.getOwner() == null) ? null : getElementID(e.getOwner()));
        return info;
    }

    public static JSONObject fillId(Element e, JSONObject einfo) {
        JSONObject info = einfo;
        if (info == null) {
            info = new JSONObject();
        }
        info.put(MDKConstants.SYSML_ID_KEY, getElementID(e));
        return info;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject fillMetatype(Element e, JSONObject einfo) {
        JSONObject info = einfo;
        if (info == null) {
            info = new JSONObject();
            info.put(MDKConstants.SYSML_ID_KEY, getElementID(e));
        }
        info.put("isMetatype", false);
        if (e instanceof Stereotype) {
            info.put("isMetatype", true);
            JSONArray metatypes = new JSONArray();
            for (Class c : ((Stereotype) e).getSuperClass()) {
                if (c instanceof Stereotype) {
                    metatypes.add(c.getID());
                }
            }
            for (Class c : StereotypesHelper.getBaseClasses((Stereotype) e)) {
                metatypes.add(c.getID());
            }
            info.put("metatypesId", metatypes);
        }
        if (e instanceof Class) {
            try {
                java.lang.Class<?> c = StereotypesHelper.getClassOfMetaClass((Class) e);
                if (c != null) {
                    info.put("isMetatype", true);
                    info.put("metatypes", new JSONArray());
                }
            } catch (Exception ex) {
            }
        }
        List<Stereotype> stereotypes = StereotypesHelper.getStereotypes(e);
        JSONArray applied = new JSONArray();
        for (Stereotype s : stereotypes) {
            applied.add(s.getID());
        }
        Class baseClass = StereotypesHelper.getBaseClass(e);
        if (baseClass != null) {
            applied.add(baseClass.getID());
        }

        info.put("appliedMetatypesId", applied);
        return info;
    }

    // no one's using this, should consider removing it
    @Deprecated
    public static String getBaselineTag() {
        Element model = Application.getInstance().getProject().getModel();
        String tag = null;
        if (StereotypesHelper.hasStereotype(model, "ModelManagementSystem")) {
            tag = (String) StereotypesHelper.getStereotypePropertyFirst(model, "ModelManagementSystem", "baselineTag");
            if (tag == null || tag.equals("")) {
                baselineNotSet = true;
                // JOptionPane
                // .showMessageDialog(null,
                // "Your project root element doesn't have ModelManagementSystem baselineTag stereotype property set! Mount structure check will not be done!");
                return null;
            }
        }
        else {
            // JOptionPane
            // .showMessageDialog(null,
            // "Your project root element doesn't have ModelManagementSystem baselineTag stereotype property set! Mount structure check will not be done!");
            baselineNotSet = true;
            return null;
        }
        baselineNotSet = false;
        return tag;
    }

    public static Integer getAlfrescoProjectVersion(String projectId) {
        String baseUrl = getUrlWithWorkspace();
        String checkProjUrl = baseUrl + "/projects/" + projectId;
        return getAlfrescoProjectVersionWithUrl(checkProjUrl);
    }

    public static Integer getAlfrescoProjectVersion(String projectId, String wsId) {
        String baseUrl = getUrl(Application.getInstance().getProject());//WithWorkspace();
        baseUrl += "/workspaces/" + wsId;
        String checkProjUrl = baseUrl + "/projects/" + projectId;
        return getAlfrescoProjectVersionWithUrl(checkProjUrl);
    }

    private static Integer getAlfrescoProjectVersionWithUrl(String url) {
        String json = null;
        try {
            json = get(url, false);
        } catch (ServerException ex) {

        }
        if (json == null) {
            return null; // ??
        }
        JSONObject result = (JSONObject) JSONValue.parse(json);
        if (result.containsKey("elements")) {
            JSONArray elements = (JSONArray) result.get("elements");
            if (!elements.isEmpty() && ((JSONObject) elements.get(0)).containsKey("specialization")) {
//                JSONObject spec = (JSONObject) ((JSONObject) elements.get(0)).get("specialization");
                JSONObject elementJson = (JSONObject) elements.get(0);
                if (elementJson.containsKey("projectVersion") && elementJson.get("projectVersion") != null) {
                    return Integer.valueOf(elementJson.get("projectVersion").toString());
                }
            }
        }
        return null;
    }

   @Deprecated
    public static void sendProjectVersion(Element e) {
		/*
		 * Project prj = Application.getInstance().getProject(); if (ProjectUtilities.isElementInAttachedProject(e)) { IProject module = ProjectUtilities.getAttachedProject(e); if (ProjectUtilities.isFromTeamworkServer(module)) {
		 * IVersionDescriptor vd = ProjectUtilities.getVersion(module); ProjectVersion pv = new ProjectVersion(vd); Integer teamwork = pv.getNumber(); sendProjectVersion(module.getProjectID(), teamwork); } } else { if
		 * (ProjectUtilities.isFromTeamworkServer(prj.getPrimaryProject())) { sendProjectVersion(prj.getPrimaryProject().getProjectID(), TeamworkService.getInstance(prj).getVersion(prj) .getNumber()); } }
		 */

    }

   @Deprecated
   public static boolean okToExport(Element e) {
		/*
		 * if (mountedVersions == null) mountedVersions = new HashMap<String, Integer>(); Project prj = Application.getInstance().getProject(); if (ProjectUtilities.isElementInAttachedProject(e)) { IAttachedProject module =
		 * ProjectUtilities.getAttachedProject(e); if (ProjectUtilities.isFromTeamworkServer(module)) { IVersionDescriptor vd = ProjectUtilities.getVersion(module); ProjectVersion pv = new ProjectVersion(vd); Integer teamwork =
		 * pv.getNumber(); // Integer teamwork = // TeamworkService.getInstance(prj).getVersion(modulePrj).getNumber(); Integer mms = getAlfrescoProjectVersion(module.getProjectID()); if (teamwork == mms || mms == null || teamwork >= mms)
		 * return true; Boolean con = Utils .getUserYesNoAnswer("The element is in project " + module.getName() + " (" + teamwork + ") that is an older version of what's on the server (" + mms + "), do you want to continue export?"); if
		 * (con == null || !con) return false; } return true; } else { if (ProjectUtilities.isFromTeamworkServer(prj.getPrimaryProject())) { Integer teamwork = TeamworkService.getInstance(prj) .getVersion(prj).getNumber(); Integer mms =
		 * getAlfrescoProjectVersion(prj.getPrimaryProject() .getProjectID()); if (teamwork == mms || mms == null || teamwork >= mms) return true; Boolean con = Utils .getUserYesNoAnswer("The element is in project " + prj.getName() + " (" +
		 * teamwork + ") that is an older version of what's on the server (" + mms + "), do you want to continue export?"); if (con == null || !con) return false; } return true; }
		 */
        return true;
    }

   @Deprecated
   public static boolean okToExport(Set<Element> set) {
		/*
		 * Project prj = Application.getInstance().getProject(); mountedVersions = new HashMap<String, Integer>(); Map<String, String> projectNames = new HashMap<String, String>(); if
		 * (ProjectUtilities.isFromTeamworkServer(prj.getPrimaryProject())) { mountedVersions.put(prj.getPrimaryProject().getProjectID(), TeamworkService.getInstance(prj).getVersion(prj) .getNumber());
		 * projectNames.put(prj.getPrimaryProject().getProjectID(), prj.getName()); } for (Element e : set) { if (ProjectUtilities.isElementInAttachedProject(e)) { IProject module = ProjectUtilities.getAttachedProject(e); if
		 * (ProjectUtilities.isFromTeamworkServer(module) && !mountedVersions.containsKey(module.getProjectID())) { IVersionDescriptor vd = ProjectUtilities.getVersion(module); ProjectVersion pv = new ProjectVersion(vd); Integer teamwork =
		 * pv.getNumber(); mountedVersions.put(module.getProjectID(), teamwork); projectNames.put(module.getProjectID(), module.getName()); } } } for (String prjId : mountedVersions.keySet()) { Integer serverVersion =
		 * getAlfrescoProjectVersion(prjId); if (serverVersion != null && serverVersion > mountedVersions.get(prjId)) { Boolean con = Utils.getUserYesNoAnswer("Your project " + projectNames.get(prjId) + " is an older project version (" +
		 * mountedVersions.get(prjId) + ") than what's on the server (" + serverVersion + ") , do you want to continue?"); if (con == null || !con) return false; } }
		 */
        return true;
    }

    @Deprecated
    public static boolean okToExport() {
		/*
		 * mountedVersions = new HashMap<String, Integer>(); Map<String, String> projectNames = new HashMap<String, String>(); Project prj = Application.getInstance().getProject(); if
		 * (ProjectUtilities.isFromTeamworkServer(prj.getPrimaryProject())) { mountedVersions.put(prj.getPrimaryProject().getProjectID(), TeamworkService.getInstance(prj).getVersion(prj) .getNumber());
		 * projectNames.put(prj.getPrimaryProject().getProjectID(), prj.getName()); } for (IAttachedProject p : ProjectUtilities.getAllAttachedProjects(prj)) { if (ProjectUtilities.isFromTeamworkServer(p)) { IVersionDescriptor vd =
		 * ProjectUtilities.getVersion(p); ProjectVersion pv = new ProjectVersion(vd); Integer teamwork = pv.getNumber(); mountedVersions.put(p.getProjectID(), teamwork); projectNames.put(p.getProjectID(), p.getName()); } } for (String
		 * prjId : mountedVersions.keySet()) { Integer serverVersion = getAlfrescoProjectVersion(prjId); if (serverVersion != null && serverVersion > mountedVersions.get(prjId)) { Boolean con = Utils.getUserYesNoAnswer("Your project " +
		 * projectNames.get(prjId) + " is an older project version (" + mountedVersions.get(prjId) + ") than what's on the server (" + serverVersion + ") , do you want to continue?"); if (con == null || !con) return false; } }
		 */
        return true;
    }

    public static Map<String, Integer> getMountedVersions() {
        return mountedVersions;
    }

    public static void sendProjectVersion() {
        String baseurl = getUrlWithWorkspaceAndSite();
        if (baseurl == null) {
            return;
        }
        JSONObject result = ExportUtility.getProjectJson();
        JSONObject tosend = new JSONObject();
        JSONArray array = new JSONArray();
        tosend.put("elements", array);
        tosend.put("source", "magicdraw");
        tosend.put("mmsVersion", MDKPlugin.VERSION);
        array.add(result);
        String url = baseurl + "/projects";
        if (!url.contains("master")) {
            url += "?createSite=true";
        }
        Utils.guilog("[INFO] Request is added to queue.");
        OutputQueue.getInstance().offer(new Request(url, tosend.toJSONString(), "Project Version"));
        // send(url, tosend.toJSONString(), null, false);
    }

    public static void sendProjectVersion(String projId, Integer version) {
        String baseurl = getUrlWithWorkspaceAndSite();
        if (baseurl == null) {
            return;
        }
        JSONObject result = ExportUtility.getProjectJSON(null, projId, version);
        JSONObject tosend = new JSONObject();
        JSONArray array = new JSONArray();
        tosend.put("elements", array);
        tosend.put("source", "magicdraw");
        tosend.put("mmsVersion", MDKPlugin.VERSION);
        array.add(result);
        String url = baseurl + "/projects";
        if (!url.contains("master")) {
            url += "?createSite=true";
        }
        Utils.guilog("[INFO] Request is added to queue.");
        OutputQueue.getInstance().offer(new Request(url, tosend.toJSONString(), "Project Version"));
        // send(url, tosend.toJSONString(), null, false);
    }

    public static String initializeBranchVersion(String taskId) {
        String baseUrl = ExportUtility.getUrl(Application.getInstance().getProject());
        String site = ExportUtility.getSite();
        String projUrl = baseUrl + "/workspaces/" + taskId + "/sites/" + site + "/projects?createSite=true";
        JSONObject moduleJson = ExportUtility.getProjectJSON(Application.getInstance().getProject().getName(), Application.getInstance().getProject().getPrimaryProject().getProjectID(), 0);
        JSONObject tosend = new JSONObject();
        JSONArray array = new JSONArray();
        tosend.put("elements", array);
        tosend.put("source", "magicdraw");
        tosend.put("mmsVersion", MDKPlugin.VERSION);
        array.add(moduleJson);
        // OutputQueue.getInstance().offer(new Request(projUrl, tosend.toJSONString()));
        return ExportUtility.send(projUrl, tosend.toJSONString()/* , null */, false, false);
    }

    public static void initializeDurableQueue(String taskId) {
        String projectId = Application.getInstance().getProject().getPrimaryProject().getProjectID();
        Connection connection = null;
        Session session = null;
        MessageConsumer consumer = null;
        try {
            JMSUtils.JMSInfo jmsInfo = null;
            try {
                jmsInfo = JMSUtils.getJMSInfo(Application.getInstance().getProject());
            } catch (ServerException e) {
                e.printStackTrace();
            }
            String url = jmsInfo != null ? jmsInfo.getUrl() : null;
            if (url == null) {
                return;
            }
            ConnectionFactory connectionFactory = JMSUtils.createConnectionFactory(jmsInfo);
            connection = connectionFactory.createConnection();
            String subscriberId = projectId + "/" + taskId;
            connection.setClientID(subscriberId);
            // connection.setExceptionListener(this);
            session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
            String messageSelector = JMSUtils.constructSelectorString(projectId, taskId);
            Topic topic = session.createTopic("master");
            consumer = session.createDurableSubscriber(topic, subscriberId, messageSelector, true);
            connection.start();
        } catch (JMSException e1) {
            e1.printStackTrace();
        } finally {
            try {
                if (consumer != null) {
                    consumer.close();
                }
                if (session != null) {
                    session.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }

    public static void sendProjectVersions() {
        for (String projid : mountedVersions.keySet()) {
            sendProjectVersion(projid, mountedVersions.get(projid));
        }
    }

    public static String unescapeHtml(String s) {
        return StringEscapeUtils.unescapeHtml(s);
    }

    // whether something should be sent to alfresco - ignore specific slots, documentation comment elements, value specs, empty instance specs (most likely from just stereotype application)
    public static boolean shouldAdd(Element e) {
        if (e == null || e instanceof ValueSpecification || e instanceof Extension
                || e instanceof ProfileApplication) {
            return false;
        }
        if (e instanceof Comment
                && ExportUtility.isElementDocumentation((Comment) e)) {
            return false;
        }
        if (e instanceof InstanceSpecification && !(e instanceof EnumerationLiteral)) {
            boolean shouldIgnore = true;
            for (Classifier c : ((InstanceSpecification) e).getClassifier()) {
                if (!(c instanceof Stereotype)) {
                    return true;
                }
                if (!IGNORE_INSTANCE_CLASSIFIERS.contains(c.getID())) {
                    shouldIgnore = false;
                }
            }
            return !shouldIgnore && !e.getOwnedElement().isEmpty();
            /*if (((InstanceSpecification)e).getClassifier().size() == 1 &&
                    IGNORE_INSTANCE_CLASSIFIERS.contains(((InstanceSpecification)e).getClassifier().get(0).getID()))
                return false;*/
        }
        if (e instanceof ConnectorEnd) {
            return false;
        }
        if (e instanceof Slot && ((Slot) e).getDefiningFeature() != null
                && ExportUtility.IGNORE_SLOT_FEATURES.contains(((Slot) e)
                .getDefiningFeature().getID())) {
            return false;
        }
        if (e instanceof Slot && (e.getOwner() == null || ((Slot) e).getDefiningFeature() == null)) //model is messed up
        {
            return false;
        }
        if (e.getID().endsWith("sync") || (e.getOwner() != null && e.getOwner().getID().endsWith("sync"))) //delayed sync stuff
        {
            return false;
        }
        if (e instanceof Diagram) {
            return false;
        }
        return !(e instanceof Constraint && isViewConstraint((Constraint) e));
    }

    public static Element getViewFromConstraint(Constraint constraint) {
        Element maybeView = constraint.getOwner();
        Stereotype v = Utils.getViewStereotype();
        List<Element> constrained = constraint.getConstrainedElement();
        if (maybeView != null && v != null && StereotypesHelper.hasStereotypeOrDerived(maybeView, v) && constrained.size() == 1 && constrained.get(0) == maybeView) {
            return maybeView;
        }
        return null;
    }

    public static boolean isViewConstraint(Constraint constraint) {
        return getViewFromConstraint(constraint) != null;
    }

    public static final Pattern HTML_WHITESPACE_END = Pattern.compile(
            "\\s*</p>", Pattern.DOTALL);
    public static final Pattern HTML_WHITESPACE_START = Pattern.compile(
            "<p>\\s*", Pattern.DOTALL);

    public static String cleanHtml(String s) {
        return Utils.stripHtmlWrapper(s).replace(" class=\"pwrapper\"", "")
                .replace("<br>", "").replace("</br>", "").replace("\n", "");
        // inter = HTML_WHITESPACE_END.matcher(inter).replaceAll("</p>");
        // return HTML_WHITESPACE_START.matcher(inter).replaceAll("<p>");
    }

    public static JSONObject getProjectJson() {
        Project prj = Application.getInstance().getProject();
        Integer ver = getProjectVersion(prj);
        return getProjectJSON(Application.getInstance().getProject().getName(), Application.getInstance().getProject().getPrimaryProject().getProjectID(), ver);
    }

    public static JSONObject getProjectJsonForProject(IProject prj) {
        return getProjectJSON(prj.getName(), prj.getProjectID(), null);
    }

    @SuppressWarnings("unchecked")
    public static JSONObject getProjectJSON(String name, String projId, Integer version) {
        JSONObject result = new JSONObject();
        if (name != null) {
            result.put("name", name);
        }
        result.put(MDKConstants.SYSML_ID_KEY, projId);
        result.put("type", "Project");
        if (version != null) {
            result.put("projectVersion", version.toString());
        }
        return result;
    }

    public static String getProjectId(Project proj) {
        return proj.getPrimaryProject().getProjectID();
    }

    public static Integer getProjectVersion(Project proj) {
        Integer ver = null;
        if (ProjectUtilities.isFromTeamworkServer(proj.getPrimaryProject())) {
            IVersionDescriptor iVersionDescriptor = TeamworkService.getInstance(proj).getVersion(proj);
            if (iVersionDescriptor instanceof ProjectVersion) {
                ver = ((ProjectVersion) iVersionDescriptor).getNumber();
            }
        }
        return ver;
    }

    public static String getTeamworkBranch(Project proj) {
        String branch = null;
        if (ProjectUtilities.isFromTeamworkServer(proj.getPrimaryProject())) {
            branch = ProjectDescriptorsFactory.getProjectBranchPath(ProjectDescriptorsFactory.createRemoteProjectDescriptor(proj).getURI());
        }
        return branch;
    }

    /**
     * Method to check if the currently logged in user has permissions to edit the specified site on
     * the specified server.
     *
     * @param url  The url of the mms server you are querying. Ex: "https://mms.myOrg.gov".
     *             Also accepts the url returned by the getUrl() function.
     * @param site Site name (sysmlid) of the site you are querying for
     * @return true if the site lists "editable":"true" for the logged in user, false otherwise
     * @throws ServerException
     */
    public static boolean hasSiteEditPermission(String url, String site) throws ServerException {
        boolean print = MDKOptionsGroup.getMDKOptions().isLogJson();

        //https://cae-ems.jpl.nasa.gov/alfresco/service/workspaces/master/sites
        if (url.endsWith("/alfresco/service")) {
            url += "/workspaces/master/sites";
        }
        else {
            url += "/alfresco/service/workspaces/master/sites";
        }

        checkAndResetTicket(url);
        url = addTicketToUrl(url);
        GetMethod gm = new GetMethod(url);
        try {
            HttpClient client = new HttpClient();
            if (print) {
                log.info("checkTicket: " + url);
            }
            int code = client.executeMethod(gm);
            String json = gm.getResponseBodyAsString();
            if (print) {
                log.info("sites response: " + code + " " + json);
            }
            if (code == 200) {
                JSONObject siteResponse;
                try {
                    siteResponse = (JSONObject) (new JSONParser()).parse(json);
                } catch (ParseException e) {
                    throw new ServerException(json, 500);
                }
                JSONArray returnedSiteList = (JSONArray) siteResponse.get("sites");
                for (Object returnedSite : returnedSiteList) {
                    JSONObject rs = (JSONObject) returnedSite;
                    if (rs.containsKey("editable") && rs.containsKey(MDKConstants.SYSML_ID_KEY) && rs.get(MDKConstants.SYSML_ID_KEY).equals(site)) {
                        return (boolean) rs.get("editable");
                    }
                }
            }
            else if (code == 401 || code == 403 || code == 404) {
                return false;
            }
            else {
                throw new ServerException(json, code);
            }
            return false;
        } catch (IOException ex) {
            //Utils.printException(ex);
            ex.printStackTrace();
            throw new ServerException("", 500);
        } finally {
            gm.releaseConnection();
        }
    }
}
