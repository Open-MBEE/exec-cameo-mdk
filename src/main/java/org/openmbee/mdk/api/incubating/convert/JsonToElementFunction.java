package org.openmbee.mdk.api.incubating.convert;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import org.openmbee.mdk.json.ImportException;
import org.openmbee.mdk.util.Changelog;

/**
 * Created by igomes on 9/20/16.
 */
@FunctionalInterface
public interface JsonToElementFunction {
    Changelog.Change<Element> apply(ObjectNode objectNode, Project project, Boolean strict) throws ImportException, ReadOnlyElementException;
}
