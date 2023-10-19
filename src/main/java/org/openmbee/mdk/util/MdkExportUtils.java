package org.openmbee.mdk.util;

import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.ui.dialogs.MDDialogParentProvider;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class MdkExportUtils {

	private MdkExportUtils() {}
	
	/**
	 * @param project 
	 * @return selected folder, or null if cancelled
	 */
	public static File chooseFolder(Project project) {
		Window dialogParent = MDDialogParentProvider.getProvider().getDialogOwner();
		JFileChooser folderChooser = new JFileChooser();
		folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		folderChooser.setDialogTitle("Select folder to export into");
		
		if (!project.isRemote()) {
			File projectLocation = new File(project.getFileName());
			File projectFolder = projectLocation.getParentFile();
			folderChooser.setSelectedFile(projectFolder);
		}
		
		
		int result = folderChooser.showDialog(dialogParent, "Select");
		File selectedFolder = folderChooser.getSelectedFile();
		File folderSelection = (result == JFileChooser.APPROVE_OPTION ) ? selectedFolder : null;
		return folderSelection;
	}
	
}
