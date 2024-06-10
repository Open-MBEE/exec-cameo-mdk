package org.openmbee.mdk.hyperlinks;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.annotation.CheckForNull;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.nomagic.annotation.NotApi;
import com.nomagic.magicdraw.elementreferenceintext.resources.ElementReferenceInTextResource;
import com.nomagic.magicdraw.hyperlinks.ui.LinkNamePanel;
import com.nomagic.magicdraw.resources.DialogResource;
import com.nomagic.magicdraw.ui.Icons;
import com.nomagic.magicdraw.ui.dialogs.specifications.ElementTextField;

import java.awt.Insets;

public class TransclusionTypePanel<T, G> extends LinkNamePanel {

   private final JComboBox<T> typeBox;
   private final JComboBox<G> updateBox;
   private final ElementTextField c;
   private JTextField d;
   private final JPanel e;
   private final JLabel f;

   public TransclusionTypePanel(JComboBox<T> var1, JComboBox<G> var2) {
      this.typeBox = var1;
      this.updateBox = var2;
      this.f = u();
      this.f.setVisible(false);
      this.e = new JPanel();
      this.c = new ElementTextField();
      this.c.setName(DialogResource.getString("TEXT_TO_DISPLAY"));
   }

   public void init() {
      this.setLayout(new GridBagLayout());
      this.e.setLayout(new BorderLayout());
      GridBagConstraints var1 = new GridBagConstraints();
      var1.anchor = 21;
      var1.fill = 2;
      var1.insets = new Insets(5, 5, 0, 5);
      var1.gridy = 0;
      var1.weightx = 0.0D;
      JLabel var2 = createTextToDisplayLabel();
      var1.gridx = 0;
      this.add(var2, var1);
      var1.gridx = 1;
      this.add(this.typeBox, var1);
      var1.gridx = 2;
      this.add(this.f, var1);
      this.d = this.getLinkNameField();
      TransclusionListener listener = new TransclusionListener(this, this.typeBox);
      this.d.getDocument().addDocumentListener(listener);
      var1.gridx = 3;
      var1.weightx = 1.0D;
      this.add(this.e, var1);
      var1.gridy = 1;
      var1.weightx = 0.0D;
      var1.gridx = 0;
      this.add(new JLabel(ElementReferenceInTextResource.getString("UPDATE_MODE") + ":"), var1);
      var1.gridx = 1;
      this.add(this.updateBox, var1);
   }

   private void a(JTextField var1) {
      this.e.remove(this.d);
      this.d = var1;
      this.e.add(this.d);
      this.revalidate();
   }

   public void setLocalEditable(boolean var1) {
      super.setLocalEditable(var1);
      this.typeBox.setEnabled(var1);
      this.updateBox.setEnabled(var1);
   }

   public void setEnabled(boolean var1) {
      super.setEnabled(var1);
      this.d.setEnabled(var1);
   }

   /** @deprecated */
   @NotApi(
      reason = "No Magic internal source code. This code can be obfuscated and changed on each build."
   )
   @Deprecated
   public void s() {
      this.a((JTextField)this.c);
   }

   /** @deprecated */
   @NotApi(
      reason = "No Magic internal source code. This code can be obfuscated and changed on each build."
   )
   @Deprecated
   public void t() {
      this.a(this.getLinkNameField());
   }

   /** @deprecated */
   @NotApi(
      reason = "No Magic internal source code. This code can be obfuscated and changed on each build."
   )
   @Deprecated
   public void a(TransclusionTypePanel.Label var1) {
      SwingUtilities.invokeLater(() -> {
         this.c.setData(var1.getText(), var1.getIcon());
         LinkNamePanel.setText(var1.getText());
      });
   }

   private static JLabel u() {
      return new TransclusionTypePanel.Label(Icons.WARNING_ICON);
   }


   public void v() {
      this.f.setToolTipText(DialogResource.getString("NO_NAME_TEXT_FOR_HYPERLINK"));
      this.f.setVisible(true);
   }

   public void w() {
      this.f.setToolTipText(DialogResource.getString("NO_REPRESENTATION_TEXT_FOR_HYPERLINK"));
      this.f.setVisible(true);
   }


   public void x() {
      this.f.setVisible(false);
   }

    static class Label extends JLabel {
        private final javax.swing.Icon icon;
        private final String text;


        public Label(@CheckForNull javax.swing.Icon icon, String text) {
            this.icon = icon;
            this.text = text;
        }

        public Label(@CheckForNull javax.swing.Icon icon) {
            this(icon, "");
        }


        public javax.swing.Icon getIcon() {
            return this.icon;
        }

        public String getText() {
            return this.text;
        }
    }
}
