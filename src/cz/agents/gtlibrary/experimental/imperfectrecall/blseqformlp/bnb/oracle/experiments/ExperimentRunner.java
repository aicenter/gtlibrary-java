package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.experiments;

import cz.agents.gtlibrary.domain.bpg.BPGGameInfo;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.BilinearSeqenceFormLP;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.BilinearSequenceFormBnB;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.DoubleOracleBilinearSequenceFormBnB;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.OracleBilinearSequenceFormBnB;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class ExperimentRunner {
    public static void main(String[] args) {
        if (args[0].equals("TTT")) {
            DoubleOracleBilinearSequenceFormBnB.runTTT();
        } else if (args[0].equals("BPG")) {
            BPGGameInfo.DEPTH = Integer.parseInt(args[1]);
            BPGGameInfo.SLOW_MOVES = Boolean.parseBoolean(args[2]);
            if (args[3].equals("bnb"))
                BilinearSequenceFormBnB.runBPG();
            else
                DoubleOracleBilinearSequenceFormBnB.runBPG();

        } else {
            int BF = (args.length > 2) ? new Integer(args[2]) : RandomGameInfo.MAX_BF;
            int DEPTH = (args.length > 3) ? new Integer(args[3]) : RandomGameInfo.MAX_DEPTH;
            int seed = Integer.parseInt(args[5]);
            int obs = Integer.parseInt(args[6]);

//        BilinearTable.fixPreviousDigits = fixingDigits;
            RandomGameInfo.MAX_BF = BF;
            RandomGameInfo.MIN_BF = BF;
            RandomGameInfo.MAX_DEPTH = DEPTH;
            RandomGameInfo.ABSENT_MINDEDNESS = false;
            RandomGameInfo.IMPERFECT_RECALL = true;
            RandomGameInfo.IMPERFECT_RECALL_ONLYFORP1 = true;
            RandomGameInfo.MAX_OBSERVATION = obs;
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();


            long start = threadBean.getCurrentThreadCpuTime();
            RandomGameInfo.seed = seed;
            System.out.println("seed: " + seed);
            if (args[4].equals("bnb")) {
                BilinearSequenceFormBnB.runRandomGame();
            } else if (args[4].equals("oraclebnb")) {
                OracleBilinearSequenceFormBnB.runRandomGame();
            } else if (args[4].equals("base")) {
                BilinearSeqenceFormLP.runRandomGame();
            } else if (args[4].equals("dobnb")) {
                DoubleOracleBilinearSequenceFormBnB.runRandomGame();
            }
            System.out.println("-----------------------");
            System.out.println("OVERALL TIME = " + ((threadBean.getCurrentThreadCpuTime() - start) / 1000000));
        }
    }
}
