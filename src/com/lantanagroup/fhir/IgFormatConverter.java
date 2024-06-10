package com.lantanagroup.fhir;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.nio.file.Path;
import java.nio.file.Files;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.context.ConfigurationException;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.parser.XmlParser;


public class IgFormatConverter {
	

	private static final String IN_FOLDER = "inFolder";
	private static final String OUT_FOLDER = "outFolder";
	private static final String FORMAT = "format";
	
	private File inputFolder;
	private File outputFolder;
	private final String outputFormat;
	private final FhirContext ctx = FhirContext.forR4();
	private XmlParser xmlParser = (XmlParser) ctx.newXmlParser();
	private JsonParser jsonParser = (JsonParser) ctx.newJsonParser();
	
	public static void main(String[] args) {
		System.out.println("Launching IgFormatConverter");
		Options options = new Options();
		options.addOption(IN_FOLDER, true, "Input file");
		options.addOption(OUT_FOLDER, true, "Output folder");
		options.addOption(FORMAT, true, "Format");
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd;
		try {
			cmd = parser.parse( options, args);
			File tempFolder;

			IgFormatConverter ifc = new IgFormatConverter(
						new File(cmd.getOptionValue(IN_FOLDER)), 
						new File(cmd.getOptionValue(OUT_FOLDER)),
						cmd.getOptionValue(FORMAT)
					);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public IgFormatConverter(File inputFolder, File outputFolder, String outputFormat) {
		this.inputFolder = inputFolder;
		this.outputFolder = outputFolder;
		this.outputFormat = outputFormat;
		processFolder(inputFolder, outputFolder);
	}
	
	private void processFolder(File inputFolder, File outputFolder) {
		for (File f : inputFolder.listFiles()) processFile(f,outputFolder);
	}
	
	private boolean validInputFolder(File folder) {
		File parent = folder.getParentFile();
		if (parent.equals(inputFolder) && folder.getName().equals("input") == false) {
			System.out.println("Ignoring folder " + folder.getName());
			return false;
		}
		return true;
	}

	private void processFile(File f, File outputFolder)  {
		System.out.println("Processing " + f.getName());
		if (f.isDirectory() ) {
			if (validInputFolder(f)) {
				File subdirectory = new File(outputFolder,f.getName());
				subdirectory.mkdirs();
				processFolder(f,subdirectory);
			}
		} else {
			try {
				String oldExtension = com.google.common.io.Files.getFileExtension(f.getName()).toLowerCase();
				String newExtension = null;
				IParser inParser = null;
				IParser outParser = null;
				if (oldExtension.equals("json")) {
					newExtension = "xml";
					inParser = this.jsonParser;
					outParser = this.xmlParser;
				} else if (oldExtension.equals("xml")) {
					newExtension = "json";
					inParser = this.xmlParser;
					outParser = this.jsonParser;
				}
				if (newExtension == null) Files.copy(f.toPath(), new File(outputFolder,f.getName()).toPath());
				else convert(f,outputFolder,inParser,outParser,newExtension);
				
			} catch (Exception e) {
				System.err.println(e.toString());
			}
		}
	}

	private void convert(File f, File outputFolder, IParser inParser, IParser outParser, String newExtension) throws ConfigurationException, DataFormatException, IOException {
		IBaseResource res = inParser.parseResource(new FileReader(f));
		String baseName = com.google.common.io.Files.getNameWithoutExtension(f.getName());
		String newName = baseName + "." + newExtension;
		File newFile = new File(outputFolder, newName);
		outParser.encodeToWriter(res, new FileWriter(newFile));
	}


	

}
