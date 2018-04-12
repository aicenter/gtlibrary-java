package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff.cfr.regretcheck;

import cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff.cfr.LimitedMemoryMaxRegretIRCFR;

public interface RegretCheck {
    public boolean isAboveBound(double[] regrets, LimitedMemoryMaxRegretIRCFR algorithm);
}
