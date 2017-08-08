/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.agents.gtlibrary.algorithms.mcts.selectstrat;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.interfaces.Action;
import java.util.List;
import java.util.Random;

/**
 *
 * @author vilo
 */
public class ConjectureFactory implements BackPropFactory {
    BackPropFactory fact;

    public ConjectureFactory(BackPropFactory fact) {
        this.fact = fact;
    }
    
    @Override
    public Selector createSelector(List<Action> actions) {
        if (((MCTSInformationSet)actions.get(0).getInformationSet()).getPlayersHistory().size()==0)
            return new ConjuctureSelector(fact.createSelector(actions), actions.size());
        else return fact.createSelector(actions);
    }

    @Override
    public Selector createSelector(int N) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Random getRandom() {
        return fact.getRandom();
    }
    
    
}
