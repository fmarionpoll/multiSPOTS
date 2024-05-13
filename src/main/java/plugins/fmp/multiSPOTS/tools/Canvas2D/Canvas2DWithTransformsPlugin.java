package plugins.fmp.multiSPOTS.tools.Canvas2D;

import icy.canvas.IcyCanvas;
import icy.gui.viewer.Viewer;
import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginCanvas;


public class Canvas2DWithTransformsPlugin extends Plugin implements PluginCanvas
{
	@Override
	public String getCanvasClassName() {
		return Canvas2DWithTransformsPlugin.class.getName();
	}

	@Override
	public IcyCanvas createCanvas(Viewer viewer) 
	{		
		return new Canvas2DWithTransforms(viewer);
	}

}
