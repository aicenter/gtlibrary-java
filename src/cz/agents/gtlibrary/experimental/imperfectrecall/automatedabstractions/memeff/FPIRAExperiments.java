package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff;

import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.GeneralDoubleOracle;
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;

public class FPIRAExperiments {
    public static void main(String[] args) {
        if (args[0].equals("DO")) {
            if (args[1].equals("GP")) {
                setGP(args);
                GeneralDoubleOracle.runGenericPoker();
            } else if (args[1].equals("GS")) {
                setGS(args);
                GeneralDoubleOracle.runIIGoofspiel();
            }
        } else if (args[0].equals("FPIRA")) {
            if (args[1].equals("GP")) {
                setGP(args);
                FPIRA.runGenericPoker();
            } else if (args[1].equals("GS")) {
                setGS(args);
                FPIRA.runIIGoofspiel();
            }
        } else if (args[0].equals("IRCFR")) {
            if (args[1].equals("GP")) {
                setGP(args);
                if (args[5].equals("NONE"))
                    IRCFR.heuristic = IRCFR.SPLIT_HEURISTIC.NONE;
                else if (args[5].equals("AVG_VISITED"))
                    IRCFR.heuristic = IRCFR.SPLIT_HEURISTIC.AVG_VISITED;
                else if (args[5].equals("VISITED"))
                    IRCFR.heuristic = IRCFR.SPLIT_HEURISTIC.VISITED;
                else if (args[5].equals("UPDATED"))
                    IRCFR.heuristic = IRCFR.SPLIT_HEURISTIC.UPDATED;
                IRCFR.runGenericPoker();
            } else if (args[1].equals("GS")) {
                setGS(args);
                if (args[3].equals("NONE"))
                    IRCFR.heuristic = IRCFR.SPLIT_HEURISTIC.NONE;
                else if (args[3].equals("AVG_VISITED"))
                    IRCFR.heuristic = IRCFR.SPLIT_HEURISTIC.AVG_VISITED;
                else if (args[3].equals("VISITED"))
                    IRCFR.heuristic = IRCFR.SPLIT_HEURISTIC.VISITED;
                else if (args[3].equals("UPDATED"))
                    IRCFR.heuristic = IRCFR.SPLIT_HEURISTIC.UPDATED;
                IRCFR.runIIGoofspiel();
            }
        } else if (args[0].equals("MRCFR")) {
            if (args[1].equals("GP")) {
                setGP(args);
                MaxRegretIRCFR.DELETE_REGRETS = Boolean.parseBoolean(args[5]);
                MaxRegretIRCFR.ITERATION_MULTIPLIER = Double.parseDouble(args[6]);
                IRCFR.REGRET_MATCHING_PLUS = Boolean.parseBoolean(args[7]);
                MaxRegretIRCFR.runGenericPoker();
            } else if (args[1].equals("GS")) {
                setGS(args);
                MaxRegretIRCFR.DELETE_REGRETS = Boolean.parseBoolean(args[3]);
                MaxRegretIRCFR.ITERATION_MULTIPLIER = Double.parseDouble(args[4]);
                MaxRegretIRCFR.REGRET_MATCHING_PLUS = Boolean.parseBoolean(args[5]);
                MaxRegretIRCFR.runIIGoofspiel();
            }
        }
    }

    private static void setGS(String[] args) {
        GSGameInfo.CARDS_FOR_PLAYER = new int[Integer.parseInt(args[2])];
        for (int i = 0; i < Integer.parseInt(args[2]); i++) {
            GSGameInfo.CARDS_FOR_PLAYER[i] = i + 1;
        }
    }

    private static void setGP(String[] args) {
        GPGameInfo.MAX_DIFFERENT_BETS = Integer.parseInt(args[2]);
        GPGameInfo.MAX_DIFFERENT_RAISES = Integer.parseInt(args[3]);
        GPGameInfo.MAX_RAISES_IN_ROW = Integer.parseInt(args[4]);
    }
}
