package plugins.fmp.multispots.dlg.spots;



import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import icy.gui.frame.IcyFrame;
import plugins.fmp.multispots.multiSPOTS;
import plugins.fmp.multispots.dlg.JComponents.CapillaryTableModel;
import plugins.fmp.multispots.experiment.Spot;
import plugins.fmp.multispots.experiment.Experiment;


public class InfosSpotsTable extends JPanel 
{
	/**
	 * 
	 */
	private static final long serialVersionUID 		= -8611587540329642259L;
	IcyFrame 					dialogFrame 		= null;
    private JTable 				tableView 			= new JTable();
	private CapillaryTableModel capillaryTableModel = null;
	private JButton				copyButton 			= new JButton("Copy table");
	private JButton				pasteButton 		= new JButton("Paste");
	private JButton				duplicateLRButton 	= new JButton("Duplicate cell to L/R");
	private JButton				duplicateCageButton	= new JButton("Duplicate cage stim");
	
	private JButton				exchangeLRButton 	= new JButton("Exchg L/R");
	
	private JButton				duplicateAllButton 	= new JButton("Duplicate cell to all");
	private JButton				getNfliesButton 	= new JButton("Get n flies from cage");
	private JButton				getCageNoButton		= new JButton("Set cage n#");
	private JButton				noFliesButton 		= new JButton("Cages0/0: no flies");
	private multiSPOTS 			parent0 			= null; 
	private List <Spot> 	capillariesArrayCopy= null;
	
	
	
	public void initialize (multiSPOTS parent0, List <Spot> capCopy) 
	{
		this.parent0 = parent0;
		capillariesArrayCopy = capCopy;
		
		capillaryTableModel = new CapillaryTableModel(parent0.expListCombo);
	    tableView.setModel(capillaryTableModel);
	    tableView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    tableView.setPreferredScrollableViewportSize(new Dimension(500, 400));
	    tableView.setFillsViewportHeight(true);
	    TableColumnModel columnModel = tableView.getColumnModel();
	    DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
	    centerRenderer.setHorizontalAlignment( JLabel.CENTER );
	    for (int i = 0 ; i < capillaryTableModel.getColumnCount(); i++) {
	    	TableColumn col = columnModel.getColumn(i);
	    	col.setCellRenderer( centerRenderer );
	    	}
        JScrollPane scrollPane = new JScrollPane(tableView);
        
		JPanel topPanel = new JPanel(new GridLayout(2, 1));
		FlowLayout flowLayout = new FlowLayout(FlowLayout.LEFT); 
		JPanel panel1 = new JPanel (flowLayout);
		panel1.add(copyButton);
        panel1.add(pasteButton);
        panel1.add(duplicateLRButton);
        panel1.add(duplicateAllButton);
        panel1.add(exchangeLRButton);
        topPanel.add(panel1);
        
        JPanel panel2 = new JPanel (flowLayout);
        panel2.add(getCageNoButton);
        panel2.add(getNfliesButton);
        panel2.add(noFliesButton);
        panel2.add(duplicateCageButton);
        topPanel.add(panel2);
        
        JPanel tablePanel = new JPanel();
		tablePanel.add(scrollPane);
        
		dialogFrame = new IcyFrame ("Capillaries properties", true, true);	
		dialogFrame.add(topPanel, BorderLayout.NORTH);
		dialogFrame.add(tablePanel, BorderLayout.CENTER);
		
		dialogFrame.pack();
		dialogFrame.addToDesktopPane();
		dialogFrame.requestFocus();
		dialogFrame.center();
		dialogFrame.setVisible(true);
		defineActionListeners();
		pasteButton.setEnabled(capillariesArrayCopy.size() > 0);
	}
	
	
	private void defineActionListeners() 
	{
		copyButton.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				Experiment exp =(Experiment) parent0.expListCombo.getSelectedItem();
				if (exp != null)
					copyInfos(exp);
			}});
		
		pasteButton.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
				if (exp != null)
					pasteInfos(exp);
				capillaryTableModel.fireTableDataChanged();
			}});
		
		noFliesButton.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{
				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
				if (exp != null)
				{
					setFliesNumbers(exp);
					capillaryTableModel.fireTableDataChanged();
				}
			}});

		duplicateLRButton.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				Experiment exp = (Experiment)parent0.expListCombo.getSelectedItem();
				if (exp != null)
					duplicateLR(exp);
			}});
		
		duplicateCageButton.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				Experiment exp = (Experiment)parent0.expListCombo.getSelectedItem();
				if (exp != null)
					duplicateCage(exp);
			}});
		
		exchangeLRButton.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				Experiment exp = (Experiment)parent0.expListCombo.getSelectedItem();
				if (exp == null || exp.capillaries.capillariesDescription.grouping != 2)
					return;
				exchangeLR(exp);
			}});
		
		duplicateAllButton.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
				if (exp != null)
				{
					duplicateAll(exp);
				}
			}});
		
		getNfliesButton.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
				if (exp != null && exp.cages.cagesList.size() > 0) 
				{
					exp.cages.transferNFliesFromCagesToCapillaries(exp.capillaries.capillariesList);
					capillaryTableModel.fireTableDataChanged();
				}
			}});
		
		getCageNoButton.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
				if (exp != null)
				{
					exp.cages.setCageNbFromName(exp.capillaries.capillariesList);
					capillaryTableModel.fireTableDataChanged();
				}
			}});
	}
	
	void close() 
	{
		dialogFrame.close();
	}
	
	private void exchangeLR(Experiment exp) 
	{
		int columnIndex = tableView.getSelectedColumn();
		if (columnIndex < 0) 
			columnIndex = 5;
		String side0 =  exp.capillaries.capillariesList.get(0).getCapillarySide();
		Spot cap0 = new Spot(); 
		storeCapillaryValues(exp.capillaries.capillariesList.get(0), cap0);
		Spot cap1 = new Spot();
		storeCapillaryValues(exp.capillaries.capillariesList.get(1), cap1);
	
		for (Spot cap: exp.capillaries.capillariesList) 
		{
			if ((cap.getCapillarySide().equals(side0)))
				switchCapillaryValue(cap1, cap, columnIndex);
			else 
				switchCapillaryValue(cap0, cap, columnIndex);
		}
	}
	
	private void storeCapillaryValues(Spot sourceCapillary, Spot destinationCapillary) 
	{
		destinationCapillary.capNFlies = sourceCapillary.capNFlies; 
		destinationCapillary.capVolume = sourceCapillary.capVolume;
		destinationCapillary.capStimulus = sourceCapillary.capStimulus;
		destinationCapillary.capConcentration = sourceCapillary.capConcentration;
		destinationCapillary.capSide = sourceCapillary.capSide;
	}
	
	private void switchCapillaryValue(Spot sourceCapillary, Spot destinationCapillary, int columnIndex) 
	{
		switch (columnIndex) 
    	{
        case 2: destinationCapillary.capNFlies = sourceCapillary.capNFlies; break;
        case 3: destinationCapillary.capVolume = sourceCapillary.capVolume; break;
        case 4: destinationCapillary.capStimulus = sourceCapillary.capStimulus; break;
        case 5: destinationCapillary.capConcentration = sourceCapillary.capConcentration; break;
        default: break;
    	}
		
	}
	
	private void copyInfos(Experiment exp)
	{
		capillariesArrayCopy.clear();
		for (Spot cap: exp.capillaries.capillariesList ) 
			capillariesArrayCopy.add(cap);
		pasteButton.setEnabled(true);	
	}
	
	private void pasteInfos(Experiment exp)
	{
		for (Spot capFrom: capillariesArrayCopy ) 
		{
			capFrom.valid = false;
			for (Spot capTo: exp.capillaries.capillariesList) 
			{
				if (!capFrom.getRoiName().equals (capTo.getRoiName()))
					continue;
				capFrom.valid = true;
				capTo.capCageID = capFrom.capCageID;
				capTo.capNFlies = capFrom.capNFlies;
				capTo.capVolume = capFrom.capVolume;
				capTo.capStimulus = capFrom.capStimulus;
				capTo.capConcentration = capFrom.capConcentration;
			}
		}
	}
	
	private void setFliesNumbers(Experiment exp)
	{
		int ncapillaries =  exp.capillaries.capillariesList.size();
		for (int i=0; i < ncapillaries; i++) 
		{
			Spot cap = exp.capillaries.capillariesList.get(i);
			if (i< 2 || i >= ncapillaries-2) {
				cap.capNFlies = 0;
			}
			else 
			{
				cap.capNFlies = 1;
			}
		}
	}
	
	
	private void duplicateLR(Experiment exp)
	{
		int rowIndex = tableView.getSelectedRow();
		int columnIndex = tableView.getSelectedColumn();
		if (rowIndex < 0)
			return;
		
		Spot cap0 = exp.capillaries.capillariesList.get(rowIndex);	
		String side = cap0.getCapillarySide();
		int modulo2 = 0;
		if (side.equals("L"))
			modulo2 = 0;
		else if (side.equals("R"))
			modulo2 = 1;
		else
			modulo2 = Integer.valueOf(cap0.getCapillarySide()) % 2;
		
		for (Spot cap: exp.capillaries.capillariesList) 
		{
			if (cap.getKymographName().equals(cap0.getKymographName()))
				continue;
			if ((exp.capillaries.capillariesDescription.grouping == 2) && (!cap.getCapillarySide().equals(side)))
				continue;
			else 
			{
				try 
				{
					int mod = Integer.valueOf(cap.getCapillarySide()) % 2;
					if (mod != modulo2)
						continue;
				} 
				catch (NumberFormatException nfe) 
				{
					if (!cap.getCapillarySide().equals(side))
						continue;
				}
			}
        	switch (columnIndex) 
        	{
            case 2: cap.capNFlies = cap0.capNFlies; break;
            case 3: cap.capVolume = cap0.capVolume; break;
            case 4: cap.capStimulus = cap0.capStimulus; break;
            case 5: cap.capConcentration = cap0.capConcentration; break;
            default: break;
        	}					
		}
	}
	
	private void duplicateAll(Experiment exp)
	{
		int rowIndex = tableView.getSelectedRow();
		int columnIndex = tableView.getSelectedColumn();
		if (rowIndex < 0) 
			return;
		
		Spot cap0 = exp.capillaries.capillariesList.get(rowIndex);	
		for (Spot cap: exp.capillaries.capillariesList) 
		{
			if (cap.getKymographName().equals(cap0.getKymographName()))
				continue;
			switch (columnIndex) 
			{
            case 2: cap.capNFlies = cap0.capNFlies; break;
            case 3: cap.capVolume = cap0.capVolume; break;
            case 4: cap.capStimulus = cap0.capStimulus; break;
            case 5: cap.capConcentration = cap0.capConcentration; break;
            default: break;
        	}					
		}	
	}
	
	private void duplicateCage(Experiment exp)
	{
		int rowIndex = tableView.getSelectedRow();
		int columnIndex = tableView.getSelectedColumn();
		if (rowIndex < 0)
			return;
		
		Spot capFrom = exp.capillaries.capillariesList.get(rowIndex);	
		int cageFrom = capFrom.capCageID; 
		int cageTo = -1;
				
		int nCapillariesPerCage = getCageNCapillaries(exp, cageFrom);
		int indexFirstCapillaryOfCageFrom = getIndexFirstCapillaryOfCage(exp, cageFrom);
		int indexFirstCapillaryOfCageTo = -1;
		
		for (int i = 0; i < exp.capillaries.capillariesList.size(); i++) 
		{
			Spot cap = exp.capillaries.capillariesList.get(i);
			if (cap.capCageID == cageFrom)
				continue;
			
			if (cap.capCageID != cageTo) 
			{
				cageTo = cap.capCageID;
				indexFirstCapillaryOfCageTo = getIndexFirstCapillaryOfCage(exp, cageTo);
			}
						
			if (getCageNCapillaries(exp, cap.capCageID) != nCapillariesPerCage)
				continue;

			int indexFrom = i - indexFirstCapillaryOfCageTo + indexFirstCapillaryOfCageFrom;
			Spot cap0 = exp.capillaries.capillariesList.get(indexFrom);

        	switch (columnIndex) 
        	{
            case 2: cap.capNFlies = cap0.capNFlies; break;
            case 3: cap.capVolume = cap0.capVolume; break;
            case 4: cap.capStimulus = cap0.capStimulus; break;
            case 5: cap.capConcentration = cap0.capConcentration; break;
            default: break;
        	}					
		}
		
	}
	
	private int getCageNCapillaries(Experiment exp, int cageID)
	{
		int nCapillaries = 0;
		for (Spot cap: exp.capillaries.capillariesList)
		{
			if (cap.capCageID == cageID)
				nCapillaries ++;
		}
			
		return nCapillaries;
	}
	
	private int getIndexFirstCapillaryOfCage(Experiment exp, int cageID) 
	{
		int index = -1;
		for (int i = 0; i < exp.capillaries.capillariesList.size(); i++) 
		{
			Spot cap = exp.capillaries.capillariesList.get(i);
			if (cap.capCageID == cageID) {
				index = i;
				break;
			}
		}
		return index;		
	}
}
