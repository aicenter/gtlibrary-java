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


package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.convergence;

import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.GeneralDoubleOracle;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpanderWithMoveOrdering;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgame.RandomGameState;
import cz.agents.gtlibrary.domain.randomgame.SimRandomGameState;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import cz.agents.gtlibrary.utils.PermutationGenerator;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 4/24/13
 * Time: 1:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConvergenceExperiments {


    public static void main(String[] args) {
        int BF = 7;
        final int MAXSEED = 1000;
        RandomGameInfo.MAX_BF = BF;
        RandomGameInfo.MAX_DEPTH = 1;
        RandomGameInfo.UTILITY_CORRELATION = false;
        RandomGameInfo.BINARY_UTILITY = false;
        RandomGameInfo.MAX_UTILITY = 5;
        RandomGameInfo.FIXED_SIZE_BF = true;

        System.out.println("Running RG with BF:" + BF + " and MAXSEED:"+MAXSEED);

        int size = 1;
        for (int i=1; i<=BF; i++) size *= i;
        double[] suppSize = new double[MAXSEED * size];
        double[] genSize = new double[MAXSEED * size];
        for (int seed = 0; seed < MAXSEED; seed++) {
            if (seed % 10 == 0) {
                System.out.println("Seed reached " + seed);
            }
            RandomGameInfo.seed = seed;

//            boolean isFull = true;
            PermutationGenerator pg = new PermutationGenerator(BF);
//            while (pg.hasMore() && isFull) {
            int pgI = 0;
            while (pg.hasMore()) {
                RandomGameInfo.rnd = new HighQualityRandom(RandomGameInfo.seed);
                int[] order = pg.getNext();

//                System.out.println(Arrays.toString(order));

                GameState rootState = new SimRandomGameState();
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
                genSize[seed*size+pgI] = 1;
                suppSize[seed*size+pgI] = 1;
                for (Map<Sequence, Double> rps : result.values()) {
//                    isFull = isFull && (rps.keySet().size() == (BF + 1));
                    int nonZeroActions = 0;
                    for (double d : rps.values())
                        if (d > 0.000000001) nonZeroActions++;
                    genSize[seed*size+pgI] *= rps.size()/(double)BF;
                    suppSize[seed*size+pgI] *= nonZeroActions/(double)BF;
                }
//                if (isFull) {
//                    for (Map<Sequence, Double> rps : result.values()) {
//                        boolean allUsed = true;
//                        for (double d : rps.values()) {
//                            if (d == 0) allUsed = false;
//                        }
//                        if (allUsed)
//                            isFull = false; // this means that all actions are in the support
//                    }
//                }
                pgI++;
            }

//            if (isFull) {
//                System.out.println("Found! BF:" + BF + " SEED:"+seed);
//            }

        }
        System.out.println("Finished.");
        System.out.println("GenSizes:" + Arrays.toString(genSize));
        System.out.println("SuppSizes:" + Arrays.toString(suppSize));

    }
}
