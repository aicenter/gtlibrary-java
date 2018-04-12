package cz.agents.gtlibrary.algorithms.stackelberg.experiments;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPTable;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergRunner;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergSequenceFormLP;
import cz.agents.gtlibrary.algorithms.stackelberg.iterativelp.SumForbiddingStackelbergLP;
import cz.agents.gtlibrary.algorithms.stackelberg.multiplelps.StackelbergSequenceFormMultipleLPs;
import cz.agents.gtlibrary.domain.randomgame.GeneralSumRandomGameState;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.GameState;
import ilog.cplex.IloCplex;

import java.io.*;
import java.util.StringTokenizer;

public class RandomGameFromConfigRunner {

    /**
     * @param args [0] alg
     *             [1] config file
     *             [2] leader index
     *             [3] correlation
     *             [4] line
     *             [5] cplex alg
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(args[1]))));
        String line = null;

        RandomGameInfo.FIXED_SIZE_BF = false;
        RandomGameInfo.BINARY_UTILITY = true;
        RandomGameInfo.UTILITY_CORRELATION = false;
        RandomGameInfo.CORRELATION = Double.parseDouble(args[3]);
        BufferedWriter timeWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[0] + " " + RandomGameInfo.CORRELATION + " finalTime.csv", true)));
        BufferedWriter cutsWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[0] + " " + RandomGameInfo.CORRELATION + " cuts.csv", true)));
        BufferedWriter lpCountWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[0] + " " + RandomGameInfo.CORRELATION + " LPInvocation.csv", true)));

        int lineIndex = Integer.parseInt(args[4]);
        int count = 0;

        setCplexAlg(args[5]);
        while (count++ <= lineIndex) {
            line = reader.readLine();
        }
        if (line == null)
            return;
//        while ((line = reader.readLine()) != null) {
        System.out.println("line: " + ++count);
        StringTokenizer tokenizer = new StringTokenizer(line);
        RandomGameInfo.seed = Integer.parseInt(tokenizer.nextToken());
        RandomGameInfo.MAX_OBSERVATION = Integer.parseInt(tokenizer.nextToken());
        RandomGameInfo.MAX_DEPTH = Integer.parseInt(tokenizer.nextToken());
        RandomGameInfo.MAX_BF = Integer.parseInt(tokenizer.nextToken());
//            System.out.println("!!!!stored: " + tokenizer.nextToken());
        runRandomGame(timeWriter, cutsWriter, lpCountWriter, args[0], Integer.parseInt(args[2]), Integer.parseInt(tokenizer.nextToken()));
//        }
        timeWriter.close();
        cutsWriter.close();
        reader.close();
    }

    public static void runRandomGame(BufferedWriter timeWriter, BufferedWriter cutsWriter, BufferedWriter lpCountWriter, String algType, int leaderIndex, int expectedRPCount) {
        try {
            GameState rootState = new GeneralSumRandomGameState();
            GameInfo gameInfo = new RandomGameInfo();
            StackelbergConfig algConfig = new StackelbergConfig(rootState);
            Expander<SequenceInformationSet> expander = new RandomGameExpander<>(algConfig);
            StackelbergRunner runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);
            StackelbergSequenceFormLP solver = StackelbergExperiments.getStackelbergSolver(algType, rootState, rootState.getAllPlayers()[leaderIndex], rootState.getAllPlayers()[1 - leaderIndex], gameInfo, expander);

            runner.generate(rootState.getAllPlayers()[leaderIndex], solver);
//            System.out.println("current: " + ConfigurationGenerator.getRPCount(rootState.getAllPlayers()[leaderIndex], rootState.getAllPlayers()[1 - leaderIndex], algConfig, expander));
            timeWriter.write(String.valueOf(runner.getFinalTime()));
            timeWriter.newLine();
            timeWriter.flush();
            if (algType.startsWith("MultLP")) {
                int prunnedRPCountWhileBuilding = ((StackelbergSequenceFormMultipleLPs) solver).prunnedRPCountWhileBuilding(algConfig);
                int feasibilityCuts = ((StackelbergSequenceFormMultipleLPs) solver).getFeasibilityCuts();
                int evaluatedLPs = ((StackelbergSequenceFormMultipleLPs) solver).getEvaluatedLPCount();

                cutsWriter.write(String.valueOf(prunnedRPCountWhileBuilding));
                cutsWriter.write(",");
                cutsWriter.write(String.valueOf(feasibilityCuts));
                cutsWriter.write(",");
                cutsWriter.write(String.valueOf(evaluatedLPs));
                cutsWriter.write(",");
                cutsWriter.write(String.valueOf(((StackelbergSequenceFormMultipleLPs) solver).getAllRPCount(algConfig)));
                cutsWriter.newLine();
                cutsWriter.flush();
                assert ((StackelbergSequenceFormMultipleLPs) solver).getAllRPCount(algConfig) == expectedRPCount;
            } else if (solver instanceof SumForbiddingStackelbergLP) {
                int lpCount = ((SumForbiddingStackelbergLP) solver).getLPInvocationCount();

                lpCountWriter.write(String.valueOf(lpCount));
                lpCountWriter.newLine();
                lpCountWriter.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setCplexAlg(String cplexAlg) {
        if (cplexAlg.equals("DualSimplex"))
            LPTable.CPLEXALG = IloCplex.Algorithm.Dual;
        else if (cplexAlg.equals("PrimalSimplex"))
            LPTable.CPLEXALG = IloCplex.Algorithm.Primal;
        else if (cplexAlg.equals("NetworkSimplex"))
            LPTable.CPLEXALG = IloCplex.Algorithm.Network;
        else if (cplexAlg.equals("Barrier"))
            LPTable.CPLEXALG = IloCplex.Algorithm.Barrier;
        else if (cplexAlg.equals("Auto"))
            LPTable.CPLEXALG = IloCplex.Algorithm.Auto;
        else
            throw new UnsupportedOperationException("Cplex algorithm unsupported");
    }
}
