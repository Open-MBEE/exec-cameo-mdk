/*******************************************************************************
 * Copyright (c) <2013>, California Institute of Technology ("Caltech").  
 * U.S. Government sponsorship acknowledged.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are 
 * permitted provided that the following conditions are met:
 * 
 *  - Redistributions of source code must retain the above copyright notice, this list of 
 *    conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice, this list 
 *    of conditions and the following disclaimer in the documentation and/or other materials 
 *    provided with the distribution.
 *  - Neither the name of Caltech nor its operating division, the Jet Propulsion Laboratory, 
 *    nor the names of its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER  
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package gov.nasa.jpl.mbee.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.simple.JSONObject;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.EnumerationLiteral;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;

import gov.nasa.jpl.mbee.DocGen3Profile;
import gov.nasa.jpl.mbee.lib.GeneratorUtils;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.lib.Utils.AvailableAttribute;
import gov.nasa.jpl.mbee.lib.Utils2;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBHasContent;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBParagraph;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;

public class TemporalDiff extends Table {
	private String baseVersionTime;
	private String compareToTime;
	private AvailableAttribute attributeToCompare;

	public TemporalDiff() {
		setSortElementsByName(false);
	}

	public void addStereotypeProperties(DBHasContent parent, Element e, Property p) {
		Common.addReferenceToDBHasContent(Reference.getPropertyReference(e, p), parent);
	}

	@Override
	public void initialize() {

		Object attr = GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.temporalDiffStereotype, "attributeToCompare", null);
		if (attr instanceof EnumerationLiteral) {
			attributeToCompare = Utils.AvailableAttribute.valueOf(((EnumerationLiteral) attr).getName());
			if (attributeToCompare != null) {
				setAttributeToCompare(Utils.getAvailableAttribute(attributeToCompare));
			}
		}
		baseVersionTime = (String) GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.temporalDiffStereotype, "baseVersionTime", false);
		compareToTime = (String) GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.temporalDiffStereotype, "compareToTime", false);
	}

	@Override
	public List<DocumentElement> visit(boolean forViewEditor, String outputDir) {
		List<DocumentElement> res = new ArrayList<DocumentElement>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssSSSZ");
		Date baseVersionDate = null;
		Date compareToDate = null;
		if (null != baseVersionTime & !baseVersionTime.isEmpty() & !baseVersionTime.equalsIgnoreCase("latest")) {
			baseVersionDate = parseDate(baseVersionTime);
		}
		if (null != compareToTime & !compareToTime.isEmpty() & !compareToTime.equalsIgnoreCase("latest")) {
			compareToDate = parseDate(compareToTime);
		}
		List<Object> list = getTargets(); // This is not the right list of objects so far?
		if (forViewEditor) {
			// for every target.
			DBParagraph retval = new DBParagraph();
			StringBuffer tag = new StringBuffer();
			for (Object e : list) {
				if (e instanceof Element) {
					tag.append("<mms-diff-attr mms-eid=\"");
					tag.append(((Element) e).getID() + "\"");
					tag.append(" mms-attr=\"" + attributeToCompare.name() + "\" mms-version-one=");
					if (compareToTime == null) {
						tag.append("latest");
					} else if (compareToDate == null) {
						tag.append(compareToTime);
					} else {
						tag.append(sdf.format(compareToDate));
					}
					tag.append("\" mms-version-two=\"");
					if (baseVersionTime == null) {
						tag.append("latest");
					} else if (baseVersionDate == null) {
						tag.append(baseVersionTime);
					} else {
						tag.append(sdf.format(baseVersionDate));
					}
					tag.append("\"></mms-diff-attr>");
				}
			}

			retval.setText(tag); // concatenate the elements
			// System.out.println(tag);
			res.add(retval);
			return res;
		} else {
			for (Object e : list) {
				if (e instanceof Element) {
					if (compareToTime == null) {
						Object v = Utils.getElementAttribute((Element) e, attributeToCompare);
						if (!Utils2.isNullOrEmpty(v)) {
							if (v instanceof String) {
								// System.out.println(v);
							}
						}
					} else {
						JSONObject compareJson = TimeQueryUtil.getHistoryOfElement((Element) e, compareToDate);
						// System.out.println("Comp _____________" + compareJson);
					}
					if (baseVersionTime == null) {
						Object v = Utils.getElementAttribute((Element) e, attributeToCompare);
						if (!Utils2.isNullOrEmpty(v)) {
							if (v instanceof String) {
								// System.out.println(v);
							}
						}
					} else {
						JSONObject baseJson = TimeQueryUtil.getHistoryOfElement((Element) e, baseVersionDate);
						// System.out.println("Base _____________" + baseJson);

					}
				}
				// diff the elements
				DBParagraph retval = new DBParagraph();
				retval.setText("</diffResult>this will be the results.</diffResults>"); // concatenate the elements
				res.add(retval);

			}
			return res;
		}
	}

	public String getBaseVersionTime() {
		return baseVersionTime;
	}

	public void setBaseVersionTime(String baseVersionTime) {
		this.baseVersionTime = baseVersionTime;
	}

	public String getCompareToTime() {
		return compareToTime;
	}

	public void setCompareToTime(String compareToTime) {
		this.compareToTime = compareToTime;
	}

	public AvailableAttribute getAttributeToCompare() {
		return attributeToCompare;
	}

	public void setAttributeToCompare(AvailableAttribute attributeToCompare) {
		this.attributeToCompare = attributeToCompare;
	}

	private Date parseDate(String candidate) {
		List<SimpleDateFormat> knownPatterns = new ArrayList<SimpleDateFormat>();
		knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
		knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX"));
		knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss"));
		knownPatterns.add(new SimpleDateFormat("yyyy/MM/dd'T'HH:mm:ss"));
		knownPatterns.add(new SimpleDateFormat("yyyy/MM/dd'T'HH:mm"));
		knownPatterns.add(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"));
		knownPatterns.add(new SimpleDateFormat("yyyy/MM/dd HH:mm"));
		knownPatterns.add(new SimpleDateFormat("MM/dd/yyyy'T'HH:mm:ss"));
		knownPatterns.add(new SimpleDateFormat("MM/dd/yyyy'T'HH"));
		knownPatterns.add(new SimpleDateFormat("yyyy/MM/dd"));
		knownPatterns.add(new SimpleDateFormat("MM/dd/yyyy"));
		knownPatterns.add(new SimpleDateFormat("yyyyMMdd"));
		knownPatterns.add(new SimpleDateFormat("MMM/dd/yyyy"));
		for (SimpleDateFormat pattern : knownPatterns) {
			try {
				// Take a try
				return new Date(pattern.parse(candidate).getTime());

			} catch (ParseException pe) {
				// Loop on
			}
		}
		System.err.println("No known Date format found: " + candidate);
		return null;
	}
}
