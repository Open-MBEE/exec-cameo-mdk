package gov.nasa.jpl.mgss.mbee.docgen.actions;

import gov.nasa.jpl.mgss.mbee.docgen.sync.CommentUtil;
import gov.nasa.jpl.mgss.mbee.docgen.sync.CommentsViewWindow;

import java.awt.event.ActionEvent;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

/**
 * see comments annotating a View in a popup dialog
 * @author dlam
 *
 */
@SuppressWarnings("serial")
public class ViewViewCommentsAction extends MDAction {
	private NamedElement selectedElement;
	public static final String actionid = "ViewViewComments";
	public ViewViewCommentsAction(NamedElement selectedElement) {
		super(actionid, "View Comments", null, null);
		this.selectedElement = selectedElement;
	}
	
	@Override
	public void actionPerformed(ActionEvent ac) {
		Stereotype d = CommentUtil.getCommentStereotype();
		CommentsViewWindow frame = new CommentsViewWindow(
				"Comments for " + selectedElement.getName());
		for (Comment e: selectedElement.get_commentOfAnnotatedElement()) {
			if (StereotypesHelper.hasStereotypeOrDerived(e, d)) {
				frame.addComment(e, d);
			}
		}
		if (frame.getCommentCount() == 0) {
			frame.noComments();
		}
		frame.setVisible(true); 
	}
}
