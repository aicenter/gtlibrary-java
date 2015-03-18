package cz.agents.gtlibrary.algorithms.sequenceform.refinements.librarycom.resultparser;

import cz.agents.gtlibrary.interfaces.Sequence;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class SimplexResultParser implements ResultParser {

    private BufferedReader reader;
    private Map<Integer, Sequence> p1Sequences;
    private Map<Integer, Sequence> p2Sequences;

    private Map<Sequence, Double> p1RealPlan;
    private Map<Sequence, Double> p2RealPlan;

    private double gameValue;
    private long time;

    public SimplexResultParser(String fileName, Map<Integer, Sequence> p1Sequences, Map<Integer, Sequence> p2Sequences) {
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
        time = (long) Double.parseDouble(reader.readLine());

        loadStrategy(p1RealPlan, p1Sequences);
        loadStrategy(p2RealPlan, p2Sequences);
    }

    private void loadValueOfGame() throws IOException {
        reader.readLine();

        while(!reader.readLine().equals(""));
        StringTokenizer tokenizer = new StringTokenizer(reader.readLine(), ",");

        tokenizer.nextToken();

        String token = tokenizer.nextToken();
        gameValue = Double.parseDouble(token.substring(0, token.length() - 1));
    }

    public void loadStrategy(Map<Sequence, Double> realPlan, Map<Integer, Sequence> sequences) throws IOException {
        String line = null;
        int index = 0;

        while(!(line = reader.readLine()).equals("")) {
            StringTokenizer rowTokenizer = new StringTokenizer(line);

            rowTokenizer.nextToken();
            String strategy = rowTokenizer.nextToken();

            strategy = stripBrackets(strategy);
            realPlan.put(sequences.get(index++), Double.parseDouble(strategy));
        }
    }

    private String stripBrackets(String strategy) {
        strategy = strategy.substring(1);
        strategy = strategy.substring(0, strategy.length() - 1);
        return strategy;
    }

    public double getGameValue() {
        return gameValue;
    }

    @Override
    public long getTime() {
        return time;
    }
}
