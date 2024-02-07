package plugins.fmp.multispots.tools.toExcel;

import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.poi.ss.util.CellReference;
import icy.gui.frame.progress.ProgressFrame;
import plugins.fmp.multispots.experiment.Experiment;


public class XLSExportGulpsResults  extends XLSExport 
{
	// -----------------------
	
	public void exportToFile(String filename, XLSExportOptions opt) 
	{	
		System.out.println("XLS capillary measures output");
		options = opt;
		expList = options.expList;

		boolean loadCapillaries = true;
		boolean loadDrosoTrack = options.onlyalive;
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
				progress.setMessage("XLSExpoportGulps:exportToFile() - Export experiment "+ (index+1) +" of "+ nbexpts);
				String charSeries = CellReference.convertNumToColString(iSeries);

				if (options.derivative) 	
					getDataAndExport(exp, column, charSeries, EnumXLSExportType.DERIVEDVALUES);
				if (options.sumGulps) 	
					getDataAndExport(exp, column, charSeries, EnumXLSExportType.SUMGULPS);
				if (options.lrPI && options.sumGulps) 	
					getDataAndExport(exp, column, charSeries, EnumXLSExportType.SUMGULPS_LR);
				if (options.nbGulps)
					getDataAndExport(exp, column, charSeries, EnumXLSExportType.NBGULPS);
				if (options.amplitudeGulps)
					getDataAndExport(exp, column, charSeries, EnumXLSExportType.AMPLITUDEGULPS);
				if (options.tToNextGulp)
					getDataAndExport(exp, column, charSeries, EnumXLSExportType.TTOGULP);
				if (options.tToNextGulp_LR)
					getDataAndExport(exp, column, charSeries, EnumXLSExportType.TTOGULP_LR);
				if (options.autocorrelation) {
					getDataAndExport(exp, column, charSeries, EnumXLSExportType.AUTOCORREL);
					getDataAndExport(exp, column, charSeries, EnumXLSExportType.AUTOCORREL_LR);
				}
				if (options.crosscorrelation) {
					getDataAndExport(exp, column, charSeries, EnumXLSExportType.CROSSCORREL);
					getDataAndExport(exp, column, charSeries, EnumXLSExportType.CROSSCORREL_LR);
				}
				
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
		System.out.println("XLSExpoportGulps:exportToFile() - XLS output finished");
	}

}
