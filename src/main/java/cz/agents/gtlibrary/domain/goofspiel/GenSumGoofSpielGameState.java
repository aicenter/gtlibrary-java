package cz.agents.gtlibrary.domain.goofspiel;

import cz.agents.gtlibrary.interfaces.GameState;

public class GenSumGoofSpielGameState extends GoofSpielGameState {

    public GenSumGoofSpielGameState() {
        super();
    }

    public GenSumGoofSpielGameState(GoofSpielGameState gameState) {
        super(gameState);
    }

    @Override
    protected double[] getEndGameUtilities() {
        double[] utilities = new double[3];

        for (int i = 0; i < playerScore.length; i++) {
            utilities[i] = playerScore[i];
        }
        return utilities;
    }

    @Override
    public GameState copy() {
        return new GenSumGoofSpielGameState(this);
    }


}
