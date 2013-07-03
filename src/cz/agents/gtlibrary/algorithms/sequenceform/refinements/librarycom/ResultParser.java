package cz.agents.gtlibrary.algorithms.sequenceform.refinements.librarycom;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import cz.agents.gtlibrary.interfaces.Sequence;

public class ResultParser {
	
	private BufferedReader reader;
	private Map<Integer, Sequence> p1Sequences;
	private Map<Integer, Sequence> p2Sequences;
	
	private Map<Sequence, Double> p1RealPlan;
	private Map<Sequence, Double> p2RealPlan;
	
	public ResultParser(String fileName, Map<Integer, Sequence> p1Sequences, Map<Integer, Sequence> p2Sequences) {
		try {
			reader = new BufferedReader(new FileReader(new File(fileName)));
		} catch (FileNotFoundException e) {
			System.err.println("Error during loading of result file...");
		}
		this.p1Sequences = p1Sequences;
		this.p2Sequences = p2Sequences;
	}
	
	public Map<Sequence, Double> getP1RealizationPlan() {
		if(p1RealPlan == null)
			try {
				parse();
			} catch (IOException e) {
				System.err.println("Error while parsing result file...");
			}
		return p1RealPlan;
	}

	public Map<Sequence, Double> getP2RealizationPlan() {
		if(p2RealPlan == null) 
			try {
				parse();
			} catch (IOException e) {
				System.err.println("Error while parsing result file...");
			}
		return p2RealPlan;
	}
	
	private void parse() throws IOException {
		p1RealPlan = new HashMap<Sequence, Double>();
		p2RealPlan = new HashMap<Sequence, Double>();
		reader.readLine();
		reader.readLine();
		
		StringTokenizer sizeTokenizer = new StringTokenizer(reader.readLine());
		
		sizeTokenizer.nextToken();
		int p1SequenceCount = Integer.parseInt(sizeTokenizer.nextToken());
		sizeTokenizer.nextToken();
		int p2SequenceCount = Integer.parseInt(sizeTokenizer.nextToken());
		
		loadP1Strategy(p1SequenceCount);
		loadP2Strategy(p2SequenceCount);
	}

	public void loadP2Strategy(int p2SequenceCount) throws IOException {
		reader.readLine();
		for (int i = 0; i < p2SequenceCount; i++) {
			StringTokenizer rowTokenizer = new StringTokenizer(reader.readLine());
			
			rowTokenizer.nextToken();
			String strategy = rowTokenizer.nextToken();
			StringTokenizer strategyTokenizer = new StringTokenizer(strategy.substring(1, strategy.length() - 1), ",");
			
			strategyTokenizer.nextToken();
			p2RealPlan.put(p2Sequences.get(i), Double.parseDouble(strategyTokenizer.nextToken()));			
		}
	}

	public void loadP1Strategy(int p1SequenceCount) throws IOException {
		reader.readLine();
		for (int i = 0; i < p1SequenceCount; i++) {
			StringTokenizer rowTokenizer = new StringTokenizer(reader.readLine());
			
			rowTokenizer.nextToken();
			String strategy = rowTokenizer.nextToken();
			StringTokenizer strategyTokenizer = new StringTokenizer(strategy.substring(1, strategy.length() - 1), ",");
			
			strategyTokenizer.nextToken();
			p1RealPlan.put(p1Sequences.get(i), Double.parseDouble(strategyTokenizer.nextToken()));			
		}
	}

}
