package gov.nasa.jpl.mgss.mbee.docgen.docbook;


/**
 * A list, can be unordered or ordered<br/>
 * If your list items are simple, single element things, add them directly, else use DBListItem
 * @author dlam
 *
 */

public class DBList extends DBHasContent {

	private boolean ordered;

	public DBList() {
		ordered = false;
	}

	
	public void setOrdered(boolean b) {
		ordered = b;
	}
	
	public boolean isOrdered() {
		return ordered;
	}

	@Override
	public void accept(IDBVisitor v) {
		v.visit(this);
	}
}
