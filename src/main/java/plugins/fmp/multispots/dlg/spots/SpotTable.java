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
import plugins.fmp.multispots.MultiSPOTS;
import plugins.fmp.multispots.experiment.Experiment;
import plugins.fmp.multispots.experiment.Spot;
import plugins.fmp.multispots.dlg.JComponents.SpotTableModel;

public class SpotTable extends JPanel 
{
	/**
	 * 
	 */
	private static final long serialVersionUID 		= -8611587540329642259L;
	IcyFrame 					dialogFrame 		= null;
    private JTable 				tableView 			= new JTable();
	private SpotTableModel 		spotTableModel = null;
	private JButton				copyButton 			= new JButton("Copy table");
	private JButton				pasteButton 		= new JButton("Paste");
	private JButton				duplicateLRButton 	= new JButton("Duplicate cell to L/R");
	private JButton				duplicateCageButton	= new JButton("Duplicate cage stim");
	
	private JButton				exchangeLRButton 	= new JButton("Exchg L/R");
	
	private JButton				duplicateAllButton 	= new JButton("Duplicate cell to all");
	private JButton				getNfliesButton 	= new JButton("Get n flies from cage");
	private JButton				getCageNoButton		= new JButton("Set cage n#");
	private JButton				noFliesButton 		= new JButton("Cages0/0: no flies");
	private MultiSPOTS 			parent0 			= null; 
	private List <Spot> 		spotsArrayCopy 		= null;
	
	
	
	public void initialize (MultiSPOTS parent0, List <Spot> spotCopy) 
	{
		this.parent0 = parent0;
		spotsArrayCopy = spotCopy;
		
		spotTableModel = new SpotTableModel(parent0.expListCombo);
	    tableView.setModel(spotTableModel);
	    tableView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    tableView.setPreferredScrollableViewportSize(new Dimension(500, 400));
	    tableView.setFillsViewportHeight(true);
	    TableColumnModel columnModel = tableView.getColumnModel();
	    DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
	    centerRenderer.setHorizontalAlignment( JLabel.CENTER );
	    for (int i = 0 ; i < spotTableModel.getColumnCount(); i++) {
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
        
		dialogFrame = new IcyFrame ("Spots properties", true, true);	
		dialogFrame.add(topPanel, BorderLayout.NORTH);
		dialogFrame.add(tablePanel, BorderLayout.CENTER);
		
		dialogFrame.pack();
		dialogFrame.addToDesktopPane();
		dialogFrame.requestFocus();
		dialogFrame.center();
		dialogFrame.setVisible(true);
		defineActionListeners();
		
		pasteButton.setEnabled(spotsArrayCopy.size() > 0);
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
				spotTableModel.fireTableDataChanged();
			}});
		
		noFliesButton.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{
				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
				if (exp != null)
				{
					setFliesNumbers(exp);
					spotTableModel.fireTableDataChanged();
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
				if (exp == null || exp.spotsArray.spotsDescription.grouping != 2)
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
					exp.cages.transferNFliesFromCagesToSpots(exp.spotsArray.spotsList);
					spotTableModel.fireTableDataChanged();
				}
			}});
		
		getCageNoButton.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				Experiment exp = (Experiment) parent0.expListCombo.getSelectedItem();
				if (exp != null)
				{
					exp.cages.setCageNbFromSpotName(exp.spotsArray.spotsList);
					spotTableModel.fireTableDataChanged();
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
		String side0 =  exp.spotsArray.spotsList.get(0).getSpotSide();
		Spot spot0 = new Spot(); 
		copyAllSpotValues(exp.spotsArray.spotsList.get(0), spot0);
		Spot spot1 = new Spot();
		copyAllSpotValues(exp.spotsArray.spotsList.get(1), spot1);
	
		for (Spot spot: exp.spotsArray.spotsList) 
		{
			if ((spot.getSpotSide().equals(side0)))
				copySingleSpotValue(spot1, spot, columnIndex);
			else 
				copySingleSpotValue(spot0, spot, columnIndex);
		}
	}
	
	private void copyAllSpotValues(Spot sourceSpot, Spot destinationSpot) 
	{
		destinationSpot.nFlies = sourceSpot.nFlies; 
		destinationSpot.volume = sourceSpot.volume;
		destinationSpot.stimulus = sourceSpot.stimulus;
		destinationSpot.concentration = sourceSpot.concentration;
		destinationSpot.cageSide = sourceSpot.cageSide;
	}
	
	private void copySingleSpotValue(Spot sourceSpot, Spot destinationSpot, int columnIndex) 
	{
		switch (columnIndex) 
    	{
        case 2: destinationSpot.nFlies = sourceSpot.nFlies; break;
        case 3: destinationSpot.volume = sourceSpot.volume; break;
        case 4: destinationSpot.stimulus = sourceSpot.stimulus; break;
        case 5: destinationSpot.concentration = sourceSpot.concentration; break;
        default: break;
    	}
		
	}
	
	private void copyInfos(Experiment exp)
	{
		spotsArrayCopy.clear();
		for (Spot spot: exp.spotsArray.spotsList ) 
			spotsArrayCopy.add(spot);
		pasteButton.setEnabled(true);	
	}
	
	private void pasteInfos(Experiment exp)
	{
		for (Spot spotFrom: spotsArrayCopy ) 
		{
			spotFrom.valid = false;
			for (Spot spotTo: exp.spotsArray.spotsList) 
			{
				if (!spotFrom.getRoiName().equals (spotTo.getRoiName()))
					continue;
				spotFrom.valid = true;
				spotTo.cageID = spotFrom.cageID;
				spotTo.nFlies = spotFrom.nFlies;
				spotTo.volume = spotFrom.volume;
				spotTo.stimulus = spotFrom.stimulus;
				spotTo.concentration = spotFrom.concentration;
			}
		}
	}
	
	private void setFliesNumbers(Experiment exp)
	{
		int nspots =  exp.spotsArray.spotsList.size();
		for (int i = 0; i < nspots; i++) 
		{
			Spot spot = exp.spotsArray.spotsList.get(i);
			if (i< 2 || i >= nspots-2)
				spot.nFlies = 0;
			else 
				spot.nFlies = 1;
		}
	}
	
	
	private void duplicateLR(Experiment exp)
	{
		int rowIndex = tableView.getSelectedRow();
		int columnIndex = tableView.getSelectedColumn();
		if (rowIndex < 0)
			return;
		
		Spot spot0 = exp.spotsArray.spotsList.get(rowIndex);	
		String side = spot0.getSpotSide();
		int modulo2 = 0;
		if (side.equals("L"))
			modulo2 = 0;
		else if (side.equals("R"))
			modulo2 = 1;
		else
			modulo2 = Integer.valueOf(spot0.getSpotSide()) % 2;
		
		for (Spot spot: exp.spotsArray.spotsList) 
		{
			if (spot.getRoiName().equals(spot0.getRoiName()))
				continue;
			if ((exp.spotsArray.spotsDescription.grouping == 2) && (!spot.getSpotSide().equals(side)))
				continue;
			else 
			{
				try 
				{
					int mod = Integer.valueOf(spot.getSpotSide()) % 2;
					if (mod != modulo2)
						continue;
				} 
				catch (NumberFormatException nfe) 
				{
					if (!spot.getSpotSide().equals(side))
						continue;
				}
			}
        	switch (columnIndex) 
        	{
            case 2: spot.nFlies = spot0.nFlies; break;
            case 3: spot.volume = spot0.volume; break;
            case 4: spot.stimulus = spot0.stimulus; break;
            case 5: spot.concentration = spot0.concentration; break;
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
		
		Spot spot0 = exp.spotsArray.spotsList.get(rowIndex);	
		for (Spot spot: exp.spotsArray.spotsList) 
		{
			if (spot.getRoiName().equals(spot0.getRoiName()))
				continue;
			switch (columnIndex) 
			{
            case 2: spot.nFlies = spot0.nFlies; break;
            case 3: spot.volume = spot0.volume; break;
            case 4: spot.stimulus = spot0.stimulus; break;
            case 5: spot.concentration = spot0.concentration; break;
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
		
		Spot spotFrom = exp.spotsArray.spotsList.get(rowIndex);	
		int cageFrom = spotFrom.cageID; 
		int cageTo = -1;
				
		int nSpotsPerCage = getCageNSpots(exp, cageFrom);
		int indexFirstSpotOfCageFrom = getIndexFirstSpotOfCage(exp, cageFrom);
		int indexFirstSpotOfCageTo = -1;
		
		for (int i = 0; i < exp.spotsArray.spotsList.size(); i++) 
		{
			Spot spot = exp.spotsArray.spotsList.get(i);
			if (spot.cageID == cageFrom)
				continue;
			
			if (spot.cageID != cageTo) 
			{
				cageTo = spot.cageID;
				indexFirstSpotOfCageTo = getIndexFirstSpotOfCage(exp, cageTo);
			}
						
			if (getCageNSpots(exp, spot.cageID) != nSpotsPerCage)
				continue;

			int indexFrom = i - indexFirstSpotOfCageTo + indexFirstSpotOfCageFrom;
			Spot spot0 = exp.spotsArray.spotsList.get(indexFrom);

        	switch (columnIndex) 
        	{
            case 2: spot.nFlies = spot0.nFlies; break;
            case 3: spot.volume = spot0.volume; break;
            case 4: spot.stimulus = spot0.stimulus; break;
            case 5: spot.concentration = spot0.concentration; break;
            default: break;
        	}					
		}
		
	}
	
	private int getCageNSpots(Experiment exp, int cageID)
	{
		int nSpots = 0;
		for (Spot spot: exp.spotsArray.spotsList)
		{
			if (spot.cageID == cageID)
				nSpots ++;
		}
			
		return nSpots;
	}
	
	private int getIndexFirstSpotOfCage(Experiment exp, int cageID) 
	{
		int index = -1;
		for (int i = 0; i < exp.spotsArray.spotsList.size(); i++) 
		{
			Spot spot = exp.spotsArray.spotsList.get(i);
			if (spot.cageID == cageID) {
				index = i;
				break;
			}
		}
		return index;		
	}
}
