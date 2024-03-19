package plugins.fmp.multiSPOTS.series;

import plugins.fmp.multiSPOTS.experiment.Experiment;

public class CropMeasuresToDimensions  extends BuildSeries  {
	void analyzeExperiment(Experiment exp) 
	{
		exp.loadXML_MCExperiment();
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
