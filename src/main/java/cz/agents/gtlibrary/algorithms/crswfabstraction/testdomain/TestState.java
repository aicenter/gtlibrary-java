package cz.agents.gtlibrary.algorithms.crswfabstraction.testdomain;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Rational;
import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;

public class TestState extends GameStateImpl {

    protected GameEnum value;

    public TestState() {
        super(GameEnum.players);
        this.value = GameEnum.ROOT;
    }

    public TestState(TestState gameState) {
        super(gameState);
        this.value = gameState.value;
    }

    @Override
    public Player getPlayerToMove() {
        return value.player;
    }

    @Override
    public GameState copy() {
        return new TestState(this);
    }

    @Override
    public double[] getUtilities() {
        Rational[] exactUtilities = getExactUtilities();
        double[] utilities = new double[exactUtilities.length];
        for (int i = 0; i < exactUtilities.length; i++) {
            utilities[i] = exactUtilities[i].doubleValue();
        }
        return utilities;
    }

    @Override
    public Rational[] getExactUtilities() {
        Rational[] utilities = new Rational[value.utilities.length];
        for (int i = 0; i < utilities.length; i++) {
            utilities[i] = new Rational(value.utilities[i], 1);
        }
        return utilities;
    }

    @Override
    public double getProbabilityOfNatureFor(Action action) {
        return value.probabilities[action.hashCode()].doubleValue();
    }

    @Override
    public Rational getExactProbabilityOfNatureFor(Action action) {
        return value.probabilities[action.hashCode()];
    }

    @Override
    public boolean isGameEnd() {
        return value.isLeaf;
    }

    @Override
    public boolean isPlayerToMoveNature() {
        return value.player.getId() == 2;
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        return new SimpleISKey(value.informationSet);
    }

    @Override
    public int hashCode() {
        return value.ordinal();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (getClass() != o.getClass()) return false;

        TestState other = (TestState) o;
        return this.value == other.value;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
