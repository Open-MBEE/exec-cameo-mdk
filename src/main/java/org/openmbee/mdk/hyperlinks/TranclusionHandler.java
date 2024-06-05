package org.openmbee.mdk.hyperlinks;


import com.dassault_systemes.modeler.magic.ui.BaseElementClickHandlersManager;
import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.hyperlinks.ElementHyperlink;
import com.nomagic.magicdraw.hyperlinks.ElementHyperlinkHandler;
import com.nomagic.magicdraw.hyperlinks.Hyperlink;
import com.nomagic.magicdraw.hyperlinks.HyperlinkHandler;
import com.nomagic.magicdraw.hyperlinks.HyperlinkUtils;
import com.nomagic.magicdraw.hyperlinks.HyperlinksUIUtilsInternal;
import com.nomagic.magicdraw.hyperlinks.ui.HyperlinkEditor;
import com.nomagic.magicdraw.resources.DialogResource;
import com.nomagic.magicdraw.ui.Hint;
import com.nomagic.magicdraw.ui.HintsManager;
import com.nomagic.magicdraw.ui.LocationInTool;
import com.nomagic.magicdraw.ui.browser.Node;
import com.nomagic.magicdraw.ui.dialogs.MDDialogParentProvider;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.magicdraw.uml.actions.SelectInContainmentTreeAction;
import com.nomagic.ui.BaseMessageDialog;
import com.nomagic.ui.SimpleBaseDialog;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import java.awt.AWTEvent;
import java.util.function.Function;
import javax.annotation.CheckForNull;
import javax.swing.Icon;


public class TranclusionHandler implements HyperlinkHandler {
    private static Function<HyperlinkHandler, HyperlinkEditor> EDITOR_FACTORY = TransclusionEditorPanel::new;

    public static void setEditorFactory(Function<HyperlinkHandler, HyperlinkEditor> factoryFunction) {
        EDITOR_FACTORY = factoryFunction;
     }

    @Override
    public boolean isSupportedProtocol(String arg0) {
        return "cf".equals(arg0);
    }

    @Override
    public void activate(@CheckForNull Element var1, Hyperlink var2) {
      activate(var1, var2, (AWTEvent)null, LocationInTool.unknown);
   }

   public static void activate(@CheckForNull Element var0, Hyperlink var1, @CheckForNull AWTEvent var2, LocationInTool var3) {
    Transclusion var4 = var1 instanceof Transclusion ? (Transclusion)var1 : null;
      BaseElement var5 = var4 != null && !var4.isExternalHyperlink() ? var4.getElement() : null;
      if (var5 != null) {
         if (var5 instanceof Element) {
            showNotEditableHyperlinkHint((Element)var5, var2);
         }

         activateTreeOrOnFind(var5, var2, var3);
      } else if (var0 != null) {//(!ExternalElementURLActivator.openExternalURL(var1.getUrl(), var2, var3) && var0 != null) {
         BaseMessageDialog var6 = new BaseMessageDialog(MDDialogParentProvider.getProvider().getDialogOwner(), DialogResource.getString("ERROR"), true, SimpleBaseDialog.YES_NO_CANCEL_LABELS, SimpleBaseDialog.YES_NO_CANCEL_ARMAP, (String)null, DialogResource.getString("NOT_VALID_HYPERLINK_REMOVE"), 0, "/com/nomagic/ui/icons/error.png");
         var6.setVisible(true);
         if (var6.getResult() == 2) {
            Project var7 = Project.getProject(var0);
            var7.getCommandHistory().startCommand(DialogResource.getString("EDIT_HYPERLINKS_COMMAND"));
            HyperlinkUtils.removeHyperlink(var0, var1);
            var7.getCommandHistory().complete();
         }
      }

   }

   private static void showNotEditableHyperlinkHint(Element var0, @CheckForNull AWTEvent var1) {
      int var2 = HyperlinksUIUtilsInternal.getEventOrLastHyperLinkEventModifiers(var1);
      boolean var3 = (var2 & 8) != 0;
      if (!(var0 instanceof Diagram) && !var3) {
         Hint var4;
         if ((var2 & 2) == 0) {
            var4 = new Hint("SHOW_NOT_EDITABLE_HYPERLINK_CLICK", DialogResource.getString("NOT_EDITABLE_HYPERLINK_ACTIVATED"), 1);
         } else {
            var4 = new Hint("SHOW_NOT_EDITABLE_CTRL_HYPERLINK_CLICK", DialogResource.getString("NOT_EDITABLE_CTRL_HYPERLINK_ACTIVATED", new Object[]{NMAction.getMenuShortcutMaskAsString()}), 1);
         }

         HintsManager.getInstance().showTip(var4);
      }

   }

    @Override
    public HyperlinkEditor getEditor() {
        return (HyperlinkEditor)EDITOR_FACTORY.apply(this);
    }

    @Override
    public Icon getIcon(Hyperlink arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getIcon'");
    }

    public Hyperlink create(String text, String url, @CheckForNull Project project) {
      return createHyperlinkInProject(text, url, project);
   }

   public static Hyperlink createHyperlinkInProject(@CheckForNull String text, String url, @CheckForNull Project project) {
      boolean var3 = false; //Transclusion.isExternalHyperlink(var1);
      if (var3) {
         return new Transclusion(url, text);
      } else {
         BaseElement element = project != null ? Transclusion.getElement(url, project) : null;
         return (Hyperlink)(element != null ? create(text, project) : new DTransclusion(url, text, text));
      }
   }

   public static Transclusion create(@CheckForNull String text, BaseElement element) {
      return (Transclusion)(element instanceof Element ? new ITransclusion((Element)element, text) : new Transclusion(element, text));
   }


   public static void activateTreeOrOnFind(BaseElement element, @CheckForNull AWTEvent var1, LocationInTool var2) {
      int var3 = HyperlinksUIUtilsInternal.getEventOrLastHyperLinkEventModifiers(var1);
      boolean var4 = (var3 & 8) != 0;
      boolean var5 = (var1 == null || var4) && element instanceof Element;
      if (var5) {
         boolean var6 = Node.isLocked();
         long var7 = 0L;

         try {
            if (var6) {
               var7 = Node.setLocked(false);
            }

            SelectInContainmentTreeAction.selectInBrowser(element);
         } finally {
            if (var6) {
               Node.resetLock(true, var7);
            }

         }
      } else {
         BaseElementClickHandlersManager.onFind(element, var2, var1);
      }

   }
}
 