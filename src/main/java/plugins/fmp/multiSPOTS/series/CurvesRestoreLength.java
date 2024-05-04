package plugins.fmp.multiSPOTS.series;

import plugins.fmp.multiSPOTS.experiment.Capillary;
import plugins.fmp.multiSPOTS.experiment.Experiment;
import plugins.fmp.multiSPOTS.experiment.SequenceKymos;

public class CurvesRestoreLength extends BuildSeries 
{
	void analyzeExperiment(Experiment exp) 
	{
		exp.loadXML_MCExperiment();
		exp.loadMCCapillaries();
		if (exp.loadKymographs()) 
		{
			SequenceKymos seqKymos = exp.seqKymos;
			for (int t= 0; t< seqKymos.nTotalFrames; t++) 
			{
				Capillary cap = exp.capillaries.capillariesList.get(t);
				cap.restoreClippedMeasures();
			}
			exp.save_CapillariesMeasures();
		}
		exp.seqCamData.closeSequence();
		exp.seqKymos.closeSequence();
	}
}
