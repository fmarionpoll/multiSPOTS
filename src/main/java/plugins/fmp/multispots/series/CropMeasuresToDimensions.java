package plugins.fmp.multispots.series;

import plugins.fmp.multispots.experiment.Experiment;

public class CropMeasuresToDimensions  extends BuildSeries  {
	void analyzeExperiment(Experiment exp) 
	{
		exp.loadMCExperiment();
		exp.loadMCCapillaries();
		if (exp.loadKymographs()) 
		{
			exp.cropCapillaryMeasuresDimensions();
			exp.saveCapillariesMeasures(exp.getKymosBinFullDirectory());
		}
		exp.seqCamData.closeSequence();
		exp.seqKymos.closeSequence();
	}
}
