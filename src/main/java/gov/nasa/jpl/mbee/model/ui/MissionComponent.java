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

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import gov.nasa.jpl.mbee.lib.Utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MissionComponent {

    private String name;
    private NamedElement element;
    private Set<MissionCharacterization> chars;
    private Set<LibraryComponent> libs;

    private Set<LibraryComponent> addedLib;
    private Set<LibraryComponent> removedLib;
    private Set<MissionCharacterization> addedChar;
    private Set<MissionCharacterization> removedChar;

    private Map<NamedElement, MissionCharacterization> removedLibrary2MissionChar;
    private Map<NamedElement, MissionCharacterization> library2missionChar;

    public Set<LibraryComponent> getAddedLib() {
        return addedLib;
    }

    public Set<LibraryComponent> getRemovedLib() {
        return removedLib;
    }

    public Set<MissionCharacterization> getAddedChar() {
        return addedChar;
    }

    public Set<MissionCharacterization> getRemovedChar() {
        return removedChar;
    }

    public MissionComponent(String name, NamedElement element) {
        this.name = name;
        this.element = element;
        init();
    }

    public MissionComponent(String name) {
        this.name = name;
        init();
    }

    public boolean isPackage() {
        return element != null && element instanceof Package;
    }

    private void init() {
        chars = new HashSet<MissionCharacterization>();
        libs = new HashSet<LibraryComponent>();
        addedLib = new HashSet<LibraryComponent>();
        removedLib = new HashSet<LibraryComponent>();
        addedChar = new HashSet<MissionCharacterization>();
        removedChar = new HashSet<MissionCharacterization>();
        library2missionChar = new HashMap<NamedElement, MissionCharacterization>();
        removedLibrary2MissionChar = new HashMap<NamedElement, MissionCharacterization>();
    }

    public void addCharacterization(MissionCharacterization mc) {
        library2missionChar.put(mc.getLibraryCharacterization(), mc);
        chars.add(mc);
        addedChar.add(mc);
        removedChar.remove(mc);
    }

    public void removeCharacterization(MissionCharacterization mc) {
        library2missionChar.remove(mc.getLibraryCharacterization());
        removedLibrary2MissionChar.put(mc.getLibraryCharacterization(), mc);
        chars.remove(mc);
        removedChar.add(mc);
        addedChar.remove(mc);
    }

    public void removeCharacterizationForLibraryChar(NamedElement libraryChar) {
        if (library2missionChar.containsKey(libraryChar)) {
            removeCharacterization(library2missionChar.get(libraryChar));
        }
    }

    public void addLibraryComponent(LibraryComponent lc) {
        addedLib.add(lc);
        removedLib.remove(lc);
        libs.add(lc);
    }

    public void removeLibraryComponent(LibraryComponent lc) {
        Set<MissionCharacterization> willBeRemoved = new HashSet<MissionCharacterization>();

        libs.remove(lc);
        for (MissionCharacterization mc : new HashSet<MissionCharacterization>(this.chars)) {
            if (!isLibraryCharAllowed(mc.getLibraryCharacterization())) {
                willBeRemoved.add(mc);
            }
        }
        String list = "";
        for (MissionCharacterization mc : willBeRemoved) {
            list += mc.getName() + ",";
        }
        if (!list.equals("")) {
            if (!Utils.getUserYesNoAnswer("If you remove inheritance to component " + lc.getName()
                    + ", the following characterizations will also be removed from the model: " + list
                    + " is this ok?")) {
                libs.add(lc);
                return;
            }
        }
        addedLib.remove(lc);
        removedLib.add(lc);
        for (MissionCharacterization mc : willBeRemoved) {
            removeCharacterization(mc);
        }

    }

    public MissionCharacterization createAndAddMissionCharForLibChar(NamedElement libraryChar) {
        return createAndAddMissionCharForLibChar(libraryChar.getName(), libraryChar);
    }

    // default name should be same as library char name, until user can specify
    // name
    public MissionCharacterization createAndAddMissionCharForLibChar(String name, NamedElement libraryChar) {
        MissionCharacterization mc = null;
        if (removedLibrary2MissionChar.containsKey(libraryChar)) {
            mc = removedLibrary2MissionChar.get(libraryChar);
            removedLibrary2MissionChar.remove(libraryChar);
        }
        else {
            mc = new MissionCharacterization(name);
            mc.setLibraryCharacterization(libraryChar);
        }
        addCharacterization(mc);
        return mc;
    }

    public boolean hasLibraryComponent(LibraryComponent lc) {
        return libs.contains(lc);
    }

    public boolean hasMissionCharacterization(MissionCharacterization mc) {
        return chars.contains(mc);
    }

    public boolean hasLibraryCharacterization(NamedElement e) {
        return library2missionChar.containsKey(e);
    }

    public MissionCharacterization getMissionCharacterizationFor(NamedElement libraryChar) {
        return library2missionChar.get(libraryChar);
    }

    public boolean isLibraryCharAllowed(NamedElement libraryChar) {
        for (LibraryComponent lc : libs) {
            if (lc.getCharacterizations().contains(libraryChar)) {
                return true;
            }
        }
        return false;
    }

    public Set<MissionCharacterization> getMissionCharacterizations() {
        return chars;
    }

    public Set<LibraryComponent> getLibraryComponents() {
        return libs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NamedElement getElement() {
        return element;
    }

    public void updateLibrary2MissionCharMapping() {
        for (MissionCharacterization mc : chars) {
            library2missionChar.put(mc.getLibraryCharacterization(), mc);
        }
    }
}
