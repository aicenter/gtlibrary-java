package cz.agents.gtlibrary.domain.randomabstraction;

import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState;
import cz.agents.gtlibrary.domain.randomgameimproved.io.BasicGameBuilder;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRConfig;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRInformationSet;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.io.GambitEFG;

import java.util.Map;
import java.util.stream.Collectors;

public class RandomAbstractionGameStateFactory {

    public static void main(String[] args) {
        GameState wrappedRoot = new RandomGameState();
        Expander<SequenceFormIRInformationSet> wrappedExpander = new RandomGameExpander<>(new SequenceFormIRConfig(new RandomGameInfo()));
        BasicGameBuilder.build(wrappedRoot, wrappedExpander.getAlgorithmConfig(), wrappedExpander);

        GameState root = RandomAbstractionGameStateFactory.createRoot(wrappedRoot, wrappedExpander.getAlgorithmConfig());
        Expander<SequenceFormIRInformationSet> expander = new RandomAbstractionExpander<>(wrappedExpander, new SequenceFormIRConfig(new RandomAbstractionGameInfo(new RandomGameInfo())));

        BasicGameBuilder.build(root, expander.getAlgorithmConfig(), expander);

        GambitEFG gambit = new GambitEFG();

        gambit.buildAndWrite("test.gbt", root, expander);
    }
    public static RandomAbstractionGameState createRoot(GameState wrappedRoot, Expander<? extends InformationSet> wrappedExpander) {
        return null;
    }

    public static RandomAbstractionGameState createRoot(GameState wrappedRoot, AlgorithmConfig<? extends InformationSet> config) {
        Map<ISKey, ISKey> keyMap = config.getAllInformationSets().keySet().stream().collect(Collectors.toMap(key -> key, key -> key));

        return new RandomAbstractionGameState(keyMap, wrappedRoot);
    }

}
