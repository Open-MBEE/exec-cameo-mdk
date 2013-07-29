package gov.nasa.jpl.mbee.lib;

//import gov.nasa.jpl.ae.event.EventInvocation;
//import gov.nasa.jpl.ae.solver.HasId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Pair<A,B> implements Comparable< Pair< A, B > >, Cloneable {
  public A first;
  public B second;

  public Pair(A a, B b) {
    first = a;
    second = b;
  }

  public < C extends B > Pair(Pair<A,C> p) {
    first = p.first;
    second = (B)p.second;
  }
  
  /**
   * Create a shallow copy of the Pair.
   */
  @Override
  public Pair< A, B > clone() {
    return new Pair< A, B >( this );
  }
  
  /**
   * Write the Pair to a {@link String} like a {@link List} with the elements in
   * parentheses and separated by a comma: "(first, second)"
   */
  @Override
  public String toString() {
    return "(" + first + ", " + second + ")";
  }

  @Override
  public int compareTo( Pair< A, B > o ) {
    if ( this == o ) return 0;
    if ( o == null ) return 1;
    int compare = CompareUtils.compare( first, o.first, true );//, true );
    if ( compare != 0 ) return compare;
    return CompareUtils.compare( second, o.second, true );//, true );
  }
  
  /**
   * @param c collection of pairs
   * @return a list of pairs' second items
   */
  public static < A, B > List< B > getSeconds( Collection< Pair< A, B > > c ) {
    List< B > seconds = new ArrayList< B >();
    for ( Pair< A, B > p : c ) {
      seconds.add( p.second );
    }
    return seconds;
  }

  /**
   * @param c collection of pairs
   * @return a list of pairs' first items
   */
  public static < A, B > List< A > getFirsts( Collection< Pair< A, B > > c ) {
    List< A > firsts = new ArrayList< A >();
    for ( Pair< A, B > p : c ) {
      firsts.add( p.first );
    }
    return firsts;
  }

}