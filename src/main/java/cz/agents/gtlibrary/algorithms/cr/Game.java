package cz.agents.gtlibrary.algorithms.cr;

import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNodeImpl;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNodeImpl;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.InnerNode;
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.IIGoofSpielGameState;
import cz.agents.gtlibrary.domain.liarsdice.LDGameInfo;
import cz.agents.gtlibrary.domain.liarsdice.LiarsDiceExpander;
import cz.agents.gtlibrary.domain.liarsdice.LiarsDiceGameState;
import cz.agents.gtlibrary.domain.multilevel.MLExpander;
import cz.agents.gtlibrary.domain.multilevel.MLGameInfo;
import cz.agents.gtlibrary.domain.multilevel.MLGameState;
import cz.agents.gtlibrary.domain.oshizumo.OZGameInfo;
import cz.agents.gtlibrary.domain.oshizumo.OshiZumoExpander;
import cz.agents.gtlibrary.domain.oshizumo.OshiZumoGameState;
import cz.agents.gtlibrary.domain.phantomTTT.TTTExpander;
import cz.agents.gtlibrary.domain.phantomTTT.TTTInfo;
import cz.agents.gtlibrary.domain.phantomTTT.TTTState;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.domain.pursuit.PursuitExpander;
import cz.agents.gtlibrary.domain.pursuit.PursuitGameInfo;
import cz.agents.gtlibrary.domain.pursuit.PursuitGameState;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgame.SimRandomGameState;
import cz.agents.gtlibrary.domain.rps.*;
import cz.agents.gtlibrary.domain.tron.TronExpander;
import cz.agents.gtlibrary.domain.tron.TronGameInfo;
import cz.agents.gtlibrary.domain.tron.TronGameState;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.GameState;

import java.util.Random;

public class Game {
    public final Random rnd;
    public final String domain;
    public final Expander<MCTSInformationSet> expander;
    public final MCTSConfig config;
    public final GameInfo gameInfo;
    public final GameState rootState;
    public InnerNode rootNode;

    public Game(String domain, Random rnd) {
        this.domain = domain;
        this.rnd = rnd;

        config = new MCTSConfig(rnd);
        switch (domain) {
            case "IIGS":
                gameInfo = new GSGameInfo();
                rootState = new IIGoofSpielGameState();
                expander = new GoofSpielExpander<>(config);
                break;
            case "LD":
                gameInfo = new LDGameInfo();
                rootState = new LiarsDiceGameState();
                expander = new LiarsDiceExpander<>(config);
                break;
            case "GP":
                gameInfo = new GPGameInfo();
                rootState = new GenericPokerGameState();
                expander = new GenericPokerExpander<>(config);
                break;
            case "PE":
                gameInfo = new PursuitGameInfo();
                rootState = new PursuitGameState();
                expander = new PursuitExpander<>(config);
                break;
            case "OZ":
                gameInfo = new OZGameInfo();
                rootState = new OshiZumoGameState();
                expander = new OshiZumoExpander<>(config);
                break;
            case "RG":
                gameInfo = new RandomGameInfo();
                rootState = new SimRandomGameState();
                expander = new RandomGameExpander<>(config);
                break;
            case "Tron":
                gameInfo = new TronGameInfo();
                rootState = new TronGameState();
                expander = new TronExpander<>(config);
                break;
            case "PTTT":
                gameInfo = new TTTInfo();
                rootState = new TTTState();
                expander = new TTTExpander(config);
                break;
            case "RPS":
            case "BRPS":
                gameInfo = new RPSGameInfo();
                rootState = new RPSGameState();
                expander = new RPSExpander<>(config);
                break;
            case "ML":
                gameInfo = new MLGameInfo();
                rootState = new MLGameState();
                expander = new MLExpander<>(config);
                break;
            default:
                throw new IllegalArgumentException("Incorrect game:" + domain);
        }

        expander.setGameInfo(gameInfo);
    }

    public Game(Game game) {
        this.domain = game.domain;
        this.rnd = game.rnd;

        config = game.config.clone();
        switch (domain) {
            case "IIGS":
                gameInfo = new GSGameInfo();
                rootState = new IIGoofSpielGameState();
                expander = new GoofSpielExpander<>(config);
                break;
            case "LD":
                gameInfo = new LDGameInfo();
                rootState = new LiarsDiceGameState();
                expander = new LiarsDiceExpander<>(config);
                break;
            case "GP":
                gameInfo = new GPGameInfo();
                rootState = new GenericPokerGameState();
                expander = new GenericPokerExpander<>(config);
                break;
            case "PE":
                gameInfo = new PursuitGameInfo();
                rootState = new PursuitGameState();
                expander = new PursuitExpander<>(config);
                break;
            case "OZ":
                gameInfo = new OZGameInfo();
                rootState = new OshiZumoGameState();
                expander = new OshiZumoExpander<>(config);
                break;
            case "RG":
                gameInfo = new RandomGameInfo();
                rootState = new SimRandomGameState();
                expander = new RandomGameExpander<>(config);
                break;
            case "Tron":
                gameInfo = new TronGameInfo();
                rootState = new TronGameState();
                expander = new TronExpander<>(config);
                break;
            case "PTTT":
                gameInfo = new TTTInfo();
                rootState = new TTTState();
                expander = new TTTExpander(config);
                break;
            case "RPS":
            case "BRPS":
                gameInfo = new RPSGameInfo();
                rootState = new RPSGameState();
                expander = new RPSExpander<>(config);
                break;
            case "ML":
                gameInfo = new MLGameInfo();
                rootState = new MLGameState();
                expander = new MLExpander<>(config);
                break;
            default:
                throw new IllegalArgumentException("Incorrect game:" + domain);
        }

        expander.setGameInfo(gameInfo);
    }

    public Game deepClone() {
        return new Game(this);
    }

    public Game clone() {
        return new Game(this.domain, this.rnd);
    }
    public Game clone(Random rnd) {
        return new Game(this.domain, rnd);
    }

    public InnerNode getRootNode() {
        if(rootNode == null) {
            if (rootState.isPlayerToMoveNature()) {
                rootNode = new ChanceNodeImpl(expander, rootState, rnd);
            } else {
                rootNode = new InnerNodeImpl(expander, rootState);
            }
        }
        return rootNode;
    }
}
