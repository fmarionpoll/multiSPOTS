package plugins.fmp.multiSPOTS.tools.JComponents;

import javax.swing.table.AbstractTableModel;

import plugins.fmp.multiSPOTS.experiment.Experiment;
import plugins.fmp.multiSPOTS.experiment.Spot;

public class TableModelSpot extends AbstractTableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6325792669154093747L;
	private JComboBoxExperiment expList = null;
	String columnNames[] = { "Name", "Cage", "N flies", "N pixels", "Volume", "Stimulus", "Concentration" };

	public TableModelSpot(JComboBoxExperiment expList) {
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
		case 0:
			return String.class;
		case 1:
			return Integer.class;
		case 2:
			return Integer.class;
		case 3:
			return Integer.class;
		case 4:
			return Double.class;
		case 5:
			return String.class;
		case 6:
			return String.class;
		}
		return String.class;
	}

	@Override
	public String getColumnName(int column) {
		return columnNames[column];
	}

	@Override
	public int getRowCount() {
		if (expList != null && expList.getSelectedIndex() >= 0) {
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
			case 0:
				return spot.getRoiName();
			case 1:
				return spot.cageIndex;
			case 2:
				return spot.spotNFlies;
			case 3:
				return spot.spotNPixels;
			case 4:
				return spot.spotVolume;
			case 5:
				return spot.spotStim;
			case 6:
				return spot.spotConc;
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
			case 0:
				spot.setRoiName(aValue.toString());
				break;
			case 1:
				spot.cageIndex = (int) aValue;
				break;
			case 2:
				spot.spotNFlies = (int) aValue;
				break;
			case 3:
				spot.spotNPixels = (int) aValue;
				break;
			case 4:
				spot.spotVolume = (double) aValue;
				break;
			case 5:
				spot.spotStim = aValue.toString();
				break;
			case 6:
				spot.spotConc = aValue.toString();
				break;
			}
		}
	}

	private Spot getSpotAt(int rowIndex) {
		Spot spot = null;
		if (expList != null && expList.getSelectedIndex() >= 0) {
			Experiment exp = (Experiment) expList.getSelectedItem();
			spot = exp.spotsArray.spotsList.get(rowIndex);
		}
		return spot;
	}
}
