package cz.agents.gtlibrary.experimental.imperfectrecall;

import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.BilinearSequenceFormLP;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * Created by kail on 1/19/16.
 */
public class IRExperiments {

    public static void main(String[] args) {
        int startingSeed = (args.length > 0) ? new Integer(args[0]) : 77;
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
//            cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.BilinearSeqenceFormBNB.main(null);
            double lpValue = BilinearSequenceFormLP.runRandomGame();
//            BilinearSeqenceFormSingleOracle.main(null);
//            double bnbValue = BilinearSequenceFormBnB.runRandomGame();
//            assert Math.abs(lpValue - bnbValue) < 1e-3;
            System.out.println("-----------------------");
        }

        System.out.println("OVERALL TIME = " + ((threadBean.getCurrentThreadCpuTime() - start)/1000000));
    }
}
