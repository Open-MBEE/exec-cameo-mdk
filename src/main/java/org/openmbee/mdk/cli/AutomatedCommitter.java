package org.openmbee.mdk.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.nomagic.magicdraw.commandline.CommandLineAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.project.ProjectDescriptor;
import com.nomagic.magicdraw.esi.EsiUtils;
import com.nomagic.magicdraw.teamwork2.ITeamworkService;
import com.nomagic.magicdraw.teamwork2.ServerLoginInfo;
import com.nomagic.magicdraw.teamwork2.locks.ILockProjectService;
import com.nomagic.magicdraw.teamwork2.locks.LockService;
import org.openmbee.mdk.api.MDKHelper;
import org.openmbee.mdk.http.ServerException;
import org.openmbee.mdk.mms.MMSUtils;
import org.openmbee.mdk.util.TaskRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

public class AutomatedCommitter implements CommandLineAction {
    @Parameter(names = {"--help", "-h"}, help = true)
    private boolean help;

    @Parameter(names = {"--twcHost"}, required = true)
    private String twcHost;
    @Parameter(names = {"--twcPort"}, required = true)
    private String twcPort;
    @Parameter(names = {"--twcUsername"}, required = true)
    private String twcUsername;
    @Parameter(names = {"--twcPassword"}, required = true)
    private String twcPassword;
    @Parameter(names = {"--twcProjectId"}, required = true)
    private String twcProjectId;
    @Parameter(names = {"--twcBranchId"})
    private String twcBranchId;

    @Parameter(names = {"--mmsUsername"}, required = true)
    private String mmsUsername;
    @Parameter(names = {"--mmsPassword"}, required = true)
    private String mmsPassword;

    @Parameter(names = {"--timeout", "-t"}, description = "Specifies the number of seconds after which the execution will be attempted to be stopped.")
    private int timeout;

    @Override
    public byte execute(String... args) {
        long startTime = System.currentTimeMillis();
        JCommander jcommander = JCommander.newBuilder().addObject(this).build();
        jcommander.setProgramName(this.getClass().getName());
        jcommander.setAcceptUnknownOptions(true);
        jcommander.parse(args);
        if (help) {
            jcommander.usage();
            return 0;
        }

        // Step 1: Login to TWC
        MDKHelper.setPopupsDisabled(true);
        ITeamworkService twcService = EsiUtils.getTeamworkService();
        twcService.login(new ServerLoginInfo(twcHost + ":" + twcPort, twcUsername, twcPassword, true), true);
        if (!twcService.isConnected()) {
            throw new IllegalArgumentException("Unable to log in to Teamwork Cloud with specified address and credentials.");
        }

        // Step 2: Open TWC project
        MDKHelper.setMMSLoginCredentials(mmsUsername, mmsPassword);
        ProjectDescriptor projectDescriptor;
        try {
            projectDescriptor = EsiUtils.getRemoteProjectDescriptors().stream().filter(descriptor -> descriptor.getURI().getPath().startsWith("/" + twcProjectId)).findAny().orElseThrow(() -> new IllegalArgumentException("Project with specified twcProjectId not found in Teamwork Cloud with specified address."));
        } catch (Exception e) {
            throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
        }
        if (twcBranchId != null) {
            projectDescriptor = EsiUtils.getDescriptorByBranchID(projectDescriptor, UUID.fromString(twcBranchId));
        }
        Application.getInstance().getProjectsManager().loadProject(projectDescriptor, true);
        Project project = Application.getInstance().getProject();
        if (project == null) {
            throw new IllegalArgumentException("Provided user credentials for TWC cannot open project with specified twcProjectId.");
        }

        // Step 3: Commit project to TWC
        ILockProjectService lockService = LockService.getLockService(project);
        if (lockService == null) {
            throw new IllegalStateException("Could not get lock service.");
        }
        EsiUtils.commitProject(project, "[AUTO-COMMIT] " + getClass().getSimpleName(), lockService.getLockedByMe(), lockService.getModulesLockedByMe(), true, Collections.emptyList());
        while (!Arrays.stream(TaskRunner.ThreadExecutionStrategy.values()).allMatch(strategy -> strategy.getExecutor().getQueue().isEmpty() && strategy.getExecutor().getActiveCount() == 0)) {
            try {
                if (timeout > 0 && System.currentTimeMillis() > startTime + timeout * 1000) {
                    throw new InterruptedException("Timeout reached.");
                }
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        Exception e = MMSUtils.getLastException().get();
        if (e != null) {
            if (e instanceof ServerException && ((ServerException) e).getCode() == 403) {
                throw new IllegalStateException("Provided user credentials for MMS cannot commit elements.");
            }
            else {
                throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
            }
        }
        return 0;
    }

    public static void main(String... args) {
        new AutomatedCommitter().execute(args);
    }
}
