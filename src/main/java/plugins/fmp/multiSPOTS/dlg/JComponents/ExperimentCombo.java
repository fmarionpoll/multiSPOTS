package plugins.fmp.multiSPOTS.dlg.JComponents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.swing.JComboBox;

import icy.gui.frame.progress.ProgressFrame;
import icy.system.SystemUtil;
import icy.system.thread.Processor;
import plugins.fmp.multiSPOTS.experiment.Experiment;
import plugins.fmp.multiSPOTS.tools.Comparators;
import plugins.fmp.multiSPOTS.tools.toExcel.EnumXLSColumnHeader;
import plugins.fmp.multiSPOTS.tools.toExcel.XLSExportOptions;

public class ExperimentCombo extends JComboBox<Experiment>
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public 	int 	index0 						= 0;
	public 	int 	index1 						= 0;
	public	int		maxSizeOfSpotsArrays 		= 0;
	public 	String 	stringExpBinSubDirectory 	= null; 
	

	public ExperimentCombo () 
	{
	}
	
	@Override
	public void removeAllItems() 
	{
		super.removeAllItems();
		stringExpBinSubDirectory = null;
	}
	
	public Experiment get_MsTime_of_StartAndEnd_AllExperiments(XLSExportOptions options) 
	{
		Experiment expAll = new Experiment();
		Experiment exp0 = getItemAt(0);
		if (options.fixedIntervals) 
		{
			expAll.camImageFirst_ms = options.startAll_Ms;
			expAll.camImageLast_ms = options.endAll_Ms;
		}
		else 
		{
			if (options.absoluteTime) 
			{
				Experiment expFirst =  exp0.getFirstChainedExperiment(options.collateSeries);
				expAll.setFileTimeImageFirst(expFirst.firstImage_FileTime);
				Experiment expLast = exp0.getLastChainedExperiment(options.collateSeries);
				expAll.setFileTimeImageLast(expLast.lastImage_FileTime);
				for (int i=0; i < getItemCount(); i++) 
				{
					Experiment exp = getItemAt(i);
					expFirst = exp.getFirstChainedExperiment(options.collateSeries);
					if (expAll.firstImage_FileTime.compareTo(expFirst.firstImage_FileTime) > 0) 
						expAll.setFileTimeImageFirst(expFirst.firstImage_FileTime);
					expLast = exp.getLastChainedExperiment(options.collateSeries);
					if (expAll.lastImage_FileTime .compareTo(expLast.lastImage_FileTime) <0)
						expAll.setFileTimeImageLast(expLast.lastImage_FileTime);
				}
				expAll.camImageFirst_ms = expAll.firstImage_FileTime.toMillis();
				expAll.camImageLast_ms = expAll.lastImage_FileTime.toMillis();	
			} 
			else 
			{
				expAll.camImageFirst_ms = 0;
				expAll.camImageLast_ms = exp0.binLast_ms- exp0.binFirst_ms;
				long firstOffset_Ms = 0;
				long lastOffset_Ms = 0;
				
				for (int i=0; i< getItemCount(); i++) 
				{
					Experiment exp = getItemAt(i);
					Experiment expFirst =  exp.getFirstChainedExperiment(options.collateSeries);
					firstOffset_Ms = expFirst.binFirst_ms + expFirst.camImageFirst_ms;
					exp.chainImageFirst_ms = expFirst.camImageFirst_ms + expFirst.binFirst_ms;
					
					Experiment expLast =  exp.getLastChainedExperiment (options.collateSeries); 
					if (expLast.binLast_ms <= 0) 
					{
						expLast.binLast_ms = expLast.camImageLast_ms - expLast.camImageFirst_ms;
					}
					lastOffset_Ms = expLast.binLast_ms + expLast.camImageFirst_ms;
					
					long diff = lastOffset_Ms - firstOffset_Ms;
					if (diff < 1) 
					{
						System.out.println("ExperimentCombo:get_MsTime_of_StartAndEnd_AllExperiments() Expt # " + i + ": FileTime difference between last and first image < 1; set dt between images = 1 ms");
						diff = exp.seqCamData.seq.getSizeT();
					}
					if (expAll.camImageLast_ms < diff) 
						expAll.camImageLast_ms = diff;
				}
			}
		}
		return expAll;
	}
		
	public boolean loadListOfMeasuresFromAllExperiments(boolean loadCapillaries, boolean loadSpots, boolean loadDrosoTrack) 
	{
		ProgressFrame progress = new ProgressFrame("Load experiment(s) parameters");
		int nexpts = getItemCount();

		maxSizeOfSpotsArrays = 0;
		progress.setLength(nexpts);
		boolean flag = true;
		
	    final Processor processor = new Processor(SystemUtil.getNumberOfCPUs());
	    processor.setThreadName("loadAllExperiments");
	    processor.setPriority(Processor.NORM_PRIORITY);
        ArrayList<Future<?>> futuresArray = new ArrayList<Future<?>>(nexpts);
		futuresArray.clear();
		
		for (int i = 0; i < getItemCount(); i++) 
		{
			final int it = i;
			final Experiment exp = getItemAt(it);
			
			futuresArray.add(processor.submit(new Runnable () 
			{
				@Override
				public void run() 
				{
					progress.setMessage("Load experiment "+ it +" of "+ nexpts);
					exp.setBinSubDirectory(stringExpBinSubDirectory);
					if (stringExpBinSubDirectory == null)
						exp.checkKymosDirectory(exp.getBinSubDirectory());
					
					if (loadCapillaries) 
						exp.openCapillarieMeasures();
					if (loadSpots) 
						exp.openSpotsMeasures();
					if (loadDrosoTrack) 
						exp.openPositionsMeasures();
					
					if (maxSizeOfSpotsArrays < exp.capillaries.capillariesList.size())
					{
						maxSizeOfSpotsArrays = exp.capillaries.capillariesList.size();
						if (maxSizeOfSpotsArrays % 2 != 0)
							maxSizeOfSpotsArrays += 1;
					}
					progress.incPosition();
				}}));
		}
		waitFuturesCompletion(processor, futuresArray, progress);
		
		progress.close();
		return flag;
	}
	
	protected void waitFuturesCompletion(Processor processor, ArrayList<Future<?>> futuresArray,  ProgressFrame progressBar) 
    {  	
  		 int frame= 1;
  		 int nframes = futuresArray.size();
    	 while (!futuresArray.isEmpty())
         {
             final Future<?> f = futuresArray.get(futuresArray.size() - 1);
             if (progressBar != null)
   				 progressBar.setMessage("Analyze frame: " + (frame) + "//" + nframes);
             try
             {
                 f.get();
             }
             catch (ExecutionException e)
             {
                 System.out.println("ExperimentCombo:waitFuturesCompletion() - Warning: " + e);
             }
             catch (InterruptedException e)
             {
                 // ignore
             }
             futuresArray.remove(f);
             frame ++;
         }
   }
	
	public void setFirstImageForAllExperiments(boolean collate)
	{
		for (int i = 0; i < getItemCount(); i++) 
		{
			Experiment expi = getItemAt(i);
			Experiment expFirst = expi.getFirstChainedExperiment(collate);
			expi.chainImageFirst_ms = expFirst.camImageFirst_ms + expFirst.binFirst_ms;
		}
	}
	
	private void resetChaining(Experiment expi) 
	{
			expi.chainToPreviousExperiment = null;
			expi.chainToNextExperiment = null; 
	}
	
	public void chainExperimentsUsingCamIndexes(boolean collate) 
	{
		for (int i = 0; i < getItemCount(); i++) 
		{
			Experiment expi = getItemAt(i);
			if (!collate) 
			{
				resetChaining(expi);
				continue;
			}
			
			for (int j = 0; j< getItemCount(); j++) 
			{
				if (i == j)
					continue;
				Experiment expj = getItemAt(j);
				if (!isSameDescriptors(expi, expj))
					continue;
				
				// same exp series: if before, insert eventually
				if (expj.camImageLast_ms < expi.camImageFirst_ms) 
				{
					if (expi.chainToPreviousExperiment == null)
						expi.chainToPreviousExperiment = expj;
					else if (expj.camImageLast_ms > expi.chainToPreviousExperiment.camImageLast_ms ) 
					{
						(expi.chainToPreviousExperiment).chainToNextExperiment = expj;
						expj.chainToPreviousExperiment = expi.chainToPreviousExperiment;
						expj.chainToNextExperiment = expi;
						expi.chainToPreviousExperiment = expj;
					}
					continue;
				}
				// same exp series: if after, insert eventually
				if (expj.camImageFirst_ms >= expi.camImageLast_ms) 
				{
					if (expi.chainToNextExperiment == null)
						expi.chainToNextExperiment = expj;
					else if (expj.camImageFirst_ms < expi.chainToNextExperiment.camImageFirst_ms ) 
					{
						(expi.chainToNextExperiment).chainToPreviousExperiment = expj;
						expj.chainToNextExperiment = (expi.chainToNextExperiment);
						expj.chainToPreviousExperiment = expi;
						expi.chainToNextExperiment = expj;
					}
					continue;
				}
				// it should never arrive here
				System.out.println("ExperimentCombo:chainExperimentsUsingCamIndexes() error in chaining "+ expi.getResultsDirectory() +" with ->" + expj.getResultsDirectory());
			}
		}
	}
	
	public void chainExperimentsUsingKymoIndexes(boolean collate) 
	{	
		for (int i = 0; i < getItemCount(); i++) 
		{
			Experiment expi = getItemAt(i);
			if (!collate) 
			{
				resetChaining(expi);
				continue;
			}
			if (expi.chainToNextExperiment != null || expi.chainToPreviousExperiment != null)
				continue;
			
			List <Experiment> list = new ArrayList<Experiment> ();
			list.add(expi);
			
			for (int j = 0; j < getItemCount(); j++) 
			{
				if (i == j)
					continue;
				Experiment expj = getItemAt(j);
				if (!isSameDescriptors(expi, expj))
					continue;
				if (expj.chainToNextExperiment != null || expj.chainToPreviousExperiment != null)
					continue;
				list.add(expj);
			}
			
			if (list.size() < 2)
				continue;
			
			Collections.sort(list, new Comparators.Experiment_Start_Comparator ());
			for (int k = 0; k < list.size(); k++) 
			{
				Experiment expk = list.get(k);
				if (k > 0)
					expk.chainToPreviousExperiment = list.get(k-1);
				if (k < (list.size() -1))	 
					expk.chainToNextExperiment = list.get(k+1);
			}
		}
	}
		
	private boolean isSameDescriptors(Experiment exp, Experiment expi) 
	{
		boolean flag = true;
		flag &= expi.getExperimentField(EnumXLSColumnHeader.EXP_EXPT) .equals(exp.getExperimentField(EnumXLSColumnHeader.EXP_EXPT)) ; 
		flag &= expi.getExperimentField(EnumXLSColumnHeader.EXP_BOXID) .equals(exp.getExperimentField(EnumXLSColumnHeader.EXP_BOXID)) ;
		flag &= expi.getExperimentField(EnumXLSColumnHeader.EXP_STIM) .equals(exp.getExperimentField(EnumXLSColumnHeader.EXP_STIM));
		flag &= expi.getExperimentField(EnumXLSColumnHeader.EXP_CONC) .equals(exp.getExperimentField(EnumXLSColumnHeader.EXP_CONC));
		flag &= expi.getExperimentField(EnumXLSColumnHeader.EXP_STRAIN) .equals(exp.getExperimentField(EnumXLSColumnHeader.EXP_STRAIN));
		flag &= expi.getExperimentField(EnumXLSColumnHeader.EXP_SEX) .equals(exp.getExperimentField(EnumXLSColumnHeader.EXP_SEX));
		flag &= expi.getExperimentField(EnumXLSColumnHeader.EXP_COND1) 	.equals(exp.getExperimentField(EnumXLSColumnHeader.EXP_COND1));
		flag &= expi.getExperimentField(EnumXLSColumnHeader.EXP_COND2) 	.equals(exp.getExperimentField(EnumXLSColumnHeader.EXP_COND2));
		return flag;
	}

	public int getExperimentIndexFromExptName(String filename) 
	{
		int position = -1;
		if (filename != null) 
		{
			for (int i = 0; i < getItemCount(); i++) 
			{
				if (filename.equals(getItemAt(i).toString())) 
				{
					position = i;
					break;
				}
			}
		}
		return position;
	}
	
	public Experiment getExperimentFromExptName(String filename) 
	{
		Experiment exp = null;
		for (int i = 0; i < getItemCount(); i++) {
			String expString = getItemAt(i).toString();
			if (filename.equals(expString)) 
			{
				exp = getItemAt(i);
				break;
			}
		}
		return exp;
	}
	
	// ---------------------
		
	public int addExperiment (Experiment exp, boolean allowDuplicates) 
	{
		String exptName = exp.toString();
		int index = getExperimentIndexFromExptName(exptName);
		if (allowDuplicates || index < 0)
		{
			addItem(exp);
			index = getExperimentIndexFromExptName(exptName);
		}
		return index;
	}
	
	public List<String> getFieldValuesFromAllExperiments(EnumXLSColumnHeader field) 
	{
		List<String> textList = new ArrayList<>();
		for (int i = 0; i < getItemCount(); i++) 
		{
			Experiment exp = getItemAt(i);
			exp.getFieldValues(field, textList);
		}
		return textList;
	}
	
	public void getFieldValuesToCombo(JComboBox<String> combo, EnumXLSColumnHeader header)
	{
		combo.removeAllItems();
		List<String> textList = getFieldValuesFromAllExperiments(header);
		java.util.Collections.sort(textList);
		for (String text: textList)
			combo.addItem(text);
	}

	public List<Experiment> getExperimentsAsList()
	{
		int nitems = getItemCount();
		List<Experiment> expList = new ArrayList<Experiment>(nitems);
		for (int i = 0; i < nitems; i++) 
			expList.add(getItemAt(i));
		return expList;
	}
	
	public void setExperimentsFromList (List<Experiment> listExp)
	{
		removeAllItems();
		for (Experiment exp : listExp)
			addItem(exp);
	}

	
}
