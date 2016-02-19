package cz.agents.gtlibrary.experimental.imperfectRecall;

import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo;
import cz.agents.gtlibrary.experimental.imperfectRecall.blseqformlp.BilinearSeqenceFormBNB;
import cz.agents.gtlibrary.experimental.imperfectRecall.blseqformlp.BilinearSeqenceFormLP;
import cz.agents.gtlibrary.experimental.imperfectRecall.blseqformlp.BilinearSeqenceFormSingleOracle;
import cz.agents.gtlibrary.experimental.imperfectRecall.blseqformlp.BilinearTable;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * Created by kail on 1/19/16.
 */
public class IRExperiments {

    public static void main(String[] args) {
        int startingSeed = (args.length > 0) ? new Integer(args[0]) : 1;
        int endingSeed = (args.length > 1) ? new Integer(args[1]) : 100;
        boolean fixingDigits = (args.length > 2) ? new Boolean(args[2]) : BilinearTable.fixPreviousDigits;

        int BF = (args.length > 3) ? new Integer(args[3]) : RandomGameInfo.MAX_BF;
        int DEPTH = (args.length > 4) ? new Integer(args[4]) : RandomGameInfo.MAX_DEPTH;
        boolean onlyP1IR = (args.length > 5) ? new Boolean(args[5]) : RandomGameInfo.IMPERFECT_RECALL_ONLYFORP1;

        BilinearTable.fixPreviousDigits = fixingDigits;
        RandomGameInfo.MAX_BF = BF;
        RandomGameInfo.MAX_DEPTH = DEPTH;
        RandomGameInfo.IMPERFECT_RECALL_ONLYFORP1 = onlyP1IR;

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        long start = threadBean.getCurrentThreadCpuTime();

        for (int seed = startingSeed; seed<endingSeed; seed++) {
            RandomGameInfo.seed = seed;
//            BilinearSeqenceFormLP.main(null);
//            BilinearSeqenceFormSingleOracle.main(null);
            BilinearSeqenceFormBNB.main(null);
            System.out.println("-----------------------");
        }

        System.out.println("OVERALL TIME = " + ((threadBean.getCurrentThreadCpuTime() - start)/1000000));
    }
}
