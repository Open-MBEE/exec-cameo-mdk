package gov.nasa.jpl.mgss.mbee.docgen.model;

import gov.nasa.jpl.mbee.lib.GeneratorUtils;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.DocGen3Profile;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBHasContent;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBImage;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBParagraph;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.From;
import gov.nasa.jpl.mgss.mbee.docgen.generator.Generatable;

import java.util.ArrayList;
import java.util.List;

import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.ActivityNode;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class Image extends Query {

	protected List<String> captions;
	protected boolean showCaptions;
	protected boolean doNotShow;
	
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
	
	public Boolean getShowCaptions() {
		return showCaptions;
	}
	
	public List<String> getCaptions() {
		return captions;
	}

	@Override
	public List<DocumentElement> visit(boolean forViewEditor, String outputDir) {
	    List<DocumentElement> res = new ArrayList<DocumentElement>();
		if (getIgnore())
			return res;
		if (getTargets() != null) {
	    List< Element > targets =
	        isSortElementsByName() ? Utils.sortByName( getTargets() )
	                                 : getTargets();
			for (int i = 0; i < targets.size(); i++) {
				Element e = targets.get(i);
				if (e instanceof Diagram) {
					DBImage im = new DBImage();
					im.setDiagram((Diagram)e);
					im.setDoNotShow(getDoNotShow());
					String title = "";
					if (getTitles() != null && getTitles().size() > i)
						title = getTitles().get(i);
					else
						title = ((Diagram)e).getName();
					if (getTitlePrefix() != null)
						title = getTitlePrefix() + title;
					if (getTitleSuffix() != null)
						title = title + getTitleSuffix();
					im.setTitle(title);
					if (getCaptions() != null && getCaptions().size() > i && getShowCaptions())
						im.setCaption(getCaptions().get(i));
					im.setId(e.getID());
					res.add(im);
				
					String doc = ModelHelper.getComment(e);
					if (doc != null && (forViewEditor || (!doc.trim().equals("") && !getDoNotShow()))) {
						res.add(new DBParagraph(doc, e, From.DOCUMENTATION));
					}
					
				}
			}
		}	
		return res;
	}
	
	@Override
	public void initialize() {
		// TODO Auto-generated method stub
		Boolean doNotShow = (Boolean)GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.imageStereotype, "doNotShow", false);
		setCaptions((List<String>)GeneratorUtils.getListProperty(dgElement, DocGen3Profile.hasCaptions, "captions", new ArrayList<String>()));
		setShowCaptions((Boolean)GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.hasCaptions, "showCaptions", true));
		setDoNotShow(doNotShow);
	}

	
}
