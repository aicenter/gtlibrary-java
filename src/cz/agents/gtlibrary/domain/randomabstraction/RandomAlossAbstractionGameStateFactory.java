package cz.agents.gtlibrary.domain.randomabstraction;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.Triplet;

import java.util.*;
import java.util.stream.Collectors;

public class RandomAlossAbstractionGameStateFactory extends P1RandomAlossAbstractionGameStateFactory {
    @Override
    protected Collection<LinkedList<ISKey>> getMergeCandidates(AlgorithmConfig<? extends SequenceInformationSet> config) {
        Map<Object, List<Pair<Set<Sequence>, LinkedList<ISKey>>>> mergeCandidates = new HashMap<>();

        for (Map.Entry<ISKey, ? extends SequenceInformationSet> entry : config.getAllInformationSets().entrySet()) {
            Triplet<Player, Integer, Integer> key = new Triplet<>(entry.getValue().getPlayer(), getActionCount(entry.getValue()), getIncomingSequenceLength(entry.getValue()));
            List<Pair<Set<Sequence>, LinkedList<ISKey>>> list = mergeCandidates.getOrDefault(key, new ArrayList<>());

            update(list, entry);
            mergeCandidates.put(key, list);
        }
        return mergeCandidates.values().stream().flatMap(Collection::stream).map(Pair::getRight).collect(Collectors.toList());
    }
}
