package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff;

import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.GeneralDoubleOracle;
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo;
import cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff.cfr.IRCFR;
import cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff.cfr.LimitedMemoryMaxRegretIRCFR;
import cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff.cfr.MaxRegretIRCFR;
import cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff.fpira.FPIRABRFirst;
import cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff.fpira.FrequencyFPIRA;

public class FPIRAExperiments {
    public static void main(String[] args) {
        MaxRegretIRCFR.LOG_REGRETS = false;
        if (args[1].equals("GP")) {
            setGP(args);
        } else if (args[1].equals("GS")) {
            setGS(args);
        } else if (args[1].equals("RG")) {
            setRG(args);
        } else if (args[1].equals("VP")) {
            setVP(args);
        }
        if (args[0].equals("DO")) {
            if (args[1].equals("GP")) {
                GeneralDoubleOracle.runGenericPoker();
            } else if (args[1].equals("GS")) {
                GeneralDoubleOracle.runIIGoofspiel();
            } else if (args[1].equals("RG")) {
                GeneralDoubleOracle.runRandomGame();
            } else if (args[1].equals("VP")) {
                GeneralDoubleOracle.runVisibilityPursuit();
            }
        } else if (args[0].equals("FPIRA")) {
            if (args[1].equals("GP")) {
                FPIRABRFirst.runGenericPoker();
            } else if (args[1].equals("GS")) {
                FPIRABRFirst.runIIGoofspiel();
            } else if (args[1].equals("RG")) {
                FPIRABRFirst.runRandomGame();
            } else if (args[1].equals("VP")) {
                FPIRABRFirst.runVisibilityPursuit();
            }
        } else if (args[0].equals("FreqFPIRA")) {
            if (args[1].equals("GP")) {
                FrequencyFPIRA.runGenericPoker();
            } else if (args[1].equals("GS")) {
                FrequencyFPIRA.runIIGoofspiel();
            }
        } else if (args[0].equals("IRCFR")) {
            if (args[1].equals("GP")) {
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
                MaxRegretIRCFR.DELETE_REGRETS = Boolean.parseBoolean(args[5]);
                MaxRegretIRCFR.ITERATION_MULTIPLIER = Double.parseDouble(args[6]);
                IRCFR.REGRET_MATCHING_PLUS = Boolean.parseBoolean(args[7]);
                MaxRegretIRCFR.USE_SPLIT_TOLERANCE = Boolean.parseBoolean(args[8]);
                MaxRegretIRCFR.CLEAR_DATA = Boolean.parseBoolean(args[9]);
                MaxRegretIRCFR.SIMULTANEOUS_PR_IR = Boolean.parseBoolean(args[10]);
                MaxRegretIRCFR.DIRECT_REGRET_UPDATE = Boolean.parseBoolean(args[11]);
                MaxRegretIRCFR.runGenericPoker();
            } else if (args[1].equals("GS")) {
                MaxRegretIRCFR.DELETE_REGRETS = Boolean.parseBoolean(args[3]);
                MaxRegretIRCFR.ITERATION_MULTIPLIER = Double.parseDouble(args[4]);
                MaxRegretIRCFR.REGRET_MATCHING_PLUS = Boolean.parseBoolean(args[5]);
                MaxRegretIRCFR.USE_SPLIT_TOLERANCE = Boolean.parseBoolean(args[6]);
                MaxRegretIRCFR.CLEAR_DATA = Boolean.parseBoolean(args[7]);
                MaxRegretIRCFR.SIMULTANEOUS_PR_IR = Boolean.parseBoolean(args[8]);
                MaxRegretIRCFR.DIRECT_REGRET_UPDATE = Boolean.parseBoolean(args[9]);
                MaxRegretIRCFR.runIIGoofspiel();
            }
        } else if (args[0].equals("LMMRCFR")) {
            if (args[1].equals("GP")) {
                MaxRegretIRCFR.DELETE_REGRETS = Boolean.parseBoolean(args[5]);
                MaxRegretIRCFR.ITERATION_MULTIPLIER = Double.parseDouble(args[6]);
                IRCFR.REGRET_MATCHING_PLUS = Boolean.parseBoolean(args[7]);
                MaxRegretIRCFR.USE_SPLIT_TOLERANCE = Boolean.parseBoolean(args[8]);
                MaxRegretIRCFR.CLEAR_DATA = Boolean.parseBoolean(args[9]);
                MaxRegretIRCFR.SIMULTANEOUS_PR_IR = Boolean.parseBoolean(args[10]);
                LimitedMemoryMaxRegretIRCFR.sizeLimitHeuristic = Integer.parseInt(args[11]);
                MaxRegretIRCFR.DIRECT_REGRET_UPDATE = Boolean.parseBoolean(args[12]);
                LimitedMemoryMaxRegretIRCFR.sizeLimitBound = Integer.parseInt(args[13]);
                LimitedMemoryMaxRegretIRCFR.sizeLimitHeuristic = Integer.parseInt(args[14]);
                IRCFR.PRINT_EXPLOITABILITY = Boolean.parseBoolean(args[15]);
                LimitedMemoryMaxRegretIRCFR.IS_SAMPLING_SEED = Long.parseLong(args[16]);
                LimitedMemoryMaxRegretIRCFR.runGenericPoker();
            } else if (args[1].equals("GS")) {
                MaxRegretIRCFR.DELETE_REGRETS = Boolean.parseBoolean(args[3]);
                MaxRegretIRCFR.ITERATION_MULTIPLIER = Double.parseDouble(args[4]);
                MaxRegretIRCFR.REGRET_MATCHING_PLUS = Boolean.parseBoolean(args[5]);
                MaxRegretIRCFR.USE_SPLIT_TOLERANCE = Boolean.parseBoolean(args[6]);
                MaxRegretIRCFR.CLEAR_DATA = Boolean.parseBoolean(args[7]);
                MaxRegretIRCFR.SIMULTANEOUS_PR_IR = Boolean.parseBoolean(args[8]);
                LimitedMemoryMaxRegretIRCFR.sizeLimitHeuristic = Integer.parseInt(args[9]);
                MaxRegretIRCFR.DIRECT_REGRET_UPDATE = Boolean.parseBoolean(args[10]);
                LimitedMemoryMaxRegretIRCFR.sizeLimitBound = Integer.parseInt(args[11]);
                LimitedMemoryMaxRegretIRCFR.sizeLimitHeuristic = Integer.parseInt(args[12]);
                IRCFR.PRINT_EXPLOITABILITY = Boolean.parseBoolean(args[13]);
                LimitedMemoryMaxRegretIRCFR.IS_SAMPLING_SEED = Long.parseLong(args[14]);
                LimitedMemoryMaxRegretIRCFR.runIIGoofspiel();
            } else if (args[1].equals("RG")) {
                MaxRegretIRCFR.DELETE_REGRETS = Boolean.parseBoolean(args[5]);
                MaxRegretIRCFR.ITERATION_MULTIPLIER = Double.parseDouble(args[6]);
                IRCFR.REGRET_MATCHING_PLUS = Boolean.parseBoolean(args[7]);
                MaxRegretIRCFR.USE_SPLIT_TOLERANCE = Boolean.parseBoolean(args[8]);
                MaxRegretIRCFR.CLEAR_DATA = Boolean.parseBoolean(args[9]);
                MaxRegretIRCFR.SIMULTANEOUS_PR_IR = Boolean.parseBoolean(args[10]);
                LimitedMemoryMaxRegretIRCFR.sizeLimitHeuristic = Integer.parseInt(args[11]);
                MaxRegretIRCFR.DIRECT_REGRET_UPDATE = Boolean.parseBoolean(args[12]);
                LimitedMemoryMaxRegretIRCFR.sizeLimitBound = Integer.parseInt(args[13]);
                LimitedMemoryMaxRegretIRCFR.sizeLimitHeuristic = Integer.parseInt(args[14]);
                IRCFR.PRINT_EXPLOITABILITY = Boolean.parseBoolean(args[15]);
                LimitedMemoryMaxRegretIRCFR.IS_SAMPLING_SEED = Long.parseLong(args[16]);
                LimitedMemoryMaxRegretIRCFR.runRandomGame();
            } else if (args[1].equals("VP")) {
                MaxRegretIRCFR.DELETE_REGRETS = Boolean.parseBoolean(args[3]);
                MaxRegretIRCFR.ITERATION_MULTIPLIER = Double.parseDouble(args[4]);
                MaxRegretIRCFR.REGRET_MATCHING_PLUS = Boolean.parseBoolean(args[5]);
                MaxRegretIRCFR.USE_SPLIT_TOLERANCE = Boolean.parseBoolean(args[6]);
                MaxRegretIRCFR.CLEAR_DATA = Boolean.parseBoolean(args[7]);
                MaxRegretIRCFR.SIMULTANEOUS_PR_IR = Boolean.parseBoolean(args[8]);
                LimitedMemoryMaxRegretIRCFR.sizeLimitHeuristic = Integer.parseInt(args[9]);
                MaxRegretIRCFR.DIRECT_REGRET_UPDATE = Boolean.parseBoolean(args[10]);
                LimitedMemoryMaxRegretIRCFR.sizeLimitBound = Integer.parseInt(args[11]);
                LimitedMemoryMaxRegretIRCFR.sizeLimitHeuristic = Integer.parseInt(args[12]);
                IRCFR.PRINT_EXPLOITABILITY = Boolean.parseBoolean(args[13]);
                LimitedMemoryMaxRegretIRCFR.IS_SAMPLING_SEED = Long.parseLong(args[14]);
                LimitedMemoryMaxRegretIRCFR.runVisibilityPursuit();
            }
        } else if (args[0].equals("CFR+")) {
            IRCFR.REGRET_MATCHING_PLUS = true;
            MaxRegretIRCFR.DIRECT_REGRET_UPDATE = true;
            AutomatedAbstractionAlgorithm.USE_ABSTRACTION = false;
            if (args[1].equals("GP")) {
                IRCFR.PRINT_EXPLOITABILITY = Boolean.parseBoolean(args[5]);
                MaxRegretIRCFR.runGenericPoker();
            } else if (args[1].equals("GS")) {
                IRCFR.PRINT_EXPLOITABILITY = Boolean.parseBoolean(args[3]);
                MaxRegretIRCFR.runIIGoofspiel();
            } else if (args[1].equals("RG")) {
                IRCFR.PRINT_EXPLOITABILITY = Boolean.parseBoolean(args[5]);
                MaxRegretIRCFR.runRandomGame();
            } else if (args[1].equals("VP")) {
                IRCFR.PRINT_EXPLOITABILITY = Boolean.parseBoolean(args[3]);
                MaxRegretIRCFR.runVisibilityPursuit();
            }
        }
    }

    private static void setGS(String[] args) {
        GSGameInfo.CARDS_FOR_PLAYER = new int[Integer.parseInt(args[2])];
        GSGameInfo.depth = GSGameInfo.CARDS_FOR_PLAYER.length;
        for (int i = 0; i < Integer.parseInt(args[2]); i++) {
            GSGameInfo.CARDS_FOR_PLAYER[i] = i + 1;
        }
    }

    private static void setRG(String[] args) {
        RandomGameInfo.MIN_BF = Integer.parseInt(args[2]);
        RandomGameInfo.MAX_BF = Integer.parseInt(args[2]);
        RandomGameInfo.MAX_DEPTH = Integer.parseInt(args[3]);
        RandomGameInfo.seed = Integer.parseInt(args[4]);
    }

    private static void setGP(String[] args) {
        GPGameInfo.MAX_DIFFERENT_BETS = Integer.parseInt(args[2]);
        GPGameInfo.MAX_DIFFERENT_RAISES = Integer.parseInt(args[3]);
        GPGameInfo.MAX_RAISES_IN_ROW = Integer.parseInt(args[4]);
    }

    public static void setVP(String[] args) {
//        PursuitGameInfo.depth = Integer.parseInt(args[2]);
    }
}
