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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FileDiff {
	
	public static void main(String[] args) throws IOException {
		BufferedReader reader1 = new BufferedReader(new FileReader(new File("file1.txt")));
		BufferedReader reader2 = new BufferedReader(new FileReader(new File("file2.txt")));
		String line1 = reader1.readLine();
		String line2 = reader2.readLine();
		int index = 1;
		
		while(line1.equals(line2)) {
			line1 = reader1.readLine();
			line2 = reader2.readLine();
			index++;
		}
		System.out.println("Line index: " + index);
		System.out.println("line1: " + line1);
		System.out.println("line2: " + line2);
		reader1.close();
		reader2.close();
	}

}
