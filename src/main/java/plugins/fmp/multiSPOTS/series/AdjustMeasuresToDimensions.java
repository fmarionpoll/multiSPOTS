package plugins.fmp.multiSPOTS.series;


import plugins.fmp.multiSPOTS.experiment.Experiment;



public class AdjustMeasuresToDimensions  extends BuildSeries 
{
	void analyzeExperiment(Experiment exp) 
	{
		exp.loadXML_MCExperiment();
		exp.loadMCCapillaries();
		if (exp.loadKymographs()) 
		{
			exp.adjustCapillaryMeasuresDimensions();
			exp.saveCapillariesMeasures(exp.getKymosBinFullDirectory());
		}
		exp.seqCamData.closeSequence();
		exp.seqKymos.closeSequence();
	}


}
