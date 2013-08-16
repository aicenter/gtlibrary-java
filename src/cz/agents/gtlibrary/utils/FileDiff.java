package cz.agents.gtlibrary.utils;

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
