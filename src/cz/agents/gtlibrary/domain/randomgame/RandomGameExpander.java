package cz.agents.gtlibrary.domain.randomgame;


import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class RandomGameExpander<I extends InformationSet> extends ExpanderImpl<I> {

	private static final long serialVersionUID = -4773226796724312872L;
	
	private long firstSeed;

    public RandomGameExpander(AlgorithmConfig<I> algConfig) {
        super(algConfig);
        RandomGameInfo.rnd = new Random(RandomGameInfo.seed);
        firstSeed = RandomGameInfo.rnd.nextLong();
    }

    @Override
    public List<Action> getActions(GameState gameState) {
        RandomGameState gsState = (RandomGameState) gameState;
        InformationSet informationSet = getAlgorithmConfig().getInformationSetFor(gameState);
        List<Action> actions = new LinkedList<Action>();

        String newVal = firstSeed + "";
        if (gsState.getHistory().getSequenceOf(gameState.getPlayerToMove()).size() > 0)
            newVal = ((RandomGameAction)gsState.getHistory().getSequenceOf(gameState.getPlayerToMove()).getLast()).getValue();

        for (int i=0; i<RandomGameInfo.MAX_BF; i++) {
            RandomGameAction action = new RandomGameAction(informationSet, newVal + "_" + i, i);
            actions.add(action);
        }

        return actions;
    }
}
