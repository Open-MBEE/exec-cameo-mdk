package com.tomsawyer.magicdraw.action;


import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.magicdraw.uml.DiagramType;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.Connector;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.ConnectorEnd;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.tomsawyer.integrator.TSIntegratorException;
import com.tomsawyer.magicdraw.TSMagicDrawUtilities;
import com.tomsawyer.magicdraw.integrator.*;
import com.tomsawyer.model.TSModel;
import com.tomsawyer.model.TSModelElement;
import com.tomsawyer.model.schema.TSSchema;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import org.jruby.util.NailMain;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DocGenDelegateExtension extends TSGenerateDiagramDelegate
{
    private List<Element> elements;
    private String diagramType;
    public DocGenDelegateExtension(Element element, String diagramKind) {
        super(element, diagramKind);
        diagramType = diagramKind;
    }

    public Element getIbdContextElement() {
        return ibdContextElement;
    }

    private Element ibdContextElement;

    @Override
    protected void initViews(String s) {
        String secondaryDiagramName = null;
        String controlsTreeName = null;
        String nodeTableName = null;
        String edgeTableName = null;
        if(diagramType.contains("Internal Block Diagram")) {
            this.drawingViewName = "Internal Block Diagram";
            nodeTableName = "Elements";
            edgeTableName = "Associations";
            controlsTreeName = "IBD Controls";
        } else if(diagramType.contains("State Machine")) {
            this.drawingViewName = "State Machine";
            nodeTableName = "Elements";
            edgeTableName = "Associations";
        } else if(diagramType.contains("Activity Diagram")) {
            this.drawingViewName = "Activity Diagram";
            nodeTableName = "Elements";
            edgeTableName = "Associations";
        } else if(diagramType.contains("Package Diagram")) {
            this.drawingViewName = "Package Diagram";
            nodeTableName = "Elements";
            edgeTableName = "Associations";
            controlsTreeName = "Package Diagram Controls";
        } else if(diagramType.contains("Requirement Diagram")) {
            this.drawingViewName = "Requirement Diagram";
            nodeTableName = "Elements";
            edgeTableName = "Associations";
            controlsTreeName = "REQ Controls";
        } else if(diagramType.contains("Parametric Diagram")) {
            this.drawingViewName = "Parametric Diagram";
            nodeTableName = "Elements";
            edgeTableName = "Associations";
            controlsTreeName = "Parametric Diagram Controls";
        } else if(diagramType.contains("Use Case Diagram")) {
            this.drawingViewName = "Use Case Diagram";
            nodeTableName = "Elements";
            edgeTableName = "Associations";
        } else {
            this.drawingViewName = "Block Definition Diagram";
            nodeTableName = "Elements";
            edgeTableName = "Associations";
            controlsTreeName = "BDD Controls";
        }

        this.createViewInstances((String)secondaryDiagramName, nodeTableName, edgeTableName, controlsTreeName);
    }

//    @Override
//    public void doFilterCalculation() {
//        Project activeProject = Application.getInstance().getProjectsManager().getActiveProject();
//        this.defineFilterValues(activeProject, this.diagram);
//    }

//    @Override
//    protected Collection<BaseElement> getLocalElements(Project activeProject) {
//        return super.getLocalElements(activeProject, this.diagram);
//    }


//    @Override
//    protected void loadFrameData(BaseElement element) {
//        TSMagicDrawElementReader frameReader = this.createFrameReader();
//        if(frameReader.isQualifyingElement(element)) {
//            this.dataModel.addElement(frameReader.readElement(element, this.dataModel, this.schema));
//        }
//
//    }

    @Override
   public void doDataLoad() throws IOException, TSIntegratorException
   {
      Project activeProject =
         Application.getInstance().getProjectsManager().getActiveProject();
      //read in your data
      if(elements != null){

          //TSModel model = this.getModel();
          //TSSchema schema = this.getDiagramDrawing().getViewDefinition().getSchema();

          TSPropertyReader propertyReader = new TSPropertyReader();
          TSContextReader contextReader = new TSContextReader();
          TSStereotypeReader stereotypeReader = new TSStereotypeReader();

          List<TSMagicDrawElementReader> viewReaderList = this.getLocalObjectReaders(this.drawingViewName);
          viewReaderList.add(propertyReader);
          viewReaderList.add(stereotypeReader);

          Set<Stereotype> stereotypes = StereotypesHelper.getAllAssignedStereotypes(elements);
          for (Stereotype stereotype : stereotypes) {
              this.dataModel.addElement(stereotypeReader.readElement(stereotype, this.dataModel, this.schema));
          }

          if(this.drawingViewName.equals(IBD_DRAWING_VIEW_NAME)) {
              if(!elements.isEmpty()) {
                  BaseElement contextElement = elements.get(0);
                  if(contextElement instanceof Element) {
                      ibdContextElement = (Element) contextElement;
                  }
                  HashSet<Element> requiredIBDElements = new HashSet<Element>();
                  requiredIBDElements.add((Element) contextElement);
                  // TODO test nesting in IBDs and add recursion to these methods.
                  if (contextElement instanceof Class) {
                      for (Property property : ((Class) contextElement).getOwnedAttribute()) {
                          requiredIBDElements.add(property);
                          if (property.getType() != null) {
                              requiredIBDElements.add(property.getType());
                          }
                      }
                      for (Connector connector : ((Class) contextElement).getOwnedConnector()) {
                          requiredIBDElements.add(connector);
                          for (ConnectorEnd connectorEnd : connector.getEnd()) {
                              requiredIBDElements.add(connectorEnd);
                              requiredIBDElements.add(connectorEnd.getRole());
                          }
                      }
                  }
                  for (Element element : requiredIBDElements) {
                      for (TSMagicDrawElementReader reader : viewReaderList) {
                          if (reader.isQualifyingElement(element)) {
                              this.dataModel.addElement(reader.readElement(element, this.dataModel, this.schema));
                              break;
                          }
                      }
                  }
                  this.dataModel.addElement(contextReader.readElement(contextElement, this.dataModel, this.schema));
              } else {
                  System.out.println("No Block exposed for IBD?");
              }
          }else {
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
//    public void postLoadDataIBDAction(){
//
//        List<TSModelElement> contextConnectors = this.dataModel.getModelIndex().getModelElementsByType("Context Connector");
//        List<TSModelElement> contextProperties = this.dataModel.getModelIndex().getModelElementsByType("Context Property");
//        //TSModelElement contextElement = this.dataModel.getModelIndex().getModelElementsByType("Context").get(0);
//        if(!contextConnectors.isEmpty()){
//            Object connectorUUID = contextConnectors.get(0).getAttribute("Connector");
//            Object metaClass = contextConnectors.get(0).getAttribute("x_Metaclass");
//           // System.out.println(connectorUUID + " " + metaClass);
//        }
//        if(!contextProperties.isEmpty()) {
//            Object propertyUUID = contextProperties.get(0).getAttribute("Property");
//            Object metaClass = contextProperties.get(0).getAttribute("p_Metaclass");
//           // System.out.println(propertyUUID + "  " + metaClass);
//        }
//    }

    public void addObjectsToShow(List<Element> elements) {
        this.elements = elements;
    }

    public Set<String> postLoadDataGetUUID() {
        // do this method to get all the uuids for view editor.
        Project project = Application.getInstance().getProjectsManager().getActiveProject();

        Set<String> viewElementUUIDS = new HashSet<String>();
        for (TSModelElement tsModelElement : this.dataModel.getModelElements()) {
            //String uuid = (String) tsModelElement.getAttribute("UUID");
            Element element = (Element) TSMagicDrawUtilities.getBaseElement(tsModelElement);
           // Element element = Converters.getIdToElementConverter().apply(uuid, project);
            if(element != null) {
                String uuuid = Converters.getElementToIdConverter().apply(element);
                if(uuuid != null && !uuuid.isEmpty() && !uuuid.equals("null")) {
                    viewElementUUIDS.add(uuuid);
                }
               }
// else{
//                if(uuid != null && uuid.isEmpty() && uuid != "null") {
//                    viewElementUUIDS.add(uuid);
//                }
//            }

        }
  //      boolean listElements = true;
 //       System.out.println("For " + this.drawingViewName + " " + viewElementUUIDS.size() + " elements.");
//        if(listElements){
//            for (String viewElementUUID : viewElementUUIDS) {
//                if(viewElementUUID != null) {
//                    BaseElement element = project.getElementByID(viewElementUUID);
//                    if (element != null) {
////                        if (element instanceof NamedElement) {
////                            System.out.println(((NamedElement) element).getQualifiedName());
////                        } else {
////                            System.out.println(element.getHumanType());
////                        }
//                    } else {
//                        for (TSModelElement tsModelElement : this.dataModel.getModelElements()) {
//                            String uuid = (String) tsModelElement.getAttribute("UUID");
//                            if(uuid != null) {
//                                if (uuid.equals(viewElementUUID)) {
//                            //        System.out.println(tsModelElement.getTypeName() + tsModelElement.getAttributeNames());
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
        return viewElementUUIDS;
    }
}