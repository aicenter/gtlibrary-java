package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.experiments;

import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.BilinearSeqenceFormLP;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.BilinearSequenceFormBnB;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.DoubleOracleBilinearSequenceFormBnB;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.OracleBilinearSequenceFormBnB;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class ResultComparison {

    public static void main(String[] args) {
        int startingSeed = (args.length > 0) ? new Integer(args[0]) : 1;
        int endingSeed = (args.length > 1) ? new Integer(args[1]) : 100;

        int BF = (args.length > 3) ? new Integer(args[3]) : RandomGameInfo.MAX_BF;
        int DEPTH = (args.length > 4) ? new Integer(args[4]) : RandomGameInfo.MAX_DEPTH;
        boolean onlyP1IR = (args.length > 5) ? new Boolean(args[5]) : RandomGameInfo.IMPERFECT_RECALL_ONLYFORP1;

//        BilinearTable.fixPreviousDigits = fixingDigits;
        RandomGameInfo.MAX_BF = BF;
        RandomGameInfo.MAX_DEPTH = DEPTH;
        RandomGameInfo.IMPERFECT_RECALL_ONLYFORP1 = onlyP1IR;

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        long start = threadBean.getCurrentThreadCpuTime();

        for (int seed = startingSeed; seed<endingSeed; seed++) {
            RandomGameInfo.seed = seed;
            System.out.println("seed: " + seed);
//            double bnbValue = BilinearSequenceFormBnB.runRandomGame();
//            System.out.println("*********************************");
            double milpValue = DoubleOracleBilinearSequenceFormBnB.runRandomGame();
            System.out.println("*********************************");
            double oracleBnBValue = BilinearSequenceFormBnB.runRandomGame();

            System.out.println("seed: " + seed + ": " + milpValue + " vs " + oracleBnBValue/* + " vs " + milpValue*/);
            if (Math.abs(oracleBnBValue - milpValue) > 0.1) {/* &&  < 1e-2*/
                throw new IllegalStateException();
            }
            System.out.println("-----------------------");
        }

        System.out.println("OVERALL TIME = " + ((threadBean.getCurrentThreadCpuTime() - start)/1000000));
    }
}
