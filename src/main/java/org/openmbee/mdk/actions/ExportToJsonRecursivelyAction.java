package org.openmbee.mdk.actions;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.ui.ProgressStatusRunner;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import org.openmbee.mdk.util.MdkExportUtils;
import org.openmbee.mdk.emf.BulkExport;
import org.openmbee.mdk.fileexport.FileExportRunner;
import org.openmbee.mdk.fileexport.FileExportRunner.FileFormat;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

public class ExportToJsonRecursivelyAction extends MDAction {
	
    public static ExportToJsonRecursivelyAction exportEntirePrimaryModel() {
		return new ExportToJsonRecursivelyAction(p -> Collections.singleton(p.getPrimaryModel()), BulkExport.DEPTH_INFINITE, FileExportRunner.FileFormat.MODEL, "Export entire (primary) model to .mdkmodel file");
	}

    public static ExportToJsonRecursivelyAction exportElementOnly(Element e) {
		return new ExportToJsonRecursivelyAction(p -> Collections.singleton(e), BulkExport.DEPTH_NO_DESCENT, FileFormat.FRAGMENT, "Export element (without contained elements) to .mdkfragment file");
	}
    public static ExportToJsonRecursivelyAction exportElementHierarchy(Element e) {
		return new ExportToJsonRecursivelyAction(p -> Collections.singleton(e), BulkExport.DEPTH_INFINITE, FileFormat.TREE, "Export containment subtree to .mdktree file");
	}
	
    public static ExportToJsonRecursivelyAction exportElementOnly(Collection<Element> es) {
		return new ExportToJsonRecursivelyAction(p -> es, BulkExport.DEPTH_NO_DESCENT, FileFormat.FRAGMENT, "Export selected elements (without contained elements) to .mdkfragment file");
	}
    public static ExportToJsonRecursivelyAction exportElementHierarchy(Collection<Element> es) {
		return new ExportToJsonRecursivelyAction(p -> es, BulkExport.DEPTH_INFINITE, FileFormat.TREE, "Export selected containment subtrees to .mdktree file");
	}
	
    public static final String DEFAULT_ID = "ExportToJsonRecursively";

	final Function<Project, Collection<Element>> rootsProvider;
	final int depth;
	final String title;
	private FileFormat format;
	
	
	
	/**
	 * @param selectedRootElements null if entire primary model is to be exported
	 */
    public ExportToJsonRecursivelyAction(Function<Project, Collection<Element>> rootsProvider, int depth, FileFormat format, String title) {
        super(
//			DEFAULT_ID,
			String.format("%s_depth%d",
				DEFAULT_ID,
				// selectedRootElements.stream()
				 // .map(Element::getID)
				 // .map(Object::toString)
				 // .collect(Collectors.joining("_")),
				depth
			), 
			title, null, null
		);
        this.rootsProvider = rootsProvider;
		this.depth = depth;
		this.format = format;
		this.title = title;
    }
    

    @Override
    public void actionPerformed(ActionEvent e) {
		Project project = Application.getInstance().getProject();
		if (project != null) {
			Collection<Element> rootElements = rootsProvider.apply(project);
			File folderSelection =  MdkExportUtils.chooseFolder(project);
			if (folderSelection != null) exportIntoFolder(folderSelection, project, rootElements);		
		}
    }

	/**
	 * @param folderSelection
	 */
	private void exportIntoFolder(File folderSelection, Project project, Collection<Element> rootElements) {
    	FileExportRunner exportRunner = new FileExportRunner(rootElements, project, depth, format, folderSelection);
        ProgressStatusRunner.runWithProgressStatus(exportRunner, title, true, 0);
	}



}
