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

import cz.agents.gtlibrary.algorithms.sequenceform.FullSequenceEFG;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.GeneralDoubleOracle;
import cz.agents.gtlibrary.domain.randomgame.*;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import cz.agents.gtlibrary.utils.PermutationGenerator;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 4/24/13
 * Time: 1:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class PerformanceRateToSupportSize {


    public static void main(String[] args) {
        final int MAXSEED = 100;
        final int LARGEST_TREE = 100000;
        ArrayList<Double> relativePerformanceDOtoLP = new ArrayList<Double>(3*3*10*MAXSEED);
        ArrayList<Double> relativeSizeOfSupportInLP = new ArrayList<Double>(3*3*10*MAXSEED);
        ArrayList<Double> relativeSizeOfRG = new ArrayList<Double>(3*3*10*MAXSEED);

        PrintStream nullPS = new PrintStream(new OutputStream() {
            public void write(int b) {
                //DO NOTHING
            }
        });

        RandomGameInfo.UTILITY_CORRELATION = true;
        RandomGameInfo.BINARY_UTILITY = false;
        RandomGameInfo.FIXED_SIZE_BF = true;

        for (int BF = 18; BF<100; BF++) {
            RandomGameInfo.MAX_BF = BF;
            double mDepth = Math.ceil(Math.log(LARGEST_TREE)/Math.log(BF*BF));
            for (int DEPTH = 1; DEPTH < mDepth; DEPTH++) {
                RandomGameInfo.MAX_DEPTH = DEPTH;
                for (int MAXUT=1; MAXUT<11; MAXUT++) {
                    RandomGameInfo.MAX_CENTER_MODIFICATION = MAXUT;

                    System.out.println("Running RG with BF:" + BF + " DEPTH:" + DEPTH + " MAX_UTILITY:" + MAXUT + " and MAXSEED:"+MAXSEED);

                    for (int seed = 0; seed < MAXSEED; seed++) {

                        RandomGameInfo.seed = seed;
                        RandomGameInfo.rnd = new HighQualityRandom(RandomGameInfo.seed);
                        GameState rootState = new RandomGameState();
                        GameInfo gameInfo = new RandomGameInfo();

                        DoubleOracleConfig<DoubleOracleInformationSet> algConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
                        Expander<DoubleOracleInformationSet> expander = new RandomGameExpander<DoubleOracleInformationSet>(algConfig);
                        GeneralDoubleOracle doefg = new GeneralDoubleOracle(rootState,  expander, gameInfo, algConfig);

                        doefg.setDebugOutput(nullPS);

                        SequenceFormConfig<SequenceInformationSet> sqAlgConfig = new SequenceFormConfig<SequenceInformationSet>();
                        Expander<SequenceInformationSet> sqExpander = new RandomGameExpander<SequenceInformationSet>(sqAlgConfig);
                        FullSequenceEFG efg = new FullSequenceEFG(rootState, sqExpander, gameInfo, sqAlgConfig);

                        efg.setDebugOutput(nullPS);

                        Map<Player, Map<Sequence, Double>> resultEFG = efg.generate();
                        Map<Player, Map<Sequence, Double>> resultDO = doefg.generate(null);

                        long timeEFG = efg.getFinishTime();
                        long timeDO = doefg.getFinishTime();

                        relativePerformanceDOtoLP.add(timeDO/(double)timeEFG);

                        double thisRelativeSup = 1;
                        double thisSizeOfLP = 1;
                        for (Map<Sequence, Double> result : resultEFG.values()) {
                            int nonZeroActions = 0;
                            for (double d : result.values())
                                if (d > 0.000000001) nonZeroActions++;
                            thisRelativeSup *= nonZeroActions/(double)result.size();
                            thisSizeOfLP *= result.size();
                        }
                        relativeSizeOfSupportInLP.add(thisRelativeSup);

                        double thisSizeOfRG = 1;
                        for (Map<Sequence, Double> result : resultDO.values()) {
                            thisSizeOfRG *= result.size();
                        }
                        relativeSizeOfRG.add(thisSizeOfRG/thisSizeOfLP);

                    }
                    System.out.println("***************************\nSample,SupportSize,RGSize,Performance");
                    for (int i=0; i<relativePerformanceDOtoLP.size(); i++) {
                        System.out.print(i);
                        System.out.print(",");
                        System.out.print(relativeSizeOfSupportInLP.get(i));
                        System.out.print(",");
                        System.out.print(relativeSizeOfRG.get(i));
                        System.out.print(",");
                        System.out.println(relativePerformanceDOtoLP.get(i));
                    }
                    System.out.println("***************************");
                    relativePerformanceDOtoLP.clear();
                    relativeSizeOfSupportInLP.clear();
                    relativeSizeOfRG.clear();
                }
            }
        }
    }
}
