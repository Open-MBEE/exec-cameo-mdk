package gov.nasa.jpl.mgss.mbee.docgen.sync;

import gov.nasa.jpl.mgss.mbee.docgen.DocGen3Profile;

import java.awt.event.ActionEvent;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

@SuppressWarnings("serial")
public class ViewDocumentComments extends MDAction {
	private NamedElement selectedElement;
	
	public ViewDocumentComments(NamedElement selectedElement) {
		super("ViewDocumentComments", "View Document Comments", null, null);
		this.selectedElement = selectedElement;
	}
	
	@Override
	public void actionPerformed(ActionEvent ac) {
		Stereotype s = CommentUtil.getCommentStereotype();
		CommentsViewWindow frame = new CommentsViewWindow(
				"Comments for " + selectedElement.getName());
		for (Element e: selectedElement.getOwnedElement()) {
			if (isDocumentComment(e)) {
				Comment c = (Comment) e;
				frame.addComment(c, s);
			}
		}
		if (frame.getCommentCount() == 0) {
			frame.noComments();
		}
		frame.setVisible(true); 
	}

	private boolean isDocumentComment(Element e) {
		return e instanceof Comment
				&& StereotypesHelper.hasStereotypeOrDerived(e,DocGen3Profile.documentCommentStereotype);
	}

}
