package gov.nasa.jpl.mgss.mbee.docgen.model.ui;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

public class MissionCharacterization {

	private NamedElement libraryChar;
	private String name;
	private NamedElement element;
	
	public MissionCharacterization(String name, NamedElement element) {
		this.name = name;
		this.element = element;
	}
	
	public MissionCharacterization(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public NamedElement getLibraryCharacterization() {
		return libraryChar;
	}
	
	public void setLibraryCharacterization(NamedElement e) {
		libraryChar = e;
	}
	
	public NamedElement getElement() {
		return element;
	}
}
