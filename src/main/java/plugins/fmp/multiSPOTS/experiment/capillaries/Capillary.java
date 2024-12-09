package plugins.fmp.multiSPOTS.experiment.capillaries;

import java.awt.Color;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Node;

import icy.roi.ROI;
import icy.roi.ROI2D;
import icy.type.geom.Polyline2D;
import icy.util.XMLUtil;
import plugins.fmp.multiSPOTS.experiment.capillaries.Capillary;
import plugins.fmp.multiSPOTS.series.BuildSeriesOptions;
import plugins.fmp.multiSPOTS.tools.ROI2D.ROI2DAlongT;
import plugins.fmp.multiSPOTS.tools.ROI2D.ROI2DUtilities;
import plugins.fmp.multiSPOTS.tools.toExcel.EnumXLSColumnHeader;
import plugins.fmp.multiSPOTS.tools.toExcel.EnumXLSExportType;
import plugins.kernel.roi.roi2d.ROI2DLine;
import plugins.kernel.roi.roi2d.ROI2DPolyLine;

public class Capillary implements Comparable<Capillary> {

	private ROI2D roi = null;
	private ArrayList<ROI2DAlongT> roisForKymo = new ArrayList<ROI2DAlongT>();
	private String kymographName = null;
	public int kymographIndex = -1;
	private String kymographPrefix = null;

	public String version = null;
	public String filenameTIFF = null;

	public ArrayList<int[]> cap_Integer = null;

	public String stimulus = new String("..");
	public String concentration = new String("..");
	public String cageSide = ".";
	public int nFlies = 1;
	public int cageID = 0;
	public double volume = 5.;
	public int pixels = 5;
	public boolean descriptionOK = false;
	public int versionInfos = 0;

	public BuildSeriesOptions limitsOptions = new BuildSeriesOptions();

	public final String ID_TOPLEVEL = "toplevel";
	public final String ID_BOTTOMLEVEL = "bottomlevel";
	public final String ID_DERIVATIVE = "derivative";

	public CapillaryLevel ptsTop = new CapillaryLevel(ID_TOPLEVEL);
	public CapillaryLevel ptsBottom = new CapillaryLevel(ID_BOTTOMLEVEL);
	public CapillaryLevel ptsDerivative = new CapillaryLevel(ID_DERIVATIVE);

	public boolean valid = true;

	private final String ID_META = "metaMC";
	private final String ID_NFLIES = "nflies";
	private final String ID_CAGENB = "cage_number";
	private final String ID_CAPVOLUME = "capillaryVolume";
	private final String ID_CAPPIXELS = "capillaryPixels";
	private final String ID_STIML = "stimulus";
	private final String ID_CONCL = "concentration";
	private final String ID_SIDE = "side";
	private final String ID_DESCOK = "descriptionOK";
	private final String ID_VERSIONINFOS = "versionInfos";

	private final String ID_INTERVALS = "INTERVALS";
	private final String ID_NINTERVALS = "nintervals";
	private final String ID_INTERVAL = "interval_";

	private final String ID_INDEXIMAGE = "indexImageMC";
	private final String ID_NAME = "nameMC";
	private final String ID_NAMETIFF = "filenameTIFF";
	private final String ID_VERSION = "version";
	private final String ID_VERSIONNUM = "1.0.0";

	// ----------------------------------------------------

	public Capillary(ROI2D roiCapillary) {
		this.roi = roiCapillary;
		this.kymographName = replace_LR_with_12(roiCapillary.getName());
	}

	Capillary(String name) {
		this.kymographName = replace_LR_with_12(name);
	}

	public Capillary() {
	}

	@Override
	public int compareTo(Capillary o) {
		if (o != null)
			return this.kymographName.compareTo(o.kymographName);
		return 1;
	}

	// ------------------------------------------

	public void copy(Capillary cap) {
		kymographIndex = cap.kymographIndex;
		kymographName = cap.kymographName;
		version = cap.version;
		roi = (ROI2D) cap.roi.getCopy();
		filenameTIFF = cap.filenameTIFF;

		stimulus = cap.stimulus;
		concentration = cap.concentration;
		cageSide = cap.cageSide;
		nFlies = cap.nFlies;
		cageID = cap.cageID;
		volume = cap.volume;
		pixels = cap.pixels;

		limitsOptions = cap.limitsOptions;

		ptsTop.copy(cap.ptsTop);
		ptsBottom.copy(cap.ptsBottom);
		ptsDerivative.copy(cap.ptsDerivative);
	}

	public String getKymographName() {
		return kymographName;
	}

	public void setKymographName(String name) {
		this.kymographName = name;
	}

	public ROI2D getRoi() {
		return roi;
	}

	public void setRoi(ROI2D roi) {
		this.roi = roi;
	}

	public void setRoiName(String name) {
		roi.setName(name);
	}

	public String getRoiName() {
		return roi.getName();
	}

	public String getLast2ofCapillaryName() {
		if (roi == null)
			return "missing";
		return roi.getName().substring(roi.getName().length() - 2);
	}

	public String getRoiNamePrefix() {
		return kymographPrefix;
	}

	public String getCapillarySide() {
		return roi.getName().substring(roi.getName().length() - 1);
	}

	public static String replace_LR_with_12(String name) {
		String newname = name;
		if (name.contains("R"))
			newname = name.replace("R", "2");
		else if (name.contains("L"))
			newname = name.replace("L", "1");
		return newname;
	}

	public int getCageIndexFromRoiName() {
		String name = roi.getName();
		if (!name.contains("line"))
			return -1;
		return Integer.valueOf(name.substring(4, 5));
	}

	public String getSideDescriptor(EnumXLSExportType xlsExportOption) {
		String value = null;
		cageSide = getCapillarySide();
		switch (xlsExportOption) {
		case DISTANCE:
		case ISALIVE:
			value = cageSide + "(L=R)";
			break;
		case TOPLEVELDELTA_LR:
		case TOPLEVEL_LR:
			if (cageSide.equals("L"))
				value = "sum";
			else
				value = "PI";
			break;
		case XYIMAGE:
		case XYTOPCAGE:
		case XYTIPCAPS:
			if (cageSide.equals("L"))
				value = "x";
			else
				value = "y";
			break;
		default:
			value = cageSide;
			break;
		}
		return value;
	}

	public String getCapillaryField(EnumXLSColumnHeader fieldEnumCode) {
		String stringValue = null;
		switch (fieldEnumCode) {
		case CAP_STIM:
			stringValue = stimulus;
			break;
		case CAP_CONC:
			stringValue = concentration;
			break;
		default:
			break;
		}
		return stringValue;
	}

	public void setCapillaryField(EnumXLSColumnHeader fieldEnumCode, String stringValue) {
		switch (fieldEnumCode) {
		case CAP_STIM:
			stimulus = stringValue;
			break;
		case CAP_CONC:
			concentration = stringValue;
			break;
		default:
			break;
		}
	}

	// -----------------------------------------

	public boolean isThereAnyMeasuresDone(EnumXLSExportType option) {
		boolean yes = false;
		switch (option) {
		case DERIVEDVALUES:
			yes = (ptsDerivative.isThereAnyMeasuresDone());
			break;
		case BOTTOMLEVEL:
			yes = ptsBottom.isThereAnyMeasuresDone();
			break;
		case TOPLEVEL:
		default:
			yes = ptsTop.isThereAnyMeasuresDone();
			break;
		}
		return yes;
	}

	public ArrayList<Double> getCapillaryMeasuresForXLSPass1(EnumXLSExportType option, long seriesBinMs,
			long outputBinMs) {
		ArrayList<Double> datai = null;
		switch (option) {
		case DERIVEDVALUES:
			datai = ptsDerivative.getMeasures(seriesBinMs, outputBinMs);
			break;
		case BOTTOMLEVEL:
			datai = ptsBottom.getMeasures(seriesBinMs, outputBinMs);
			break;
		case TOPLEVEL:
		case TOPRAW:
		case TOPLEVEL_LR:
		case TOPLEVELDELTA:
		case TOPLEVELDELTA_LR:
		default:
			datai = ptsTop.getMeasures(seriesBinMs, outputBinMs);
			break;
		}
		return datai;
	}

	public void cropMeasuresToNPoints(int npoints) {
		if (ptsTop.polylineLevel != null)
			ptsTop.cropToNPoints(npoints);
		if (ptsBottom.polylineLevel != null)
			ptsBottom.cropToNPoints(npoints);
		if (ptsDerivative.polylineLevel != null)
			ptsDerivative.cropToNPoints(npoints);
	}

	public void restoreClippedMeasures() {
		if (ptsTop.polylineLevel != null)
			ptsTop.restoreNPoints();
		if (ptsBottom.polylineLevel != null)
			ptsBottom.restoreNPoints();
		if (ptsDerivative.polylineLevel != null)
			ptsDerivative.restoreNPoints();
	}

	public int getLastMeasure(EnumXLSExportType option) {
		int lastMeasure = 0;
		switch (option) {
		case DERIVEDVALUES:
			lastMeasure = ptsDerivative.getLastMeasure();
			break;
		case BOTTOMLEVEL:
			lastMeasure = ptsBottom.getLastMeasure();
			break;
		case TOPLEVEL:
		default:
			lastMeasure = ptsTop.getLastMeasure();
			break;
		}
		return lastMeasure;
	}

	public int getLastDeltaMeasure(EnumXLSExportType option) {
		int lastMeasure = 0;
		switch (option) {
		case DERIVEDVALUES:
			lastMeasure = ptsDerivative.getLastDeltaMeasure();
			break;
		case BOTTOMLEVEL:
			lastMeasure = ptsBottom.getLastDeltaMeasure();
			break;
		case TOPLEVEL:
		default:
			lastMeasure = ptsTop.getLastDeltaMeasure();
			break;
		}
		return lastMeasure;
	}

	public int getT0Measure(EnumXLSExportType option) {
		int t0Measure = 0;
		switch (option) {
		case DERIVEDVALUES:
			t0Measure = ptsDerivative.getT0Measure();
			break;
		case BOTTOMLEVEL:
			t0Measure = ptsBottom.getT0Measure();
			break;
		case TOPLEVEL:
		default:
			t0Measure = ptsTop.getT0Measure();
			break;
		}
		return t0Measure;
	}

	public List<ROI2D> transferMeasuresToROIs() {
		List<ROI2D> listrois = new ArrayList<ROI2D>();
		listrois.add(measureToRoi(ptsTop));
		listrois.add(measureToRoi(ptsBottom));
		listrois.add(measureToRoi(ptsDerivative));
		return listrois;
	}

	private ROI2D measureToRoi(CapillaryLevel capLevel) {
		if (capLevel.polylineLevel == null || capLevel.polylineLevel.npoints == 0)
			return null;

		ROI2D roi = new ROI2DPolyLine(capLevel.polylineLevel);
		String name = kymographPrefix + "_" + capLevel.capName;
		roi.setName(name);
		roi.setT(kymographIndex);
		if (capLevel.capName.contains(ID_DERIVATIVE)) {
			roi.setColor(Color.yellow);
			roi.setStroke(1);
		}

		return roi;
	}

	public void transferROIsToMeasures(List<ROI> listRois) {
		ptsTop.transferROIsToMeasures(listRois);
		ptsBottom.transferROIsToMeasures(listRois);
		ptsDerivative.transferROIsToMeasures(listRois);
	}

	// -----------------------------------------------------------------------------

	public boolean loadFromXML_CapillaryOnly(Node node) {
		final Node nodeMeta = XMLUtil.getElement(node, ID_META);
		boolean flag = (nodeMeta != null);
		if (flag) {
			version = XMLUtil.getElementValue(nodeMeta, ID_VERSION, "0.0.0");
			kymographIndex = XMLUtil.getElementIntValue(nodeMeta, ID_INDEXIMAGE, kymographIndex);
			kymographName = XMLUtil.getElementValue(nodeMeta, ID_NAME, kymographName);
			filenameTIFF = XMLUtil.getElementValue(nodeMeta, ID_NAMETIFF, filenameTIFF);
			descriptionOK = XMLUtil.getElementBooleanValue(nodeMeta, ID_DESCOK, false);
			versionInfos = XMLUtil.getElementIntValue(nodeMeta, ID_VERSIONINFOS, 0);
			nFlies = XMLUtil.getElementIntValue(nodeMeta, ID_NFLIES, nFlies);
			cageID = XMLUtil.getElementIntValue(nodeMeta, ID_CAGENB, cageID);
			volume = XMLUtil.getElementDoubleValue(nodeMeta, ID_CAPVOLUME, Double.NaN);
			pixels = XMLUtil.getElementIntValue(nodeMeta, ID_CAPPIXELS, 5);
			stimulus = XMLUtil.getElementValue(nodeMeta, ID_STIML, ID_STIML);
			concentration = XMLUtil.getElementValue(nodeMeta, ID_CONCL, ID_CONCL);
			cageSide = XMLUtil.getElementValue(nodeMeta, ID_SIDE, ".");

			roi = ROI2DUtilities.loadFromXML_ROI(nodeMeta);
			limitsOptions.loadFromXML(nodeMeta);

			loadFromXML_intervals(node);
		}
		return flag;
	}

	private boolean loadFromXML_intervals(Node node) {
		roisForKymo.clear();
		final Node nodeMeta2 = XMLUtil.getElement(node, ID_INTERVALS);
		if (nodeMeta2 == null)
			return false;
		int nitems = XMLUtil.getElementIntValue(nodeMeta2, ID_NINTERVALS, 0);
		if (nitems > 0) {
			for (int i = 0; i < nitems; i++) {
				Node node_i = XMLUtil.setElement(nodeMeta2, ID_INTERVAL + i);
				ROI2DAlongT roiInterval = new ROI2DAlongT();
				roiInterval.loadFromXML(node_i);
				roisForKymo.add(roiInterval);

				if (i == 0) {
					roi = roisForKymo.get(0).getRoi_in();
				}
			}
		}
		return true;
	}

	public boolean loadFromXML_MeasuresOnly(Node node) {
		String header = getLast2ofCapillaryName() + "_";
		boolean result = ptsTop.loadCapillaryLimitFromXML(node, ID_TOPLEVEL, header) > 0;
		result |= ptsBottom.loadCapillaryLimitFromXML(node, ID_BOTTOMLEVEL, header) > 0;
		result |= ptsDerivative.loadCapillaryLimitFromXML(node, ID_DERIVATIVE, header) > 0;
		return result;
	}

	// -----------------------------------------------------------------------------

	public boolean saveToXML_CapillaryOnly(Node node) {
		final Node nodeMeta = XMLUtil.setElement(node, ID_META);
		if (nodeMeta == null)
			return false;
		if (version == null)
			version = ID_VERSIONNUM;
		XMLUtil.setElementValue(nodeMeta, ID_VERSION, version);
		XMLUtil.setElementIntValue(nodeMeta, ID_INDEXIMAGE, kymographIndex);
		XMLUtil.setElementValue(nodeMeta, ID_NAME, kymographName);
		if (filenameTIFF != null) {
			String filename = Paths.get(filenameTIFF).getFileName().toString();
			XMLUtil.setElementValue(nodeMeta, ID_NAMETIFF, filename);
		}
		XMLUtil.setElementBooleanValue(nodeMeta, ID_DESCOK, descriptionOK);
		XMLUtil.setElementIntValue(nodeMeta, ID_VERSIONINFOS, versionInfos);
		XMLUtil.setElementIntValue(nodeMeta, ID_NFLIES, nFlies);
		XMLUtil.setElementIntValue(nodeMeta, ID_CAGENB, cageID);
		XMLUtil.setElementDoubleValue(nodeMeta, ID_CAPVOLUME, volume);
		XMLUtil.setElementIntValue(nodeMeta, ID_CAPPIXELS, pixels);
		XMLUtil.setElementValue(nodeMeta, ID_STIML, stimulus);
		XMLUtil.setElementValue(nodeMeta, ID_SIDE, cageSide);
		XMLUtil.setElementValue(nodeMeta, ID_CONCL, concentration);

		ROI2DUtilities.saveToXML_ROI(nodeMeta, roi);

		boolean flag = saveToXML_intervals(node);
		return flag;
	}

	private boolean saveToXML_intervals(Node node) {
		final Node nodeMeta2 = XMLUtil.setElement(node, ID_INTERVALS);
		if (nodeMeta2 == null)
			return false;
		int nitems = roisForKymo.size();
		XMLUtil.setElementIntValue(nodeMeta2, ID_NINTERVALS, nitems);
		if (nitems > 0) {
			for (int i = 0; i < nitems; i++) {
				Node node_i = XMLUtil.setElement(nodeMeta2, ID_INTERVAL + i);
				roisForKymo.get(i).saveToXML(node_i);
			}
		}
		return true;
	}

	// -------------------------------------------

	public Point2D getCapillaryTipWithinROI2D(ROI2D roi2D) {
		Point2D pt = null;
		if (roi instanceof ROI2DPolyLine) {
			Polyline2D line = ((ROI2DPolyLine) roi).getPolyline2D();
			int last = line.npoints - 1;
			if (roi2D.contains(line.xpoints[0], line.ypoints[0]))
				pt = new Point2D.Double(line.xpoints[0], line.ypoints[0]);
			else if (roi2D.contains(line.xpoints[last], line.ypoints[last]))
				pt = new Point2D.Double(line.xpoints[last], line.ypoints[last]);
		} else if (roi instanceof ROI2DLine) {
			Line2D line = ((ROI2DLine) roi).getLine();
			if (roi2D.contains(line.getP1()))
				pt = line.getP1();
			else if (roi2D.contains(line.getP2()))
				pt = line.getP2();
		}
		return pt;
	}

	public Point2D getCapillaryROILowestPoint() {
		Point2D pt = null;
		if (roi instanceof ROI2DPolyLine) {
			Polyline2D line = ((ROI2DPolyLine) roi).getPolyline2D();
			int last = line.npoints - 1;
			if (line.ypoints[0] > line.ypoints[last])
				pt = new Point2D.Double(line.xpoints[0], line.ypoints[0]);
			else
				pt = new Point2D.Double(line.xpoints[last], line.ypoints[last]);
		} else if (roi instanceof ROI2DLine) {
			Line2D line = ((ROI2DLine) roi).getLine();
			if (line.getP1().getY() > line.getP2().getY())
				pt = line.getP1();
			else
				pt = line.getP2();
		}
		return pt;
	}

	public Point2D getCapillaryROIFirstPoint() {
		Point2D pt = null;
		if (roi instanceof ROI2DPolyLine) {
			Polyline2D line = ((ROI2DPolyLine) roi).getPolyline2D();
			pt = new Point2D.Double(line.xpoints[0], line.ypoints[0]);
		} else if (roi instanceof ROI2DLine) {
			Line2D line = ((ROI2DLine) roi).getLine();
			pt = line.getP1();
		}
		return pt;
	}

	public Point2D getCapillaryROILastPoint() {
		Point2D pt = null;
		if (roi instanceof ROI2DPolyLine) {
			Polyline2D line = ((ROI2DPolyLine) roi).getPolyline2D();
			int last = line.npoints - 1;
			pt = new Point2D.Double(line.xpoints[last], line.ypoints[last]);
		} else if (roi instanceof ROI2DLine) {
			Line2D line = ((ROI2DLine) roi).getLine();
			pt = line.getP2();
		}
		return pt;
	}

	public int getCapillaryROILength() {
		Point2D pt1 = getCapillaryROIFirstPoint();
		Point2D pt2 = getCapillaryROILastPoint();
		double npixels = Math.sqrt((pt2.getY() - pt1.getY()) * (pt2.getY() - pt1.getY())
				+ (pt2.getX() - pt1.getX()) * (pt2.getX() - pt1.getX()));
		return (int) npixels;
	}

	public void adjustToImageWidth(int imageWidth) {
		ptsTop.adjustToImageWidth(imageWidth);
		ptsBottom.adjustToImageWidth(imageWidth);
		ptsDerivative.adjustToImageWidth(imageWidth);
	}

	public void cropToImageWidth(int imageWidth) {
		ptsTop.cropToImageWidth(imageWidth);
		ptsBottom.cropToImageWidth(imageWidth);
		ptsDerivative.cropToImageWidth(imageWidth);
	}

	// --------------------------------------------

	public List<ROI2DAlongT> getROIsForKymo() {
		if (roisForKymo.size() < 1)
			initROI2DForKymoList();
		return roisForKymo;
	}

	public ROI2DAlongT getROI2DKymoAt(int i) {
		if (roisForKymo.size() < 1)
			initROI2DForKymoList();
		return roisForKymo.get(i);
	}

	public ROI2DAlongT getROI2DKymoAtIntervalT(long t) {
		if (roisForKymo.size() < 1)
			initROI2DForKymoList();

		ROI2DAlongT capRoi = null;
		for (ROI2DAlongT item : roisForKymo) {
			if (t < item.getT())
				break;
			capRoi = item;
		}
		return capRoi;
	}

	public void removeROI2DIntervalStartingAt(long start) {
		ROI2DAlongT itemFound = null;
		for (ROI2DAlongT item : roisForKymo) {
			if (start != item.getT())
				continue;
			itemFound = item;
		}
		if (itemFound != null)
			roisForKymo.remove(itemFound);
	}

	private void initROI2DForKymoList() {
		roisForKymo.add(new ROI2DAlongT(0, roi));
	}

	public void setVolumeAndPixels(double volume, int pixels) {
		this.volume = volume;
		this.pixels = pixels;
		descriptionOK = true;
	}

	// -----------------------------------------------------------------------------

	public String csvExportCapillarySubSectionHeader(String sep) {
		StringBuffer sbf = new StringBuffer();

		sbf.append("#" + sep + "CAPILLARIES,describe each capillary\n");
		List<String> row2 = Arrays.asList("prefix", "kymoIndex", "kymographName", "kymoFile", "cage", "nflies",
				"volume", "npixel", "stim", "conc", "side");
		sbf.append(String.join(sep, row2));
		sbf.append("\n");
		return sbf.toString();
	}

	public String csvExportCapillaryDescription(String sep) {
		StringBuffer sbf = new StringBuffer();
		if (kymographPrefix == null)
			kymographPrefix = getLast2ofCapillaryName();

		List<String> row = Arrays.asList(kymographPrefix, Integer.toString(kymographIndex), kymographName, filenameTIFF,
				Integer.toString(cageID), Integer.toString(nFlies), Double.toString(volume), Integer.toString(pixels),
				stimulus.replace(sep, "."), concentration.replace(sep, "."), cageSide.replace(sep, "."));
		sbf.append(String.join(sep, row));
		sbf.append("\n");
		return sbf.toString();
	}

	public String csvExportMeasureSectionHeader(EnumCapillaryMeasures measureType, String sep) {
		StringBuffer sbf = new StringBuffer();
		String explanation1 = "columns=" + sep + "name" + sep + "index" + sep + " npts" + sep + "..,.(xi;yi)\n";
		switch (measureType) {
		case TOPLEVEL:
			sbf.append("#" + sep + "TOPLEVEL" + sep + explanation1);
			break;
		case BOTTOMLEVEL:
			sbf.append("#" + sep + "BOTTOMLEVEL" + sep + explanation1);
			break;
		case TOPDERIVATIVE:
			sbf.append("#" + sep + "TOPDERIVATIVE" + sep + explanation1);
			break;
		default:
			sbf.append("#" + sep + "UNDEFINED" + sep + "------------\n");
			break;
		}
		return sbf.toString();
	}

	public String csvExportCapillaryData(EnumCapillaryMeasures measureType, String sep) {
		StringBuffer sbf = new StringBuffer();
		sbf.append(kymographPrefix + sep + kymographIndex + sep);

		switch (measureType) {
		case TOPLEVEL:
			ptsTop.cvsExportDataToRow(sbf, sep);
			break;
		case BOTTOMLEVEL:
			ptsBottom.cvsExportDataToRow(sbf, sep);
			break;
		case TOPDERIVATIVE:
			ptsDerivative.cvsExportDataToRow(sbf, sep);
			break;
		default:
			break;
		}
		sbf.append("\n");
		return sbf.toString();
	}

	// --------------------------------------------

	public void csvImportCapillaryDescription(String[] data) {
		int i = 0;
		kymographPrefix = data[i];
		i++;
		kymographIndex = Integer.valueOf(data[i]);
		i++;
		kymographName = data[i];
		i++;
		filenameTIFF = data[i];
		i++;
		cageID = Integer.valueOf(data[i]);
		i++;
		nFlies = Integer.valueOf(data[i]);
		i++;
		volume = Double.valueOf(data[i]);
		i++;
		pixels = Integer.valueOf(data[i]);
		i++;
		stimulus = data[i];
		i++;
		concentration = data[i];
		i++;
		cageSide = data[i];
	}

	public void csvImportCapillaryData(EnumCapillaryMeasures measureType, String[] data) {
		switch (measureType) {
		case TOPLEVEL:
			ptsTop.csvImportDataFromRow(data, 2);
			break;
		case BOTTOMLEVEL:
			ptsBottom.csvImportDataFromRow(data, 2);
			break;
		case TOPDERIVATIVE:
			ptsDerivative.csvImportDataFromRow(data, 2);
			break;
		default:
			break;
		}
	}

}
