package cz.agents.gtlibrary.experimental.imperfectRecall;

import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo;
import cz.agents.gtlibrary.experimental.imperfectRecall.blseqformlp.BilinearSeqenceFormLP;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * Created by kail on 1/19/16.
 */
public class IRExperiments {

    public static void main(String[] args) {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        long start = threadBean.getCurrentThreadCpuTime();

        for (int seed = 1; seed<50; seed++) {
            RandomGameInfo.seed = seed;
            BilinearSeqenceFormLP.main(null);
            System.out.println("-----------------------");
        }

        System.out.println("OVERALL TIME = " + ((threadBean.getCurrentThreadCpuTime() - start)/1000000));
    }
}
