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

import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.EnumerationLiteral;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;

import gov.nasa.jpl.mbee.DocGen3Profile;
import gov.nasa.jpl.mbee.lib.GeneratorUtils;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.lib.Utils.AvailableAttribute;
import gov.nasa.jpl.mbee.lib.Utils2;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBHasContent;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;

public class TemporalDiff extends Table {
	private Date baseVersionTime;
	private Date compareToTime;
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

		String baseVersionTime = (String) GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.temporalDiffStereotype, "baseVersionTime", false);
		String compareToTime = (String) GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.temporalDiffStereotype, "compareToTime", false);

		Date baseVersionDate = null;
		Date compareToDate = null;
		if (null == baseVersionTime || baseVersionTime.isEmpty() || baseVersionTime.equalsIgnoreCase("now")) {

		} else {
			try {
				baseVersionDate = parseDate(baseVersionTime);
			} catch (ParseException e) {
				Application.getInstance().getGUILog().log("[ERROR] Cannot parse baseVersionTime date format. Please use yyyy/MM/dd or yyyy/MM/dd HH:mm if required.");
			}
		}
		if (null == compareToTime || compareToTime.isEmpty() || compareToTime.equalsIgnoreCase("now")) {

		} else {
			try {
				compareToDate = parseDate(compareToTime);
			} catch (ParseException e) {
				Application.getInstance().getGUILog().log("[ERROR] Cannot parse compareToTime date format. Please use yyyy/MM/dd or yyyy/MM/dd HH:mm if required.");
			}
		}

		setBaseVersionTime(baseVersionDate);
		setCompareToTime(compareToDate);

	}

	@Override
	public List<DocumentElement> visit(boolean forViewEditor, String outputDir) {
		List<DocumentElement> res = new ArrayList<DocumentElement>();
		List<Object> list = getTargets(); // This is not the right list of objects so far?
		for (Object e : list) {
			if (e instanceof Element) {
				if (e instanceof NamedElement) {
					System.out.println(((NamedElement) e).getName());
				}
				Object v = Utils.getElementAttribute((Element) e, attributeToCompare);
				if (!Utils2.isNullOrEmpty(v)) {
					if (v instanceof String) {
						System.out.println(v);
					}
				}
			}
		}
		return res;
	}

	public Date getBaseVersionTime() {
		return baseVersionTime;
	}

	public void setBaseVersionTime(Date baseVersionTime) {
		this.baseVersionTime = baseVersionTime;
	}

	public Date getCompareToTime() {
		return compareToTime;
	}

	public void setCompareToTime(Date compareToTime) {
		this.compareToTime = compareToTime;
	}

	public AvailableAttribute getAttributeToCompare() {
		return attributeToCompare;
	}

	public void setAttributeToCompare(AvailableAttribute attributeToCompare) {
		this.attributeToCompare = attributeToCompare;
	}

	private Date parseDate(String candidate) throws ParseException {
		List<SimpleDateFormat> knownPatterns = new ArrayList<SimpleDateFormat>();
		knownPatterns.add(new SimpleDateFormat("yyyy/MM/dd"));
		knownPatterns.add(new SimpleDateFormat("MM/dd/yyyy"));
		knownPatterns.add(new SimpleDateFormat("yyyyMMdd"));
		knownPatterns.add(new SimpleDateFormat("MMM/dd/yyyy"));
		knownPatterns.add(new SimpleDateFormat("yyyy/MM/dd'T'HH:mm:ss"));
		knownPatterns.add(new SimpleDateFormat("yyyy/MM/dd'T'HH:mm"));
		knownPatterns.add(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"));
		knownPatterns.add(new SimpleDateFormat("yyyy/MM/dd HH:mm"));
		knownPatterns.add(new SimpleDateFormat("MM/dd/yyyy'T'HH:mm:ss"));
		knownPatterns.add(new SimpleDateFormat("MM/dd/yyyy'T'HH"));
		knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss"));
		knownPatterns.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX"));

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
