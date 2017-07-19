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

import cz.agents.gtlibrary.algorithms.sequenceform.FullSequenceEFG;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.GeneralDoubleOracle;
import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.io.GambitEFG;

import java.io.PrintStream;
import java.util.BitSet;
import java.util.Map;

/**
 * Phantom tic tac toe game state.
 */

public class TTTState extends GameStateImpl {

    private static final long serialVersionUID = -8229777952409518678L;

    //bitmap with 4 bits per board field. First two are the symbol: 00 -> ' ', 10 -> 'o', 11 -> 'x'. The next two bits are if the field has been tries out by x player and o player.
    //could be reimplemented with 9x3 fields and a primitive type
    public BitSet s = new BitSet(36);
    public char toMove = 'x';
    public byte moveNum = 0;
    int hashCode;

    public char getOpponent() {
        return (toMove == 'x' ? 'o' : 'x');
    }

    public char getSymbol(int field) {
        if (s.get(4 * field) == false) {
            return ' ';
        } else if (s.get(4 * field + 1) == false) {
            return 'o';
        } else return 'x';
    }

    public void setSymbol(int field, char symbol) {
        if (symbol == ' ') {
            s.set(4 * field, false);
            s.set(4 * field + 1, false);
        } else {
            s.set(4 * field, true);
            if (symbol == 'o') {
                s.set(4 * field + 1, false);
            } else {
                s.set(4 * field + 1, true);
            }
        }
    }

    public boolean getTried(char p, int field) {
        return s.get(4 * field + (p == 'x' ? 2 : 3));
    }

    public void setTried(char p, int field) {
        s.set(4 * field + (p == 'x' ? 2 : 3), true);
    }

    private void deleteTried(char p, int field) {
        s.set(4 * field + (p == 'x' ? 2 : 3), false);
    }

    public TTTState() {
        this(null, 'x', (byte) 0);
    }


    public TTTState(BitSet s, char toMove, byte moveNum) {
        super(TTTInfo.players);
        if (s != null) this.s = (BitSet) s.clone();
        this.toMove = toMove;
        this.moveNum = moveNum;
    }

    public TTTState(TTTState state) {
        super(state);
        if (state.s != null) this.s = (BitSet) state.s.clone();
        this.toMove = state.toMove;
        this.moveNum = state.moveNum;
    }

    @Override
    public Player getPlayerToMove() {
        if (toMove == 'x') return TTTInfo.XPlayer;
        else return TTTInfo.OPlayer;
    }

    @Override
    public boolean isPlayerToMoveNature() {
        return false;
    }

    private static double[] xWin = new double[]{1.0, -1.0};
    private static double[] oWin = new double[]{-1.0, 1.0};
    private static double[] xLargeWin = new double[]{3.0, -3.0};
    private static double[] oLargeWin = new double[]{-3.0, 3.0};
    public static boolean skewed = false;
    public static boolean forceFirstMoves = false;

    @Override
    public double[] getUtilities() {
        if (skewed && ((getSymbol(0) == getSymbol(1) && getSymbol(1) == getSymbol(2))
                || (getSymbol(1) == getSymbol(4) && getSymbol(4) == getSymbol(7)))) {
            if (getSymbol(1) == 'x') return xLargeWin;
            if (getSymbol(1) == 'o') return oLargeWin;
        }
        if ((getSymbol(0) == getSymbol(1) && getSymbol(1) == getSymbol(2))
                || (getSymbol(0) == getSymbol(3) && getSymbol(3) == getSymbol(6))
                || (getSymbol(0) == getSymbol(4) && getSymbol(4) == getSymbol(8))) {
            if (getSymbol(0) == 'x') return xWin;
            if (getSymbol(0) == 'o') return oWin;
        }
        if ((getSymbol(8) == getSymbol(5) && getSymbol(5) == getSymbol(2))
                || (getSymbol(8) == getSymbol(7) && getSymbol(7) == getSymbol(6))) {
            if (getSymbol(8) == 'x') return xWin;
            if (getSymbol(8) == 'o') return oWin;
        }
        if ((getSymbol(4) == getSymbol(1) && getSymbol(4) == getSymbol(7))
                || (getSymbol(4) == getSymbol(3) && getSymbol(4) == getSymbol(5))) {
            if (getSymbol(4) == 'x') return xWin;
            if (getSymbol(4) == 'o') return oWin;
        }
        if ((getSymbol(2) == getSymbol(4) && getSymbol(4) == getSymbol(6))) {
            if (getSymbol(2) == 'x') return xWin;
            if (getSymbol(2) == 'o') return oWin;
        }
        return new double[]{0.0, 0.0};
    }

    @Override
    public boolean isGameEnd() {
        double[] u = getUtilities();
        //is finished already
        if (u[0] != 0.0) return true;
        //has an empty field
        for (int i = 0; i < 9; i++) if (getSymbol(i) == ' ') return false;
        return true;
    }

    @Override
    public GameState copy() {
        return new TTTState(this);
    }

    @Override
    public double getProbabilityOfNatureFor(Action action) {
        assert false;
        return -1;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TTTState other = (TTTState) obj;
        if (toMove != other.toMove) {
            return false;
        }
        if (!s.equals(other.s)) {
            return false;
        }
        if (!getHistory().equals(other.getHistory())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        if (hashCode == -1)
            hashCode = s.hashCode();
        return hashCode;
    }

    @Override
    public String toString() {
        String out = "";
        for (int i = 0; i < 9; i++) {
            if (i % 3 == 0) out += '|';
            char symbol = getSymbol(i);
            if (symbol != ' ') {
                if (getTried(symbol == 'x' ? 'o' : 'x', i)) {
                    symbol -= 'a' - 'A';
                }
            }
            out += symbol;
        }
        out += '|';
        return out;
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        int hash;
        if (isGameEnd()) {
            hash = 0;
        } else {
            hash = 1;//creates a bitmap of successful actions
            for (Action a : history.getSequenceOf(getPlayerToMove())) {
                hash <<= 1;
                hash |= getSymbol(((TTTAction) a).fieldID) == toMove ? 1 : 0;
            }
        }
        return new PerfectRecallISKey(hash, history.getSequenceOf(getPlayerToMove()));
    }


    public static Map<Player, Map<Sequence, Double>> runDO() {
        GameState rootState = new TTTState();
        GameInfo gameInfo = new TTTInfo();
        DoubleOracleConfig<DoubleOracleInformationSet> algConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
        Expander<DoubleOracleInformationSet> expander = new TTTExpander<DoubleOracleInformationSet>(algConfig);
        GeneralDoubleOracle doefg = new GeneralDoubleOracle(rootState, expander, gameInfo, algConfig);

        return doefg.generate(null);
    }


    public static Map<Player, Map<Sequence, Double>> runSF() {
        GameState rootState = new TTTState();
        GameInfo gameInfo = new TTTInfo();
        SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
        algConfig.addStateToSequenceForm(rootState);
        FullSequenceEFG efg = new FullSequenceEFG(rootState, new TTTExpander<SequenceInformationSet>(algConfig), gameInfo, algConfig);

        return efg.generate();
    }

    public static void saveGame() {
        GameState rootState = new TTTState();
        SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
        algConfig.addStateToSequenceForm(rootState);

        new GambitEFG().write("ttt.efg", rootState, new TTTExpander<SequenceInformationSet>(algConfig));
    }


    public static void main(String[] args) {
        Map<Player, Map<Sequence, Double>> realizationPlans = runDO();

        PrintStream out;
        try {
            out = new PrintStream("x.txt");
            for (Sequence sequence : realizationPlans.get(TTTInfo.XPlayer).keySet()) {
                double prob = realizationPlans.get(TTTInfo.XPlayer).get(sequence);
                if (prob > 0) {
                    out.print("[");
                    TTTAction prevA = null;
                    for (Action a : sequence) {
                        if (prevA != null) {
                            TTTState state = (TTTState) a.getInformationSet().getAllStates().iterator().next();
                            if (state.getSymbol(prevA.fieldID) != 'x') out.print('f');
                            out.print(",");
                        }
                        out.print(a);
                        prevA = (TTTAction) a;
                    }
                    out.println("] " + prob);
                }
            }
            out.close();

            out = new PrintStream("o.txt");
            for (Sequence sequence : realizationPlans.get(TTTInfo.OPlayer).keySet()) {
                double prob = realizationPlans.get(TTTInfo.OPlayer).get(sequence);
                if (prob > 0) {
                    out.print("[");
                    TTTAction prevA = null;
                    for (Action a : sequence) {
                        if (prevA != null) {
                            TTTState state = (TTTState) a.getInformationSet().getAllStates().iterator().next();
                            if (state.getSymbol(prevA.fieldID) != 'o') out.print('f');
                            out.print(",");
                        }
                        out.print(a);
                        prevA = (TTTAction) a;
                    }
                    out.println("] " + prob);
                }
            }
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void reverseAction() {
        TTTAction lastAction = (TTTAction) history.getLastAction();
        toMove = history.getLastPlayer().equals(TTTInfo.XPlayer) ? 'x' : 'o';
        if (toMove == 'x')
            reverseXAction(lastAction);
        else
            reverseOAction(lastAction);
        moveNum--;
        super.reverseAction();
    }

    private void reverseOAction(TTTAction lastAction) {
        deleteTried('o', lastAction.fieldID);
        if ('o' == getSymbol(lastAction.fieldID)) {
            setSymbol(lastAction.fieldID, ' ');
        }
    }

    private void reverseXAction(TTTAction lastAction) {
        deleteTried('x', lastAction.fieldID);
        if ('x' == getSymbol(lastAction.fieldID)) {
            setSymbol(lastAction.fieldID, ' ');
        }
    }
}
