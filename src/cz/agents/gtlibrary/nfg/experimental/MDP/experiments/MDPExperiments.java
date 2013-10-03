package cz.agents.gtlibrary.nfg.experimental.MDP.experiments;

import cz.agents.gtlibrary.domain.bpg.BPGGameInfo;
import cz.agents.gtlibrary.domain.phantomTTT.TTTInfo;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.nfg.experimental.MDP.DoubleOracleCostPairedMDP;
import cz.agents.gtlibrary.nfg.experimental.MDP.FullCostPairedMDP;
import cz.agents.gtlibrary.nfg.experimental.MDP.McMahan.McMahanDoubleOracle;
import cz.agents.gtlibrary.nfg.experimental.MDP.SingleOracleCostPairedMDP;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.oracle.MDPFristBetterResponse;
import cz.agents.gtlibrary.nfg.experimental.domain.bpg.BPConfig;
import cz.agents.gtlibrary.nfg.experimental.domain.transitgame.TGConfig;
import cz.agents.gtlibrary.utils.HighQualityRandom;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 9/18/13
 * Time: 7:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class MDPExperiments {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Missing Arguments: MDPExperiments {DO|LP} {BP|TG} [algorithm/domain parameters].");
            System.exit(-1);
        }
        String alg = args[0];
        MDPExperiments exp = new MDPExperiments();
        exp.handleDomain(args);
        exp.runAlgorithm(alg, args[1]);
    }

    public void handleDomain(String[] args) {
        if (args[1].equalsIgnoreCase("BP")) {  // Border Patrolling
            if (args.length < 5) {
                throw new IllegalArgumentException("Illegal domain arguments count. 3 are required {DEPTH} {GRAPH} {FLAG_PROB}");
            }
            int depth = new Integer(args[2]);
            BPGGameInfo.graphFile = args[3];
            BPConfig.FLAG_PROB = new Double(args[4]);
            BPConfig.MAX_TIME_STEP = depth;
        } else if (args[1].equalsIgnoreCase("TG")) { // Transit Game
            if (args.length < 5) {
                throw new IllegalArgumentException("Illegal random game domain arguments count. 3 are required {DEPTH} {LENGTH OF THE GRID} {WIDTH OF THE GRID}");
            }
            TGConfig.MAX_TIME_STEP = new Integer(args[2]);
            TGConfig.LENGTH_OF_GRID = new Integer(args[3]);
            TGConfig.WIDTH_OF_GRID = new Integer(args[4]);
        } else throw new IllegalArgumentException("Illegal domain: " + args[1]);
    }

    public void runAlgorithm(String alg, String domain) {
        if (alg.startsWith("DO")) {
            if (alg.startsWith("DOP")) {
                MDPFristBetterResponse.PRUNING = true;
            } else if (alg.startsWith("DON")) {
                MDPFristBetterResponse.PRUNING = false;
            } else throw new IllegalArgumentException("Illegal algorithm: " + alg);

            if (alg.endsWith("1")) {
                MDPFristBetterResponse.USE_FIRST_BT = true;
                if (alg.endsWith("R1")) {
                    DoubleOracleCostPairedMDP.USE_ROBUST_BR = true;
                } else DoubleOracleCostPairedMDP.USE_ROBUST_BR = false;
            } else {
                MDPFristBetterResponse.USE_FIRST_BT = false;
                if (alg.endsWith("R")) {
                    DoubleOracleCostPairedMDP.USE_ROBUST_BR = true;
                } else DoubleOracleCostPairedMDP.USE_ROBUST_BR = false;
            }


            if (domain.equalsIgnoreCase("BP")) {
                DoubleOracleCostPairedMDP.runBPG();
            } else if (domain.equalsIgnoreCase("TG")) {
                DoubleOracleCostPairedMDP.runTG();
            }
        } else if (alg.startsWith("SO")) {
            if (alg.startsWith("SOP")) {
                MDPFristBetterResponse.PRUNING = true;
            } else if (alg.startsWith("SON")) {
                MDPFristBetterResponse.PRUNING = false;
            } else throw new IllegalArgumentException("Illegal algorithm: " + alg);

            if (alg.endsWith("1")) {
                MDPFristBetterResponse.USE_FIRST_BT = true;
                if (alg.endsWith("R1")) {
                    SingleOracleCostPairedMDP.USE_ROBUST_BR = true;
                } else SingleOracleCostPairedMDP.USE_ROBUST_BR = false;
            } else {
                MDPFristBetterResponse.USE_FIRST_BT = false;
                if (alg.endsWith("R")) {
                    SingleOracleCostPairedMDP.USE_ROBUST_BR = true;
                } else SingleOracleCostPairedMDP.USE_ROBUST_BR = false;
            }
            if (domain.equalsIgnoreCase("BP")) {
                SingleOracleCostPairedMDP.runBPG();
            } else if (domain.equalsIgnoreCase("TG")) {
                SingleOracleCostPairedMDP.runTG();
            }
        } else if (alg.startsWith("MC")) {
            if (alg.startsWith("MCR")) {
                McMahanDoubleOracle.REMOVE_STRATEGIES = true;
            } else {
                McMahanDoubleOracle.REMOVE_STRATEGIES = false;
            }
            if (domain.equalsIgnoreCase("BP"))
                McMahanDoubleOracle.runBPG();
            else if (domain.equalsIgnoreCase("TG"))
                McMahanDoubleOracle.runTG();
        } else if (alg.startsWith("LP")) {
            if (domain.equalsIgnoreCase("BP"))
                FullCostPairedMDP.runBPG();
            else if (domain.equalsIgnoreCase("TG"))
                FullCostPairedMDP.runTG();
        }

    }

}