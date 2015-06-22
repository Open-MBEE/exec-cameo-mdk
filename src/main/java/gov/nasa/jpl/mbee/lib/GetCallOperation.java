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
package gov.nasa.jpl.mbee.lib;

import gov.nasa.jpl.mbee.DocGenUtils;
import gov.nasa.jpl.mbee.lib.CollectionAdder;
import gov.nasa.jpl.mbee.lib.EmfUtils;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.lib.Utils2;
import gov.nasa.jpl.mbee.lib.Utils.AvailableAttribute;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Slot;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

/**
 * A CallOperation implementing a blackbox function extension of the OCL library
 * that accesses (gets) data in some specified relation to some specified
 * object.
 * 
 */
public class GetCallOperation implements CallOperation {

    public enum CallReturnType {
        SELF, NAME, TYPE, VALUE, MEMBER, RELATIONSHIP, OWNER
    };

    private boolean       collect                    = true;                     // TODO
    public boolean        filter                     = true;                     // REVIEW
                                                                                  // --
                                                                                  // should
                                                                                  // always
                                                                                  // (collect
                                                                                  // ==
                                                                                  // !filter)?
    public boolean        onlyOneForAll              = false;
    public boolean        onlyOnePer                 = false;
    public int            recursionDepth             = 1;

    // List handling
    public boolean        mustFlatten                = false;
    public boolean        mayFlatten                 = false;
    public boolean        flattenIfSizeOne           = false;
    public boolean        flattenToNullIfEmpty       = false;
    public int            defaultFlattenDepth        = 1;
    public boolean        nullOk                     = true;

    public Class<?>       unflattenedCollectionType  = ArrayList.class;

    public boolean        asElement                  = true;
    public boolean        asEObject                  = true;
    public boolean        asObject                   = true;
    public boolean        asCollection               = true;
    public boolean        useName                    = true;
    public boolean        useType                    = true;
    public boolean        useValue                   = true;
    private boolean       matchNull                  = true;                     // TODO
    private boolean       activityEdgeIsRelationship = true;                     // TODO

    /**
     * Always filter on these; i.e. collected elements should match all Objects
     * in alwaysFilter.
     */
    public Object[]       alwaysFilter               = null;

    public CallReturnType resultType                 = CallReturnType.SELF;

    public GetCallOperation(CallReturnType opType, boolean onlyOneForAll, boolean onlyOnePer) {
        super();
        this.resultType = opType;
        this.onlyOneForAll = onlyOneForAll;
        this.onlyOnePer = onlyOnePer;
    }

    public GetCallOperation() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.ocl.CallOperation#callOperation(java.lang.Object,
     * java.lang.Object[])
     */
    @Override
    public Object callOperation(Object source, Object[] args) {
        CollectionAdder adder = new CollectionAdder(mustFlatten, mayFlatten, flattenIfSizeOne,
                flattenToNullIfEmpty, defaultFlattenDepth, nullOk, onlyOneForAll, unflattenedCollectionType);
        List<Object> resultList = new ArrayList<Object>();
        if (source == null)
            return resultList;
        Object[] filterArgs = Utils2.join(alwaysFilter, args);
        if (filter)
            filter = !Utils2.isNullOrEmpty(filterArgs);
        Element elem = (source instanceof Element ? (Element)source : null);
        Collection<?> coll = (source instanceof Collection ? (Collection<?>)source : null);
        Object objectToAdd = null;
        boolean loop = coll != null && asCollection && recursionDepth > 0;
        boolean filterAlreadyUsed = false;
        // boolean doingAdd = true;
        switch (resultType) {
            case SELF:
                objectToAdd = source;
                // filter = false;
                // if ( EmfUtils.matches( source, useName, useType, args ) ) {
                // objectToAdd = source;
                // // } else {
                // // doingAdd = false;
                // }
                // added = adder.add( source, resultList );
                break;
            case OWNER:
                if (loop) {
                    objectToAdd = source;
                } else {
                    if ( !( source instanceof Element ) ) {
                        objectToAdd = null;
                    } else {
                        List<Element> owners = new ArrayList< Element >();
                        Element owner = ((Element)source).getOwner();
                        while ( owner != null ) {
                            owners.add( owner );
                            if ( onlyOneForAll || onlyOnePer ) {
                                break;
                            }
                            owner = owner.getOwner(); 
                        }
                        objectToAdd = owners;
                    }
                }
                break;
            case NAME:
                // if ( onlyOnePer )
                if (loop) {
                    objectToAdd = source;
                } else {
                    objectToAdd = EmfUtils.getName(source);
                }
                // if ( filter && !EmfUtils.matches( objectToAdd, useName,
                // useType, args ) ) {
                // objectToAdd = null;
                // }
                // added = adder.add( name, resultList );
                break;
            case TYPE: // TODO -- use asElement, asEObject, asElement!! Need to
                       // pass thru to EmfUtils?
                if (loop) {
                    objectToAdd = source;
                } else {
                    if ((onlyOnePer || onlyOneForAll) && Utils2.isNullOrEmpty(filterArgs)) {
                        // objectToAdd = EmfUtils.getTypeName( source );
                        objectToAdd = EmfUtils.getType(source);
                        if (!Utils2.isNullOrEmpty(objectToAdd)) {
                            break;
                        }
                    } else {
                        objectToAdd = EmfUtils.getTypes(source);
                    }
                    if (asElement && elem != null) {
                        // Stereotypes
                        List<Stereotype> sTypes = StereotypesHelper.getStereotypes(elem);
                        if (!Utils2.isNullOrEmpty(sTypes) && objectToAdd instanceof Collection) {
                            Collection<Object> c = (Collection<Object>)objectToAdd;
                            for (Stereotype s: sTypes) {
                                if (!c.contains(s))
                                    c.add(s);
                                if ((onlyOnePer || onlyOneForAll) && c.size() > 0
                                        && Utils2.isNullOrEmpty(filterArgs)) {
                                    break;
                                }
                            }
                            if ((onlyOnePer || onlyOneForAll) && c.size() > 0
                                    && Utils2.isNullOrEmpty(filterArgs)) {
                                break;
                            }
                        } else {
                            List<Object> list = Utils2.newList(sTypes.toArray());
                            if (objectToAdd != null) {
                                list.add(0, objectToAdd);
                            }
                            objectToAdd = list;
                            if ((onlyOnePer || onlyOneForAll) && list.size() > 0
                                    && Utils2.isNullOrEmpty(filterArgs)) {
                                break;
                            }
                        }

                        // Metaclasses -- TODO -- !!!!
                        // elem.m
                        // StereotypesHelper.getM

                    }
                }
                break;
            case VALUE:
                if (loop) {
                    objectToAdd = source;
                } else {
                    objectToAdd = null;
                    // If arguments were passed, then treat them as names of properties in source.
                    if (source instanceof Element && !Utils2.isNullOrEmpty(args)) {
                        List<Object> objects = new ArrayList< Object >();
                        for ( Object arg : args ) {
                            Property prop = null;
                            List<Object> propVals = null;
                            if ( arg instanceof String ) {
                                // TODO -- REVIEW -- should this be addAll or add?
                                propVals = Utils.getElementPropertyValues( (Element)source,
                                                                           (String)arg,
                                                                           true );
                            } else if ( arg instanceof Property ) {
                                prop = (Property)arg;
                                propVals = Utils.getElementPropertyValues( (Element)source,
                                                                           prop,
                                                                           true );
                            }
                            if ( !Utils2.isNullOrEmpty( propVals ) ) {
                                filterAlreadyUsed = true;
                                objects.addAll( propVals );
                            }
                        }
                        if ( !objects.isEmpty() ) {
                            objectToAdd = objects;
                        }
                    }
                    boolean one = !filter && (onlyOneForAll || (asCollection && coll != null && onlyOnePer));

                    // If the source is a Property or slot, get its value 
                    if ( Utils2.isNullOrEmpty( objectToAdd )
                         && ( source instanceof Property || source instanceof Slot ) ) {
                        objectToAdd =
                                Utils.getElementAttribute( (Element)source,
                                                           AvailableAttribute.Value );
                    }
                    if ( Utils2.isNullOrEmpty( objectToAdd )
                         && source instanceof ValueSpecification ) {
                        objectToAdd =
                                DocGenUtils.getLiteralValue( source, true );
                    }
                    // Handle onlyOne.
                    if ( !Utils2.isNullOrEmpty( objectToAdd ) ) { 
                        if ( one && objectToAdd instanceof Collection ) {
                            Object first = ( (Collection<?>)objectToAdd ).iterator().next();
                            objectToAdd = Utils2.newList( first );
                        }
                    } else {
                        // Last resort -- try to find a member that looks like it would return a value
//                        boolean one = (onlyOneForAll || (asCollection && coll != null && onlyOnePer))
//                                && Utils2.isNullOrEmpty(filterArgs);
                        objectToAdd = EmfUtils.getValues(source, null, true, true, one, false, null);
                    }
                    if ( Utils2.isNullOrEmpty( objectToAdd ) &&
                         Utils2.isNullOrEmpty(args) ) {
                        objectToAdd = source;
                    }
                }
                break;
            case MEMBER:
                if (loop) {
                    objectToAdd = source;
                } else {
                    if (asElement && elem != null) {
                        ArrayList<Element> members = new ArrayList<Element>();
                        if (elem.getOwnedElement() != null)
                            members.addAll(elem.getOwnedElement());
                        members.addAll(Utils.getSlots(elem));
                        objectToAdd = members;
                        // } else if ( coll != null && !asCollection ) {
                        // objectToAdd = source;
                    } else if (asEObject && source instanceof EObject) {
                        EList<EObject> elist = ((EObject)source).eContents();
                        objectToAdd = elist;
                    } else if (asObject) {
                        objectToAdd = EmfUtils.getFieldValues(source, false);
                        // objectToAdd = EmfUtils.getMemberValues( source, null,
                        // true, false, onlyOnePer || onlyOneForAll,
                        // (String[])null );
                    }
                }
                break;
            case RELATIONSHIP:
                if (!loop) {
                    if (asElement && elem != null) {
                        objectToAdd = EmfUtils.getRelationships(elem);
                    } else {
                        // REVIEW -- TODO -- asEObject???!
                        // REVIEW -- TODO -- ActivityEdge?
                        // REVIEW -- TODO -- complain???!
                    }
                } else {
                    objectToAdd = source;
                }
                break;
            default:
        }
        boolean isCollection = objectToAdd instanceof Collection;
        if (loop) {
            ArrayList<Object> list = new ArrayList<Object>();
            --adder.defaultFlattenDepth;
            --recursionDepth;
            for (Object o: coll) {
                Object result = callOperation(o, filterArgs);
                adder.add(result, list);
            }
            ++recursionDepth;
            ++adder.defaultFlattenDepth;
            objectToAdd = list;
        } else {
            // TODO -- apply filter while collecting above for efficiency in
            // case returning only one!
            // REVIEW -- this todo above may already be done
            if (filter && !filterAlreadyUsed) {
                if (!Utils2.isNullOrEmpty(args)) {
                    objectToAdd = EmfUtils.collectOrFilter(adder, objectToAdd, !filter,
                            (onlyOneForAll || (isCollection && onlyOnePer)), useName, useType, useValue,
                            asObject, args);
                }
                if (!Utils2.isNullOrEmpty(alwaysFilter)) {
                    objectToAdd = EmfUtils.collectOrFilter(adder, objectToAdd, false,
                            (onlyOneForAll || (isCollection && onlyOnePer)), useName, useType, useValue,
                            asObject, alwaysFilter);
                }
            }
        }
        if (objectToAdd instanceof Collection) {
            objectToAdd = adder.fix((Collection<?>)objectToAdd);
        }
        return objectToAdd;
    }

}