package cz.agents.gtlibrary.domain.bpg;

import cz.agents.gtlibrary.algorithms.stackelberg.GeneralSumBestResponse;
import cz.agents.gtlibrary.domain.bpg.data.GenSumBorderPatrollingGraph;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.utils.graph.Node;

public class GenSumBPGGameState extends BPGGameState {

    private double evaderPenalty;

    public GenSumBPGGameState() {
        super(new GenSumBorderPatrollingGraph(BPGGameInfo.graphFile));
        evaderPenalty = 0;
    }

    public GenSumBPGGameState(GenSumBPGGameState state) {
        super(state);
        evaderPenalty = state.evaderPenalty;
    }

    public void executeAttackerAction(AttackerAction action) {
        super.executeAttackerAction(action);
        if(action.getType().equals(AttackerAction.AttackerMovementType.SLOW))
            evaderPenalty += BPGGameInfo.EVADER_MOVE_COST;
        else if(action.getType().equals(AttackerAction.AttackerMovementType.QUICK))
            evaderPenalty += 2*BPGGameInfo.EVADER_MOVE_COST;
    }

    @Override
    public double[] getUtilities() {
        if (getPlayerToMove().equals(BPGGameInfo.ATTACKER)) {
            PatrollerAction patrollerAction = (PatrollerAction) history.getSequenceOf(BPGGameInfo.DEFENDER).getLast();
            AttackerAction attackerAction = (AttackerAction) history.getSequenceOf(BPGGameInfo.ATTACKER).getLast();

            if (isCaught(patrollerAction, attackerAction))
                return new double[]{-ATTACKER_WINS - evaderPenalty, ATTACKER_WINS};
        }
        if (attackerPosition.equals(graph.getDestination())) {
            double utilityChange = ((GenSumBorderPatrollingGraph) graph).getEvaderUtilityChange(getPreviousAttackerNode());

            return new double[]{ATTACKER_WINS + utilityChange - evaderPenalty, -ATTACKER_WINS};
        }
        return new double[]{OPEN_POSITION, OPEN_POSITION};
    }

    private Node getPreviousAttackerNode() {
        return ((AttackerAction) getSequenceFor(BPGGameInfo.ATTACKER).getLast()).getFromNode();
    }

    @Override
    public GameState copy() {
        return new GenSumBPGGameState(this);
    }
}
