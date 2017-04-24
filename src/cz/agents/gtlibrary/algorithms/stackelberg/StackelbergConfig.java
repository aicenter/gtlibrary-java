/*
Copyright 2014 Faculty of Electrical Engineering at CTU in Prague

This file is part of Game Theoretic Library.

Game Theoretic Library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Game Theoretic Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.*/


package cz.agents.gtlibrary.algorithms.stackelberg;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.GenSumSequenceFormConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.multiplelps.FeasibilitySequenceFormLP;
import cz.agents.gtlibrary.algorithms.stackelberg.multiplelps.rpiterator.DepthPureRealPlanIterator;
import cz.agents.gtlibrary.algorithms.stackelberg.multiplelps.rpiterator.NoCutDepthPureRealPlanIterator;
import cz.agents.gtlibrary.algorithms.stackelberg.multiplelps.rpiterator.PureRealPlanIterator;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.FixedSizeMap;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 12/2/13
 * Time: 2:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class StackelbergConfig extends GenSumSequenceFormConfig {

    public static boolean USE_FEASIBILITY_CUT = false;
    public static boolean USE_UPPERBOUND_CUT = true;

    protected Set<GameState> allLeafs = new HashSet<>();
    private GameState rootState;

    public StackelbergConfig(GameState rootState) {
        super();
        this.rootState = rootState;
    }

    public PureRealPlanIterator getIterator(Player player, Expander<SequenceInformationSet> expander, FeasibilitySequenceFormLP solver) {
//        return new WidthPureRealPlanIterator(player, this, expander, solver);
        if (USE_UPPERBOUND_CUT)
            return new DepthPureRealPlanIterator(player, this, expander, solver);
        else
            return new NoCutDepthPureRealPlanIterator(player, this, expander, solver);
    }

    public GameState getRootState() {
        return rootState;
    }

    public void setUtility(GameState leaf, Double[] utility) {
        if (actualNonZeroUtilityValuesInLeafs.containsKey(leaf)) {
            assert (Arrays.equals(actualNonZeroUtilityValuesInLeafs.get(leaf), utility));
            return; // we have already stored this leaf
        }
        boolean allZeros = true;

        for (Player p : leaf.getAllPlayers()) {
            if (utility[p.getId()] != 0) {
                allZeros = false;
                break;
            }
        }
        allLeafs.add(leaf);
        if (allZeros) {
            return; // we do not store zero-utility
        }

        FixedSizeMap<Player, Sequence> activePlayerMap = createActivePlayerMap(leaf);
        Double[] existingUtility = utility;

        if (utilityForSequenceCombination.containsKey(activePlayerMap)) {
            Double[] storedUV = utilityForSequenceCombination.get(activePlayerMap);
            for (int i = 0; i < existingUtility.length; i++) {
                existingUtility[i] += storedUV[i];
            }
        }

        updateProbabilitiesForSeqComb(leaf, activePlayerMap);

        actualNonZeroUtilityValuesInLeafs.put(leaf, utility);
        utilityForSequenceCombination.put(activePlayerMap, existingUtility);
    }

    public Set<GameState> getAllLeafs() {
        return allLeafs;
    }
}
