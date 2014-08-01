package cz.agents.gtlibrary.algorithms.stackelberg;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.stackelberg.milp.StackelbergSequenceFormMILP;
import cz.agents.gtlibrary.algorithms.stackelberg.multiplelps.StackelbergSequenceFormMultipleLPs;
import cz.agents.gtlibrary.domain.bpg.BPGExpander;
import cz.agents.gtlibrary.domain.bpg.BPGGameInfo;
import cz.agents.gtlibrary.domain.bpg.GenSumBPGGameState;
import cz.agents.gtlibrary.domain.randomgame.GeneralSumRandomGameState;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;

public class StackelbergExperiments {
    /**
     * @param args
     *      [0] algorithm: MILP, MultLP, MultLPFeas
     *      [1] domain: BPG
     *             [2] depth
     *             [3] slow moves
     *             [4] graph file
     *             [5] attacker move penalty (-1 for default)
     *                  RND
     *             [2] max. branching factor
     *             [3] depth
     *             [4] bin. utility
     *             [5] correlation
     *             [6] observations
     *             [7] max. utility
     *             [8] fixed size branching factor
     *             [9] seed count
     */
    public static void main(String[] args) {
        if (args[1].equals("BPG")) {
            BPGGameInfo.DEPTH = Integer.parseInt(args[2]);
            BPGGameInfo.SLOW_MOVES = Boolean.parseBoolean(args[3]);
            BPGGameInfo.graphFile = args[4];
            double penalty = Double.parseDouble(args[5]);

            if (penalty != -1)
                BPGGameInfo.EVADER_MOVE_COST = penalty;
            runBPG(args[0]);
        } else if (args[1].equals("RND")) {
            RandomGameInfo.MAX_BF = Integer.parseInt(args[2]);
            RandomGameInfo.MAX_DEPTH = Integer.parseInt(args[3]);
            RandomGameInfo.BINARY_UTILITY = Boolean.parseBoolean(args[4]);
            RandomGameInfo.CORRELATION = Double.parseDouble(args[5]);
            RandomGameInfo.MAX_OBSERVATION = Integer.parseInt(args[6]);
            RandomGameInfo.MAX_UTILITY = Integer.parseInt(args[7]);
            RandomGameInfo.FIXED_SIZE_BF = Boolean.parseBoolean(args[8]);
            runRandomGame(args[0], Integer.parseInt(args[9]));
        } else {
            throw new UnsupportedOperationException("Unsupported domain");
        }
    }

    private static void runRandomGame(String algType, int seedCount) {
        for (int i = 0; i < seedCount; i++) {
            RandomGameInfo.seed = i;
            GameState rootState = new GeneralSumRandomGameState();
            GameInfo gameInfo = new RandomGameInfo();
            StackelbergConfig algConfig = new StackelbergConfig(rootState);
            Expander<SequenceInformationSet> expander = new RandomGameExpander<>(algConfig);
            StackelbergRunner runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);

            runner.generate(rootState.getAllPlayers()[0], getStackelbergSolver(algType, rootState, gameInfo, expander));
            System.out.println("------------");
        }
    }

    private static void runBPG(String algType) {
        GameState rootState = new GenSumBPGGameState();
        GameInfo gameInfo = new BPGGameInfo();
        StackelbergConfig algConfig = new StackelbergConfig(rootState);
        Expander<SequenceInformationSet> expander = new BPGExpander<>(algConfig);
        StackelbergRunner runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);

        runner.generate(rootState.getAllPlayers()[0], getStackelbergSolver(algType, rootState, gameInfo, expander));

    }

    private static StackelbergSequenceFormLP getStackelbergSolver(String algType, GameState rootState, GameInfo gameInfo, Expander<SequenceInformationSet> expander) {
        if (algType.equals("MILP"))
            return new StackelbergSequenceFormMILP(new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, gameInfo, expander);
        if (algType.startsWith("MultLP")) {
            StackelbergConfig.USE_FEASIBILITY_CUT = algType.equals("MultLPFeas");
            return new StackelbergSequenceFormMultipleLPs(new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, gameInfo, expander);
        } else {
            throw new UnsupportedOperationException("Unsupported algorithm");
        }
    }

}
