package cz.agents.gtlibrary.domain.bpg;

import cz.agents.gtlibrary.domain.bpg.data.GenSumBorderPatrollingGraph;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.utils.graph.Node;

public class GenSumBPGGameState extends BPGGameState {

    private double evaderPenalty;
    private double defenderPenalty;

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
        if (action.getType().equals(AttackerAction.AttackerMovementType.SLOW))
            evaderPenalty += BPGGameInfo.EVADER_MOVE_COST;
        else if (action.getType().equals(AttackerAction.AttackerMovementType.QUICK))
            evaderPenalty += 2 * BPGGameInfo.EVADER_MOVE_COST;
    }

    @Override
    public void executePatrollerAction(PatrollerAction action) {
        super.executePatrollerAction(action);
        defenderPenalty += BPGGameInfo.DEFENDER_MOVE_COST;
    }

    @Override
    public double[] getUtilities() {
        if (getPlayerToMove().equals(BPGGameInfo.ATTACKER)) {
            PatrollerAction patrollerAction = (PatrollerAction) history.getSequenceOf(BPGGameInfo.DEFENDER).getLast();
            AttackerAction attackerAction = (AttackerAction) history.getSequenceOf(BPGGameInfo.ATTACKER).getLast();

            if (isCaughtByP1(patrollerAction, attackerAction))
                return new double[]{-ATTACKER_WINS - evaderPenalty, ATTACKER_WINS - defenderPenalty + 0.2};
            if (isCaughtByP2(patrollerAction, attackerAction))
                return new double[]{-ATTACKER_WINS - evaderPenalty, ATTACKER_WINS - defenderPenalty - 0.1};
//            if (isCaught(patrollerAction, attackerAction))
//                return new double[]{-ATTACKER_WINS - evaderPenalty, ATTACKER_WINS - defenderPenalty};
        }
        if (attackerPosition.equals(graph.getDestination())) {
            double utilityChange = ((GenSumBorderPatrollingGraph) graph).getEvaderUtilityChange(getPreviousAttackerNode());

            return new double[]{ATTACKER_WINS + utilityChange - evaderPenalty, -ATTACKER_WINS - defenderPenalty};
        }
        return new double[]{OPEN_POSITION - evaderPenalty, OPEN_POSITION - defenderPenalty};
    }

    private boolean isCaughtByP2(PatrollerAction patrollerAction, AttackerAction attackerAction) {
        return caughtInNodeByP2(patrollerAction, attackerAction) || caughtOnEdgeByP2(patrollerAction, attackerAction);
    }

    private boolean caughtInNodeByP2(PatrollerAction patrollerAction, AttackerAction attackerAction) {
        return patrollerAction.getFromNodeForP2().equals(attackerAction.getToNode());
    }

    private boolean isCaughtByP1(PatrollerAction patrollerAction, AttackerAction attackerAction) {
        return caughtInNodeByP1(patrollerAction, attackerAction) || caughtOnEdgeByP1(patrollerAction, attackerAction);
    }

    private boolean caughtInNodeByP1(PatrollerAction patrollerAction, AttackerAction attackerAction) {
        return patrollerAction.getToNodeForP1().equals(attackerAction.getToNode());
    }

    private Node getPreviousAttackerNode() {
        return ((AttackerAction) getSequenceFor(BPGGameInfo.ATTACKER).getLast()).getFromNode();
    }

    @Override
    public GameState copy() {
        return new GenSumBPGGameState(this);
    }
}
