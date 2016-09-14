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
package gov.nasa.jpl.mbee.stylesaver;

import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import org.json.simple.JSONValue;

import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

/**
 * Nested class contains a run method for the save operation. Updates progress
 * bar dynamically.
 *
 * @author Benjamin Inada, JPL/Caltech
 */
public class RunnableSaverWithProgress implements RunnableWithProgress {
    private Map<String, String> mainStore;
    private List<PresentationElement> elemList;
    private String styleString;
    private Element diagram;
    private Stereotype workingStereotype;
    private boolean success;

    /**
     * @param mainStore2 the main style store for this diagram.
     * @param elemList   the elements to save.
     */
    public RunnableSaverWithProgress(Map<String, String> mainStore2, List<PresentationElement> elemList, Element diagram,
                                     Stereotype workingStereotype) {
        this.mainStore = mainStore2;
        this.elemList = elemList;
        this.diagram = diagram;
        this.workingStereotype = workingStereotype;
    }

    /**
     * Runs the save operation.
     *
     * @param progressStatus the status of the operation so far.
     */
    @Override
    public void run(ProgressStatus progressStatus) {
        progressStatus.init("Saving styles...", 0, elemList.size() + 1);

        for (PresentationElement elem : elemList) {
            if (progressStatus.isCancel()) {
                success = false;
                return;
            }

            // save the element's style properties
            try {
                String styleStr = ViewSaver.getStyle(elem);

                // if there is no style to save, continue to next element
                if (styleStr == null) {
                    progressStatus.increase();
                    continue;
                }

                mainStore.put(elem.getID(), styleStr);
            } catch (ClassCastException e) {
                e.printStackTrace();
            } catch (MissingResourceException e) {
                e.printStackTrace();
            }

            // recursively save child elements' style properties (e.g. ports)
            ViewSaver.getStyleChildren(elem, mainStore, progressStatus);

            progressStatus.increase();
        }

        // convert to JSON - this takes a while
        styleString = JSONValue.toJSONString(mainStore);

        if (progressStatus.isCancel()) {
            success = false;
            return;
        }

        // set the style string into the view "style" tag
        StereotypesHelper.setStereotypePropertyValue(diagram, workingStereotype, "style", styleString);

        progressStatus.increase();
        success = true;
    }

    /**
     * Gets the value of the success property.
     *
     * @return the value of the success property.
     */
    public boolean getSuccess() {
        return success;
    }

    /**
     * Gets the value of the styleString property.
     *
     * @return the value of the styleString property.
     */
    public String getStyleString() {
        return styleString;
    }
}
