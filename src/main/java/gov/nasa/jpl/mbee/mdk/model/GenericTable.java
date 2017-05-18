package gov.nasa.jpl.mbee.mdk.model;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.tomsawyer.magicdraw.action.TSMDPluginModelViewportProvider;
import com.tomsawyer.magicdraw.integrator.TSAssociationReader;
import com.tomsawyer.magicdraw.integrator.TSClassifierReader;
import com.tomsawyer.magicdraw.integrator.TSPropertyReader;
import com.tomsawyer.model.schema.TSSchema;
import gov.nasa.jpl.mbee.mdk.docgen.DocGenProfile;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.DBTable;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.DBText;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mbee.mdk.generator.DiagramTableTool;
import gov.nasa.jpl.mbee.mdk.util.GeneratorUtils;
import gov.nasa.jpl.mbee.mdk.util.Utils;
import com.tomsawyer.magicdraw.utilities.TSMagicDrawPluginAccessor;
import com.tomsawyer.model.*;
import com.tomsawyer.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class GenericTable extends Table {

    private List<String> headers;
    private boolean skipIfNoDoc;

    @SuppressWarnings("unchecked")
    public List<List<DocumentElement>> getHeaders(Diagram d, List<String> columnIds, DiagramTableTool dtt) {
        List<List<DocumentElement>> res = new ArrayList<List<DocumentElement>>();
        if (this.headers != null && !this.headers.isEmpty()) {
            List<DocumentElement> row = new ArrayList<DocumentElement>();
            for (String h : this.headers) {
                row.add(new DBText(h));
            }
            res.add(row);
        }
        else if (StereotypesHelper.hasStereotypeOrDerived(d, DocGenProfile.headersChoosable)) {
            List<DocumentElement> row = new ArrayList<DocumentElement>();
            for (String h : (List<String>) StereotypesHelper.getStereotypePropertyValue(d, DocGenProfile.headersChoosable, "headers")) {
                row.add(new DBText(h));
            }
            res.add(row);
        }
        else {
            List<DocumentElement> row = new ArrayList<DocumentElement>();
            int count = 0;
            for (String s : dtt.getColumnNames(d, columnIds)) {
                if (count == 0) {
                    count++;
                    continue;
                }
                row.add(new DBText(s));
            }
            res.add(row);
        }
        return res;

    }

    public List<List<DocumentElement>> getBody(Diagram d, List<Element> rowElements, List<String> columnIds,
                                               DiagramTableTool dtt, boolean forViewEditor) {
        List<List<DocumentElement>> res = new ArrayList<>();
        for (Element e : rowElements) {
            if (skipIfNoDoc && ModelHelper.getComment(e).trim().isEmpty()) {
                continue;
            }
            List<DocumentElement> row = new ArrayList<>();
            int count = 0;
            for (String cid : columnIds) {
                if (count == 0) {
                    count++;
                    continue;
                }
                row.add(Common.getTableEntryFromObject(getTableValues(dtt.getCellValue(d, e, cid))));
            }
            res.add(row);
        }
        return res;
    }

    @SuppressWarnings("rawtypes")
    public List<Object> getTableValues(Object o) {
        List<Object> res = new ArrayList<>();
        if (o instanceof Object[]) {
            Object[] a = (Object[]) o;
            for (int i = 0; i < a.length; i++) {
                res.addAll(getTableValues(a[i]));
            }
        }
        else if (o instanceof Collection) {
            for (Object oo : (Collection) o) {
                res.addAll(getTableValues(oo));
            }
        }
        else if (o != null) {
            res.add(o);
        }
        return res;
    }

    public void setSkipIfNoDoc(boolean b) {
        skipIfNoDoc = b;
    }

    public void setHeaders(List<String> h) {
        headers = h;
    }

    @Override
    public List<DocumentElement> visit(boolean forViewEditor, String outputDir) {
        List<DocumentElement> res = new ArrayList<DocumentElement>();
        DiagramTableTool dtt = new DiagramTableTool();
        Set<String> providers = TSMagicDrawPluginAccessor.getProviders();
        TSMDPluginModelViewportProvider provider =
                TSMagicDrawPluginAccessor.getProvider(providers.iterator().next());

        TSModel model = provider.getModel();
        TSSchema schema = provider.getDiagramDrawing().getViewDefinition().getSchema();

        TSClassifierReader reader = new TSClassifierReader();
        TSAssociationReader associationReader = new TSAssociationReader();
        TSPropertyReader propertyReader = new TSPropertyReader();
        if (getIgnore()) {
            return res;
        }
        int tableCount = 0;
        List<Object> targets = isSortElementsByName() ? Utils.sortByName(getTargets()) : getTargets();
        for (Object e : targets) {
            if(e instanceof BaseElement){
                BaseElement mdElement = (BaseElement) e;
                if (reader.isQualifyingElement(mdElement)) {
                    model.addElement(reader.readElement(mdElement, model, schema));
                }else if(associationReader.isQualifyingElement(mdElement)) {
                    model.addElement(associationReader.readElement(mdElement, model, schema));
                } else if(propertyReader.isQualifyingElement(mdElement)){
                    model.addElement(propertyReader.readElement(mdElement,model,schema));
                }else{
                    System.out.println(mdElement.getClassType() + " doesnt qualify for TomSawyer.");
                }
            }




            if (e instanceof Diagram) {
                Diagram diagram = (Diagram) e;
                if (Application.getInstance().getProject().getDiagram(diagram).getDiagramType().getType()
                        .equals("Generic Table")) {
                    DBTable t = new DBTable();
                    List<String> columnIds = dtt.getColumnIds(diagram);
                    t.setHeaders(getHeaders(diagram, columnIds, dtt));
                    List<Element> rowElements = dtt.getRowElements(diagram);
                    t.setBody(getBody(diagram, rowElements, columnIds, dtt, forViewEditor));
                    if (getTitles() != null && getTitles().size() > tableCount) {
                        t.setTitle(getTitlePrefix() + getTitles().get(tableCount) + getTitleSuffix());
                    }
                    else {
                        t.setTitle(getTitlePrefix() + (diagram).getName() + getTitleSuffix());
                    }
                    if (getCaptions() != null && getCaptions().size() > tableCount && isShowCaptions()) {
                        t.setCaption(getCaptions().get(tableCount));
                    }
                    else {
                        t.setCaption(ModelHelper.getComment(diagram));
                    }
                    t.setCols(columnIds.size() - 1);
                    res.add(t);
                    t.setStyle(getStyle());
                    tableCount++;
                }
            }
        }
        dtt.closeOpenedTables();
        return res;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initialize() {
        super.initialize();
        setHeaders((List<String>) GeneratorUtils.getListProperty(dgElement, DocGenProfile.headersChoosable,
                "headers", new ArrayList<String>()));
        setSkipIfNoDoc((Boolean) GeneratorUtils.getObjectProperty(dgElement, DocGenProfile.docSkippable,
                "skipIfNoDoc", false));
    }

}
