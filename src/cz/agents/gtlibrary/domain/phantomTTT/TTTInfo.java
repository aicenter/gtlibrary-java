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


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.domain.phantomTTT;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.HashSet;
import java.util.LinkedList;


/**
 *
 * @author vilo
 */
public class TTTInfo implements GameInfo{

    public static boolean useDomainDependentExpander = true;

    public static final Player XPlayer = new PlayerImpl(0,"x");
    public static final Player OPlayer = new PlayerImpl(1,"o");
    
    public static Player[] players = new Player[] { XPlayer, OPlayer};
    
    @Override
    public Player getFirstPlayerToMove() {
        return XPlayer;
    }

    @Override
    public int getMaxDepth() {
        return Integer.MAX_VALUE;
        //return 2;
    }

    @Override
    public String getInfo() {
        return "************ Phantom Tic Tac Toe ************* \n ";
    }

    @Override
    public double getMaxUtility() {
        return (TTTState.skewed ? 3 : 1.0);
    }

    @Override
    public Player getOpponent(Player player) {
        return player.equals(XPlayer) ? OPlayer : XPlayer;
    }

    @Override
    public Player[] getAllPlayers() {
        return players;
    }

    public static void calculateSequences(TTTState rootState, TTTExpander<SequenceInformationSet> expander) {
        LinkedList<TTTState> queue = new LinkedList<TTTState>();
        HashSet<Long> sequences = new HashSet<Long>();

        queue.add(rootState);

        while (queue.size() > 0) {
            TTTState currentState = queue.removeLast();

//            for (Player p : rootState.getAllPlayers())
            Player p = rootState.getAllPlayers()[0];
            if (currentState.isGameEnd() || !currentState.getPlayerToMove().equals(p)) {
                Sequence s = currentState.getSequenceFor(p);

                long hash = 1;//creates a bitmap of successful actions
                for (Action a : s.getAsList()){
                    hash <<= 1;
                    hash |= currentState.getSymbol(((TTTAction)a).fieldID) == p.getName().charAt(0) ? 1 : 0;
                }
                hash >>= 3;
                for (Action a : s.getAsList()){
                    hash <<= 4;
                    hash |= ((TTTAction)a).fieldID;
                }

                if (sequences.add(hash))
                    if (sequences.size() % 100000 == 0)
                        System.out.println("Current Size:"+sequences.size());
            }

            for (Action action : expander.getActions(currentState)) {
                queue.add((TTTState)currentState.performAction(action));
            }
        }

        System.out.println("final size: Second Player Sequences: " + sequences.size());
        System.exit(0);
    }

    @Override
    public double getUtilityStabilizer() {
        return 1;
    }
}
