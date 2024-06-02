package plugins.fmp.multiSPOTS.series;

public class ResultsThreshold {
	double sum = 0.;
	double sumTot = 0.;
	double nPointsOverThreshold = 0.;
	double npoints_in = 0;
	
	public ResultsThreshold () {
	}
	
	public ResultsThreshold (double sum, double sumTot, double npointsOver, double npoints) {
		this.sum = sum;
		this.sumTot = sumTot;
		this.nPointsOverThreshold = npointsOver;
		this.npoints_in = npoints;
	}
}
