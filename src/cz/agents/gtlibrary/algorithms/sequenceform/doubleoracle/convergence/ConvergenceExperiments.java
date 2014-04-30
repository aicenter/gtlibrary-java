package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.convergence;

import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.GeneralDoubleOracle;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpanderWithMoveOrdering;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgame.RandomGameState;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import cz.agents.gtlibrary.utils.PermutationGenerator;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 4/24/13
 * Time: 1:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConvergenceExperiments {


    public static void main(String[] args) {
        int BF = 4;
        final long MAXSEED = 100000;
        RandomGameInfo.MAX_BF = BF;
        RandomGameInfo.UTILITY_CORRELATION = false;
        RandomGameInfo.BINARY_UTILITY = true;
        RandomGameInfo.MAX_UTILITY = 5;

        System.out.println("Running RG with BF:" + BF + " and MAXSEED:"+MAXSEED);
        for (long seed = 0; seed < MAXSEED; seed++) {
            if (seed % 1000 == 0) {
                System.out.println("Seed reached " + seed);
            }
            RandomGameInfo.seed = seed;

            boolean isFull = true;
            PermutationGenerator pg = new PermutationGenerator(BF);
            while (pg.hasMore() && isFull) {
                RandomGameInfo.rnd = new HighQualityRandom(RandomGameInfo.seed);
                int[] order = pg.getNext();

//                System.out.println(Arrays.toString(order));

                GameState rootState = new RandomGameState();
                GameInfo gameInfo = new RandomGameInfo();
                DoubleOracleConfig<DoubleOracleInformationSet> algConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
                Expander<DoubleOracleInformationSet> expander = new RandomGameExpanderWithMoveOrdering<DoubleOracleInformationSet>(algConfig,order);
                GeneralDoubleOracle doefg = new GeneralDoubleOracle(rootState,  expander, gameInfo, algConfig);

                doefg.setDebugOutput(new PrintStream(new OutputStream() {
                                    public void write(int b) {
                                        //DO NOTHING
                                    }
                                }));

                Map<Player, Map<Sequence, Double>> result = doefg.generate(null);
                for (Map<Sequence, Double> rps : result.values()) {
                    isFull = isFull && (rps.keySet().size() == (BF + 1));
                }
                if (isFull) {
                    for (Map<Sequence, Double> rps : result.values()) {
                        boolean allUsed = true;
                        for (double d : rps.values()) {
                            if (d == 0) allUsed = false;
                        }
                        if (allUsed)
                            isFull = false; // this means that all actions are in the support
                    }
                }
            }

            if (isFull) {
                System.out.println("Found! BF:" + BF + " SEED:"+seed);
            }

        }
        System.out.println("Finished.");
    }
}
