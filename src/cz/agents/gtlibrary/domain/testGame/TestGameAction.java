package cz.agents.gtlibrary.domain.testGame;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

/**
 * Created by Jakub Cerny on 20/10/2017.
 */
public class TestGameAction extends ActionImpl {

    protected int IDX;
    protected int ID;

    public TestGameAction(int idx, int ID, InformationSet informationSet) {
        super(informationSet);
        this.IDX = idx;
        this.ID = ID;
    }

    @Override
    public void perform(GameState gameState) {
        ((TestGameState)gameState).executeAction(this);
    }

    public int getID(){
        return ID;
    }

    @Override
    public String toString() {
        return "(" + IDX + "/" + informationSet.hashCode() + ")";
    } //return "(" + ID + ":"+IDX+")";}

    @Override
    public int hashCode() {
        return IDX;
    }
}
