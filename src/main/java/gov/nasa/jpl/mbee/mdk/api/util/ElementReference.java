package gov.nasa.jpl.mbee.mdk.api.util;

import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;

import java.util.function.Function;

/**
 * Created by igomes on 8/23/16.
 */
public abstract class ElementReference<E extends Element> implements Function<Project, E> {
    public abstract Class<E> getElementClass();

    public abstract String getID();

    public abstract String getQualifiedName();

    @Override
    public E apply(Project project) {
        BaseElement element = Converters.getIdToElementConverter()
                .apply(getID(), project);
        E e = convertInstanceOfObject(element, getElementClass());
        if (e != null) {
            return e;
        }
        element = Converters.getIdToElementConverter()
                .apply(getID(), project);
        e = convertInstanceOfObject(element, getElementClass());
        return e;
    }

    private static <T> T convertInstanceOfObject(Object o, Class<T> clazz) {
        try {
            return clazz.cast(o);
        } catch (ClassCastException e) {
            return null;
        }
    }
}
