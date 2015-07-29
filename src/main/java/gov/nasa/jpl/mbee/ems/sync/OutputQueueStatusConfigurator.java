package gov.nasa.jpl.mbee.ems.sync;

import com.nomagic.actions.AMConfigurator;
import com.nomagic.actions.ActionsCategory;
import com.nomagic.actions.ActionsManager;

public class OutputQueueStatusConfigurator implements AMConfigurator {
	
	private static final OutputQueueStatusAction outputQueueStatusAction = new OutputQueueStatusAction();
	
	public static final String OUTPUT_QUEUE = "Output Queue";

	public static OutputQueueStatusAction getOutputQueueStatusAction() {
		return outputQueueStatusAction;
	}
	
	@Override
	public int getPriority() {
		return AMConfigurator.MEDIUM_PRIORITY;
	}

	@Override
	public void configure(ActionsManager mngr) {
		final ActionsCategory category = new ActionsCategory(OUTPUT_QUEUE, OUTPUT_QUEUE);
		category.addAction(outputQueueStatusAction);
		mngr.addCategory(category);
	}
}
