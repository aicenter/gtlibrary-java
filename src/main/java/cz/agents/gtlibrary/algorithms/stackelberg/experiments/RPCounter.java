package cz.agents.gtlibrary.algorithms.stackelberg.experiments;

import cz.agents.gtlibrary.algorithms.sequenceform.FullSequenceEFG;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.domain.bpg.BPGExpander;
import cz.agents.gtlibrary.domain.bpg.BPGGameInfo;
import cz.agents.gtlibrary.domain.bpg.GenSumBPGGameState;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.io.GambitEFG;

import java.util.Set;

public class RPCounter {

    public static void main(String[] args) {
//        GameState root = new StackTestGameState();
//        SequenceFormConfig<SequenceInformationSet> config = new SequenceFormConfig<>();
//        Expander<SequenceInformationSet> expander = new StackTestExpander<>(config);
//        FullSequenceEFG efg = new FullSequenceEFG(root, expander, new StackTestGameInfo(), config);
//
//        efg.generateCompleteGame();
        GameState root = new GenSumBPGGameState();
        SequenceFormConfig<SequenceInformationSet> config = new SequenceFormConfig<>();
        Expander<SequenceInformationSet> expander = new BPGExpander<>(config);
        FullSequenceEFG efg = new FullSequenceEFG(root, expander, new BPGGameInfo(), config);
//
        efg.generateCompleteGame();
        System.out.println(config.getSequencesFor(root.getAllPlayers()[0]).size());
        System.out.println(config.getSequencesFor(root.getAllPlayers()[1]).size());
        System.out.println("computing");
        System.out.println(count(config, expander, config.getInformationSetFor(root), root.getAllPlayers()[0]));
    }

    public static long count(SequenceFormConfig<SequenceInformationSet> config, Expander<SequenceInformationSet> expander, SequenceInformationSet set, Player player) {
        if (set.getPlayer().equals(player)) {
            long count = 0;

            if (allEnd(set.getAllStates()))
                return 1;
            for (Action action : expander.getActions(set)) {
                Sequence continuation = new ArrayListSequenceImpl(set.getPlayersHistory());
                long actionCount = 1;

                continuation.addLast(action);
                Set<SequenceInformationSet> reachableSets = config.getReachableSets(continuation);

                if (reachableSets != null)
                    for (SequenceInformationSet informationSet : reachableSets) {
                        actionCount *= count(config, expander, informationSet, player);
                    }
//                else
//                    assert false;
                count += actionCount;
            }
            return count;
        } else {
            long count = 1;

            if (allEnd(set.getAllStates()))
                return 1;
            for (SequenceInformationSet informationSet : config.getReachableSets(new ArrayListSequenceImpl(player))) {
                count *= count(config, expander, informationSet, player);
            }
            return count;
        }
    }

    private static boolean allEnd(Set<GameState> allStates) {
        for (GameState state : allStates) {
            if(!state.isGameEnd())
                return false;
        }
        return true;
    }

}
