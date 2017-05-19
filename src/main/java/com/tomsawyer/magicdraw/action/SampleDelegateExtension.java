package com.tomsawyer.magicdraw.action;


import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.tomsawyer.integrator.TSIntegratorException;
import com.tomsawyer.magicdraw.action.TSMDPluginModelViewportProvider;
import com.tomsawyer.magicdraw.action.TSRenderDiagramDelegate;
import com.tomsawyer.magicdraw.integrator.*;
import com.tomsawyer.magicdraw.utilities.TSMagicDrawPluginAccessor;
import com.tomsawyer.model.TSModel;
import com.tomsawyer.model.schema.TSSchema;
import gov.nasa.jpl.mbee.mdk.util.Utils;

import javax.swing.*;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public class SampleDelegateExtension extends TSRenderDiagramDelegate
{
    private List<Element> elements;

    @Override
   public void doDataLoad() throws IOException, TSIntegratorException
   {
      Project activeProject =
         Application.getInstance().getProjectsManager().getActiveProject();

      //read in your data

      if(elements != null){
//          Set<String> providers = TSMagicDrawPluginAccessor.getProviders();
//
//          TSMDPluginModelViewportProvider provider =
//                  TSMagicDrawPluginAccessor.getProvider(providers.iterator().next());

          TSModel model = this.getModel();
          TSSchema schema = this.getDiagramDrawing().getViewDefinition().getSchema();

          TSClassifierReader classifierReader = new TSClassifierReader();
          TSAssociationReader associationReader = new TSAssociationReader();
          TSPropertyReader propertyReader = new TSPropertyReader();
//          TSActivityEdgeReader activityEdgeReader = new TSActivityEdgeReader();
//          TSActivityParameterReader activityParameterReader = new TSActivityParameterReader();
//          TSActivityPartitionReader activityPartitionReader = new TSActivityPartitionReader();
//          TSActivityReader activityReader = new TSActivityReader();
//          TSCommentReader commentReader = new TSCommentReader();
//          TSConnectorReader connectorReader = new TSConnectorReader();
          TSConstraintReader constraintReader = new TSConstraintReader();
//          TSContextReader contextReader = new TSContextReader();
//          TSDataTypeElementReader dataTypeElementReader = new TSDataTypeElementReader();
//          TSGeneralizationReader generalizationReader = new TSGeneralizationReader();
//          TSNoteViewCommentReader noteViewCommentReader = new TSNoteViewCommentReader();
//          TSSignalReader signalReader = new TSSignalReader();
//          TSStateReader stateReader = new TSStateReader();
          TSStereotypeReader stereotypeReader = new TSStereotypeReader();
//          TSTransitionReader transitionReader = new TSTransitionReader();
//          TSVerifyReader verifyReader */= new TSVerifyReader();

          List<TSMagicDrawElementReader> localObjectReaderList = this.getLocalObjectReaders(this.drawingViewName);
        //  localObjectReaderList.clear();
//          localObjectReaderList.add(classifierReader);
//          localObjectReaderList.add(associationReader);
//          localObjectReaderList.add(constraintReader);
          localObjectReaderList.add(propertyReader);
          localObjectReaderList.add(stereotypeReader);

          Set<Stereotype> stereotypes = StereotypesHelper.getAllAssignedStereotypes(elements);
          for (Stereotype stereotype : stereotypes) {
              this.dataModel.addElement(stereotypeReader.readElement(stereotype, this.dataModel, this.schema));
          }
          for (Element element : elements) {
              for (TSMagicDrawElementReader reader : localObjectReaderList) {
                  if (reader.isQualifyingElement(element)) {
                      this.dataModel.addElement(reader.readElement(element,this.dataModel, this.schema));

                      break;
                  }
              }
          }

//          int tableCount = 0;
//           for (Element e : elements) {
//               BaseElement mdElement = (BaseElement) e;
//               if (reader.isQualifyingElement(mdElement)) {
//                   model.addElement(reader.readElement(mdElement, model, schema));
//               } else if (associationReader.isQualifyingElement(mdElement)) {
//                   model.addElement(associationReader.readElement(mdElement, model, schema));
//               } else if (propertyReader.isQualifyingElement(mdElement)) {
//                   model.addElement(propertyReader.readElement(mdElement, model, schema));
//               } else {
//                   System.out.println(mdElement.getClassType() + " doesnt qualify for TomSawyer.");
//               }
//          }
      }

   }


    public void addObjectsToShow(List<Element> elements) {
        this.elements = elements;
    }
}