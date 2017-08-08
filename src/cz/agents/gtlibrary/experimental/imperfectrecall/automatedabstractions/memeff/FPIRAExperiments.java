package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff;

import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.GeneralDoubleOracle;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;

public class FPIRAExperiments {
    public static void main(String[] args) {
        if (args[0].equals("DO")) {
            if(args[1].equals("GP")) {
                setGP(args);
                GeneralDoubleOracle.runGenericPoker();
            }
        } else if (args[0].equals("FPIRA")){
            if(args[1].equals("GP")) {
                setGP(args);
                FPIRA.runGenericPoker();
            }
        }  else if (args[0].equals("IRCFR")){
            if(args[1].equals("GP")) {
                setGP(args);
                if(args[5].equals("NONE"))
                    IRCFR.heuristic = IRCFR.SPLIT_HEURISTIC.NONE;
                else if (args[5].equals("AVG_VISITED"))
                    IRCFR.heuristic = IRCFR.SPLIT_HEURISTIC.AVG_VISITED;
                else if (args[5].equals("VISITED"))
                    IRCFR.heuristic = IRCFR.SPLIT_HEURISTIC.VISITED;
                else if (args[5].equals("UDPATED"))
                    IRCFR.heuristic = IRCFR.SPLIT_HEURISTIC.UPDATED;
                IRCFR.runGenericPoker();
            }
        }
    }

    private static void setGP(String[] args) {
        GPGameInfo.MAX_DIFFERENT_BETS = Integer.parseInt(args[2]);
        GPGameInfo.MAX_DIFFERENT_RAISES = Integer.parseInt(args[3]);
        GPGameInfo.MAX_RAISES_IN_ROW = Integer.parseInt(args[4]);
    }
}
