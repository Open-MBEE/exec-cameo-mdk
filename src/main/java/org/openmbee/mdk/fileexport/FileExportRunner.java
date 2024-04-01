package org.openmbee.mdk.fileexport;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.options.EnvironmentOptions;
import com.nomagic.magicdraw.export.image.ImageExporter;
import com.nomagic.magicdraw.uml.Finder;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import org.openmbee.mdk.emf.BulkExport;
import org.openmbee.mdk.json.JacksonUtils;
import org.openmbee.mdk.options.MDKEnvironmentOptionsGroup;
import org.openmbee.mdk.options.MDKProjectOptionsGroup;
import org.openmbee.mdk.util.Pair;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 *         Based on code from ManualSyncRunner by igomes
 *
 */
public class FileExportRunner implements RunnableWithProgress {

	/**
	 * 
	 */
	private static final String MDZIP_EXTENSION = ".mdzip";
	private static final String MDXML_EXTENSION = ".mdxml";

	private final Collection<Element> rootElements;
	private final Project project;
	private final int depth;
	private final File outputFolder;

	private final ObjectWriter jsonWriter = JacksonUtils.getObjectMapper().writerWithDefaultPrettyPrinter();
	private final FileFormat fileFormat;
	private final Boolean exportDiagramMappings;

	public enum FileFormat {
		MODEL,
		TREE,
		FRAGMENT,
		ZIP;

		public String getExtension() {
			return "mdk" + this.name().toLowerCase();
		}
	}

	public FileExportRunner(Collection<Element> rootElements, Project project, int depth, FileFormat fileFormat,
			File outputFolder) {
		this.rootElements = rootElements;
		this.project = project;
		this.depth = depth;
		this.fileFormat = fileFormat;
		this.outputFolder = outputFolder;
		this.exportDiagramMappings = null;
	}
	
	public FileExportRunner(Collection<Element> rootElements, Project project, int depth, FileFormat fileFormat,
			File outputFolder, boolean exportDiagramMappings) {
		this.rootElements = rootElements;
		this.project = project;
		this.depth = depth;
		this.fileFormat = fileFormat;
		this.outputFolder = outputFolder;
		this.exportDiagramMappings = exportDiagramMappings;
	}
	

	@Override
	public void run(ProgressStatus progressStatus) {
		ContextExportLevel contextExportLevel = ContextExportLevel.None;
		ElementContextProvider contextProvider = new ElementContextProvider(contextExportLevel);

		String depthDescriptionText = depth == 0 ? "without contents"
				: (depth > 0 ? ("up to depth " + depth) : "and all contents");
		String exportDescriptionText = "Exporting to JSON " + rootElements.size() + " " + ((depth != 0) ? "root " : "")
				+ "element" + (rootElements.size() != 1 ? "s " : " ") + depthDescriptionText;
		Application.getInstance().getGUILog().log("[INFO] " + exportDescriptionText);
		Application.getInstance().getGUILog()
				.log(String.format("[INFO] Using Context export level: '%s'", contextExportLevel.toString()));

		Instant startInstant = Instant.now();

		progressStatus.setDescription(exportDescriptionText);
		progressStatus.setIndeterminate(false);
		progressStatus.setMax(rootElements.size());
		progressStatus.setCurrent(0);

		Set<String> usedNames = new HashSet<>();

		try {

			for (Element rootElement : rootElements) {

				String name = rootElement.getHumanName();
				File outputFile;
				if (FileFormat.ZIP == fileFormat)
					outputFile = getOutputFile(usedNames, rootElement, project.getName());
				else
					outputFile = getOutputFile(usedNames, rootElement, name);

				Application.getInstance().getGUILog().log(String.format("[INFO] Exporting element '%s' %s to file %s",
						name, depthDescriptionText, outputFile.getPath().toString()));

				Stream<Pair<Element, ObjectNode>> elements = BulkExport.exportElementsRecursively(project, rootElement,
						depth);

				if (fileFormat == FileFormat.ZIP) {
					Class<?>[] types = new Class<?>[] { Diagram.class };
					Collection<DiagramPresentationElement> diagramPresentationElements = Finder.byTypeRecursively()
							.find(project.getPrimaryModel(), types).stream().map(Diagram.class::cast)
							.map(project::getDiagram).collect(Collectors.toList());

					progressStatus.setMax(progressStatus.getMax() + diagramPresentationElements.size());

					try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
							ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream,
									java.nio.charset.StandardCharsets.UTF_8)) {

						exportDiagramsToZip(diagramPresentationElements, zipOutputStream, progressStatus);
						extractDiagramInfoToZip(diagramPresentationElements, zipOutputStream);
						writeDataToZip(progressStatus, contextProvider, zipOutputStream, elements);

					}
				} else {
					writeDataToFile(progressStatus, contextProvider, outputFile, elements);
				}

				progressStatus.increase();

				if (progressStatus.isCancel()) {
					Application.getInstance().getGUILog().log("[INFO] JSON export cancelled by user.");
					return;
				}
			}

			Instant endInstant = Instant.now();
			final Duration elapsed = Duration.between(startInstant, endInstant);
			Application.getInstance().getGUILog().log(
					String.format("[INFO] JSON export finished successfully in %d seconds.", elapsed.getSeconds()));

		} catch (IOException ioEx) {
			handleIOException(ioEx);
		}

	}

	private void writeDataToZip(ProgressStatus progressStatus, ElementContextProvider contextProvider,
			ZipOutputStream zipOutputStream, Stream<Pair<Element, ObjectNode>> elements) throws IOException {

		ZipEntry zipEntry = new ZipEntry("project." + FileFormat.MODEL.getExtension());
		zipOutputStream.putNextEntry(zipEntry);

		try (PrintWriter filePrinter = new PrintWriter(zipOutputStream)) {
			writeData(progressStatus, contextProvider, elements, filePrinter);
		}
	}

	private void writeDataToFile(ProgressStatus progressStatus, ElementContextProvider contextProvider, File outputFile,
			Stream<Pair<Element, ObjectNode>> elements) throws IOException {
		try (PrintWriter filePrinter = new PrintWriter(outputFile, "UTF-8")) {
			writeData(progressStatus, contextProvider, elements, filePrinter);
		}
	}

	private void writeData(ProgressStatus progressStatus, ElementContextProvider contextProvider,
			Stream<Pair<Element, ObjectNode>> elements, PrintWriter filePrinter) throws IOException {
		filePrinter.println("{");

		int writtenCount = writeElementsToFile(progressStatus, filePrinter, elements, contextProvider);

		Application.getInstance().getGUILog()
				.log(String.format("[INFO] Written %d element(s) to file.", writtenCount));

		filePrinter.println(",");

		writtenCount = writeContextElementsToFile(progressStatus, filePrinter, contextProvider);

		filePrinter.println("}");

		Application.getInstance().getGUILog()
				.log(String.format("[INFO] Written %d context element(s) to file.", writtenCount));
	}

	private File getOutputFile(Set<String> usedNames, Element rootElement, String name) {
		File outputFile;

		if ((FileFormat.MODEL == fileFormat)
				&& project.getPrimaryModel().equals(rootElement)) {
			outputFile = wholeProjectFileName(usedNames);
		} else {
			outputFile = getOutputFile(name, usedNames);
		}

		return outputFile;
	}

	private int writeContextElementsToFile(ProgressStatus progressStatus, PrintWriter filePrinter,
			ElementContextProvider contextProvider) throws IOException {
		prefixSequence(filePrinter, "contextElements");

		Iterator<ObjectNode> contextElements = contextProvider.getAggregatedContextElements().iterator();

		int count = 0;
		while (!progressStatus.isCancel() && contextElements.hasNext()) {
			if (count++ != 0) {
				midfixSequence(filePrinter);
			}

			final ObjectNode element = contextElements.next();

			String jsonString = jsonWriter.writeValueAsString(element);

			filePrinter.print(jsonString);
		}

		postfixSequence(filePrinter);

		return count;
	}

	private ObjectMapper setupJsonObjectMapper() {
		JsonFactory jsonFactory = new JsonFactory();
		jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);

		ObjectMapper mapper = new ObjectMapper(jsonFactory);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);

		return mapper;
	}

	private void extractDiagramInfoToZip(Collection<DiagramPresentationElement> diagrams,
			ZipOutputStream zipOutputStream) throws IOException {

		ObjectMapper mapper = setupJsonObjectMapper();

		extractPresentationElementsMapping(diagrams, zipOutputStream, mapper);

		ZipEntry zipEntry = new ZipEntry("diagramInfo.mdkdiagrams");
		zipOutputStream.putNextEntry(zipEntry);

		Map<String, Object> rootElement = new HashMap<String, Object>();
		Collection<DiagramBean> diagramToJsonList = new ArrayList<DiagramBean>();
		diagrams.forEach(diag -> {
			diagramToJsonList.add(new DiagramBean(diag));
		});
		rootElement.put("diagrams", diagramToJsonList);
		mapper.disable(SerializationFeature.WRAP_ROOT_VALUE);

		mapper.writeValue(zipOutputStream, rootElement);

	}

	private void extractPresentationElementsMapping(Collection<DiagramPresentationElement> diagrams,
			ZipOutputStream zipOutputStream, ObjectMapper mapper)
			throws IOException, JsonGenerationException, JsonMappingException {

		if (MDKEnvironmentOptionsGroup.getInstance().isExportMdkzipDiagramElementsMappingEnabled()
				&& this.exportDiagramMappings == null) {

			writePresentationElementsToZip(diagrams, zipOutputStream, mapper);
		} else if (this.exportDiagramMappings) {
			writePresentationElementsToZip(diagrams, zipOutputStream, mapper);
		}
	}

	private void writePresentationElementsToZip(Collection<DiagramPresentationElement> diagrams,
			ZipOutputStream zipOutputStream, ObjectMapper mapper)
			throws IOException, JsonGenerationException, JsonMappingException {
		for (DiagramPresentationElement diag : diagrams) {

			ZipEntry diagramMappingEntry = new ZipEntry("diagrams/" + getDiagramExportName(diag) + ".mdkmapping");
			zipOutputStream.putNextEntry(diagramMappingEntry);
			mapper.writeValue(zipOutputStream, new DiagramMapping(diag));
}
	}

	private void exportDiagramsToZip(Collection<DiagramPresentationElement> diagrams, ZipOutputStream zipOutputStream,
			ProgressStatus progressStatus) throws FileNotFoundException, IOException {

		Iterator<DiagramPresentationElement> diagramIterator = diagrams.iterator();
		Application application = Application.getInstance();
		EnvironmentOptions options = application.getEnvironmentOptions();
		
		boolean useSvg = options.getGeneralOptions().isUseSVGTextTag();
		options.getGeneralOptions().setUseSVGTextTag(true);
		try {
			while (!progressStatus.isCancel() && diagramIterator.hasNext()) {
				progressStatus.increase();
				DiagramPresentationElement currentDiagram = diagramIterator.next();

				String diagramExportName = getDiagramExportName(currentDiagram);
				File diagramFile = new File(diagramExportName);
				diagramFile.createNewFile();

				ImageExporter.export(currentDiagram, ImageExporter.SVG, diagramFile);

				ZipEntry zipEntry = new ZipEntry("diagrams/" + diagramFile.getName() + ".svg");
				zipOutputStream.putNextEntry(zipEntry);

				try (FileInputStream fileInputStream = new FileInputStream(diagramFile)) {
					byte[] bytes = new byte[1024];
					int length;
					while ((length = fileInputStream.read(bytes)) >= 0) {
						zipOutputStream.write(bytes, 0, length);
					}
				}

				diagramFile.delete();
			}
		} finally {
			options.getGeneralOptions().setUseSVGTextTag(useSvg);
		}
	}

	private String getDiagramExportName(DiagramPresentationElement currentDiagram) {

		return currentDiagram.getName().replaceAll("[^\\p{IsAlphabetic}\\p{Digit}-_]", "_") + "_"
				+ currentDiagram.getID();
	}

	private int writeElementsToFile(ProgressStatus progressStatus, PrintWriter targetFileWriter,
			Stream<Pair<Element, ObjectNode>> exportedElementsStream, ElementContextProvider elementContextProvider)
			throws IOException {
		prefixSequence(targetFileWriter, "elements");

		Iterator<Pair<Element, ObjectNode>> exportedElementsIterator = exportedElementsStream.iterator();

		int count = 0;
		while (!progressStatus.isCancel() && exportedElementsIterator.hasNext()) {
			if (count++ != 0) {
				midfixSequence(targetFileWriter);
			}

			final Pair<Element, ObjectNode> exportedElement = exportedElementsIterator.next();

			elementContextProvider.registerExportedElement(exportedElement.getKey());
			String jsonString = jsonWriter.writeValueAsString(exportedElement.getValue());

			targetFileWriter.print(jsonString);
		}

		postfixSequence(targetFileWriter);

		return count;
	}

	private void prefixSequence(PrintWriter filePrinter, String arrayName) {
		filePrinter.println("\"" + arrayName + "\" : [");
	}

	private void postfixSequence(PrintWriter filePrinter) {
		filePrinter.println("\n]");
	}

	private void midfixSequence(PrintWriter filePrinter) {
		filePrinter.println(",");
	}

	private File getOutputFile(String originalName, Set<String> usedNames) {
		String rootName = originalName.replaceAll("[^\\p{IsAlphabetic}\\p{Digit}-_]", "_");
		if (rootName.isEmpty())
			rootName = "export";
		String name = rootName;
		int postFix = 0;
		while (!usedNames.add(name)) {
			name = String.format("%s_%04d", rootName, postFix++);
		}

		return new File(outputFolder, name + "." + fileFormat.getExtension());
	}

	public File wholeProjectFileName(Set<String> usedNames) {
		if (project.isRemote()) {
			return getOutputFile(project.getName(), usedNames);
		} else {
			String projectName = project.getFileName();
			if (projectName.endsWith(MDZIP_EXTENSION)) {
				projectName = projectName.substring(0, projectName.length() - MDZIP_EXTENSION.length());
			} else if (projectName.endsWith(MDXML_EXTENSION)) {
				projectName = projectName.substring(0, projectName.length() - MDXML_EXTENSION.length());
			}
			String outputFileName = projectName + "." + fileFormat.getExtension();
			File outputFile = new File(outputFileName);
			return outputFile;
		}
	}

	private void handleIOException(Throwable ioEx) {
		Application.getInstance().getGUILog()
				.log("[ERROR] An error occurred while exporting model as JSON, aborting. Reason: " + ioEx.getMessage());
		ioEx.printStackTrace();
	}
	
	private class DiagramBean {
		@JsonProperty("id")
		private String id;
		@JsonProperty("name")
		private String name;
		@JsonProperty("parent_id")
		private String parentId;
		@JsonProperty("type")
		private String type;

		public DiagramBean(DiagramPresentationElement diagram) {

			this.id = diagram.getID();
			this.name = diagram.getHumanName();

			if (diagram.getDiagram().getContext() == null) {

				this.parentId = diagram.getDiagram().getOwner().getID();
			} else {

				this.parentId = diagram.getDiagram().getContext().getID();
			}
			this.type = diagram.getHumanType();

		}
	}
	
	@JsonRootName("diagram")
	private class DiagramMapping {
		@JsonProperty("id")
		private String diagramID;
		
		@JsonProperty("representedElementsRecursively")
		private Collection<String> representedElementsRecursively;

		public DiagramMapping(DiagramPresentationElement diag) {
			this.diagramID = diag.getID();
			this.representedElementsRecursively = diag.collectPresentationElementsRecursively().stream().filter(elem-> elem.getElement()!=null)
					.map(elem -> elem.getElement().getID()).collect(Collectors.toList());
		}
		
	}
	
	
	
	
}
