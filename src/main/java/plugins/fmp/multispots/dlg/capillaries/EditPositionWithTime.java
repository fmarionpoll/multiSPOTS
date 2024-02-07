package plugins.fmp.multispots.dlg.capillaries;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import icy.gui.frame.IcyFrame;
import icy.gui.viewer.Viewer;
import icy.roi.ROI2D;
import icy.sequence.Sequence;
import icy.type.geom.Polygon2D;

import plugins.kernel.roi.roi2d.ROI2DPolygon;
import plugins.fmp.multispots.multiSPOTS;
import plugins.fmp.multispots.dlg.JComponents.CapillariesWithTimeTableModel;
import plugins.fmp.multispots.experiment.Capillary;
import plugins.fmp.multispots.experiment.Experiment;
import plugins.fmp.multispots.tools.ROI2DUtilities;



public class EditPositionWithTime extends JPanel implements ListSelectionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID 		= 1L;
	
	IcyFrame 					dialogFrame 		= null;
	
    private JButton				addItemButton		= new JButton("Add");
    private JButton				deleteItemButton	= new JButton("Delete");
    private JButton				saveCapillariesButton = new JButton("Save capillaries");
    private JCheckBox			showFrameButton		= new JCheckBox("Show frame");
    private JButton				fitToFrameButton	= new JButton("Fit capillaries to frame");
    private JTable 				tableView 			= new JTable();    
    
	private final String 		dummyname 			= "perimeter_enclosing_capillaries";
	private ROI2DPolygon 		envelopeRoi 		= null;
	private ROI2DPolygon 		envelopeRoi_initial	= null;
	private multiSPOTS 			parent0 			= null; 
	
	private CapillariesWithTimeTableModel capillariesWithTimeTablemodel = null;
	
		
	
	public void initialize (multiSPOTS parent0, Point pt) 
	{
		this.parent0 = parent0;
		capillariesWithTimeTablemodel = new CapillariesWithTimeTableModel(parent0.expListCombo);
		
		JPanel topPanel = new JPanel(new GridLayout(3, 1));
		FlowLayout flowLayout = new FlowLayout(FlowLayout.LEFT); 
		
		JPanel panel1 = new JPanel (flowLayout);
		panel1.add(new JLabel("Viewer frame T:"));
		panel1.add(addItemButton);
		panel1.add(deleteItemButton);
		topPanel.add(panel1);
        
        JPanel panel2 = new JPanel (flowLayout);
        panel2.add(showFrameButton);
        panel2.add(fitToFrameButton);
        panel2.add(saveCapillariesButton);
        topPanel.add(panel2);
        
        JPanel panel3 = new JPanel (flowLayout);
        panel3.add(saveCapillariesButton);
        topPanel.add(panel3);
        
        
        tableView.setModel(capillariesWithTimeTablemodel);
	    tableView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    tableView.setPreferredScrollableViewportSize(new Dimension(180, 120));
	    tableView.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(tableView);
        JPanel tablePanel = new JPanel();
		tablePanel.add(scrollPane);
        
		dialogFrame = new IcyFrame ("Edit capillaries position", true, true);	
		dialogFrame.add(topPanel, BorderLayout.NORTH);
		dialogFrame.add(tablePanel, BorderLayout.CENTER);
		dialogFrame.setLocation(pt);
		
		dialogFrame.pack();
		dialogFrame.addToDesktopPane();
		dialogFrame.requestFocus();
		dialogFrame.setVisible(true);
		
		defineActionListeners();
		defineSelectionListener();
		
		fitToFrameButton.setEnabled(false);	
	}
	
	private void defineActionListeners() {
		
		fitToFrameButton.addActionListener(new ActionListener () { 
			@Override public void actionPerformed( final ActionEvent e ) { 
				moveAllCapillaries();
			}});
		
		showFrameButton.addActionListener(new ActionListener () { 
			@Override public void actionPerformed( final ActionEvent e ) { 
				fitToFrameButton.setEnabled(showFrameButton.isSelected());
				showFrame(showFrameButton.isSelected()) ;
			}});
		
		addItemButton.addActionListener(new ActionListener () { 
			@Override public void actionPerformed( final ActionEvent e ) { 
				addTableItem();
			}});
		
		deleteItemButton.addActionListener(new ActionListener () { 
			@Override public void actionPerformed( final ActionEvent e ) { 
				int selectedRow = tableView.getSelectedRow();
				deleteTableItem(selectedRow);
			}});
		
		saveCapillariesButton.addActionListener(new ActionListener () { 
			@Override public void actionPerformed( final ActionEvent e ) { 
				int selectedRow = tableView.getSelectedRow();
				saveCapillaries(selectedRow);
			}});
	}
	
	private void defineSelectionListener() {
		tableView.getSelectionModel().addListSelectionListener(this);
	}
	
	void close() {
		dialogFrame.close();
	}
	
	private void moveAllCapillaries() {
		if (envelopeRoi == null) 
			return;
		Point2D pt0 = envelopeRoi_initial.getPosition2D();
		Point2D pt1 = envelopeRoi.getPosition2D();
		envelopeRoi_initial = new ROI2DPolygon(envelopeRoi.getPolygon2D());
		double deltaX = pt1.getX() - pt0.getX();
		double deltaY = pt1.getY() - pt0.getY();
		shiftPositionOfCapillaries(deltaX, deltaY);		
	}
	
	private void shiftPositionOfCapillaries(double deltaX, double deltaY) {
		Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();;
		if (exp == null) 
			return;
		Sequence seq = exp.seqCamData.seq;
		ArrayList<ROI2D> listRois = seq.getROI2Ds();
		for (ROI2D roi : listRois) {
			if (!roi.getName().contains("line")) 
				continue;
			Point2D point2d = roi.getPosition2D();
			roi.setPosition2D(new Point2D.Double(point2d.getX()+deltaX, point2d.getY()+ deltaY));
		}
	}
	
	private void showFrame(boolean show) {
		Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
		if (exp == null)
			return;
		
		if (show)
			addFrameAroundCapillaries(exp.seqCamData.seq);
		else 
			removeFrameAroundCapillaries(exp.seqCamData.seq);
	}
	
	private void addFrameAroundCapillaries(Sequence seq) {
		Polygon2D polygon = ROI2DUtilities.getPolygonEnclosingCapillaries(seq.getROI2Ds());
		envelopeRoi_initial = new ROI2DPolygon (polygon);
		envelopeRoi = new ROI2DPolygon(polygon);
		envelopeRoi.setName(dummyname);
		envelopeRoi.setColor(Color.YELLOW);
		
		seq.addROI(envelopeRoi);
		seq.setSelectedROI(envelopeRoi);
	}

	private void removeFrameAroundCapillaries(Sequence seq) {
		ArrayList<ROI2D> listRois = seq.getROI2Ds();
		for (ROI2D roi: listRois) {
			if (roi.getName().equals(dummyname)) {
				seq.removeROI(roi);
				break;
			}
		}
		envelopeRoi = null;
	}
	
	private void addTableItem() {
		Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
		if (exp == null) return;

		Viewer v = exp.seqCamData.seq.getFirstViewer();
		long intervalT = v.getPositionT();
		
		if (exp.capillaries.findKymoROI2DIntervalStart(intervalT) < 0) {
			exp.capillaries.addKymoROI2DInterval(intervalT);
		}
	}
	
	private void deleteTableItem(int selectedRow) {
		Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
		if (exp == null) return;
		
		Viewer v = exp.seqCamData.seq.getFirstViewer();
		long intervalT = v.getPositionT();
        
		if (exp.capillaries.findKymoROI2DIntervalStart(intervalT) >= 0) {
			exp.capillaries.deleteKymoROI2DInterval(intervalT);
		}
	}
	
	private void displayCapillariesForSelectedInterval(int selectedRow) {
		Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
		if (exp == null) return;
		Sequence seq = exp.seqCamData.seq;
		
		int intervalT =  (int) exp.capillaries.getKymoROI2DIntervalsStartAt(selectedRow);
		seq.removeAllROI();	
		List<ROI2D> listRois = new ArrayList<ROI2D>();
		for (Capillary cap: exp.capillaries.capillariesList) {
			listRois.add(cap.getROI2DKymoAtIntervalT((int) intervalT).getRoi());
		}
		seq.addROIs(listRois, false);
		
		Viewer v = seq.getFirstViewer();
		v.setPositionT((int)intervalT);
	}
	
	private void saveCapillaries(int selectedRow) {
		Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
		if (exp == null) 
			return;
		Sequence seq = exp.seqCamData.seq;
		
		int intervalT =  (int) exp.capillaries.getKymoROI2DIntervalsStartAt(selectedRow);
		List<ROI2D> listRois = seq.getROI2Ds();
		for (ROI2D roi: listRois) {
			if (!roi.getName().contains("line")) 
				continue;
			Capillary cap = exp.capillaries.getCapillaryFromRoiName(roi.getName());
			if (cap != null) {
				ROI2D roilocal = (ROI2D) roi.getCopy();
				cap.getROI2DKymoAtIntervalT(intervalT).setRoi(roilocal);
			}
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if(e.getValueIsAdjusting()) 
			return;
		
        int selectedRow = tableView.getSelectedRow();
        if (selectedRow < 0) {
        	tableView.setRowSelectionInterval(0, 0);
        	selectedRow = 0;
        }    
        displayCapillariesForSelectedInterval(selectedRow);
        showFrame(showFrameButton.isSelected()) ;
	}

	
}
