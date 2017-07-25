package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.experiments;

import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.BilinearSequenceFormLP;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.BilinearSequenceFormBnB;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.DoubleOracleBilinearSequenceFormBnB;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class ResultComparison {

    public static void main(String[] args) {
//        new Scanner(System.in).next();

        int startingSeed = (args.length > 0) ? new Integer(args[0]) : 1005;
        int endingSeed = (args.length > 1) ? new Integer(args[1]) : 2000;

        int BF = (args.length > 3) ? new Integer(args[3]) : RandomGameInfo.MAX_BF;
        int DEPTH = (args.length > 4) ? new Integer(args[4]) : RandomGameInfo.MAX_DEPTH;
        boolean onlyP1IR = (args.length > 5) ? new Boolean(args[5]) : RandomGameInfo.IMPERFECT_RECALL_ONLYFORP1;

//        BilinearTable.fixPreviousDigits = fixingDigits;
        RandomGameInfo.MAX_BF = BF;
        RandomGameInfo.MAX_DEPTH = DEPTH;
        RandomGameInfo.IMPERFECT_RECALL_ONLYFORP1 = onlyP1IR;

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        long start = threadBean.getCurrentThreadCpuTime();

        for (int seed = startingSeed; seed < endingSeed; seed++) {
            RandomGameInfo.seed = seed;
            System.err.println("seed: " + seed);
//            double bnbValue = BilinearSequenceFormBnB.runRandomGame();
//            System.out.println("*********************************");
            System.err.println("*******DO*******");
            double milpValue = DoubleOracleBilinearSequenceFormBnB.runAbstractedRandomGame();
            System.err.println("**************BnB*******************");
            double oracleBnBValue = BilinearSequenceFormLP.runAbstractedRandomGame();

            System.out.println("seed: " + seed + ": " + milpValue + " vs " + oracleBnBValue/* + " vs " + milpValue*/);
            if (Math.abs(oracleBnBValue - milpValue) > 0.05) {/* &&  < 1e-2*/
                throw new IllegalStateException();
            }
            System.out.println("-----------------------");
        }

        System.out.println("OVERALL TIME = " + ((threadBean.getCurrentThreadCpuTime() - start) / 1000000));
    }
}
