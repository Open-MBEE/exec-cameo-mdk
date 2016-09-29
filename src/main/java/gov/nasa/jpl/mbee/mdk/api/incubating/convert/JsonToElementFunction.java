package gov.nasa.jpl.mbee.mdk.api.incubating.convert;

import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.ems.ImportException;
import gov.nasa.jpl.mbee.mdk.lib.Changelog;
import org.json.simple.JSONObject;;

/**
 * Created by igomes on 9/20/16.
 */
@FunctionalInterface
public interface JsonToElementFunction {
    Changelog.Change<Element> apply(JSONObject jsonObject, Project project, Boolean strict) throws ImportException;
}
