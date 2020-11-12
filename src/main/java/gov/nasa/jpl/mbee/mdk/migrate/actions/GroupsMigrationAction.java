package gov.nasa.jpl.mbee.mdk.migrate.actions;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Dependency;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Generalization;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;

import javax.annotation.CheckForNull;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GroupsMigrationAction extends MDAction {
    public GroupsMigrationAction() {
        super(GroupsMigrationAction.class.getSimpleName(), "Groups", null, null);
    }

    @Override
    public void actionPerformed(@CheckForNull ActionEvent actionEvent) {
        Project project = Application.getInstance().getProject();
        if (project == null) {
            Application.getInstance().getGUILog().log("[ERROR] No open project. Skipping group migration.");
            return;
        }
        Element element = Converters.getIdToElementConverter().apply("_17_0_5_1_8660276_1415063844134_132446_18688", project);
        if (!(element instanceof Classifier)) {
            Application.getInstance().getGUILog().log("[WARNING] General Site Characterization not found. Ensure that the SysML Extensions profile is mounted. Skipping group migration.");
            return;
        }
        Classifier siteCharacterization = (Classifier) element;
        element = Converters.getIdToElementConverter().apply("_18_5_3_8bf0285_1520469040211_2821_15754", project);
        if (!(element instanceof Stereotype)) {
            Application.getInstance().getGUILog().log("[WARNING] Group stereotype not found. Ensure that the SysML Extensions profile is mounted. Skipping group migration.");
            return;
        }
        Stereotype groupStereotype = (Stereotype) element;
        // no need to recurse since the deprecated logic only looked one level of generalization down
        List<Classifier> specials = siteCharacterization.get_generalizationOfGeneral().stream().map(Generalization::getSpecific).filter(classifier -> classifier != null && !ProjectUtilities.isElementInAttachedProject(classifier)).collect(Collectors.toList());
        Set<Element> elementsToDelete = new HashSet<>();
        List<Package> groups = new ArrayList<>();
        for (Classifier special : specials) {
            List<Dependency> dependencies = special.getSupplierDependency().stream().filter(dependency -> StereotypesHelper.getStereotypes(dependency).stream().anyMatch(stereotype -> "_17_0_5_1_8660276_1407362513794_939259_26181".equals(Converters.getElementToIdConverter().apply(stereotype)))).filter(dependency -> !ProjectUtilities.isElementInAttachedProject(dependency)).collect(Collectors.toList());
            elementsToDelete.addAll(dependencies);
            dependencies.stream().flatMap(dependency -> dependency.getClient().stream()).filter(e -> e instanceof Package).map(e -> (Package) e).forEach(groups::add);
            if (special.getOwnedMember().isEmpty()) {
                elementsToDelete.add(special);
            }
            else {
                Application.getInstance().getGUILog().log("[WARNING] Skipping deletion of " + Converters.getElementToHumanNameConverter().apply(special) + " as it has owned members.");
            }
        }
        SessionManager.getInstance().createSession(project, "Group Migration");
        try {
            groups.forEach(group -> StereotypesHelper.addStereotype(group, groupStereotype));
            for (Element elementToDelete : elementsToDelete) {
                System.out.println("Deleting " + Converters.getElementToHumanNameConverter().apply(elementToDelete));
                ModelElementsManager.getInstance().removeElement(elementToDelete);
            }
        } catch (ReadOnlyElementException e) {
            Application.getInstance().getGUILog().log("[ERROR] Element(s), including " + Converters.getElementToHumanNameConverter().apply((Element) e.getElement()) + ", are not editable. Lock the entire model before migrating. Skipping group migration.");
            SessionManager.getInstance().cancelSession(project);
            e.printStackTrace();
        } finally {
            if (SessionManager.getInstance().isSessionCreated(project)) {
                SessionManager.getInstance().closeSession(project);
            }
        }
        Application.getInstance().getGUILog().log("[INFO] Migrated " + NumberFormat.getInstance().format(groups.size()) + " group" + (groups.size() != 1 ? "s" : "") + " and deleted " + NumberFormat.getInstance().format(elementsToDelete.size()) + " site characterization related element" + (elementsToDelete.size() != 1 ? "s" : "") + ".");
    }

    @Override
    public void updateState() {
        this.setEnabled(Application.getInstance().getProject() != null);
    }
}
