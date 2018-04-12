/*
Copyright 2016 Department of Computing Science, University of Alberta, Edmonton

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


package cz.agents.gtlibrary.nfg.MDP.domain.paws;

import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPConfigImpl;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPStateActionMarginal;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPAction;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPState;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.Triplet;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * 
 * @author viliam
 */
public class PAWSConfig extends MDPConfigImpl{
    public static double MAX_UTILITY = 32000; //marginal
    static int BASE_ID = 276;
    static int MAX_DISTANCE = 16000;
    
    
    public static boolean SHUFFLE = false;
    public static int SHUFFLE_ID = 0;
    public PAWSConfig() {
        allPlayers = new ArrayList<Player>(2);
        allPlayers.add(new PlayerImpl(0));
        allPlayers.add(new PlayerImpl(1));
        String shuffle = System.getProperty("ACTION_SHUFFLE");
        if (shuffle != null){
            SHUFFLE = true;
            SHUFFLE_ID = Integer.parseInt(shuffle);
        }
        if (patrolMDP == null) loadPatrolMDP("data/paws_mdp.txt.gz", "data/paws_attack_cells.txt", allPlayers.get(0), allPlayers.get(1));
    }
    
    
    static HashMap<MDPState,List<MDPAction>> patrolMDP;
    static List<MDPAction> attackActions;
    static HashMap<MDPAction,Double> densitySums = new HashMap<>();
    static HashSet<Triplet<Pair<Integer,Integer>, Double, MDPAction>> uniqueDensity = new HashSet<>();
    static private void loadPatrolMDP(String mdpfile, String attacksfile, Player patroller, Player attacker){
        HashMap<MDPState,List<MDPAction>> mdp = new HashMap<>();
        HashSet<MDPAction> attacks = new HashSet<>();
        
        PAWSPatrolState curState = new PAWSPatrolState(patroller);
        
        try {
            BufferedReader br;
            if (mdpfile.endsWith(".gz")){
                    br =  new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(mdpfile))));                
            } else {
                    br = new BufferedReader(new FileReader(mdpfile));
            }
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split("[(), ]+");
                curState.set(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]));
                PAWSPatrolState toState = new PAWSPatrolState(patroller);
                toState.set(Integer.parseInt(tokens[3]), (int)Double.parseDouble(tokens[4]));
                
                List<MDPAction> actions = mdp.get(curState);
                if (actions == null){
                    actions = new ArrayList<MDPAction>();
                    mdp.put(curState.copy(), actions);
                }
                actions.add(new PAWSPatrolAction(patroller, toState, Integer.parseInt(tokens[6]), 
                                Integer.parseInt(tokens[7]), Double.parseDouble(tokens[5])));
                PAWSAttackAction attack = new PAWSAttackAction(attacker, Integer.parseInt(tokens[6]), Integer.parseInt(tokens[7]));
                attacks.add(attack);
                if (Integer.parseInt(tokens[1]) < Integer.parseInt(tokens[3])) {
                    uniqueDensity.add(new Triplet(new Pair(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[3])), Double.parseDouble(tokens[5]), attack));
                } else {
                    uniqueDensity.add(new Triplet(new Pair(Integer.parseInt(tokens[3]), Integer.parseInt(tokens[1])), Double.parseDouble(tokens[5]), attack));
                }
            }
        } catch(Exception ex){
            ex.printStackTrace();
            System.out.println("MDP file (" + mdpfile + ") not loaded.");
            System.exit(0);
        };
        
        patrolMDP = mdp;
        attackActions = new ArrayList(attacks);

        if (attacksfile != null){
            try {
                BufferedReader br = new BufferedReader(new FileReader(attacksfile));
                attackActions = new ArrayList();
                String line;
                while ((line = br.readLine()) != null) {
                    String[] tokens = line.split("[(), ]+");
                    PAWSAttackAction attack = new PAWSAttackAction(attacker, Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]));
                    attackActions.add(attack);
                    densitySums.put(attack, Double.parseDouble(tokens[3]));
                }
            } catch(Exception ex){
                ex.printStackTrace();
                System.out.println("Attacks file (" + attacksfile + ") not loaded, using MDP edge sums in cells.");
                System.exit(0);
            };
        } else {
            for (Triplet<Pair<Integer,Integer>,Double,MDPAction> trip : uniqueDensity){
                Double sum = densitySums.get(trip.getThird());
                if (sum == null) sum = 0d;
                densitySums.put(trip.getThird(), sum + trip.getSecond());
            }
        }
        uniqueDensity = null;
        
        if (SHUFFLE) {
            for (List<MDPAction> l : patrolMDP.values()) Collections.shuffle(l,new HighQualityRandom(SHUFFLE_ID));
            Collections.shuffle(attackActions, new HighQualityRandom(SHUFFLE_ID));
        }
    }
        
    
    @Override
    public double getUtility(MDPStateActionMarginal firstPlayerAction, MDPStateActionMarginal secondPlayerAction) {
        PAWSPatrolAction pAct;
        PAWSAttackAction aAct;
        if (firstPlayerAction.getPlayer().getId()==0){ //required for DO only
            pAct = (PAWSPatrolAction) firstPlayerAction.getAction();
            aAct = (PAWSAttackAction) secondPlayerAction.getAction();
        } else {
            pAct = (PAWSPatrolAction) secondPlayerAction.getAction();
            aAct = (PAWSAttackAction) firstPlayerAction.getAction();
        }
        
        //return "selected cell sum" with a dummy patroller action
        if (pAct.row == -1) {
            return densitySums.get(aAct) * (firstPlayerAction.getPlayer().getId()==0 ? -1 : 1);
        }
        
        //TODO: use real streetmap. Different edges between KAPs are likely to pass the same paths.
        if (pAct.col == aAct.col && pAct.row == aAct.row) return pAct.density * (firstPlayerAction.getPlayer().getId()==0 ? 1 : -1);
        return 0;
    }

    @Override
    public double getBestUtilityValue(Player player) {
        if (player.getId() == 0) {
            return -1d*MAX_UTILITY;
        } else {
            return 1d*MAX_UTILITY;
        }
    }

    @Override
    public MDPState getDomainRootState(Player player) {
        if (player.getId() == 0) return new PAWSPatrolState(player);
        else return new PAWSAttackState(player);
    }

    public static int getMaxTimeStep() {
        return MAX_DISTANCE/100;
    }

    @Override
    public String toString() {
        return "PAWS: MAX_DISTANCE=" + MAX_DISTANCE + "; BASE_ID=" + BASE_ID + ";"  ;
    }
    
    
}
