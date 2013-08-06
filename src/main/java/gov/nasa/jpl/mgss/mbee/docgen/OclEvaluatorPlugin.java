/**
 * 
 */
package gov.nasa.jpl.mgss.mbee.docgen;

import java.awt.event.ActionEvent;
import java.lang.reflect.Method;

import com.nomagic.actions.NMAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import gov.nasa.jpl.mbee.lib.ClassUtils;
import gov.nasa.jpl.mbee.lib.Debug;
import gov.nasa.jpl.mbee.lib.MDUtils;
import gov.nasa.jpl.mgss.mbee.docgen.actions.OclQueryAction;

public class OclEvaluatorPlugin extends MDPlugin {

  //OclQueryAction action = null;
  /**
   * 
   */
  public OclEvaluatorPlugin() {
    this(OclQueryAction.class);
  }
  public OclEvaluatorPlugin(Class<? extends NMAction> cls) {
    super(cls);
  }

  // unused -- TODO -- remove after testing
  public static void doIt( ActionEvent event, Element element ) {
    OclQueryAction action = new OclQueryAction( element );
    action.actionPerformed( event );
  }
  
  /* (non-Javadoc)
   * @see gov.nasa.jpl.mgss.mbee.docgen.MDPlugin#initConfigurations()
   */
  @Override
  public void initConfigurations() {
    //Debug.turnOn();
    if ( !MDUtils.isDeveloperMode() ) {
      Debug.outln( "OclEvaluatorPlugin will be hidden since MD is not in developer mode." );
      return;
    }
    Debug.outln("initializing OclEvaluatorPlugin!");

    //Method method = ClassUtils.getMethodsForName( OclEvaluatorPlugin.class, "doIt")[ 0 ];
    // TODO -- shouldn't have to look this method up and pass it--just get rid of
    // method argument in addConfiguration calls below.
    Method method = getNmActionMethod();

    addConfiguration( "MainMenu", "", OclQueryAction.actionText, "DocGen", method, this );
    addConfiguration( "ContainmentBrowserContext", "", OclQueryAction.actionText, "DocGen", method, this );
    addConfiguration( "BaseDiagramContext", "Class Diagram", OclQueryAction.actionText, "DocGen", method, this );
    addConfiguration( "BaseDiagramContext", "Activity Diagram", OclQueryAction.actionText, "DocGen", method, this );
    addConfiguration( "BaseDiagramContext", "SysML Block Definition Diagram", OclQueryAction.actionText, "DocGen", method, this );
    addConfiguration( "BaseDiagramContext", "SysML Internal Block Diagram", OclQueryAction.actionText, "DocGen", method, this );
    //addConfiguration( "BaseDiagramContext", "DocumentView", OclQueryAction.actionText, "DocGen", method, this );

    Debug.outln("finished initializing TestPlugin!");
  }

}
