package plugins.fmp.multispots.series;

import java.util.ArrayList;

import plugins.fmp.multispots.experiment.Spot;
import plugins.fmp.multispots.experiment.Experiment;
import plugins.fmp.multispots.experiment.SequenceKymos;



public class ClipCagesMeasuresToSmallest extends BuildSeries 
{
	void analyzeExperiment(Experiment exp) 
	{
		exp.loadMCExperiment();
		exp.loadMCCapillaries();
		if (exp.loadKymographs()) 
		{
			SequenceKymos seqKymos = exp.seqKymos;
			ArrayList<Integer> listCageID = new ArrayList<Integer> (seqKymos.nTotalFrames);
			for (int t= 0; t< seqKymos.nTotalFrames; t++) 
			{
				Spot tcap = exp.capillaries.spotsList.get(t);
				int tcage = tcap.capCageID;
				if (findCageID(tcage, listCageID)) 
					continue;
				listCageID.add(tcage);
				int minLength = findMinLength(exp, t, tcage);
				for (int tt = t; tt< seqKymos.nTotalFrames; tt++) 
				{
					Spot ttcap = exp.capillaries.spotsList.get(tt);
					int ttcage = ttcap.capCageID;
					if (ttcage == tcage && ttcap.ptsTop.polylineLevel.npoints > minLength)
						ttcap.cropMeasuresToNPoints(minLength);
				}
			}
			exp.saveCapillariesMeasures();
		}
		exp.seqCamData.closeSequence();
		exp.seqKymos.closeSequence();
	}
	
	boolean findCageID(int cageID, ArrayList<Integer> listCageID) 
	{
		boolean found = false;
		for (int iID: listCageID) 
		{
			if (iID == cageID) 
			{
				found = true;
				break;
			}
		}
		return found;
	}
	
	private int findMinLength (Experiment exp, int t, int tcage ) 
	{
		Spot tcap = exp.capillaries.spotsList.get(t);
		int minLength = tcap.ptsTop.polylineLevel.npoints;
		for (int tt = t; tt< exp.capillaries.spotsList.size(); tt++) 
		{
			Spot ttcap = exp.capillaries.spotsList.get(tt);
			int ttcage = ttcap.capCageID;
			if (ttcage == tcage) 
			{
				int dataLength = ttcap.ptsTop.polylineLevel.npoints;
				if (dataLength < minLength)
					minLength = dataLength;
			}
		}
		return minLength;
	}
}