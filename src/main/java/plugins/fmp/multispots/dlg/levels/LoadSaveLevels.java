package plugins.fmp.multispots.dlg.levels;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import icy.gui.frame.progress.ProgressFrame;
import icy.gui.util.FontUtil;
import plugins.fmp.multispots.multiSPOTS;
import plugins.fmp.multispots.experiment.Experiment;




public class LoadSaveLevels  extends JPanel 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3973928400949966679L;

	private JButton		loadMeasuresButton		= new JButton("Load");
	private JButton		saveMeasuresButton		= new JButton("Save");
	private multiSPOTS 	parent0 				= null;
	
	void init(GridLayout capLayout, multiSPOTS parent0) 
	{
		setLayout(capLayout);
		this.parent0 = parent0;
	
		JLabel loadsaveText = new JLabel ("-> File (xml) ", SwingConstants.RIGHT); 
		loadsaveText.setFont(FontUtil.setStyle(loadsaveText.getFont(), Font.ITALIC));
		
		FlowLayout flowLayout = new FlowLayout(FlowLayout.RIGHT);
		flowLayout.setVgap(0);
		JPanel panel1 = new JPanel(flowLayout);
		panel1.add(loadsaveText);
		panel1.add(loadMeasuresButton);
		panel1.add(saveMeasuresButton);
		panel1.validate();
		add(panel1);
		
		defineActionListeners();
	}
	
	private void defineActionListeners() 
	{
		loadMeasuresButton.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				Experiment exp =(Experiment)  parent0.expListCombo.getSelectedItem();
				if (exp != null)
					dlg_levels_loadCapillaries_Measures(exp);
			}}); 
		
		saveMeasuresButton.addActionListener(new ActionListener () 
		{ 
			@Override public void actionPerformed( final ActionEvent e ) 
			{ 
				Experiment exp =(Experiment)  parent0.expListCombo.getSelectedItem();
				if (exp != null)
				{
					dlg_levels_saveCapillaries_Measures(exp);
					firePropertyChange("MEASURES_SAVE", false, true);
				}
			}});	
	}

	public boolean dlg_levels_loadCapillaries_Measures(Experiment exp) 
	{
		boolean flag = false;
		if (exp.seqKymos != null ) 
		{
			ProgressFrame progress = new ProgressFrame("load capillary measures");
			flag = exp.loadCapillariesMeasures();
			if (flag) 
				exp.seqKymos.transferCapillariesMeasuresToKymos(exp.capillaries);
			progress.close();
		}
		return flag;
	}
	
	public boolean dlg_levels_saveCapillaries_Measures(Experiment exp) 
	{
		boolean flag = true;
		if (exp.seqKymos != null ) 
		{
			ProgressFrame progress = new ProgressFrame("save capillary measures");
			flag = exp.saveCapillariesMeasures(exp.getKymosBinFullDirectory());
			progress.close();
		}
		return flag;
	}
}
