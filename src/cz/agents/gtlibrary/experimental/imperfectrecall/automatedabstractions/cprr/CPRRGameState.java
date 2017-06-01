package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.cprr;

import cz.agents.gtlibrary.algorithms.cfr.ir.IRCFRConfig;
import cz.agents.gtlibrary.algorithms.cfr.ir.IRCFRInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.FullSequenceEFG;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Rational;
import cz.agents.gtlibrary.domain.randomabstraction.RandomAbstractionExpander;
import cz.agents.gtlibrary.domain.randomabstraction.P1RandomAbstractionGameStateFactory;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState;
import cz.agents.gtlibrary.iinodes.*;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.io.GambitEFG;


public class CPRRGameState extends GameStateImpl {

    private GameStateImpl wrappedState;

    public static void main(String[] args) {
        GameState wrappedRoot = new RandomGameState();
        SequenceFormConfig<SequenceInformationSet> wrappedConfig = new SequenceFormConfig<>();
        Expander<SequenceInformationSet> wrappedExpander = new RandomGameExpander<>(wrappedConfig);
        FullSequenceEFG efg = new FullSequenceEFG(wrappedRoot, wrappedExpander, new RandomGameInfo(), wrappedConfig);
        efg.generateCompleteGame();

        IRCFRConfig config = new IRCFRConfig();
        GameState root = new P1RandomAbstractionGameStateFactory().createRoot(wrappedRoot, wrappedExpander.getAlgorithmConfig());
        Expander<IRCFRInformationSet> expander = new RandomAbstractionExpander<>(wrappedExpander, config);
        CPRRExpander<IRCFRInformationSet> cprrExpander = new CPRRExpander<>(new RandomAbstractionExpander<>(wrappedExpander, config));

        GambitEFG gambitEFG = new GambitEFG();

        gambitEFG.buildAndWrite("irTest.gbt", root, expander);

        GambitEFG gambitEFG1 = new GambitEFG();

        gambitEFG1.buildAndWrite("cprrTest.gbt", new CPRRGameState(root), cprrExpander);
    }

    public CPRRGameState(GameState gameState) {
        super(gameState.getAllPlayers());
        wrappedState = (GameStateImpl) gameState;
    }

    @Override
    public void performActionModifyingThisState(Action action) {
        wrappedState.performActionModifyingThisState(action);
    }

    @Override
    public double getProbabilityOfNatureFor(Action action) {
        return wrappedState.getProbabilityOfNatureFor(action);
    }

    @Override
    public Rational getExactProbabilityOfNatureFor(Action action) {
        return wrappedState.getExactProbabilityOfNatureFor(action);
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        Observations newObservations = new Observations(getPlayerToMove(), getPlayerToMove());
        ISKey key = wrappedState.getISKeyForPlayerToMove();

        newObservations.add(new SequenceObservation(getSequenceForPlayerToMove()));
        newObservations.add(new KeyObservation(key));
        return new ImperfectRecallISKey(newObservations, null, null);
    }

    @Override
    public double getNatureProbability() {
        return wrappedState.getNatureProbability();
    }

    @Override
    public Sequence getSequenceForPlayerToMove() {
        return wrappedState.getSequenceForPlayerToMove();
    }

    @Override
    public Sequence getSequenceFor(Player player) {
        return wrappedState.getSequenceFor(player);
    }

    @Override
    public History getHistory() {
        return wrappedState.getHistory();
    }

    @Override
    public Player getPlayerToMove() {
        return wrappedState.getPlayerToMove();
    }

    @Override
    public GameState copy() {
        return new CPRRGameState(wrappedState.copy());
    }

    @Override
    public double[] getUtilities() {
        return wrappedState.getUtilities();
    }

    @Override
    public boolean isGameEnd() {
        return wrappedState.isGameEnd();
    }

    @Override
    public boolean isPlayerToMoveNature() {
        return wrappedState.isPlayerToMoveNature();
    }

    @Override
    public int hashCode() {
        return wrappedState.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof CPRRGameState))
            return false;
        return wrappedState.equals(((CPRRGameState) object).getWrappedState());
    }

    public GameState getWrappedState() {
        return wrappedState;
    }

    @Override
    public String toString() {
        return wrappedState.toString();
    }

    private class SequenceObservation implements Observation {
        private Sequence sequence;

        public SequenceObservation(Sequence sequence) {
            this.sequence = new ArrayListSequenceImpl(sequence);
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SequenceObservation)) return false;

            SequenceObservation that = (SequenceObservation) o;

            return sequence.equals(that.sequence);

        }

        @Override
        public int hashCode() {
            return sequence.hashCode();
        }
    }

    private class KeyObservation implements Observation {
        private ISKey key;

        public KeyObservation(ISKey key) {
            this.key = key;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof KeyObservation)) return false;

            KeyObservation that = (KeyObservation) o;

            return key.equals(that.key);

        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }
}
