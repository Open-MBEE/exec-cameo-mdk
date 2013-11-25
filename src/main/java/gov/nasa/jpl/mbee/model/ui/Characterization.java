package gov.nasa.jpl.mbee.model.ui;

import java.util.HashSet;
import java.util.Set;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

public class Characterization {
    private NamedElement            abstractComponent;
    private final NamedElement      abstractCharacterization;
    private final Set<NamedElement> concreteComponents = new HashSet<NamedElement>();

    public NamedElement getAbstractComponent() {
        return abstractComponent;
    }

    public void setAbstractCompnent(NamedElement abstractComponent) {
        this.abstractComponent = abstractComponent;
    }

    public NamedElement getAbstractCharacterization() {
        return abstractCharacterization;
    }

    public Characterization(NamedElement abstractComponent, NamedElement abstractCharacterization) {
        this.abstractComponent = abstractComponent;
        this.abstractCharacterization = abstractCharacterization;
    }

    public Set<NamedElement> getConcreteComponents() {
        return concreteComponents;
    }
}
