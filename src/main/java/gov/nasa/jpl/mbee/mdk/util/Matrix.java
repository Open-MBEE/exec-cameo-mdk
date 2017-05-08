package gov.nasa.jpl.mbee.mdk.util;

import com.nomagic.annotation.InternalApi;
import com.nomagic.annotation.OpenApiAll;
import com.nomagic.magicdraw.autoid.AutoIdManager;
import com.nomagic.magicdraw.dependencymatrix.DependencyMatrix;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.magicdraw.uml.RepresentationTextCreator;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.utils.sorting.AlphanumericStrings;
import java.util.ArrayList;
import java.util.Comparator;
 import java.util.List;
import javax.annotation.CheckForNull;

@OpenApiAll
public class Matrix {
    private DependencyMatrix matrix;

    public Matrix(DependencyMatrix var1) {
        this.setMatrix(var1);
    }

    @CheckForNull
    public List<Element> getRows() {
        ArrayList var1 = null;

        try {
            if(this.matrix != null) {
                var1 = new ArrayList(this.matrix.getMatrixData().getRowElements());
                sortElements(var1);
            }
        } catch (Exception var3) {
            ;
        }

        return var1;
    }

    @CheckForNull
    public List<Element> getColumns() {
        ArrayList var1 = null;

        try {
            if(this.matrix != null) {
                var1 = new ArrayList(this.matrix.getMatrixData().getColumnElements());
                sortElements(var1);
            }
        } catch (Exception var3) {
            ;
        }

        return var1;
    }

//    public List<Relation> getRelation(Element var1, Element var2) {
//        ArrayList var3 = new ArrayList();
//        this.matrix.buildIfDirty();
//
//        try {
//            AbstractMatrixCell var4 = this.matrix.getMatrixData().getValue(var1, var2);
//            Iterator var5 = var4.getDependencies().iterator();
//
//            while(var5.hasNext()) {
//                DependencyEntry var6 = (DependencyEntry)var5.next();
//                if(var6 != null) {
//                    String var7 = var6.getType();
//                    String var8 = var6.getName();
//                    String var9 = var6.getDirection().toString();
//                    Element var10 = null;
//                    List var11 = var6.getCause();
//                    if(var11 != null && var11.size() == 1) {
//                        var10 = (Element)var11.get(0);
//                    }
//
//                    var3.add(new Relation(var7, var10, var8, var9));
//                }
//            }
//        } catch (Exception var12) {
//            ;
//        }
//
//        return var3;
//    }

    public DependencyMatrix getMatrix() {
        return this.matrix;
    }

    public void setMatrix(DependencyMatrix var1) {
        this.matrix = var1;
    }

    private static void sortElements(List<Element> var0) {
        if(!var0.isEmpty()) {
            boolean var1 = AutoIdManager.getInstance((BaseElement)var0.get(0)).isShowNumberTag();
            var0.sort(new Matrix.ElementComparator(var1));
        }

    }

    /** @deprecated */
    @InternalApi(
        reason = "No Magic internal API. This code can change without any notification."
    )
    @Deprecated
    private static class ElementComparator implements Comparator<Element> {
        private boolean showAutoID = false;

        /** @deprecated */
        @InternalApi(
            reason = "No Magic internal API. This code can change without any notification."
        )
        @Deprecated
        public ElementComparator(boolean arg0) {
            this.showAutoID = arg0;
        }

        /** @deprecated */
        @InternalApi(
            reason = "No Magic internal API. This code can change without any notification."
        )
        @Deprecated
        public int compare(Element arg0, Element arg1) {
            return AlphanumericStrings.compare(RepresentationTextCreator.getRepresentedText(arg0, false, this.showAutoID), RepresentationTextCreator.getRepresentedText(arg1, false, this.showAutoID));
        }
    }
}
