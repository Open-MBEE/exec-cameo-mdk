package org.openmbee.mdk.hyperlinks;

import javax.swing.JComboBox;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


public class TransclusionListener<T> implements DocumentListener {
    
    private final TransclusionTypePanel panel;
    private final JComboBox<T> box;
 
    public TransclusionListener(TransclusionTypePanel panel, JComboBox<T> box) {
       this.panel = panel;
       this.box = box;
    }
 
    public void insertUpdate(DocumentEvent var1) {
       this.refresh();
    }
 
    public void removeUpdate(DocumentEvent var1) {
       this.refresh();
    }
 
    public void changedUpdate(DocumentEvent var1) {
       this.refresh();
    }
 
    private void refresh() {
       if (!this.panel.isShowing()) {
          this.box.setSelectedItem(TransclusionType.CUSTOM_TEXT);
       }
 
    }
 }
