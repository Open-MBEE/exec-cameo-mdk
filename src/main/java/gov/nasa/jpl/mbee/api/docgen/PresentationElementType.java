package gov.nasa.jpl.mbee.api.docgen;

import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import gov.nasa.jpl.mbee.api.ElementFinder;

/**
 * Created by igomes on 8/2/16.
 */
public enum PresentationElementType {
    EQUATION("_17_0_5_1_407019f_1431905053808_352752_11992", "SysML Extensions::DocGen::MDK EMP Client::Presentation Elements::Equation"),
    IMAGE("_17_0_5_1_407019f_1431903748021_2367_12034", "SysML Extensions::DocGen::MDK EMP Client::Presentation Elements::Image"),
    LIST("_17_0_5_1_407019f_1431903739087_549326_12013", "SysML Extensions::DocGen::MDK EMP Client::Presentation Elements::List"),
    OPAQUE_IMAGE("_17_0_5_1_407019f_1430628206190_469511_11978", "SysML Extensions::DocGen::MDK EMP Client::Presentation Elements::OpaqueImage"),
    OPAQUE_LIST("_17_0_5_1_407019f_1430628190151_363897_11927", "SysML Extensions::DocGen::MDK EMP Client::Presentation Elements::OpaqueList"),
    OPAQUE_PARAGRAPH("_17_0_5_1_407019f_1430628197332_560980_11953", "SysML Extensions::DocGen::MDK EMP Client::Presentation Elements::OpaqueParagraph"),
    OPAQUE_SECTION("_17_0_5_1_407019f_1430628211976_255218_12002", "SysML Extensions::DocGen::MDK EMP Client::Presentation Elements::OpaqueSection"),
    OPAQUE_TABLE("_17_0_5_1_407019f_1430628178633_708586_11903", "SysML Extensions::DocGen::MDK EMP Client::Presentation Elements::OpaqueTable"),
    PARAGRAPH("_17_0_5_1_407019f_1431903758416_800749_12055", "SysML Extensions::DocGen::MDK EMP Client::Presentation Elements::Paragraph"),
    PRESENTATION_ELEMENT("_17_0_5_1_407019f_1430628230072_232804_12027", "SysML Extensions::DocGen::MDK EMP Client::Presentation Elements::PresentationElement"),
    SECTION("_18_0_2_407019f_1435683487667_494971_14412", "SysML Extensions::DocGen::MDK EMP Client::Presentation Elements::Section"),
    TABLE("_17_0_5_1_407019f_1431903724067_825986_11992", "SysML Extensions::DocGen::MDK EMP Client::Presentation Elements::Table");

    private String id, qualifiedName;

    PresentationElementType(String id, String qualifiedName) {
        this.id = id;
        this.qualifiedName = qualifiedName;
    }

    public String getId() {
        return id;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public Classifier getClassifier(Project project) {
        BaseElement element = project.getElementByID(id);
        if (element instanceof Classifier) {
            return (Classifier) element;
        }
        element = ElementFinder.getElementByQualifiedName(qualifiedName, project);
        return element instanceof Classifier ? (Classifier) element : null;
    }
}
