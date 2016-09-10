package cz.agents.gtlibrary.domain.randomabstraction;

import cz.agents.gtlibrary.algorithms.sequenceform.FullSequenceEFG;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.domain.bpg.BPGExpander;
import cz.agents.gtlibrary.domain.bpg.BPGGameInfo;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState;
import cz.agents.gtlibrary.domain.randomgameimproved.io.BasicGameBuilder;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRConfig;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRInformationSet;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.ImperfectRecallISKey;
import cz.agents.gtlibrary.iinodes.Observations;
import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.io.GambitEFG;

import java.util.*;

public class RandomAbstractionGameStateFactory {

    public static void main(String[] args) {
        GameState wrappedRoot = new RandomGameState();
        SequenceFormConfig<SequenceInformationSet> config = new SequenceFormConfig<>();
        Expander<SequenceInformationSet> wrappedExpander = new RandomGameExpander<>(config);

        FullSequenceEFG efg = new FullSequenceEFG(wrappedRoot, wrappedExpander, new RandomGameInfo(), config);
        efg.generateCompleteGame();

        GameState root = RandomAbstractionGameStateFactory.createRoot(wrappedRoot, wrappedExpander.getAlgorithmConfig());
        Expander<SequenceFormIRInformationSet> expander = new RandomAbstractionExpander<>(wrappedExpander, new SequenceFormIRConfig(new RandomAbstractionGameInfo(new RandomGameInfo())));

        BasicGameBuilder.build(root, expander.getAlgorithmConfig(), expander);

        GambitEFG gambit = new GambitEFG();

        gambit.buildAndWrite("test.gbt", root, expander);
    }

    public static RandomAbstractionGameState createRoot(GameState wrappedRoot, AlgorithmConfig<? extends SequenceInformationSet> config) {
//        Map<ISKey, ISKey> keyMap = config.getAllInformationSets().keySet().stream().collect(Collectors.toMap(key -> key, key -> key));
        Map<ISKey, ISKey> keyMap = new HashMap<>();
        Collection<LinkedList<ISKey>> mergeCandidates = getMergeCandidates(config);
        Random random = new HighQualityRandom(1);
        int counter = 0;

        for (LinkedList<ISKey> mergeCandidate : mergeCandidates) {
            Collections.shuffle(mergeCandidate);

            while (!mergeCandidate.isEmpty()) {
                ISKey newKey = getKey(counter++);
                keyMap.put(mergeCandidate.removeFirst(), newKey);
                Iterator<ISKey> iterator = mergeCandidate.iterator();

                while (iterator.hasNext()) {
                    ISKey current = iterator.next();
                    double p = random.nextDouble();

                    if (p <= RandomAbstractionGameInfo.JOIN_PROB) {
                        keyMap.put(current, newKey);
                        iterator.remove();
                    }
                }
            }
        }
        return new RandomAbstractionGameState(keyMap, wrappedRoot);
    }

    /**
     * Assumes information sets with the same length of incoming sequences
     *
     * @param config
     * @return
     */
    private static Collection<LinkedList<ISKey>> getMergeCandidates(AlgorithmConfig<? extends SequenceInformationSet> config) {
        Map<Object, LinkedList<ISKey>> mergeCandidates = new HashMap<>();
        int counter = 0;

        for (Map.Entry<ISKey, ? extends SequenceInformationSet> entry : config.getAllInformationSets().entrySet()) {
            if (entry.getValue().getPlayer().getId() == 1 || entry.getValue().getOutgoingSequences().isEmpty()) {
                LinkedList<ISKey> list = new LinkedList<>();

                list.add(entry.getKey());
                mergeCandidates.put(counter++, list);
            } else {
                Pair<Integer, Integer> key = new Pair<>(getActionCount(entry.getValue()), getIncomingSequenceLength(entry.getValue()));
                LinkedList<ISKey> list = mergeCandidates.getOrDefault(key, new LinkedList<>());

                list.add(entry.getKey());
                mergeCandidates.put(key, list);
            }
        }
        return mergeCandidates.values();
    }

    private static int getIncomingSequenceLength(SequenceInformationSet informationSet) {
        Sequence incomingSequence = informationSet.getPlayersHistory();

        return incomingSequence.size();
    }

    private static int getActionCount(SequenceInformationSet informationSet) {
        return informationSet.getOutgoingSequences().size();
    }

    private static ISKey getKey(int id) {
        Player player = new PlayerImpl(0);
        Observations observations = new Observations(player, player);
        observations.add(new IDObservation(id));

        return new ImperfectRecallISKey(observations, null, null);
    }

    private static class IDObservation implements Observation {
        private int id;

        public IDObservation(int id) {
            this.id = id;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof IDObservation)) return false;

            IDObservation that = (IDObservation) o;

            return id == that.id;

        }

        @Override
        public int hashCode() {
            return id;
        }
    }
}
