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
package gov.nasa.jpl.mbee.web.sync;

import static gov.nasa.jpl.mbee.web.sync.CommentUtil.AUTHOR;
import static gov.nasa.jpl.mbee.web.sync.CommentUtil.DOCUMENT_COMMENT;
import static gov.nasa.jpl.mbee.web.sync.CommentUtil.MODIFIED_TIMESTAMP;
import static gov.nasa.jpl.mbee.web.sync.CommentUtil.TIME_FORMAT;

import java.util.Date;

import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

public class UpdateCommentTags extends ChangeTheModel {
    private final Comment comment;

    public UpdateCommentTags(Comment comment) {
        this.comment = comment;
    }

    @Override
    public String getDescription() {
        return "Update DocumentComment Timestamp";
    }

    @Override
    protected void makeChange() {
        if (comment == null) {
            fail("Comment not properly set for action");
            return;
        }
        Stereotype stereotype = getStereotype(DOCUMENT_COMMENT);
        if (stereotype == null) {
            fail("Couldn't find " + DOCUMENT_COMMENT + " stereotype");
            return;
        }

        String timestamp = TIME_FORMAT.format(new Date());
        StereotypesHelper.setStereotypePropertyValue(comment, stereotype, MODIFIED_TIMESTAMP, timestamp);

        String username = getUsername();
        if (!username.isEmpty()) {
            StereotypesHelper.setStereotypePropertyValue(comment, stereotype, AUTHOR, username);
        }
    }
}
