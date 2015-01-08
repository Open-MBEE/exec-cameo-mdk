package gov.nasa.jpl.mbee.ems.validation.actions;

import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.sync.AutoSyncProjectListener;
import gov.nasa.jpl.mgss.mbee.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mgss.mbee.docgen.validation.RuleViolationAction;

import java.awt.event.ActionEvent;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.project.ProjectDescriptor;
import com.nomagic.magicdraw.teamwork.application.TeamworkUtils;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

public class CreateTeamworkBranch extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {

    private static final long serialVersionUID = 1L;
    private String branchName;
    private String taskId;
    private Map<String, String> wsMapping;
    private Map<String, String> wsIdMapping;
    private Map<String, ProjectDescriptor> branchDescriptors;
    
    public CreateTeamworkBranch(String branchName, String taskId, Map<String, String> wsMapping, Map<String, String> wsIdMapping, Map<String, ProjectDescriptor> branchDescriptors) {
        super("CreateTeamworkBranch", "Create Teamwork Branch", null, null);
        this.branchName = branchName;
        this.taskId = taskId;
        this.wsMapping = wsMapping;
        this.wsIdMapping = wsIdMapping;
        this.branchDescriptors = branchDescriptors;
    }
    
    @Override
    public boolean canExecute(Collection<Annotation> arg0) {
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(Collection<Annotation> annos) {
        
    }

    @SuppressWarnings("unchecked")
    @Override
    public void actionPerformed(ActionEvent e) {
        String[] branches = branchName.split("/");
        String parentBranch = "master";
        for (int i = 1; i < branches.length - 1; i++) {
            parentBranch += "/" + branches[i];
        }
        ProjectDescriptor parentBranchPd = branchDescriptors.get(parentBranch);
        if (parentBranchPd == null) {
            Application.getInstance().getGUILog().log("The parent teamwork branch doesn't exist, create the parent branch first.");
            return;
        }
        ProjectDescriptor child = createBranch(branches[branches.length-1], parentBranchPd);
        if (child == null)
            return;
        branchDescriptors.put(branchName, child);
        Application.getInstance().getGUILog().log("Created Branch");
        //initialize jms queue
        
        Application.getInstance().getGUILog().log("Initializing Branch Sync");
        initializeBranchVersion();
        initializeDurableQueue();
    }
    
    private void initializeBranchVersion() {
        String baseUrl = ExportUtility.getUrl();
        String site = ExportUtility.getSite();
        String projUrl = baseUrl + "/workspaces/" + taskId + "/sites/" + site + "/projects?createSite=true";
        JSONObject moduleJson = ExportUtility.getProjectJSON(Application.getInstance().getProject().getName(), Application.getInstance().getProject().getPrimaryProject().getProjectID(), 0);
        JSONObject tosend = new JSONObject();
        JSONArray array = new JSONArray();
        tosend.put("elements", array);
        array.add(moduleJson);
        ExportUtility.send(projUrl, tosend.toJSONString(), null, false);
    }
    
    private void initializeDurableQueue() {
        String projectId = Application.getInstance().getProject().getPrimaryProject().getProjectID();
        Connection connection = null;
        Session session = null;
        MessageConsumer consumer = null;
        try {
            String url = AutoSyncProjectListener.getJMSUrl();
            if (url == null) {
                return;
            }
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
            connection = connectionFactory.createConnection();
            String subscriberId = projectId + "/" + taskId;
            connection.setClientID(subscriberId);
            // connection.setExceptionListener(this);
            session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
            String messageSelector = AutoSyncProjectListener.constructSelectorString(projectId, taskId);
            Topic topic = session.createTopic("master");
            consumer = session.createDurableSubscriber(topic, subscriberId, messageSelector, true);
            connection.start();
        } catch (JMSException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } finally {
            try {
                if (consumer != null)
                    consumer.close();
                if (session != null)
                    session.close();
                if (connection != null)
                    connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
        
    }
    
    private ProjectDescriptor createBranch(String name, ProjectDescriptor parentBranch) {
        //need to take into account time and version?
        try {
            Map<String, String> result = TeamworkUtils.branchProject(parentBranch, new HashSet<String>(), name, "Branched due to validation violation with alfresco task");
            Collection<String> branched = result.values();
            if (branched.size() > 0) {
                String branchId = branched.iterator().next();
                return TeamworkUtils.getRemoteProjectDescriptor(branchId);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }
}

