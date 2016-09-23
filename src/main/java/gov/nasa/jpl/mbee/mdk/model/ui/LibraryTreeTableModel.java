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
package gov.nasa.jpl.mbee.mdk.model.ui;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import gov.nasa.jpl.mbee.mdk.model.LibraryMapping;
import gov.nasa.jpl.mbee.mdk.tree.Node;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LibraryTreeTableModel extends AbstractTreeTableModel {

    private final LibraryMapping libraryMapping;
    private List<NamedElement> characterizations = new ArrayList<NamedElement>();

    public LibraryTreeTableModel(LibraryMapping libraryMapping) {
        this.libraryMapping = libraryMapping;
        Node<String, LibraryComponent> rootNode = libraryMapping.getRoot();
        root = rootNode;
        collectCharacterizations();
    }

    private void collectCharacterizations() {
        for (NamedElement e : libraryMapping.getCharacterizations()) {
            characterizations.add(e);
        }
        Collections.sort(characterizations, new Comparator<NamedElement>() {
            @Override
            public int compare(NamedElement arg0, NamedElement arg1) {
                return arg0.getName().compareTo(arg1.getName());
            }
        });
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0) {
            return "Name";
        }
        else {
            return "<html>" + characterizations.get(column - 1).getName().replaceAll(" ", "<br/>")
                    + "</html>";
        }
    }

    @Override
    public int getColumnCount() {
        return characterizations.size() + 1;
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public Object getValueAt(Object node, int column) {
        @SuppressWarnings("unchecked")
        Node<String, LibraryComponent> libraryNode = (Node<String, LibraryComponent>) node;
        if (column == 0) {
            return libraryNode.getData().getName();
        }
        else if (column > 0 && column <= characterizations.size()) {
            return libraryNode.getData().getCharacterizations().contains(characterizations.get(column - 1));
        }
        else {
            return "Unknown";
        }
    }

    @Override
    public Class<?> getColumnClass(int column) {
        if (column > 0 && column <= characterizations.size()) {
            return Boolean.class;
        }

        return super.getColumnClass(column);
    }

    @Override
    public Object getChild(Object node, int index) {
        @SuppressWarnings("unchecked")
        Node<String, LibraryComponent> libraryNode = (Node<String, LibraryComponent>) node;
        List<Node<String, LibraryComponent>> childList = libraryNode.getChildrenAsList();
        return (childList != null) ? childList.get(index) : null;
    }

    @Override
    public int getChildCount(Object node) {
        @SuppressWarnings("unchecked")
        Node<String, LibraryComponent> libraryNode = (Node<String, LibraryComponent>) node;
        List<Node<String, LibraryComponent>> childList = libraryNode.getChildrenAsList();
        return childList != null ? childList.size() : 0;
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        @SuppressWarnings("unchecked")
        List<Node<String, LibraryComponent>> children = ((Node<String, LibraryComponent>) parent)
                .getChildrenAsList();
        if (children == null) {
            return -1;
        }
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i) == child) {
                return i;
            }
        }
        return -1;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean isCellEditable(Object node, int column) {
        return (!((Node<String, LibraryComponent>) node).getData().isPackage() && column > 0);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setValueAt(Object value, Object node, int column) {
        boolean checked = (Boolean) value;
        LibraryComponent component = ((Node<String, LibraryComponent>) node).getData();
        NamedElement characterization = characterizations.get(column - 1);
        if (checked) {
            component.addCharacterization(characterization);
        }
        else {
            component.removeCharacterization(characterization);
        }
    }

    /**
     * Need to override isLeaf so Packages are never shown as leaves.
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean isLeaf(Object node) {
        Node<String, LibraryComponent> lnode = (Node<String, LibraryComponent>) node;
        if (lnode.getData().getElement() instanceof Package) {
            return false;
        }
        return getChildCount(node) == 0;
    }

    public LibraryMapping getLibraryMapping() {
        return libraryMapping;
    }

}
