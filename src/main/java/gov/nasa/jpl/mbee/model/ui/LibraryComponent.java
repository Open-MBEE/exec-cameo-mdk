/*******************************************************************************
 * Copyright (c) <2013>, California Institute of Technology ("Caltech").  
 * U.S. Government sponsorship acknowledged.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are 
 * permitted provided that the following conditions are met:
 * 
 *  - Redistributions of source code must retain the above copyright notice, this list of 
 *    conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice, this list 
 *    of conditions and the following disclaimer in the documentation and/or other materials 
 *    provided with the distribution.
 *  - Neither the name of Caltech nor its operating division, the Jet Propulsion Laboratory, 
 *    nor the names of its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER  
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
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
