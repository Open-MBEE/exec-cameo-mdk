package gov.nasa.jpl.mbee.mdk.test.elements;

import com.nomagic.ci.persistence.IPrimaryProject;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;

/**
 * Created by ablack on 3/1/17.
 */
public class MDKProject extends com.nomagic.magicdraw.core.Project {

    private boolean remote;

    public MDKProject(boolean remote) {
        this.remote = remote;
    }

    {}

    @Override
    public IPrimaryProject getPrimaryProject() {
        return null;
    }

    @Override
    public Package getPrimaryModel() {
        return null;
    }

    @Override
    public BaseElement getElementByID(String id) {
        return null;
    }

    @Override
    public String getFileName() {
        return null;
    }

    @Override
    public com.nomagic.magicdraw.utils.ExtendedFile getFile() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean isRemote() {
        return this.remote;
    }

    @Override
    public String getID() {
        return null;
    }

}
