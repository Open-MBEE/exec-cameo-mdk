package gov.nasa.jpl.mbee.mdk.util;

import com.nomagic.annotation.OpenApiAll;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.dependencymatrix.DependencyMatrix;
import com.nomagic.magicdraw.dependencymatrix.MatrixManager;
import com.nomagic.magicdraw.dependencymatrix.persistence.DependencyMatrixProfile;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicreport.engine.Tool;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import java.util.Collection;
import java.util.Iterator;
import javax.annotation.CheckForNull;

@OpenApiAll
public class DependencyMatrixTool extends Tool {
    private static final long serialVersionUID = 3181889276894499167L;

    public DependencyMatrixTool() {
    }

    @CheckForNull
    public Matrix getMatrix(Diagram var1) {
        Matrix var2 = null;
        if(var1 != null) {
            Project var3 = Project.getProject(var1);
            DependencyMatrix var4 = MatrixManager.getInstance(var3).getMatrix(var1);
            if(var4 != null) {
                var2 = new Matrix(var4);
                var4.buildIfDirty();
            }
        }

        return var2;
    }

    @CheckForNull
    public Matrix getMatrix(String var1) {
        Matrix var2 = null;

        try {
            Collection var3 = Application.getInstance().getProject().getDiagrams();
            Diagram var4 = null;
            Iterator var5 = var3.iterator();

            while(var5.hasNext()) {
                DiagramPresentationElement var6 = (DiagramPresentationElement)var5.next();
                if(var1.equals(var6.getName())) {
                    Diagram var7 = var6.getDiagram();
                    if(DependencyMatrixProfile.isDependencyMatrix(var7)) {
                        var4 = var7;
                        break;
                    }
                }
            }

            if(var4 != null) {
                var2 = this.getMatrix(var4);
            }
        } catch (Exception var8) {
            ;
        }

        return var2;
    }
}
