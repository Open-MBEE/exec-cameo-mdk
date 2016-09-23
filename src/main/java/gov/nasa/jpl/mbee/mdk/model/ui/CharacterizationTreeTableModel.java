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
import gov.nasa.jpl.mbee.mdk.model.MissionMapping;
import gov.nasa.jpl.mbee.mdk.tree.Node;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CharacterizationTreeTableModel extends AbstractTreeTableModel {

    private MissionMapping mapping;
    private List<NamedElement> characterizations = new ArrayList<NamedElement>();

    public CharacterizationTreeTableModel(MissionMapping mapping) {
        this.mapping = mapping;
        Node<String, MissionComponent> rootNode = mapping.getRoot();
        root = rootNode;
        collectCharacterizations();
    }

    private void collectCharacterizations() {
        for (NamedElement e : mapping.getLibraryCharacterizations()) {
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
        else if (column == 1) {
            return "Library Components";
        }
        else if (column > 1 && column <= characterizations.size() + 1) {
            String characterizationName = characterizations.get(column - 2).getName();

            return "<html>" + characterizationName.replaceAll(" ", "<br/>") + "</html>";
        }
        else {
            return "Unknown";
        }
    }

    @Override
    public int getColumnCount() {
        return characterizations.size() + 2;
    }

    @Override
    public Object getRoot() {
        return root;
    }

    class ButtonAction implements ActionListener {
        Node<String, MissionComponent> missionNode;

        public ButtonAction(Node<String, MissionComponent> node) {
            missionNode = node;
        }

        @Override
        public void actionPerformed(ActionEvent paramActionEvent) {
            new LibraryComponentChooserUI(missionNode, mapping);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object getValueAt(Object node, int column) {
        Node<String, MissionComponent> missionNode = (Node<String, MissionComponent>) node;
        if (column == 0) {
            return missionNode.getData().getName();
        }
        else if (column == 1) {
            JButton button = new JButton("Edit");
            button.addActionListener(new ButtonAction(missionNode));
            return button;
        }
        else if (column > 1 && column <= characterizations.size() + 1) {
            return missionNode.getData().hasLibraryCharacterization(characterizations.get(column - 2));
        }
        else {
            return "Unknown";
        }
    }

    @Override
    public Class<?> getColumnClass(int column) {
        if (column == 0) {
            return String.class;
        }
        else if (column == 1) {
            return JButton.class;
        }
        else if (column <= characterizations.size() + 1) {
            return Boolean.class;
        }
        else {
            return String.class;
        }
    }

    @Override
    public Object getChild(Object node, int index) {
        @SuppressWarnings("unchecked")
        Node<String, MissionComponent> libraryNode = (Node<String, MissionComponent>) node;
        List<Node<String, MissionComponent>> childList = libraryNode.getChildrenAsList();
        return (childList != null) ? childList.get(index) : null;
    }

    @Override
    public int getChildCount(Object parent) {
        @SuppressWarnings("unchecked")
        Node<String, MissionComponent> libraryNode = (Node<String, MissionComponent>) parent;
        List<Node<String, MissionComponent>> childList = libraryNode.getChildrenAsList();
        return childList != null ? childList.size() : 0;
    }

    @SuppressWarnings("unchecked")
    @Override
    public int getIndexOfChild(Object parent, Object child) {
        List<Node<String, MissionComponent>> children = ((Node<String, MissionComponent>) parent)
                .getChildrenAsList();
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
        MissionComponent com = ((Node<String, MissionComponent>) node).getData();
        if (column == 1) {
            return false;
        }
        if (column > 1) {
            NamedElement libchar = characterizations.get(column - 2);
            return !(com.isPackage() || !com.isLibraryCharAllowed(libchar));
        }
        return true;
        // TODO should also check to see if this component inherits from the
        // library component
        // return (!((Node<String,MissionComponent>)node).getData().isPackage()
        // && column > 0);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setValueAt(Object value, Object node, int column) {
        Node<String, MissionComponent> missionNode = (Node<String, MissionComponent>) node;
        MissionComponent component = missionNode.getData();
        if (column == 0) {
            component.setName((String) value);
            return;
        }
        if (column == 1) {
            return;
        }
        boolean checked = (Boolean) value;
        NamedElement libraryCharacterization = characterizations.get(column - 2);
        if (checked) {
            component.createAndAddMissionCharForLibChar(libraryCharacterization);
        }
        else {
            component.removeCharacterizationForLibraryChar(libraryCharacterization);
        }
    }
}
