package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff;

import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.GeneralDoubleOracle;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;

public class DOvsFPIRAExperiments {
    public static void main(String[] args) {
        if (args[0].equals("DO")) {
            if(args[1].equals("GP")) {
                setGP(args);
                GeneralDoubleOracle.runGenericPoker();
            }
        } else {
            if(args[1].equals("GP")) {
                setGP(args);
                FPIRA.runGenericPoker();
            }
        }
    }

    private static void setGP(String[] args) {
        GPGameInfo.MAX_DIFFERENT_BETS = Integer.parseInt(args[2]);
        GPGameInfo.MAX_DIFFERENT_RAISES = Integer.parseInt(args[3]);
        GPGameInfo.MAX_RAISES_IN_ROW = Integer.parseInt(args[4]);
    }
}
