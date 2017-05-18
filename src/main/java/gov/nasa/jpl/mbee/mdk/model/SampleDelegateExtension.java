package gov.nasa.jpl.mbee.mdk.model;


import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
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
          Set<String> providers = TSMagicDrawPluginAccessor.getProviders();

          TSMDPluginModelViewportProvider provider =
                  TSMagicDrawPluginAccessor.getProvider(providers.iterator().next());

          TSModel model = provider.getModel();
          TSSchema schema = provider.getDiagramDrawing().getViewDefinition().getSchema();

          TSClassifierReader reader = new TSClassifierReader();
          TSAssociationReader associationReader = new TSAssociationReader();
          TSPropertyReader propertyReader = new TSPropertyReader();
          TSActivityEdgeReader activityEdgeReader = new TSActivityEdgeReader();
          TSActivityParameterReader activityParameterReader = new TSActivityParameterReader();
          TSActivityPartitionReader activityPartitionReader = new TSActivityPartitionReader();
          TSActivityReader activityReader = new TSActivityReader();
          TSCommentReader commentReader = new TSCommentReader();
          TSConnectorReader connectorReader = new TSConnectorReader();
          TSConstraintReader constraintReader = new TSConstraintReader();
          TSContextReader contextReader = new TSContextReader();
          TSDataTypeElementReader dataTypeElementReader = new TSDataTypeElementReader();
          TSGeneralizationReader generalizationReader = new TSGeneralizationReader();
          TSNoteViewCommentReader noteViewCommentReader = new TSNoteViewCommentReader();
          TSSignalReader signalReader = new TSSignalReader();
          TSStateReader stateReader = new TSStateReader();
          TSStereotypeReader stereotypeReader = new TSStereotypeReader();
          TSTransitionReader transitionReader = new TSTransitionReader();
          TSVerifyReader verifyReader = new TSVerifyReader();

          int tableCount = 0;
           for (Element e : elements) {
               BaseElement mdElement = (BaseElement) e;
               if (reader.isQualifyingElement(mdElement)) {
                   model.addElement(reader.readElement(mdElement, model, schema));
               } else if (associationReader.isQualifyingElement(mdElement)) {
                   model.addElement(associationReader.readElement(mdElement, model, schema));
               } else if (propertyReader.isQualifyingElement(mdElement)) {
                   model.addElement(propertyReader.readElement(mdElement, model, schema));
               } else {
                   System.out.println(mdElement.getClassType() + " doesnt qualify for TomSawyer.");
               }
          }
      }

   }


    public void addObjectsToShow(List<Element> elements) {
        this.elements = elements;
    }
}