package org.openmbee.mdk.model;

import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import org.openmbee.mdk.api.incubating.convert.Converters;
import org.openmbee.mdk.docgen.docbook.DBImage;
import org.openmbee.mdk.docgen.docbook.DBParagraph;
import org.openmbee.mdk.docgen.docbook.DocumentElement;
import org.openmbee.mdk.docgen.docbook.From;
import org.openmbee.mdk.util.GeneratorUtils;
import org.openmbee.mdk.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class Image extends Query {

    protected List<String> captions;
    protected boolean showCaptions;
    protected boolean doNotShow;
    protected boolean excludeFromList;

    public void setCaptions(List<String> c) {
        captions = c;
    }

    public void setShowCaptions(boolean b) {
        showCaptions = b;
    }

    public void setDoNotShow(boolean b) {
        doNotShow = b;
    }

    public Boolean getDoNotShow() {
        return doNotShow;
    }

    public boolean isExcludeFromList() {
        return excludeFromList;
    }

    public void setExcludeFromList(boolean excludeFromList) {
        this.excludeFromList = excludeFromList;
    }

    public Boolean getShowCaptions() {
        return showCaptions;
    }

    public List<String> getCaptions() {
        return captions;
    }

    @Override
    public List<DocumentElement> visit(boolean forViewEditor, String outputDir) {
        List<DocumentElement> res = new ArrayList<DocumentElement>();
        if (getIgnore()) {
            return res;
        }
        if (getTargets() != null) {
            List<Object> targets = isSortElementsByName() ? Utils.sortByName(getTargets()) : getTargets();
            for (int i = 0; i < targets.size(); i++) {
                Object o = targets.get(i);
                if (o instanceof Diagram) {
                    Diagram diagram = (Diagram) o;
                    DBImage im = new DBImage();
                    im.setDiagram(diagram);
                    im.setDoNotShow(doNotShow);
                    im.setExcludeFromList(excludeFromList);
                    String title = "";
                    if (getTitles() != null && getTitles().size() > i) {
                        title = getTitles().get(i);
                    }
                    else {
                        title = diagram.getName();
                    }
                    if (getTitlePrefix() != null) {
                        title = getTitlePrefix() + title;
                    }
                    if (getTitleSuffix() != null) {
                        title = title + getTitleSuffix();
                    }
                    im.setTitle(title);
                    if (getCaptions() != null && getCaptions().size() > i && getShowCaptions()) {
                        im.setCaption(getCaptions().get(i));
                    }
                    im.setId(Converters.getElementToIdConverter().apply(diagram));
                    res.add(im);

                    String doc = ModelHelper.getComment(diagram);
                    if (doc != null && (forViewEditor || (!doc.trim().isEmpty() && !getDoNotShow()))) {
                        if ((Boolean) GeneratorUtils.getStereotypePropertyFirst(diagram, profile.editableChoosable().getEditableProperty(), true)) {
                            res.add(new DBParagraph(doc, diagram, From.DOCUMENTATION));
                        }
                        else {
                            res.add(new DBParagraph(doc));
                        }
                    }

                }
            }
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initialize() {
        super.initialize();
        setDoNotShow((Boolean) GeneratorUtils.getStereotypePropertyFirst(dgElement,
                profile.image().getDoNotShowProperty(), false));
        setCaptions((List<String>) GeneratorUtils.getStereotypePropertyValue(dgElement,
                profile.hasCaptions().getCaptionsProperty(), new ArrayList<String>()));
        setShowCaptions((Boolean) GeneratorUtils.getStereotypePropertyFirst(dgElement,
                profile.hasCaptions().getShowCaptionsProperty(), true));
        setExcludeFromList((Boolean) GeneratorUtils.getStereotypePropertyFirst(dgElement,
                profile.hasCaptions().getExcludeFromListProperty(), false));
    }

}
