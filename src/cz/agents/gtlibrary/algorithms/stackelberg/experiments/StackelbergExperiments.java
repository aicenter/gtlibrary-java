package cz.agents.gtlibrary.algorithms.stackelberg.experiments;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergRunner;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergSequenceFormLP;
import cz.agents.gtlibrary.algorithms.stackelberg.iterativelp.*;
import cz.agents.gtlibrary.algorithms.stackelberg.iterativelp.bfs.BFSEnforcingStackelbergLP;
import cz.agents.gtlibrary.algorithms.stackelberg.iterativelp.bfs.BFSForbiddingStackelbergLP;
import cz.agents.gtlibrary.algorithms.stackelberg.iterativelp.bfs.CompleteBFSEnforcingStackelbergLP;
import cz.agents.gtlibrary.algorithms.stackelberg.milp.DOBSS;
import cz.agents.gtlibrary.algorithms.stackelberg.milp.StackelbergSequenceFormMILP;
import cz.agents.gtlibrary.algorithms.stackelberg.multiplelps.StackelbergMultipleLPs;
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

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class StackelbergExperiments {
    /**
     * @param args [0] algorithm: MILP, MultLP, MultLPFeas, DOBBS
     *             [1] domain: BPG
     *             [2] leader index
     *             [3] depth
     *             [4] slow moves
     *             [5] graph file
     *             [6] attacker move penalty (-1 for default)
     *             [1] domain: BPG
     *             [2] leader index
     *             [3] max. branching factor
     *             [4] depth
     *             [5] bin. utility
     *             [6] correlation
     *             [7] observations
     *             [8] max. utility
     *             [9] fixed size branching factor
     *             [10] seed count
     */
    public static void main(String[] args) {
        if (args[1].equals("BPG")) {
            BPGGameInfo.DEPTH = Integer.parseInt(args[3]);
            BPGGameInfo.SLOW_MOVES = Boolean.parseBoolean(args[4]);
            BPGGameInfo.graphFile = args[5];
            double penalty = Double.parseDouble(args[6]);

            if (penalty != -1)
                BPGGameInfo.EVADER_MOVE_COST = penalty;
            runBPG(args[0], Integer.parseInt(args[2]));
        } else if (args[1].equals("RND")) {
            RandomGameInfo.MAX_BF = Integer.parseInt(args[3]);
            RandomGameInfo.MAX_DEPTH = Integer.parseInt(args[4]);
            RandomGameInfo.BINARY_UTILITY = Boolean.parseBoolean(args[5]);
            RandomGameInfo.CORRELATION = Double.parseDouble(args[6]);
            RandomGameInfo.MAX_OBSERVATION = Integer.parseInt(args[7]);
            RandomGameInfo.MAX_UTILITY = Integer.parseInt(args[8]);
            RandomGameInfo.FIXED_SIZE_BF = Boolean.parseBoolean(args[9]);
            runRandomGame(args[0], Integer.parseInt(args[2]), Integer.parseInt(args[10]));
        } else {
            throw new UnsupportedOperationException("Unsupported domain");
        }
    }

    public static void runRandomGame(String algType, int leaderIndex, int seedCount) {
        try {
            BufferedWriter timeWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(algType + ", " + new RandomGameInfo().getInfo() + " finalTime.csv")));
            BufferedWriter cutsWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(algType + ", " + new RandomGameInfo().getInfo() + " cuts.csv")));

            for (int i = 0; i < seedCount; i++) {
                RandomGameInfo.seed = i;
                GameState rootState = new GeneralSumRandomGameState();
                GameInfo gameInfo = new RandomGameInfo();
                StackelbergConfig algConfig = new StackelbergConfig(rootState);
                Expander<SequenceInformationSet> expander = new RandomGameExpander<>(algConfig);
                StackelbergRunner runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);
                StackelbergSequenceFormLP solver = getStackelbergSolver(algType, rootState, rootState.getAllPlayers()[leaderIndex], rootState.getAllPlayers()[1 - leaderIndex], gameInfo, expander);

                runner.generate(rootState.getAllPlayers()[leaderIndex], solver);
                timeWriter.write(String.valueOf(runner.getFinalTime()));
                timeWriter.newLine();
                timeWriter.flush();
                if (algType.startsWith("MultLP")) {
                    cutsWriter.write(String.valueOf(((StackelbergSequenceFormMultipleLPs) solver).prunnedRPCountWhileBuilding(algConfig)));
                    cutsWriter.write(",");
                    cutsWriter.write(String.valueOf(((StackelbergSequenceFormMultipleLPs) solver).getFeasibilityCuts()));
                    cutsWriter.write(",");
                    cutsWriter.write(String.valueOf(((StackelbergSequenceFormMultipleLPs) solver).getEvaluatedLPCount()));
                    cutsWriter.newLine();
                    cutsWriter.flush();
                }
                System.out.println("------------");
            }
            timeWriter.close();
            cutsWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void runBPG(String algType, int leaderIndex) {
        try {
            BufferedWriter timeWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(algType + ", " + new BPGGameInfo().getInfo() + " finalTime.csv")));
            BufferedWriter cutsWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(algType + ", " + new BPGGameInfo().getInfo() + " cuts.csv")));
            GameState rootState = new GenSumBPGGameState();
            GameInfo gameInfo = new BPGGameInfo();
            StackelbergConfig algConfig = new StackelbergConfig(rootState);
            Expander<SequenceInformationSet> expander = new BPGExpander<>(algConfig);
            StackelbergRunner runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);
            StackelbergSequenceFormLP solver = getStackelbergSolver(algType, rootState, rootState.getAllPlayers()[leaderIndex], rootState.getAllPlayers()[1 - leaderIndex], gameInfo, expander);

            runner.generate(rootState.getAllPlayers()[leaderIndex], solver);
            timeWriter.write(String.valueOf(runner.getFinalTime()));
            timeWriter.newLine();
            if (algType.equals("MultLP")) {
                cutsWriter.write(String.valueOf(((StackelbergSequenceFormMultipleLPs) solver).prunnedRPCountWhileBuilding(algConfig)));
                cutsWriter.write(",");
                cutsWriter.write(String.valueOf(((StackelbergSequenceFormMultipleLPs) solver).getFeasibilityCuts()));
                cutsWriter.write(",");
                cutsWriter.write(String.valueOf(((StackelbergSequenceFormMultipleLPs) solver).getEvaluatedLPCount()));
                cutsWriter.newLine();
            }
            timeWriter.close();
            cutsWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static StackelbergSequenceFormLP getStackelbergSolver(String algType, GameState rootState, Player leader, Player follower, GameInfo gameInfo, Expander<SequenceInformationSet> expander) {
        if (algType.equals("MILP"))
            return new StackelbergSequenceFormMILP(new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, leader, follower, gameInfo, expander);
        if (algType.equals("BFSEnforcingIterLP")) {
            SumForbiddingStackelbergLP.USE_BR_CUT = false;
            return new BFSEnforcingStackelbergLP(leader, gameInfo);
        }
        if (algType.equals("BFSEnforcingIterLPBR")) {
            SumEnforcingStackelbergLP.USE_BR_CUT = true;
            return new BFSEnforcingStackelbergLP(leader, gameInfo);
        }
        if (algType.equals("BFSCompleteEnforcingIterLP")) {
            SumEnforcingStackelbergLP.USE_BR_CUT = false;
            return new CompleteBFSEnforcingStackelbergLP(leader, gameInfo);
        }
        if (algType.equals("BFSForbiddingIterLP"))
            return new BFSForbiddingStackelbergLP(leader, gameInfo);
        if (algType.equals("IterLP"))
            return new SumForbiddingStackelbergLP(leader, gameInfo);
        if (algType.equals("IterLPCplexBroken")) {
            SumForbiddingStackelbergLP.USE_BR_CUT = false;
            return new ShallowestBrokenCplexStackelbergLP(leader, gameInfo);
        }
        if (algType.equals("IterLPCplexBrokenBR")) {
            SumForbiddingStackelbergLP.USE_BR_CUT = true;
            return new ShallowestBrokenCplexStackelbergLP(leader, gameInfo);
        }
        if (algType.equals("IterLPCplexBrokenAllShallowest"))
            return new AllShallowestBrokenCplexStackelbergLP(leader, gameInfo);
        if (algType.equals("IterLPCplexBrokenAll"))
            return new CompleteBrokenCplexStackelbergLP(leader, gameInfo);
        if (algType.equals("IterLPCplexCompleteAll"))
            return new CompleteAllCplexStackelbergLP(leader, gameInfo);
        if (algType.equals("IterLPCplexComplete"))
            return new ShallowestAllCplexStackelbergLP(leader, gameInfo);
        if(algType.equals("IterLPEnforcing"))
            return new SumEnforcingStackelbergLP(leader, gameInfo);
        if(algType.equals("MaxIterLPEnforcing"))
            return new MaxEnforcingStackelbergLP(leader, gameInfo);
        if(algType.equals("ExpValIterLPEnforcing"))
            return new ExpValEnforcingStackelbergLP(leader, gameInfo);
        if(algType.equals("ExpValIterLPForbidding"))
            return new ExpValForbiddingStackelbergLP(leader, gameInfo);
        if (algType.equals("MaxIterLP"))
            return new MaxForbiddingStackelbergLP(leader, gameInfo);
        if (algType.startsWith("MultLP")) {
            StackelbergConfig.USE_FEASIBILITY_CUT = algType.startsWith("MultLPFeas");
            StackelbergConfig.USE_UPPERBOUND_CUT = !algType.endsWith("NoCut");
            return new StackelbergSequenceFormMultipleLPs(new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, leader, follower, gameInfo, expander);
        }
        if (algType.startsWith("ExpMultLP")) {
            StackelbergConfig.USE_UPPERBOUND_CUT = !algType.endsWith("NoCut");
            return new StackelbergMultipleLPs(new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, leader, follower, gameInfo, expander);
        }
        if (algType.equals("DOBBS"))
            return new DOBSS(new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, leader, follower, gameInfo, expander);
        throw new UnsupportedOperationException("Unsupported algorithm");
    }

}
