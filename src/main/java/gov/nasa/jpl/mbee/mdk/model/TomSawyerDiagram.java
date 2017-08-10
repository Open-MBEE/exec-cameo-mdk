package gov.nasa.jpl.mbee.mdk.model;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.EnumerationLiteral;
import gov.nasa.jpl.mbee.mdk.docgen.DocGenProfile;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.DBTomSawyerDiagram;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mbee.mdk.util.GeneratorUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static gov.nasa.jpl.mbee.mdk.model.TomSawyerDiagram.diagramType.*;
import static gov.nasa.jpl.mbee.mdk.model.TomSawyerDiagram.diagramType.Table;


/**
 * Created by johannes on 11/21/16.
 */
public class TomSawyerDiagram extends Query {
    private diagramType type;

    public void setType(diagramType type) {
        this.type = type;
    }

    public enum diagramType {
        Block_Definition_Diagram, Internal_Block_Diagram, State_Machine_Diagram, Activity_Diagram, Sequence_Diagram, Table
    }


    public TomSawyerDiagram() {
        super();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initialize() {
        Object enumliteral = GeneratorUtils.getStereotypePropertyFirst(dgElement, DocGenProfile.tomSawyerDiagramStereotype,
                "diagram_type", DocGenProfile.PROFILE_NAME, false);
        if (enumliteral instanceof String) {
            setType(diagramType.valueOf(enumliteral.toString()));
        }
        if (enumliteral instanceof EnumerationLiteral) {
            ((EnumerationLiteral) enumliteral).getEnumeration();

            switch (((EnumerationLiteral) enumliteral).getName()) {
                case "Block_Definition_Diagram":
                    setType(Block_Definition_Diagram);
                    break;
                case "Internal_Block_Diagram":
                    setType(Block_Definition_Diagram);
                    break;
                case "State_Machine_Diagram":
                    setType(State_Machine_Diagram);
                    break;
                case "Activity_Diagram":
                    setType(Activity_Diagram);
                    break;
                case "Sequence_Diagram":
                    setType(Sequence_Diagram);
                    break;
                case "Table":
                    setType(Table);
                    break;
                default:
            }
        }


    }

    public List<DocumentElement> visit(boolean forViewEditor, String outputDir) {
        List<Element> elements = new ArrayList<>();
        for (Object ob : this.getTargets()) {
            if (ob instanceof Element) {
                elements.add((Element) ob);
            }
        }
        DBTomSawyerDiagram dbts = new DBTomSawyerDiagram(elements);
        dbts.setType(type);
        return Collections.singletonList(dbts);

    }
}
