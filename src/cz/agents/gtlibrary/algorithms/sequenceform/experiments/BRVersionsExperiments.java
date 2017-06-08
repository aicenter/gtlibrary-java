package cz.agents.gtlibrary.algorithms.sequenceform.experiments;

import cz.agents.gtlibrary.algorithms.sequenceform.SQFBestResponseAlgorithm;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.GeneralDoubleOracle;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.GameState;

import java.util.ArrayList;

/**
 * Created by Jakub Cerny on 05/06/2017.
 *
 * Experiments ensuring correctness of several versions of SQFBestResponse algorithm used in DO.
 * Compares calculated game values.
 */
public class BRVersionsExperiments{

    public static void main(String[] args) {
        runImprovedRandomTests();
    }

    private static void runImprovedRandomTests(){
        ArrayList<String> results = new ArrayList<>();
        final int MAX_DEPTH = 7;
        final int MAX_BF = 4;
        final int MIN_BF = 4;
        final int MAX_OBSERVATION = 5;
        final int MAX_SEED = 7;
        double gameValue;
        int instances = 0;
        int errors = 0;
        int trueSequences = 0;
        int falseSequences = 0;
        int p1seqs;
        int p2seqs;
        int smallerRG = 0;
        for (int depth = 4; depth < MAX_DEPTH; depth++){
            for (int minBf = 2; minBf < MIN_BF; minBf ++){
                for (int maxBF = minBf; maxBF < MAX_BF; maxBF++){
                    for (int obs = 2; obs < MAX_OBSERVATION; obs++){
                        for (int seed = 4; seed < MAX_SEED; seed++) {
                            instances++;
                            SQFBestResponseAlgorithm.useOriginalBRFormulation = true;
                            GameInfo gameInfo = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo();
                            ((cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo) gameInfo).MAX_DEPTH = depth;
                            ((cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo) gameInfo).MIN_BF = minBf;
                            ((cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo) gameInfo).MAX_BF = maxBF;
                            ((cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo) gameInfo).MAX_OBSERVATION = obs;
                            ((cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo) gameInfo).seed = seed;
                            gameInfo = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo();
                            GameState rootState = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState();
                            DoubleOracleConfig<DoubleOracleInformationSet> algConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
                            Expander<DoubleOracleInformationSet> expander = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander<DoubleOracleInformationSet>(algConfig);
                            GeneralDoubleOracle doefg = new GeneralDoubleOracle(rootState, expander, gameInfo, algConfig);
                            SQFBestResponseAlgorithm.useOriginalBRFormulation = true;
                            doefg.generate(null);
                            String out = "";
                            out += ((cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo) gameInfo).MAX_DEPTH + " ";
                            out += ((cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo) gameInfo).MIN_BF + " ";
                            out += ((cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo) gameInfo).MAX_BF + " ";
                            out += ((cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo) gameInfo).MAX_OBSERVATION + " ";
                            out += doefg.gameValue;
                            gameValue = doefg.gameValue;
                            p1seqs =  algConfig.getSequencesFor(gameInfo.getAllPlayers()[0]).size();
                            p2seqs = algConfig.getSequencesFor(gameInfo.getAllPlayers()[1]).size();
                            trueSequences += p1seqs + p2seqs;
//                        results.add(out);

                            SQFBestResponseAlgorithm.useOriginalBRFormulation = false;
                            algConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
                            expander = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander<DoubleOracleInformationSet>(algConfig);
                            doefg = new GeneralDoubleOracle(rootState, expander, gameInfo, algConfig);
                            SQFBestResponseAlgorithm.useOriginalBRFormulation = false;
                            doefg.generate(null);
                            if (Math.abs(gameValue - doefg.gameValue) > 1e-6) {
                                errors++;
                            }
                            falseSequences += algConfig.getSequencesFor(gameInfo.getAllPlayers()[0]).size() + algConfig.getSequencesFor(gameInfo.getAllPlayers()[1]).size();
//
                            if (p1seqs < algConfig.getSequencesFor(gameInfo.getAllPlayers()[0]).size() || p2seqs < algConfig.getSequencesFor(gameInfo.getAllPlayers()[1]).size()){
                                smallerRG ++;
                            }
                        }

                    }
                }
            }
        }
        System.out.println("Evaluation : " + errors + " errors on " + instances + " instances.");
        System.out.println("With negation sequences : " + trueSequences + ", without negation sequences : "+falseSequences);
        System.out.println("Smaller RG with original formulation on "+smallerRG+" instances.");
    }
}
