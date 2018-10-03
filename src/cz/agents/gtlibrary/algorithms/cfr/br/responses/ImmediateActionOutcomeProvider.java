package cz.agents.gtlibrary.algorithms.cfr.br.responses;

import cz.agents.gtlibrary.interfaces.Action;

/**
 * Created by Jakub Cerny on 06/06/2018.
 */
public interface ImmediateActionOutcomeProvider {

    public double getImmediateRewardForAction(Action action);

    public double getImmediateReward();
    public double getImmediateCost();
}
