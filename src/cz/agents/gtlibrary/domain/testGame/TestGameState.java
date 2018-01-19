package cz.agents.gtlibrary.domain.testGame;

import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Created by Jakub Cerny on 20/10/2017.
 */
public class TestGameState extends GameStateImpl {

    protected int ID;
    protected int hash;

    public TestGameState() {
        super(TestGameInfo.ALL_PLAYERS);
        ID = 1;
        hash = 1;
    }

    public TestGameState(GameStateImpl gameState) {
        super(gameState);
        this.ID = ((TestGameState)gameState).getID();
        this.hash = ((TestGameState)gameState).getHash();
    }

    @Override
    public void transformInto(GameState gameState) {
        super.transformInto(gameState);
        this.ID = ((TestGameState)gameState).getID();
//        this.hash = ((TestGameState)gameState).getHash();
    }

    public void setHash(int hs){hash = hs;}

    public void executeAction(TestGameAction action){
        this.ID = action.getID();
    }

    @Override
    public double getProbabilityOfNatureFor(Action action) {
        return 0;
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
//        System.out.println(this.history.toString());
        if (isGameEnd())
            return new PerfectRecallISKey(getSequenceForPlayerToMove().hashCode(), getSequenceForPlayerToMove());
        return new PerfectRecallISKey(new HashCodeBuilder().append(TestGameInfo.iss.get(ID)).append(hash).hashCode(), getSequenceForPlayerToMove());
    }

    @Override
    public Player getPlayerToMove() {
        if (isGameEnd()) return TestGameInfo.PL0;
        return TestGameInfo.players.get(TestGameInfo.iss.get(ID)).equals(0) ? TestGameInfo.PL0 : TestGameInfo.PL1;
    }

    @Override
    public GameState copy() {
        return new TestGameState(this);
    }

    @Override
    public double[] getUtilities() {
        return ArrayUtils.toPrimitive(TestGameInfo.utilities.get(ID));
    }

    @Override
    public boolean isGameEnd() {
        return !TestGameInfo.successors.containsKey(ID);
    }

    @Override
    public boolean isPlayerToMoveNature() {
        return false;
    }

    @Override
    public int hashCode() {
        return ID;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof TestGameState)) return false;
        return ID == ((TestGameState)object).getID();
    }

    public int getID(){
        return ID;
    }
    public int getHash(){ return hash; }

    @Override
    public String toString() {
        return "TestGameState{" +
                "ID=" + ID +
                '}';
    }
}
