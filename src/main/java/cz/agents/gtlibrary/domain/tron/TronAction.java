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


package cz.agents.gtlibrary.domain.tron;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;

public class TronAction extends ActionImpl implements Comparable<TronAction> {

    // lanctot: Note: maybe need to change this
    private static final long serialVersionUID = -3504137065821329745L;

    public static int VAL_N = 1; 
    public static int VAL_E = 2; 
    public static int VAL_S = 3; 
    public static int VAL_W = 4; 

    private final int value;

    private Player player;
    private int hashCode = -1;

    public TronAction(int value, Player player, InformationSet informationSet) {
        super(informationSet);
        this.value = value;
        this.player = player;
    }

    @Override
    public void perform(GameState gameState) {
        if (player.equals(TronGameInfo.FIRST_PLAYER)) {
            ((TronGameState) gameState).performFirstPlayerAction(this);
        } else if (player.equals(TronGameInfo.SECOND_PLAYER)) {
            ((TronGameState) gameState).performSecondPlayerAction(this);
        } else {
            ((TronGameState) gameState).performNatureAction(this);
        }
    }

    public int getValue() {
        return value;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public int compareTo(TronAction action) {
        return value - action.getValue();
    }


    @Override
    public int hashCode() {
        if (hashCode != -1)
            return hashCode;
        final int prime = 53; // assumes startCoints <= 53

        hashCode = 1;
        hashCode = prime * hashCode + ((player == null) ? 0 : player.hashCode());
        hashCode = prime * hashCode + ((informationSet == null) ? 0 : informationSet.hashCode());
        hashCode = prime * hashCode + value;
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        TronAction other = (TronAction) obj;
        if (player == null) {
            if (other.player != null)
                return false;
        } else if (!player.equals(other.player))
            return false;
        if (value != other.value)
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        String descIS = "";
                /*try {
                    TronGameState fistState = (TronGameState) informationSet.getAllStates().iterator().next();
                    //descIS += fistState.getISKeyForPlayerToMove().getLeft() + ", ";
                    //descIS += fistState.getPlayerScore()[0] + "-" + fistState.getPlayerScore()[1];
                    //descIS += fistState.getP1 + "-" + fistState.getPlayerScore()[1];
                    descIS = "first state";
                } catch (NullPointerException ex){
                    //intentionally empty
                }*/

        //builder.append("[" + reward + ", " + descIS + ", " + player);
        builder.append("[" + value + ", " + player);
        builder.append("]");
        return builder.toString();
    }

}
