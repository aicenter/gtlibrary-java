/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.nfg.MDP.cfr;

import cz.agents.gtlibrary.nfg.MDP.implementations.MDPStateActionMarginal;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPAction;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPState;
import cz.agents.gtlibrary.utils.FixedSizeMap;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.Triplet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author viliam
 */
public class CFRMDPState {
    MDPState mdpState;
    MDPStateActionMarginal[] allActions;
    FixedSizeMap<CFRMDPState, Double>[] outcomes;
    
    //state, action ID, utility of the related actions in theopponent's MDP
    ArrayList<Triplet<CFRMDPState, Integer, Double>> relatedOpActions[];
    double stateProb;
    double stateValue;
    double[] regrets;
    double[] curStrategy;
    double[] meanStrategy;
    
    public CFRMDPState(MDPState mdpState, List<MDPAction> allActions) {
        this.mdpState = mdpState;
        this.allActions = new MDPStateActionMarginal[allActions.size()];
        this.outcomes = new FixedSizeMap[allActions.size()];
        int i=0;
        for (MDPAction a : allActions) this.allActions[i++] = new MDPStateActionMarginal(mdpState, a);
        regrets = new double[allActions.size()];
        curStrategy = new double[allActions.size()];
        Arrays.fill(curStrategy, 1.0/allActions.size());
        meanStrategy = new double[allActions.size()];
        relatedOpActions = new ArrayList[allActions.size()];
        for (i=0;i<allActions.size();i++) relatedOpActions[i] = new ArrayList<>();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.mdpState);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CFRMDPState other = (CFRMDPState) obj;
        if (!Objects.equals(this.mdpState, other.mdpState)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return mdpState.toString();
    }
    
    
    
}
