package cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative.iinodes;

import cz.agents.gtlibrary.domain.flipit.AllPointsFlipItGameState;
import cz.agents.gtlibrary.domain.testGame.TestGameState;
import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.iinodes.HistoryImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.History;
import cz.agents.gtlibrary.interfaces.InformationSet;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Jakub Cerny on 07/11/2017.
 */
public class TLAction extends ActionImpl {

    protected int index;
    protected HashMap<GameState, ArrayList<GameState>> temporaryLeaves;
    protected GameState state;

    public TLAction(InformationSet informationSet, int idx, HashMap<GameState, ArrayList<GameState>> tLs, GameState state) {
        super(informationSet);
        index = idx;
        temporaryLeaves = tLs;
        this.state = state;
    }

    public int getIndex(){
        return index;
    }

    @Override
    public void perform(GameState gameState) {

        int hash = gameState.hashCode();
        History history = gameState.getHistory().copy();
//        System.out.println(gameState);
        gameState.transformInto(state);
//        history.addActionOf(this, gameState.getPlayerToMove());
        gameState.setHistory(history);

        // hack for changing the ISKEY
        if (gameState instanceof AllPointsFlipItGameState) {
//            System.out.println("changing");
            ((AllPointsFlipItGameState) gameState).setHash(gameState.getHistory().hashCode());
        }

        if (gameState instanceof TestGameState) {
//            System.out.println("changing");
            ((TestGameState) gameState).setHash(gameState.getHistory().hashCode());
        }

//        gameState = new TLGameState(gameState.getAllPlayers(), temporaryLeaves.get(gameState).get(index).gameState);
//        ((TLGameState)gameState).setHistory(history);

//        TLGameState state = temporaryLeaves.get(gameState).get(index);
//        state.setHistory(gameState.getHistory().copy());
//        state.getHistory().addActionOf(this,informationSet.getPlayer());
//
//        gameState = state;
    }

    @Override
    public String toString() {
        return "TLA{" + index +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        TLAction tlAction = (TLAction) o;

        if (index != tlAction.index) return false;

        return  (informationSet.equals(tlAction.informationSet));
    }

    @Override
    public int hashCode() {
        int result = index;
//        result = 31 * result + temporaryLeaves.hashCode();
        result = 31 * result + informationSet.hashCode();
        return result;
    }
}
