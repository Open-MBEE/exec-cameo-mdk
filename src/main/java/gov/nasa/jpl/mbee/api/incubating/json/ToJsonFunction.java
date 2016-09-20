package gov.nasa.jpl.mbee.api.incubating.json;

import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.ems.ImportException;
import gov.nasa.jpl.mbee.lib.Changelog;
import org.json.simple.JSONObject;;

/**
 * Created by igomes on 9/20/16.
 */
@FunctionalInterface
public interface ToJsonFunction {
    Changelog.Change<Element> apply(JSONObject jsonObject, Project project, Boolean strict) throws ImportException;
}
