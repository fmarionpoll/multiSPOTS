package plugins.fmp.multiSPOTS.dlg.JComponents;

import javax.swing.table.AbstractTableModel;

import plugins.fmp.multiSPOTS.experiment.Capillary;
import plugins.fmp.multiSPOTS.experiment.Experiment;


public class CapillaryTableModel extends AbstractTableModel  
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6325792669154093747L;
	private ExperimentCombo expList 	= null;
	String columnNames[] = { "Name", "Cage", "N flies", "Volume", "Stimulus", "Concentration"};
	
	
	public CapillaryTableModel (ExperimentCombo expList) {
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
			return exp.capillaries.capillariesList.size();
    	}
        return 0;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
    	Capillary cap = getCapillaryAt(rowIndex);
    	if (cap != null) {
        	switch (columnIndex) {
            case 0: return cap.getRoiName();
            case 1: return cap.cageID;
            case 2: return cap.nFlies;
            case 3: return cap.volume;
            case 4: return cap.stimulus;
            case 5: return cap.concentration;
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
    	Capillary cap = getCapillaryAt(rowIndex);
    	if (cap != null) {
        	switch (columnIndex) {
            case 0: cap.setRoiName(aValue.toString()); break;
            case 1: cap.cageID = (int) aValue; break;
            case 2: cap.nFlies = (int) aValue; break;
            case 3: cap.volume = (double) aValue; break;
            case 4: cap.stimulus = aValue.toString(); break;
            case 5: cap.concentration = aValue.toString(); break;
        	}
    	}
    }
    
    private Capillary getCapillaryAt(int rowIndex) {
		Capillary cap = null;
    	if (expList != null && expList.getSelectedIndex() >=0 ) {
    		Experiment exp = (Experiment) expList.getSelectedItem();
    		cap = exp.capillaries.capillariesList.get(rowIndex);
    	}
    	return cap;
	}

}
