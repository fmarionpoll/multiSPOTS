package plugins.fmp.multiSPOTS.experiment;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;

import icy.common.exception.UnsupportedFormatException;
import icy.file.Loader;
import icy.file.Saver;
import icy.gui.frame.progress.ProgressFrame;
import icy.image.IcyBufferedImage;
import icy.roi.ROI2D;
import icy.sequence.MetaDataUtil;
import icy.type.DataType;
import icy.type.collection.array.Array1DUtil;
import loci.formats.FormatException;
import ome.xml.meta.OMEXMLMetadata;
import plugins.fmp.multiSPOTS.experiment.spots.SpotsArray;
import plugins.fmp.multiSPOTS.tools.Comparators;
import plugins.fmp.multiSPOTS.tools.ROI2D.ROI2DUtilities;
import plugins.kernel.roi.roi2d.ROI2DPolyLine;

public class SequenceKymos extends SequenceCamData {
	public boolean isRunning_loadImages = false;
	public int imageWidthMax = 0;
	public int imageHeightMax = 0;

	// -----------------------------------------------------

	public SequenceKymos() {
		super();
		status = EnumStatus.KYMOGRAPH;
	}

	public SequenceKymos(String name, IcyBufferedImage image) {
		super(name, image);
		status = EnumStatus.KYMOGRAPH;
	}

	public SequenceKymos(List<String> listNames) {
		super();
		setImagesList(convertLinexLRFileNames(listNames));
		status = EnumStatus.KYMOGRAPH;
	}

	// ----------------------------

	public void validateRois() {
		List<ROI2D> listRois = seq.getROI2Ds();
		int width = seq.getWidth();
		for (ROI2D roi : listRois) {
			if (!(roi instanceof ROI2DPolyLine))
				continue;
			// interpolate missing points if necessary
			if (roi.getName().contains("level")) {
				ROI2DUtilities.interpolateMissingPointsAlongXAxis((ROI2DPolyLine) roi, width);
				continue;
			}
			if (roi.getName().contains("derivative"))
				continue;
		}
		Collections.sort(listRois, new Comparators.ROI2D_Name_Comparator());
	}

//	public boolean transferKymosRoisToCapillaries_Measures(CapillariesArray capillaries) {
//		List<ROI> allRois = seq.getROIs();
//		if (allRois.size() < 1)
//			return false;
//		for (int kymo = 0; kymo < seq.getSizeT(); kymo++) {
//			List<ROI> roisAtT = new ArrayList<ROI>();
//			for (ROI roi : allRois) {
//				if (roi instanceof ROI2D && ((ROI2D) roi).getT() == kymo)
//					roisAtT.add(roi);
//			}
//			if (capillaries.capillariesList.size() <= kymo)
//				capillaries.capillariesList.add(new Capillary());
//			Capillary cap = capillaries.capillariesList.get(kymo);
//			cap.filenameTIFF = getFileNameFromImageList(kymo);
//			cap.kymographIndex = kymo;
//			cap.transferROIsToMeasures(roisAtT);
//		}
//		return true;
//	}
//
//	public void transferCapillariesMeasuresToKymos(CapillariesArray capillaries) {
//		List<ROI2D> seqRoisList = seq.getROI2Ds(false);
//		ROI2DUtilities.removeROIsMissingChar(seqRoisList, '_');
//
//		List<ROI2D> newRoisList = new ArrayList<ROI2D>();
//		int ncapillaries = capillaries.capillariesList.size();
//		for (int i = 0; i < ncapillaries; i++) {
//			List<ROI2D> listOfRois = capillaries.capillariesList.get(i).transferMeasuresToROIs();
//			newRoisList.addAll(listOfRois);
//		}
//		ROI2DUtilities.mergeROIsListNoDuplicate(seqRoisList, newRoisList, seq);
//		seq.removeAllROI();
//		seq.addROIs(seqRoisList, false);
//	}
//
//	public void saveKymosCurvesToCapillariesMeasures(Experiment exp) {
//		exp.seqSpotKymos.validateRois();
//		exp.seqSpotKymos.transferKymosRoisToCapillaries_Measures(exp.capillaries);
//		exp.save_CapillariesMeasures();
//	}

	// ----------------------------

	public List<ImageFileDescriptor> loadListOfPotentialKymographsFromSpots(String dir, SpotsArray spotsArray) {
		String directoryFull = dir + File.separator;
		int nspots = spotsArray.spotsList.size();
		List<ImageFileDescriptor> myListOfFiles = new ArrayList<ImageFileDescriptor>(nspots);
		for (int i = 0; i < nspots; i++) {
			ImageFileDescriptor temp = new ImageFileDescriptor();
			temp.fileName = directoryFull + spotsArray.spotsList.get(i).getRoiName() + ".tiff";
			myListOfFiles.add(temp);
		}
		return myListOfFiles;
	}

	// -------------------------

	public boolean loadImagesFromList(List<ImageFileDescriptor> kymoImagesDesc, boolean adjustImagesSize) {
		isRunning_loadImages = true;
		boolean flag = (kymoImagesDesc.size() > 0);
		if (!flag)
			return flag;

		if (adjustImagesSize)
			adjustImagesToMaxSize(kymoImagesDesc, getMaxSizeofTiffFiles(kymoImagesDesc));

		List<String> myList = new ArrayList<String>();
		for (ImageFileDescriptor prop : kymoImagesDesc) {
			if (prop.exists)
				myList.add(prop.fileName);
		}

		if (myList.size() > 0) {
			myList = ExperimentDirectories.keepOnlyAcceptedNames_List(myList, "tiff");
			setImagesList(convertLinexLRFileNames(myList));

			// threaded by default here
			loadImages();
			setParentDirectoryAsCSCamFileName(camImagesList.get(0));
			status = EnumStatus.KYMOGRAPH;
		}
		isRunning_loadImages = false;
		return flag;
	}

	protected void setParentDirectoryAsCSCamFileName(String filename) {
		if (filename != null) {
			Path path = Paths.get(filename);
			csCamFileName = path.getName(path.getNameCount() - 2).toString();
			seq.setName(csCamFileName);
		}
	}

	Rectangle getMaxSizeofTiffFiles(List<ImageFileDescriptor> files) {
		imageWidthMax = 0;
		imageHeightMax = 0;
		for (int i = 0; i < files.size(); i++) {
			ImageFileDescriptor fileProp = files.get(i);
			if (!fileProp.exists)
				continue;
			getImageDim(fileProp);
			if (fileProp.imageWidth > imageWidthMax)
				imageWidthMax = fileProp.imageWidth;
			if (fileProp.imageHeight > imageHeightMax)
				imageHeightMax = fileProp.imageHeight;
		}
		return new Rectangle(0, 0, imageWidthMax, imageHeightMax);
	}

	boolean getImageDim(final ImageFileDescriptor fileProp) {
		boolean flag = false;
		OMEXMLMetadata metaData = null;
		try {
			metaData = Loader.getOMEXMLMetaData(fileProp.fileName);
			fileProp.imageWidth = MetaDataUtil.getSizeX(metaData, 0);
			fileProp.imageHeight = MetaDataUtil.getSizeY(metaData, 0);
			flag = true;
		} catch (UnsupportedFormatException | IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return flag;
	}

	void adjustImagesToMaxSize(List<ImageFileDescriptor> files, Rectangle rect) {
		ProgressFrame progress = new ProgressFrame("Make kymographs the same width and height");
		progress.setLength(files.size());
		for (int i = 0; i < files.size(); i++) {
			ImageFileDescriptor fileProp = files.get(i);
			if (!fileProp.exists)
				continue;
			if (fileProp.imageWidth == rect.width && fileProp.imageHeight == rect.height)
				continue;

			progress.setMessage("adjust image " + fileProp.fileName);
			IcyBufferedImage ibufImage1 = null;
			try {
				ibufImage1 = Loader.loadImage(fileProp.fileName);
			} catch (UnsupportedFormatException | IOException | InterruptedException e1) {
				e1.printStackTrace();
			}

			IcyBufferedImage ibufImage2 = new IcyBufferedImage(imageWidthMax, imageHeightMax, ibufImage1.getSizeC(),
					ibufImage1.getDataType_());
			transferImage1To2(ibufImage1, ibufImage2);

			try {
				Saver.saveImage(ibufImage2, new File(fileProp.fileName), true);
			} catch (FormatException | IOException e) {
				e.printStackTrace();
			}

			progress.incPosition();
		}
		progress.close();
	}

	private static void transferImage1To2(IcyBufferedImage source, IcyBufferedImage result) {
		final int sizeY = source.getSizeY();
		final int endC = source.getSizeC();
		final int sourceSizeX = source.getSizeX();
		final int destSizeX = result.getSizeX();
		final DataType dataType = source.getDataType_();
		final boolean signed = dataType.isSigned();
		result.lockRaster();
		try {
			for (int ch = 0; ch < endC; ch++) {
				final Object src = source.getDataXY(ch);
				final Object dst = result.getDataXY(ch);
				int srcOffset = 0;
				int dstOffset = 0;
				for (int curY = 0; curY < sizeY; curY++) {
					Array1DUtil.arrayToArray(src, srcOffset, dst, dstOffset, sourceSizeX, signed);
					result.setDataXY(ch, dst);
					srcOffset += sourceSizeX;
					dstOffset += destSizeX;
				}
			}
		} finally {
			result.releaseRaster(true);
		}
		result.dataChanged();
	}

	// ----------------------------

	private List<String> convertLinexLRFileNames(List<String> myListOfFilesNames) {
		List<String> newList = new ArrayList<String>();
		for (String oldName : myListOfFilesNames)
			newList.add(convertLinexLRFileName(oldName));
		return newList;
	}

	private String convertLinexLRFileName(String oldName) {
		Path path = Paths.get(oldName);
		String test = path.getFileName().toString();
		String newName = oldName;
		if (test.contains("R.")) {
			newName = path.getParent() + File.separator + test.replace("R.", "2.");
			renameOldFile(oldName, newName);
		} else if (test.contains("L")) {
			newName = path.getParent() + File.separator + test.replace("L.", "1.");
			renameOldFile(oldName, newName);
		}
		return newName;
	}

	private void renameOldFile(String oldName, String newName) {
		File oldfile = new File(oldName);
		if (newName != null && oldfile.exists()) {
			try {
				FileUtils.moveFile(FileUtils.getFile(oldName), FileUtils.getFile(newName));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
