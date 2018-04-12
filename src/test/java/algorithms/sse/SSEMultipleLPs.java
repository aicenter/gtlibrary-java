package algorithms.sse;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp.GeneralSumGameBuilder;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.iterativelp.ShallowestBrokenCplexStackelbergLP;
import cz.agents.gtlibrary.algorithms.stackelberg.milp.StackelbergSequenceFormMILP;
import cz.agents.gtlibrary.algorithms.stackelberg.multiplelps.StackelbergSequenceFormMultipleLPs;
import cz.agents.gtlibrary.domain.bpg.BPGExpander;
import cz.agents.gtlibrary.domain.bpg.BPGGameInfo;
import cz.agents.gtlibrary.domain.bpg.GenSumBPGGameState;
import cz.agents.gtlibrary.domain.informeraos.InformerAoSExpander;
import cz.agents.gtlibrary.domain.informeraos.InformerAoSGameInfo;
import cz.agents.gtlibrary.domain.informeraos.InformerAoSGameState;
import cz.agents.gtlibrary.domain.poker.kuhn.ExtendedGenSumKPGameState;
import cz.agents.gtlibrary.domain.poker.kuhn.ExtendedKuhnPokerExpander;
import cz.agents.gtlibrary.domain.poker.kuhn.KPGameInfo;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SSEMultipleLPs {
    @Test
    public void extendedKuhnTest() {
        GameState rootState = new ExtendedGenSumKPGameState(0.1);
        StackelbergConfig config = new StackelbergConfig(rootState);
        Expander<SequenceInformationSet> expander = new ExtendedKuhnPokerExpander<>(config);

        GeneralSumGameBuilder.build(rootState, config, expander);
        StackelbergSequenceFormMultipleLPs bfsEnforcingStackelbergLP = new StackelbergSequenceFormMultipleLPs(new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, rootState.getAllPlayers()[0], rootState.getAllPlayers()[1], new KPGameInfo(), expander);
        double value = bfsEnforcingStackelbergLP.calculateLeaderStrategies(config, expander);
        assertEquals(0.8894557823129252, value, 1e-6);
    }

    @Test
    public void IAoSTest() {
        GameState rootState = new InformerAoSGameState();
        StackelbergConfig config = new StackelbergConfig(rootState);
        Expander<SequenceInformationSet> expander = new InformerAoSExpander<>(config);

        GeneralSumGameBuilder.build(rootState, config, expander);
        StackelbergSequenceFormMultipleLPs bfsEnforcingStackelbergLP = new StackelbergSequenceFormMultipleLPs(new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, rootState.getAllPlayers()[0], rootState.getAllPlayers()[1], new KPGameInfo(), expander);
        double value = bfsEnforcingStackelbergLP.calculateLeaderStrategies(config, expander);
        assertEquals(0, value, 1e-6);
    }

    @Test
    public void BPGTest() {
        BPGGameInfo.DEPTH = 3;
        BPGGameInfo.SLOW_MOVES = true;
        BPGGameInfo.graphFile = "GridW3-almost-connected.txt";//"GridW4.txt";"GridW3-small.txt"
        BPGGameInfo.EVADER_MOVE_COST = 0.5/(2*BPGGameInfo.DEPTH);
        BPGGameInfo.DEFENDER_MOVE_COST = 0.5/(2*BPGGameInfo.DEPTH);
        GameState rootState = new GenSumBPGGameState();
        StackelbergConfig config = new StackelbergConfig(rootState);
        Expander<SequenceInformationSet> expander = new BPGExpander<>(config);

        GeneralSumGameBuilder.build(rootState, config, expander);
        StackelbergSequenceFormMultipleLPs bfsEnforcingStackelbergLP = new StackelbergSequenceFormMultipleLPs(new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, rootState.getAllPlayers()[0], rootState.getAllPlayers()[1], new KPGameInfo(), expander);
        double value = bfsEnforcingStackelbergLP.calculateLeaderStrategies(config, expander);
        assertEquals(-0.6666666666666666, value, 1e-6);
    }
}
