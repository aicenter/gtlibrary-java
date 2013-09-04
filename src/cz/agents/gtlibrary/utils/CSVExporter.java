package cz.agents.gtlibrary.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.wicket.util.file.File;

public class CSVExporter {
	
	public static void export(Exportable exportable, String fileName, String experimentName) {
		File file = new File(fileName);
		BufferedWriter writer;
		
		try {
			if (!file.exists()) {
				writer = new BufferedWriter(new FileWriter(file));
				writer.write(exportable.getColumnLabels());
			} else {
				writer = new BufferedWriter(new FileWriter(file, true));
			}
			writer.write(experimentName + exportable.getColumnValues());
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
