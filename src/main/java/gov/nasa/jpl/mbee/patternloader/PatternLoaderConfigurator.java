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
package gov.nasa.jpl.mbee.patternloader;

import com.nomagic.actions.ActionsCategory;
import com.nomagic.actions.ActionsManager;
import com.nomagic.magicdraw.actions.ConfiguratorWithPriority;
import com.nomagic.magicdraw.actions.DiagramContextAMConfigurator;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;

/**
 * A class for configurating the Pattern Loader right-click menu option.
 *
 * @author Benjamin Inada, JPL/Caltech
 */
public class PatternLoaderConfigurator implements DiagramContextAMConfigurator {
    /**
     * Configures the element right-click menu option.
     *
     * @param manager   the actions manager to add the category to.
     * @param diagram   the diagram to configurate.
     * @param selected  the selected elements.
     * @param requester the element that was right-clicked
     */
    @Override
    public void configure(ActionsManager manager, DiagramPresentationElement diagram,
                          PresentationElement[] selected, PresentationElement requester) {
        // check if the category was already added to the manager
        if (manager.getActionFor("PatternLoader") != null) {
            return;
        }

        // check that the requester is good for either an element or diagram
        if (PatternLoaderUtils.isGoodElementRequester(requester)) {
            ActionsCategory category = new ActionsCategory("Pattern Loader", "Pattern Loader");
            category.addAction(new PatternLoader("PatternLoader", "Pattern Loader", 0, null, requester));

            manager.addCategory(1, category);
        }
        else if ((requester == null) && (PatternLoaderUtils.isGoodElementRequester(diagram))) {
            ActionsCategory category = new ActionsCategory("Pattern Loader", "Pattern Loader");
            category.addAction(new PatternLoader("PatternLoader", "Pattern Loader", 0, null, requester));

            manager.addCategory(1, category);
        }
    }

    /**
     * Gets the priority of the configurator.
     *
     * @return the priority.
     */
    @Override
    public int getPriority() {
        return ConfiguratorWithPriority.MEDIUM_PRIORITY;
    }
}
