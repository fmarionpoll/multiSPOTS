package plugins.fmp.multiSPOTS.tools.Sequence;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import icy.canvas.Layer;
import icy.gui.frame.progress.ProgressFrame;
import icy.gui.viewer.Viewer;
import icy.roi.ROI;
import icy.sequence.Sequence;
import icy.system.thread.Processor;


public abstract class SequenceTransformAbstract  extends SwingWorker<Integer, Integer> 
{

	public 	SequenceTransformOptions options = new SequenceTransformOptions();
	public	boolean stopFlag 		= false;
	public 	boolean  threadRunning 	= false;
			int selectedExperimentIndex = -1;
			Viewer temporaryViewer = null;
			
	@Override
	protected Integer doInBackground() throws Exception 
	{
		System.out.println("SequenceTransform:diBackgraound loop over images");
        threadRunning = true;
		
		ProgressFrame progress = new ProgressFrame("Analyze sequence");
		long startTimeInNs = System.nanoTime();
		progress.setMessage("Processing sequence ");
			
		runFilter(options.seq);
		long endTime2InNs = System.nanoTime();
		System.out.println("process ended - duration: "+((endTime2InNs-startTimeInNs)/ 1000000000f) + " s");
			
		progress.close();
		threadRunning = false;
		return 1;
	}

	@Override
	protected void done() 
	{
		int statusMsg = 0;
		try 
		{
			statusMsg = super.get();
		} 
		catch (InterruptedException | ExecutionException e) 
		{
			e.printStackTrace();
		} 
		if (!threadRunning || stopFlag) 
		{
			firePropertyChange("thread_ended", null, statusMsg);
		} 
		else 
		{
			firePropertyChange("thread_done", null, statusMsg);
		}
    }
	
	abstract void runFilter(Sequence seq);
	
    protected void waitFuturesCompletion(Processor processor, ArrayList<Future<?>> futuresArray,  ProgressFrame progressBar) 
    {  	
  		 int frame = 0;
  		 int nframes = futuresArray.size();

  		while (!futuresArray.isEmpty())
        {
            final Future<?> f = futuresArray.get(futuresArray.size() - 1);
            if (progressBar != null)
  				 progressBar.setMessage("Frame: " + (frame) + "//" + nframes);
            try
            {
                f.get();
            }
            catch (ExecutionException e)
            {
                System.out.println("SequenceTransform:waitFuturesCompletion - frame:" + frame +" Execution exception: " + e);
            }
            catch (InterruptedException e)
            {
           	 	System.out.println("SequenceTransform:waitFuturesCompletion - Interrupted exception: " + e);
            }
            futuresArray.remove(f);
            frame ++;
        }
   }
    
	void closeSequenceViewer ()
	{
		if (temporaryViewer != null) 
		{
			temporaryViewer.close();
			temporaryViewer = null;
		}
	}

	void openSequenceViewer(Sequence seq) 
	{
		closeSequenceViewer();
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() 
				{			
					temporaryViewer = new Viewer(seq, true);
					List<Layer> layers = temporaryViewer.getCanvas().getLayers(false);
					if (layers != null) {
						for (Layer layer: layers) 
						{
							ROI roi = layer.getAttachedROI();
							if (roi == null)
								continue;
							layer.setVisible(false);
						}
					}
				}});
		} 
		catch (InvocationTargetException | InterruptedException e) 
		{
			e.printStackTrace();
		}
	}

}
