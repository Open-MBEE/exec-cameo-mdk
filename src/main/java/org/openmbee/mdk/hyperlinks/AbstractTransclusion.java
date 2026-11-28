package org.openmbee.mdk.hyperlinks;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.CheckForNull;

import com.nomagic.magicdraw.hyperlinks.AbstractElementHyperlink;
import com.nomagic.magicdraw.hyperlinks.AbstractElementHyperlink.UrlResolver;
import com.nomagic.magicdraw.hyperlinks.Hyperlink;
import com.nomagic.magicdraw.hyperlinks.HyperlinkTextAttributes;
import com.nomagic.magicdraw.hyperlinks.Hyperlinks;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.uml2.project.ElementProject;



public abstract class AbstractTransclusion implements Hyperlink {
    private static AbstractTransclusion.UrlResolver urlResolver = new AbstractTransclusion.UrlResolverImpl();

    private String url;
   @CheckForNull
   private String text;
   private final String protocol;
   private final String typeText;
   private Map<Object, Object> attributes;
   private Map<HyperlinkTextAttributes, Object> textAttributes;

   @CheckForNull
   private BaseElement element;

   protected AbstractTransclusion(String url, @CheckForNull String text) {
      this(url, text, TransclusionUtils.getProtocol(url), (String)null);
   }


   public AbstractTransclusion(@CheckForNull BaseElement element, String url, @CheckForNull String text, @CheckForNull String typeText, Map<Object, Object> attributes) {
      this(url, text, "cf", typeText, attributes);
      this.element = element;
   }

   public AbstractTransclusion(@CheckForNull BaseElement element, String url, @CheckForNull String text, @CheckForNull String typeText, Map<Object, Object> attributes, Map<HyperlinkTextAttributes, Object> textAttributes) {
      this(url, text, "cf", typeText, attributes, textAttributes);
      this.element = element;
   }
   protected AbstractTransclusion(String url, @CheckForNull String text, String protocol, @CheckForNull String typeText) {
      this(url, text, protocol, typeText, Collections.emptyMap());
   }


   protected AbstractTransclusion(String url, @CheckForNull String text, String protocol, @CheckForNull String typeText, Map<Object, Object> attributes) {
      this(url, text, protocol, typeText, attributes, Collections.emptyMap());
   }

   protected AbstractTransclusion(String url, @CheckForNull String text, String protocol, @CheckForNull String typeText, Map<Object, Object> attributes, Map<HyperlinkTextAttributes, Object> textAttributes) {
      this.url = url;
      this.text = text;
      this.protocol = protocol;
      if (typeText == null) {
         this.typeText = protocol;
      } else {
         this.typeText = typeText;
      }

      this.attributes = new HashMap(attributes);
      this.textAttributes = new HashMap(textAttributes);
      this.fixURL();
    }

   public final void fixURL() {
        if (this.url != null && this.protocol != null && this.protocol.length() > 0) {
        int i = this.url.indexOf("://");
        if (i < 0) {
            this.url = this.protocol + "://" + this.url;
        }
        }

    }

    public static String toUrl(@CheckForNull BaseElement element) {
        return urlResolver.toUrl(element);
     }

    public String getProtocol() {
        return this.protocol;
    }

    public String getUrl() {
        return this.url;
    }

    public String getText() {
        if (this.text == null) {
        this.text = "";
        }

        return this.text;
    }

    public BaseElement getElement() {
        return this.element;
    }

    public static BaseElement getElement(String url, @CheckForNull ElementProject project) {
        return urlResolver.getElement(url, project);
    }

    protected void setElement(@CheckForNull BaseElement element) {
        this.element = element;
    }

    public boolean isValid() {
        return this.url != null && this.url.length() > 0;
    }

    public String getTypeText() {
        return this.typeText == null ? this.getProtocol() : this.typeText;
    }

    public Map<Object, Object> getAttributes() {
        return Collections.unmodifiableMap(this.attributes);
    }

    public Map<HyperlinkTextAttributes, Object> getTextAttributes() {
        return Collections.unmodifiableMap(this.textAttributes);
    }

    public static void setUrlResolver(AbstractTransclusion.UrlResolver urlResolver) {
      AbstractTransclusion.urlResolver = urlResolver;
   }

   public interface UrlResolver {
        String toUrl(@CheckForNull BaseElement var1);
    
        BaseElement getElement(String var1, @CheckForNull ElementProject var2);
    }

    public boolean isExternalHyperlink() {
        return this.getElement() == null && isExternalHyperlink(this.getUrl());
     }

     public static boolean isExternalHyperlink(String url) {
        return getProjectName(url) != null;
     }

   public static class UrlResolverImpl implements UrlResolver {
        private UrlResolverImpl() {}

        public String toUrl(BaseElement element) {
            return element != null ? "cf://" + element.getID() : "cf://ANY";
        }

        @CheckForNull
        public BaseElement getElement(String url, ElementProject project) {
            BaseElement element = null;
            if ("cf".equals(Hyperlinks.getProtocol(url))) {
                url = Hyperlinks.clearParameters(url);
                String address = Hyperlinks.getAddress(url);
                if (address == null) {
                    return null;
                }

                if (AbstractElementHyperlink.getDiagramID(address) != null) {
                    return null;
                }

                element = project.getElementByID(AbstractElementHyperlink.getElementID(address));
                String qualifiedName;
                if (element == null) {
                    qualifiedName = AbstractElementHyperlink.getModelElementID(url);
                    if (qualifiedName != null) {
                    element = project.getElementByID(qualifiedName);
                    }
                }

                if (element == null) {
                    qualifiedName = AbstractElementHyperlink.getElementQualifiedName(url);
                    String metaType = AbstractElementHyperlink.getElementMetaType(url);
                    if (qualifiedName != null && metaType != null) {
                    return null;
                    }
                }
            }

            return element;
        }
    }


   public static String getProjectBranch(String url) {
      return TransclusionUtils.getParameterValue(url, "projectBranch");
   }

   public static String getProjectName(String url) {
      return TransclusionUtils.getParameterValue(url, "projectName");
   }

   public static String getProjectVersion(String url) {
      return TransclusionUtils.getParameterValue(url, "projectVersion");
   }

}
