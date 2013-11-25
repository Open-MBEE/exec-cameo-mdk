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

import gov.nasa.jpl.mbee.lib.Utils;

import java.util.HashMap;
import java.util.Map;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.impl.ElementsFactory;

public class MappingUtil {
    private static final String DEPPREFIX = "zz_";

    /**
     * Utility for refactoring properties between the library and mission
     * characterizations (e.g, lib properties are mandatory for mc)
     * 
     * @param lib
     *            Library characterization
     * @param mc
     *            Concrete characterization
     * @param ef
     *            ElementsFactory
     */
    public static void refactorProperties(Element lib, Element mc, ElementsFactory ef) {
        GUILog log = Application.getInstance().getGUILog();
        Map<String, Property> mprops = new HashMap<String, Property>();
        Map<String, Property> lprops = new HashMap<String, Property>();

        // get the property sets for the mission characterization and the
        // library characterization
        for (Element e: mc.getOwnedElement()) {
            if (e instanceof Property) {
                Property p = (Property)e;
                mprops.put(p.getName(), p);
            }
        }
        for (Element e: lib.getOwnedElement()) {
            if (e instanceof Property) {
                Property p = (Property)e;
                lprops.put(p.getName(), p);
            }
        }

        for (Property mprop: mprops.values()) {
            if (lprops.containsKey(mprop.getName().replace(DEPPREFIX, ""))) {
                if (mprop.getName().startsWith(DEPPREFIX)) {
                    mprop.setName(mprop.getName().replace(DEPPREFIX, ""));
                    mprops.put(mprop.getName(), mprop); // so it's not recreated
                                                        // in next pass
                    log.log("Property undeprecated: " + mprop.getName() + " undeprecated in "
                            + mc.getHumanName());
                }
            } else {
                if (!mprop.getName().startsWith(DEPPREFIX)) {
                    mprop.setName(DEPPREFIX + mprop.getName());
                    log.log("Property deprecated: " + mprop.getName() + " deprecated in " + mc.getHumanName());
                }
            }
        }
        for (Property lprop: lprops.values()) {
            if (!mprops.containsKey(lprop.getName())) {
                Property np = ef.createPropertyInstance();
                np.setName(lprop.getName());
                np.setOwner(mc);
                np.setType(lprop.getType());
                np.setAggregation(lprop.getAggregation());
                np.getRedefinedProperty().add(lprop);
                Utils.copyStereotypes(lprop, np);
                log.log("Property created: " + np.getName() + " added to " + mc.getHumanName());
            }
        }
    }
}
