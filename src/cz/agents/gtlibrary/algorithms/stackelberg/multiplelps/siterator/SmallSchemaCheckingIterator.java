package cz.agents.gtlibrary.algorithms.stackelberg.multiplelps.siterator;

import cz.agents.gtlibrary.algorithms.cfr.br.responses.AbstractActionProvider;
import cz.agents.gtlibrary.algorithms.cfr.br.responses.AbstractObservationProvider;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.multiplelps.FeasibilitySequenceFormLP;
import cz.agents.gtlibrary.algorithms.stackelberg.multiplelps.rpiterator.DepthPureRealPlanIterator;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Jakub Cerny on 20/07/2018.
 */
public class SmallSchemaCheckingIterator extends DepthPureRealPlanIterator {

    private final int MAX_SCHEMA_SIZE;
    private int skipped;

    public SmallSchemaCheckingIterator(Player player, StackelbergConfig config, Expander<SequenceInformationSet> expander, FeasibilitySequenceFormLP solver, int maxSchemaSize) {
        super(player, config, expander, solver);
        MAX_SCHEMA_SIZE = maxSchemaSize;
        skipped = 0;
    }

    protected boolean isRepresentableBySameFunctionSchemaOfSize(int size){
        HashSet<SequenceInformationSet> sets = new HashSet<>();
        HashMap<SequenceInformationSet,Object> actions = new HashMap<>();
        HashMap<SequenceInformationSet,HashMap<Object,SequenceInformationSet>> transitions = new HashMap<>();
        for(Sequence seq : currentSet){
            for(Action a : seq) {
                sets.add((SequenceInformationSet) a.getInformationSet());
                actions.put((SequenceInformationSet) a.getInformationSet(), ((AbstractActionProvider)a).getActionAbstraction());
            }
        }
        for(SequenceInformationSet set: sets){
            Object abstractObservation = ((AbstractObservationProvider)set.getAllStates().iterator().next()).getAbstractObservation();
            if(set.getPlayersHistory().size()>0){
                SequenceInformationSet previous = (SequenceInformationSet) set.getPlayersHistory().getLast().getInformationSet();
                if(!transitions.containsKey(previous)) transitions.put(previous, new HashMap<>());
                transitions.get(previous).put(abstractObservation,set);
            }
        }

        HashMap<Object,HashMap<Integer,HashSet<SequenceInformationSet>>> initial = new HashMap<>();
        for(SequenceInformationSet set: sets){
            int hash = transitions.get(set).keySet().hashCode();
            Object abstractAction = actions.get(set);
            if(!initial.containsKey(abstractAction)) initial.put(abstractAction, new HashMap<>());
            if(!initial.get(abstractAction).containsKey(hash)) initial.get(abstractAction).put(hash, new HashSet<>());
            initial.get(abstractAction).get(hash).add(set);
        }

        HashSet<HashSet<SequenceInformationSet>> partition = new HashSet<>();
        for(Object action : initial.keySet())
            for(Integer hash : initial.get(action).keySet())
                partition.add(initial.get(action).get(hash));
        HashMap<SequenceInformationSet, Integer> partitions = new HashMap<>();
        int pp = 0;
        for(HashSet<SequenceInformationSet> p : partition){
            for(SequenceInformationSet set : p)
                partitions.put(set,pp);
            pp++;
        }

        boolean changed = true;
        HashSet<SequenceInformationSet> deletedSet = null;
        HashSet<HashSet<SequenceInformationSet>> newSets = null;
        while(changed){
            changed = false;
            if(partition.size() > size)
                return false;
            for(HashSet<SequenceInformationSet> p : partition){
                if(changed) break;
                SequenceInformationSet set = p.iterator().next();
                for(Object abstractObservation : transitions.get(set).keySet()){
                    if(changed) break;
                    int out = partitions.get(transitions.get(set).get(abstractObservation));
                    // do jakych partitions tohle vede?
                    for(SequenceInformationSet s : p){
                        if (partitions.get(transitions.get(s).get(abstractObservation)) != out){
                            // vede do jine partition
                            changed = true;
                            deletedSet = p;
                            // TODO: split, update partitions
                            HashMap<Integer,HashSet<SequenceInformationSet>> newSetsMap = new HashMap<>();
                            for(SequenceInformationSet ss : p){
                                int oout = partitions.get(transitions.get(ss).get(abstractObservation));
                                if(!newSetsMap.containsKey(oout)) newSetsMap.put(oout, new HashSet<>());
                                newSetsMap.get(oout).add(ss);
                            }
                            pp = partition.size();
                            for(HashSet<SequenceInformationSet> hset : newSetsMap.values()){
                                for(SequenceInformationSet hhset:hset)
                                    partitions.put(hhset, pp);
                                pp++;
                            }
                            newSets = new HashSet<>(newSetsMap.values());
                            break;
                        }
                    }
                }
            }
            if(changed){
                partition.remove(deletedSet);
                partition.addAll(newSets);
            }
        }
        return true;
    }

    protected boolean isRepresentableBySchemaOfSize(int size){
        HashSet<SequenceInformationSet> sets = new HashSet<>();
        HashMap<SequenceInformationSet,Object> actions = new HashMap<>();
        HashMap<SequenceInformationSet,HashMap<Object,SequenceInformationSet>> transitions = new HashMap<>();
        for(Sequence seq : currentSet){
            for(Action a : seq) {
                sets.add((SequenceInformationSet) a.getInformationSet());
                actions.put((SequenceInformationSet) a.getInformationSet(), ((AbstractActionProvider)a).getActionAbstraction());
            }
        }
        for(SequenceInformationSet set: sets){
            Object abstractObservation = ((AbstractObservationProvider)set.getAllStates().iterator().next()).getAbstractObservation();
            if(set.getPlayersHistory().size()>0){
                SequenceInformationSet previous = (SequenceInformationSet) set.getPlayersHistory().getLast().getInformationSet();
                if(!transitions.containsKey(previous)) transitions.put(previous, new HashMap<>());
                transitions.get(previous).put(abstractObservation,set);
            }
        }

        HashMap<Object,HashSet<SequenceInformationSet>> initial = new HashMap<>();
        for(SequenceInformationSet set: sets){
            Object abstractAction = actions.get(set);
            if(!initial.containsKey(abstractAction)) initial.put(abstractAction, new HashSet<>());
            initial.get(abstractAction).add(set);
        }

        HashSet<HashSet<SequenceInformationSet>> partition = new HashSet<>();
        for(Object action : initial.keySet())
            partition.add(initial.get(action));
        HashMap<SequenceInformationSet, Integer> partitions = new HashMap<>();
        int pp = 0;
        for(HashSet<SequenceInformationSet> p : partition){
            for(SequenceInformationSet set : p)
                partitions.put(set,pp);
            pp++;
        }

        boolean changed = true;
        HashSet<SequenceInformationSet> deletedSet = null;
        HashSet<HashSet<SequenceInformationSet>> newSets = null;
        while(changed){
            changed = false;
            if(partition.size() > size)
                return false;
            for(HashSet<SequenceInformationSet> p : partition){
                if(changed) break;

                for(SequenceInformationSet set_1 : p){
                    if(changed) break;
                    if(!transitions.containsKey(set_1)) continue;
                    for(SequenceInformationSet set_2 : p){
                        if(changed) break;
                        for(Object observation : transitions.get(set_1).keySet()){
                            if(transitions.containsKey(set_2) && transitions.get(set_2).containsKey(observation)){
                                int out_1 = partitions.get(transitions.get(set_1).get(observation));
                                int out_2 = partitions.get(transitions.get(set_2).get(observation));
                                if(out_1 != out_2){
                                    // vede do jine partition
                                    changed = true;
                                    deletedSet = p;
                                    HashMap<Integer,HashSet<SequenceInformationSet>> newSetsMap = new HashMap<>();
                                    for(SequenceInformationSet ss : p){
                                        int oout = 0;
                                        if(!transitions.containsKey(ss) || !transitions.get(ss).containsKey(observation)){
                                            oout = getRandomObservation(p, observation,transitions, partitions);
                                        }
                                        else{
                                            oout = partitions.get(transitions.get(ss).get(observation));
                                        }
                                        if(!newSetsMap.containsKey(oout)) newSetsMap.put(oout, new HashSet<>());
                                        newSetsMap.get(oout).add(ss);
                                    }
                                    pp = partition.size();
                                    for(HashSet<SequenceInformationSet> hset : newSetsMap.values()){
                                        for(SequenceInformationSet hhset:hset)
                                            partitions.put(hhset, pp);
                                        pp++;
                                    }
                                    newSets = new HashSet<>(newSetsMap.values());
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            if(changed){
                partition.remove(deletedSet);
                partition.addAll(newSets);
            }
        }
        if(partition.size() > size)
            return false;
        return true;
    }

    private int getRandomObservation(HashSet<SequenceInformationSet> p, Object observation, HashMap<SequenceInformationSet, HashMap<Object, SequenceInformationSet>> transitions, HashMap<SequenceInformationSet, Integer> partitions) {
        for(SequenceInformationSet set : p)
            if(transitions.containsKey(set) && transitions.get(set).containsKey(observation))
                return partitions.get(transitions.get(set).get(observation));
        return 0;
    }

    @Override
    public Set<Sequence> next() {
        getNext();
        int skipped = 0;
        while(!isRepresentableBySchemaOfSize(MAX_SCHEMA_SIZE) && hasNext()){
            getNext();
            skipped++;
        }
        this.skipped += skipped;
//        System.out.printf(skipped+"...");
        return new HashSet<>(currentSet);
    }

    public Set<Sequence> getNext() {
        if (first) {
            first = false;
            return new HashSet<>(currentSet);
        }
        int index = getIndexOfReachableISWithActionsLeftFrom(stack.size() - 1);

        updateRealizationPlan(index);

        return currentSet;
    }

    public int getSkipped(){
        return skipped;
    }

}
