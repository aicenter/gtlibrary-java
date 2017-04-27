package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.experiments;

import cz.agents.gtlibrary.domain.bpg.BPGExpander;
import cz.agents.gtlibrary.domain.bpg.BPGGameInfo;
import cz.agents.gtlibrary.domain.bpg.imperfectrecall.IRBPGGameState;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRConfig;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRInformationSet;
import cz.agents.gtlibrary.iinodes.ConfigImpl;
import cz.agents.gtlibrary.iinodes.IRInformationSetImpl;
import cz.agents.gtlibrary.iinodes.InformationSetImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.BasicGameBuilder;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SizeExperiment {
    public static void main(String[] args) {
        Set<Sequence> p1Sequences = new HashSet<>(1000000);
        Set<Sequence> p2Sequences = new HashSet<>(100000);
        BPGGameInfo.DEPTH = Integer.parseInt(args[0]);
        BasicGameBuilder builder = new BasicGameBuilder();
        ConfigImpl config = new ConfigImpl<IRInformationSetImpl>() {
            @Override
            public IRInformationSetImpl createInformationSetFor(GameState gameState) {
                return new IRInformationSetImpl(gameState);
            }
        };
        GameState root = new IRBPGGameState();
        Expander<SequenceFormIRInformationSet> expander = new BPGExpander<>(config);

        ArrayDeque<GameState> queue = new ArrayDeque<>();
        int stateCounter = 0;

        queue.add(root);
        while (queue.size() > 0) {
            GameState currentState = queue.removeLast();

            stateCounter++;
            if(stateCounter % 100000 == 0) {
                System.out.println("states: " + stateCounter);
                System.out.println("P1 sequences: " + p1Sequences.size());
                System.out.println("P2 sequences: " + p2Sequences.size());
            }
            p1Sequences.add(currentState.getSequenceFor(BPGGameInfo.DEFENDER));
            p2Sequences.add(currentState.getSequenceFor(BPGGameInfo.ATTACKER));
            config.addInformationSetFor(currentState);
            if (currentState.isGameEnd())
                continue;
            queue.addAll(expander.getActions(currentState).stream().map(currentState::performAction).collect(Collectors.toList()));
        }
        System.out.println("State count: " + stateCounter);
        builder.build(root, config, expander);
//        System.out.println("Information sets: " + config.getCountIS(0));
        System.out.println("Sequences P1: " + p1Sequences.size());
        System.out.println("Sequences P2: " + p2Sequences.size());
    }
}
