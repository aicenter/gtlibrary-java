package algorithms.sse;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp.GeneralSumGameBuilder;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.iterativelp.bfs.BFSEnforcingStackelbergLP;
import cz.agents.gtlibrary.domain.bpg.BPGExpander;
import cz.agents.gtlibrary.domain.bpg.BPGGameInfo;
import cz.agents.gtlibrary.domain.bpg.GenSumBPGGameState;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SSESILPTest {

    @Test
    public void BPGTest() {
        BPGGameInfo.DEPTH = 3;
        BPGGameInfo.SLOW_MOVES = true;
        BPGGameInfo.graphFile = "GridW3-almost-connected.txt";//"GridW4.txt";"GridW3-small.txt"
        BPGGameInfo.EVADER_MOVE_COST = 0.5 / (2 * BPGGameInfo.DEPTH);
        BPGGameInfo.DEFENDER_MOVE_COST = 0.5 / (2 * BPGGameInfo.DEPTH);
        GameState root = new GenSumBPGGameState();
        StackelbergConfig config = new StackelbergConfig(root);
        Expander<SequenceInformationSet> expander = new BPGExpander<>(config);

        GeneralSumGameBuilder.build(root, config, expander);
        BFSEnforcingStackelbergLP bfsEnforcingStackelbergLP = new BFSEnforcingStackelbergLP(root.getAllPlayers()[0], new BPGGameInfo());
        double value = bfsEnforcingStackelbergLP.calculateLeaderStrategies(config, expander);
        assertEquals(-0.6666666666666666, value, 1e-6);
    }

    @Test
    public void BPGTest1() {
        BPGGameInfo.DEPTH = 4;
        BPGGameInfo.SLOW_MOVES = true;
        BPGGameInfo.graphFile = "GridW3-almost-connected.txt";//"GridW4.txt";"GridW3-small.txt"
        BPGGameInfo.EVADER_MOVE_COST = 0.5 / (2 * BPGGameInfo.DEPTH);
        BPGGameInfo.DEFENDER_MOVE_COST = 0.5 / (2 * BPGGameInfo.DEPTH);
        GameState root = new GenSumBPGGameState();
        StackelbergConfig config = new StackelbergConfig(root);
        Expander<SequenceInformationSet> expander = new BPGExpander<>(config);

        GeneralSumGameBuilder.build(root, config, expander);
        BFSEnforcingStackelbergLP bfsEnforcingStackelbergLP = new BFSEnforcingStackelbergLP(root.getAllPlayers()[0], new BPGGameInfo());
        double value = bfsEnforcingStackelbergLP.calculateLeaderStrategies(config, expander);
        assertEquals(-0.23791458311791785, value, 1e-6);
    }
}
