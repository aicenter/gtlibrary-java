package algorithms.mccr;

import cz.agents.gtlibrary.algorithms.mccr.MCCRAlgorithm;
import cz.agents.gtlibrary.algorithms.mccr.MCCR_CFV_Experiments;
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStratDist;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.interfaces.Action;
import org.junit.Test;

import java.util.Collection;
import java.util.Random;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class MCCRTest extends MCCR_CFV_Experiments {

    public MCCRTest(Long seed) {
        super(seed);
    }

    @Test
    public void testGamesRpIsSameInAllHistoriesWithinInfoSets() {
        checkDomain("IIGS", new String[]{"0", "4", "true", "true"});
        checkDomain("IIGS", new String[]{"0", "5", "true", "true"});
        checkDomain("RPS", new String[]{"0"});
        checkDomain("LD", new String[]{"1", "1", "6"});
    }


    private void checkDomain(String domain, String[] params) {
        for (long seed = 0; seed < 10; seed++) {
            MCCRTest exp = new MCCRTest(seed);
            exp.handleDomain(domain, params);
            exp.loadGame(domain, new Random(seed));
            exp.expander.getAlgorithmConfig().createInformationSetFor(exp.rootState);

            MCCRAlgorithm alg = new MCCRAlgorithm(exp.rootState, exp.expander, 0.6);
            alg.runStepStateful(100000); // root

            for (int i = 0; i < 10; i++) {
                Action action = alg.runStepStateful(100000); // first gadget
                if (action == null) break;

                Collection<MCTSInformationSet> infoSets = ((MCTSConfig) exp.expander.getAlgorithmConfig())
                        .getAllInformationSets().values();
                for (MCTSInformationSet infoSet : infoSets) {
                    Double rp = null;
                    for (InnerNode node : infoSet.getAllNodes()) {
                        if (rp == null) {
                            rp = node.getReachPr();
                        }

                        assertEquals(rp, node.getReachPr(), 1e-7);
                        assertTrue(node.getReachPr() <= 1.0);
                        assertTrue(node.getReachPr() >= 0.0);
                    }
                }
            }
        }
    }

    @Test
    public void testAlgorithmIsCorrectlySeeded() {
        for (int seed = 0; seed < 10; seed++) {
            assertEquals(runAlgGetHashCode(seed), runAlgGetHashCode(seed));
        }
    }

    private long runAlgGetHashCode(int seed) {
        long hc = 0;

        String domain = "IIGS";
        String[] params = new String[]{"0", "4", "true", "true"};

        MCCRTest exp = new MCCRTest(0L);
        exp.handleDomain(domain, params);
        exp.loadGame(domain, new Random(seed));
        exp.expander.getAlgorithmConfig().createInformationSetFor(exp.rootState);

        MCCRAlgorithm alg = new MCCRAlgorithm(exp.rootState, exp.expander, 0.6);
        alg.runIterations(1000, 1000);

        Collection<MCTSInformationSet> infoSets = ((MCTSConfig) exp.expander.getAlgorithmConfig())
                .getAllInformationSets().values();
        for (MCTSInformationSet infoSet : infoSets) {
            OOSAlgorithmData  data = ((OOSAlgorithmData) infoSet.getAlgorithmData());
            if(data!=null) {
                hc += data.hashCode();
            }
        }

        return hc;
    }

}
