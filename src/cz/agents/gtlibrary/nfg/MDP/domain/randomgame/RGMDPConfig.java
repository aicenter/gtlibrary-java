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


package cz.agents.gtlibrary.nfg.MDP.domain.randomgame;

import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPConfigImpl;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPStateActionMarginal;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPState;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 9/12/13
 * Time: 2:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class RGMDPConfig extends MDPConfigImpl {

    public final static int MAX_UTILITY = 2;
    public final static int BF_ACTIONS = 3;
    public final static int STEPS = 5;
//    public final static double[] NATURE_PROB = {0.7,0.15,0.1,0.05};
//    public final static double[] NATURE_PROB = {0.9,0.1};
    public final static double[] NATURE_PROB = {0.8,0.0,0.2};
    public final static int BF_NATURE = NATURE_PROB.length;
    public final static boolean onlySameStepUtility = true;
    public final static int SHIFT = (int)Math.ceil(Math.log(RGMDPConfig.BF_ACTIONS)/Math.log(2))+1;

    public RGMDPConfig() {
        allPlayers = new ArrayList<Player>(2);
        allPlayers.add(new PlayerImpl(0));
        allPlayers.add(new PlayerImpl(1));
    }

    @Override
    public List<Player> getAllPlayers() {
        return allPlayers;
    }

    @Override
    public double getBestUtilityValue(Player player) {
        if (player.getId() == 0)
            return MAX_UTILITY;
        else return -MAX_UTILITY;
    }

    @Override
    public double getUtility(MDPStateActionMarginal firstPlayerAction, MDPStateActionMarginal secondPlayerAction) {
        if (firstPlayerAction.getState().isRoot() || secondPlayerAction.getState().isRoot())
            return 0;
        if (onlySameStepUtility && ((RGMDPState)firstPlayerAction.getState()).getStep() != ((RGMDPState)secondPlayerAction.getState()).getStep())
            return 0d;

        RGMDPState pl1state = null;
        RGMDPState pl2state = null;
        RGMDPAction pl1action = null;
        RGMDPAction pl2action = null;

        if (firstPlayerAction.getPlayer().getId() == 0) {
            pl1state = (RGMDPState)firstPlayerAction.getState();
            pl1action = (RGMDPAction)firstPlayerAction.getAction();
            pl2state = (RGMDPState)secondPlayerAction.getState();
            pl2action = (RGMDPAction)secondPlayerAction.getAction();
        } else {
            pl2state = (RGMDPState)firstPlayerAction.getState();
            pl2action = (RGMDPAction)firstPlayerAction.getAction();
            pl1state = (RGMDPState)secondPlayerAction.getState();
            pl1action = (RGMDPAction)secondPlayerAction.getAction();
        }

        HashCodeBuilder hb = new HashCodeBuilder(17, 31);
        hb.append(pl1state.getID());
        hb.append(pl1action.getID());
        hb.append(pl2state.getID());
        hb.append(pl2action.getID());
        double utility = (new HighQualityRandom(hb.toHashCode()).nextInt(MAX_UTILITY * 2+1))-MAX_UTILITY;
        return utility;
    }

    @Override
    public MDPState getDomainRootState(Player player) {
        return new RGMDPState(player);
    }

    public static int getMaxSteps() {
        return STEPS;
    }
}
