package plugins.fmp.multiSPOTS.tools.toExcel;

import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.ss.util.CellReference;

import icy.gui.frame.progress.ProgressFrame;
import plugins.fmp.multiSPOTS.experiment.Experiment;

public class XLSExportSpotAreasResults extends XLSExport 
{	
	public void exportToFile(String filename, XLSExportOptions opt) 
	{	
		System.out.println("XLSExpoportSpotAreas:exportToFile() - start output");
		options = opt;
		expList = options.expList;

		expList.loadListOfMeasuresFromAllExperiments(false, true, options.onlyalive);
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
			for (int index = options.expIndexFirst; index <= options.expIndexLast; index++) 
			{
				Experiment exp = expList.getItemAt(index);
				if (exp.chainToPreviousExperiment != null)
					continue;
				progress.setMessage("Export experiment "+ (index+1) +" of "+ nbexpts);
				String charSeries = CellReference.convertNumToColString(iSeries);
				
				int collast = column;
				if (options.spotAreas) 
				{	
					collast = getDataAndExport(exp, column, charSeries, EnumXLSExportType.AREA_SUM);
//					getDataAndExport(exp, column, charSeries, EnumXLSExportType.AREA_SUM2);
					getDataAndExport(exp, column, charSeries, EnumXLSExportType.AREA_CNTPIX);
					getDataAndExport(exp, column, charSeries, EnumXLSExportType.AREA_AVGGREY);
					if (options.lrPI ) 
					{
						getDataAndExport(exp, column, charSeries, EnumXLSExportType.AREA_SUM_LR);
						getDataAndExport(exp, column, charSeries, EnumXLSExportType.AREA_CNTPIX_LR);
						getDataAndExport(exp, column, charSeries, EnumXLSExportType.AREA_AVGGREY_LR);
					}
				}
				column = collast +2;
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
		System.out.println("XLSExpoportSpotAreas:exportToFile() XLS output finished");
	}
}
