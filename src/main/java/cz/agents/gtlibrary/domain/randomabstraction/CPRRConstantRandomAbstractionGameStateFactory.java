package cz.agents.gtlibrary.domain.randomabstraction;

import cz.agents.gtlibrary.algorithms.cfr.ir.IRCFRInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.FullSequenceEFG;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState;
import cz.agents.gtlibrary.domain.randomgameimproved.io.BasicGameBuilder;
import cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.cprr.CPRRExpander;
import cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.cprr.CPRRGameState;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRConfig;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRInformationSet;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.io.GambitEFG;

import java.util.*;
import java.util.stream.Collectors;

public class CPRRConstantRandomAbstractionGameStateFactory extends RandomAbstractionGameStateFactory {

    public static void main(String[] args) {
        GameState wrappedRoot = new RandomGameState();
        SequenceFormConfig<SequenceInformationSet> config = new SequenceFormConfig<>();
        Expander<SequenceInformationSet> wrappedExpander = new RandomGameExpander<>(config);

        FullSequenceEFG efg = new FullSequenceEFG(wrappedRoot, wrappedExpander, new RandomGameInfo(), config);
        efg.generateCompleteGame();
        GambitEFG gambit = new GambitEFG();

        gambit.buildAndWrite("test_orig.gbt", wrappedRoot, wrappedExpander);
//            config.getSequencesFor(RandomGameInfo.FIRST_PLAYER).forEach(System.out::println);
        GameState root = new CPRRConstantRandomAbstractionGameStateFactory().createRoot(wrappedRoot, wrappedExpander.getAlgorithmConfig());
        Expander<SequenceFormIRInformationSet> expander = new RandomAbstractionExpander<>(wrappedExpander, new SequenceFormIRConfig(new RandomAbstractionGameInfo(new RandomGameInfo())));

        BasicGameBuilder.build(root, expander.getAlgorithmConfig(), expander);

        CPRRExpander<SequenceFormIRInformationSet> cprrExpander = new CPRRExpander<>(expander);
        GambitEFG gambit1 = new GambitEFG();

        gambit1.buildAndWrite("test.gbt", root, expander);

        GambitEFG gambitEFG1 = new GambitEFG();

        gambitEFG1.buildAndWrite("test_cprr.gbt", new CPRRGameState(root), cprrExpander);
    }

    protected Collection<LinkedList<ISKey>> getMergeCandidates(AlgorithmConfig<? extends SequenceInformationSet> config) {
        List<LinkedList<ISKey>> newMergeCandidates = new ArrayList<>();
        Collection<LinkedList<ISKey>> originalMergeCandidates = super.getMergeCandidates(config);

        for (LinkedList<ISKey> candidate : originalMergeCandidates) {
            Map<Sequence, List<ISKey>> collect = candidate.stream().collect(Collectors.groupingBy(key -> ((PerfectRecallISKey) key).getSequence()));

            if (collect.size() == candidate.size()) {
                newMergeCandidates.add(candidate);
                continue;
            }
            while (true) {
                LinkedList<ISKey> newGrouping = collect.values().stream().filter(isKeys -> !isKeys.isEmpty()).map(isKeys -> isKeys.remove(0)).collect(Collectors.toCollection(LinkedList::new));

                if (newGrouping.isEmpty())
                    break;
                newMergeCandidates.add(newGrouping);
            }
        }
        return newMergeCandidates;
    }
}
