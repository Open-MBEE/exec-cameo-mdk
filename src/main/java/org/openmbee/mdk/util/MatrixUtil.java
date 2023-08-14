package org.openmbee.mdk.util;

import com.nomagic.annotation.InternalApi;
import com.nomagic.annotation.OpenApiAll;
import com.nomagic.magicdraw.autoid.AutoIdManager;
import com.nomagic.magicdraw.dependencymatrix.DependencyMatrix;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.magicdraw.uml.RepresentationTextCreator;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.utils.sorting.AlphanumericStrings;

import javax.annotation.CheckForNull;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@OpenApiAll
public class MatrixUtil {
    private DependencyMatrix matrix;

    public MatrixUtil(DependencyMatrix var1) {
        this.setMatrix(var1);
    }

    @CheckForNull
    public List<Element> getRows() {
        ArrayList var1 = null;

        try {
            if (this.matrix != null) {
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
            if (this.matrix != null) {
                var1 = new ArrayList(this.matrix.getMatrixData().getColumnElements());
                sortElements(var1);
            }
        } catch (Exception var3) {
            ;
        }

        return var1;
    }

    public DependencyMatrix getMatrix() {
        return this.matrix;
    }

    public void setMatrix(DependencyMatrix var1) {
        this.matrix = var1;
    }

    private static void sortElements(List<Element> var0) {
        if (!var0.isEmpty()) {
            boolean var1 = AutoIdManager.getInstance((BaseElement) var0.get(0)).isShowNumberTag();
            var0.sort(new MatrixUtil.ElementComparator(var1));
        }

    }

    /**
     * @deprecated
     */
    @InternalApi(
            reason = "No Magic internal API. This code can change without any notification."
    )
    @Deprecated
    private static class ElementComparator implements Comparator<Element> {
        private boolean showAutoID = false;

        /**
         * @deprecated
         */
        @InternalApi(
                reason = "No Magic internal API. This code can change without any notification."
        )
        @Deprecated
        public ElementComparator(boolean arg0) {
            this.showAutoID = arg0;
        }

        /**
         * @deprecated
         */
        @InternalApi(
                reason = "No Magic internal API. This code can change without any notification."
        )
        @Deprecated
        public int compare(Element arg0, Element arg1) {
            return AlphanumericStrings.compare(RepresentationTextCreator.getRepresentedText(arg0, false, this.showAutoID), RepresentationTextCreator.getRepresentedText(arg1, false, this.showAutoID));
        }
    }
}
