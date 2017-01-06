package cz.agents.gtlibrary.domain.poker.generic.ir;

import cz.agents.gtlibrary.algorithms.sequenceform.FullSequenceEFG;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRConfig;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRInformationSet;
import cz.agents.gtlibrary.iinodes.*;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.utils.BasicGameBuilder;

public class CPRRConstIRGenericPokerGameState extends IRGenericPokerGameState {

    public static void main(String[] args) {
        SequenceFormIRConfig config = new SequenceFormIRConfig(new GPGameInfo());
        GameState root = new CPRRConstIRGenericPokerGameState();
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

    public CPRRConstIRGenericPokerGameState() {
        super();
    }

    public CPRRConstIRGenericPokerGameState(GenericPokerGameState gameState) {
        super(gameState);
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        if (cachedISKey != null)
            return cachedISKey;
        if (isPlayerToMoveNature()) {
            cachedISKey = getISKeyForNature();
            return cachedISKey;
        }
        Observations ownObservations = new Observations(getPlayerToMove(), getPlayerToMove());

        populateObservations(ownObservations);
        Observations opponentObservations = new Observations(getPlayerToMove(), players[1 - currentPlayerIndex]);

        populateObservations(opponentObservations);
        Observations natureObservations = new Observations(GPGameInfo.FIRST_PLAYER, GPGameInfo.SECOND_PLAYER);

        if (firstMoveAfterTable())
            natureObservations.add(new IRGenericPokerGameState.ImperfectPokerObservation(getTable().getActionType()));
        else if(lastMovePlayedByNature())
            natureObservations.add(new IRGenericPokerGameState.ImperfectPokerObservation(getCardForActingPlayer().getActionType()));
        cachedISKey = new ImperfectRecallISKey(ownObservations, opponentObservations, natureObservations);
        return cachedISKey;
    }

    private boolean firstMoveAfterTable() {
        return lastMovePlayedByNature() && getTable() != null;
    }

    private ISKey getPRKey(ISKey isKeyForPlayerToMove) {
        Observations ownObservations = new Observations(getPlayerToMove(), getPlayerToMove());

        ownObservations.add(new PerfectRecallObservation((PerfectRecallISKey) isKeyForPlayerToMove));
        return new ImperfectRecallISKey(ownObservations, null, null);
    }

    @Override
    public GameState copy() {
        return new CPRRConstIRGenericPokerGameState(this);
    }

}
