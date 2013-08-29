package gov.nasa.jpl.mbee.patternloader.validationfixes;

import gov.nasa.jpl.mbee.patternloader.PatternLoader;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

import org.json.simple.JSONObject;

import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;

import com.jidesoft.swing.CheckBoxList;
import com.jidesoft.swing.JideSwingUtilities;

/**
 * Class for fixing a mismatch between a diagram and its corresponding pattern.
 * The user selects element types to sync with the pattern in this fix.
 * 
 * @author Benjamin Inada, JPL/Caltech
 */
public class FixPatternMismatchSelect extends NMAction implements AnnotationAction {
	private static final long serialVersionUID = 1L;
	private DiagramPresentationElement diagToFix;
	private JSONObject pattern;
	private HashSet<String> typesToRepair;
	
	/**
	 * Initializes this instance and adds a description to the fix.
	 * 
	 * @param diag		the diagram to fix.
	 * @param pattern	the pattern to load.
	 */
	public FixPatternMismatchSelect(DiagramPresentationElement diag, JSONObject pattern, HashSet<String> typesToRepair) {
		super("FIX_PATTERN_MISMATCH_SELECT", "Fix Pattern Mismatch: Manually choose types to fix", 0);
		
		this.diagToFix = diag;
		this.pattern = pattern;
		this.typesToRepair = typesToRepair;
	}

	@Override
	public void execute(Collection<Annotation> paramCollection) {
	}

	@Override
	public boolean canExecute(Collection<Annotation> paramCollection) {
		return false;
	}

	@Override
	public void actionPerformed(ActionEvent paramActionEvent) {
		SessionManager.getInstance().createSession("Fixing mismatch");
		syncSelection();
		SessionManager.getInstance().closeSession();
	}
	
	private void syncSelection() {
		String[] typeNames = typesToRepair.toArray(new String[0]);
		
		// sort the type names for better UI
		Arrays.sort(typeNames, String.CASE_INSENSITIVE_ORDER);
		
		// make a check box list of all the type names
		CheckBoxList cbl = new CheckBoxList(typeNames);
		cbl.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		cbl.setPreferredSize(new Dimension(200, 250));
		cbl.setVisible(true);
		
		JPanel cblPanel = new JPanel();
		cblPanel.add(cbl);
		
		JLabel label = new JLabel("Select types to fix:");
		
		JPanel labelPanel = new JPanel();
		labelPanel.add(label);
		
		// set up the window
		JFrame frame = new JFrame();
		
		JButton submitButton = new JButton("Submit");
		ActionListener submitListener = new SubmitListener(cbl, frame);
		submitButton.addActionListener(submitListener);
		submitButton.setVisible(true);

		JButton cancelButton = new JButton("Cancel");
		ActionListener cancelListener = new CancelListener(frame);
		cancelButton.addActionListener(cancelListener);
		cancelButton.setVisible(true);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(submitButton, BorderLayout.WEST);
		buttonPanel.add(cancelButton, BorderLayout.EAST);
		
		Container content = frame.getContentPane();
		content.setLayout(new BorderLayout());
		content.add(labelPanel, BorderLayout.NORTH);
		content.add(cblPanel, BorderLayout.CENTER);
		content.add(buttonPanel, BorderLayout.SOUTH);
	
		frame.setTitle("Pattern Mismatch Fixer");
		frame.pack();
		frame.setVisible(true);
		JideSwingUtilities.globalCenterWindow(frame);
	}
	
	class SubmitListener implements ActionListener {
		private CheckBoxList cbl;
		private JFrame frame;
		
		public SubmitListener(CheckBoxList cbl, JFrame frame) {
			this.cbl = cbl;
			this.frame = frame;
		}
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			Object[] userSelections = cbl.getCheckBoxListSelectedValues();
			
			List<PresentationElement> loadList = new ArrayList<PresentationElement>();
			for(PresentationElement elem : diagToFix.getPresentationElements()) {
				if(Arrays.asList(userSelections).contains(elem.getHumanType())) {
					loadList.add(elem);
				}
			}
			
			SessionManager.getInstance().createSession("Loading pattern...");
			try {
				PatternLoader.loadPattern(loadList, pattern, null);
			} catch(RuntimeException e) {
				e.printStackTrace();
				SessionManager.getInstance().cancelSession();
				return;
			}
			JOptionPane.showMessageDialog(null, "Load complete.", "Info", JOptionPane.INFORMATION_MESSAGE);
			SessionManager.getInstance().closeSession();
			
			frame.dispose();
		}
	}

	class CancelListener implements ActionListener {
		private JFrame frame;
		
		public CancelListener(JFrame frame) {
			this.frame = frame;
		}
		@Override
		public void actionPerformed(ActionEvent arg0) {
			frame.dispose();
		}
	}
}