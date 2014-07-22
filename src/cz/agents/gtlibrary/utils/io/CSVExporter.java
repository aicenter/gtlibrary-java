/*
Copyright 2014 Faculty of Electrical Engineering at CTU in Prague

This file is part of Game Theoretic Library.

Game Theoretic Library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Game Theoretic Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.*/


package cz.agents.gtlibrary.utils.io;

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
