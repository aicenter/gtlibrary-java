package cz.agents.gtlibrary.domain.randomabstraction;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.Triplet;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class RandomAbstractionGameStateFactory extends P1RandomAbstractionGameStateFactory {

    @Override
    protected Collection<LinkedList<ISKey>> getMergeCandidates(AlgorithmConfig<? extends SequenceInformationSet> config) {
        Map<Object, LinkedList<ISKey>> mergeCandidates = new HashMap<>();

        for (Map.Entry<ISKey, ? extends SequenceInformationSet> entry : config.getAllInformationSets().entrySet()) {

            Triplet<Player, Integer, Integer> key = new Triplet<>(entry.getValue().getPlayer(), getActionCount(entry.getValue()), getIncomingSequenceLength(entry.getValue()));
            LinkedList<ISKey> list = mergeCandidates.getOrDefault(key, new LinkedList<>());

            list.add(entry.getKey());
            mergeCandidates.put(key, list);
        }
        return mergeCandidates.values();
    }
}
