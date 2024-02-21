package plugins.fmp.multispots.tools.toExcel;

import java.awt.Point;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import icy.gui.frame.progress.ProgressFrame;

import plugins.fmp.multispots.dlg.JComponents.ExperimentCombo;
import plugins.fmp.multispots.experiment.Cage;
import plugins.fmp.multispots.experiment.Experiment;
import plugins.fmp.multispots.experiment.XYTaSeriesArrayList;
import plugins.fmp.multispots.experiment.XYTaValue;
import plugins.fmp.multispots.tools.Comparators;



public class XLSExportMoveResults extends XLSExport 
{
	ExperimentCombo expList = null;
	List <XYTaSeriesArrayList> rowsForOneExp = new ArrayList <XYTaSeriesArrayList> ();
	
	public void exportToFile(String filename, XLSExportOptions opt) 
	{	
		System.out.println("XLSExpoportMove:exportToFile() start output");
		options = opt;
		expList = options.expList;

		boolean loadCapillaries = true;
		boolean loadDrosoTrack = true; 
		expList.loadListOfMeasuresFromAllExperiments(loadCapillaries, loadDrosoTrack);
//		expList.chainExperimentsUsingCamIndexes(options.collateSeries);
		expList.chainExperimentsUsingKymoIndexes(options.collateSeries);
		expList.setFirstImageForAllExperiments(options.collateSeries);
		expAll = expList.get_MsTime_of_StartAndEnd_AllExperiments(options);
	
		ProgressFrame progress = new ProgressFrame("Export data to Excel");
		int nbexpts = expList.getItemCount();
		progress.setLength(nbexpts);

		try 
		{ 
			int column = 1;
			int iSeries = 0;
			workbook = xlsInitWorkbook();
			for (int index = options.firstExp; index <= options.lastExp; index++) 
			{
				Experiment exp = expList.getItemAt(index);
				if (exp.chainToPreviousExperiment != null)
					continue;
				progress.setMessage("Export experiment "+ (index+1) +" of "+ nbexpts);
				String charSeries = CellReference.convertNumToColString(iSeries);
				
				if (options.xyImage)		
					getMoveDataAndExport(exp, column, charSeries, EnumXLSExportType.XYIMAGE);
				if (options.xyCage) 		
					getMoveDataAndExport(exp, column, charSeries, EnumXLSExportType.XYTOPCAGE);
				if (options.xyCapillaries)  	
					getMoveDataAndExport(exp, column, charSeries, EnumXLSExportType.XYTIPCAPS);
				if (options.ellipseAxes)
					getMoveDataAndExport(exp, column, charSeries, EnumXLSExportType.ELLIPSEAXES);
				if (options.distance)  	
					getMoveDataAndExport(exp, column, charSeries, EnumXLSExportType.DISTANCE);
				if (options.alive)	
					getMoveDataAndExport(exp, column, charSeries, EnumXLSExportType.ISALIVE);
				if (options.sleep) 	
					getMoveDataAndExport(exp, column, charSeries, EnumXLSExportType.SLEEP);
				
				if (!options.collateSeries || exp.chainToPreviousExperiment == null)
					column += expList.maxSizeOfCapillaryArrays +2;
				iSeries++;
				progress.incPosition();
			}
			progress.setMessage( "Save Excel file to disk... ");
			FileOutputStream fileOut = new FileOutputStream(filename);
			workbook.write(fileOut);
	        fileOut.close();
	        workbook.close();
	        progress.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		System.out.println("XLSExpoportMove:exportToFile() - output finished");
	}
	
	private int getMoveDataAndExport(Experiment exp, int col0, String charSeries, EnumXLSExportType xlsExport) 
	{	
		getMoveDataFromOneSeriesOfExperiments(exp, xlsExport);
		XSSFSheet sheet = xlsInitSheet(xlsExport.toString(), xlsExport);
		int colmax = xlsExportResultsArrayToSheet(sheet, xlsExport, col0, charSeries);	
		if (options.onlyalive) 
		{
			trimDeadsFromRowMoveData(exp);
			sheet = xlsInitSheet(xlsExport.toString()+"_alive", xlsExport);
			xlsExportResultsArrayToSheet(sheet, xlsExport, col0, charSeries);
		}
		return colmax;
	}
	
	private void getMoveDescriptorsForOneExperiment( Experiment exp, EnumXLSExportType xlsOption) 
	{
		// loop to get all capillaries into expAll and init rows for this experiment
		expAll.cages.copy(exp.cages);
		expAll.capillaries.copy(exp.capillaries);
		expAll.firstImage_FileTime 	= exp.firstImage_FileTime;
		expAll.lastImage_FileTime 	= exp.lastImage_FileTime;
		expAll.setExperimentDirectory( exp.getExperimentDirectory());
		expAll.setExperimentFieldNoTest(EnumXLSColumnHeader.EXP_BOXID, exp.getExperimentField(EnumXLSColumnHeader.EXP_BOXID));
		expAll.setExperimentFieldNoTest(EnumXLSColumnHeader.EXP_EXPT, exp.getExperimentField(EnumXLSColumnHeader.EXP_EXPT));
		expAll.setExperimentFieldNoTest(EnumXLSColumnHeader.EXP_STIM, exp.getExperimentField(EnumXLSColumnHeader.EXP_STIM));
		expAll.setExperimentFieldNoTest(EnumXLSColumnHeader.EXP_CONC, exp.getExperimentField(EnumXLSColumnHeader.EXP_CONC));
		expAll.setExperimentFieldNoTest(EnumXLSColumnHeader.EXP_SEX, exp.getExperimentField(EnumXLSColumnHeader.EXP_SEX));
		expAll.setExperimentFieldNoTest(EnumXLSColumnHeader.EXP_STRAIN, exp.getExperimentField(EnumXLSColumnHeader.EXP_STRAIN));
	
		Experiment expi = exp.chainToNextExperiment;
		while (expi != null ) 
		{
			expAll.cages.mergeLists(expi.cages);
			expAll.lastImage_FileTime = expi.lastImage_FileTime;
			expi = expi.chainToNextExperiment;
		}
		expAll.camImageFirst_ms = expAll.firstImage_FileTime.toMillis();
		expAll.camImageLast_ms = expAll.lastImage_FileTime.toMillis();
		int nFrames = (int) ((expAll.camImageLast_ms - expAll.camImageFirst_ms) / options.buildExcelStepMs +1);
		int ncages = expAll.cages.cagesList.size();
		rowsForOneExp = new ArrayList <XYTaSeriesArrayList> (ncages);
		for (int i=0; i< ncages; i++) 
		{
			Cage cage = expAll.cages.cagesList.get(i);
			XYTaSeriesArrayList row = new XYTaSeriesArrayList (cage.cageRoi2D.getName(), xlsOption, nFrames, options.buildExcelStepMs);
			row.nflies = cage.cageNFlies;
			rowsForOneExp.add(row);
		}
		Collections.sort(rowsForOneExp, new Comparators.XYTaSeries_Name_Comparator());
	}
	
	private void getMoveDataFromOneSeriesOfExperiments(Experiment exp, EnumXLSExportType xlsOption) 
	{	
		getMoveDescriptorsForOneExperiment (exp, xlsOption);
		Experiment expi = exp.getFirstChainedExperiment(true);  
				
		while (expi != null) 
		{
			int len =  1 + (int) (expi.camImageLast_ms - expi.camImageFirst_ms) / options.buildExcelStepMs;
			if (len == 0)
				continue;
			double pixelsize = 32. / expi.capillaries.capillariesList.get(0).pixels;
			
			List <XYTaSeriesArrayList> resultsArrayList = new ArrayList <XYTaSeriesArrayList> (expi.cages.cagesList.size());
			for (Cage cage: expi.cages.cagesList) 
			{
				XYTaSeriesArrayList results = new XYTaSeriesArrayList(cage.cageRoi2D.getName(), xlsOption, len, options.buildExcelStepMs );
				results.nflies = cage.cageNFlies;
				if (results.nflies > 0) 
				{
					results.setPixelSize(pixelsize);
					
					switch (xlsOption) 
					{
						case DISTANCE:
							results.excelComputeDistanceBetweenPoints(cage.flyPositions, (int) expi.camImageBin_ms,  options.buildExcelStepMs);
							break;
						case ISALIVE:
							results.excelComputeIsAlive(cage.flyPositions, (int) expi.camImageBin_ms, options.buildExcelStepMs);
							break;
						case SLEEP:
							results.excelComputeSleep(cage.flyPositions, (int) expi.camImageBin_ms, options.buildExcelStepMs);
							break;
						case XYTOPCAGE:
							results.excelComputeNewPointsOrigin(cage.getCenterTopCage(), cage.flyPositions, (int) expi.camImageBin_ms, options.buildExcelStepMs);
							break;
						case XYTIPCAPS:
							results.excelComputeNewPointsOrigin(cage.getCenterTipCapillaries(exp.capillaries), cage.flyPositions, (int) expi.camImageBin_ms, options.buildExcelStepMs);
							break;
						case ELLIPSEAXES:
							results.excelComputeEllipse(cage.flyPositions, (int) expi.camImageBin_ms, options.buildExcelStepMs);
							break;
						case XYIMAGE:
						default:
							break;
					}
					
					results.convertPixelsToPhysicalValues();
					resultsArrayList.add(results);
				}
				// here add resultsArrayList to expAll
				addMoveResultsTo_rowsForOneExp(expi, resultsArrayList);
			}
			expi = expi.chainToNextExperiment;
		}
		for (XYTaSeriesArrayList row: rowsForOneExp ) 
			row.checkIsAliveFromAliveArray();
	}
	
	private XYTaSeriesArrayList getResultsArrayWithThatName(String testname, List <XYTaSeriesArrayList> resultsArrayList) 
	{
		XYTaSeriesArrayList resultsFound = null;
		for (XYTaSeriesArrayList results: resultsArrayList) 
		{
			if (!results.name.equals(testname))
				continue;
			resultsFound = results;
			break;
		}
		return resultsFound;
	}
	
	private void addMoveResultsTo_rowsForOneExp(Experiment expi, List <XYTaSeriesArrayList> resultsArrayList) 
	{
		long start_Ms = expi.camImageFirst_ms - expAll.camImageFirst_ms;
		long end_Ms = expi.camImageLast_ms - expAll.camImageFirst_ms;
		if (options.fixedIntervals) 
		{
			if (start_Ms < options.startAll_Ms)
				start_Ms = options.startAll_Ms;
			if (start_Ms > expi.camImageLast_ms)
				return;
			
			if (end_Ms > options.endAll_Ms)
				end_Ms = options.endAll_Ms;
			if (end_Ms > expi.camImageFirst_ms)
				return;
		}
		
		final long from_first_Ms = start_Ms + expAll.camImageFirst_ms;
		final long from_lastMs = end_Ms + expAll.camImageFirst_ms;
		final int to_first_index = (int) (from_first_Ms - expAll.camImageFirst_ms) / options.buildExcelStepMs ;
		final int to_nvalues = (int) ((from_lastMs - from_first_Ms)/options.buildExcelStepMs)+1;

		for (XYTaSeriesArrayList row: rowsForOneExp ) 
		{
			XYTaSeriesArrayList results = getResultsArrayWithThatName(row.name,  resultsArrayList);
			if (results != null) 
			{
				if (options.collateSeries && options.padIntervals && expi.chainToPreviousExperiment != null) 
					padWithLastPreviousValue(row, to_first_index);
				
				for (long fromTime = from_first_Ms; fromTime <= from_lastMs; fromTime += options.buildExcelStepMs) 
				{					
					int from_i = (int) ((fromTime - from_first_Ms) / options.buildExcelStepMs);
					if (from_i >= results.xytArrayList.size())
						break;
					XYTaValue aVal = results.xytArrayList.get(from_i);
					int to_i = (int) ((fromTime - expAll.camImageFirst_ms) / options.buildExcelStepMs) ;
					if (to_i >= row.xytArrayList.size())
						break;
					if (to_i < 0)
						continue;
					row.xytArrayList.get(to_i).copy(aVal);
				}
				
			} 
			else 
			{
				if (options.collateSeries && options.padIntervals && expi.chainToPreviousExperiment != null) 
				{
					XYTaValue posok = padWithLastPreviousValue(row, to_first_index);
					int nvalues = to_nvalues;
					if (posok != null) 
					{
						if (nvalues > row.xytArrayList.size())
							nvalues = row.xytArrayList.size();
						int tofirst = to_first_index;
						int tolast = tofirst + nvalues;
						if (tolast > row.xytArrayList.size())
							tolast = row.xytArrayList.size();
						for (int toi = tofirst; toi < tolast; toi++) 
							row.xytArrayList.get(toi).copy(posok);
					}
				}
			}
		}
	}
	
	private XYTaValue padWithLastPreviousValue(XYTaSeriesArrayList row, int transfer_first_index) 
	{
		XYTaValue posok = null;
		int index = getIndexOfFirstNonEmptyValueBackwards(row, transfer_first_index);
		if (index >= 0) 
		{
			posok = row.xytArrayList.get(index);
			for (int i=index+1; i< transfer_first_index; i++) 
			{
				XYTaValue pos = row.xytArrayList.get(i);
				pos.copy(posok);
				pos.bPadded = true;
			}
		}
		return posok;
	}
	
	private int getIndexOfFirstNonEmptyValueBackwards(XYTaSeriesArrayList row, int fromindex) 
	{
		int index = -1;
		for (int i= fromindex; i>= 0; i--) 
		{
			XYTaValue pos = row.xytArrayList.get(i);
			if (!Double.isNaN(pos.rectBounds.getX())) 
			{
				index = i;
				break;
			}
		}
		return index;
	}
	
	private void trimDeadsFromRowMoveData(Experiment exp) 
	{
		for (Cage cage: exp.cages.cagesList) 
		{
			int cagenumber = Integer.valueOf(cage.cageRoi2D.getName().substring(4));
			int ilastalive = 0;
			if (cage.cageNFlies > 0) 
			{
				Experiment expi = exp;
				while (expi.chainToNextExperiment != null && expi.chainToNextExperiment.cages.isFlyAlive(cagenumber)) 
				{
					expi = expi.chainToNextExperiment;
				}
				long lastIntervalFlyAlive_Ms = expi.cages.getLastIntervalFlyAlive(cagenumber) 
						* expi.cages.detectBin_Ms;
				long lastMinuteAlive = lastIntervalFlyAlive_Ms + expi.camImageFirst_ms - expAll.camImageFirst_ms;		
				ilastalive = (int) (lastMinuteAlive / options.buildExcelStepMs);
			}
			for (XYTaSeriesArrayList row : rowsForOneExp) 
			{
				int rowCageNumber = Integer.valueOf(row.name.substring(4));
				if ( rowCageNumber == cagenumber) {
					row.clearValues(ilastalive+1);
				}
			}
		}	
	}
	
	private int xlsExportResultsArrayToSheet(XSSFSheet sheet, EnumXLSExportType xlsExportOption, int col0, String charSeries) 
	{
		Point pt = new Point(col0, 0);
		writeExperiment_descriptors(expAll, charSeries, sheet, pt, xlsExportOption);
		pt = writeData2(sheet, xlsExportOption, pt);
		return pt.x;
	}
			
	private Point writeData2 (XSSFSheet sheet, EnumXLSExportType option, Point pt_main) 
	{
		int rowseries = pt_main.x +2;
		int columndataarea = pt_main.y;
		Point pt = new Point(pt_main);
		writeRows(sheet, columndataarea, rowseries, pt);		
		pt_main.x = pt.x+1;
		return pt_main;
	}
	
	private void writeRows(XSSFSheet sheet, int column_dataArea, int rowSeries, Point pt) 
	{
		boolean transpose = options.transpose;
		for (XYTaSeriesArrayList row: rowsForOneExp) 
		{
			pt.y = column_dataArea;
			int col = getRowIndexFromCageName(row.name)*2;
			pt.x = rowSeries + col; 
			if (row.nflies < 1)
				continue;
		
			long last = expAll.camImageLast_ms - expAll.camImageFirst_ms;
			if (options.fixedIntervals)
				last = options.endAll_Ms-options.startAll_Ms;
			
			for (long coltime = 0; coltime <= last; coltime += options.buildExcelStepMs, pt.y++) 
			{
				int i_from = (int) (coltime  / options.buildExcelStepMs);
				if (i_from >= row.xytArrayList.size())
					break;
				
				double valueL = Double.NaN;
				double valueR = Double.NaN;
				XYTaValue pos = row.xytArrayList.get(i_from);
				
				switch (row.exportType) 
				{
					case DISTANCE:
						valueL = pos.distance;
						valueR = valueL;
						break;
					case ISALIVE:
						valueL = pos.bAlive ? 1: 0;
						valueR = valueL;
						break;
					case SLEEP:
						valueL = pos.bSleep? 1: 0;
						valueR = valueL;
						break;
					case XYTOPCAGE:
					case XYTIPCAPS:
					case XYIMAGE:
						valueL = pos.rectBounds.getX() + pos.rectBounds.getWidth()/2.;
						valueR = pos.rectBounds.getY() + pos.rectBounds.getHeight()/2.;
						break;
					case ELLIPSEAXES:
						valueL = pos.axis1;
						valueR = pos.axis2;
						break;
					default:
						break;
				}
				
				if (!Double.isNaN(valueL)) 
				{
					XLSUtils.setValue(sheet, pt, transpose, valueL);
					if (pos.bPadded)
						XLSUtils.getCell(sheet, pt, transpose).setCellStyle(xssfCellStyle_red);
				}
				if (!Double.isNaN(valueR)) 
				{
					pt.x++;
					XLSUtils.setValue(sheet, pt, transpose, valueR);
					if (pos.bPadded)
						XLSUtils.getCell(sheet, pt, transpose).setCellStyle(xssfCellStyle_red);
					pt.x--;
				}
			}
			pt.x += 2;
		}
	}
	

	
}
