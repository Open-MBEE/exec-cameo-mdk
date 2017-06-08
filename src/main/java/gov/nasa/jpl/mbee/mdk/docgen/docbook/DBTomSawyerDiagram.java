package gov.nasa.jpl.mbee.mdk.docgen.docbook;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import java.util.List;
import java.util.Set;

import static gov.nasa.jpl.mbee.mdk.model.TomSawyerDiagram.diagramType;

public class DBTomSawyerDiagram extends DocumentElement {
    private Element context;

    public Set<String> getElementIDs() {
        return elements;
    }

    public void setElements(Set<String> elements) {
        this.elements = elements;
    }

    private Set<String> elements;
    private String caption;
    private boolean showForEditing = false;

    public shortDiagramType getShortType() {
        switch (type) {
            case Block_Definition_Diagram:
                return shortDiagramType.BDD;
            case Internal_Block_Diagram:
                return shortDiagramType.IBD;
            case State_Machine_Diagram:
                return shortDiagramType.SMD;
            case Activity_Diagram:
                return shortDiagramType.AD;
            case Sequence_Diagram:
                return shortDiagramType.SD;
            case Table:
                return shortDiagramType.Table;
        }
        return null;
    }

    public String getContext() {
        return context.getID();
    }

    public void setContext(Element context) {
        this.context = context;
    }

    public enum shortDiagramType {
        BDD, IBD, SMD, AD, SD, Table
    }

    private diagramType type;

//    public DBTomSawyerDiagram(List<Element> classifiers) {
//        elements = classifiers;
//    }

    @Override
    public void accept(IDBVisitor v) {

        v.visit(this);


    }

    public void setCaption(String cap) {
        caption = cap;
    }

    public String getCaption() {
        return caption;
    }

//    public List<Element> getClassifiers() {
//        return elements;
//
//    }


    public boolean isShowForEditing() {
        return showForEditing;
    }

    public void setShowForEditing(boolean showForEditing2) {
        this.showForEditing = showForEditing2;
    }


    public void setType(diagramType type) {
        this.type = type;
    }


}