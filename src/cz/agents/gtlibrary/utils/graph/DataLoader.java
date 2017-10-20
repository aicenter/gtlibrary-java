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


package cz.agents.gtlibrary.utils.graph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;

import cz.agents.gtlibrary.domain.bpg.BPGGameInfo;

public class DataLoader implements Serializable {

	private double[][] originalGraphIncMatrix;
	private int nodesInOriginalGraph;
	public static String graphFile = BPGGameInfo.graphFile;

    @Deprecated
	public DataLoader() {
		readGraphFromFile(graphFile);
	}

	public DataLoader(String filename) {
		readGraphFromFile(filename);
	}

	private void readGraphFromFile(String fileName) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String thisLine = null;
			String nString = null;
			int n = 0;

			if ((nString = br.readLine()) != null) {
				n = Integer.parseInt(nString);
			}

			double[][] incidenceMatrix = new double[n][n];
			int i = 0;

			while ((thisLine = br.readLine()) != null) {
				String[] inc = thisLine.split(" ");
				for (int j = 0; j < inc.length; j++) {
					incidenceMatrix[i][j] = Integer.parseInt(inc[j]);
				}
				i++;
			}

			this.originalGraphIncMatrix = incidenceMatrix;
			this.nodesInOriginalGraph = n;
			br.close();
		} catch (IOException e) {
			System.err.println("Error: " + e);
		}
	}

	public double[][] getOriginalGraphIncMatrix() {
		return originalGraphIncMatrix;
	}

	public int getNodesInOriginalGraph() {
		return nodesInOriginalGraph;
	}
}
