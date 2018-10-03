package cz.agents.gtlibrary.algorithms.stackelberg.multiplelps.siterator;

import cz.agents.gtlibrary.algorithms.cfr.br.responses.AbstractActionProvider;
import cz.agents.gtlibrary.algorithms.cfr.br.responses.AbstractObservationProvider;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.multiplelps.FeasibilitySequenceFormLP;
import cz.agents.gtlibrary.algorithms.stackelberg.multiplelps.rpiterator.DepthPureRealPlanIterator;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.Quadruple;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by Jakub Cerny on 18/07/2018.
 */
public class SmallSchemaGeneratingIterator extends DepthPureRealPlanIterator {

    private final int maxNumberOfStates;

    // schema : behavioral function and transitions
    private Object[] actions;
    private HashMap<Object, Integer>[] transitions;

    private HashSet<HashSet<Sequence>> smallSchemata;
    private ArrayList<HashSet<Sequence>> smallSchemataList;

//    private
    private Stack<Quadruple<GameState, Action, Pair<Integer,Integer>,Boolean>> schemaUpdates;
    private Quadruple<GameState, Action, Pair<Integer,Integer>,Boolean> deletedUpdate;
    private boolean changeSuccessful;

    private HashSet<Integer> blacklist;

    private int nextSchemaIntex;
    private int numberOfGenerated;

    private int[] abstractActions;
    private int[][] abstractObservations;

    protected final boolean VERBOSE = false;


    public SmallSchemaGeneratingIterator(Player player, StackelbergConfig config, Expander<SequenceInformationSet> expander, FeasibilitySequenceFormLP solver, int numberOfStates) {
        super(player, config, expander, solver);
        maxNumberOfStates = numberOfStates;
        actions = new Object[maxNumberOfStates+1];
        transitions = new HashMap[maxNumberOfStates+1];
        abstractActions = new int[maxNumberOfStates+1];
        for(int i = 0; i <= maxNumberOfStates; i++) transitions[i] = new HashMap<>();
        schemaUpdates = new Stack<>();
        blacklist = new HashSet<>();

        numberOfGenerated = 0;
        ArrayList<Object> fobservations = checkNumberOfObservations();
        System.out.printf("Generating all schemata of size " + maxNumberOfStates + "...");
        smallSchemata = new HashSet<>();
        ArrayList<Object> factions = getFollowersActionAbstractions();

        /* 01
        treeRecursion(0);
         */

        /* 02
        while (hasNextActions(factions)){
            for(int i = 1; i <= maxNumberOfStates; i++)
                actions[i] = factions.get(abstractActions[i]);
//            System.out.printf(Arrays.toString(abstractActions));
            treeRecursion(0);
            getNextActions(factions);
        }
         */

        /* 03 */
        //abstractObservations = new int[maxNumberOfStates+1][fobservations.size()];
        for(Object obs : fobservations) transitions[0].put(obs,1);
        while (hasNextActions(factions)){
            boolean hasSameAction = true;
            for(int i = 1; i <= maxNumberOfStates; i++) {
                actions[i] = factions.get(abstractActions[i]);
                if(!actions[i].equals(actions[1])) hasSameAction = false;
            }
            if (VERBOSE) System.out.println(Arrays.toString(abstractActions));
            /* if actions are the same, just one */
            if(hasSameAction){
                for(Object obs : fobservations) transitions[1].put(obs,1);
                addNextStrategy();
                if (VERBOSE) System.out.println("******************************");
            }
            else{
                abstractObservations = new int[maxNumberOfStates+1][fobservations.size()];
                abstractObservations[1][fobservations.size()-1] = 1;
                while(hasNextObservation()){
                    for(int state = 1; state < abstractObservations.length; state++){
                        if (VERBOSE) System.out.println(Arrays.toString(abstractObservations[state]));
                        for(int idx = 0; idx < abstractObservations[0].length; idx++)
                            transitions[state].put(fobservations.get(idx), abstractObservations[state][idx]+1);
                    }
                    addNextStrategy();
                    getNextObservation();
                    if (VERBOSE) System.out.println("******************************");
                }

            }

            getNextActions(factions);
        }
        smallSchemataList = new ArrayList<>(smallSchemata);
        System.out.println("done.");
        System.out.println("# of schemata generated: " + numberOfGenerated);
        nextSchemaIntex = 0;
    }

    protected void addNextStrategy(){
        numberOfGenerated++;
        HashSet<Sequence> currentSet = getSetOfSequences();
        smallSchemata.add(currentSet);
        if(numberOfGenerated % 500 == 0)
            System.out.printf(smallSchemata.size()+"/"+numberOfGenerated+"...");
    }

    protected boolean hasNextActions(ArrayList<Object> actions){
        return abstractActions[0] != 1;
    }
    protected boolean hasNextObservation(){
        return abstractObservations[0][abstractObservations[0].length-1] != 1;
    }

    protected void getNextObservation(){
        boolean finalSchema = true;
        for(int i = 0; i < abstractObservations[0].length; i++) {
            if (abstractObservations[1][i] != maxNumberOfStates - 1) {
                finalSchema = false;
                break;
            }
        }
        if(finalSchema){
            abstractObservations[0][abstractObservations[0].length-1] = 1;
            return;
        }

        int state = maxNumberOfStates;
        int idx = abstractObservations[0].length - 1;
        while (idx >= 0){
            if(abstractObservations[state][idx] < maxNumberOfStates - 1){
                abstractObservations[state][idx]++;
                return;
            }
            else{
                abstractObservations[state][idx] = 0;
            }
            if(idx > 0) idx--;
            else{
                idx = abstractObservations[0].length - 1;
                state--;
            }
        }
    }

    protected void getNextActions(ArrayList<Object> actions){
        int idx = maxNumberOfStates;
        while (idx >= 0){
            if(abstractActions[idx] < actions.size() - 1) {
                abstractActions[idx]++;
                break;
            }
            else abstractActions[idx] = 0;
            idx--;
        }
    }

    protected ArrayList<Object> getFollowersActionAbstractions(){
        LinkedList<GameState> queue = new LinkedList<>();

        queue.add(rootState);

        while (queue.size() > 0) {
            GameState currentState = queue.removeFirst();

            if(currentState.getPlayerToMove().equals(follower)){
                ArrayList<Object> abstractions = new ArrayList<>();
                for (Action action : expander.getActions(currentState)) {
                    abstractions.add(((AbstractActionProvider)action).getActionAbstraction());
                }
                return abstractions;
            }


            if (currentState.isGameEnd()) {
                continue;
            }
            for (Action action : expander.getActions(currentState)) {
                queue.add(currentState.performAction(action));
            }
        }
        return null;
    }

    public ArrayList<Object> checkNumberOfObservations(){
        HashSet<Object> obs = new HashSet<>();
        LinkedList<GameState> queue = new LinkedList<>();

        queue.add(rootState);

        while (queue.size() > 0) {
            GameState currentState = queue.removeFirst();

            if(currentState.getPlayerToMove().equals(follower))
                obs.add(((AbstractObservationProvider)currentState).getAbstractObservation());

            if (currentState.isGameEnd()) {
                continue;
            }
            for (Action action : expander.getActions(currentState)) {
                queue.add(currentState.performAction(action));
            }
        }
        System.out.println("Number of possible observations = " + obs.size());
        return new ArrayList<>(obs);
    }

    public void treeRecursion(/* SCHEMA */ int previousSchemaState){

        LinkedList<Pair<GameState,Integer>> queue = new LinkedList<>();

        boolean fullStrategy = true;

        queue.add(new Pair<GameState, Integer>(rootState,0));

        while (queue.size() > 0) {
            Pair<GameState, Integer> currentStatePair = queue.removeFirst();
            GameState currentState = currentStatePair.getLeft();
            previousSchemaState = currentStatePair.getRight();

            if (currentState.isGameEnd()) {
                continue;
            }

            if(currentState.getPlayerToMove().equals(follower)){
                Object abstractObservation = ((AbstractObservationProvider)currentState).getAbstractObservation();

                if (transitions[previousSchemaState].containsKey(abstractObservation)){
                    int currentSchemaState = transitions[previousSchemaState].get(abstractObservation);
                    if(actions[currentSchemaState] != null){
                        for(Action a : expander.getActions(currentState)){
                            if(((AbstractActionProvider)a).getActionAbstraction().equals(actions[currentSchemaState])){
                                queue.add(new Pair<GameState, Integer>(currentState.performAction(a), currentSchemaState));
                                break;
                            }
                        }
                    }
                    else{
//                        System.out.println("FAIL");
                        fullStrategy = false;
                        for(Action a : expander.getActions(currentState)){
                            // recursion
                            actions[currentSchemaState] = ((AbstractActionProvider)a).getActionAbstraction();
                            treeRecursion(currentSchemaState);
                            actions[currentSchemaState] = null;
                        }
                    }

                }
                else{
                    fullStrategy = false;
                    int max = previousSchemaState == 0 ? 1 : maxNumberOfStates;
                    for(int currentSchemaState = 1; currentSchemaState <= max; currentSchemaState++){
                        transitions[previousSchemaState].put(abstractObservation, currentSchemaState);
                        if(actions[currentSchemaState] != null){
                            for(Action a : expander.getActions(currentState)){
                                if(((AbstractActionProvider)a).getActionAbstraction().equals(actions[currentSchemaState])){
                                    treeRecursion(currentSchemaState);
                                    break;
                                }
                            }
                        }
                        else{
//                            System.out.println("FAIL");
                            for(Action a : expander.getActions(currentState)){
                                // recursion
                                actions[currentSchemaState] = ((AbstractActionProvider)a).getActionAbstraction();
                                treeRecursion(currentSchemaState);
                                actions[currentSchemaState] = null;
                            }
                        }
                        transitions[previousSchemaState].remove(abstractObservation);
                    }
                }
            }
            else {
                for (Action action : expander.getActions(currentState)) {
                    queue.add(new Pair<GameState, Integer>(currentState.performAction(action),previousSchemaState));
                }
            }
        }

        // NO CHANGE ?
        if(fullStrategy){
            numberOfGenerated++;
            // find all sequences, add to set of sets of sequences
            HashSet<Sequence> currentSet = getSetOfSequences();
            smallSchemata.add(currentSet);
            if(numberOfGenerated % 50000 == 0)
                System.out.printf(smallSchemata.size()+"/"+numberOfGenerated+"...");
        }

    }

    private HashSet<Sequence> getSetOfSequences() {
        LinkedList<Pair<GameState,Integer>> queue = new LinkedList<>();

        HashSet<Sequence> sequences = new HashSet<>();

//        int currentSchemaState = 0;

        queue.add(new Pair<GameState, Integer>(rootState,0));

        while (queue.size() > 0) {
            Pair<GameState, Integer> currentStatePair = queue.removeFirst();
            GameState currentState = currentStatePair.getLeft();
            int currentSchemaState = currentStatePair.getRight();

            if (currentState.isGameEnd()) {
                sequences.add(currentState.getSequenceFor(follower));
                continue;
            }

            if(currentState.getPlayerToMove().equals(follower)){
                sequences.add(currentState.getSequenceForPlayerToMove());
                Object abstractObservation = ((AbstractObservationProvider)currentState).getAbstractObservation();
                // move to next state
//                if(!transitions[currentSchemaState].containsKey(abstractObservation))
//                    System.out.println(currentSchemaState + " " + abstractObservation.toString());
                currentSchemaState = transitions[currentSchemaState].get(abstractObservation);
                // getAction
                for (Action action : expander.getActions(currentState)) {
                    if(((AbstractActionProvider)action).getActionAbstraction().equals(actions[currentSchemaState])){
                        queue.add(new Pair<GameState, Integer>(currentState.performAction(action),currentSchemaState));
                        break;
                    }
                }
            }
            else {
                for (Action action : expander.getActions(currentState)) {
                    queue.add(new Pair<GameState, Integer>(currentState.performAction(action), currentSchemaState));
                }
            }
        }
        return sequences;
    }

    public void recursion(GameState state, int schemaState){

        if(state.isGameEnd()){
            if(state.getUtilities()[1-follower.getId()] > leaderUpperBound){
                leaderUpperBound = state.getUtilities()[1-follower.getId()];
            }
            currentSet.addAll(state.getSequenceFor(follower).getAllPrefixes());
            return;
        }

        Object abstractObservation = null;
        if(state.getPlayerToMove().equals(follower)){
            abstractObservation = ((AbstractObservationProvider)state).getAbstractObservation();
        }

        boolean skipActions = false;
        boolean changingState = false;
        if(state.equals(deletedUpdate.getFirst())) {
            changingState = true;
            skipActions = true;
        }

        for(Action a : expander.getActions(state)){
            if(skipActions && !a.equals(deletedUpdate.getSecond()))
                continue;
            else{
                skipActions = false;
            }

            if(state.getPlayerToMove().equals(follower)) {
                assert abstractObservation != null;
                Object abstractAction = ((AbstractActionProvider)a).getActionAbstraction();

                int startingState = 1;
                if(changingState && a.equals(deletedUpdate.getSecond())) startingState = deletedUpdate.getThird().getLeft() + 1;

                for(int nextSchemaState = startingState; nextSchemaState < maxNumberOfStates; nextSchemaState++) {
                    if(!transitions[schemaState].containsKey(abstractObservation) &&
                            actions[nextSchemaState] == null){
                        // update both, ok
                        transitions[schemaState].put(abstractObservation, nextSchemaState);
                        actions[nextSchemaState] = abstractAction;
                        schemaUpdates.push(new Quadruple<>(state,a,new Pair(nextSchemaState,schemaState), false));
                        if (changingState) changeSuccessful = true;
                        recursion(state.performAction(a), nextSchemaState);
                    }
                    if(!transitions[schemaState].containsKey(abstractObservation) &&
                            actions[nextSchemaState].equals(abstractAction)){
                        // update transition, ok
                        transitions[schemaState].put(abstractObservation, nextSchemaState);
                        schemaUpdates.push(new Quadruple<>(state,a,new Pair<Integer, Integer>(nextSchemaState,schemaState),true));
                        if (changingState) changeSuccessful = true;
                        recursion(state.performAction(a), nextSchemaState);
                    }
                    if(transitions[schemaState].get(abstractObservation).equals(nextSchemaState) &&
                            actions[nextSchemaState].equals(abstractAction)){
                        // all ok
                        if (changingState) changeSuccessful = true;
                        recursion(state.performAction(a), nextSchemaState);
                    }
                }
            }
            else { // leader
                recursion(state.performAction(a), schemaState);
            }
        }
    }

    @Override
    public Set<Sequence> next(){
        if(nextSchemaIntex < smallSchemataList.size()) {
            nextSchemaIntex++;
            return smallSchemataList.get(nextSchemaIntex - 1);
        }
        else throw new NoSuchElementException();
    }

//    @Override
//    public Set<Sequence> next() {
//
//        // update slacks
//        // update currentSet
//        // update currentlyReachableLeafs (?) -- neni nutny, pouzivaji se jen pro upper bound
//        // update leaderUpperBound
//
////        if (leaderUpperBound <= bestValue + 1e-8 || (StackelbergConfig.USE_FEASIBILITY_CUT && !solver.checkFeasibilityFor(currentSet))) {
//
////        if (first) {
////            first = false;
////            return new HashSet<>(currentSet);
////        }
////        int index = getIndexOfReachableISWithActionsLeftFrom(stack.size() - 1);
////
////        updateRealizationPlan(index);
////        return new HashSet<>(currentSet);
//
//
//
//        // delete all current slacks
//        for(Sequence seq : currentSet){
//            solver.addSlackFor(seq);
//        }
//        currentSet.clear();
//        leaderUpperBound = Double.NEGATIVE_INFINITY;
//
//        // find next
//        if(first){
//            first = false;
//            deletedUpdate = new Quadruple<>(null,null, null, null);
//            recursion(rootState, 0);
//            System.out.println("Behavioral function:");
//            for(int i = 0; i < maxNumberOfStates; i++){
//                System.out.println("\t state[" + i + "] = " + actions[i]);
//            }
//            System.out.println("Transition function:");
//            for(int i = 0; i < maxNumberOfStates; i++){
//                if(transitions[i].keySet().size()>0) System.out.printf("\tstate["+i+"] : ");
//                for(Object obs : transitions[i].keySet())
//                    System.out.printf("["+obs.toString() + "] = " + transitions[i].get(obs) + "; ");
//                System.out.println();
//            }
//            System.out.println("-------------------------------");
//        }
//        else {
//            changeSuccessful = false;
//            while ((!changeSuccessful || blacklist.contains(currentSet.hashCode())) && schemaUpdates.size() > 0) {
//                changeSuccessful = false;
//                currentSet.clear();
//                leaderUpperBound = Double.NEGATIVE_INFINITY;
//                // delete last update
//                if(deletedUpdate.getFirst() != null) {
//                    transitions[deletedUpdate.getThird().getRight()].remove(
//                            ((AbstractObservationProvider) deletedUpdate.getFirst()).getAbstractObservation());
//                    if (!deletedUpdate.getFourth()) {
//                        actions[deletedUpdate.getThird().getLeft()] = null;
//                    }
//                }
//                deletedUpdate = schemaUpdates.pop();
//                transitions[deletedUpdate.getThird().getRight()].remove(
//                        ((AbstractObservationProvider)deletedUpdate.getFirst()).getAbstractObservation());
//                if(!deletedUpdate.getFourth()){
//                    actions[deletedUpdate.getThird().getLeft()] = null;
//                }
//
//                recursion(rootState, 0);
//
//                System.out.println("Changing success: " + changeSuccessful + ", blacklist: " + blacklist.contains(currentSet.hashCode()));
//                System.out.println("Behavioral function:");
//                for(int i = 0; i < maxNumberOfStates; i++){
//                    System.out.println("\t state[" + i + "] = " + actions[i]);
//                }
//                System.out.println("Transition function:");
//                for(int i = 0; i < maxNumberOfStates; i++){
//                    if(transitions[i].keySet().size()>0) System.out.printf("\tstate["+i+"] : ");
//                    for(Object obs : transitions[i].keySet())
//                        System.out.printf("["+obs.toString() + "] = " + transitions[i].get(obs) + "; ");
//                    System.out.println();
//                }
//                System.out.println("-------------------------------");
//            }
//        }
//
//        if(currentSet.isEmpty())
//            return null;
//
//        // set new slacks
//        for(Sequence seq : currentSet){
//            solver.removeSlackFor(seq);
//        }
//
//        blacklist.add(currentSet.hashCode());
//
//        if (leaderUpperBound <= bestValue + 1e-8 || (StackelbergConfig.USE_FEASIBILITY_CUT && !solver.checkFeasibilityFor(currentSet)))
//            next();
//
//        return new HashSet<>(currentSet);
//    }


}
