package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff.cfr.regretcheck;

import cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff.cfr.LimitedMemoryMaxRegretIRCFR;

import java.util.Arrays;

public class SquareRootCheck implements RegretCheck {
    @Override
    public boolean isAboveBound(double[] regrets, LimitedMemoryMaxRegretIRCFR algorithm) {
        double maxRegret = Arrays.stream(regrets).max().getAsDouble()/(algorithm.getIteration());

//        if(maxRegret > 1e-4)
//            System.err.println(maxRegret + " vs " + Math.sqrt(regrets.length)/Math.sqrt(algorithm.getIteration()) + " now stored for " + algorithm.getCurrentSampleIterations());
        return maxRegret > Math.sqrt(regrets.length)/Math.pow(algorithm.getIteration(), 0.5);
    }
}
