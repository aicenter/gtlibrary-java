package algorithms.sequenceform;

import cz.agents.gtlibrary.algorithms.sequenceform.FullSequenceEFG;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielGameState;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.domain.poker.kuhn.KPGameInfo;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerExpander;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerGameState;
import cz.agents.gtlibrary.interfaces.Expander;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FullSequenceEFGTest {

    @Test
    public void kuhnPokerTest() {
        SequenceFormConfig<SequenceInformationSet> config = new SequenceFormConfig<>();
        Expander<SequenceInformationSet> expander = new KuhnPokerExpander<>(config);
        FullSequenceEFG solver = new FullSequenceEFG(new KuhnPokerGameState(), expander, new KPGameInfo(), config);

        solver.generate();

        assertEquals(-0.0555555555555555, solver.getGameValue(), 1e-6);
    }

    @Test
    public void leducTest() {
        GPGameInfo.MAX_RAISES_IN_ROW = 1;

        GPGameInfo.MAX_DIFFERENT_BETS = 1;
        GPGameInfo.MAX_DIFFERENT_RAISES = GPGameInfo.MAX_DIFFERENT_BETS;

        GPGameInfo.BETS_FIRST_ROUND = new int[GPGameInfo.MAX_DIFFERENT_BETS];
        for (int i = 0; i < GPGameInfo.MAX_DIFFERENT_BETS; i++)
            GPGameInfo.BETS_FIRST_ROUND[i] = (i + 1) * 2;


        GPGameInfo.RAISES_FIRST_ROUND = new int[GPGameInfo.MAX_DIFFERENT_RAISES];
        for (int i = 0; i < GPGameInfo.MAX_DIFFERENT_RAISES; i++)
            GPGameInfo.RAISES_FIRST_ROUND[i] = (i + 1) * 2;

        GPGameInfo.MAX_CARD_TYPES = 3;
        GPGameInfo.CARD_TYPES = new int[GPGameInfo.MAX_CARD_TYPES];
        for (int i = 0; i < GPGameInfo.MAX_CARD_TYPES; i++)
            GPGameInfo.CARD_TYPES[i] = i;


        GPGameInfo.MAX_CARD_OF_EACH_TYPE = 2;
        GPGameInfo.DECK = new int[GPGameInfo.MAX_CARD_OF_EACH_TYPE * GPGameInfo.MAX_CARD_TYPES];
        for (int i = 0; i < GPGameInfo.MAX_CARD_TYPES; i++)
            for (int j = 0; j < GPGameInfo.MAX_CARD_OF_EACH_TYPE; j++) {
                GPGameInfo.DECK[i * GPGameInfo.MAX_CARD_OF_EACH_TYPE + j] = i;
            }

        GPGameInfo.BETS_SECOND_ROUND = new int[GPGameInfo.BETS_FIRST_ROUND.length];
        for (int i = 0; i < GPGameInfo.BETS_FIRST_ROUND.length; i++) {
            GPGameInfo.BETS_SECOND_ROUND[i] = 2 * GPGameInfo.BETS_FIRST_ROUND[i];
        }

        GPGameInfo.RAISES_SECOND_ROUND = new int[GPGameInfo.RAISES_FIRST_ROUND.length];
        for (int i = 0; i < GPGameInfo.RAISES_FIRST_ROUND.length; i++) {
            GPGameInfo.RAISES_SECOND_ROUND[i] = 2 * GPGameInfo.RAISES_FIRST_ROUND[i];
        }

        SequenceFormConfig<SequenceInformationSet> config = new SequenceFormConfig<>();
        Expander<SequenceInformationSet> expander = new GenericPokerExpander<>(config);
        FullSequenceEFG solver = new FullSequenceEFG(new GenericPokerGameState(), expander, new GPGameInfo(), config);

        solver.generate();

        assertEquals(-0.08560642407800045, solver.getGameValue(), 1e-6);
    }

    @Test
    public void GoofspielTest() {
        GSGameInfo.depth = 4;
        SequenceFormConfig<SequenceInformationSet> config = new SequenceFormConfig<>();
        Expander<SequenceInformationSet> expander = new GoofSpielExpander<>(config);
        FullSequenceEFG solver = new FullSequenceEFG(new GoofSpielGameState(), expander, new GSGameInfo(), config);

        solver.generate();

        assertEquals(0, solver.getGameValue(), 1e-6);
    }
}
