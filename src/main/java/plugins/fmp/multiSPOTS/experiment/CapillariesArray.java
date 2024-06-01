package plugins.fmp.multiSPOTS.experiment;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import icy.roi.ROI;
import icy.roi.ROI2D;
import icy.sequence.Sequence;
import icy.type.geom.Polygon2D;
import icy.util.XMLUtil;
import plugins.fmp.multiSPOTS.tools.Comparators;
import plugins.fmp.multiSPOTS.tools.ROI2D.ROI2DUtilities;
import plugins.fmp.multiSPOTS.tools.toExcel.EnumXLSExportType;
import plugins.kernel.roi.roi2d.ROI2DShape;

public class CapillariesArray {
	public CapillariesDescription capillariesDescription = new CapillariesDescription();
	public CapillariesDescription desc_old = new CapillariesDescription();
	public ArrayList<Capillary> capillariesList = new ArrayList<Capillary>();
	private KymoIntervals capillariesListTimeIntervals = null;

	private final static String ID_CAPILLARYTRACK = "capillaryTrack";
	private final static String ID_NCAPILLARIES = "N_capillaries";
	private final static String ID_LISTOFCAPILLARIES = "List_of_capillaries";
	private final static String ID_CAPILLARY_ = "capillary_";
	private final static String ID_MCCAPILLARIES_XML = "MCcapillaries.xml";

	// ---------------------------------

	public boolean load_Measures(String directory) {
		boolean flag = false;
		try {
			flag = csvLoadCapillaries_Measures(directory);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (!flag) {
			flag = xmlLoadCapillaries_Measures(directory);
		}
		return flag;
	}

	public boolean save_Measures(String directory) {
		if (directory == null)
			return false;

		csvSaveCapillariesMeasures_Data(directory);
		return true;
	}

	// ---------------------------------

	public String getXMLCapillariesName() {
		return ID_MCCAPILLARIES_XML;
	}

	public boolean xmlSaveCapillaries_Descriptors(String csFileName) {
		if (csFileName != null) {
			final Document doc = XMLUtil.createDocument(true);
			if (doc != null) {
				capillariesDescription.xmlSaveCapillaryDescription(doc);
				xmlSaveListOfCapillaries(doc);
				return XMLUtil.saveDocument(doc, csFileName);
			}
		}
		return false;
	}

	// ---------------------------------

	private boolean xmlSaveListOfCapillaries(Document doc) {
		Node node = XMLUtil.getElement(XMLUtil.getRootElement(doc), ID_CAPILLARYTRACK);
		if (node == null)
			return false;
		XMLUtil.setElementIntValue(node, "version", 2);
		Node nodecaps = XMLUtil.setElement(node, ID_LISTOFCAPILLARIES);
		XMLUtil.setElementIntValue(nodecaps, ID_NCAPILLARIES, capillariesList.size());
		int i = 0;
		Collections.sort(capillariesList);
		for (Capillary cap : capillariesList) {
			Node nodecapillary = XMLUtil.setElement(node, ID_CAPILLARY_ + i);
			cap.saveToXML_CapillaryOnly(nodecapillary);
			i++;
		}
		return true;
	}

	public boolean loadMCCapillaries_Descriptors(String csFileName) {
		boolean flag = false;
		if (csFileName == null)
			return flag;

		final Document doc = XMLUtil.loadDocument(csFileName);
		if (doc != null) {
			capillariesDescription.xmlLoadCapillaryDescription(doc);
			flag = xmlLoadCapillaries_Only_v1(doc);
		}
		return flag;
	}

	public boolean xmlLoadOldCapillaries_Only(String csFileName) {
		if (csFileName == null)
			return false;
		final Document doc = XMLUtil.loadDocument(csFileName);
		if (doc != null) {
			capillariesDescription.xmlLoadCapillaryDescription(doc);
			switch (capillariesDescription.version) {
			case 1: // old xml storage structure
				xmlLoadCapillaries_Only_v1(doc);
				break;
			case 0: // old-old xml storage structure
				xmlLoadCapillaries_v0(doc, csFileName);
				break;
			default:
				xmlLoadCapillaries_Only_v2(doc, csFileName);
				return false;
			}
			return true;
		}
		return false;
	}

	private boolean xmlLoadCapillaries_Measures(String directory) {
		boolean flag = false;
		int ncapillaries = capillariesList.size();
		for (int i = 0; i < ncapillaries; i++) {
			String csFile = directory + File.separator + capillariesList.get(i).getKymographName() + ".xml";
			final Document capdoc = XMLUtil.loadDocument(csFile);
			Node node = XMLUtil.getRootElement(capdoc, true);
			Capillary cap = capillariesList.get(i);
			cap.kymographIndex = i;
			flag |= cap.loadFromXML_MeasuresOnly(node);
		}
		return flag;
	}

	private void xmlLoadCapillaries_v0(Document doc, String csFileName) {
		List<ROI> listOfCapillaryROIs = ROI.loadROIsFromXML(XMLUtil.getRootElement(doc));
		capillariesList.clear();
		Path directorypath = Paths.get(csFileName).getParent();
		String directory = directorypath + File.separator;
		int t = 0;
		for (ROI roiCapillary : listOfCapillaryROIs) {
			xmlLoadIndividualCapillary_v0((ROI2DShape) roiCapillary, directory, t);
			t++;
		}
	}

	private void xmlLoadIndividualCapillary_v0(ROI2D roiCapillary, String directory, int t) {
		Capillary cap = new Capillary(roiCapillary);
		if (!isPresent(cap))
			capillariesList.add(cap);
		String csFile = directory + roiCapillary.getName() + ".xml";
		cap.kymographIndex = t;
		final Document dockymo = XMLUtil.loadDocument(csFile);
		if (dockymo != null) {
			NodeList nodeROISingle = dockymo.getElementsByTagName("roi");
			if (nodeROISingle.getLength() > 0) {
				List<ROI> rois = new ArrayList<ROI>();
				for (int i = 0; i < nodeROISingle.getLength(); i++) {
					Node element = nodeROISingle.item(i);
					ROI roi_i = ROI.createFromXML(element);
					if (roi_i != null)
						rois.add(roi_i);
				}
				cap.transferROIsToMeasures(rois);
			}
		}
	}

	private boolean xmlLoadCapillaries_Only_v1(Document doc) {
		Node node = XMLUtil.getElement(XMLUtil.getRootElement(doc), ID_CAPILLARYTRACK);
		if (node == null)
			return false;
		Node nodecaps = XMLUtil.getElement(node, ID_LISTOFCAPILLARIES);
		int nitems = XMLUtil.getElementIntValue(nodecaps, ID_NCAPILLARIES, 0);
		capillariesList = new ArrayList<Capillary>(nitems);
		for (int i = 0; i < nitems; i++) {
			Node nodecapillary = XMLUtil.getElement(node, ID_CAPILLARY_ + i);
			Capillary cap = new Capillary();
			cap.loadFromXML_CapillaryOnly(nodecapillary);
//			if (capillariesDescription.grouping == 2 && (cap.stimulus != null && cap.stimulus.equals(".."))) 
//			{
//				if (cap.getCapillarySide().equals("R")) 
//				{
//					cap.stimulus = capillariesDescription.stimulusR;
//					cap.concentration = capillariesDescription.concentrationR;
//				} 
//				else 
//				{
//					cap.stimulus = capillariesDescription.stimulusL;
//					cap.concentration = capillariesDescription.concentrationL;
//				}
//			}
			if (!isPresent(cap))
				capillariesList.add(cap);
		}
		return true;
	}

	private void xmlLoadCapillaries_Only_v2(Document doc, String csFileName) {
		xmlLoadCapillaries_Only_v1(doc);
		Path directorypath = Paths.get(csFileName).getParent();
		String directory = directorypath + File.separator;
		for (Capillary cap : capillariesList) {
			String csFile = directory + cap.getKymographName() + ".xml";
			final Document capdoc = XMLUtil.loadDocument(csFile);
			Node node = XMLUtil.getRootElement(capdoc, true);
			cap.loadFromXML_CapillaryOnly(node);
		}
	}

	// ---------------------------------

	public void copy(CapillariesArray cap) {
		capillariesDescription.copy(cap.capillariesDescription);
		capillariesList.clear();
		for (Capillary ccap : cap.capillariesList) {
			Capillary capi = new Capillary();
			capi.copy(ccap);
			capillariesList.add(capi);
		}
	}

	public boolean isPresent(Capillary capNew) {
		boolean flag = false;
		for (Capillary cap : capillariesList) {
			if (cap.getKymographName().contentEquals(capNew.getKymographName())) {
				flag = true;
				break;
			}
		}
		return flag;
	}

	public void mergeLists(CapillariesArray caplist) {
		for (Capillary capm : caplist.capillariesList) {
			if (!isPresent(capm))
				capillariesList.add(capm);
		}
	}

	public void adjustToImageWidth(int imageWidth) {
		for (Capillary cap : capillariesList)
			cap.adjustToImageWidth(imageWidth);
	}

	public void cropToImageWidth(int imageWidth) {
		for (Capillary cap : capillariesList)
			cap.cropToImageWidth(imageWidth);

	}

	public void transferDescriptionToCapillaries() {
		for (Capillary cap : capillariesList) {
			transferCapGroupCageIDToCapillary(cap);
			cap.setVolumeAndPixels(capillariesDescription.volume, capillariesDescription.pixels);
		}
	}

	private void transferCapGroupCageIDToCapillary(Capillary cap) {
		if (capillariesDescription.grouping != 2)
			return;
		String name = cap.getRoiName();
		String letter = name.substring(name.length() - 1);
		cap.cageSide = letter;
		if (letter.equals("R")) {
			String nameL = name.substring(0, name.length() - 1) + "L";
			Capillary cap0 = getCapillaryFromRoiName(nameL);
			if (cap0 != null) {
//				cap.capNFlies = cap0.capNFlies;
				cap.cageID = cap0.cageID;
			}
		}
	}

	public Capillary getCapillaryFromRoiName(String name) {
		Capillary capFound = null;
		for (Capillary cap : capillariesList) {
			if (cap.getRoiName().equals(name)) {
				capFound = cap;
				break;
			}
		}
		return capFound;
	}

	public Capillary getCapillaryFromKymographName(String name) {
		Capillary capFound = null;
		for (Capillary cap : capillariesList) {
			if (cap.getKymographName().equals(name)) {
				capFound = cap;
				break;
			}
		}
		return capFound;
	}

	public Capillary getCapillaryFromRoiNamePrefix(String name) {
		Capillary capFound = null;
		for (Capillary cap : capillariesList) {
			if (cap.getRoiNamePrefix().equals(name)) {
				capFound = cap;
				break;
			}
		}
		return capFound;
	}

	public void updateCapillariesFromSequence(Sequence seq) {
		List<ROI2D> listROISCap = ROI2DUtilities.getROIs2DContainingString("line", seq);
		Collections.sort(listROISCap, new Comparators.ROI2D_Name_Comparator());
		for (Capillary cap : capillariesList) {
			cap.valid = false;
			String capName = Capillary.replace_LR_with_12(cap.getRoiName());
			Iterator<ROI2D> iterator = listROISCap.iterator();
			while (iterator.hasNext()) {
				ROI2D roi = iterator.next();
				String roiName = Capillary.replace_LR_with_12(roi.getName());
				if (roiName.equals(capName)) {
					cap.setRoi((ROI2DShape) roi);
					cap.valid = true;
				}
				if (cap.valid) {
					iterator.remove();
					break;
				}
			}
		}
		Iterator<Capillary> iterator = capillariesList.iterator();
		while (iterator.hasNext()) {
			Capillary cap = iterator.next();
			if (!cap.valid)
				iterator.remove();
		}
		if (listROISCap.size() > 0) {
			for (ROI2D roi : listROISCap) {
				Capillary cap = new Capillary((ROI2DShape) roi);
				if (!isPresent(cap))
					capillariesList.add(cap);
			}
		}
		Collections.sort(capillariesList);
		return;
	}

	public void transferCapillaryRoiToSequence(Sequence seq) {
		for (Capillary cap : capillariesList)
			seq.addROI(cap.getRoi());
	}

	public void initCapillariesWith10Cages(int nflies, boolean optionZeroFlyFirstLastCapillary) {
		int capArraySize = capillariesList.size();
		for (int i = 0; i < capArraySize; i++) {
			Capillary cap = capillariesList.get(i);
			cap.nFlies = nflies;
			if (optionZeroFlyFirstLastCapillary && (i <= 1 || i >= capArraySize - 2))
				cap.nFlies = 0;
			cap.cageID = i / 2;
		}
	}

	public void initCapillariesWith6Cages(int nflies) {
		int capArraySize = capillariesList.size();
		for (int i = 0; i < capArraySize; i++) {
			Capillary cap = capillariesList.get(i);
			cap.nFlies = 1;
			if (i <= 1) {
				cap.nFlies = 0;
				cap.cageID = 0;
			} else if (i >= capArraySize - 2) {
				cap.nFlies = 0;
				cap.cageID = 5;
			} else {
				cap.nFlies = nflies;
				cap.cageID = 1 + (i - 2) / 4;
			}
		}
	}

	// -------------------------------------------------

	public KymoIntervals getKymoIntervalsFromCapillaries() {
		if (capillariesListTimeIntervals == null) {
			capillariesListTimeIntervals = new KymoIntervals();
			for (Capillary cap : capillariesList) {
				for (ROI2DAlongTime roiFK : cap.getROIsForKymo()) {
					Long[] interval = { roiFK.getT(), (long) -1 };
					capillariesListTimeIntervals.addIfNew(interval);
				}
			}
		}
		return capillariesListTimeIntervals;
	}

	public int addKymoROI2DInterval(long start) {
		Long[] interval = { start, (long) -1 };
		int item = capillariesListTimeIntervals.addIfNew(interval);

		for (Capillary cap : capillariesList) {
			List<ROI2DAlongTime> listROI2DForKymo = cap.getROIsForKymo();
			ROI2D roi = cap.getRoi();
			if (item > 0)
				roi = (ROI2D) listROI2DForKymo.get(item - 1).getRoi().getCopy();
			listROI2DForKymo.add(item, new ROI2DAlongTime(start, roi));
		}
		return item;
	}

	public void deleteKymoROI2DInterval(long start) {
		capillariesListTimeIntervals.deleteIntervalStartingAt(start);
		for (Capillary cap : capillariesList)
			cap.removeROI2DIntervalStartingAt(start);
	}

	public int findKymoROI2DIntervalStart(long intervalT) {
		return capillariesListTimeIntervals.findStartItem(intervalT);
	}

	public long getKymoROI2DIntervalsStartAt(int selectedItem) {
		return capillariesListTimeIntervals.get(selectedItem)[0];
	}

	public double getScalingFactorToPhysicalUnits(EnumXLSExportType xlsoption) {
		double scalingFactorToPhysicalUnits;
		switch (xlsoption) {
		case AUTOCORREL:
		case CROSSCORREL:
		case CROSSCORREL_LR:
			scalingFactorToPhysicalUnits = 1.;
			break;
		default:
			scalingFactorToPhysicalUnits = capillariesDescription.volume / capillariesDescription.pixels;
			break;
		}
		return scalingFactorToPhysicalUnits;
	}

	public Polygon2D get2DPolygonEnclosingCapillaries() {
		Rectangle outerRectangle = null;
		for (Capillary cap : capillariesList) {
			Rectangle rect = cap.getRoi().getBounds();
			if (outerRectangle == null) {
				outerRectangle = rect;
			} else
				outerRectangle.add(rect);
		}
		if (outerRectangle == null)
			return null;

		return new Polygon2D(outerRectangle);
	}

	public void deleteAllCapillaries() {
		capillariesList.clear();
	}

	// --------------------------------

	final String csvSep = ";";

	private boolean csvLoadCapillaries_Measures(String directory) throws Exception {
		String pathToCsv = directory + File.separator + "CapillariesMeasures.csv";
		File csvFile = new File(pathToCsv);
		if (!csvFile.isFile())
			return false;

		BufferedReader csvReader = new BufferedReader(new FileReader(pathToCsv));
		String row;
		String sep = ";";
		while ((row = csvReader.readLine()) != null) {
			if (row.charAt(0) == '#')
				sep = String.valueOf(row.charAt(1));

			String[] data = row.split(sep);
			if (data[0].equals("#")) {
				switch (data[1]) {
				case "DESCRIPTION":
					csvLoadDescription(csvReader, sep);
					break;
				case "CAPILLARIES":
					csvLoadCapillariesDescription(csvReader, sep);
					break;
				case "TOPLEVEL":
					csvLoadCapillariesMeasures(csvReader, EnumCapillaryMeasures.TOPLEVEL, sep);
					break;
				case "BOTTOMLEVEL":
					csvLoadCapillariesMeasures(csvReader, EnumCapillaryMeasures.BOTTOMLEVEL, sep);
					break;
				case "TOPDERIVATIVE":
					csvLoadCapillariesMeasures(csvReader, EnumCapillaryMeasures.TOPDERIVATIVE, sep);
					break;
				default:
					break;
				}
			}
		}
		csvReader.close();

		return true;
	}

	private String csvLoadCapillariesDescription(BufferedReader csvReader, String sep) {
		String row;
		try {
			row = csvReader.readLine();
			while ((row = csvReader.readLine()) != null) {
				String[] data = row.split(sep);
				if (data[0].equals("#"))
					return data[1];
				Capillary cap = getCapillaryFromKymographName(data[2]);
				if (cap == null)
					cap = new Capillary();
				cap.csvImportCapillaryDescription(data);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private String csvLoadDescription(BufferedReader csvReader, String sep) {
		String row;
		try {
			row = csvReader.readLine();
			row = csvReader.readLine();
			String[] data = row.split(sep);
			capillariesDescription.csvImportCapillariesDescriptionData(data);

			row = csvReader.readLine();
			data = row.split(sep);
			if (data[0].substring(0, Math.min(data[0].length(), 5)).equals("n cap")) {
				int ncapillaries = Integer.valueOf(data[1]);
				if (ncapillaries >= capillariesList.size())
					capillariesList.ensureCapacity(ncapillaries);
				else
					capillariesList.subList(ncapillaries, capillariesList.size()).clear();

				row = csvReader.readLine();
				data = row.split(sep);
			}
			if (data[0].equals("#")) {
				return data[1];
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private String csvLoadCapillariesMeasures(BufferedReader csvReader, EnumCapillaryMeasures measureType, String sep) {
		String row;
		try {
			while ((row = csvReader.readLine()) != null) {
				String[] data = row.split(sep);
				if (data[0].equals("#"))
					return data[1];

				Capillary cap = getCapillaryFromRoiNamePrefix(data[0]);
				if (cap == null)
					cap = new Capillary();
				cap.csvImportCapillaryData(measureType, data);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	// ---------------------------------

	private boolean csvSaveCapillariesMeasures_Data(String directory) {
		Path path = Paths.get(directory);
		if (!Files.exists(path))
			return false;

		try {
			FileWriter csvWriter = new FileWriter(directory + File.separator + "CapillariesMeasures.csv");

			csvSaveDescriptionSection(csvWriter);

			csvSaveMeasuresSection(csvWriter, EnumCapillaryMeasures.TOPLEVEL);
			csvSaveMeasuresSection(csvWriter, EnumCapillaryMeasures.BOTTOMLEVEL);
			csvSaveMeasuresSection(csvWriter, EnumCapillaryMeasures.TOPDERIVATIVE);
			csvWriter.flush();
			csvWriter.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;
	}

	private boolean csvSaveDescriptionSection(FileWriter csvWriter) {
		try {
			csvWriter.append(capillariesDescription.csvExportSectionHeader(csvSep));
			csvWriter.append(capillariesDescription.csvExportExperimentDescriptors(csvSep));
			csvWriter.append("n caps=" + csvSep + Integer.toString(capillariesList.size()) + "\n");
			csvWriter.append("#" + csvSep + "#\n");

			if (capillariesList.size() > 0) {
				csvWriter.append(capillariesList.get(0).csvExportCapillarySubSectionHeader(csvSep));
				for (Capillary cap : capillariesList)
					csvWriter.append(cap.csvExportCapillaryDescription(csvSep));
				csvWriter.append("#" + csvSep + "#\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;
	}

	private boolean csvSaveMeasuresSection(FileWriter csvWriter, EnumCapillaryMeasures measureType) {
		try {
			if (capillariesList.size() <= 1)
				return false;

			csvWriter.append(capillariesList.get(0).csvExportMeasureSectionHeader(measureType, csvSep));
			for (Capillary cap : capillariesList)
				csvWriter.append(cap.csvExportCapillaryData(measureType, csvSep));

			csvWriter.append("#" + csvSep + "#\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

}