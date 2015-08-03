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


package cz.agents.gtlibrary.algorithms.sequenceform.refinements.librarycom.resultparser;

import cz.agents.gtlibrary.interfaces.Sequence;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class LemkeResultParser implements ResultParser {
	
	private BufferedReader reader;
	private Map<Integer, Sequence> p1Sequences;
	private Map<Integer, Sequence> p2Sequences;
	
	private Map<Sequence, Double> p1RealPlan;
	private Map<Sequence, Double> p2RealPlan;
	
	private double gameValue;
	
	public LemkeResultParser(String fileName, Map<Integer, Sequence> p1Sequences, Map<Integer, Sequence> p2Sequences) {
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
		p1RealPlan = new HashMap<>();
		p2RealPlan = new HashMap<>();
//		reader.readLine();
//		reader.readLine();
		
//		String firstRPLine = reader.readLine();
		
//		if(firstRPLine == null)
//			return;
//		StringTokenizer sizeTokenizer = new StringTokenizer(firstRPLine);
		
//		sizeTokenizer.nextToken();
//		int p1SequenceCount = Integer.parseInt(sizeTokenizer.nextToken());
//		sizeTokenizer.nextToken();
//		int p2SequenceCount = Integer.parseInt(sizeTokenizer.nextToken());
		
		loadP1Strategy();
        reader.readLine();
		loadP2Strategy();
//		loadValueOfGame();
	}

	private void loadValueOfGame() throws IOException {
		reader.readLine();
		
		while(!reader.readLine().equals(""));
		StringTokenizer tokenizer = new StringTokenizer(reader.readLine(), ",");
		
		tokenizer.nextToken();
		
		String token = tokenizer.nextToken();
		gameValue = Double.parseDouble(token.substring(0, token.length() - 1));
	}

	public void loadP2Strategy() throws IOException {
        for (int i = 0; i < p2Sequences.size(); i++) {
			StringTokenizer rowTokenizer = new StringTokenizer(reader.readLine());
			
			rowTokenizer.nextToken();
			String strategy = rowTokenizer.nextToken();
			StringTokenizer strategyTokenizer = new StringTokenizer(strategy.substring(1, strategy.length() - 1), ",");
			
			strategyTokenizer.nextToken();
			p2RealPlan.put(p2Sequences.get(i), Double.parseDouble(strategyTokenizer.nextToken()));
		}
	}

	public void loadP1Strategy() throws IOException {
        for (int i = 0; i < p1Sequences.size(); i++) {
			StringTokenizer rowTokenizer = new StringTokenizer(reader.readLine());
			
			rowTokenizer.nextToken();
			String strategy = rowTokenizer.nextToken();
			StringTokenizer strategyTokenizer = new StringTokenizer(strategy.substring(1, strategy.length() - 1), ",");
			
			strategyTokenizer.nextToken();
			p1RealPlan.put(p1Sequences.get(i), Double.parseDouble(strategyTokenizer.nextToken()));
		}
	}
	
	public double getGameValue() {
		return gameValue;
	}

    @Override
    public long getTime() {
        return -1;
    }

}
