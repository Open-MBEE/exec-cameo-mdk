package gov.nasa.jpl.mbee.mdk.viewedit;

import com.nomagic.magicdraw.core.Project;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashSet;
import java.util.Set;

public class DBAlfrescoListVisitor extends DBAlfrescoVisitor {

    private JSONObject listjson;
    private Set<String> listelements;
    private JSONArray curitem;

    public DBAlfrescoListVisitor(boolean recurse) {
        super(recurse);
        listjson = new JSONObject();
        listelements = new HashSet<String>();
    }

    public JSONObject getObject() {
        return listjson;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(DBList list) {
        if (listjson.containsKey("type")) {
            DBAlfrescoListVisitor inner = new DBAlfrescoListVisitor(recurse);
            list.accept(inner);
            curitem.add(inner.getObject());
            listelements.addAll(inner.getListElements());
            elementSet.addAll(inner.getElementSet());
        }
        else {
            listjson.put("type", "List");
            if (list.isOrdered()) {
                listjson.put("ordered", true);
            }
            else {
                listjson.put("ordered", false);
            }
            listjson.put("bulleted", true);
            JSONArray l = new JSONArray();
            listjson.put("list", l);
            for (DocumentElement de : list.getChildren()) {
                curitem = new JSONArray();
                de.accept(this);
                l.add(curitem);
            }
        }
    }

    @Override
    public void visit(DBListItem listitem) {
        for (DocumentElement de : listitem.getChildren()) {
            de.accept(this);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(DBParagraph para) {
        JSONObject o = getJSONForDBParagraph(para);
        if (para.getFrom() != null && para.getFromProperty() != null) {
            // TODO how necessary is this?
            if (Converters.getElementToJsonConverter().apply(para.getFrom(), Project.getProject(para.getFrom())) != null) {
                this.listelements.add(Converters.getElementToIdConverter().apply(para.getFrom()));
            }
        }
        curitem.add(o);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(DBText text) {
        JSONObject o = getJSONForDBText(text);
        if (text.getFrom() != null && text.getFromProperty() != null) {
            // TODO how necessary is this?
            if (Converters.getElementToJsonConverter().apply(text.getFrom(), Project.getProject(text.getFrom())) != null) {
                this.listelements.add(Converters.getElementToIdConverter().apply(text.getFrom()));
            }
        }
        curitem.add(o);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(DBTable table) {
        DBAlfrescoTableVisitor v = new DBAlfrescoTableVisitor(this.recurse);
        table.accept(v);
        listelements.addAll(v.getTableElements());
        elementSet.addAll(v.getElementSet());
        curitem.add(v.getObject());
    }

    public Set<String> getListElements() {
        return listelements;
    }
}

