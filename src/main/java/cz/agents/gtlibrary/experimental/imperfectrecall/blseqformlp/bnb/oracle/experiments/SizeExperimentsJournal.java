package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.experiments;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.domain.bpg.BPGExpander;
import cz.agents.gtlibrary.domain.bpg.BPGGameInfo;
import cz.agents.gtlibrary.domain.bpg.BPGGameState;
import cz.agents.gtlibrary.domain.bpg.imperfectrecall.IRBPGGameState;
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielGameState;
import cz.agents.gtlibrary.domain.goofspiel.ir.IRGoofSpielGameState;
import cz.agents.gtlibrary.domain.oshizumo.OZGameInfo;
import cz.agents.gtlibrary.domain.oshizumo.OshiZumoExpander;
import cz.agents.gtlibrary.domain.oshizumo.OshiZumoGameState;
import cz.agents.gtlibrary.domain.oshizumo.ir.IROshiZumoGameState;
import cz.agents.gtlibrary.domain.phantomTTT.TTTExpander;
import cz.agents.gtlibrary.domain.phantomTTT.TTTInfo;
import cz.agents.gtlibrary.domain.phantomTTT.TTTState;
import cz.agents.gtlibrary.domain.phantomTTT.imperfectrecall.IRTTTState;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRConfig;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRInformationSet;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.utils.BasicGameBuilder;

import java.util.stream.IntStream;

public class SizeExperimentsJournal {
    public static void main(String[] args) {
        if (args[0].equals("BPG"))
            measureBPG(args);
        else if (args[0].equals("TTT"))
            measureTTT(args);
        else if (args[0].equals("BPGPR"))
            measureBPGPR(args);
        else if (args[0].equals("TTTPR"))
            measureTTTPR(args);
        else if (args[0].equals("GS"))
            measureGS(args);
        else if (args[0].equals("GSPR"))
            measureGSPR(args);
        else if (args[0].equals("OZ"))
            measureOZ(args);
        else if (args[0].equals("OZPR"))
            measureOZPR(args);
    }

    private static void measureTTT(String[] args) {
        SequenceFormIRConfig config = new SequenceFormIRConfig(new TTTInfo());
        GameState root = new IRTTTState();
        Expander<SequenceFormIRInformationSet> expander = new TTTExpander<>(config);
        IRTTTState.REMEMBERED_MOVES = Integer.parseInt(args[1]);

        BasicGameBuilder.build(root, config, expander);

        System.out.println("IR IS count: " + config.getAllInformationSets().size());
        System.out.println("sequences: " + config.getSequencesFor(BPGGameInfo.DEFENDER).size());

//        expander = null;
//        config = null;
//        System.gc();
//
//        root = new BPGGameState();
//        SequenceFormConfig<SequenceInformationSet> config1 = new SequenceFormConfig<>();
//        Expander<SequenceInformationSet> expander1 = new BPGExpander<>(config1);
//
//        BasicGameBuilder.build(root, config1, expander1);
//        System.out.println("PR IS count: " + config1.getAllInformationSets().size());
//        System.out.println("sequences: " + config1.getSequencesFor(BPGGameInfo.DEFENDER).size());
    }

    private static void measureTTTPR(String[] args) {
        GameState root = new TTTState();
        SequenceFormConfig<SequenceInformationSet> config1 = new SequenceFormConfig<>();
        Expander<SequenceInformationSet> expander1 = new TTTExpander<>(config1);

        BasicGameBuilder.build(root, config1, expander1);
        System.out.println("PR IS count: " + config1.getAllInformationSets().size());
        System.out.println("sequences: " + config1.getSequencesFor(TTTInfo.XPlayer).size());
    }

    private static void measureBPG(String[] args) {
        BPGGameInfo.DEPTH = Integer.parseInt(args[2]);
        SequenceFormIRConfig config = new SequenceFormIRConfig(new BPGGameInfo());
        GameState root = new IRBPGGameState();
        Expander<SequenceFormIRInformationSet> expander = new BPGExpander<>(config);
        IRBPGGameState.REMEMBERED_MOVES = Integer.parseInt(args[1]);

        BasicGameBuilder.build(root, config, expander);

        System.out.println("IR IS count: " + config.getAllInformationSets().size());
        System.out.println("P1 IS count: " + config.getAllInformationSets().values().stream().filter(i -> i.getPlayer().equals(BPGGameInfo.DEFENDER)).count());
        System.out.println("sequences: " + config.getSequencesFor(BPGGameInfo.DEFENDER).size());
    }

    private static void measureBPGPR(String[] args) {
        BPGGameInfo.DEPTH = Integer.parseInt(args[2]);

        GameState root = new BPGGameState();
        SequenceFormConfig<SequenceInformationSet> config1 = new SequenceFormConfig<>();
        Expander<SequenceInformationSet> expander1 = new BPGExpander<>(config1);

        BasicGameBuilder.build(root, config1, expander1);
        System.out.println("PR IS count: " + config1.getAllInformationSets().size());
        System.out.println("P1 IS count: " + config1.getAllInformationSets().values().stream().filter(i -> i.getPlayer().equals(BPGGameInfo.DEFENDER)).count());
        System.out.println("sequences: " + config1.getSequencesFor(BPGGameInfo.DEFENDER).size());
    }

    private static void measureGS(String[] args) {
        GSGameInfo.depth = Integer.parseInt(args[1]);
        GSGameInfo.CARDS_FOR_PLAYER = IntStream.range(1, GSGameInfo.depth + 1).toArray();
        IRGoofSpielGameState.REMEMBERED_MOVES = Integer.parseInt(args[2]);

        GSGameInfo info = new GSGameInfo();
        GameState root = new IRGoofSpielGameState();
        SequenceFormIRConfig config1 = new SequenceFormIRConfig(info);
        Expander<SequenceFormIRInformationSet> expander1 = new GoofSpielExpander<>(config1);

        BasicGameBuilder.build(root, config1, expander1);
        System.out.println("IR IS count: " + config1.getAllInformationSets().size());
        System.out.println("P1 IS count: " + config1.getAllInformationSets().values().stream().filter(i -> i.getPlayer().equals(GSGameInfo.FIRST_PLAYER)).count());
        System.out.println("sequences: " + config1.getSequencesFor(GSGameInfo.FIRST_PLAYER).size());
        System.out.println("sequences: " + config1.getSequencesFor(GSGameInfo.SECOND_PLAYER).size());
    }

    private static void measureGSPR(String[] args) {
        GSGameInfo.depth = Integer.parseInt(args[1]);
        GSGameInfo.CARDS_FOR_PLAYER = IntStream.range(1, GSGameInfo.depth + 1).toArray();

        new GSGameInfo();
        GameState root = new GoofSpielGameState();
        SequenceFormConfig<SequenceInformationSet> config1 = new SequenceFormConfig<>();
        Expander<SequenceInformationSet> expander1 = new GoofSpielExpander<>(config1);

        BasicGameBuilder.build(root, config1, expander1);
        System.out.println("PR IS count: " + config1.getAllInformationSets().size());
        System.out.println("P1 IS count: " + config1.getAllInformationSets().values().stream().filter(i -> i.getPlayer().equals(GSGameInfo.FIRST_PLAYER)).count());
        System.out.println("sequences: " + config1.getSequencesFor(GSGameInfo.FIRST_PLAYER).size());
    }

    private static void measureOZ(String[] args) {
        OZGameInfo.startingCoins= Integer.parseInt(args[1]);
        OZGameInfo.locK = Integer.parseInt(args[2]);
        OZGameInfo.minBid = Integer.parseInt(args[3]);
        IROshiZumoGameState.REMEMBERED_MOVES = Integer.parseInt(args[4]);

        GSGameInfo info = new GSGameInfo();
        GameState root = new IROshiZumoGameState();
        SequenceFormIRConfig config1 = new SequenceFormIRConfig(info);
        Expander<SequenceFormIRInformationSet> expander1 = new OshiZumoExpander<>(config1);

        BasicGameBuilder.build(root, config1, expander1);
        System.out.println("IR IS count: " + config1.getAllInformationSets().size());
        System.out.println("P1 IS count: " + config1.getAllInformationSets().values().stream().filter(i -> i.getPlayer().equals(OZGameInfo.FIRST_PLAYER)).count());
        System.out.println("sequences: " + config1.getSequencesFor(OZGameInfo.FIRST_PLAYER).size());
        System.out.println("sequences: " + config1.getSequencesFor(OZGameInfo.SECOND_PLAYER).size());
    }

    private static void measureOZPR(String[] args) {
        OZGameInfo.startingCoins= Integer.parseInt(args[1]);
        OZGameInfo.locK = Integer.parseInt(args[2]);
        OZGameInfo.minBid = Integer.parseInt(args[3]);
        IROshiZumoGameState.REMEMBERED_MOVES = Integer.parseInt(args[4]);

        GameState root = new OshiZumoGameState();
        SequenceFormConfig<SequenceInformationSet> config1 = new SequenceFormConfig<>();
        Expander<SequenceInformationSet> expander1 = new OshiZumoExpander<>(config1);

        BasicGameBuilder.build(root, config1, expander1);
        System.out.println("PR IS count: " + config1.getAllInformationSets().size());
        System.out.println("P1 IS count: " + config1.getAllInformationSets().values().stream().filter(i -> i.getPlayer().equals(OZGameInfo.FIRST_PLAYER)).count());
        System.out.println("sequences: " + config1.getSequencesFor(OZGameInfo.FIRST_PLAYER).size());
    }



}
