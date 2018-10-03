package cz.agents.gtlibrary.algorithms.cfr.br.responses;

/**
 * Created by Jakub Cerny on 05/06/2018.
 */
public interface AbstractActionProvider {

    public Object getSituationAbstraction();
    public Object getActionAbstraction();
    public double getMaximumActionUtility();
    public double[] getAllPossibleOutcomes();
}
