package gov.nasa.jpl.mbee.mdk.model;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Dependency;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.impl.ElementsFactory;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.lib.Utils;

import java.util.List;
import java.util.Stack;


public class HierarchyMigrationVisitor extends AbstractModelVisitor {

    private Element owner;
    private Project project;
    private Stack<Class> parentView;
    private ElementsFactory ef;
    private Stereotype ourExpose,
            docS,
            viewS,
            conformS;
    private boolean preserveId = false;
    private boolean cannotChangeId = false;

    public HierarchyMigrationVisitor(Element owner, boolean id) {
        this.owner = owner;
        this.project = Project.getProject(owner);
        this.ourExpose = Utils.getExposeStereotype(project);
        this.docS = Utils.getDocumentStereotype(project);
        this.viewS = Utils.getViewClassStereotype(project);
        this.conformS = Utils.getConformsStereotype(project);

        this.parentView = new Stack<Class>();
        this.ef = project.getElementsFactory();
        project.getCounter().setCanResetIDForObject(true);
        this.preserveId = id;
    }

    public boolean changeIdFailed() {
        return cannotChangeId;
    }

    @Override
    public void visit(Document doc) {
        if (doc.getDgElement() != null) {
            Element d = doc.getDgElement();
            Class newDoc = ef.createClassInstance();
            if (preserveId) {
                setId(d, newDoc);
            }
            newDoc.setName(((NamedElement) d).getName());
            ModelHelper.setComment(newDoc, ModelHelper.getComment(d));
            newDoc.setOwner(owner);
            StereotypesHelper.addStereotype(newDoc, docS);
            parentView.push(newDoc);
            visitChildren(doc);
            setExposeAndConforms(null, d, newDoc);
        }
    }

    @Override
    public void visit(Section sec) {
        if (sec.isView()) {// && !sec.isNoSection()) {
            Element v = sec.getDgElement();
            Class newView = ef.createClassInstance();
            if (preserveId) {
                setId(v, newView);
            }
            newView.setName(((NamedElement) v).getName());
            ModelHelper.setComment(newView, ModelHelper.getComment(v));
            newView.setOwner(parentView.peek());
            Association a = ef.createAssociationInstance();
            a.setOwner(owner);
            a.getMemberEnd().get(0).setOwner(parentView.peek());
            a.getMemberEnd().get(1).setOwner(a);
            a.getMemberEnd().get(0).setName(newView.getName().toLowerCase());
            a.getMemberEnd().get(0).setType(newView);
            a.getMemberEnd().get(1).setType(parentView.peek());
            a.getMemberEnd().get(0).setAggregation(AggregationKindEnum.COMPOSITE);
            setExposeAndConforms(sec, v, newView);
            StereotypesHelper.addStereotype(newView, viewS);
            parentView.push(newView);
            visitChildren(sec);
            parentView.pop();
        }
    }

    private void setId(Element old, Element neww) {
        if (old.isEditable()) {
            if (!(old instanceof Diagram)) {
                String oldId = Converters.getElementToIdConverter().apply(old);
                String newId = Converters.getElementToIdConverter().apply(neww);
                neww.setID(oldId);
                old.setID(newId);
            }
        }
        else {
            cannotChangeId = true;
        }
    }

    private void setExposeAndConforms(Section sec, Element old, Class neww) {
        if (sec == null) {
            return;
        }
        List<Element> expose = sec.getExposes();
        Element viewpoint = sec.getViewpoint();
        if (viewpoint != null) {
            Generalization g = ef.createGeneralizationInstance();
            g.setOwner(neww);
            g.setGeneral((Classifier) viewpoint);
            g.setSpecific(neww);
            StereotypesHelper.addStereotype(g, conformS);
        }
        if (old instanceof Diagram) {
            Dependency dep = ef.createDependencyInstance();
            dep.setOwner(owner);
            dep.getTarget().add(old);
            dep.getSource().add(neww);
            StereotypesHelper.addStereotype(dep, ourExpose);
        }
        else if (expose != null) {
            for (Element e : expose) {
                Dependency dep = ef.createDependencyInstance();
                dep.setOwner(owner);
                dep.getTarget().add(e);
                dep.getSource().add(neww);
                StereotypesHelper.addStereotype(dep, ourExpose);
            }
        }
    }
}
