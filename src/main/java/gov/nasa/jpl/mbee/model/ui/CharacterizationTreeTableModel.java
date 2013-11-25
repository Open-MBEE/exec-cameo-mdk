package gov.nasa.jpl.mbee.model.ui;

import gov.nasa.jpl.mbee.model.MissionMapping;
import gov.nasa.jpl.mbee.tree.Node;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JButton;

import org.jdesktop.swingx.treetable.AbstractTreeTableModel;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

public class CharacterizationTreeTableModel extends AbstractTreeTableModel {

    private MissionMapping     mapping;
    private List<NamedElement> characterizations = new ArrayList<NamedElement>();

    public CharacterizationTreeTableModel(MissionMapping mapping) {
        this.mapping = mapping;
        Node<String, MissionComponent> rootNode = mapping.getRoot();
        root = rootNode;
        collectCharacterizations();
    }

    private void collectCharacterizations() {
        for (NamedElement e: mapping.getLibraryCharacterizations()) {
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
        if (column == 0)
            return "Name";
        else if (column == 1)
            return "Library Components";
        else if (column > 1 && column <= characterizations.size() + 1) {
            String characterizationName = characterizations.get(column - 2).getName();

            return "<html>" + characterizationName.replaceAll(" ", "<br/>") + "</html>";
        } else {
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

    @Override
    public Object getValueAt(Object node, int column) {
        Node<String, MissionComponent> missionNode = (Node<String, MissionComponent>)node;
        if (column == 0)
            return missionNode.getData().getName();
        else if (column == 1) {
            JButton button = new JButton("Edit");
            button.addActionListener(new ButtonAction(missionNode));
            return button;
        } else if (column > 1 && column <= characterizations.size() + 1) {
            return missionNode.getData().hasLibraryCharacterization(characterizations.get(column - 2));
        } else
            return "Unknown";
    }

    @Override
    public Class<?> getColumnClass(int column) {
        if (column == 0)
            return String.class;
        else if (column == 1)
            return JButton.class;
        else if (column <= characterizations.size() + 1)
            return Boolean.class;
        else
            return String.class;
    }

    @Override
    public Object getChild(Object node, int index) {
        @SuppressWarnings("unchecked")
        Node<String, MissionComponent> libraryNode = (Node<String, MissionComponent>)node;
        List<Node<String, MissionComponent>> childList = libraryNode.getChildrenAsList();
        return (childList != null) ? childList.get(index) : null;
    }

    @Override
    public int getChildCount(Object parent) {
        @SuppressWarnings("unchecked")
        Node<String, MissionComponent> libraryNode = (Node<String, MissionComponent>)parent;
        List<Node<String, MissionComponent>> childList = libraryNode.getChildrenAsList();
        return childList != null ? childList.size() : 0;
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        List<Node<String, MissionComponent>> children = ((Node<String, MissionComponent>)parent)
                .getChildrenAsList();
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i) == child)
                return i;
        }
        return -1;
    }

    @Override
    public boolean isCellEditable(Object node, int column) {
        MissionComponent com = ((Node<String, MissionComponent>)node).getData();
        if (column == 1)
            return false;
        if (column > 1) {
            NamedElement libchar = characterizations.get(column - 2);
            if (com.isPackage() || !com.isLibraryCharAllowed(libchar))
                return false;
            return true;
        }
        return true;
        // TODO should also check to see if this component inherits from the
        // library component
        // return (!((Node<String,MissionComponent>)node).getData().isPackage()
        // && column > 0);
    }

    @Override
    public void setValueAt(Object value, Object node, int column) {
        Node<String, MissionComponent> missionNode = (Node<String, MissionComponent>)node;
        MissionComponent component = missionNode.getData();
        if (column == 0) {
            component.setName((String)value);
            return;
        }
        if (column == 1) {
            return;
        }
        boolean checked = (Boolean)value;
        NamedElement libraryCharacterization = characterizations.get(column - 2);
        if (checked)
            component.createAndAddMissionCharForLibChar(libraryCharacterization);
        else
            component.removeCharacterizationForLibraryChar(libraryCharacterization);
    }
}
