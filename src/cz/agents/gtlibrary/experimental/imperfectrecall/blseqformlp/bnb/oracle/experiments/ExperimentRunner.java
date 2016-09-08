package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.experiments;

import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.BilinearSeqenceFormLP;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.BilinearSequenceFormBnB;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.OracleBilinearSequenceFormBnB;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class ExperimentRunner {
    public static void main(String[] args) {
        if(args[0].equals("TTT")) {
              OracleBilinearSequenceFormBnB.runTTT();
        } else {
            int BF = (args.length > 2) ? new Integer(args[2]) : RandomGameInfo.MAX_BF;
            int DEPTH = (args.length > 3) ? new Integer(args[3]) : RandomGameInfo.MAX_DEPTH;
            int seed = Integer.parseInt(args[5]);
//        BilinearTable.fixPreviousDigits = fixingDigits;
            RandomGameInfo.MAX_BF = BF;
            RandomGameInfo.MAX_DEPTH = DEPTH;
            RandomGameInfo.IMPERFECT_RECALL_ONLYFORP1 = true;

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
            }
            System.out.println("-----------------------");
            System.out.println("OVERALL TIME = " + ((threadBean.getCurrentThreadCpuTime() - start) / 1000000));
        }
    }
}
