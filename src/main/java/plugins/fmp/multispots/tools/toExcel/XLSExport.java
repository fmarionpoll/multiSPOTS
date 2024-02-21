package plugins.fmp.multispots.tools.toExcel;

import java.awt.Point;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import plugins.fmp.multispots.dlg.JComponents.ExperimentCombo;
import plugins.fmp.multispots.experiment.Cage;
import plugins.fmp.multispots.experiment.Capillary;
import plugins.fmp.multispots.experiment.Experiment;
import plugins.fmp.multispots.experiment.Spot;



public class XLSExport 
{
	protected XLSExportOptions 	options 			= null;
	protected Experiment 		expAll 				= null;

	XSSFCellStyle 				xssfCellStyle_red 	= null;
	XSSFCellStyle 				xssfCellStyle_blue 	= null;
    XSSFFont 					font_red 			= null;
    XSSFFont 					font_blue 			= null;
    XSSFWorkbook 				workbook			= null;		
    
	ExperimentCombo 			expList 			= null;
//	XLSResultsArray 			rowListForOneExp 	= new XLSResultsArray ();


	// ------------------------------------------------
    	
	protected Point writeExperiment_descriptors(Experiment exp, String charSeries, XSSFSheet sheet, Point pt, EnumXLSExportType xlsExportOption) 
	{
		boolean transpose = options.transpose;
		int row = pt.y;
		int col0 = pt.x;
		XLSUtils.setValue(sheet, pt, transpose, "..");
		pt.x++;
		XLSUtils.setValue(sheet, pt, transpose, "..");
		pt.x++;
		int colseries = pt.x;
		int len = EnumXLSColumnHeader.values().length;
		for (int i = 0; i < len; i++) 
		{
			XLSUtils.setValue(sheet, pt, transpose, "--");
			pt.x++;
		}
		pt.x = colseries;
		
		String filename = exp.getExperimentDirectory();
		if (filename == null)
			filename = exp.seqCamData.getImagesDirectory();
		Path path = Paths.get(filename);

		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		String date = df.format(exp.chainImageFirst_ms);

		String name0 = path.toString();
		int pos = name0.indexOf("cam");
		String cam = "-"; 
		if (pos > 0) 
		{
			int pos5 = pos+5;
			if (pos5 >= name0.length())
				pos5 = name0.length() -1;
			cam = name0.substring(pos, pos5);
		}
		
		String sheetName = sheet.getSheetName();
		
		int rowmax = -1;
		for (EnumXLSColumnHeader dumb: EnumXLSColumnHeader.values()) 
		{
			if (rowmax < dumb.getValue())
				rowmax = dumb.getValue();		
		}
		
		List<Capillary> capList = exp.capillaries.capillariesList;
		for (int t = 0; t < capList.size(); t++) 
		{ 
			Capillary cap = capList.get(t);
			String	name = cap.getRoiName();
			int col = getRowIndexFromKymoFileName(name);
			if (col >= 0) 
				pt.x = colseries + col;
			int x = pt.x;
			int y = row;
			XLSUtils.setValue(sheet, x, y+EnumXLSColumnHeader.PATH.getValue(), transpose, name0);
			XLSUtils.setValue(sheet, x, y+EnumXLSColumnHeader.DATE.getValue(), transpose, date);
			XLSUtils.setValue(sheet, x, y+EnumXLSColumnHeader.CAM.getValue(), transpose, cam);
			
			XLSUtils.setValue(sheet, x, y+EnumXLSColumnHeader.EXP_BOXID.getValue(), transpose, exp.getExperimentField(EnumXLSColumnHeader.EXP_BOXID));
			XLSUtils.setValue(sheet, x, y+EnumXLSColumnHeader.EXP_EXPT.getValue(), transpose, exp.getExperimentField(EnumXLSColumnHeader.EXP_EXPT));
			XLSUtils.setValue(sheet, x, y+EnumXLSColumnHeader.EXP_STIM.getValue(), transpose, exp.getExperimentField(EnumXLSColumnHeader.EXP_STIM));
			XLSUtils.setValue(sheet, x, y+EnumXLSColumnHeader.EXP_CONC.getValue(), transpose, exp.getExperimentField(EnumXLSColumnHeader.EXP_CONC));
			XLSUtils.setValue(sheet, x, y+EnumXLSColumnHeader.EXP_STRAIN.getValue(), transpose, exp.getExperimentField(EnumXLSColumnHeader.EXP_STRAIN));
			XLSUtils.setValue(sheet, x, y+EnumXLSColumnHeader.EXP_SEX.getValue(), transpose, exp.getExperimentField(EnumXLSColumnHeader.EXP_SEX));			

			XLSUtils.setValue(sheet, x, y+EnumXLSColumnHeader.CAP_VOLUME.getValue(), transpose, exp.capillaries.capillariesDescription.volume);
			XLSUtils.setValue(sheet, x, y+EnumXLSColumnHeader.CAP_PIXELS.getValue(), transpose, exp.capillaries.capillariesDescription.pixels);
			
			XLSUtils.setValue(sheet, x, y+EnumXLSColumnHeader.CAP.getValue(), transpose, cap.getSideDescriptor(xlsExportOption));
			outputStimAndConc_according_to_DataOption(sheet, xlsExportOption, cap, transpose, x, y);

			
			XLSUtils.setValue(sheet, x, y+EnumXLSColumnHeader.CAP_CAGEINDEX.getValue(), transpose, cap.cageID);
			XLSUtils.setValue(sheet, x, y+EnumXLSColumnHeader.CAGEID.getValue(), transpose, charSeries+cap.cageID);
			XLSUtils.setValue(sheet, x, y+EnumXLSColumnHeader.CAP_NFLIES.getValue(), transpose, cap.nFlies); 

			XLSUtils.setValue(sheet, x, y+EnumXLSColumnHeader.DUM4.getValue(), transpose, sheetName);
			XLSUtils.setValue(sheet, x, y+EnumXLSColumnHeader.CHOICE_NOCHOICE.getValue(), transpose, desc_getChoiceTestType(capList, t));
			if (exp.cages.cagesList.size() > t/2) 
			{
					Cage cage = exp.cages.cagesList.get(t/2); //cap.capCageID);
					XLSUtils.setValue(sheet, x, y+EnumXLSColumnHeader.CAGE_STRAIN.getValue(), transpose, cage.strCageStrain );
					XLSUtils.setValue(sheet, x, y+EnumXLSColumnHeader.CAGE_SEX.getValue(), transpose, cage.strCageSex  );
					XLSUtils.setValue(sheet, x, y+EnumXLSColumnHeader.CAGE_AGE.getValue(), transpose, cage.cageAge);
					XLSUtils.setValue(sheet, x, y+EnumXLSColumnHeader.CAGE_COMMENT.getValue(), transpose, cage.strCageComment);
			} 
		}
		pt.x = col0;
		pt.y = rowmax +1;
		return pt;
	}
	
	private String desc_getChoiceTestType(List<Capillary> capList, int t)
	{
		Capillary cap = capList.get(t);
		String choiceText = "..";
		String side = cap.getCapillarySide();
		if (side.contains("L"))
			t = t+1;
		else
			t = t-1;
		if (t >= 0 && t < capList.size()) {
			Capillary othercap = capList.get(t);
			String otherSide = othercap.getCapillarySide();
			if (!otherSide .contains(side))
			{
				if (cap.stimulus.equals(othercap.stimulus)
					&& cap.concentration.equals(othercap.concentration))
					choiceText  = "no-choice";
				else
					choiceText = "choice";
			}
		}
		return choiceText;
	}
	
	private void outputStimAndConc_according_to_DataOption(XSSFSheet sheet, EnumXLSExportType xlsExportOption, Capillary cap, boolean transpose, int x, int y)
	{
		switch (xlsExportOption) {
		case TOPLEVEL_LR:
		case TOPLEVELDELTA_LR:
		case SUMGULPS_LR:
			if (cap.getCapillarySide().equals("L")) 
				XLSUtils.setValue(sheet, x, y+EnumXLSColumnHeader.CAP_STIM.getValue(), transpose, "L+R");
			else 
				XLSUtils.setValue(sheet, x, y+EnumXLSColumnHeader.CAP_STIM.getValue(), transpose, "(L-R)/(L+R)");
			XLSUtils.setValue(sheet, x, y+EnumXLSColumnHeader.CAP_CONC.getValue(), transpose, cap.stimulus + ": "+ cap.concentration);
			break;
			
		case TTOGULP_LR:
			if (cap.getCapillarySide().equals("L")) 
			{
				XLSUtils.setValue(sheet, x, y+EnumXLSColumnHeader.CAP_STIM.getValue(), transpose, "min_t_to_gulp");
			} 
			else 
			{
				XLSUtils.setValue(sheet, x, y+EnumXLSColumnHeader.CAP_STIM.getValue(), transpose, "max_t_to_gulp");
			}
			XLSUtils.setValue(sheet, x, y+EnumXLSColumnHeader.CAP_CONC.getValue(), transpose, cap.stimulus + ": "+ cap.concentration);
			break;
			
		default:
			XLSUtils.setValue(sheet, x, y+EnumXLSColumnHeader.CAP_STIM.getValue(), transpose, 	cap.stimulus);
			XLSUtils.setValue(sheet, x, y+EnumXLSColumnHeader.CAP_CONC.getValue(), transpose, 	cap.concentration);	
			break;
		}
	}
	
	int writeTopRow_descriptors(XSSFSheet sheet) 
	{		
		Point pt = new Point(0,0);
		int x = 0;
		boolean transpose = options.transpose;
		int nextcol = -1;
		for (EnumXLSColumnHeader dumb: EnumXLSColumnHeader.values()) 
		{
			XLSUtils.setValue(sheet, x, dumb.getValue(), transpose, dumb.getName());
			if (nextcol < dumb.getValue())
				nextcol = dumb.getValue();
		}
		pt.y = nextcol+1;
		return pt.y;
	}
	
	void writeTopRow_timeIntervals(XSSFSheet sheet, int row, EnumXLSExportType xlsExport) 
	{
		switch (xlsExport)
		{
			case AUTOCORREL:
			case CROSSCORREL:
			case AUTOCORREL_LR:
			case CROSSCORREL_LR:
				writeTopRow_timeIntervals_Correl(sheet, row);
				break;
				
			default:
				writeTopRow_timeIntervals_Default(sheet, row);
				break;
		}
	}
	
	void writeTopRow_timeIntervals_Correl(XSSFSheet sheet, int row)
	{
		boolean transpose = options.transpose;
		Point pt = new Point(0, row);
		long interval = - options.nbinscorrelation;
		while (interval < options.nbinscorrelation) 
		{
			int i = (int) interval;
			XLSUtils.setValue(sheet, pt, transpose, "t"+i);
			pt.y++;
			interval += 1;
		}
	}
	
	void writeTopRow_timeIntervals_Default(XSSFSheet sheet, int row)
	{
		boolean transpose = options.transpose;
		Point pt = new Point(0, row);
		long duration = expAll.camImageLast_ms - expAll.camImageFirst_ms;
		long interval = 0;
		while (interval < duration) 
		{
			int i = (int) (interval / options.buildExcelUnitMs);
			XLSUtils.setValue(sheet, pt, transpose, "t"+i);
			pt.y++;
			interval += options.buildExcelStepMs;
		}
	}
	
	protected int desc_getCageFromCapillaryName(String name) 
	{
		if (!name .contains("line"))
			return -1;
		String num = name.substring(4, 5);
		int numFromName = Integer.valueOf(num);
		return numFromName;
	}
	
	protected int getRowIndexFromKymoFileName(String name) 
	{
		if (!name .contains("line"))
			return -1;
		String num = name.substring(4, 5);
		int numFromName = Integer.valueOf(num);
		if( name.length() > 5) 
		{
			String side = name.substring(5, 6);
			if (side != null) 
			{
				if (side .equals("R")) 
				{
					numFromName = numFromName* 2;
					numFromName += 1;
				}
				else if (side .equals("L"))
					numFromName = numFromName* 2;
			}
		}
		return numFromName;
	}
		
	protected int getRowIndexFromCageName(String name) 
	{
		if (!name .contains("cage"))
			return -1;
		String num = name.substring(4, name.length());
		int numFromName = Integer.valueOf(num);
		return numFromName;
	}
	
	protected Point getCellXCoordinateFromDataName(XLSResults xlsResults, Point pt_main, int colseries) 
	{
		int col = getRowIndexFromKymoFileName(xlsResults.name);
		if (col >= 0)
			pt_main.x = colseries + col;
		return pt_main;
	}
	
	protected int getCageFromKymoFileName(String name) 
	{
		if (!name .contains("line"))
			return -1;
		return Integer.valueOf(name.substring(4, 5));
	}
	
	XSSFWorkbook xlsInitWorkbook() 
	{
		XSSFWorkbook workbook = new XSSFWorkbook(); 
		workbook.setMissingCellPolicy(Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
		xssfCellStyle_red = workbook.createCellStyle();
	    font_red = workbook.createFont();
	    font_red.setColor(HSSFColor.HSSFColorPredefined.RED.getIndex());
	    xssfCellStyle_red.setFont(font_red);
	    
		xssfCellStyle_blue = workbook.createCellStyle();
	    font_blue = workbook.createFont();
	    font_blue.setColor(HSSFColor.HSSFColorPredefined.BLUE.getIndex());
	    xssfCellStyle_blue.setFont(font_blue);
	    return workbook;
	}
	
	XSSFSheet xlsInitSheet(String title, EnumXLSExportType xlsExport) 
	{
		XSSFSheet sheet = workbook.getSheet(title);
		if (sheet == null) 
		{
			sheet = workbook.createSheet(title);
			int row = writeTopRow_descriptors(sheet);
			writeTopRow_timeIntervals(sheet, row, xlsExport);
		}
		return sheet;
	}
	
	protected int getDataAndExport(Experiment exp, int col0, String charSeries, EnumXLSExportType xlsExport) 
	{	
		XLSResultsArray rowListForOneExp = getCapDataFromOneExperimentSeries(exp, xlsExport);
		XSSFSheet sheet = xlsInitSheet(xlsExport.toString(), xlsExport);
		int colmax = xlsExportResultsArrayToSheet(rowListForOneExp, sheet, xlsExport, col0, charSeries);
		
		if (options.onlyalive) 
		{
			trimDeadsFromArrayList(rowListForOneExp, exp);
			sheet = xlsInitSheet(xlsExport.toString()+"_alive", xlsExport);
			xlsExportResultsArrayToSheet(rowListForOneExp, sheet, xlsExport, col0, charSeries);
		}
		
		if (options.sumPerCage) 
		{
			combineDataForOneCage(rowListForOneExp, exp);
			sheet = xlsInitSheet(xlsExport.toString()+"_cage", xlsExport);
			xlsExportResultsArrayToSheet(rowListForOneExp, sheet, xlsExport, col0, charSeries);
		}
		
		return colmax;
	}
		
	private XLSResultsArray getCapDescriptorsForOneExperiment( Experiment exp, EnumXLSExportType xlsOption) 
	{
		if (expAll == null) 
			return null;
		
		// loop to get all capillaries into expAll and init rows for this experiment
		expAll.cages.copy(exp.cages);
		expAll.capillaries.copy(exp.capillaries);
		expAll.chainImageFirst_ms = exp.chainImageFirst_ms;
		expAll.copyExperimentFields(exp);
		expAll.setExperimentDirectory(exp.getExperimentDirectory());
		
		Experiment expi = exp.chainToNextExperiment;
		while (expi != null ) 
		{
			expAll.capillaries.mergeLists(expi.capillaries);
			expi = expi.chainToNextExperiment;
		}

		int nFrames = (int) ((expAll.camImageLast_ms - expAll.camImageFirst_ms)/options.buildExcelStepMs  +1) ;
		int ncapillaries = expAll.capillaries.capillariesList.size();
		XLSResultsArray rowListForOneExp = new XLSResultsArray(ncapillaries);
		for (int i = 0; i < ncapillaries; i++) 
		{
			Capillary cap 		= expAll.capillaries.capillariesList.get(i);
			XLSResults row 		= new XLSResults (cap.getRoiName(), cap.nFlies, cap.cageID, xlsOption, nFrames);
			row.stimulus 		= cap.stimulus;
			row.concentration 	= cap.concentration;
			row.cageID 			= cap.cageID;
			rowListForOneExp.addRow(row);
		}
		rowListForOneExp.sortRowsByName();
		return rowListForOneExp;
	}
	
	private XLSResultsArray getSpotsDescriptorsForOneExperiment( Experiment exp, EnumXLSExportType xlsOption) 
	{
		if (expAll == null) 
			return null;
		
		// loop to get all capillaries into expAll and init rows for this experiment
		expAll.cages.copy(exp.cages);
		expAll.spotsArray.copy(exp.spotsArray);
		expAll.chainImageFirst_ms = exp.chainImageFirst_ms;
		expAll.copyExperimentFields(exp);
		expAll.setExperimentDirectory(exp.getExperimentDirectory());
		
		Experiment expi = exp.chainToNextExperiment;
		while (expi != null ) 
		{
			expAll.spotsArray.mergeLists(expi.spotsArray);
			expi = expi.chainToNextExperiment;
		}

		int nFrames = (int) ((expAll.camImageLast_ms - expAll.camImageFirst_ms)/options.buildExcelStepMs  +1) ;
		int nspots = expAll.spotsArray.spotsList.size();
		XLSResultsArray rowListForOneExp = new XLSResultsArray(nspots);
		for (int i = 0; i < nspots; i++) 
		{
			Spot spot 			= expAll.spotsArray.spotsList.get(i);
			XLSResults row 		= new XLSResults (spot.getRoiName(), spot.nFlies, spot.cageID, xlsOption, nFrames);
			row.stimulus 		= spot.stimulus;
			row.concentration 	= spot.concentration;
			row.cageID 			= spot.cageID;
			rowListForOneExp.addRow(row);
		}
		rowListForOneExp.sortRowsByName();
		return rowListForOneExp;
	}

		
	public XLSResultsArray getCapDataFromOneExperiment(Experiment exp, EnumXLSExportType exportType, XLSExportOptions options) 
	{
		this.options = options;
		expAll = new Experiment();
		expAll.camImageLast_ms = exp.camImageLast_ms;
		expAll.camImageFirst_ms = exp.camImageFirst_ms;
		return getCapDataFromOneExperimentSeries(exp, exportType);
	}
	
	public XLSResultsArray getSpotsDataFromOneExperiment(Experiment exp, EnumXLSExportType exportType, XLSExportOptions options) 
	{
		this.options = options;
		expAll = new Experiment();
		expAll.camImageLast_ms = exp.camImageLast_ms;
		expAll.camImageFirst_ms = exp.camImageFirst_ms;
		return getSpotsDataFromOneExperimentSeries(exp, exportType);
	}
	
	private void exportError (Experiment expi, int nOutputFrames) 
	{
		String error = "XLSExport:ExportError() ERROR in "+ expi.getExperimentDirectory() 
		+ "\n nOutputFrames="+ nOutputFrames 
		+ " kymoFirstCol_Ms=" + expi.binFirst_ms 
		+ " kymoLastCol_Ms=" + expi.binLast_ms;
		System.out.println(error);
	}
	
	private int getNOutputFrames (Experiment expi)
	{
		int nOutputFrames = (int) ((expi.binLast_ms - expi.binFirst_ms) / options.buildExcelStepMs +1);
		if (nOutputFrames <= 1) 
		{
			if (expi.seqKymos.imageWidthMax == 0)
				expi.loadKymographs();
			expi.binLast_ms = expi.binFirst_ms + expi.seqKymos.imageWidthMax * expi.binDuration_ms;
			if (expi.binLast_ms <= 0)
				exportError(expi, -1);
			nOutputFrames = (int) ((expi.binLast_ms - expi.binFirst_ms) / options.buildExcelStepMs +1);
			
			if (nOutputFrames <= 1) 
			{
				nOutputFrames = expi.seqCamData.nTotalFrames;
				exportError(expi, nOutputFrames);
			}
		}
		return nOutputFrames;
	}
	
	private XLSResultsArray getCapDataFromOneExperimentSeries(Experiment exp, EnumXLSExportType xlsExportType) 
	{	
		XLSResultsArray rowListForOneExp =  getCapDescriptorsForOneExperiment (exp, xlsExportType);
		Experiment expi = exp.getFirstChainedExperiment(true); 
		
		while (expi != null) 
		{
			int nOutputFrames = getNOutputFrames(expi);
			if (nOutputFrames > 1)
			{
				XLSResultsArray resultsArrayList = new XLSResultsArray (expi.capillaries.capillariesList.size());
				options.compensateEvaporation = false;
				switch (xlsExportType) 
				{
					case BOTTOMLEVEL:
					case NBGULPS:
					case AMPLITUDEGULPS:
					case TTOGULP:
					case TTOGULP_LR:
						
					case DERIVEDVALUES:
					case SUMGULPS:
					case SUMGULPS_LR:
						
					case AUTOCORREL:
					case AUTOCORREL_LR:
					case CROSSCORREL:
					case CROSSCORREL_LR:
						resultsArrayList.getResults1(expi.capillaries, xlsExportType, nOutputFrames, exp.binDuration_ms, options);
						break;
						
					case TOPLEVEL:
					case TOPLEVEL_LR:
					case TOPLEVELDELTA:
					case TOPLEVELDELTA_LR:
						options.compensateEvaporation = options.subtractEvaporation;
						
					case TOPRAW:
						resultsArrayList.getResults_T0(expi.capillaries, xlsExportType, nOutputFrames, exp.binDuration_ms, options);
						break;
	
					default:
						break;
				}
				addResultsTo_rowsForOneExp(rowListForOneExp, expi, resultsArrayList);
			}
			expi = expi.chainToNextExperiment;
		}
		
		switch (xlsExportType) 
		{
			case TOPLEVELDELTA:
			case TOPLEVELDELTA_LR:
				rowListForOneExp.subtractDeltaT(1, 1); //options.buildExcelStepMs);
				break;
			default:
				break;
		}
		return rowListForOneExp;
	}
	
	private XLSResultsArray getSpotsDataFromOneExperimentSeries(Experiment exp, EnumXLSExportType xlsExportType) 
	{	
		XLSResultsArray rowListForOneExp =  getSpotsDescriptorsForOneExperiment (exp, xlsExportType);
		Experiment expi = exp.getFirstChainedExperiment(true); 
		while (expi != null) 
		{
			int nOutputFrames = getNOutputFrames(expi);
			if (nOutputFrames > 1)
			{
				XLSResultsArray resultsArrayList = new XLSResultsArray (expi.spotsArray.spotsList.size());
				options.compensateEvaporation = false;
				switch (xlsExportType) 
				{
					case AREA_NPIXELS:
						resultsArrayList.getResults1(expi.spotsArray, 
								xlsExportType, 
								nOutputFrames, 
							x	exp.binDuration_ms, // TODO check this
								options);
//						resultsArrayList.getResults_T0(expi.capillaries, xlsExportType, nOutputFrames, exp.binDuration_ms, options);
						break;
	
					default:
						break;
				}
				addResultsTo_rowsForOneExp(rowListForOneExp, expi, resultsArrayList);
			}
			expi = expi.chainToNextExperiment;
		}
		
		switch (xlsExportType) 
		{
			case TOPLEVELDELTA:
			case TOPLEVELDELTA_LR:
				rowListForOneExp.subtractDeltaT(1, 1); //options.buildExcelStepMs);
				break;
			default:
				break;
		}
		return rowListForOneExp;
	}
	
	private XLSResults getResultsArrayWithThatName(
			String testname, 
			XLSResultsArray resultsArrayList) 
	{
		XLSResults resultsFound = null;
		for (XLSResults results: resultsArrayList.resultsList) 
		{
			if (results.name.equals(testname)) 
			{
				resultsFound = results;
				break;
			}
		}
		return resultsFound;
	}
	
	private void addResultsTo_rowsForOneExp(XLSResultsArray rowListForOneExp, Experiment expi, XLSResultsArray resultsArrayList) 
	{
		if (resultsArrayList.resultsList.size() <1)
			return;
		
		EnumXLSExportType xlsoption = resultsArrayList.getRow(0).exportType;
		
		long offsetChain = expi.camImageFirst_ms - expi.chainImageFirst_ms;
		long start_Ms = expi.binFirst_ms + offsetChain; // TODO check when collate?
		long end_Ms = expi.binLast_ms + offsetChain;
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
		
		// TODO check this 
		final long from_first_Ms = start_Ms - offsetChain;
		final long from_lastMs = end_Ms - offsetChain;
		final int to_first_index = (int) (start_Ms / options.buildExcelStepMs) ;
		final int to_nvalues = (int) ((end_Ms - start_Ms)/options.buildExcelStepMs)+1;
		
		for (int iRow = 0; iRow < rowListForOneExp.size(); iRow++ ) 
		{
			XLSResults row = rowListForOneExp.getRow(iRow);
			XLSResults results = getResultsArrayWithThatName(row.name, resultsArrayList);
			if (results != null && results.valuesOut != null) 
			{
				double dvalue = 0.;
				switch (xlsoption) 
				{
					case TOPLEVEL:
					case TOPLEVEL_LR:
					case SUMGULPS:
					case SUMGULPS_LR:
					case TOPLEVELDELTA:
					case TOPLEVELDELTA_LR:
						if (options.collateSeries && options.padIntervals && expi.chainToPreviousExperiment != null) 
							dvalue = padWithLastPreviousValue(row, to_first_index);
						break;
					default:
						break;
				}

				int icolTo = 0;
				if (options.collateSeries || options.absoluteTime)
					icolTo = to_first_index;
				for (long fromTime = from_first_Ms; fromTime <= from_lastMs; fromTime += options.buildExcelStepMs, icolTo++) 
				{
					int from_i = (int) Math.round(((double)(fromTime - from_first_Ms)) / ((double) options.buildExcelStepMs));
					if (from_i >= results.valuesOut.length)
						break;
					// TODO check how this can happen
					if (from_i < 0)
						continue;
					double value = results.valuesOut[from_i] + dvalue;
					if (icolTo >= row.valuesOut.length)
						break;
					row.valuesOut[icolTo] = value;
				}

			} 
			else 
			{
				if (options.collateSeries && options.padIntervals && expi.chainToPreviousExperiment != null) 
				{
					double dvalue = padWithLastPreviousValue(row, to_first_index);
					int tofirst = (int) to_first_index;
					int tolast = (int) (tofirst + to_nvalues);
					if (tolast > row.valuesOut.length)
						tolast = row.valuesOut.length;
					for (int toi = tofirst; toi < tolast; toi++) 
						row.valuesOut[toi] = dvalue;
				}
			}
		}
	}
	
	private double padWithLastPreviousValue(XLSResults row, long to_first_index) 
	{
		double dvalue = 0;
		if (to_first_index >= row.valuesOut.length)
			return dvalue;
		
		int index = getIndexOfFirstNonEmptyValueBackwards(row, to_first_index);
		if (index >= 0) 
		{
			dvalue = row.valuesOut[index];
			for (int i = index+1; i < to_first_index; i++) 
			{
				row.valuesOut[i] = dvalue;
				row.padded_out[i] = true;
			}
		}
		return dvalue;
	}
	
	private int getIndexOfFirstNonEmptyValueBackwards(XLSResults row, long fromindex) 
	{
		int index = -1;
		int ifrom = (int) fromindex;
		for (int i= ifrom; i>= 0; i--) 
		{
			if (!Double.isNaN(row.valuesOut[i])) 
			{
				index = i;
				break;
			}
		}
		return index;
	}
	
	private void trimDeadsFromArrayList(XLSResultsArray rowListForOneExp, Experiment exp) 
	{
		for (Cage cage: exp.cages.cagesList) 
		{
			String roiname = cage.cageRoi2D.getName();
			if (roiname.length() < 4 || !roiname.substring( 0 , 4 ).contains("cage"))
				continue;
			
			String cagenumberString = roiname.substring(4);		
			int cagenumber = Integer.valueOf(cagenumberString);
			int ilastalive = 0;
			if (cage.cageNFlies > 0) 
			{
				Experiment expi = exp;
				while (expi.chainToNextExperiment != null && expi.chainToNextExperiment.cages.isFlyAlive(cagenumber)) 
				{
					expi = expi.chainToNextExperiment;
				}
				int lastIntervalFlyAlive = expi.cages.getLastIntervalFlyAlive(cagenumber);
				int lastMinuteAlive = (int) (lastIntervalFlyAlive * expi.camImageBin_ms 
						+ (expi.camImageFirst_ms - expAll.camImageFirst_ms));		
				ilastalive = (int) (lastMinuteAlive / expAll.binDuration_ms);
			}
			if (ilastalive > 0)
				ilastalive += 1;
			
			for (int iRow = 0; iRow < rowListForOneExp.size(); iRow++ ) 
			{
				XLSResults row = rowListForOneExp.getRow(iRow);
				if (desc_getCageFromCapillaryName (row.name) == cagenumber)
					row.clearValues(ilastalive);
			}
		}	
	}
	
	private void combineDataForOneCage(XLSResultsArray rowListForOneExp, Experiment exp) 
	{
		for (int iRow0 = 0; iRow0 < rowListForOneExp.size(); iRow0++ ) 
		{
			XLSResults row_master = rowListForOneExp.getRow(iRow0);
			if (row_master.nflies == 0 || row_master.valuesOut == null)
				continue;
			
			for (int iRow = 0; iRow < rowListForOneExp.size(); iRow++ ) 
			{
				XLSResults row = rowListForOneExp.getRow(iRow);
				if (row.nflies == 0 || row.valuesOut == null)
					continue;
				if (row.cageID != row_master.cageID)
					continue;
				if (row.name .equals(row_master.name))
					continue;
				if (row.stimulus .equals(row_master.stimulus) && row.concentration .equals(row_master.concentration)) 
				{
					row_master.sumValues_out(row);
					row.clearAll();
				}
			}
		}
	}
	
	private int xlsExportResultsArrayToSheet(XLSResultsArray rowListForOneExp,
			XSSFSheet sheet, 
			EnumXLSExportType xlsExportOption, 
			int col0, 
			String charSeries) 
	{
		Point pt = new Point(col0, 0);
		writeExperiment_descriptors(expAll, charSeries, sheet, pt, xlsExportOption);
		pt = writeExperiment_data(rowListForOneExp, sheet, xlsExportOption, pt);
		return pt.x;
	}
			
	private Point writeExperiment_data (XLSResultsArray rowListForOneExp, XSSFSheet sheet, EnumXLSExportType option, Point pt_main) 
	{
		int rowSeries = pt_main.x +2;
		int column_dataArea = pt_main.y;
		Point pt = new Point(pt_main);
		writeExperiment_data_simpleRows(rowListForOneExp, sheet, column_dataArea, rowSeries, pt);			
		pt_main.x = pt.x+1;
		return pt_main;
	}
	
	private void writeExperiment_data_simpleRows(XLSResultsArray rowListForOneExp,
			XSSFSheet sheet, 
			int column_dataArea, 
			int rowSeries, 
			Point pt) 
	{
		for (int iRow = 0; iRow < rowListForOneExp.size(); iRow++ ) 
		{
			XLSResults row = rowListForOneExp.getRow(iRow);
			writeRow(sheet, column_dataArea, rowSeries, pt, row);
		}
		
	}
	
	private void writeRow(XSSFSheet sheet, int column_dataArea, int rowSeries, Point pt, XLSResults row) 
	{
		boolean transpose = options.transpose;
		pt.y = column_dataArea;
		int col = getRowIndexFromKymoFileName(row.name);
		pt.x = rowSeries + col; 
		if (row.valuesOut == null)
			return;
		
		for (long coltime = expAll.camImageFirst_ms; coltime < expAll.camImageLast_ms; coltime += options.buildExcelStepMs, pt.y++) 
		{
			int i_from = (int) ((coltime - expAll.camImageFirst_ms) / options.buildExcelStepMs);
			if (i_from >= row.valuesOut.length) 
				break;
			double value = row.valuesOut[i_from];
			if (!Double.isNaN(value)) 
			{
				XLSUtils.setValue(sheet, pt, transpose, value);
				if (i_from < row.padded_out.length && row.padded_out[i_from])
					XLSUtils.getCell(sheet, pt, transpose).setCellStyle(xssfCellStyle_red);
			}
		}
		pt.x++;
	}

	
}
