package cz.agents.gtlibrary.experimental.imperfectrecall;

import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.BilinearSequenceFormBnB;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oldimpl.BilinearSequenceFormBNB;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.BilinearSequenceFormLP;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class IRExperimentRunner {

    public static void main(String[] args) {
        int BF = (args.length > 3) ? new Integer(args[2]) : RandomGameInfo.MAX_BF;
        int DEPTH = (args.length > 4) ? new Integer(args[3]) : RandomGameInfo.MAX_DEPTH;

        RandomGameInfo.MAX_BF = BF;
        RandomGameInfo.MAX_DEPTH = DEPTH;
        RandomGameInfo.IMPERFECT_RECALL_ONLYFORP1 = true;

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        long start = threadBean.getCurrentThreadCpuTime();

        RandomGameInfo.seed = new Integer(args[0]);
        if (args[4].equals("base"))
            BilinearSequenceFormLP.main(null);
        else if (args[4].equals("bnb"))
            BilinearSequenceFormBnB.main(null);
        else
            BilinearSequenceFormBNB.main(null);
        System.out.println("OVERALL TIME = " + ((threadBean.getCurrentThreadCpuTime() - start) / 1000000));
    }
}
