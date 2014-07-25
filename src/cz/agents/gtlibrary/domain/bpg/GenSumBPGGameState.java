package cz.agents.gtlibrary.domain.bpg;

import cz.agents.gtlibrary.domain.bpg.data.GenSumBorderPatrollingGraph;
import cz.agents.gtlibrary.utils.graph.Node;

public class GenSumBPGGameState extends BPGGameState {

    public GenSumBPGGameState() {
        super(new GenSumBorderPatrollingGraph(BPGGameInfo.graphFile));
    }

    @Override
    public double[] getUtilities() {
        if (getPlayerToMove().equals(BPGGameInfo.ATTACKER)) {
            PatrollerAction patrollerAction = (PatrollerAction) history.getSequenceOf(BPGGameInfo.DEFENDER).getLast();
            AttackerAction attackerAction = (AttackerAction) history.getSequenceOf(BPGGameInfo.ATTACKER).getLast();

            if (isCaught(patrollerAction, attackerAction))
                return new double[] { -ATTACKER_WINS, ATTACKER_WINS };
        }
        if (attackerPosition.equals(graph.getDestination())) {
            double utilityChange = ((GenSumBorderPatrollingGraph)graph).getEvaderUtilityChange(getPreviousAttackerNode());

            return new double[]{ATTACKER_WINS + utilityChange, -ATTACKER_WINS};
        }
        return new double[] { OPEN_POSITION, OPEN_POSITION };
    }

    private Node getPreviousAttackerNode() {
        return ((AttackerAction)getSequenceFor(BPGGameInfo.ATTACKER).getLast()).getFromNode();
    }
}
