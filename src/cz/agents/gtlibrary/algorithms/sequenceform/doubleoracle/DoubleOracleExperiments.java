package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle;

import cz.agents.gtlibrary.algorithms.sequenceform.FullSequenceEFG;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.unprunning.UnprunningDoubleOracle;
import cz.agents.gtlibrary.domain.bpg.BPGGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.utils.HighQualityRandom;

import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 3/8/13
 * Time: 10:09 AM
 * To change this template use File | Settings | File Templates.
 */
public class DoubleOracleExperiments {



    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Missing Arguments: DoubleOracleExperiments {DO|LP} {BP|GP|RG} [algorithm/domain parameters].");
            System.exit(-1);
        }
        String alg = args[0];
        DoubleOracleExperiments exp = new DoubleOracleExperiments();
        exp.handleDomain(args);
        exp.runAlgorithm(alg, args[1]);
    }


    public void handleDomain(String[] args) {
        if (args[1].equalsIgnoreCase("BP")) {  // Border Patrolling
            if (args.length != 5) {
                throw new IllegalArgumentException("Illegal domain arguments count. 3 are required {DEPTH} {GRAPH} {SLOW_MOVES}");
            }
            int depth = new Integer(args[2]);
            BPGGameInfo.graphFile = args[3];
            BPGGameInfo.SLOW_MOVES = new Boolean(args[4]);
            BPGGameInfo.DEPTH = depth;
        } else if (args[1].equalsIgnoreCase("GP")) { // Generic Poker
            if (args.length != 6) {
                throw new IllegalArgumentException("Illegal poker domain arguments count. 4 are required {MAX_RISES} {MAX_BETS} {MAX_CARD_TYPES} {MAX_CARD_OF_EACH_TYPE}");
            }
            GPGameInfo.MAX_RAISES_IN_ROW = new Integer(args[2]);
            GPGameInfo.MAX_DIFFERENT_BETS = new Integer(args[3]);
            GPGameInfo.MAX_DIFFERENT_RAISES = GPGameInfo.MAX_DIFFERENT_BETS;
            GPGameInfo.MAX_CARD_TYPES = new Integer(args[4]);
            GPGameInfo.MAX_CARD_OF_EACH_TYPE = new Integer(args[5]);
        } else if (args[1].equalsIgnoreCase("RG")) { // Random Games
            if (args.length != 9) {
                throw new IllegalArgumentException("Illegal random game domain arguments count. 7 are required {SEED} {DEPTH} {BF} {OBSERVATION} {UTILITY} {BIN} {CORR}");
            }
            RandomGameInfo.seed = new Integer(args[2]);
            RandomGameInfo.rnd = new HighQualityRandom(RandomGameInfo.seed);
            RandomGameInfo.MAX_DEPTH = new Integer(args[3]);
            RandomGameInfo.MAX_BF = new Integer(args[4]);
            RandomGameInfo.MAX_OBSERVATION = new Integer(args[5]);
            RandomGameInfo.MAX_UTILITY = new Integer(args[6]);
            RandomGameInfo.BINARY_UTILITY = new Boolean(args[7]);
            RandomGameInfo.UTILITY_CORRELATION = new Boolean(args[8]);
        } else throw new IllegalArgumentException("Illegal domain: " + args[1]);
    }

    public void runAlgorithm(String alg, String domain) {
        if (alg.startsWith("DO")) {
            if (alg.equalsIgnoreCase("DO-B")) {
                GeneralDoubleOracle.playerSelection = GeneralDoubleOracle.PlayerSelection.BOTH;
            } else if (alg.equalsIgnoreCase("DO-SA")) {
                GeneralDoubleOracle.playerSelection = GeneralDoubleOracle.PlayerSelection.SINGLE_ALTERNATING;
            } else if (alg.equalsIgnoreCase("DO-SI")) {
                GeneralDoubleOracle.playerSelection = GeneralDoubleOracle.PlayerSelection.SINGLE_IMPROVED;
            }
            if (domain.equalsIgnoreCase("BP"))
                GeneralDoubleOracle.runBP();
            else if (domain.equalsIgnoreCase("GP"))
                GeneralDoubleOracle.runGenericPoker();
            else if (domain.equalsIgnoreCase("RG"))
                GeneralDoubleOracle.runRandomGame();
        } else if (alg.equals("UDO")) {
            UnprunningDoubleOracle.main(null);
        } else if (alg.equalsIgnoreCase("LP")) {
            if (domain.equalsIgnoreCase("BP"))
                FullSequenceEFG.runBPG();
            else if (domain.equalsIgnoreCase("GP"))
                FullSequenceEFG.runGenericPoker();
            else if (domain.equalsIgnoreCase("RG"))
                FullSequenceEFG.runRandomGame();
        } else throw new IllegalArgumentException("Illegal algorithm: " + alg);
    }
}
