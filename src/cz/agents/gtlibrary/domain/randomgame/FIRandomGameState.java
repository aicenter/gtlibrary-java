/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.domain.randomgame;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;

/**
 *
 * @author vilo
 */
public class FIRandomGameState extends RandomGameState {

	private static final long serialVersionUID = -3801266782217001643L;

	public FIRandomGameState(FIRandomGameState gameState) {
        super(gameState);
    }

    public FIRandomGameState() {
        super();
    }
    
    @Override
    protected void generateObservations(int newID, RandomGameAction action) {
        switchPlayers();
    }

    @Override
    public Pair<Integer, Sequence> getISKeyForPlayerToMove() {
        if (ISKey == null) {
            ISKey = new Pair<Integer, Sequence>(
                    hashCode(),
                    getHistory().getSequenceOf(getPlayerToMove()));
        }
        return ISKey;
    }
    
    @Override
    public GameState copy() {
        return new FIRandomGameState(this);
    }

    @Override
    public String toString() {
        return "";
        //return "FIGS" + getHistory();
    }
}
