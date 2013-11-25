package gov.nasa.jpl.mbee.model.ui;

import java.util.HashSet;
import java.util.Set;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;

public class LibraryComponent {

    private Set<NamedElement> characterizations;
    private NamedElement      element;
    private String            name;
    private Set<NamedElement> added;
    private Set<NamedElement> removed;
    private boolean           pseudoPackage = false;

    /**
     * Should only be used when making a placeholder for a root node for
     * LibraryComponent tree - so it's always a pseudoPackage and won't be
     * processed in any of the choosers.
     * 
     * @param name
     */
    public LibraryComponent(String name) {
        this.name = name;
        pseudoPackage = true;
        init();
    }

    public LibraryComponent(String name, NamedElement element) {
        this.element = element;
        this.name = name;
        init();
    }

    private void init() {
        characterizations = new HashSet<NamedElement>();
        added = new HashSet<NamedElement>();
        removed = new HashSet<NamedElement>();
    }

    public boolean isPackage() {
        if (element != null)
            return (element instanceof Package);
        return pseudoPackage;
    }

    public void addCharacterization(NamedElement chara) {
        characterizations.add(chara);
        added.add(chara);
        removed.remove(chara);
    }

    public void removeCharacterization(NamedElement chara) {
        characterizations.remove(chara);
        removed.add(chara);
        added.remove(chara);
    }

    public Set<NamedElement> getCharacterizations() {
        return characterizations;
    }

    public NamedElement getElement() {
        return element;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean hasCharacterization(NamedElement chara) {
        return characterizations.contains(chara);
    }

    public Set<NamedElement> getAdded() {
        return added;
    }

    public Set<NamedElement> getRemoved() {
        return removed;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
