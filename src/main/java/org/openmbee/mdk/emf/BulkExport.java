package org.openmbee.mdk.emf;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import org.openmbee.mdk.api.incubating.convert.Converters;
import org.openmbee.mdk.util.Pair;

import java.util.stream.Stream;

/**
 * 
 * Original implementation lifted from ManualSyncRunner::collectClientElementsRecursively() by igomes
 *
 */
public class BulkExport {
	
    public static final int DEPTH_INFINITE = -1;
    public static final int DEPTH_NO_DESCENT = 0;
	
	public static Stream<Pair<Element, ObjectNode>> exportElementsRecursively(Project project, Element element, int depth) {
        ObjectNode jsonObject = Converters.getElementToJsonConverter().apply(element, project);
        if (jsonObject == null) {
            return Stream.empty();
        }
        Stream<Pair<Element, ObjectNode>> result = Stream.of(
            new Pair<>(element, jsonObject)
        );
        if (depth != DEPTH_NO_DESCENT) {
            int childDepth = depth - 1;
            result = Stream.concat(result, element.getOwnedElement().stream()
                        .flatMap(elementChild -> exportElementsRecursively(project, elementChild, childDepth))
            );
        }
        if (element.equals(project.getPrimaryModel())) {

            final Package primaryModel = project.getPrimaryModel();

            result = Stream.concat(result, project.getModels().stream()
                        .filter(attachedModel -> attachedModel != primaryModel)
                        .flatMap(attachedModel -> exportElementsRecursively(project, attachedModel, DEPTH_NO_DESCENT))
            );
        }
        return result;
    }

}
