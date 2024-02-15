package plugins.fmp.multispots.series;

import plugins.fmp.multispots.experiment.Spot;
import plugins.fmp.multispots.experiment.Experiment;
import plugins.fmp.multispots.experiment.SequenceKymos;

public class CurvesRestoreLength extends BuildSeries 
{
	void analyzeExperiment(Experiment exp) 
	{
		exp.loadMCExperiment();
		exp.loadMCCapillaries();
		if (exp.loadKymographs()) 
		{
			SequenceKymos seqKymos = exp.seqKymos;
			for (int t= 0; t< seqKymos.nTotalFrames; t++) 
			{
				Spot cap = exp.capillaries.spotsList.get(t);
				cap.restoreClippedMeasures();
			}
			exp.saveCapillariesMeasures();
		}
		exp.seqCamData.closeSequence();
		exp.seqKymos.closeSequence();
	}
}
