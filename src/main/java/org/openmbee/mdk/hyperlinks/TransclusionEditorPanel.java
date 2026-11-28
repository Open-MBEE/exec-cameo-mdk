package org.openmbee.mdk.hyperlinks;

import javax.annotation.CheckForNull;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.nomagic.annotation.NotApi;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.elementreferenceintext.TransclusionType;
import com.nomagic.magicdraw.elementreferenceintext.ElementReferenceInTextInternalUtils;
import com.nomagic.magicdraw.elementreferenceintext.UpdateMode;
import com.nomagic.magicdraw.elementreferenceintext.resources.ElementReferenceInTextResource;
import com.nomagic.magicdraw.hyperlinks.ElementHyperlink;
import com.nomagic.magicdraw.hyperlinks.Hyperlink;
import com.nomagic.magicdraw.hyperlinks.HyperlinkHandler;
import com.nomagic.magicdraw.hyperlinks.HyperlinkTextAttributes;
import com.nomagic.magicdraw.hyperlinks.ui.HyperlinkEditorOptions;
import com.nomagic.magicdraw.hyperlinks.ui.HyperlinkEditorPanel;
import com.nomagic.magicdraw.hyperlinks.ui.LinkNamePanel;
import com.nomagic.magicdraw.hyperlinks.ui.LinkNameWrapper;
import com.nomagic.magicdraw.resources.DialogResource;
import com.nomagic.magicdraw.ui.Icons;
import com.nomagic.magicdraw.ui.dialogs.banners.BannerFactory;
import com.nomagic.magicdraw.ui.dialogs.banners.DefaultBanner;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.ui.FocusTextField;
import com.nomagic.ui.HiDPIScaleUtilities;
import com.nomagic.ui.ResizableIcon;
import com.nomagic.ui.banners.BannerProvider;

import java.awt.Insets;
import java.util.Map;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

public class TransclusionEditorPanel extends HyperlinkEditorPanel implements BannerProvider {
    private final JComboBox<TransclusionType> typeBox;
   private final JComboBox<UpdateMode> updateBox = new JComboBox(UpdateMode.values());
   private final TransclusionTypePanel<TransclusionType, UpdateMode> typePanel;
    private BaseElement b;
    private JLabel label;

    public TransclusionEditorPanel(HyperlinkHandler handler) {
        super(TransclusionDialogResource.getString("TRANSCLUSION_TAB"),TransclusionDialogResource.getString("TRANSCLUSION_SELECT"), true, handler, "cf");
        this.updateBox.setName(ElementReferenceInTextResource.getString("UPDATE_MODE"));
        this.typeBox = new JComboBox(new TransclusionType[]{TransclusionType.NAME, TransclusionType.REPRESENTATION_TEXT, TransclusionType.IMAGE_REPRESENTATION_TEXT, TransclusionType.CUSTOM_TEXT});
        this.typeBox.setName(ElementReferenceInTextResource.getString("DISPLAY_MODE"));
        this.typeBox.setSelectedItem(u());
        this.typeBox.addItemListener((var1x) -> {
            if (var1x.getStateChange() == 1) {
                this.a((TransclusionType)var1x.getItem(), false);
            }

        });
        this.typePanel = new TransclusionTypePanel(this.typeBox, this.updateBox);
        this.typePanel.init();
        b var2 = new b();
        this.typeBox.setRenderer(var2);
        this.updateBox.setRenderer(var2);
        this.a((TransclusionType)this.typeBox.getSelectedItem(), true);
    }

    protected JComponent createLinkLabel(String text) {
      JPanel panel = new JPanel(new GridBagLayout());
      JLabel textLabel = new JLabel(text);
      this.label = getLabel();
      this.label.setVisible(false);
      GridBagConstraints constraints = new GridBagConstraints();
      constraints.anchor = 18;
      constraints.fill = 2;
      constraints.insets = new Insets(0, 5, HiDPIScaleUtilities.scaleUI(3), 0);
      constraints.gridx = 0;
      panel.add(textLabel, constraints);
      constraints.insets = new Insets(0, 5, 0, 0);
      constraints.gridx = 1;
      panel.add(this.label, constraints);
      constraints.gridx = 2;
      constraints.weightx = 1.0D;
      panel.add(new JPanel(), constraints);
      return panel;
   }

    protected void browse() {}

    @Override
    public boolean isProjectScope() {
        return true;
    }

    private static JLabel getLabel() {
        return new JLabel(Icons.WARNING_ICON);
     }

    public DefaultBanner getBanner() {
      return (DefaultBanner) BannerFactory.createBanner(TransclusionDialogResource.getString("TRANSCLUSION_EDITOR_DIALOG_BANNER_TITLE"), TransclusionDialogResource.getString("TRANSCLUSION_EDITOR_DIALOG_BANNER_DESCRIPTION"), BannerFactory.readBannerIcon("manage_hyperlinks_dialog_banner", ".png"));
   }

   private static UpdateMode getUpdateMode() {
      Project var0 = Application.getInstance().getProject();
      return var0 != null ? com.nomagic.magicdraw.elementreferenceintext.e.b(var0) : UpdateMode.AUTOMATIC_UPDATE;
   }

   private static TransclusionType getTransclusionType() {
      Project var0 = Application.getInstance().getProject();
      return var0 != null ? TransclusionUtils.getTransclusionType(var0) : TransclusionType.NAME;
   }

   public void setOptions(@CheckForNull HyperlinkEditorOptions var1) {
      if (var1 != null && var1.isSimpleMode()) {
         this.typeBox.removeItem(TransclusionType.IMAGE_REPRESENTATION_TEXT);
      }

   }

   protected FocusTextField createLinkAddressTextField(boolean var1, String var2) {
      FocusTextField var3 = super.createLinkAddressTextField(var1, var2);
      var3.getDocument().addDocumentListener(new Listener(this));
      return var3;
   }

   private void a(@CheckForNull TransclusionType type, boolean var2) {
      Hyperlink var3 = this.getHyperlink(LinkNamePanel.getText());
      if (var3 != null) {
         this.b(var3);
      }

      this.a(var1);
      boolean var4 = var1 == TransclusionType.CUSTOM_TEXT;
      this.typePanel.setEnabled(var4);
      this.e.setEnabled(!var4);
      if (var4) {
         this.updateBox.setSelectedItem(UpdateMode.NO_UPDATE);
      } else if (var2) {
         this.updateBox.setSelectedItem(t());
      }

   }

   private void a(@CheckForNull TransclusionType var1) {
      if (var1 == TransclusionType.CUSTOM_TEXT) {
         this.typePanel.t();
      } else {
         this.typePanel.s();
      }

   }

   public Component getComponent() {
      Component var1 = super.getComponent();
      return (Component)(var1 instanceof LinkNameWrapper ? new LinkNameWrapper(this.typePanel, ((LinkNameWrapper)var1).getComponent()) : var1);
   }

   @CheckForNull
   public Hyperlink getHyperlink(String var1) {
      Hyperlink var2 = super.getHyperlink(var1);
      return this.typePanel.isAvailable() && var2 instanceof ElementHyperlink && c(var2) != null ? this.a((ElementHyperlink)var2) : var2;
   }

   private Hyperlink a(ElementHyperlink var1) {
      TransclusionType var2 = (TransclusionType)this.d.getSelectedItem();
      BaseElement var3 = var1.getElement();
      if (var3 != null) {
         Map var4 = this.a(var1.getAttributes());
         Map var5 = this.b(var3);
         String var6 = var1.getText();
         if (var2 != TransclusionType.CUSTOM_TEXT) {
            var6 = var2.getReferencedElementTextRepresentation(var3);
         }

         return new ElementHyperlink(var3, var6, var4, var5);
      } else {
         return var1;
      }
   }

   private Map<Object, Object> a(Map<Object, Object> var1) {
      return a(var1, (TransclusionType)this.d.getSelectedItem(), (UpdateMode)this.e.getSelectedItem());
   }

   /** @deprecated */
   @NotApi(
      reason = "No Magic internal source code. This code can be obfuscated and changed on each build."
   )
   @Deprecated
   public static Map<Object, Object> a(Map<Object, Object> var0, TransclusionType var1, UpdateMode var2) {
      HashMap var3 = new HashMap(var0);
      var3.put("erit:display", var1.toString());
      var3.put("erit:update", var2.toString());
      return var3;
   }

   private Map<HyperlinkTextAttributes, Object> b(BaseElement var1) {
      return ElementReferenceInTextInternalUtils.getTextAttributes(var1, (TransclusionType)this.d.getSelectedItem());
   }

   public void setHyperlink(Hyperlink var1) {
      super.setHyperlink(var1);
      this.a(var1);
   }

   private void a(Hyperlink var1) {
      Map var2 = var1.getAttributes();
      if (!var2.isEmpty()) {
         Object var3 = var2.get("erit:display");
         if (var3 != null && !var3.equals(this.d.getSelectedItem().toString())) {
            TransclusionType var4 = TransclusionType.getTransclusionType(var3.toString());
            this.d.setSelectedItem(var4);
         }

         Object var6 = var2.get("erit:update");
         if (var6 != null) {
            UpdateMode var5 = UpdateMode.getUpdateMode(var6.toString(), UpdateMode.AUTOMATIC_CHECK);
            if (!var5.equals(this.e.getSelectedItem())) {
               this.e.setSelectedItem(var5);
            }
         }

         if (var3 == null) {
            this.d.setSelectedItem(TransclusionType.CUSTOM_TEXT);
         }
      }

   }

   private void b(Hyperlink var1) {
      TransclusionType var2 = (TransclusionType)this.typeBox.getSelectedItem();
      this.typePanel.x();
      if (var2 != TransclusionType.CUSTOM_TEXT) {
         BaseElement var3 = c(var1);
         TransclusionTypePanel.Label var4 = this.a(var2, var3);
         this.typePanel.a(var4);
      }

   }

   private TransclusionTypePanel.Label a(TransclusionType var1, @CheckForNull BaseElement var2) {
      ResizableIcon var3 = var1 == TransclusionType.IMAGE_REPRESENTATION_TEXT ? Icons.getIconFor(var2) : null;
      String var4 = var1.getReferencedElementTextRepresentation(var2);
      if (var4.equals("< >")) {
         if (var1 == TransclusionType.NAME) {
            this.typePanel.v();
         }

         if (var1 == TransclusionType.REPRESENTATION_TEXT) {
            this.typePanel.w();
         }
      }

      return new TransclusionTypePanel.Label(var3, var4);
   }

   @CheckForNull
   private static BaseElement c(@CheckForNull Hyperlink var0) {
      return var0 instanceof ElementHyperlink ? ((ElementHyperlink)var0).getElement() : null;
   }

   private void c(BaseElement var1) {
      if (var1 instanceof PresentationElement) {
         this.d.setSelectedItem(TransclusionType.CUSTOM_TEXT);
         this.d.setEnabled(false);
      } else {
         if (!this.d.isEditable()) {
            this.d.setEnabled(true);
         }

      }
   }
    private class Listener implements DocumentListener {
        TransclusionEditorPanel instance;
        private Listener(TransclusionEditorPanel instance) {
            this.instance = instance;
        }

        public void insertUpdate(DocumentEvent var1) {
            this.s();
            this.t();
        }

        public void removeUpdate(DocumentEvent var1) {
            this.s();
        }

        public void changedUpdate(DocumentEvent var1) {
            this.s();
        }

        private void s() {
            Hyperlink var1 = this.instance.getHyperlink(LinkNamePanel.getText());
            if (var1 != null) {
            this.instance.b(var1);
            }

        }

        private void t() {
            Hyperlink var1 = this.instance.getHyperlink(LinkNamePanel.getText());
            if (var1 != null) {
            BaseElement var2 = TransclusionEditorPanel.c(var1);
            if (var2 != null) {
                this.instance.c(var2);
            }
            }

        }
    }
    
}
