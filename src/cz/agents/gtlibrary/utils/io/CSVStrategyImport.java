package cz.agents.gtlibrary.utils.io;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleInformationSet;
import cz.agents.gtlibrary.domain.poker.kuhn.KPGameInfo;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerExpander;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerGameState;
import cz.agents.gtlibrary.interfaces.*;
import org.apache.wicket.util.file.File;

import java.io.*;
import java.util.*;

/**
 *
 * Reads a CSV file with multiple lines, where there is a single complete strategy for an extensive form game on each line.
 *
 *
 * Created by bosansky on 2/11/14.
 */
public class CSVStrategyImport {

    /**
     *
     *
     *
     * @param filename
     * @param root
     * @param expander
     * @return null in case of a problem
     */
    public static ArrayList<Map<Sequence, Double>> readStrategyFromCSVForEFG(String filename, GameState root, Expander<SequenceInformationSet> expander) {
        ArrayList<Map<Sequence, Double>> result = new ArrayList<Map<Sequence, Double>>();

        File file = new File(filename);
        BufferedReader reader;

        try {
            if (!file.exists()) {
                return null;
            }

            reader = new BufferedReader(new FileReader(file));
            ArrayList<Sequence> sequences = prepareStrategyFromEfg(root, expander);

            for (String x = reader.readLine(); x != null ; x = reader.readLine()) {
                Map<Sequence, Double> strategy = new HashMap<Sequence, Double>();
                String[] strategyInString = x.split(",");
                assert (sequences.size() + 1 == strategyInString.length);
                for (int i=1; i<strategyInString.length; i++) {
                    Double d = new Double(strategyInString[i]);
                    Sequence thisSequence = sequences.get(i-1);
                    if (thisSequence.size() > 1) {
                        Sequence predecessor = thisSequence.getSubSequence(0,thisSequence.size()-1);
                        d = d*strategy.get(predecessor);
                    }
                    strategy.put(thisSequence,d);
                }
                result.add(strategy);
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }


        return result;
    }

    private static ArrayList<Sequence> prepareStrategyFromEfg(GameState root, Expander<SequenceInformationSet> expander) {
        ArrayList<Sequence> result = new ArrayList<Sequence>();
        ArrayList<Sequence> p2 = new ArrayList<Sequence>();

        ArrayList<GameState> queue = new ArrayList<GameState>();
        Set<Sequence> visited = new HashSet<Sequence>();

        queue.add(root);

        while (queue.size() > 0) {
            GameState currentState = queue.remove(queue.size() - 1);

            if (currentState.isGameEnd()) {
                continue;
            }
            for (Action action : expander.getActions(currentState)) {
                GameState newState = currentState.performAction(action);
                Sequence p1Sequence = newState.getHistory().getSequenceOf(root.getAllPlayers()[0]);
                Sequence p2Sequence = newState.getHistory().getSequenceOf(root.getAllPlayers()[1]);
                if (!visited.contains(p1Sequence)) {
                    visited.add(p1Sequence);
                    if (p1Sequence.size() > 0) result.add(p1Sequence);
                }
                if (!visited.contains(p2Sequence)) {
                    visited.add(p2Sequence);
                    if (p2Sequence.size() > 0) p2.add(p2Sequence);
                }
                queue.add(newState);
            }
        }

        result.addAll(p2);

        return result;
    }

    public static void main(String[] args) {
        GameState rootState = new KuhnPokerGameState();
        GameInfo gameInfo = new KPGameInfo();
        DoubleOracleConfig<DoubleOracleInformationSet> algConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
        Expander expander = new KuhnPokerExpander<DoubleOracleInformationSet>(algConfig);

//        ArrayList<Sequence> sequences = prepareStrategyFromEfg(rootState,expander);
//        System.out.println(sequences);
        ArrayList<Map<Sequence, Double>> strategie = readStrategyFromCSVForEFG("/home/kail/tmp/KuhnQRE.csv", rootState, expander);
        System.out.println(strategie);
    }
}
