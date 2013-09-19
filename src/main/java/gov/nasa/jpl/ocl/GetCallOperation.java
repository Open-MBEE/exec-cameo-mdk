package gov.nasa.jpl.ocl;

import gov.nasa.jpl.mbee.lib.EmfUtils;
import gov.nasa.jpl.mbee.lib.CollectionAdder;
import gov.nasa.jpl.mbee.lib.Utils2;
import gov.nasa.jpl.mgss.mbee.docgen.DocGenUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

/**
 * A CallOperation implementing a blackbox function extension of the OCL library
 * that accesses (gets) data in some specified relation to some specified
 * object.
 * 
 */
public class GetCallOperation implements CallOperation {

  public enum CallReturnType { SELF, NAME, TYPE, VALUE, MEMBER, RELATIONSHIP };
  
  private boolean collect = true; // TODO
  public boolean filter = true; // REVIEW -- should always (collect == !filter)?
  public boolean onlyOneForAll = false;
  public boolean onlyOnePer = false;
  public int recursionDepth = 1;

  // List handling
  public boolean mustFlatten = false;
  public boolean mayFlatten = false;
  public boolean flattenIfSizeOne = false;
  public boolean flattenToNullIfEmpty = false;
  public int defaultFlattenDepth = 1;
  public boolean nullOk = true;

  public Class<?> unflattenedCollectionType = (Class< ? >)ArrayList.class;
  
  public boolean asElement = true;
  public boolean asEObject = true;
  public boolean asObject = true;
  public boolean asCollection = true;
  public boolean useName = true;
  public boolean useType = true;
  public boolean useValue = true;
  private boolean matchNull = true; // TODO
  private boolean activityEdgeIsRelationship = true; // TODO

  public Object[] alwaysFilter = null;
  
  public CallReturnType resultType = CallReturnType.SELF;
  
  public GetCallOperation() {
    super();
  }
  
  /* (non-Javadoc)
   * @see gov.nasa.jpl.ocl.CallOperation#callOperation(java.lang.Object, java.lang.Object[])
   */
  @Override
  public Object callOperation( Object source, Object[] args ) {
    CollectionAdder adder =
        new CollectionAdder( mustFlatten, mayFlatten, flattenIfSizeOne,
                             flattenToNullIfEmpty, defaultFlattenDepth, nullOk,
                             onlyOneForAll, unflattenedCollectionType );
    List< Object > resultList = new ArrayList< Object >();
    if ( source == null ) return resultList;
    if ( recursionDepth < 0 ) return resultList;
    Object[] filterArgs = Utils2.join( alwaysFilter, args );
    if ( filter ) filter = !Utils2.isNullOrEmpty( filterArgs );
    Element elem = ( source instanceof Element ? (Element)source : null );
    Collection< ? > coll =
        ( source instanceof Collection ? (Collection< ? >)source : null );
    Object objectToAdd = null;
    boolean loop = false;
    //boolean doingAdd = true;
      switch (resultType) {
        case SELF:
          objectToAdd = source;
//          filter = false;
//          if ( EmfUtils.matches( source, useName, useType, args ) ) {
//            objectToAdd = source;
////          } else {
////            doingAdd = false;
//          }
          //added = adder.add( source, resultList );
          break;
        case NAME:
          //if ( onlyOnePer )
          loop = coll != null && asCollection;
          if ( loop ) {
            objectToAdd = source;
          } else {
            objectToAdd = EmfUtils.getName( source );
          }
//          if ( filter && !EmfUtils.matches( objectToAdd, useName, useType, args ) ) {
//            objectToAdd = null;
//          }
          //added = adder.add( name, resultList );
          break;
        case TYPE: // TODO -- Using asElement, asEObject, asElement??!!  Need to pass thru to EmfUtils
          loop = coll != null && asCollection;
          if ( loop ) {
            objectToAdd = source;
          } else {
            if ( onlyOnePer || onlyOneForAll ) {
              //objectToAdd = EmfUtils.getTypeName( source );
              objectToAdd = EmfUtils.getType( source );
              if ( !Utils2.isNullOrEmpty( objectToAdd ) ) {
                  break;
              }
            } else {
              objectToAdd = EmfUtils.getTypes( source );
            }
            if ( asElement && elem != null ) {
              // Stereotypes
              List< Stereotype > sTypes = StereotypesHelper.getStereotypes(elem);
              if ( objectToAdd instanceof Collection ) {
                  Collection<Object> c = (Collection<Object>)objectToAdd;
                  for ( Stereotype s : sTypes ) {
                      if ( !c.contains( s ) ) c.add( s );
                      if ( (onlyOnePer || onlyOneForAll) && c.size() > 0) {
                          break;
                      }
                  }
                  if ( (onlyOnePer || onlyOneForAll) && c.size() > 0) {
                      break;
                  }
              } else {
                  List<Object> list = Utils2.newList( sTypes.toArray() );
                  if ( objectToAdd != null ) {
                      list.add( 0, objectToAdd );
                  }
                  objectToAdd = list;
                  if ( (onlyOnePer || onlyOneForAll) && list.size() > 0) {
                      break;
                  }
              }

              // Metaclasses -- TODO -- !!!!
//              elem.m
//              StereotypesHelper.getM

            }
          }
          break;
        case VALUE:
          loop = coll != null && asCollection;
          if ( loop ) {
            objectToAdd = source;
          } else {
            boolean one = onlyOneForAll || ( asCollection && coll != null && onlyOnePer );
            objectToAdd = EmfUtils.getValues( source, null, true, true, one,
                                              false, null );
          }
          break;
        case MEMBER:
          loop = coll != null && asCollection;
          if ( loop ) {
            objectToAdd = source;
          } else {
            if ( asElement && elem != null ) {
              objectToAdd = elem.getOwnedElement();
//            } else if ( coll != null && !asCollection ) {
//              objectToAdd = source;
            } else if ( asEObject && source instanceof EObject ) {
              EList< EObject > elist = ((EObject)source).eContents();
              objectToAdd = elist;
            } else if ( asObject ) {
              objectToAdd = EmfUtils.getFieldValues( source, false );
              //objectToAdd = EmfUtils.getMemberValues( source, null, true, false, onlyOnePer || onlyOneForAll, (String[])null );
            }
          }
          break;
        case RELATIONSHIP:
          loop = coll != null && asCollection;
          if ( !loop ) {
            if ( asElement && elem != null ) {
              objectToAdd = EmfUtils.getRelationships( elem );
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
      if ( loop ) {
        ArrayList< Object > list = new ArrayList< Object >();
        --adder.defaultFlattenDepth;
        --recursionDepth;
        for ( Object o : coll ) {
          Object result = callOperation( o, filterArgs );
          adder.add( result, list );
        }
        ++recursionDepth;
        ++adder.defaultFlattenDepth;
        objectToAdd = list;
      } else {
        if ( filter ) {
          objectToAdd =
              EmfUtils.collectOrFilter( adder, objectToAdd, !filter,
                                        ( onlyOneForAll || ( isCollection && onlyOnePer ) ),
                                        useName, useType, useValue, asObject,
                                        filterArgs );
        }
      }
      if ( objectToAdd instanceof Collection ) {
        objectToAdd = adder.fix( (Collection< ? >)objectToAdd );
      }
//      if ( objectToAdd instanceof Collection ) {
//        return CollectionUtil.asSequence( (Collection< ? >)objectToAdd );
//      }
      return objectToAdd;
  }
  
}