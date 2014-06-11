package cz.agents.gtlibrary.domain.goofspiel;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;
import java.util.Iterator;

public class IIGoofSpielGameState extends GoofSpielGameState {

    public IIGoofSpielGameState(GoofSpielGameState gameState) {
        super(gameState);
    }

    public IIGoofSpielGameState(Sequence natureSequence) {
        super(natureSequence);
    }

    public IIGoofSpielGameState() {
        super();
    }
    
    
    
    @Override
    public Pair<Integer, Sequence> getISKeyForPlayerToMove() {
            if (key == null) {
                    if (isPlayerToMoveNature())
                            key = new Pair<Integer, Sequence>(0, history.getSequenceOf(getPlayerToMove()));
                    else {
                            int code=playerScore[0];
                            Iterator<Action> it = sequenceForAllPlayers.iterator();
                            for (int i=0;i<round;i++){
                                it.next();//nature player
                                GoofSpielAction a0 = (GoofSpielAction)it.next();
                                GoofSpielAction a1 = (GoofSpielAction)it.next();
                                code *=3;
                                code +=1+Math.signum(a0.compareTo(a1));
                            }
                            key = new Pair<Integer, Sequence>(code, getSequenceForPlayerToMove());
                    }
            }
            return key;
    }

    @Override
    public GameState copy() {
            return new IIGoofSpielGameState(this);
    }

}
