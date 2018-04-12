package cz.agents.gtlibrary.domain.randomabstraction;

import cz.agents.gtlibrary.algorithms.sequenceform.FullSequenceEFG;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState;
import cz.agents.gtlibrary.domain.randomgameimproved.io.BasicGameBuilder;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRConfig;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRInformationSet;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.io.GambitEFG;

import java.util.*;
import java.util.stream.Collectors;

public class P1RandomAlossAbstractionGameStateFactory extends P1RandomAbstractionGameStateFactory {

    public static void main(String[] args) {
        for (int i = 0; i <= 100; i++) {
            RandomGameInfo.seed = i;
            GameState wrappedRoot = new RandomGameState();
            SequenceFormConfig<SequenceInformationSet> config = new SequenceFormConfig<>();
            Expander<SequenceInformationSet> wrappedExpander = new RandomGameExpander<>(config);

            FullSequenceEFG efg = new FullSequenceEFG(wrappedRoot, wrappedExpander, new RandomGameInfo(), config);
            efg.generateCompleteGame();
            GambitEFG gambit = new GambitEFG();

            gambit.buildAndWrite("test_orig.gbt", wrappedRoot, wrappedExpander);
            System.out.println("***************");
            System.out.println(i + " " + config.getSequencesFor(RandomGameInfo.FIRST_PLAYER).size() + " " + config.getSequencesFor(RandomGameInfo.SECOND_PLAYER).size() + " " + config.getAllInformationSets().size());
//            config.getSequencesFor(RandomGameInfo.FIRST_PLAYER).forEach(System.out::println);
            GameState root = new P1RandomAlossAbstractionGameStateFactory().createRoot(wrappedRoot, wrappedExpander.getAlgorithmConfig());
            Expander<SequenceFormIRInformationSet> expander = new RandomAbstractionExpander<>(wrappedExpander, new SequenceFormIRConfig(new RandomAbstractionGameInfo(new RandomGameInfo())));

            BasicGameBuilder.build(root, expander.getAlgorithmConfig(), expander);
            GambitEFG gambit1 = new GambitEFG();

            gambit1.buildAndWrite("test.gbt", root, expander);
            System.out.println(i + " " + ((SequenceFormIRConfig) expander.getAlgorithmConfig()).getSequencesFor(RandomGameInfo.FIRST_PLAYER).size() + " " + ((SequenceFormIRConfig) expander.getAlgorithmConfig()).getSequencesFor(RandomGameInfo.SECOND_PLAYER).size()  + " " + ((SequenceFormIRConfig) expander.getAlgorithmConfig()).getAllInformationSets().size());
//            ((SequenceFormIRConfig)expander.getAlgorithmConfig()).getSequencesFor(RandomGameInfo.FIRST_PLAYER).forEach(System.out::println);
        }
    }

    /**
     * Assumes information sets with the same length of incoming sequences
     *
     * @param config
     * @return
     */
    protected Collection<LinkedList<ISKey>> getMergeCandidates(AlgorithmConfig<? extends SequenceInformationSet> config) {
        Map<Object, List<Pair<Set<Sequence>, LinkedList<ISKey>>>> mergeCandidates = new HashMap<>();
        int counter = 0;

        for (Map.Entry<ISKey, ? extends SequenceInformationSet> entry : config.getAllInformationSets().entrySet()) {
            if (entry.getValue().getPlayer().getId() == 1 || entry.getValue().getOutgoingSequences().isEmpty()) {
                LinkedList<ISKey> list = new LinkedList<>();

                list.add(entry.getKey());
                mergeCandidates.put(counter++, Arrays.asList(new Pair<>(Collections.emptySet(), list)));
            } else {
                Pair<Integer, Integer> key = new Pair<>(getActionCount(entry.getValue()), getIncomingSequenceLength(entry.getValue()));
                List<Pair<Set<Sequence>, LinkedList<ISKey>>> list = mergeCandidates.getOrDefault(key, new ArrayList<>());

                update(list, entry);
                mergeCandidates.put(key, list);
            }
        }
        return mergeCandidates.values().stream().flatMap(Collection::stream).map(Pair::getRight).collect(Collectors.toList());
    }

    protected void update(List<Pair<Set<Sequence>, LinkedList<ISKey>>> list, Map.Entry<ISKey, ? extends SequenceInformationSet> entry) {
        for (Pair<Set<Sequence>, LinkedList<ISKey>> pair : list) {
            if (compatible(pair.getLeft(), entry.getValue().getPlayersHistory())) {
                pair.getLeft().add(entry.getValue().getPlayersHistory());
                pair.getRight().add(entry.getKey());
                return;
            }
        }
        Set<Sequence> sequences = new HashSet<>();

        sequences.add(entry.getValue().getPlayersHistory());
        LinkedList<ISKey> isKeys = new LinkedList<>();

        isKeys.add(entry.getKey());
        list.add(new Pair<>(sequences, isKeys));
    }

    private boolean compatible(Set<Sequence> left, Sequence playersHistory) {
        for (Sequence sequence : left) {
            Map<InformationSet, Action> sequenceISs = sequence.getAsList().stream().collect(Collectors.toMap(Action::getInformationSet, a -> a));

            if (!playersHistory.equals(sequence) && !playersHistory.getAsList().stream().anyMatch(a -> sequenceISs.containsKey(a.getInformationSet()) && !a.equals(sequenceISs.get(a.getInformationSet()))))
                return false;
        }
        return true;
    }

}
