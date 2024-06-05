package org.openmbee.mdk.hyperlinks;

import javax.annotation.CheckForNull;

public class DTransclusion extends AbstractTransclusion {
    @CheckForNull
   private final String a;

   public DTransclusion(String var1, @CheckForNull String var2) {
      this(var1, var2, var2);
   }

   public DTransclusion(String var1, @CheckForNull String var2, @CheckForNull String var3) {
      super(var1, var2);
      this.a = var3;
   }

   @CheckForNull
   public String s() {
      return this.a;
   }
}
