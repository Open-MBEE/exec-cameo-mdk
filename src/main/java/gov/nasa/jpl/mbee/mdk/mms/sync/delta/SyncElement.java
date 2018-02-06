package gov.nasa.jpl.mbee.mdk.mms.sync.delta;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

/**
 * Created by igomes on 7/23/16.
 */
public class SyncElement {
    private NamedElement element;
    private Type type;

    public SyncElement(NamedElement element, Type type) {
        this.element = element;
        this.type = type;
    }

    public NamedElement getElement() {
        return element;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        LOCAL,
        MMS,
        MMS_COMMIT;

        private String prefix;

        Type() {
            this(null);
        }

        Type(String prefix) {
            this.prefix = prefix;
        }

        public String getPrefix() {
            if (prefix == null) {
                prefix = name().toLowerCase();
            }
            return prefix;
        }
    }
}
