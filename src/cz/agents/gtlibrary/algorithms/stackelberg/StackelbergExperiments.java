package cz.agents.gtlibrary.algorithms.stackelberg;

import cz.agents.gtlibrary.domain.bpg.BPGGameInfo;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;

public class StackelbergExperiments {
    /**
     *
     * @param args
     * [0] algorithm: MILP, MultLP, MultLPFeas
     * [1] domain: BPG
     *      [2] depth
     *      [3] slow moves
     *      [4] graph file
     *      [5] attacker move penalty (-1 for default)
     *             RND
     *      [2] max. nramching factor
     *      [3] depth
     *      [4] bin. utility
     *      [5] correlation
     *      [6] observations
     *      [7] max. utility
     *      [8] fixed size branching factor
     *      [9] seed count
     */
    public static void main(String[] args) {
        if(args[1].equals("BPG")) {
            BPGGameInfo.DEPTH = Integer.parseInt(args[2]);
            BPGGameInfo.SLOW_MOVES = Boolean.parseBoolean(args[3]);
            BPGGameInfo.graphFile = args[4];
            double penalty =  Double.parseDouble(args[5]);

            if(penalty != -1)
                BPGGameInfo.EVADER_MOVE_COST = penalty;
            runBPG(args[0]);
        } else if(args[1].equals("RND")) {
            RandomGameInfo.MAX_BF = Integer.parseInt(args[2]);
            RandomGameInfo.MAX_DEPTH = Integer.parseInt(args[3]);
            RandomGameInfo.BINARY_UTILITY = Boolean.parseBoolean(args[4]);
            RandomGameInfo.CORRELATION = Double.parseDouble(args[5]);
            RandomGameInfo.MAX_OBSERVATION = Integer.parseInt(args[6]);
            RandomGameInfo.MAX_UTILITY = Integer.parseInt(args[7]);
            RandomGameInfo.FIXED_SIZE_BF = Boolean.parseBoolean(args[8]);
            runRandomGame(args[0], Integer.parseInt(args[9]));
        } else {
            throw new UnsupportedOperationException("Unsupported Domain");
        }
    }

    private static void runRandomGame(String algType, int seedCount) {
    }

    private static void runBPG(String algType) {


    }
}
