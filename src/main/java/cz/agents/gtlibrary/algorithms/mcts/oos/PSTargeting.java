/*
Copyright 2014 Faculty of Electrical Engineering at CTU in Prague

This file is part of Game Theoretic Library.

Game Theoretic Library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Game Theoretic Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.*/

package cz.agents.gtlibrary.algorithms.mcts.oos;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.MCTSPublicState;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStratDist;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.LeafNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.Node;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielGameState;
import cz.agents.gtlibrary.interfaces.*;

import java.util.List;

/**
 *
 * @author vilo
 */
public class PSTargeting implements OOSTargeting {
    History sampleHist;
    InnerNode rootNode;
    double delta;
    private MCTSPublicState samplePs;
    private List<PublicState> samplePsHist;

    public PSTargeting(InnerNode rootNode, double delta) {
        this.rootNode = rootNode;
        this.delta = delta;
    }
    
    @Override
    public boolean isAllowedAction(InnerNode node, Action action) {
        if (!(action instanceof PublicAction)) return true;
        Player pl = node.getGameState().getPlayerToMove();
        if (node.getGameState().getSequenceFor(pl).size() >= sampleHist.getSequenceOf(pl).size()) return true; //all actions are allowed here
        Action histAct = sampleHist.getSequenceOf(pl).get(node.getGameState().getSequenceFor(pl).size());//in other games, this might need to be more sophisticated
        return ((PublicAction)action).publicEquals(histAct);
    }


//    @Override
//    public boolean isAllowedAction(InnerNode node, Action action) {
//        List<PublicState> nextPsHistory = node.getPublicState().getPsHistory();
//        PublicState lastSamplePs = samplePsHist.get(samplePsHist.size()-1);
//        if(!nextPsHistory.contains(lastSamplePs)) return false;
//
//        Node next = node.getChildFor(action);
//        if(next instanceof LeafNode) return true;
//
//        InnerNode nextNode = (InnerNode) next;
//        nextPsHistory = nextNode.getPublicState().getPsHistory();
//        return nextPsHistory.contains(lastSamplePs);
//    }

//    @Override
//    public boolean isAllowedAction(InnerNode node, Action action) {
////        System.out.println("smpl: "+samplePsHist.stream().map(PublicState::toString)
////                .reduce("", String::concat));
////        System.out.println("node: "+node.getPublicState().getPsHistory().stream().map(PublicState::toString)
////                .reduce("", String::concat));
//
//        Player pl = node.getGameState().getPlayerToMove();
//        if (node.getGameState().getSequenceFor(pl).size() >= sampleHist.getSequenceOf(pl).size()) {
////            System.out.println("allw: true");
//            return true; //all actions are allowed here
//        }
//
//        Node next = node.getChildFor(action);
//        if(next instanceof LeafNode) {
////            System.out.println("allw: true");
//            return true;
//        }
//
//        InnerNode nextNode = (InnerNode) next;
////        System.out.println("next: "+nextNode.getPublicState().getPsHistory().stream().map(PublicState::toString)
////                .reduce("", String::concat));
//        List<PublicState> nodePsHistory = nextNode.getPublicState().getPsHistory();
//        int pspos = nodePsHistory.size()-1;
////        System.out.println("allw: "+samplePsHist.get(pspos).equals(nodePsHistory.get(pspos)));
//        return samplePsHist.get(pspos).equals(nodePsHistory.get(pspos));
////
////        // action is allowed if the last public state of node OR next node is contained
////        // in sequence of public states stated by the current information set
////
////        // check if node is on path to current PS
////        List<PublicState> nodePsHistory = node.getPublicState().getPsHistory();
////        PublicState lastSamplePs = nodePsHistory.get(nodePsHistory.size()-1);
//////        System.out.println("smpl: "+samplePsHist.stream().map(PublicState::toString)
//////                .reduce("", String::concat));
//////        System.out.println("node: "+nodePsHistory.stream().map(PublicState::toString)
//////                .reduce("", String::concat));
////        if(nodePsHistory.contains(samplePsHist.get(samplePsHist.size()-1))) return true;
////
////        if(!samplePsHist.contains(lastSamplePs)) return false;
////        if(samplePsHist.get(samplePsHist.size()-1).equals(lastSamplePs)) return true;
////
////        // check if next node is on path to current PS
////
//////        System.out.println("smpl: "+samplePsHist.stream().map(PublicState::toString)
//////                .reduce("", String::concat));
//////        System.out.println("actn: "+nodePsHistory.stream().map(PublicState::toString)
//////                .reduce("", String::concat));
//
//    }

    private double probMultiplayer = 1;
    @Override
    public double getSampleProbMultiplayer(){
        return probMultiplayer;
    }
    
    private Action getSingleAllowedAction(InnerNode n){
        Action out=null;
        for (Action a : n.getActions()){
            if (isAllowedAction(n, a)){
                if (out == null) out = a;
                else return null;
            }
        }
        return out;
    }

    
    private MeanStratDist meanDist = new MeanStratDist();
    @Override
    public void update(InformationSet curIS) {
        GameState gs = curIS.getAllStates().iterator().next();
        sampleHist = gs.getHistory();
        samplePsHist = ((MCTSInformationSet) curIS).getAllNodes().iterator().next().getPublicState().getPsHistory();
        
        probMultiplayer = 1;
        InnerNode n = rootNode;
        while (n.getGameState().getHistory().getLength() < sampleHist.getLength()){
            final Action selected = getSingleAllowedAction(n);
            if (selected != null){
                double p;
                if (n.getGameState().isPlayerToMoveNature()) p = delta / n.getGameState().getProbabilityOfNatureFor(selected);
                else p = delta / meanDist.getDistributionFor(n.getInformationSet().getAlgorithmData()).get(selected);
                p += (1-delta);
                probMultiplayer *= p;
            }
            final Action played = sampleHist.getSequenceOf(n.getGameState().getPlayerToMove()).get(n.getGameState().getSequenceForPlayerToMove().size());
            n = (InnerNode) n.getChildOrNull(played);
        }
    }

    @Override
    public String toString() {
        return "PST(" + sampleHist + ')';
    }
    
}
