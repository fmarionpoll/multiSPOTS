package plugins.fmp.multiSPOTS.tools.toExcel;

import plugins.fmp.multiSPOTS.tools.JComponents.JComboBoxExperiment;

public class XLSExportOptions {
	public boolean xyImage = true;
	public boolean xyCage = true;
	public boolean xyCapillaries = true;
	public boolean ellipseAxes = false;

	public boolean distance = false;
	public boolean alive = true;
	public boolean sleep = true;
	public int sleepThreshold = 5;

	public boolean topLevel = true;
	public boolean topLevelDelta = false;
	public boolean bottomLevel = false;
	public boolean derivative = false;
	public boolean lrPI = true;
	public double lrPIThreshold = 0.;

	public boolean spotAreas = true;
	public boolean sum = true;
	public boolean sum2 = true;
	public boolean nPixels = true;

	public boolean autocorrelation = false;
	public boolean crosscorrelation = false;
	public boolean crosscorrelationLR = false;
	public int nbinscorrelation = 40;

	public boolean sumPerCage = true;
	public boolean subtractT0 = true;
	public boolean relativeToT0 = true;
	public boolean onlyalive = true;
	public boolean subtractEvaporation = true;

	public boolean transpose = false;
	public boolean duplicateSeries = true;
	public int buildExcelStepMs = 1;
	public int buildExcelUnitMs = 1;
	public boolean fixedIntervals = false;
	public long startAll_Ms = 0;
	public long endAll_Ms = 999999;
	public boolean exportAllFiles = true;
	public boolean absoluteTime = false;
	public boolean collateSeries = false;
	public boolean padIntervals = true;

	public int expIndexFirst = -1;
	public int expIndexLast = -1;
	public JComboBoxExperiment expList = null;

	// internal parameters
	public boolean trim_alive = false;
	public boolean compensateEvaporation = false;
	public EnumXLSExportType exportType;
}
