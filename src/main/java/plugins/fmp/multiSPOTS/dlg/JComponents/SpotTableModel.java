package plugins.fmp.multiSPOTS.dlg.JComponents;

import javax.swing.table.AbstractTableModel;

import plugins.fmp.multiSPOTS.experiment.Experiment;
import plugins.fmp.multiSPOTS.experiment.Spot;

public class SpotTableModel extends AbstractTableModel  
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6325792669154093747L;
	private ExperimentCombo expList 	= null;
	String columnNames[] = { "Name", "Cage", "N flies", "Volume", "Stimulus", "Concentration"};
	
	
	public SpotTableModel (ExperimentCombo expList) {
		super();
		this.expList = expList;
	}
	
	@Override
	public int getColumnCount() {
		return columnNames.length;
	}
	
    @Override
    public Class<?> getColumnClass(int columnIndex) {
    	switch (columnIndex) {
    	case 0: return String.class;
    	case 1: return Integer.class;
    	case 2: return Integer.class;
    	case 3:	return Double.class;
    	case 4: return String.class;
    	case 5: return String.class;
        }
    	return String.class;
    }
    
	@Override
	public String getColumnName(int column) {
		return columnNames[column];
	}
	
    @Override
    public int getRowCount() {
    	if (expList != null && expList.getSelectedIndex() >= 0 ) {
    		Experiment exp = (Experiment) expList.getSelectedItem();
			return exp.spotsArray.spotsList.size();
    	}
        return 0;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
    	Spot spot = getSpotAt(rowIndex);
    	if (spot != null) {
        	switch (columnIndex) {
            case 0: return spot.getRoiName();
            case 1: return spot.cageID;
            case 2: return spot.nFlies;
            case 3: return spot.volume;
            case 4: return spot.stimulus;
            case 5: return spot.concentration;
        	}
    	}
    	return null;
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
    	switch (columnIndex) {
        case 0: 
        	return false;
        default:
        	return true;
    	}
    }
    
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    	Spot spot = getSpotAt(rowIndex);
    	if (spot != null) {
        	switch (columnIndex) {
            case 0: spot.setRoiName(aValue.toString()); break;
            case 1: spot.cageID = (int) aValue; break;
            case 2: spot.nFlies = (int) aValue; break;
            case 3: spot.volume = (double) aValue; break;
            case 4: spot.stimulus = aValue.toString(); break;
            case 5: spot.concentration = aValue.toString(); break;
        	}
    	}
    }
    
    private Spot getSpotAt(int rowIndex) {
		Spot spot = null;
    	if (expList != null && expList.getSelectedIndex() >=0 ) {
    		Experiment exp = (Experiment) expList.getSelectedItem();
    		spot = exp.spotsArray.spotsList.get(rowIndex);
    	}
    	return spot;
	}
}
