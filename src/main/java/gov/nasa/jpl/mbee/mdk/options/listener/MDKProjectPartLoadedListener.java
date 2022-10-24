package gov.nasa.jpl.mbee.mdk.options.listener;

import com.nomagic.magicdraw.core.Application;

import java.beans.PropertyChangeEvent;

public class MDKProjectPartLoadedListener {
    private static MDKProjectPartLoadedListener INSTANCE;


    public MDKProjectPartLoadedListener() {
    }

    public static MDKProjectPartLoadedListener getInstance() {
        try {
            if (INSTANCE == null) {
                INSTANCE = new MDKProjectPartLoadedListener();
            }
        } catch (IllegalStateException var0) {
            throw InstanceNotFound(var0);
        }

        return INSTANCE;
    }

    public void configureEnvironment() {
        //TODO: hook into environment options if desired
    }


    public static void listenToProjectLoad(String var1) {
        if ("SysML Extensions.mdxml".equals(var1)) {
            Application.getInstance().getGUILog().log("Detected SysML Extensions!");
        }
    }

//    public void propertyChange(PropertyChangeEvent var1) {
//        if (var1.getSource() instanceof Slot) {
//            if (var1.getPropertyName().equals("value")) {
//                StructuralFeature var2 = ((Slot)var1.getSource()).getDefiningFeature();
//                if (var2 instanceof Property && !(var2 instanceof Port)) {
//                    sysmla((Property)var2);
//                }
//            }
//        } else if (var1.getSource() instanceof InstanceSpecification) {
//            if (var1.getPropertyName().equals("slot")) {
//                InstanceSpecification var4 = (InstanceSpecification)var1.getSource();
//                this.sysmla(var4);
//            }
//        } else if (var1.getSource() instanceof ValueSpecification) {
//            if (var1.getPropertyName().equals("instance")) {
//                ValueSpecification var5 = (ValueSpecification)var1.getSource();
//                Element var3 = var5.getOwner();
//                if (var3 instanceof Property) {
//                    sysmla((Property)var3);
//                }
//            }
//        } else if (var1.getSource() instanceof Classifier && var1.getPropertyName().equals("generalization")) {
//            Classifier var6 = (Classifier)var1.getSource();
//            this.sysmla(var6, (List)null);
//        }
//
//    }
//
//    @Override
//    public void registerToProject() {
//        if (sysmld.sysmla()) {
//            EventSupport var1 = this.getProject().getRepository().getEventSupport();
//            Iterator var2 = sysmlb.iterator();
//
//            while(var2.hasNext()) {
//                String var3 = (String)var2.next();
//                var1.addPropertyChangeListener(this, var3);
//            }
//        }
//
//    }
//
//    @Override
//    public void unregisterFromProject() {
//        EventSupport var1 = this.getProject().getRepository().getEventSupport();
//        Iterator<String> var2 = sysmlb.iterator();
//
//        while(var2.hasNext()) {
//            String var3 = var2.next();
//            var1.removePropertyChangeListener(this, var3);
//        }
//
//    }

    private static IllegalStateException InstanceNotFound(IllegalStateException var0) {
        return var0;
    }
}
