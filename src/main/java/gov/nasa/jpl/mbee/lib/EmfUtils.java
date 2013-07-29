package gov.nasa.jpl.mbee.lib;


//import gov.nasa.jpl.ae.event.Expression;
//import gov.nasa.jpl.ae.magicdrawPlugin.modelQuery.ModelReference;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.Assert;


//import javax.measure.quantity.Duration;
//import javax.measure.unit.SI;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.ETypedElement;
import org.eclipse.emf.ecore.impl.EClassifierImpl;
import org.eclipse.emf.ecore.impl.ENamedElementImpl;

import com.io_software.jmi.util.Util;

public final class EmfUtils {

  public static String spewIndentCharacters = "-> ";
  public static String spewObjectPrefix = "* * * * *";
  public static String spewObjectSuffix = spewObjectPrefix;

  public static String writeNameAndTypeOfEObject(Object o, String indent) {
    StringBuffer sb = new StringBuffer();
    sb.append(indent + spewObjectPrefix + "\n");
    EObject eo = (EObject)( o instanceof EObject ? o : null );
    if ( eo != null ) {
      sb.append(indent + "EClass: " + eo.eClass() + "\n");
    } else {
      sb.append(indent + "Class: " + o.getClass().getSimpleName() + "\n");
    }
    sb.append(indent + "name: " + getName( o ) + "\n");
    sb.append(indent + "type: " + getTypeName( o ) + "\n");
    sb.append(indent + spewObjectSuffix + "\n");
    return sb.toString();
  }

  
  public static String spewFields( Object o, int thisLevel, int maxDepth,
                                   boolean justNameType, Set< Object > seen ) {
    Pair< Boolean, Set< Object > > p = Utils2.seen( o, true, seen );
    if ( p.first ) return "";
    seen = p.second;

    StringBuffer sb = new StringBuffer();
    //sb.append( spewFields( o, indent ) );
    Field[] fields = o.getClass().getFields();
    String indent = chain( spewIndentCharacters, thisLevel );
    for (Field f : fields) {
      f.setAccessible( true );
      Object r;
      try {
        r = f.get( o );
        if ( r != null ) {
          if ( ClassUtils.isPrimitive( r ) ) return "";
          sb.append( indent + f.getName() + ":\n" );
          sb.append( spewContents( r, thisLevel + 1, maxDepth, justNameType, seen ) );
        }
      } catch ( IllegalArgumentException e ) {
      } catch ( IllegalAccessException e ) {}
    }
    return sb.toString();
  }
  public static String spewFields( Object o, String indent ) {
    StringBuffer sb = new StringBuffer();
    Field[] fields = o.getClass().getFields();
    for (Field f : fields) {
      f.setAccessible( true );
      try {
        Object v = f.get( o );
        sb.append( indent + f.getType().getSimpleName() + " " + f.getName() + " = " );
        sb.append( v );
        sb.append( "\n" );
      } catch ( IllegalArgumentException e ) {
      } catch ( IllegalAccessException e ) {}
    }
    return sb.toString();
  }

  public static String spewMethods( Object o, String indent ) {
    StringBuffer sb = new StringBuffer();
    Class<?> c = o.getClass();
    Method[] methods = c.getMethods();
    for (Method m : methods) {
      m.setAccessible( true );
      if (m.getReturnType() == void.class || m.getReturnType() == null
          || m.getName().startsWith("wait")
          || m.getName().startsWith("notify")
          || m.getName().startsWith("remove")
          || m.getName().startsWith("delete") ) {
        continue;
      }
      if (m.getParameterTypes().length == 0) {
          sb.append(indent + m.getDeclaringClass() + ", "
              + m.toGenericString() + " --> "
              + ClassUtils.runMethod(true, o, m).second + "\n");
      }
    }
    return sb.toString();
  }
  
  public static String spewObject(Object o, String indent) {
    StringBuffer sb = new StringBuffer();
    sb.append(indent + spewObjectPrefix + "\n");
    sb.append( spewFields( o, indent ) );
    sb.append( spewMethods( o, indent ) );
    sb.append(indent + spewObjectSuffix + "\n");
    return sb.toString();
    // System.out.println( "EObject.eAllContents()=" + o.eAllContents() +
    // "\n"
    // );
  }

  /**
   * Create a chain, repeating String s the number of times specified by length.
   * @param s
   * @param length
   * @return the chain
   */
  protected static String chain( String s, int length ) {
    StringBuffer sb = new StringBuffer();
    for ( int i = 0; i < length; ++i ) {
      sb.append( s );
    }
    return sb.toString();
  }
  
  public static String spewContents(Object o, int thisLevel, int maxDepth,
                                    boolean justNameType, Set<Object> seen) {
    Pair< Boolean, Set< Object > > p = Utils2.seen( o, true, seen );
    if ( p.first ) return "";
    seen = p.second;
    
    if ( ClassUtils.isPrimitive( o ) ) return "";

    StringBuffer sb = new StringBuffer();
    String indent = chain( spewIndentCharacters, thisLevel );

//    sb.append(indent + spewObjectPrefix + " "
//        + getName( o ) + " " + getTypeName( o ) + " "
//        + spewObjectSuffix);

    //    StringBuffer indent = new StringBuffer();
//    for (int i = 0; i < thisLevel; ++i) {
//      indent.append(spewIndentCharacters);
//    }
    sb.append(writeNameAndTypeOfEObject(o, indent));
    if (!justNameType) {
      sb.append(spewObject(o, indent));
    }
    if (thisLevel < maxDepth) {
      seen.remove( o );
      sb.append(spewFields( o, thisLevel + 1, maxDepth, justNameType, seen ) );
      if ( o instanceof EObject ) {
        Iterator< EObject > iter = ( (EObject)o ).eContents().iterator();
        while ( iter.hasNext() ) {
          sb.append( spewContents( iter.next(), thisLevel + 1, maxDepth,
                                   justNameType, seen ) );
        }
      }
    }
    return sb.toString();
  }

  public static String spewContents( Object o, int maxDepth,
                                     boolean justNameType, Set< Object > seen ) {
//    Pair< Boolean, Set< Object > > p = Utils2.seen( o, true, seen );
//    if ( p.first ) return "";
//    seen = p.second;
    if ( o == null ) return null;
    
    StringBuffer sb = new StringBuffer();

//    //String name = getName( def );
//    //String type = getTypeName( def );
//    sb.append(spewObjectPrefix + " "
//        + getName( def ) + " "
//        + spewObjectSuffix);

    sb.append( spewContents( o, 0, maxDepth, justNameType, seen ) );
    return sb.toString();
  }

  public static String spew( Object o ) {
    return spewContents( o, 3, false, null );
  }

  public static String spewContents(Collection<?> objs,
      int maxDepth, boolean justNameType, Set< Object > seen) {
    Pair< Boolean, Set< Object > > p = Utils2.seen( objs, true, seen );
    if ( p.first ) return "";
    seen = p.second;

    StringBuffer sb = new StringBuffer();
    sb.append("\n <<<<< LEVEL = " + maxDepth + " >>>>>\n\n");
    for (Object o : objs) {
      sb.append(spewContents(o, maxDepth, justNameType, seen));
    }
    return sb.toString();
  }

  /**
   * @param specifier
   * @return
   */
  public static List< String > getPossibleFieldNames( String specifier ) {
    List< String > possibleFieldNames = new ArrayList< String >();

    // get field(s) with matching name
    possibleFieldNames.add( specifier );
    possibleFieldNames.add( specifier.toUpperCase(Locale.US) );
    possibleFieldNames.add( specifier.toLowerCase(Locale.US) );
    String capitalizedSpec = Utils2.capitalize( specifier );
    if ( Character.isLowerCase( specifier.charAt( 0 ) ) ) {
      possibleFieldNames.add( capitalizedSpec );
    }
    return possibleFieldNames;
  }

  /**
   * @param specifier
   * @return
   */
  public static List< String > getPossibleMethodNames( String specifier ) {
    return getPossibleMethodNames( specifier, new String[]{ "e", "eGet", "get", "get_" } );
  }

  /**
   * @param specifier
   * @param methodPrefixes
   * @return methods with names that match the specifier or the specifier with
   *         one of the method prefixes
   */
  public static List< String > getPossibleMethodNames( String specifier,
                                                       String ... methodPrefixes ) {
    // get methods with matching names
    List< String > possibleMethodNames = getPossibleFieldNames( specifier );
    String capitalizedSpec = Utils2.capitalize( specifier );
    for ( String prefix : methodPrefixes ) {
      possibleMethodNames.add( prefix + capitalizedSpec );
    }
    return possibleMethodNames;
  }

  /**
   * Determines the validity of a return value packaged with a {@link Boolean}
   * in a {@link Pair}.
   * <p>
   * 
   * A method may return a success/fail {@link Boolean} with another return
   * value in a pair. For example, a getValue() method may want to return null
   * sometimes as a valid value and at other times as an invalid value.
   * <p>
   * 
   * The {@link Boolean} may be null, which this method assumes means that
   * success is "unknown." When unknown, this method gives the benefit of the
   * doubt for non-null return values, so true is returned for Pair(null,
   * nonNullT), and false for Pair(null,null), assuming that an unsuccessful
   * call always has a null return value.
   * 
   * @param p
   *          a return value from another function paired with a success flag,
   *          Pair(Boolean success, T returnValue).
   * @return true if the success flag is true or null with a non-null return
   *         value.
   */
  public static <T> boolean trueOrNotNull( Pair< Boolean, T > p ) {
    return ( p.first == null && p.second != null ) || ( p.first != null && p.first );
  }
  
  /**
   * Get the value of the object's field with the specified name (or some close
   * variation). Or, if the field does not exist, find a method whose name is a
   * variation of the one specified and return the result of its invocation.
   * Only return values that are instances of the specified {@link Class}.
   * 
   * @param o
   * @param specifier
   * @param cls
   * @param propagate
   * @return
   */
  public static <T> T getMethodResults( Object o, String specifier, Class<T> cls,
                                        boolean propagate, boolean strictMatch ) {
    List<T> list = getMethodResults( o, cls, propagate, strictMatch, true, specifier );
    if ( !Utils2.isNullOrEmpty( list ) ) return list.get( 0 );
    return null;
  }
  
  /**
   * Get the values of the object's fields that have one the specified names (or, if not strict, some
   * close variation).  Only return values that
   * are instances of the specified {@link Class}. If cls is null, return all
   * values for matching fields.
   * 
   * @param o
   * @param cls
   * @param propagate
   * @param justFirst
   * @param specifiers
   * @return field members of o that have one of the specified names or are instances of cls
   */
  public static < T > List< T > getMethodResults( Object o, Class< T > cls,
                                            boolean propagate,
                                            boolean strictMatch,
                                            boolean justFirst,
                                            String... specifiers ) {
    LinkedHashSet< T > results = new LinkedHashSet< T >();
    if ( o == null || specifiers == null ) return Utils2.getEmptyList();
    for ( String specifier : specifiers ) {
      List< String > possibleMethodNames =
          ( strictMatch ? Utils2.newList( specifier )
                       : getPossibleMethodNames( specifier ) );
      for ( String name : possibleMethodNames ) {
        Method[] methods = ClassUtils.getMethodsForName( o.getClass(), name );
        if ( methods != null ) {
          for ( Method method : methods ) {
            // TODO -- pass in potential arguments? can they be deduced? 
            Pair< Boolean, Object > pr = ClassUtils.runMethod( true, o, method );
            if ( trueOrNotNull( pr ) ) {
              Pair< Boolean, T > pc = ClassUtils.coerce( pr.second, cls, propagate );//, true );
              if ( trueOrNotNull( pc ) ) {
                results.add( pc.second );
                if ( justFirst ) return Utils2.asList( results );
              }
            }
          }
        }
      }
    }
    return Utils2.asList( results );
  }
  
  /**
   * Get methods whose names are variations of those
   * specified and return the results of their invocations. Only return values that
   * are instances of the specified {@link Class}. If cls is null, return all
   * values for matching members.
   * 
   * @param o
   * @param cls
   * @param propagate
   * @param justFirst
   * @param specifiers
   * @return member values of o that have one of the specified names or are instances of cls
   */
  public static < T > List< T > getFieldValues( Object o, Class< T > cls,
                                            boolean propagate,
                                            boolean strictMatch,
                                            boolean justFirst,
                                            String... specifiers ) {
    LinkedHashSet< T > results = new LinkedHashSet< T >();
    if ( o == null || specifiers == null ) return Utils2.getEmptyList();
    for ( String specifier : specifiers ) {
      List< String > possibleFieldNames = ( strictMatch ? Utils2.newList( specifier )
                                                        : getPossibleFieldNames( specifier ) );
      for ( String name : possibleFieldNames ) {
        if ( ClassUtils.hasField( o, name ) ) {
          Object r = ClassUtils.getField( o, name, true );
          Pair< Boolean, T > p = //Expression.
              ClassUtils.coerce( r, cls, propagate );//, true );
          if ( trueOrNotNull( p ) ) {
            results.add( p.second );
            if ( justFirst ) return Utils2.asList( results );
          }
        }
      }
    }
    return Utils2.asList( results );
  }
  
  /**
   * Get the value of the object's field with the specified name (or some close
   * variation). Or, if the field does not exist, find a method whose name is a
   * variation of the one specified and return the result of its invocation.
   * 
   * @param o
   * @param specifier
   * @param propagate 
   * @return
   */
  public static Object getMemberValue( Object o, String specifier, boolean propagate,
                                       boolean strictMatch  ) {
    List<Object> members = getMemberValues( o, Object.class, propagate, strictMatch, true, specifier );
    if ( Utils2.isNullOrEmpty( members ) ) return null;
    return members.get( 0 );
  }
  
  /**
   * Get the values of the object's fields that have one the specified names (or
   * some close variation). Then get methods whose names are variations of those
   * specified and return the results of their invocations. Only return values
   * that are instances of the specified {@link Class}. If cls is null, return
   * all values for matching members.
   * 
   * @param o
   * @param cls
   * @param propagate
   * @param justFirst
   * @param specifiers
   * @param strictMatch
   * @return member values of o that have one of the specified names or are
   *         instances of cls
   */
  public static < T > List< T > getMemberValues( Object o, Class< T > cls,
                                                 boolean propagate,
                                                 boolean strictMatch,
                                                 boolean justFirst,
                                                 String... specifiers ) {
    if ( o == null || specifiers == null ) return Utils2.getEmptyList();
    LinkedHashSet< T > results = new LinkedHashSet< T >();
    results.addAll( getFieldValues( o, cls, propagate, true, justFirst, specifiers ) );
    if ( justFirst && !results.isEmpty() ) return Utils2.asList( results ); 
    results.addAll( getFieldValues( o, cls, propagate, true, justFirst, specifiers ) );
    if ( justFirst && !results.isEmpty() ) return Utils2.asList( results ); 
    if ( !strictMatch ) {
      results.addAll( getMethodResults( o, cls, propagate, false, justFirst, specifiers ) );
      if ( justFirst && !results.isEmpty() ) return Utils2.asList( results ); 
      results.addAll( getMethodResults( o, cls, propagate, false, justFirst, specifiers ) );
    }
    return Utils2.asList( results );
  }
  
  private static String getTypeName( Object o ) {
    if ( o == null ) return null;
    EObject eo = (EObject)( o instanceof EObject ? o : null );
    Class< ? > c = ( eo != null ? getType( eo ) : o.getClass() );
    if ( c == null ) return null;
    return c.getSimpleName();
  }

  public static String getName(Object o) {
    // for the fancy EObject
    EObject eo = (EObject)( o instanceof EObject ? o : null );
    if ( eo != null ) {
      if ( o instanceof EClassifier ) {
        return ( (EClassifier)o ).getInstanceClassName();
      }; 
      if ( eo instanceof ENamedElement ) {
        return ( (ENamedElement)eo ).getName();
      }
      EStructuralFeature nameFeature =
          eo.eClass().getEStructuralFeature( "name" );
      if ( nameFeature != null ) {
        return (String)eo.eGet( nameFeature );
      }
    }
    // for the vanilla object
    Object n = getMemberValue( o, "name", true, false );
    if ( n != null ) return n.toString();
    return null;
  }

  public static String getId(EObject o) {
    EStructuralFeature nameFeature = o.eClass().getEStructuralFeature("id");
    if (nameFeature == null) {
      return null;
    }
    return (String) o.eGet(nameFeature);
  }

  public static void getEObjectsOfType(EObject o, Class<?> type,
      Set<EObject> set) {
    assert set != null;
    if (type.isAssignableFrom(o.getClass())) {
      if (set.contains(o)) {
        return;
      } else {
        set.add(o);
      }
    }
    Iterator<EObject> iter = o.eContents().iterator();
    while (iter.hasNext()) {
      EObject subO = iter.next();
      getEObjectsOfType(subO, type, set);
    }
  }

  public static Set<EObject> getEObjectsOfType(EObject o, Class<?> type) {
    Set<EObject> set = new HashSet<EObject>();
    getEObjectsOfType(o, type, set);
    return set;
  }

  public static EObject getFirstContaining(Collection<? extends EObject> c,
      EObject contained) {
    for (EObject o : c) {
      if (contains(o, contained)) {
        return o;
      }
    }
    return null;
  }

  public static boolean contains(EObject outer, EObject inner) {
    for (EObject o : getEObjectsOfType(outer, inner.getClass())) {
      if (o == inner) {
        return true;
      }
    }
    return false;
  }

  public static <T extends EObject> T getContainerOfEType(EObject eObj,
      Class<T> cls) {
    return getContainerOfEType(eObj, cls, false);
  }

  public static <T extends EObject> T getContainerOfEType(EObject eObj,
      Class<T> cls, boolean includeSelf) {
    if (eObj == null)
      return null;
    if (includeSelf) {
      if (cls.isInstance(eObj))
        return (T) eObj;
    }
    return getContainerOfEType(eObj.eContainer(), cls, true);
  }

  /**
   * @param objects
   *            a collection of Objects
   * @return a comma separated, parenthesized list of the names of the
   *         elements in the collection. If they do not have a getName()
   *         method, then toString() is used.
   */
  public static String toStringNames(Collection<Object> objects) {
    StringBuffer sb = new StringBuffer();
    sb.append("(");
    boolean first = true;
    for (Object obj : objects) {
      if (first)
        first = false;
      else {
        sb.append(", ");
      }
      String name = getName( obj );
      if ( Utils2.isNullOrEmpty( name ) ) {
        name = MoreToString.Helper.toShortString( obj );
      }
      sb.append( name );
    }
    sb.append(")");
    return sb.toString();
  }

  public static String toNamesString(Collection<EObject> objects) {
    List<Object> l = new ArrayList< Object >();
    l.addAll(objects);
    return toStringNames(l);
  }

  /**
   * Find the "value" of this object hidden in the contents by looking for
   * structural features that look like synonyms of "value."
   * @see gov.nasa.jpl.ae.magicdrawPlugin.modelQuery.ModelReference#getValue()
   * 
   * @param eObj
   * @return
   */
  public static Object getValue( EObject eObj ) {
    return getValue( eObj, null, false, true, true );
  }

  /**
   * Find the "value" of the object hidden in the contents by looking for
   * structural features that look like synonyms of "value."
   * @see gov.nasa.jpl.ae.magicdrawPlugin.modelQuery.ModelReference#getValue()
   * 
   * @param eObj
   * @param cls
   * @param propagate
   * @param strictMatch
   * @return
   */
  public static <TT> TT getValue( EObject eObj, Class< TT > cls, boolean propagate,
                                 boolean strictMatch, boolean complainIfNotFound ) {
    List< TT > list = getEValues( eObj, cls, propagate, strictMatch, true,
                                 complainIfNotFound, (Seen< Object >)null );
    if ( !Utils2.isNullOrEmpty( list ) ) return list.get( 0 );
    return null;
  }

  /**
   * @param eObj
   * @param cls
   * @param propagate
   *          whether to propagate value dependencies through Expressions.<br>
   *          TODO -- Expression-specific stuff like this should go back to
   *          Expression. A version of coerce() can be defined in ClassUtils
   *          without referencing Parameters and Expressions.
   * @param strictMatch
   * @param justFirst
   * @return
   */
  public static < TT > List< TT > getEValues( EObject eObj, Class< TT > cls,
                                             boolean propagate,
                                             boolean strictMatch,
                                             boolean justFirst,
                                             boolean complainIfNotFound,
                                             Seen< Object > seen ) {
    // Check for bad input
    if ( eObj == null ) {
      if ( complainIfNotFound ) {
        Debug.error( true, "Error! Passed null object to getValues()." );
      }
      return Utils2.getEmptyList();
    }
    // return if we've already tried this eObj to avoid infinite recursion
    Pair< Boolean, Set< Object > > sp = Utils2.seen( eObj, true, (Set< Object >)seen );
    if ( sp.first ) return Utils2.getEmptyList();
    seen = (Seen< Object >)sp.second;

    List<TT> results = new ArrayList< TT >();

    // add "Value" to a list with its other words that may reference the value
    List< String > list = Utils2.newList();
    list.addAll( Arrays.asList( eWordsForValue ) );
    boolean strictThisTime = true;
    List< EStructuralFeature > seenFeatures = Utils2.newList();
    Pair< Boolean, TT > p = null;
    // At most two loop iterations. First loop is for strict matches. Second is
    // for non-strict.
    while ( true ) {
      String[] sArr = new String[list.size()];
      Utils2.toArrayOfType( list, sArr, String.class );
      List< EStructuralFeature > features =
          findStructuralFeaturesMatching( eObj, strictThisTime, false, sArr );
      // remove strict matches that are duplicated in non-strict matches
      features.removeAll( seenFeatures );
      // collect seen features for next loop iteration
      seenFeatures.addAll( features );
      // get non-null results
      Debug.outln("features=" + features);
      for ( EStructuralFeature f : features ) {
        if ( f != null ) {
          Object res = eObj.eGet( f );
          if ( res != null ) {
            p = //Expression.
                ClassUtils.coerce( res, cls, propagate );//, true );
            if ( trueOrNotNull( p ) ) {
              results.add( p.second );
              if ( justFirst ) return results;
            }
          }
        }
      }
      // get results for contents
      for ( EObject eo : eObj.eContents() ) {
        // check if contained object's name matches
        boolean found = false;
        String myName = getName( eo );
        boolean noName = Utils2.isNullOrEmpty( myName );
        if ( !noName && list.contains( myName ) ) {
          p = //Expression.
              ClassUtils.coerce( eo, cls, propagate );//, true );
          if ( trueOrNotNull( p ) ) {
            results.add( p.second );
            if ( justFirst ) return results;
            found = true;
          }
        } if ( !noName && !strictThisTime ) {
          // non-strict name check
          for ( String name : list ) {
            if ( myName.contains( name ) ) {
              p = //Expression.
                  ClassUtils.coerce( eo, cls, propagate );//, true );
              if ( trueOrNotNull( p ) ) {
                results.add( p.second );
                if ( justFirst ) return results;
                found = true;
                break;
              }
            }
          }
        }
//        if ( !found ) {
          // check if contained object has "values"
          List<TT> resList = getValues( eo, cls, propagate,
                                        strictThisTime, justFirst,
                                        false, seen );
          if ( !Utils2.isNullOrEmpty( resList ) ) {
            if ( justFirst ) return resList;
            // Don't combine using Utils2.addAll(), which is unordered!
            results.addAll( resList );
          }
        }
//      }
      if ( strictMatch ) break;
      if ( !strictThisTime ) break;
      strictThisTime = false;
      //sizeOfLast = features.size(); // skip the ones we have already seen on the next loop
    }

    // If failed print the last Exception's stack trace.
    if ( complainIfNotFound && Utils2.isNullOrEmpty( results ) ) {
      Debug.error( false, "Error! EmfUtils.getValues(" + getName( eObj ) +
                          ") found no value to return!" );
    }
    
    return results;
    
/*  // resurrect this code to walk through structural features directly instead of calling   
    for ( String s : eWordsForValue ) {
      list.add( s.toLowerCase() );
    }
    Set<String> wordsForValue = new TreeSet<String>( list );
    for ( EStructuralFeature f : eObj.eClass().getEStructuralFeatures() ) {
      String fName = f.getName().toLowerCase();
      boolean found = wordsForValue.contains( fName ); 
      if ( found ) {
        res = eObj.eGet( f );
        if ( res != null ) break;
      }
      for ( String valueWord : wordsForValue ) {
        if ( res == null && fName.contains( valueWord ) ) {
          res = eObj.eGet( f );
          break;
        }
      }
    }
    if ( res == null ) {
      for ( EObject eo : eObj.eContents() ) {
        res = getValue(eo);
        if ( res != null ) break;
      }
    }
    return res;
*/
  }
  
  /**
   * Get a "value" corresponding to the {@link Object} that are instances of the
   * input {@link Class} type.
   * 
   * @param obj
   * @param cls
   * @param propagate
   * @param strictMatch
   * @param complainIfNotFound
   * @return
   */
  public static < TT > TT getValue( Object obj, Class< TT > cls,
                                    boolean propagate, boolean strictMatch,
                                    boolean complainIfNotFound ) {
    List<TT> values = getValues( obj, cls, propagate, strictMatch, true,
                                 complainIfNotFound, null );
    if ( Utils2.isNullOrEmpty( values ) ) return null;
    return values.get( 0 );
  }

//  public static < TT > List< TT > getEValues( EObject obj, Class< TT > cls,
//                                             boolean propagate,
//                                             boolean strictMatch,
//                                             boolean justFirst,
//                                             boolean complainIfNotFound,
//                                             Seen<Object> seen ) {
//    return getValues( obj, cls, propagate, strictMatch, justFirst,
//                      complainIfNotFound, seen );
////    Assert.assertFalse(true);
////    return null;
//  }
  /**
   * Get "values" corresponding to the {@link Object} that are instances of the
   * input {@link Class} type.
   * 
   * @param obj
   * @param cls
   * @param propagate
   * @param strictMatch
   * @param justFirst
   * @param complainIfNotFound
   * @return
   */
  public static < TT > List< TT > getValues( Object obj, Class< TT > cls,
                                             boolean propagate,
                                             boolean strictMatch,
                                             boolean justFirst,
                                             boolean complainIfNotFound,
                                             Seen<Object> seen ) {
    // Check for bad input
    if ( obj == null ) {
      if ( complainIfNotFound ) {
        Debug.error( true, "Error! Passed null object to getValues()." );
      }
      return Utils2.getEmptyList();
    }

    // return if we've already tried this eObj to avoid infinite recursion
    Pair< Boolean, Seen< Object > > sp = Utils2.seen( obj, true, seen );
    if ( sp.first ) return Utils2.getEmptyList();
    seen = sp.second;

    List<TT> results = null;// new called by getMembers() call below. // new ArrayList< TT >();
    Pair< Boolean, TT > p = null;

    // Try for an exact match with a member field or function.
    results = getMemberValues( obj, cls, propagate, true, justFirst, "value" );
    if ( !Utils2.isNullOrEmpty( results ) && justFirst ) return results;

    // Try getting value as an EObject.
    EObject eObj = null;
    if ( obj instanceof EObject ) {
      eObj = (EObject)obj;
      seen.remove( obj );
      // Try getting strict matching values as an EObject.  Will be less strict later.
      List<TT> vList = EmfUtils.getEValues( eObj, cls, propagate, false, justFirst,
                                            false, seen );
      if ( justFirst && !vList.isEmpty() ) return vList;
      // Don't combine using Utils2.addAll(), which is unordered!
      results.addAll( vList );
//      for ( Object v : vList ) {
//        p = Expression.coerce( v, cls, propagate, true );
//        if ( trueOrNotNull( p ) ) {
//          results.add( p.second );
//          if ( justFirst ) return results;
//        }
//      }
////      if ( cls != null && cls.equals( String.class ) ) {
////        for ( Object v : vList ) {
////          if ( v.getClass().equals( String.class ) ) continue;
////          p = Expression.coerce( v.toString(), cls, propagate, true );
////          if ( trueOrNotNull( p ) ) {
////            results.add( p.second );
////            if ( justFirst ) return results;
////          }
////        }
////      }
    }

    // Return the obj if it is already the *exact* right type.
    if ( cls != null && cls.equals( obj.getClass() ) ) {
      p = //Expression.
          ClassUtils.coerce( obj, cls, propagate );//, true );
      if ( trueOrNotNull( p ) ) {
        results.add( p.second );
        if ( justFirst ) return results;
      } else {
        Debug.error( true, "Error! Coercion of " + obj + " of type " +
                           obj.getClass().getSimpleName() + " to " +
                           cls.getSimpleName() + " unexpectedly failed!" );
      }
    }
    
    // Try finding members of other names that could mean "value."
    String clsName = (cls == null ? "object": cls.getSimpleName() );
    ArrayList<String> wordsForValueList = new ArrayList< String >();
    //String[] wordsForValue = null;
    String[] wordsForValue =
        new String[] { clsName + "Value", "literalValue", clsName };
//    if ( eObj != null ) {
//      wordsForValueList.addAll( Arrays.asList( EmfUtils.eWordsForValue ) );
//      wordsForValueList.addAll( Arrays.asList( wordsForValue ) );
//      //wordsForValue = eWords;
//    } else {
      wordsForValueList.addAll( Arrays.asList( wordsForValue ) );
      wordsForValueList.addAll( Arrays.asList( EmfUtils.oWordsForValue ) );
//      //wordsForValue = oWords;
//    }
    wordsForValue = new String[wordsForValueList.size()];
    Utils2.toArrayOfType( wordsForValueList, wordsForValue, String.class );
    //wordsForValue = (String[])wordsForValueList.toArray();

    // Try for an exact match with a member field or function.
    List<TT> resList = getMemberValues( obj, cls, propagate, strictMatch, justFirst,
                                   (String[])wordsForValue ); 
    if ( justFirst && !resList.isEmpty() ) return resList;
    // Don't combine using Utils2.addAll(), which is unordered!
    results.addAll( resList );

//    Object v = null;
//    for ( String word : wordsForValue ) {
////      try {
////        ModelReference< TT > mr =
////            new ModelReference< TT >( obj, word, null,
////                                      getCollectionClass( cls ), true );
////        if ( !mr.isEmpty( propagate ) ) {
////          v = Expression.evaluate( mr.evaluateAndGetOne( propagate ), cls,
////                                   propagate );
////          if ( v != null ) {
////            tt = (TT)cls.cast( v );
////            return tt;
////          }
////        }
////      } catch ( ClassCastException e ) {
//        tt = null;
//        cce = e; // ignore for now
//        // Return v.toString() if cls == String.class.
//        if ( v != null && cls != null && cls.equals( String.class ) ) {
//          // REVIEW -- shouldn't this have been done inside EmfUtils or
//          // Expression.evaluate()?
//          try {
//            v = v.toString();
//            tt = (TT)cls.cast( v );
//            return tt;
//          } catch ( ClassCastException ce ) {
//            ce.printStackTrace();
//            tt = null;
//          }        
//        }
//      }
////    }
    
    // Try to coerce the input obj to the correct type.
    if ( cls != null ) {//&& Utils2.isNullOrEmpty( results ) ) {
      p = //Expression.
          ClassUtils.coerce( obj, cls, propagate ); //, true );
      if ( trueOrNotNull( p ) ) {
        results.add( p.second );
        if ( justFirst ) return results;
      }
    }

    // if yet unsuccessful and cls == String.class return toString()
    if ( Utils2.isNullOrEmpty( results ) && cls != null &&
         cls.equals( String.class ) ) {
      p = //Expression.
          ClassUtils.coerce( obj.toString(), cls, propagate ); //, true );
      if ( trueOrNotNull( p ) ) {
        results.add( p.second );
        if ( justFirst ) return results;
      } else {
        // We should never get here.
        String msg = "Error! Coercion of " + obj + " of type " +
            obj.getClass().getSimpleName() + " to " +
            cls.getSimpleName() + " unexpectedly failed!";
        Assert.assertFalse( msg, true );
        Debug.error( true, msg );
      }
    }

    // If we aren't restricted by type and are yet unsuccessful, return the
    // object itself.
    if ( ( cls == null || cls.equals( Object.class ) ) &&
         Utils2.isNullOrEmpty( results ) ) {
      p = //Expression.
          ClassUtils.coerce( obj, cls, propagate );//, true );
      if ( trueOrNotNull( p ) ) {
        results.add( p.second );
        if ( justFirst ) return results;
      }
    }

    // If failed print the last Exception's stack trace.
    if ( complainIfNotFound && Utils2.isNullOrEmpty( results ) ) {
      Debug.error( false, "Error! EmfUtils.getValues(" + getName( obj ) +
                          ") found no value to return!" );
    }
    
    return results;
  }

  

  public static EStructuralFeature
      findStructuralFeatureMatching( EObject eObj, boolean strictMatch,
                                     String... possibleNames ) {
    List< EStructuralFeature > res =
        findStructuralFeaturesMatching( eObj, strictMatch, true, possibleNames );
    if ( !Utils2.isNullOrEmpty( res ) ) return res.get( 0 );
    return null;
  }

  public static List<EStructuralFeature>
      findStructuralFeaturesMatching( EObject eObj, boolean strictMatch,
                                      boolean justfirst,
                                      String... possibleNames ) {
    List<EStructuralFeature> features = new ArrayList< EStructuralFeature >();
    ArrayList< String > list = new ArrayList< String >();
    for ( String s : possibleNames ) {
      if ( !strictMatch ) s = s.toLowerCase();
      list.add( s );
    }
    TreeSet< String > names = new TreeSet< String >( list );
    for ( EStructuralFeature f : eObj.eClass().getEStructuralFeatures() ) {
      String fName = f.getName();
      if ( !strictMatch ) fName = fName.toLowerCase();
      if ( names.contains( fName ) ) {
        features.add( f );
        if ( justfirst ) return features;
      }
      if ( !strictMatch ) {
        for ( String valueWord : names ) {
          if ( fName.contains( valueWord ) ) {
            features.add( f );
            if ( justfirst ) return features;
          }
        }
      }
    }
    return features;
  }
  
  /**
   * Try to translate a SysML type name to a Java Class for primitives, but the
   * capitalized Class, like Integer.class instead of int.class.
   * 
   * @param sysMLType
   * @return the Java Class rough equivalent to sysMLType
   */
  public static Class< ? > classForSysMLType( String sysMLType ) {
    // TODO -- handle BigDecimal, BigInteger, etc.?
    String lower = sysMLType.toLowerCase();
    boolean s = lower.contains( "string" );
    if ( s && lower.equals( "string" ) ) return String.class;
    boolean d = lower.contains( "double" );
    boolean n = lower.equals( "number" );
    boolean dec = lower.contains( "decimal" );
    boolean f = lower.contains( "float" );
    boolean l = lower.contains( "long" );
    //boolean ll = l && lower.contains( "longlong" );
    if ( l && !f && !d && !dec ) return Long.class;
    boolean ii = lower.contains( "integer" );
    if ( !ii && ( d || ( l && f ) || ( ( n || dec) && ( l || !f ) ) ) ) {
      return Double.class;
    }
    if ( l || d ) {
      Debug.error( false, "Warning! classForSysMLType(" + sysMLType + "): not returning Long or Double!" );
    }
    if ( f ) return Float.class;
    boolean h = lower.contains( "short" );
    if ( h && ( lower.equals( "short" ) || lower.startsWith( "short int" ) ) ) {
      return Short.class;
    }
    boolean i = lower.matches( ".*[^a-z]int.*" ) || lower.startsWith( "int" );
    if ( i || ii ) {
      if ( h ) return Short.class;
      return Integer.class;
    }
    if ( s ) return String.class;
    boolean c = lower.equals( "char" );
    Class<?> cls = boolean.class;
    return ClassUtils.getClassForName( sysMLType, null, false );
  }

  public static Class<?> getType( EObject eObj ) {
    return getType( eObj, true );
  }
  public static Class<?> getType( EObject eObj, boolean strictMatch ) {
    List<Class<?>> list = getTypes( eObj, true, strictMatch, true, true, null );
    if ( !Utils2.isNullOrEmpty( list ) ) return list.get( 0 );
    return null;
  }

  public static Class<?> asClass( Object obj, Seen<Object> seen ) {
    Class<?> cls = null;
    if ( obj instanceof EClassifier ) {
      cls = ((EClassifier)obj).getInstanceClass();
    }
    if ( cls == null ) {
      cls = //Expression.
          ClassUtils.evaluate( obj, Class.class, true ); //, false );
    }
    if ( cls == null ) {
      List<Class> values = getValues( obj, Class.class, true, true, true,
                                      false, seen );
      if ( !Utils2.isNullOrEmpty( values ) ) {
        cls = values.get( 0 );
      }
    }
    return cls;
  }
  
  public static List< Class< ? > > getTypes( EObject eObj, boolean propagate,
                                             boolean strictMatch,
                                             boolean justFirst,
                                             boolean complainIfNotFound,
                                             Seen< Object > seen ) {
    if ( eObj == null ) return null;

    // return if we've already tried this eObj to avoid infinite recursion
    Pair< Boolean, Seen< Object > > sp = Utils2.seen( eObj, true, seen );
    if ( sp.first ) return Utils2.getEmptyList();
    seen = sp.second;
    
    ArrayList< Class<?> > results = new ArrayList< Class<?> >();
    results.add( eObj.eClass().getInstanceClass() );
    if ( justFirst ) return results;
    
    TreeSet< String > wordsForTypeSet =
        new TreeSet<String>( Arrays.asList( wordsForType ) );
    
    boolean strictThisTime = true;
    int sizeOfLast = 0;
    // At most two loop iterations. First loop is for strict matches. Second is
    // for non-strict.
    while ( true ) {
      // Get structural features whose names are words for "type"
      List< EStructuralFeature > features =
          findStructuralFeaturesMatching( eObj, strictThisTime, false,
                                          wordsForType );
      // Ignore (remove) the features we already saw from the last loop.
      if ( sizeOfLast > 0 ) {
        features = features.subList( sizeOfLast, features.size() );
      }
      Object res = null;
      // See if the eObj's instantiations of these structural features are Classes.
      for ( EStructuralFeature f : features ) {
        if ( f != null ) {
          res  = eObj.eGet( f );
          Class<?> cls = asClass( res, seen );
          if ( cls != null ) {
            results.add( cls );
            if ( justFirst ) return results;
          }
        }
      }
      if ( res == null ) {
        for ( EObject eo : eObj.eContents() ) {
          // check if contained object's name indicates that it's a type
          boolean found = false;
          String myName = getName( eo );
          if ( myName != null && wordsForTypeSet.contains( getName( eo ) ) ) {
            // Is the contained object itself represent a type? 
            Class<?> cls = asClass( eo, seen );
            if ( cls != null ) {
              results.add( cls );
              if ( justFirst ) return results;
              found = true;
            } else {
              // S
              List< Object > oList =
                  getValues( (Object)eo, Object.class, propagate, strictMatch,
                             justFirst, false, seen );
              for ( Object o : oList ) {
                // TODO -- REVIEW -- have already seen o from getValues()?!!
                cls = asClass( o, seen );
                if ( cls != null ) {
                  results.add( cls );
                  if ( justFirst ) return results;
                  found = true;
                }
              }
            }
            found = true;
          } if ( myName != null && !strictThisTime ) {
            // non-strict name check
            for ( String name : wordsForType ) {
              if ( myName.toLowerCase().contains( name.toLowerCase() ) ) {
                Class<?> cls = asClass( eo, seen ); // REVIEW seen correct here?
                if ( cls != null ) {
                  results.add( cls );
                  if ( justFirst ) return results;
                  found = true;
//                  break;
                }
              }
            }
          }
          if ( !found )
          // get type of value
          if ( !found && !strictThisTime ){
            // get types of contents???
            List< Class< ? > > resList =
                getTypes( eo, propagate, strictMatch, justFirst, false, seen );
            if ( !Utils2.isNullOrEmpty( resList ) ) {
              if ( justFirst ) return resList;
              results.addAll( resList );
            }
          }
        }
      }
      if ( strictMatch ) break;
      if ( !strictThisTime ) break;
      // skip the ones we have already seen on the next loop
      sizeOfLast = features.size();  // TODO -- REVIEW Didn't we get rid of lastSize??!
      strictThisTime = false;
    }
    return results;
  }
  
  public static String[] wordsForType = new String[] { "Type", "Class",
                                                       "Typename", "ClassName",
                                                       "DefaultType", "eClass" };

  public static String[] eWordsForValue = new String[] { "value",
      "StringExpression", "OpaqueExpression", "LiteralBoolean",
      "LiteralInteger", "LiteralNull", "LiteralSpecification",
      "LiteralString", "LiteralUnlimitedNatural", "ElementValue",
      "Expression", "InstanceValue", "TimeExpression", "TimeInterval",
      "Duration", "DurationInterval", "Interval",

      "ValueSpecification", "propertyValue", "attributeValue",
      "referenceValue", "body", "result",

      "defaultValue", "specification",

      "literal", "instance" };
  
  public static String[] oWordsForValue = new String[] { "value",
      "literal", "instance",

      "Expression", "InstanceValue", "body", "result",

      "StringExpression", "OpaqueExpression", "LiteralBoolean",
      "LiteralInteger", "LiteralNull", "LiteralSpecification",
      "LiteralString", "LiteralUnlimitedNatural", "ElementValue",

      "defaultValue", "specification",

      "ValueSpecification", "propertyValue", "attributeValue",
      "referenceValue",

      "TimeExpression", "TimeInterval", "Duration", "DurationInterval",
      "Interval" };

}
