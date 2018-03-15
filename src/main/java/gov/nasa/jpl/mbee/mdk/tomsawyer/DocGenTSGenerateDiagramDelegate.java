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
import gov.nasa.jpl.mbee.mdk.model.TomSawyerDiagram;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class DocGenTSGenerateDiagramDelegate extends TSGenerateDiagramDelegate {
    private final Set<Element> elements = new LinkedHashSet<>();
    private final TomSawyerDiagram.DiagramType diagramType;

    public DocGenTSGenerateDiagramDelegate(Element element, TomSawyerDiagram.DiagramType diagramType) {
        super(element, diagramType.getName());
        this.diagramType = diagramType;
        this.drawingViewName = diagramType.getName();
    }

    public Element getContext() {
        return contextElement;
    }

    @Override
    protected void initViews(String s) {
        createViewInstances(null, "Elements", "Associations", "Controls");
    }

    @Override
    public void doDataLoad() throws IOException, TSIntegratorException {
        if (elements.isEmpty()) {
            return;
        }
        switch (diagramType) {
            case INTERNAL_BLOCK:
            case PARAMETRIC:
                super.doDataLoad();
                break;
            default:
                TSPropertyReader propertyReader = new TSPropertyReader();
                TSStereotypeReader stereotypeReader = new TSStereotypeReader();

                List<TSMagicDrawElementReader> viewReaderList = getLocalObjectReaders(diagramType.getName());
                viewReaderList.add(propertyReader);
                viewReaderList.add(stereotypeReader);

                Set<Stereotype> stereotypes = StereotypesHelper.getAllAssignedStereotypes(elements);
                for (Stereotype stereotype : stereotypes) {
                    dataModel.addElement(stereotypeReader.readElement(stereotype, dataModel, schema));
                }

                for (Element element : elements) {
                    for (TSMagicDrawElementReader reader : viewReaderList) {
                        if (reader.isQualifyingElement(element)) {
                            dataModel.addElement(reader.readElement(element, dataModel, schema));
                            break;
                        }
                    }
                }
                break;
        }
    }

    public Set<Element> getElements() {
        return elements;
    }

    public Set<Element> getDataModelElementIds() {
        return dataModel.getModelElements().stream().map(TSMagicDrawUtilities::getBaseElement).filter(Objects::nonNull).filter(baseElement -> baseElement instanceof Element).map(baseElement -> (Element) baseElement).collect(Collectors.toCollection(LinkedHashSet::new));
    }
}