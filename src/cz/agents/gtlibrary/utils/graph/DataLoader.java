package cz.agents.gtlibrary.utils.graph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import cz.agents.gtlibrary.domain.bpg.BPGGameInfo;

public class DataLoader {

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
