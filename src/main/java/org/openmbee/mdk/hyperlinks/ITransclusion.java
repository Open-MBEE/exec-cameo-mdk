package org.openmbee.mdk.hyperlinks;

import java.util.Map;

import javax.annotation.CheckForNull;

import com.nomagic.magicdraw.hyperlinks.i;

import com.nomagic.magicdraw.hyperlinks.HyperlinkTextAttributes;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class ITransclusion extends Transclusion implements i {
    public ITransclusion(Element var1, @CheckForNull String var2) {
      super(var1, var2);
   }

   public ITransclusion(Element var1, @CheckForNull String var2, Map<Object, Object> var3) {
      super(var1, var2, var3);
   }

   public ITransclusion(Element var1, @CheckForNull String var2, Map<Object, Object> var3, Map<HyperlinkTextAttributes, Object> var4) {
      super(var1, var2, var3, var4);
   }

   public Element s() {
      return (Element)this.getElement();
   }
}
