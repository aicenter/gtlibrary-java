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

package cz.agents.gtlibrary.domain.randomgame;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.utils.Pair;

/**
 *
 * @author Vilo
 */
public class SimSystematicGS extends SimRandomGameState {
   public static int gameCode = 423;
   public static double[] utilities = null;

   public SimSystematicGS(SimRandomGameState gameState) {
        super(gameState);
    }

    public SimSystematicGS() {
        super();
    }

      @Override
    public GameState copy() {
        return new SimSystematicGS(this);
    }


    static int D = 3;
    static int leafCodes = D*D*(D*D+1)/2;
    static int columnBase = leafCodes*leafCodes;

    @Override
    public double[] getUtilities() {
        int index = 0;
        if (((RandomGameAction)getHistory().getSequenceOf(getAllPlayers()[0]).getFirst()).getOrder() == 1) index += 8;
        if (((RandomGameAction)getHistory().getSequenceOf(getAllPlayers()[1]).getFirst()).getOrder() == 1) index += 4;
        if (((RandomGameAction)getHistory().getSequenceOf(getAllPlayers()[0]).getLast()).getOrder() == 1) index += 2;
        if (((RandomGameAction)getHistory().getSequenceOf(getAllPlayers()[1]).getLast()).getOrder() == 1) index += 1;
        return new double[]{utilities[index], -utilities[index]};
    }

    public static double[] computeUtilities(int gameCode){
        assert RandomGameInfo.MAX_DEPTH == 2 && RandomGameInfo.MAX_BF == 2;
        Pair<Integer, Integer> p = code2pair(gameCode, columnBase);
        int secondColumn = p.getRight();
        int firstColumn = p.getLeft();
        assert (secondColumn >= firstColumn);
        double[] out = new double[16];
        fillColumn(firstColumn, 0, out);
        fillColumn(secondColumn, 8, out);
        return out;
    }

    static void fillColumn(int colCode,int i, double[] u){
        int secondLeaf  = colCode % leafCodes;;
        int firstLeaf = colCode / leafCodes;
        fillLeaf(i, u, firstLeaf);
        fillLeaf(i+4, u, secondLeaf);
    }

    static void fillLeaf(int i, double[] u, int leafCode){
        Pair<Integer, Integer> p = code2pair(leafCode, (D*D));
        int col1 = p.getLeft();
        int col2 = p.getRight();
        assert  (col1 <= col2);
        u[i+0] = ((double)(col1 / D))/(D-1);
        u[i+1] = ((double)(col1 % D))/(D-1);
        u[i+2] = ((double)(col2 / D))/(D-1);
        u[i+3] = ((double)(col2 % D))/(D-1);
    }
    
    //TODO: substitute by formula
    public static Pair<Integer, Integer> code2pair(int code, int base){
        int first = 0;
        int toRemove = base;
        while (code >= toRemove){
            code -= toRemove;
            toRemove--;
            first++;
        }
        return new Pair<Integer, Integer>(first,first+code);
    }
}
