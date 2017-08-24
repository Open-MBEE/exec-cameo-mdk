package gov.nasa.jpl.mbee.mdk.docgen.docbook;

/**
 * A Section or Appendix. If you want to make a new section, instance this and
 * add content to it. You can also set it to skip or output a string if the
 * content ends up being empty.<br/>
 * You don't have to set the chapter flags, those will be done automatically
 * based on document structure once the whole document is assembled.<br/>
 * <p>
 * This should really be a representation of a view instead since now view info
 * is carried all the way through the generation process (even if they're not
 * sections)
 *
 * @author dlam
 */
public class DBSection extends DBHasContent {
    private boolean isAppendix;
    private boolean isChapter;
    private boolean skipIfEmpty;
    private String stringIfEmpty = "";
    private boolean isView;

    public void isAppendix(boolean a) {
        isAppendix = a;
    }

    public boolean isAppendix() {
        return isAppendix;
    }

    public void isChapter(boolean c) {
        isChapter = c;
    }

    public boolean isChapter() {
        return isChapter;
    }

    public boolean isSkipIfEmpty() {
        return skipIfEmpty;
    }

    public String getStringIfEmpty() {
        return stringIfEmpty;
    }

    public void setSkipIfEmpty(boolean s) {
        skipIfEmpty = s;
    }

    public void setStringIfEmpty(String s) {
        stringIfEmpty = s;
    }

    public boolean isView() {
        return isView;
    }

    public void setView(boolean b) {
        this.isView = b;
    }

    @Override
    public void accept(IDBVisitor v) {
        v.visit(this);
    }
}
