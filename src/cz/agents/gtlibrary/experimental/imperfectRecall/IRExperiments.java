package cz.agents.gtlibrary.experimental.imperfectRecall;

import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo;
import cz.agents.gtlibrary.experimental.imperfectRecall.blseqformlp.BilinearSeqenceFormLP;

/**
 * Created by kail on 1/19/16.
 */
public class IRExperiments {

    public static void main(String[] args) {
        for (int seed = 1; seed<50; seed++) {
            RandomGameInfo.seed = seed;
            BilinearSeqenceFormLP.main(null);
            System.out.println("-----------------------");
        }
    }
}
