package gov.nasa.jpl.mbee.mdk.docgen.docbook;

/**
 * Use this if your list item itself contains multiple stuff
 *
 * @author dlam
 */
public class DBListItem extends DBHasContent {

    public DBListItem() {
    }

    @Override
    public void accept(IDBVisitor v) {
        v.visit(this);
    }
}
