package gov.nasa.jpl.mbee.mdk.model;

import com.nomagic.magicdraw.core.Application;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;

public abstract class AbstractModelVisitor implements IModelVisitor {

    @Override
    public void visit(Query q) {

    }

    @Override
    public void visit(Document doc) {
        visitChildren(doc);

    }

    @Override
    public void visit(Section sec) {
        visitChildren(sec);
    }

    protected void visitChildren(Container c) {
        for (DocGenElement dge : c.getChildren()) {
            try {
                dge.accept(this);
            } catch (RuntimeException e) {
                String errorMessage = "[ERROR] An unexpected error occurred when generating view" + (dge.getDgElement() != null ? " with " + Converters.getElementToHumanNameConverter().apply(dge.getDgElement()) : "") + ". Skipping and continuing. Reason: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
                Application.getInstance().getGUILog().log(errorMessage);
                System.err.println(errorMessage);
                e.printStackTrace();
            }
        }
    }

}
