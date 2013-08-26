package gov.nasa.jpl.mbee.stylesaver;

import java.util.List;

import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;


/**
 * This class contains a run method for the load operation.
 * Updates progress bar dynamically.
 */
public class RunnableLoaderWithProgress implements RunnableWithProgress {
	private List<PresentationElement> list;
	private String style;
	private boolean success;

	/**
	 * @param list	the list of elements to load styles into.
	 * @param style	the style string to reference.
	 */
	public RunnableLoaderWithProgress(List<PresentationElement> list, String style) {
		this.list = list;
		this.style = style;
	}
	
	/**
	 * Runs the load operation.
	 * 
	 * @param progressStatus the status of the operation so far.
	 */
	@Override
	public void run(ProgressStatus progressStatus) {
		progressStatus.init("Loading styles...", 0, list.size());
		success = ViewLoader.load(list, style, progressStatus);
	}
	
	/**
	 * Gets the value of the success property.
	 * 
	 * @return the value of the success property.
	 */
	public boolean getSuccess() {
		return success;
	}
}