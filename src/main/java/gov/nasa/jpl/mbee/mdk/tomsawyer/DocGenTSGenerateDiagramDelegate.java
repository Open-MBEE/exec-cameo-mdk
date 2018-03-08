package gov.nasa.jpl.mbee.mdk.tomsawyer;


import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.tomsawyer.integrator.TSIntegratorException;
import com.tomsawyer.magicdraw.TSMagicDrawUtilities;
import com.tomsawyer.magicdraw.action.TSGenerateDiagramDelegate;
import com.tomsawyer.magicdraw.integrator.TSMagicDrawElementReader;
import com.tomsawyer.magicdraw.integrator.TSPropertyReader;
import com.tomsawyer.magicdraw.integrator.TSStereotypeReader;
import com.tomsawyer.model.TSModelElement;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DocGenTSGenerateDiagramDelegate extends TSGenerateDiagramDelegate {
    private List<Element> elements;
    private String diagramType;

    public DocGenTSGenerateDiagramDelegate(Element element, String diagramKind) {
        super(element, diagramKind); //first element should be context element where applicable (ibd, par)
        diagramType = diagramKind;
    }

    public Element getContextElement() {
        return this.contextElement;
    }

    @Override
    protected void initViews(String s) {
        String controlsTreeName = null;
        String nodeTableName;
        String edgeTableName;
        if (diagramType.contains("Internal Block Diagram")) {
            this.drawingViewName = "Internal Block Diagram";
            nodeTableName = "Elements";
            edgeTableName = "Associations";
            controlsTreeName = "IBD Controls";
        }
        else if (diagramType.contains("State Machine")) {
            this.drawingViewName = "State Machine";
            nodeTableName = "Elements";
            edgeTableName = "Associations";
        }
        else if (diagramType.contains("Activity Diagram")) {
            this.drawingViewName = "Activity Diagram";
            nodeTableName = "Elements";
            edgeTableName = "Associations";
        }
        else if (diagramType.contains("Package Diagram")) {
            this.drawingViewName = "Package Diagram";
            nodeTableName = "Elements";
            edgeTableName = "Associations";
            controlsTreeName = "Package Diagram Controls";
        }
        else if (diagramType.contains("Requirement Diagram")) {
            this.drawingViewName = "Requirement Diagram";
            nodeTableName = "Elements";
            edgeTableName = "Associations";
            controlsTreeName = "REQ Controls";
        }
        else if (diagramType.contains("Parametric Diagram")) {
            this.drawingViewName = "Parametric Diagram";
            nodeTableName = "Elements";
            edgeTableName = "Associations";
            controlsTreeName = "Parametric Diagram Controls";
        }
        else if (diagramType.contains("Use Case Diagram")) {
            this.drawingViewName = "Use Case Diagram";
            nodeTableName = "Elements";
            edgeTableName = "Associations";
        }
        else {
            this.drawingViewName = "Block Definition Diagram";
            nodeTableName = "Elements";
            edgeTableName = "Associations";
            controlsTreeName = "BDD Controls";
        }

        this.createViewInstances(null, nodeTableName, edgeTableName, controlsTreeName);
    }

    @Override
    public void doDataLoad() throws IOException, TSIntegratorException {
        //if ibd or para, set this.contextElement (constructor should pass contextElement), super.doDataLoad()
        //if bdd, need to override getLocalElements
        //per josh, generate activity will come 8.2 or after
        if (elements != null && !elements.isEmpty()) {
            if (this.drawingViewName.equals(IBD_DRAWING_VIEW_NAME)) {
                // or parametric, or bdd if generating bdd
                //this.contextElement should have been set to the context block by the constructor
                super.doDataLoad();
            }
            else {
                TSPropertyReader propertyReader = new TSPropertyReader();
                TSStereotypeReader stereotypeReader = new TSStereotypeReader();

                List<TSMagicDrawElementReader> viewReaderList = this.getLocalObjectReaders(this.drawingViewName);
                viewReaderList.add(propertyReader);
                viewReaderList.add(stereotypeReader);

                Set<Stereotype> stereotypes = StereotypesHelper.getAllAssignedStereotypes(elements);
                for (Stereotype stereotype : stereotypes) {
                    this.dataModel.addElement(stereotypeReader.readElement(stereotype, this.dataModel, this.schema));
                }

                for (Element element : elements) {
                    for (TSMagicDrawElementReader reader : viewReaderList) {
                        if (reader.isQualifyingElement(element)) {
                            this.dataModel.addElement(reader.readElement(element, this.dataModel, this.schema));
                            break;
                        }
                    }
                }
            }
        }
    }

    public void addObjectsToShow(List<Element> elements) {
        this.elements = elements;
    }

    public Set<String> postLoadDataGetUUID() {
        // do this method to get all the uuids for view editor.
        Set<String> viewElementUUIDS = new HashSet<>();
        for (TSModelElement tsModelElement : this.dataModel.getModelElements()) {
            Element element = (Element) TSMagicDrawUtilities.getBaseElement(tsModelElement);
            if (element != null) {
                String uuid = Converters.getElementToIdConverter().apply(element);
                if (uuid != null && !uuid.isEmpty()) {
                    viewElementUUIDS.add(uuid);
                }
            }
        }
        return viewElementUUIDS;
    }
}