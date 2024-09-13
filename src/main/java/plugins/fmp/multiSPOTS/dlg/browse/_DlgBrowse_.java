package plugins.fmp.multiSPOTS.dlg.browse;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import icy.canvas.IcyCanvas;
import icy.gui.frame.IcyFrame;
import icy.gui.viewer.Viewer;
import icy.gui.viewer.ViewerEvent;
import icy.gui.viewer.ViewerListener;
import icy.gui.viewer.ViewerEvent.ViewerEventType;
import icy.main.Icy;
import icy.sequence.DimensionId;
import icy.sequence.Sequence;

import plugins.fmp.multiSPOTS.MultiSPOTS;
import plugins.fmp.multiSPOTS.experiment.Experiment;

public class _DlgBrowse_ extends JPanel implements ViewerListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6826269677524125173L;
	public LoadSaveExperiment panelLoadSave = new LoadSaveExperiment();
	private MultiSPOTS parent0 = null;

	public void init(JPanel mainPanel, String string, MultiSPOTS parent0) {
		this.parent0 = parent0;
		JPanel filesPanel = panelLoadSave.initPanel(parent0);
		mainPanel.add(filesPanel, BorderLayout.CENTER);
		mainPanel.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				parent0.mainFrame.revalidate();
				parent0.mainFrame.pack();
				parent0.mainFrame.repaint();
			}
		});
	}

	public void updateViewerForSequenceCam(Experiment exp) {
		Sequence seq = exp.seqCamData.seq;
		if (seq == null)
			return;

		final ViewerListener parent = this;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Viewer v = seq.getFirstViewer();
				if (v == null) {
					v = new Viewer(exp.seqCamData.seq, true);
					List<String> list = IcyCanvas.getCanvasPluginNames();
					String pluginName = list.stream().filter(s -> s.contains("Canvas2DWithFilters")).findFirst()
							.orElse(null);
					v.setCanvas(pluginName);
				}

				if (v != null) {
					placeViewerNextToDialogBox(v, parent0.mainFrame);
					v.toFront();
					v.requestFocus();
					v.addListener(parent);
					v.setTitle(exp.seqCamData.getDecoratedImageName(0));
					v.setRepeat(false);
				}
			}
		});
	}

	private void placeViewerNextToDialogBox(Viewer v, IcyFrame mainFrame) {
		Rectangle rectv = v.getBoundsInternal();
		Rectangle rect0 = mainFrame.getBoundsInternal();
		if (rect0.x + rect0.width < Icy.getMainInterface().getMainFrame().getDesktopWidth()) {
			rectv.setLocation(rect0.x + rect0.width, rect0.y);
			v.setBounds(rectv);
		}
	}

	@Override
	public void viewerChanged(ViewerEvent event) {
		if ((event.getType() == ViewerEventType.POSITION_CHANGED)) {
			if (event.getDim() == DimensionId.T) {
				Viewer v = event.getSource();
				int idViewer = v.getSequence().getId();
				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
				if (exp != null) {
					int idCurrentExp = exp.seqCamData.seq.getId();
					if (idViewer == idCurrentExp) {
						int t = v.getPositionT();
						v.setTitle(exp.seqCamData.getDecoratedImageName(t));
						if (parent0.dlgCages.bTrapROIsEdit)
							exp.saveDetRoisToPositions();
						exp.updateROIsAt(t);
					}
				}
			}
		}
	}

	@Override
	public void viewerClosed(Viewer viewer) {
		viewer.removeListener(this);
	}

}
