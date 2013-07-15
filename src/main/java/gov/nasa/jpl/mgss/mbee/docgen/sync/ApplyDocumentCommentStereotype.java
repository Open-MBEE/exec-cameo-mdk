package gov.nasa.jpl.mgss.mbee.docgen.sync;

import static gov.nasa.jpl.mgss.mbee.docgen.sync.CommentUtil.DOCUMENT_COMMENT;

import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

public class ApplyDocumentCommentStereotype extends ChangeTheModel {
	private final Comment comment;

	public ApplyDocumentCommentStereotype(Comment comment) {
		this.comment = comment;
	}

	@Override
	public String getDescription() {
		return "Apply DocumentComment Stereotype";
	}

	@Override
	protected void makeChange() {
		if (comment == null) {
			fail("Comment not properly set for action");
		}
		Stereotype stereotype = getStereotype(DOCUMENT_COMMENT);
		if (stereotype == null) {
			fail("Couldn't find " + DOCUMENT_COMMENT + " stereotype");
			return;
		}
		if (StereotypesHelper.hasStereotype(comment, stereotype)) {
			return;
		}
		if (!StereotypesHelper.canApplyStereotype(comment,  stereotype)) {
			fail("Can't apply stereotype");
			return;
		}

		StereotypesHelper.addStereotype(comment, stereotype);
	}
}