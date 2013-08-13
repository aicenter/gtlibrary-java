package cz.agents.gtlibrary.nfg.experimental.domain.bpg;

import cz.agents.gtlibrary.domain.bpg.data.BorderPatrollingGraph;
import cz.agents.gtlibrary.domain.bpg.data.Node;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPStateImpl;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPAction;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPState;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.lang.management.MemoryUsage;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 7/25/13
 * Time: 3:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class BPState extends MDPStateImpl {

    private BorderPatrollingGraph graph;
    private int[] unitNodes;
    private int timeStep = 0;
    final private int UNITS;

    private TreeSet<Integer> flaggedNodesObservedByPatroller;
    private TreeSet<Integer> flaggedNodes;

    private int hash;
    private boolean changed = true;

    public BPState(Player player) {
        super(player);

        graph = new BorderPatrollingGraph();
        flaggedNodesObservedByPatroller = new TreeSet<Integer>();
        flaggedNodes = new TreeSet<Integer>();

        if (player.getId() == 0) {
            UNITS = 1;
            unitNodes = new int[UNITS];
            unitNodes[0] = graph.getOrigin().getIntID();
        } else if (player.getId() == 1) {
            UNITS = 2;
            unitNodes = new int[UNITS];
            unitNodes[0] = graph.getP1Start().getIntID();
            unitNodes[1] = graph.getP2Start().getIntID();
        } else {
            assert false;
            UNITS = -1;
        }
    }

    public BPState(BPState state) {
        super(state.getPlayer());

        this.graph = state.getGraph();
        this.flaggedNodesObservedByPatroller = new TreeSet<Integer>(state.getFlaggedNodesObservedByPatroller());
        this.flaggedNodes = new TreeSet<Integer>(state.getFlaggedNodes());
        this.UNITS = state.getUNITS();
        this.unitNodes = new int[UNITS];
        this.timeStep = state.timeStep;
        for (int i=0; i<UNITS; i++) {
            this.unitNodes[i] = state.unitNodes[i];
        }
    }

    @Override
    public MDPState performAction(MDPAction action) {
        BPState newState = (BPState)this.copy();
        BPAction a = (BPAction)action;

        if (getPlayer().getId() == 0) {
            if (a.getMoves()[0].getFromNode() == -1 && a.getMoves()[0].getToNode() == -1) {
                return newState;
            }
            if (newState.moveUnit(a.getMoves()[0].getUnitNumber(),a.getMoves()[0].getToNode(),null)) {
                newState.incTimeStep();
                return newState;
            }
        } else {
            if ((a.getMoves()[0].getFromNode() == -1 && a.getMoves()[0].getToNode() == -1) &&
                (a.getMoves()[1].getFromNode() == -1 && a.getMoves()[1].getToNode() == -1)){
                return newState;
            }
            if (newState.moveUnit(a.getMoves()[0].getUnitNumber(),a.getMoves()[0].getToNode(),a.getMoves()[0].isWillSeeTheFlag()) &&
                newState.moveUnit(a.getMoves()[1].getUnitNumber(),a.getMoves()[1].getToNode(),a.getMoves()[0].isWillSeeTheFlag())) {
                newState.incTimeStep();
                return newState;
            }
        }
        return null;
    }

    @Override
    public MDPState copy() {
        MDPState result = new BPState(this);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        if (this.hashCode() != obj.hashCode())
            return false;
        BPState other = (BPState)obj;
        if (this.UNITS != other.UNITS)
            return false;
        if (!this.getPlayer().equals(other.getPlayer()))
            return false;
        if (this.flaggedNodes.size() != other.getFlaggedNodes().size())
            return false;
        if (this.flaggedNodesObservedByPatroller.size() != other.getFlaggedNodesObservedByPatroller().size())
            return false;
        for (int i=0; i<UNITS; i++)
            if (this.unitNodes[i] != other.unitNodes[i])
                return false;
        if (!other.getFlaggedNodes().containsAll(this.flaggedNodes))
            return false;
        if (!other.getFlaggedNodesObservedByPatroller().containsAll(this.flaggedNodesObservedByPatroller))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        if (changed) {
            HashCodeBuilder hb = new HashCodeBuilder(17, 31);
            hb.append(getPlayer());
            hb.append(UNITS);
            hb.append(unitNodes);
            hb.append(getTimeStep());
            if (getPlayer().getId() == 0) {
                hb.append(flaggedNodes);
            } else {
                hb.append(flaggedNodesObservedByPatroller);
            }
            hash = hb.toHashCode();
        }
        changed = false;
        return hash;
    }

    public BorderPatrollingGraph getGraph() {
        return graph;
    }

    public int[] getUnitNodes() {
        return unitNodes;
    }

    public int getUNITS() {
        return UNITS;
    }

    public TreeSet<Integer> getFlaggedNodesObservedByPatroller() {
        return flaggedNodesObservedByPatroller;
    }

    public TreeSet<Integer> getFlaggedNodes() {
        return flaggedNodes;
    }

    public void incTimeStep() {
        changed = true;
        timeStep++;
    }

    public void decTimeStep() {
        changed = true;
        timeStep--;
    }


    protected boolean moveUnit(int unitNumber, int targetID, Boolean willSeeFlag) {
        assert (unitNumber < unitNodes.length && unitNumber >= 0);
        Node fromNode = graph.getNodeByID(unitNodes[unitNumber]);
        Node toNode = graph.getNodeByID(targetID);
        assert (fromNode != null);
        assert (toNode != null);
        if (!graph.getGraph().containsEdge(fromNode, toNode))
            return false;

        if (getPlayer().getId() == 0) {
            flaggedNodes.add(unitNodes[unitNumber]);
        } else {
            if (willSeeFlag != null) {
                if (willSeeFlag)
                    flaggedNodesObservedByPatroller.add(targetID);
                else if (!willSeeFlag && flaggedNodesObservedByPatroller.contains(targetID))
                    return false;
            }
        }

        unitNodes[unitNumber] = targetID;
        changed = true;
        return true;
    }

    protected boolean undoUnitMove(int unitNumber, int sourceID, Boolean removeFlag) {
        assert (unitNumber < unitNodes.length && unitNumber >= 0);
        Node fromNode = graph.getNodeByID(sourceID);
        Node toNode = graph.getNodeByID(unitNodes[unitNumber]);
        assert (fromNode != null);
        assert (toNode != null);
        if (!graph.getGraph().containsEdge(fromNode, toNode))
            return false;

        if (getPlayer().getId() == 0) {
            if (removeFlag)
                flaggedNodes.remove(unitNodes[unitNumber]);
        } else {
            if (removeFlag != null) {
                if (removeFlag) {
                    if (!flaggedNodesObservedByPatroller.contains(unitNodes[unitNumber])) {
                        return false;
                    } else {
                        flaggedNodesObservedByPatroller.remove(unitNodes[unitNumber]);
                    }
                }
            }
        }

        unitNodes[unitNumber] = sourceID;
        changed = true;
        return true;
    }

    public int getTimeStep() {
        return timeStep;
    }

    @Override
    public String toString() {
        String result = "BPState:"+getPlayer()+":T="+getTimeStep()+":"+Arrays.toString(getUnitNodes());
        if (getPlayer().getId() == 0) {
            return result + "FL:" + getFlaggedNodes();
        } else {
            return result + "OFL:" + getFlaggedNodesObservedByPatroller();
        }
    }
}
