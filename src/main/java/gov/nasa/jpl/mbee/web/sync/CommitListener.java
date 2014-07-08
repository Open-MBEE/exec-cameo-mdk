package gov.nasa.jpl.mbee.web.sync;

import java.beans.PropertyChangeEvent;
import java.util.Collection;

import com.nomagic.uml2.transaction.TransactionCommitListener;


public class CommitListener implements TransactionCommitListener {

	@Override
	public Runnable transactionCommited(Collection<PropertyChangeEvent> arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
