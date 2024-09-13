package plugins.fmp.multiSPOTS.experiment;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Node;

import icy.image.IcyBufferedImage;
import icy.roi.BooleanMask2D;
import icy.roi.ROI2D;
import icy.util.XMLUtil;
import plugins.fmp.multiSPOTS.series.BuildSeriesOptions;
import plugins.fmp.multiSPOTS.tools.ROI2D.ROI2DUtilities;
import plugins.fmp.multiSPOTS.tools.toExcel.EnumXLSColumnHeader;
import plugins.fmp.multiSPOTS.tools.toExcel.EnumXLSExportType;
import plugins.kernel.roi.roi2d.ROI2DPolyLine;
import plugins.kernel.roi.roi2d.ROI2DShape;

public class Spot implements Comparable<Spot> {

	private ROI2DShape spotRoi_in = null;
	private ROI2DShape spotRoi_out = null;
	private ROI2DShape spotRoi_old = null;
	private ArrayList<ROI2DAlongT> listRoiAlongT = new ArrayList<ROI2DAlongT>();

	public BooleanMask2D mask2DSpot = null;

	public int cageIndex = -1;
	public String version = null;
	public String spotStim = new String("..");
	public String spotConc = new String("..");
	public String spotCageSide = ".";
	public int spotNFlies = 1;
	public int spotIndex = 0;
	public double spotVolume = 1;
	public int spotNPixels = 1;
	public int radius = 30;
	public boolean descriptionOK = false;
	public int versionInfos = 0;

	public BuildSeriesOptions limitsOptions = new BuildSeriesOptions();

	public int spot_CamData_T = -1;
	public int spot_Kymograph_T = -1;
	public String spot_filenameTIFF = null;
	public IcyBufferedImage spot_Image = null;

	public SpotMeasure sum_in = new SpotMeasure("sum");
	public SpotMeasure sum_clean = new SpotMeasure("clean");
	public SpotMeasure flyPresent = new SpotMeasure("flyPresent");
	public boolean valid = true;

	private final String ID_META = "metaMC";
	private final String ID_NFLIES = "nflies";
	private final String ID_CAGENB = "cage_number";
	private final String ID_SPOTVOLUME = "volume";
	private final String ID_PIXELS = "pixels";
	private final String ID_RADIUS = "radius";
	private final String ID_STIML = "stimulus";
	private final String ID_CONCL = "concentration";
	private final String ID_SIDE = "side";
	private final String ID_DESCOK = "descriptionOK";
	private final String ID_VERSIONINFOS = "versionInfos";
	private final String ID_INTERVALS = "INTERVALS";
	private final String ID_NINTERVALS = "nintervals";
	private final String ID_INTERVAL = "interval_";
	private final String ID_INDEXIMAGE = "indexImageMC";
	private final String ID_VERSION = "version";
	private final String ID_VERSIONNUM = "1.0.0";

	// ----------------------------------------------------

	public Spot(ROI2DShape roi) {
		this.spotRoi_in = roi;
	}

	Spot(String name) {
	}

	public Spot() {
	}

	@Override
	public int compareTo(Spot o) {
		if (o != null)
			return (this.spotRoi_in.getName()).compareTo(o.spotRoi_in.getName());
		return 1;
	}

	// ------------------------------------------

	public void copySpot(Spot spotFrom) {
		cageIndex = spotFrom.cageIndex;
		version = spotFrom.version;
		spotRoi_in = (ROI2DShape) spotFrom.spotRoi_in.getCopy();

		spotStim = spotFrom.spotStim;
		spotConc = spotFrom.spotConc;
		spotCageSide = spotFrom.spotCageSide;
		spotNFlies = spotFrom.spotNFlies;
		spotIndex = spotFrom.spotIndex;
		spotVolume = spotFrom.spotVolume;
		spotNPixels = spotFrom.spotNPixels;
		radius = spotFrom.radius;

		limitsOptions = spotFrom.limitsOptions;

		sum_in.copyLevel2D(spotFrom.sum_in);
		sum_clean.copyLevel2D(spotFrom.sum_clean);
//		sum_out.copyLevel2D(spotFrom.sum_out);
//		sum_diff.copyLevel2D(spotFrom.sum_diff);
		flyPresent.copyLevel2D(spotFrom.flyPresent);
	}

	public ROI2D getRoi_in() {
		return spotRoi_in;
	}

	public ROI2D getRoi_old() {
		return spotRoi_old;
	}

	public ROI2D getRoi_out() {
		return spotRoi_out;
	}

	public void setRoi_in(ROI2DShape roi) {
		this.spotRoi_in = roi;
		listRoiAlongT.clear();
	}

	public void setRoi_old(ROI2DShape roi) {
		this.spotRoi_old = roi;
	}

	public void setRoi_out(ROI2DShape roi) {
		this.spotRoi_out = roi;
	}

	public void setRoiName(String name) {
		spotRoi_in.setName(name);
	}

	public String getRoiName() {
		return spotRoi_in.getName();
	}

	public String getLast2ofSpotName() {
		if (spotRoi_in == null)
			return "missing";
		return spotRoi_in.getName().substring(spotRoi_in.getName().length() - 2);
	}

	public String getSpotSide() {
		return spotRoi_in.getName().substring(spotRoi_in.getName().length() - 2);
	}

	public static String xreplace_LR_with_12(String name) {
		String newname = name;
		if (name.contains("R"))
			newname = name.replace("R", "2");
		else if (name.contains("L"))
			newname = name.replace("L", "1");
		return newname;
	}

	public int getCageIndexFromRoiName() {
		String name = spotRoi_in.getName();
		if (!name.contains("spot"))
			return -1;
		return Integer.valueOf(name.substring(4, 6));
	}

	public String getSideDescriptor(EnumXLSExportType xlsExportOption) {
		String value = null;
		spotCageSide = getSpotSide();
		switch (xlsExportOption) {
		case DISTANCE:
		case ISALIVE:
			value = spotCageSide + "(T=B)";
			break;
		case TOPLEVELDELTA_LR:
		case TOPLEVEL_LR:
			if (spotCageSide.equals("00"))
				value = "sum";
			else
				value = "PI";
			break;
		case XYIMAGE:
		case XYTOPCAGE:
		case XYTIPCAPS:
			if (spotCageSide.equals("00"))
				value = "x";
			else
				value = "y";
			break;
		default:
			value = spotCageSide;
			break;
		}
		return value;
	}

	public String getSpotField(EnumXLSColumnHeader fieldEnumCode) {
		String stringValue = null;
		switch (fieldEnumCode) {
		case CAP_STIM:
			stringValue = spotStim;
			break;
		case CAP_CONC:
			stringValue = spotConc;
			break;
		default:
			break;
		}
		return stringValue;
	}

	public void setSpotField(EnumXLSColumnHeader fieldEnumCode, String stringValue) {
		switch (fieldEnumCode) {
		case CAP_STIM:
			spotStim = stringValue;
			break;
		case CAP_CONC:
			spotConc = stringValue;
			break;
		default:
			break;
		}
	}

	public Point2D getSpotCenter() {
		Point pt = spotRoi_in.getPosition();
		Rectangle rect = spotRoi_in.getBounds();
		pt.translate(rect.height / 2, rect.width / 2);
		return pt;
	}

	private SpotMeasure getSpotArea(EnumXLSExportType option) {
		switch (option) {
		case AREA_SUM:
		case AREA_SUM_LR:
			return sum_in;
		case AREA_SUMCLEAN:
		case AREA_SUMCLEAN_LR:
			return sum_clean;
//		case AREA_OUT:
//			return sum_out;
//		case AREA_DIFF:
//			return sum_diff;
		case AREA_FLYPRESENT:
			return flyPresent;
		default:
			return null;
		}
	}

	// -----------------------------------------

	public boolean isThereAnyMeasuresDone(EnumXLSExportType option) {
		SpotMeasure spotArea = getSpotArea(option);
		if (spotArea != null)
			return spotArea.isThereAnyMeasuresDone();
		return false;
	}

	public ArrayList<Double> getSpotMeasuresForXLSPass1(EnumXLSExportType option, long seriesBinMs, long outputBinMs) {
		SpotMeasure spotArea = getSpotArea(option);
		if (spotArea != null)
			return spotArea.getLevel2D_Y_subsampled(seriesBinMs, outputBinMs);
		return null;
	}

	public void cropSpotMeasuresToNPoints(int npoints) {
		cropSpotMeasureToNPoints(sum_in, npoints);
		cropSpotMeasureToNPoints(sum_clean, npoints);
//		cropSpotMeasureToNPoints(sum_out, npoints);
//		cropSpotMeasureToNPoints(sum_diff, npoints);
		cropSpotMeasureToNPoints(flyPresent, npoints);
	}

	private void cropSpotMeasureToNPoints(SpotMeasure spotMeasure, int npoints) {
		if (spotMeasure.getLevel2DNPoints() > 0)
			spotMeasure.cropLevel2DToNPoints(npoints);
	}

	public void restoreClippedSpotMeasures() {
		restoreClippedMeasures(sum_in);
		restoreClippedMeasures(sum_clean);
//		restoreClippedMeasures(sum_out);
//		restoreClippedMeasures(sum_diff);
		restoreClippedMeasures(flyPresent);
	}

	private void restoreClippedMeasures(SpotMeasure spotMeasure) {
		if (spotMeasure.getLevel2DNPoints() > 0)
			spotMeasure.restoreCroppedLevel2D();
	}

	public void transferROIsMeasuresToLevel2D() {
		sum_in.transferROItoLevel2D();
		sum_clean.transferROItoLevel2D();
//		sum_out.transferROItoLevel2D();
//		sum_diff.transferROItoLevel2D();
		flyPresent.transferROItoLevel2D();
	}

	// -----------------------------------------------------------------------------

	public boolean loadFromXML_SpotOnly(Node node) {
		final Node nodeMeta = XMLUtil.getElement(node, ID_META);
		boolean flag = (nodeMeta != null);
		if (flag) {
			version = XMLUtil.getElementValue(nodeMeta, ID_VERSION, "0.0.0");
			cageIndex = XMLUtil.getElementIntValue(nodeMeta, ID_INDEXIMAGE, cageIndex);

			descriptionOK = XMLUtil.getElementBooleanValue(nodeMeta, ID_DESCOK, false);
			versionInfos = XMLUtil.getElementIntValue(nodeMeta, ID_VERSIONINFOS, 0);
			spotNFlies = XMLUtil.getElementIntValue(nodeMeta, ID_NFLIES, spotNFlies);
			spotIndex = XMLUtil.getElementIntValue(nodeMeta, ID_CAGENB, spotIndex);
			spotVolume = XMLUtil.getElementDoubleValue(nodeMeta, ID_SPOTVOLUME, Double.NaN);
			spotNPixels = XMLUtil.getElementIntValue(nodeMeta, ID_PIXELS, 5);
			radius = XMLUtil.getElementIntValue(nodeMeta, ID_RADIUS, 30);
			spotStim = XMLUtil.getElementValue(nodeMeta, ID_STIML, ID_STIML);
			spotConc = XMLUtil.getElementValue(nodeMeta, ID_CONCL, ID_CONCL);
			spotCageSide = XMLUtil.getElementValue(nodeMeta, ID_SIDE, ".");

			spotRoi_in = (ROI2DShape) ROI2DUtilities.loadFromXML_ROI(nodeMeta);
			setSpotRoi_InColorAccordingToSpotIndex();
			limitsOptions.loadFromXML(nodeMeta);

			loadFromXML_SpotAlongT(node);
		}
		return flag;
	}

	public void setSpotRoi_InColorAccordingToSpotIndex() {
		Color value = ((spotIndex % 2) == 0) ? Color.red : Color.blue;
		spotRoi_in.setColor(value);
	}

	private boolean loadFromXML_SpotAlongT(Node node) {
		listRoiAlongT.clear();
		final Node nodeMeta2 = XMLUtil.getElement(node, ID_INTERVALS);
		if (nodeMeta2 == null)
			return false;
		int nitems = XMLUtil.getElementIntValue(nodeMeta2, ID_NINTERVALS, 0);
		if (nitems > 0) {
			for (int i = 0; i < nitems; i++) {
				Node node_i = XMLUtil.setElement(nodeMeta2, ID_INTERVAL + i);
				ROI2DAlongT roiInterval = new ROI2DAlongT();
				roiInterval.loadFromXML(node_i);
				listRoiAlongT.add(roiInterval);

				if (i == 0) {
					spotRoi_in = (ROI2DShape) listRoiAlongT.get(0).getRoi_in();
				}
			}
		}
		return true;
	}

	public boolean saveToXML_SpotOnly(Node node) {
		final Node nodeMeta = XMLUtil.setElement(node, ID_META);
		if (nodeMeta == null)
			return false;
		if (version == null)
			version = ID_VERSIONNUM;
		XMLUtil.setElementValue(nodeMeta, ID_VERSION, version);
		XMLUtil.setElementIntValue(nodeMeta, ID_INDEXIMAGE, cageIndex);

		XMLUtil.setElementBooleanValue(nodeMeta, ID_DESCOK, descriptionOK);
		XMLUtil.setElementIntValue(nodeMeta, ID_VERSIONINFOS, versionInfos);
		XMLUtil.setElementIntValue(nodeMeta, ID_NFLIES, spotNFlies);
		XMLUtil.setElementIntValue(nodeMeta, ID_CAGENB, spotIndex);
		XMLUtil.setElementDoubleValue(nodeMeta, ID_SPOTVOLUME, spotVolume);
		XMLUtil.setElementIntValue(nodeMeta, ID_PIXELS, spotNPixels);
		XMLUtil.setElementIntValue(nodeMeta, ID_RADIUS, radius);
		XMLUtil.setElementValue(nodeMeta, ID_STIML, spotStim);
		XMLUtil.setElementValue(nodeMeta, ID_SIDE, spotCageSide);
		XMLUtil.setElementValue(nodeMeta, ID_CONCL, spotConc);

		ROI2DUtilities.saveToXML_ROI(nodeMeta, spotRoi_in);

		boolean flag = saveToXML_SpotAlongT(node);
		return flag;
	}

	private boolean saveToXML_SpotAlongT(Node node) {
		final Node nodeMeta2 = XMLUtil.setElement(node, ID_INTERVALS);
		if (nodeMeta2 == null)
			return false;
		int nitems = listRoiAlongT.size();
		XMLUtil.setElementIntValue(nodeMeta2, ID_NINTERVALS, nitems);
		if (nitems > 0) {
			for (int i = 0; i < nitems; i++) {
				Node node_i = XMLUtil.setElement(nodeMeta2, ID_INTERVAL + i);
				listRoiAlongT.get(i).saveToXML(node_i);
			}
		}
		return true;
	}

	// --------------------------------------------

	public List<ROI2DAlongT> getROIAlongTList() {
		if (listRoiAlongT.size() < 1)
			initROIAlongTList();
		return listRoiAlongT;
	}

	public ROI2DAlongT getROIAtT(long t) {
		if (listRoiAlongT.size() < 1)
			initROIAlongTList();

		ROI2DAlongT spotRoi = null;
		for (ROI2DAlongT item : listRoiAlongT) {
			if (t < item.getT())
				break;
			spotRoi = item;
		}
		return spotRoi;
	}

	public void removeROIAlongTListItem(long t) {
		ROI2DAlongT itemFound = null;
		for (ROI2DAlongT item : listRoiAlongT) {
			if (t != item.getT())
				continue;
			itemFound = item;
		}
		if (itemFound != null)
			listRoiAlongT.remove(itemFound);
	}

	private void initROIAlongTList() {
		listRoiAlongT.add(new ROI2DAlongT(0, spotRoi_in));
	}

	// --------------------------------------------

	public void adjustLevel2DMeasuresToImageWidth(int imageWidth) {
		sum_in.adjustLevel2DToImageWidth(imageWidth);
		sum_clean.adjustLevel2DToImageWidth(imageWidth);
//		sum_out.adjustLevel2DToImageWidth(imageWidth);
//		sum_diff.adjustLevel2DToImageWidth(imageWidth);
		flyPresent.adjustLevel2DToImageWidth(imageWidth);
	}

	public void cropLevel2DMeasuresToImageWidth(int imageWidth) {
		sum_in.cropLevel2DToNPoints(imageWidth);
		sum_clean.cropLevel2DToNPoints(imageWidth);
//		sum_out.cropLevel2DToNPoints(imageWidth);
//		sum_diff.cropLevel2DToNPoints(imageWidth);
		flyPresent.cropLevel2DToNPoints(imageWidth);
	}

	public void initLevel2DMeasures() {
		sum_in.initLevel2D_fromMeasureValues(getRoi_in().getName());
		sum_clean.initLevel2D_fromMeasureValues(getRoi_in().getName());
//		sum_out.initLevel2D_fromMeasureValues(getRoi_in().getName());
//		sum_diff.initLevel2D_fromMeasureValues(getRoi_in().getName());
		flyPresent.initLevel2D_fromBooleans(getRoi_in().getName());
	}

	public void buildRunningMedianFromSumLevel2D(int imageHeight) {
		int span = 10;
		if (sum_in.values != null)
			sum_clean.buildRunningMedian(span, sum_in.values);
		else
			sum_clean.buildRunningMedian(span, sum_in.getLevel2D().ypoints);
		sum_clean.initLevel2D_fromMeasureValues(sum_clean.getName());
	}

	public List<ROI2D> transferSpotMeasuresToROIs(int imageHeight) {
		List<ROI2D> measuresRoisList = new ArrayList<ROI2D>();
		if (sum_in.getLevel2DNPoints() != 0)
			measuresRoisList.add(sum_in.getROIForImage(spotRoi_in.getName(), spot_Kymograph_T, imageHeight));
		if (sum_clean.getLevel2DNPoints() != 0)
			measuresRoisList.add(sum_clean.getROIForImage(spotRoi_in.getName(), spot_Kymograph_T, imageHeight));
//		if (sum_out.getLevel2DNPoints() != 0)
//			measuresRoisList.add(sum_out.getROIForImage(spotRoi_in.getName(), spot_Kymograph_T, imageHeight));
//		if (sum_diff.getLevel2DNPoints() != 0)
//			measuresRoisList.add(sum_diff.getROIForImage(spotRoi_in.getName(), spot_Kymograph_T, imageHeight));
		if (flyPresent.getLevel2DNPoints() != 0)
			measuresRoisList.add(flyPresent.getROIForImage(spotRoi_in.getName(), spot_Kymograph_T, 10));
		return measuresRoisList;
	}

	public void transferROItoMeasures(ROI2D roi, int imageHeight) {
		String name = roi.getName();
		if (name.contains(sum_in.getName())) {
			transferROItoMeasureValue(roi, imageHeight, sum_in);
		} else if (name.contains(sum_clean.getName())) {
			transferROItoMeasureValue(roi, imageHeight, sum_clean);
//		} else if (name.contains(sum_out.getName())) {
//			transferROItoMeasureValue(roi, imageHeight, sum_out);
//		} else if (name.contains(sum_diff.getName())) {
//			transferROItoMeasureValue(roi, imageHeight, sum_diff);
		} else if (name.contains(flyPresent.getName())) {
			transferROItoMeasureBoolean(roi, flyPresent);
		}
	}

	private void transferROItoMeasureValue(ROI2D roi, int imageHeight, SpotMeasure spotMeasure) {
		if (roi instanceof ROI2DPolyLine) {
			Level2D level2D = new Level2D(((ROI2DPolyLine) roi).getPolyline2D());
			level2D.multiply_Y(imageHeight);
			spotMeasure.setLevel2D(level2D);
		}
	}

	private void transferROItoMeasureBoolean(ROI2D roi, SpotMeasure spotMeasure) {
		if (roi instanceof ROI2DPolyLine) {
			Level2D level2D = new Level2D(((ROI2DPolyLine) roi).getPolyline2D());
			level2D.threshold_Y(1.);
			spotMeasure.setLevel2D(level2D);
		}
	}

	// -----------------------------------------------------------------------------

	public String csvExportSpotArrayHeader(String csvSep) {
		StringBuffer sbf = new StringBuffer();
		sbf.append("#" + csvSep + "SPOTS" + csvSep + "describe each spot\n");
		List<String> row2 = Arrays.asList("index", "name", "cage", "nflies", "volume", "npixel", "radius", "stim",
				"conc", "side");
		sbf.append(String.join(csvSep, row2));
		sbf.append("\n");
		return sbf.toString();
	}

	public String csvExportDescription(String csvSep) {
		StringBuffer sbf = new StringBuffer();
		List<String> row = Arrays.asList(Integer.toString(spotIndex), getRoi_in().getName(),
				Integer.toString(cageIndex), Integer.toString(spotNFlies), Double.toString(spotVolume),
				Integer.toString(spotNPixels), Integer.toString(radius), spotStim.replace(",", "."),
				spotConc.replace(",", "."), spotCageSide.replace(",", "."));
		sbf.append(String.join(csvSep, row));
		sbf.append("\n");
		return sbf.toString();
	}

	public String csvExportMeasures_SectionHeader(EnumSpotMeasures measureType, String csvSep) {
		StringBuffer sbf = new StringBuffer();
		List<String> listExplanation1 = Arrays.asList("\n name", "index", "npts", "yi", "\n");
		String explanation1 = String.join(csvSep, listExplanation1);
		switch (measureType) {
		case AREA_SUM:
		case AREA_SUMCLEAN:
		case AREA_FLYPRESENT:
		case AREA_OUT:
		case AREA_DIFF:
			sbf.append("#" + csvSep + measureType.toString() + csvSep + explanation1);
			break;
		default:
			sbf.append("#" + csvSep + "UNDEFINED" + csvSep + "------------\n");
			break;
		}
		return sbf.toString();
	}

	public String csvExportMeasures_OneType(EnumSpotMeasures measureType, String csvSep) {
		StringBuffer sbf = new StringBuffer();
		sbf.append(spotRoi_in.getName() + csvSep + spotIndex + csvSep);
		switch (measureType) {
		case AREA_SUM:
			sum_in.cvsExportYDataToRow(sbf, csvSep);
			break;
		case AREA_SUMCLEAN:
			sum_clean.cvsExportYDataToRow(sbf, csvSep);
			break;
//		case AREA_OUT:
//			sum_out.cvsExportYDataToRow(sbf, csvSep);
//			break;
//		case AREA_DIFF:
//			sum_diff.cvsExportYDataToRow(sbf, csvSep);
//			break;
		case AREA_FLYPRESENT:
			flyPresent.cvsExportYDataToRow(sbf, csvSep);
			break;
		default:
			break;
		}
		sbf.append("\n");
		return sbf.toString();
	}

	public void csvImportDescription(String[] data, boolean dummyColumn) {
		int i = dummyColumn ? 1 : 0;
		spotIndex = Integer.valueOf(data[i]);
		i++;
		spotRoi_in.setName(data[i]);
		i++;
		cageIndex = Integer.valueOf(data[i]);
		i++;
		spotNFlies = Integer.valueOf(data[i]);
		i++;
		spotVolume = Double.valueOf(data[i]);
		i++;
		spotNPixels = Integer.valueOf(data[i]);
		i++;
		radius = Integer.valueOf(data[i]);
		i++;
		spotStim = data[i];
		i++;
		spotConc = data[i];
		i++;
		spotCageSide = data[i];
	}

	public void csvImportMeasures_OneType(EnumSpotMeasures measureType, String[] data, boolean x, boolean y) {
		if (x && y) {
			switch (measureType) {
			case AREA_SUM:
				sum_in.csvImportXYDataFromRow(data, 2);
				break;
			case AREA_SUMCLEAN:
				sum_clean.csvImportXYDataFromRow(data, 2);
				break;
//			case AREA_OUT:
//				sum_out.csvImportXYDataFromRow(data, 2);
//				break;
//			case AREA_DIFF:
//				sum_diff.csvImportXYDataFromRow(data, 2);
//				break;
			case AREA_FLYPRESENT:
				flyPresent.csvImportXYDataFromRow(data, 2);
				break;
			default:
				break;
			}
		} else if (!x && y) {
			switch (measureType) {
			case AREA_SUM:
				sum_in.csvImportYDataFromRow(data, 2);
				break;
			case AREA_SUMCLEAN:
				sum_clean.csvImportYDataFromRow(data, 2);
				break;
//			case AREA_OUT:
//				sum_out.csvImportYDataFromRow(data, 2);
//				break;
//			case AREA_DIFF:
//				sum_diff.csvImportYDataFromRow(data, 2);
//				break;
			case AREA_FLYPRESENT:
				flyPresent.csvImportYDataFromRow(data, 2);
				break;
			default:
				break;
			}
		}
	}

}
