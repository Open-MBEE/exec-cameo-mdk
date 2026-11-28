package org.openmbee.mdk.hyperlinks;

import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.uml2.ext.jmi.smartlistener.SmartListenerConfig;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import java.util.Locale;
import java.util.function.Function;
import javax.annotation.CheckForNull;

public enum TransclusionType {
    NAME,
    IMAGE_REPRESENTATION_TEXT,
    REPRESENTATION_TEXT,
    CUSTOM_TEXT;
    
    private static final String EMPTY_TEXT = "";
    
    private static String getElementValue(@CheckForNull BaseElement element, String defaultValue, Function<BaseElement, String> textProducer) {
        if (element != null) {
            String text = (String)textProducer.apply(element);
            return !text.isEmpty() ? text : defaultValue;
        } else {
            return "";
        }
    }
    
    @CheckForNull
    public abstract SmartListenerConfig getSmartListenerConfig(Element var1);
    
    public abstract String getReferencedElementTextRepresentation(@CheckForNull BaseElement var1);
    
    public static TransclusionType getTransclusionType(String name) {
        try {
            return valueOf(name.toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException var2) {
            return CUSTOM_TEXT;
        }
    }
     }
