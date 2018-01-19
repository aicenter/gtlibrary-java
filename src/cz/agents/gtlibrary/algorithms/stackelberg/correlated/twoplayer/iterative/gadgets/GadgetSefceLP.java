package cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative.gadgets;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.interfaces.*;

import java.util.HashSet;
import java.util.Map;

/**
 * Created by Jakub Cerny on 04/12/2017.
 */
public class GadgetSefceLP implements Solver {

    HashSet<SequenceInformationSet> gadgetRoots;



    /*
        Making roots of gadgets from a given IS.
     */
    protected void makeTL(SequenceInformationSet set){

    }


    @Override
    public double calculateLeaderStrategies(AlgorithmConfig algConfig, Expander expander) {
        return 0;
    }

    @Override
    public Double getResultForPlayer(Player leader) {
        return null;
    }

    @Override
    public Map<Sequence, Double> getResultStrategiesForPlayer(Player player) {
        return null;
    }

    @Override
    public long getOverallConstraintGenerationTime() {
        return 0;
    }

    @Override
    public long getOverallConstraintLPSolvingTime() {
        return 0;
    }

    @Override
    public String getInfo() {
        return "Complete Sefce solver with gadgets.";
    }
}
