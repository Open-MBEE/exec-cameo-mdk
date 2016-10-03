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
package gov.nasa.jpl.mbee.mdk.model.actions;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import gov.nasa.jpl.mbee.mdk.DgviewDBSwitch;
import gov.nasa.jpl.mbee.mdk.dgview.MDEditableTable;
import gov.nasa.jpl.mbee.mdk.model.UserScript;
import gov.nasa.jpl.mbee.mdk.docgen.table.EditableTable;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;

public class RunUserEditableTableAction extends MDAction {

    private static final long serialVersionUID = 1L;
    private UserScript scripti;

    public RunUserEditableTableAction(UserScript us) {
        super(null, "Run Editable Script", null, null);
        scripti = us;
        String name = scripti.getStereotypeName();
        if (name != null) {
            this.setName("Edit " + name + " Table");
        }
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        GUILog log = Application.getInstance().getGUILog();
        Map<?, ?> o = scripti.getScriptOutput(null);
        if (o != null && o.containsKey("EditableTable")) {
            Object l = o.get("EditableTable");
            if (l instanceof EditableTable) {
                ((EditableTable) l).showTable();
            }
        }
        else if (o != null && o.containsKey("editableTable")) {
            if (o.get("editableTable") instanceof List) {
                for (Object object : (List<?>) o.get("editableTable")) {
                    if (object instanceof MDEditableTable) {
                        DgviewDBSwitch.convertEditableTable((MDEditableTable) object).showTable();
                    }
                }
            }
        }
        else {
            log.log("script has no editable table output!");
        }
    }
}
