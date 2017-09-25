package gov.nasa.jpl.mbee.mdk.generator;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification;

import java.util.List;

public class PresentationElementInfo {
    private List<InstanceSpecification> all;
    private List<InstanceSpecification> opaque;
    private List<InstanceSpecification> images;
    private List<InstanceSpecification> tables;
    private List<InstanceSpecification> figures;
    private List<InstanceSpecification> lists;
    private List<InstanceSpecification> paras;
    private List<InstanceSpecification> manuals;
    private List<InstanceSpecification> sections;

    private List<InstanceSpecification> extraRef; //opaque instances that are referenced by > 1 view
    private List<InstanceSpecification> extraManualRef; //manual instances that are referenced by > 1 view
    private List<InstanceSpecification> unused; //unused instances in view instance package

    public PresentationElementInfo(List<InstanceSpecification> all, List<InstanceSpecification> images, List<InstanceSpecification> tables, List<InstanceSpecification> figures, List<InstanceSpecification> lists, List<InstanceSpecification> paras, List<InstanceSpecification> sections, List<InstanceSpecification> manuals, List<InstanceSpecification> extraRef, List<InstanceSpecification> extraManualRef, List<InstanceSpecification> unused, List<InstanceSpecification> opaque) {
        this.all = all;
        this.images = images;
        this.tables = tables;
        this.figures = figures;
        this.lists = lists;
        this.manuals = manuals;
        this.paras = paras;
        this.sections = sections;
        this.extraRef = extraRef;
        this.extraManualRef = extraManualRef;
        this.unused = unused;
        this.opaque = opaque;
    }

    public List<InstanceSpecification> getAll() {
        return all;
    }

    public void setAll(List<InstanceSpecification> all) {
        this.all = all;
    }

    public List<InstanceSpecification> getImages() {
        return images;
    }

    public void setImages(List<InstanceSpecification> images) {
        this.images = images;
    }

    public List<InstanceSpecification> getTables() {
        return tables;
    }

    public void setTables(List<InstanceSpecification> tables) {
        this.tables = tables;
    }

    public List<InstanceSpecification> getLists() {
        return lists;
    }

    public List<InstanceSpecification> getFigures() {
        return figures;
    }

    public void setFigures(List<InstanceSpecification> figures) {
        this.figures = figures;
    }

    public void setLists(List<InstanceSpecification> lists) {
        this.lists = lists;
    }

    public List<InstanceSpecification> getParas() {
        return paras;
    }

    public void setParas(List<InstanceSpecification> paras) {
        this.paras = paras;
    }

    public List<InstanceSpecification> getManuals() {
        return manuals;
    }

    public void setManuals(List<InstanceSpecification> manuals) {
        this.manuals = manuals;
    }

    public List<InstanceSpecification> getSections() {
        return sections;
    }

    public void setSections(List<InstanceSpecification> sections) {
        this.sections = sections;
    }

    public List<InstanceSpecification> getExtraRef() {
        return extraRef;
    }

    public void setExtraRef(List<InstanceSpecification> extraRef) {
        this.extraRef = extraRef;
    }

    public List<InstanceSpecification> getUnused() {
        return unused;
    }

    public void setUnused(List<InstanceSpecification> unused) {
        this.unused = unused;
    }

    public List<InstanceSpecification> getOpaque() {
        return opaque;
    }

    public void setOpaque(List<InstanceSpecification> opaque) {
        this.opaque = opaque;
    }

    public List<InstanceSpecification> getExtraManualRef() {
        return extraManualRef;
    }

    public void setExtraManualRef(List<InstanceSpecification> extraManualRef) {
        this.extraManualRef = extraManualRef;
    }

}
