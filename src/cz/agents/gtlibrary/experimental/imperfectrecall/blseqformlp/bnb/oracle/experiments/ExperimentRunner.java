package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.experiments;

import cz.agents.gtlibrary.domain.bpg.BPGGameInfo;
import cz.agents.gtlibrary.domain.bpg.imperfectrecall.IRBPGGameState;
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.ir.IRGoofSpielGameState;
import cz.agents.gtlibrary.domain.oshizumo.OZGameInfo;
import cz.agents.gtlibrary.domain.oshizumo.ir.IROshiZumoGameState;
import cz.agents.gtlibrary.domain.poker.generic.ir.IRGenericPokerGameState;
import cz.agents.gtlibrary.domain.randomabstraction.RandomAbstractionGameInfo;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.BilinearSequenceFormLP;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.BilinearSequenceFormBnB;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.DoubleOracleBilinearSequenceFormBnB;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.OracleBilinearSequenceFormBnB;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.br.ALossBestResponseAlgorithm;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.stream.IntStream;

public class ExperimentRunner {
    public static void main(String[] args) {
        if (args[0].equals("TTT")) {
            DoubleOracleBilinearSequenceFormBnB.STATE_CACHE_USE = false;
            DoubleOracleBilinearSequenceFormBnB.RESOLVE_CURRENT_BEST = Boolean.parseBoolean(args[1]);
            DoubleOracleBilinearSequenceFormBnB.runTTT();
        } else if (args[0].equals("GS")) {
            DoubleOracleBilinearSequenceFormBnB.STATE_CACHE_USE = false;
            GSGameInfo.depth = Integer.parseInt(args[1]);
            IRGoofSpielGameState.REMEMBERED_MOVES = Integer.parseInt(args[2]);
            GSGameInfo.CARDS_FOR_PLAYER = IntStream.range(1, GSGameInfo.depth + 1).toArray();
            DoubleOracleBilinearSequenceFormBnB.runGoofSpiel();
        } else if (args[0].equals("OZ")) {
            DoubleOracleBilinearSequenceFormBnB.STATE_CACHE_USE = false;
            OZGameInfo.startingCoins= Integer.parseInt(args[1]);
            OZGameInfo.locK = Integer.parseInt(args[2]);
            OZGameInfo.minBid = Integer.parseInt(args[3]);
            IROshiZumoGameState.REMEMBERED_MOVES = Integer.parseInt(args[4]);
            DoubleOracleBilinearSequenceFormBnB.runOshiZumo();
        } else if (args[0].equals("BPG")) {
            DoubleOracleBilinearSequenceFormBnB.STATE_CACHE_USE = false;
            BPGGameInfo.DEPTH = Integer.parseInt(args[1]);
            BPGGameInfo.SLOW_MOVES = Boolean.parseBoolean(args[2]);
            if (args[3].equals("bnb")) {
                BilinearSequenceFormBnB.runBPG();
            } else {
                DoubleOracleBilinearSequenceFormBnB.RESOLVE_CURRENT_BEST = Boolean.parseBoolean(args[4]);
                IRBPGGameState.REMEMBERED_MOVES = Integer.parseInt(args[5]);
                DoubleOracleBilinearSequenceFormBnB.runBPG();
            }
        } else {
            DoubleOracleBilinearSequenceFormBnB.STATE_CACHE_USE = true;
            int BF = (args.length > 2) ? new Integer(args[2]) : RandomGameInfo.MAX_BF;
            int DEPTH = (args.length > 3) ? new Integer(args[3]) : RandomGameInfo.MAX_DEPTH;
            int seed = Integer.parseInt(args[5]);
            int obs = Integer.parseInt(args[6]);
            double joinProbability = Double.parseDouble(args[7]);

//        BilinearTable.fixPreviousDigits = fixingDigits;
            RandomGameInfo.MAX_BF = BF;
            RandomGameInfo.MIN_BF = BF;
            RandomGameInfo.MAX_DEPTH = DEPTH;
            RandomGameInfo.ABSENT_MINDEDNESS = false;
            RandomGameInfo.IMPERFECT_RECALL = false;
            RandomGameInfo.IMPERFECT_RECALL_ONLYFORP1 = false;
            RandomGameInfo.MAX_OBSERVATION = obs;
            RandomAbstractionGameInfo.JOIN_PROB = joinProbability;
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();


            long start = threadBean.getCurrentThreadCpuTime();
            RandomGameInfo.seed = seed;
            System.out.println("seed: " + seed);
            if (args[4].equals("bnb")) {
                BilinearSequenceFormBnB.runAbstractedRandomGame();
            } else if (args[4].equals("oraclebnb")) {
                OracleBilinearSequenceFormBnB.runAbstractedRandomGame();
            } else if (args[4].equals("base")) {
                BilinearSequenceFormLP.runAbstractedRandomGame();
            } else if (args[4].equals("dobnb")) {
                DoubleOracleBilinearSequenceFormBnB.RESOLVE_CURRENT_BEST = Boolean.parseBoolean(args[5]);
                DoubleOracleBilinearSequenceFormBnB.runAbstractedRandomGame();
            }
            System.out.println("-----------------------");
            System.out.println("OVERALL TIME = " + ((threadBean.getCurrentThreadCpuTime() - start) / 1000000));
        }
    }
}
