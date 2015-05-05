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

import gov.nasa.jpl.mbee.generator.Generatable;
import gov.nasa.jpl.mbee.lib.ClassUtils;
import gov.nasa.jpl.mbee.lib.MoreToString;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

/**
 * <p>
 * This class should be extended if writing java extensions, or any of its
 * subclass like Table
 * </p>
 * 
 * @author dlam
 * 
 */
public abstract class Query extends DocGenElement implements Generatable {
    /**
     * The elements passed into this query. These are magicdraw elements
     * resulting from collect/filter/sort actions
     */
    protected List<Object> targets;
    protected List<String>  titles;
    protected boolean       sortElementsByName = false;

    public void setTargets(List<Object> t) {
        targets = t;
    }

    public List<Object> getTargets() {
        return targets;
    }

    public void setTitles(List<String> t) {
        titles = t;
    }

    public List<String> getTitles() {
        return titles;
    }

    public boolean isSortElementsByName() {
        return sortElementsByName;
    }

    public void setSortElementsByName(boolean sortElementsByName) {
        this.sortElementsByName = sortElementsByName;
    }

    /**
     * This is called after the query object has been constructed and the
     * targets and dgElement fields are set
     */
    @Override
    public void initialize() {

    }

    /**
     * This method must be overidden by subclasses to return the result of the
     * query
     */
    @Override
    public List<DocumentElement> visit(boolean forViewEditor, String outputDir) {
        return new ArrayList<DocumentElement>();
    }

    /**
     * This is called after initialize
     */
    @Override
    public void parse() {

    }

    /**
     * <p>
     * These actions will show up as menu items under View Interaction, if the
     * user right clicks on a view that will execute this query
     * </p>
     * <p>
     * targets and dgElement would have been filled
     * </p>
     */
    @Override
    public List<MDAction> getActions() {
        return new ArrayList<MDAction>();
    }

    @Override
    public void accept(IModelVisitor visitor) {
        visitor.visit(this);
    }
    
    protected static HashSet<Field> notToStringSet = new HashSet< Field >() {
            private static final long serialVersionUID = -2943965696220565323L;
            {
                try{
                    //add(Query.class.getField( "sortElementsByName" ));
                } catch( Exception e ) {
                    e.printStackTrace();
                }
            }
    };
    protected Set<Field> notToString() {
        return notToStringSet;
    }
    
    @Override
    public String toStringStart() {
        StringBuffer sb = new StringBuffer();
        sb.append( super.toStringStart() );
        for ( Field f : getClass().getDeclaredFields() ) {
            if ( notToString().contains( f ) ) {
                continue;
            }
            f.setAccessible( true );
            try {
                sb.append( "," + f.getName() + "=" + f.get( this ) );
            } catch ( IllegalArgumentException e ) {
            } catch ( IllegalAccessException e ) {
            }
        }
        return sb.toString();
    }

}
