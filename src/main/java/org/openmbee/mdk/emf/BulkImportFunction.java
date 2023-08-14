package org.openmbee.mdk.emf;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.task.ProgressStatus;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import org.openmbee.mdk.util.Changelog;
import org.openmbee.mdk.util.Pair;

import java.util.Collection;

/**
 * Created by igomes on 10/13/16.
 */
@FunctionalInterface
public interface BulkImportFunction {
    Changelog<String, Pair<Element, ObjectNode>> apply(Collection<ObjectNode> objectNodes, Project project, ProgressStatus progressStatus);
}
