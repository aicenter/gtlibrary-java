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


package cz.agents.gtlibrary.domain.goofspiel;

import cz.agents.gtlibrary.iinodes.LinkedListSequenceImpl;
import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.HighQualityRandom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class GSGameInfo implements GameInfo {

    public static final Player FIRST_PLAYER = new PlayerImpl(0);
    public static final Player SECOND_PLAYER = new PlayerImpl(1);
    public static final Player NATURE = new PlayerImpl(2);

    public static final Player[] ALL_PLAYERS = {FIRST_PLAYER, SECOND_PLAYER, NATURE};
    public static int[] CARDS_FOR_PLAYER = new int[]{1, 2, 3, 4, 5/*, 6/*, 7, 8, 9, 10, 11, 12, 13*/};
    public static long seed = 1;
    public static boolean useFixedNatureSequence = true;
    public static boolean regenerateCards = false;
    public static int depth = CARDS_FOR_PLAYER.length;
    public static boolean BINARY_UTILITIES = false;

    public static Sequence natureSequence;


    public GSGameInfo() {
        if (regenerateCards) {
            CARDS_FOR_PLAYER = new int[depth];
            //for (int i=1; i<=depth; i++) {
            //    CARDS_FOR_PLAYER[i-1]=i;
            for (int i = 0; i < depth; i++) {
                CARDS_FOR_PLAYER[i] = i;
            }
        }

        natureSequence = createRandomSequence();
    }

    @Override
    public double getMaxUtility() {

        if (BINARY_UTILITIES)
            return 1;
        else {
            double value = 0;

            for (int cardValue : CARDS_FOR_PLAYER) {
                value += cardValue;
            }
            return value;
        }
    }

    private Sequence createRandomSequence() {
        ArrayList<Action> actions = new ArrayList(GSGameInfo.CARDS_FOR_PLAYER.length);
        for (int card : GSGameInfo.CARDS_FOR_PLAYER)
            actions.add(new GoofSpielAction(card, GSGameInfo.NATURE, null));
        if (GSGameInfo.useFixedNatureSequence && GSGameInfo.seed == 1) {
            Collections.reverse(actions);
        } else {
            Collections.shuffle(actions, new HighQualityRandom(GSGameInfo.seed));
        }
        Sequence natureSequence = new LinkedListSequenceImpl(GSGameInfo.NATURE);
        natureSequence.addAllAsLast(actions);
        return natureSequence;
    }

    @Override
    public Player getFirstPlayerToMove() {
        return NATURE;
    }

    @Override
    public Player getOpponent(Player player) {
        return player.equals(FIRST_PLAYER) ? SECOND_PLAYER : FIRST_PLAYER;
    }

    @Override
    public String getInfo() {
        return "Goofspiel, cards: " + Arrays.toString(CARDS_FOR_PLAYER) + ", fixed nature sequence: " + useFixedNatureSequence;
    }

    @Override
    public int getMaxDepth() {
        return 3 * CARDS_FOR_PLAYER.length;
    }

    @Override
    public Player[] getAllPlayers() {
        return ALL_PLAYERS;
    }

    @Override
    public double getUtilityStabilizer() {
        return 1;
    }
}
