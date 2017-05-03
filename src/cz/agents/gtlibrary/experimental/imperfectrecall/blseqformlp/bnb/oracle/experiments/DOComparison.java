package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.experiments;

import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.GeneralDoubleOracle;
import cz.agents.gtlibrary.domain.bpg.BPGGameInfo;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.DoubleOracleBilinearSequenceFormBnB;

public class DOComparison {
    public static void main(String[] args) {

//        OZGameInfo.startingCoins = Integer.parseInt(args[1]);
//        OZGameInfo.locK = Integer.parseInt(args[2]);
//        OZGameInfo.minBid = Integer.parseInt(args[3]);
//        if(args[0].equals("DO"))
//            GeneralDoubleOracle.runIIOshiZumo();
//        else
//            DoubleOracleBilinearSequenceFormBnB.runOshiZumo();
        BPGGameInfo.SLOW_MOVES = false;
        BPGGameInfo.DEPTH = Integer.parseInt(args[1]);
        if(args[0].equals("DO"))
            GeneralDoubleOracle.runBP();
        else
            DoubleOracleBilinearSequenceFormBnB.runAttackerBPG();
    }
}
