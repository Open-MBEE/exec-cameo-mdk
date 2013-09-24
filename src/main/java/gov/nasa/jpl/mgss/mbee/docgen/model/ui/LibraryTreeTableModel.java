package gov.nasa.jpl.mgss.mbee.docgen.model.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import gov.nasa.jpl.mbee.tree.Node;
import gov.nasa.jpl.mgss.mbee.docgen.model.LibraryMapping;

import org.jdesktop.swingx.treetable.AbstractTreeTableModel;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;

public class LibraryTreeTableModel extends AbstractTreeTableModel {

	private final LibraryMapping libraryMapping;
	private List<NamedElement>characterizations = new ArrayList<NamedElement>();

	public LibraryTreeTableModel(LibraryMapping libraryMapping) {
		this.libraryMapping = libraryMapping;
		Node<String, LibraryComponent> rootNode = libraryMapping.getRoot();
		root = rootNode;
		collectCharacterizations();
	}

	private void collectCharacterizations() {
		for (NamedElement e: libraryMapping.getCharacterizations()) {
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
		else
			return "<html>" + characterizations.get(column-1).getName().replaceAll(" ", "<br/>") + "</html>";
	}
	
	@Override
	public int getColumnCount() {
		return characterizations.size()+1;
	}

	public Object getRoot() {
		return root;
	};
	
	@Override
	public Object getValueAt(Object node, int column) {
		@SuppressWarnings("unchecked")
		Node<String, LibraryComponent> libraryNode = (Node<String, LibraryComponent>)node;
		if (column == 0)
			return libraryNode.getData().getName();
		else if (column > 0 && column <= characterizations.size())
			return libraryNode.getData().getCharacterizations().contains(characterizations.get(column-1));
		else
			return "Unknown";
	}

	@Override
	public Class<?> getColumnClass(int column) {
		if (column > 0 && column <= characterizations.size())
			return Boolean.class;
			
		return super.getColumnClass(column);
	}
	
	@Override
	public Object getChild(Object node, int index) {
		@SuppressWarnings("unchecked")
		Node<String, LibraryComponent> libraryNode = (Node<String, LibraryComponent>)node;
		List<Node<String, LibraryComponent>> childList = libraryNode.getChildrenAsList();
		return (childList != null) ? childList.get(index) : null;
	}

	@Override
	public int getChildCount(Object node) {
		@SuppressWarnings("unchecked")
		Node<String, LibraryComponent> libraryNode = (Node<String, LibraryComponent>)node;
		List<Node<String, LibraryComponent>> childList = libraryNode.getChildrenAsList();
		return childList != null ? childList.size() : 0;
	}

	@Override
	public int getIndexOfChild(Object parent, Object child) {
		@SuppressWarnings("unchecked")
		List<Node<String, LibraryComponent>> children = ((Node<String, LibraryComponent>)parent).getChildrenAsList();
		if (children == null)
			return -1;
		for (int i = 0; i < children.size(); i++) {
			if (children.get(i) == child)
				return i;
		}
		return -1;		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean isCellEditable(Object node, int column) {
		return (!((Node<String,LibraryComponent>)node).getData().isPackage() && column > 0);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setValueAt(Object value, Object node, int column) {
		boolean checked = (Boolean) value;
		LibraryComponent component = ((Node<String,LibraryComponent>)node).getData();
		NamedElement characterization = characterizations.get(column-1);
		if (checked)
			component.addCharacterization(characterization);
		else
			component.removeCharacterization(characterization);
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
