package org.openmbee.mdk.hyperlinks;

import java.util.Collections;
import java.util.Map;

import javax.annotation.CheckForNull;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.hyperlinks.HyperlinkTextAttributes;
import com.nomagic.magicdraw.resources.DialogResource;
import com.nomagic.magicdraw.uml.BaseElement;

public class Transclusion extends AbstractTransclusion {
   
   private static final String TYPE_TEXT = DialogResource.getString("HyperlinksManagingGeneralPanel.TransclusionTypeValue");

    public Transclusion(String var1, @CheckForNull String var2) {
      this(var1, var2, Collections.emptyMap());
   }

   public Transclusion(BaseElement var1, @CheckForNull String var2) {
      this(var1, var2, Collections.emptyMap());
   }

   public Transclusion(String var1, @CheckForNull String var2, Map<Object, Object> var3) {
      super((BaseElement)null, var1, var2, TYPE_TEXT, var3);
      this.setElement(getProjectName(var1) == null ? getElement(var1, this.getProject()) : null);
   }

   public Transclusion(@CheckForNull BaseElement var1, @CheckForNull String var2, Map<Object, Object> var3) {
      super(var1, toUrl(var1), var2, TYPE_TEXT, var3);
   }

   public Transclusion(@CheckForNull BaseElement var1, @CheckForNull String var2, Map<Object, Object> var3, Map<HyperlinkTextAttributes, Object> var4) {
      super(var1, toUrl(var1), var2, TYPE_TEXT, var3, var4);
   }

   @CheckForNull
   private Project getProject() {
      BaseElement var1 = super.getElement();
      return var1 != null ? Project.getProject(var1) : Application.getInstance().getProject();
   }


   
}
