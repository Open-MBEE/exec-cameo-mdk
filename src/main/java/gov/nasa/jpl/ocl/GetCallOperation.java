package gov.nasa.jpl.ocl;

import gov.nasa.jpl.mbee.lib.EmfUtils;
import gov.nasa.jpl.mbee.lib.CollectionAdder;
import gov.nasa.jpl.mbee.lib.Utils2;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class GetCallOperation implements CallOperation {

  public enum CallReturnType { SELF, NAME, TYPE, VALUE, MEMBER, RELATIONSHIP };
  
  public boolean collect = true;
  public boolean filter = true; // REVIEW -- should always (collect == !filter)?
  public boolean onlyOneForAll = false;
  public boolean onlyOnePer = true;
  public int recursionDepth = 1;

  // List handling
  public boolean mustFlatten = false;
  public boolean mayFlatten = false;
  public boolean flattenIfSizeOne = false;
  public boolean flattenToNullIfEmpty = false;
  public int defaultFlattenDepth = 1;
  public boolean nullOk = true;

  public Class<? extends Collection<?>> unflattenedCollectionType =
      (Class< ? extends Collection< ? >>)ArrayList.class;
  
  public boolean asElement = true;
  public boolean asEObject = true;
  public boolean asObject = true;
  public boolean asCollection = true;
  public boolean useName = true;
  public boolean useType = true;
  public boolean useValue = true;
  public boolean matchNull = true; 

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
    if ( filter ) filter = !Utils2.isNullOrEmpty( args );
    Element elem = ( source instanceof Element ? (Element)source : null );
    Collection< ? > coll =
        ( source instanceof Collection ? (Collection< ? >)source : null );
    boolean one = onlyOneForAll || ( asCollection && coll != null && onlyOnePer );
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
        case TYPE:
          loop = coll != null && asCollection;
          if ( loop ) {
            objectToAdd = source;
          } else {
            if ( onlyOnePer || onlyOneForAll ) {
              objectToAdd = EmfUtils.getTypeName( source );
            } else {
              objectToAdd = EmfUtils.getTypes( source );
            }
          }
          break;
        case VALUE:
          loop = coll != null && asCollection;
          if ( loop ) {
            objectToAdd = source;
          } else {
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
        for ( Object o : coll ) {
          Object result = callOperation( o, args );
          adder.add( result, list );
        }
        ++adder.defaultFlattenDepth;
        objectToAdd = list;
      } else {
        if ( filter ) {
          objectToAdd =
              EmfUtils.collectOrFilter( adder, objectToAdd, !filter,
                                        ( onlyOneForAll || ( isCollection && onlyOnePer ) ),
                                        useName, useType, useValue, asObject,
                                        args );
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