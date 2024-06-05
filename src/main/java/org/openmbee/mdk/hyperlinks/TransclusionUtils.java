package org.openmbee.mdk.hyperlinks;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import javax.annotation.CheckForNull;

import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.options.ProjectOptions;
import com.nomagic.magicdraw.properties.Property;

public class TransclusionUtils {

    public static String getProtocol(String url) {
        if (url != null) {
           int i = url.indexOf("://");
           if (i > 0) {
              return url.substring(0, i);
           }
        }
  
        return "";
     }
    

     public static String getParameterValue(@CheckForNull String url, String param) {
      String ps = param + "=";
      if (url == null) {
         return null;
      } else {
         int i = url.indexOf(ps);
         if (i != -1) {
            String value = url.substring(i + ps.length());
            i = value.indexOf(38);
            if (i != -1) {
               value = value.substring(0, i);
            }

            return URLDecoder.decode(value, StandardCharsets.UTF_8);
         } else {
            return null;
         }
      }
   }

   public static TransclusionType getTransclusionType(Project var0) {
    String var1 = getTransclusionType(var0, "DEFAULT_ELEMENT_REFERENCE_DISPLAY_MODE");
        return var1 != null ? TransclusionType.getTransclusionType(var1) : TransclusionType.NAME;
    }

    private static String getTransclusionType(Project var0, String var1) {
        Property var2 = getTransclusionProjectProperty(var0, var1);
        return var2 != null ? (String)var2.getValue() : null;
    }
    
   @CheckForNull
   private static Property getTransclusionProjectProperty(Project var0, String var1) {
      ProjectOptions var2 = var0.getOptions();
      return var2 != null ? var2.getProperty("PROJECT_GENERAL_PROPERTIES", var1) : null;
   }
}
