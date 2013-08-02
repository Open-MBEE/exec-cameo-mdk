/**
 * 
 */
package gov.nasa.jpl.mbee.lib;

import java.util.Comparator;

/**
 * @author bclement
 *
 */
public class MostAbstractFirst implements Comparator< Class<?> > {

  public static MostAbstractFirst instance = instance();

  public static MostAbstractFirst instance() {
    if ( instance == null ) instance = new MostAbstractFirst();
    return instance;
  }
  
  /**
   * 
   */
  public MostAbstractFirst() {
  }

  @Override
  public int compare( Class< ? > o1, Class< ? > o2 ) {
    if ( o1 == o2 ) return 0; 
    if ( o1 == null ) return -1; 
    if ( o2 == null ) return 1;
    int count1 = 0, count2 = 0;
    Class<?> p1=o1, p2=o2;
    while ( p1 != null ) {
      ++count1;
      p1 = p1.getSuperclass();
    }
    while ( p2 != null ) {
      ++count2;
      if ( count2 > count1 ) return -1;
      p1 = p1.getSuperclass();
    }
    if ( count1 > count2 ) return 1;
    // tie break with generic comparator
    return CompareUtils.GenericComparator.instance().compare( o1, o2 );
  }

}
