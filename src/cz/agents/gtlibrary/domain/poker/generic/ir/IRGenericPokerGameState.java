package cz.agents.gtlibrary.domain.poker.generic.ir;

import cz.agents.gtlibrary.algorithms.sequenceform.FullSequenceEFG;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerAction;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRConfig;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRInformationSet;
import cz.agents.gtlibrary.iinodes.*;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.BasicGameBuilder;

public class IRGenericPokerGameState extends GenericPokerGameState {

    public static void main(String[] args) {
        SequenceFormIRConfig config = new SequenceFormIRConfig(new GPGameInfo());
        GameState root = new IRGenericPokerGameState();
        Expander<SequenceFormIRInformationSet> expander = new GenericPokerExpander<>(config);

        BasicGameBuilder.build(root, config, expander);

        System.out.println(config.getAllInformationSets().values().stream().filter(i -> i.getPlayer().equals(GPGameInfo.FIRST_PLAYER)).count() + " " +
                config.getAllInformationSets().values().stream().filter(i -> i.getPlayer().equals(GPGameInfo.SECOND_PLAYER)).count());
        System.out.println(config.getSequencesFor(GPGameInfo.FIRST_PLAYER).size() + " " + config.getSequencesFor(GPGameInfo.SECOND_PLAYER).size());

        root = new GenericPokerGameState();
        SequenceFormConfig<SequenceInformationSet> config1 = new SequenceFormConfig<>();
        Expander<SequenceInformationSet> expander1 = new GenericPokerExpander<>(config1);
        FullSequenceEFG efg = new FullSequenceEFG(root, expander1, new GPGameInfo(), config1);
        efg.generateCompleteGame();
        System.out.println(config1.getAllInformationSets().values().stream().filter(i -> i.getPlayer().equals(GPGameInfo.FIRST_PLAYER)).count() + " " +
                config1.getAllInformationSets().values().stream().filter(i -> i.getPlayer().equals(GPGameInfo.SECOND_PLAYER)).count());
        System.out.println(config1.getSequencesFor(GPGameInfo.FIRST_PLAYER).size() + " " + config1.getSequencesFor(GPGameInfo.SECOND_PLAYER).size());
    }

    public IRGenericPokerGameState() {
        super();
    }

    public IRGenericPokerGameState(GenericPokerGameState gameState) {
        super(gameState);
    }

//    @Override
//    public ISKey getISKeyForPlayerToMove() {
//        if (getTable() == null)
//            return getKeyBeforeTable();
//        return getKeyAfterTable();
//    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        Observations observations = new Observations(getPlayerToMove(), getPlayerToMove());

        observations.add(new PerfectRecallObservation((PerfectRecallISKey) super.getISKeyForPlayerToMove()));
        return new ImperfectRecallISKey(observations, null, null);
    }

    public ISKey getKeyBeforeTable() {
        if (getPlayerToMove().equals(GPGameInfo.FIRST_PLAYER)) {
            Observations ownObservations = new Observations(GPGameInfo.FIRST_PLAYER, GPGameInfo.FIRST_PLAYER);

            populateObservations(ownObservations);
            Observations opponentObservations = new Observations(GPGameInfo.FIRST_PLAYER, GPGameInfo.SECOND_PLAYER);

            populateObservations(opponentObservations);
            Observations natureObservations = new Observations(GPGameInfo.FIRST_PLAYER, GPGameInfo.NATURE);

            natureObservations.add(new ImperfectPokerObservation(getCardFor(GPGameInfo.FIRST_PLAYER).getActionType()));
            return new ImperfectRecallISKey(ownObservations, opponentObservations, natureObservations);
        }
        if (getPlayerToMove().equals(GPGameInfo.SECOND_PLAYER))
            return getISKeyForP2();
        return getISKeyForNature();
    }

    private ISKey getISKeyForP2() {
        Observations ownObservations = new Observations(GPGameInfo.SECOND_PLAYER, GPGameInfo.SECOND_PLAYER);

        populateObservations(ownObservations);
        Observations opponentObservations = new Observations(GPGameInfo.SECOND_PLAYER, GPGameInfo.FIRST_PLAYER);

        opponentObservations.add(new PerfectObservablePokerObservation(new ArrayListSequenceImpl(getSequenceFor(GPGameInfo.FIRST_PLAYER))));
        Observations natureObservations = new Observations(GPGameInfo.SECOND_PLAYER, GPGameInfo.NATURE);

        natureObservations.add(new ImperfectPokerObservation(getCardFor(GPGameInfo.SECOND_PLAYER).getActionType()));
        if (getTable() != null)
            natureObservations.add(new ImperfectPokerObservation(getTable().getActionType()));
        return new ImperfectRecallISKey(ownObservations, opponentObservations, natureObservations);
    }

    protected void populateObservations(Observations observations) {
        for (Action action : getSequenceFor(observations.getObservedPlayer())) {
            GenericPokerAction pokerAction = (GenericPokerAction) action;

            observations.add(new ImperfectPokerObservation(pokerAction.getActionType()));
        }
    }

    public ISKey getKeyAfterTable() {
        if (getPlayerToMove().equals(GPGameInfo.FIRST_PLAYER)) {
            Observations ownObservations = new Observations(GPGameInfo.FIRST_PLAYER, GPGameInfo.FIRST_PLAYER);

            populateObservations(ownObservations);
            Observations opponentObservations = new Observations(GPGameInfo.FIRST_PLAYER, GPGameInfo.SECOND_PLAYER);

            populateObservations(opponentObservations);
            Observations natureObservations = new Observations(GPGameInfo.FIRST_PLAYER, GPGameInfo.NATURE);
            String cardComparison = getCardComparison(((GenericPokerAction) getTable()), ((GenericPokerAction) getCardFor(GPGameInfo.FIRST_PLAYER)));

//            natureObservations.add(new ImperfectPokerObservation(cardComparison));
            natureObservations.add(new ImperfectPokerObservation(getCardFor(GPGameInfo.FIRST_PLAYER).getActionType()));
            natureObservations.add(new ImperfectPokerObservation(getTable().getActionType()));
            return new ImperfectRecallISKey(ownObservations, opponentObservations, natureObservations);
        }
        if (getPlayerToMove().equals(GPGameInfo.SECOND_PLAYER))
            return getISKeyForP2();
        return getISKeyForNature();
    }

    @Override
    public GameState copy() {
        return new IRGenericPokerGameState(this);
    }

    private String getCardComparison(GenericPokerAction table, GenericPokerAction cardFor) {
        if (cardFor.getValue() > table.getValue())
            return "higher";
        if (cardFor.getValue() < table.getValue())
            return "lower";
        return "pair";
    }

    public ISKey getISKeyForNature() {
        Observations ownObservations = new Observations(GPGameInfo.NATURE, GPGameInfo.NATURE);

        ownObservations.add(new PerfectPokerObservation(new ArrayListSequenceImpl(getSequenceFor(GPGameInfo.NATURE))));
        Observations p1Observations = new Observations(GPGameInfo.NATURE, GPGameInfo.FIRST_PLAYER);
        Observations p2Observations = new Observations(GPGameInfo.NATURE, GPGameInfo.SECOND_PLAYER);

        return new ImperfectRecallISKey(p1Observations, p2Observations, ownObservations);
    }

    public class ImperfectPokerObservation implements Observation {

        String type;

        public ImperfectPokerObservation(String type) {
            this.type = type;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ImperfectPokerObservation)) return false;

            ImperfectPokerObservation that = (ImperfectPokerObservation) o;

            return type.equals(that.type);

        }

        @Override
        public int hashCode() {
            return type.hashCode();
        }

        @Override
        public String toString() {
            return type;
        }
    }

    public class PerfectPokerObservation implements Observation {

        Sequence sequence;

        public PerfectPokerObservation(Sequence sequence) {
            this.sequence = sequence;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PerfectPokerObservation)) return false;

            PerfectPokerObservation that = (PerfectPokerObservation) o;

            return sequence.equals(that.sequence);

        }

        @Override
        public int hashCode() {
            return sequence.hashCode();
        }

        @Override
        public String toString() {
            return sequence.toString();
        }
    }

    public class PerfectObservablePokerObservation implements Observation {

        Sequence sequence;

        public PerfectObservablePokerObservation(Sequence sequence) {
            this.sequence = sequence;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PerfectObservablePokerObservation)) return false;

            PerfectObservablePokerObservation that = (PerfectObservablePokerObservation) o;

            if (sequence.size() != that.sequence.size())
                return false;
            for (int i = 0; i < sequence.size(); i++) {
                GenericPokerAction thisAction = (GenericPokerAction) sequence.get(i);
                GenericPokerAction thatAction = (GenericPokerAction) that.sequence.get(i);

                if (!thisAction.getActionType().equals(thatAction.getActionType()) ||
                        thisAction.getValue() != thatAction.getValue())
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public String toString() {
            return sequence.toString();
        }
    }
}
