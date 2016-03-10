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

import static gov.nasa.jpl.mbee.web.sync.CommentUtil.DOCUMENT_COMMENT;
import gov.nasa.jpl.mbee.lib.Utils;

import java.beans.PropertyChangeEvent;
import java.util.Collection;

import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.impl.PropertyNames;
import com.nomagic.uml2.transaction.TransactionCommitListener;

/**
 * Responds to changes in document comments. Could theoretically be implemented
 * as a PropertyChangeListener instead of TransactionCommitListener, but it's
 * easier to get the right undo/redo behavior this way. As a
 * TransactionCommitListener, its changes are made within the same
 * session/transaction as the triggering change.
 */
@Deprecated
public class CommentChangeListener implements TransactionCommitListener {
    /**
     * Allow listener to be disabled during imports, to prevent automatic update
     * of timestamps.
     */
    private static boolean disabled = false;

    public static void disable() {
        disabled = true;
    }

    public static void enable() {
        disabled = false;
    }

    @Override
    public Runnable transactionCommited(final Collection<PropertyChangeEvent> events) {
        if (disabled) {
            return new EmptyRunnable();
        }
        return new TransactionCommitHandler(events);
    }

    /** Does nothing. Used when this listener is temporarily disabled. */
    private class EmptyRunnable implements Runnable {
        @Override
        public void run() {
        }
    }

    /**
     * Adapter to call handleChangeEvent() from the TransactionCommitListener
     * interface.
     */
    private class TransactionCommitHandler implements Runnable {
        private final Collection<PropertyChangeEvent> events;

        TransactionCommitHandler(final Collection<PropertyChangeEvent> events) {
            this.events = events;
        }

        @Override
        public void run() {
            for (PropertyChangeEvent e: events) {
                handleChangeEvent(e);
            }
        }
    }

    private void handleChangeEvent(PropertyChangeEvent event) {
        if (!(event.getSource() instanceof Comment)) {
            return;
        }
        Comment c = (Comment)event.getSource();
        String eventPropertyName = event.getPropertyName();

        if (PropertyNames.BODY.equals(eventPropertyName)) {
            handleBodyChange(c);
        }
        // This could also listen for PropertyNames.ANNOTATED_ELEMENT,
        // to react when a comment connects or disconnects its anchor
        // to/from an element.
    }

    private void handleBodyChange(Comment comment) {
        // apply stereotype before updating tags,
        // so this works properly for new comments
        if (inDocument(comment)) {
            new ApplyDocumentCommentStereotype(comment).run();
        }
        if (StereotypesHelper.hasStereotypeOrDerived(comment, DOCUMENT_COMMENT)) {
            new UpdateCommentTags(comment).run();
        }

    }

    private boolean inDocument(Comment comment) {
        Stereotype sysmlView = Utils.getViewStereotype();
        if (ModelHelper.getComment(comment.getOwner()).equals(comment.getBody()))
            return false; // this is to prevent adding stereotype to
                          // documentation of elements - magicdraw treats
                          // element documentation as comments too
        return StereotypesHelper.hasStereotypeOrDerived(comment.getOwner(), sysmlView);
        // DOCUMENT_VIEW.equals(comment.getOwner().getHumanType());
    }

    // Some stuff that might be useful for debug logging:
    //
    // import com.nomagic.magicdraw.core.Application;
    // import com.nomagic.magicdraw.core.GUILog;
    //
    // private void showParent(Comment comment) {
    // GUILog log = Application.getInstance().getGUILog();
    // log.log("         owner:  " + comment.getOwner().getHumanType()
    // + " '" + comment.getOwner().getHumanName() + "'");
    // log.log("         parent: " + comment.getObjectParent().getHumanType()
    // + " '" + comment.getObjectParent().getHumanName() + "'");
    // }
    //
    // private String debugString(PropertyChangeEvent e) {
    // String debug = e.getSource().getClass().getName() + " " +
    // e.getPropertyName() + ": "
    // + e.getOldValue().getClass().getName()
    // + e.getOldValue() + " -> " + e.getNewValue() + " [" +
    // e.getPropagationId() + "]";
    // return debug;
    // }
}
