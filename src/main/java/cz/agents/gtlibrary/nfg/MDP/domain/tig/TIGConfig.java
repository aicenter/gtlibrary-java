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


package cz.agents.gtlibrary.nfg.MDP.domain.tig;

import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPConfigImpl;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPStateActionMarginal;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPState;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.Triplet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 *
 * @author viliam
 */
public class TIGConfig extends MDPConfigImpl{
    public static int MAX_TIME = 10;
    public static int NUM_STOPS = 3;
    public static int NUM_TRAINS = 1;
    public static int APPROXIMATE_PASSANGERS = 200;
    public static double[] stopsPopularity = new double[]{5,1,0.9,1,2,3,5,6,10,6,5,10,3,4,2,1,1,1,2,1,2,1,5,1,2,1,3,1,1,1};
    public static int[][] trainStarts = null;
    //public static int[][] trainStarts = new int[][] {new int[]{1,11,21,30,39,47,55,60,65,70,73,76,79,81,84,87,90,},//,93,96,99,102,105,108,111,116,121,126,134,144,154,164},
    //                                                 new int[]{1,11,21,30,39,47,55,60,65,70,73,76,79,81,84,87,90,}};//93,96,99,102,105,108,111,116,121,126,134,144,154,164}};
    //                                                 new int[]{1,31,61,81,101,121,131,141,151,171}};
    //public static int[][] trainStarts = new int[][] {new int[]{1,31,61,81,101,121,131,141,151,171},
    //                                                 new int[]{1,31,61,81,101,121,131,141,151,171}};
    //public static int[][] trainStarts = new int[][] {new int[]{15},
    //                                                 new int[]{1}};
    public static double MOVEMENT_UNCERTAINTY = 0.1;
    public static boolean SHUFFLE = false;
    public static int SHUFFLE_ID = 0;
    public static double FINE = 10000;
    public static double FARE = 150d;//in cents for better precision
    public static int PAT_IN_STOP_TIME = 15;
    public static int PAS_IN_STOP_TIME = 3;
    public static int CHECKS_PER_MINUTE = 5;
    
    //Stop,Time -> amount
    private static HashMap<Pair<Integer,Integer>,Integer> passAtStops = new HashMap<>();
    //TrainDir,TrainNum,ToStop -> amount
    private static HashMap<Triplet<Integer,Integer,Integer>,Integer> passOnTrains = new HashMap<>();

    private static int[] intervals;
    //time difference between train sarting and arriving to stop given by index
    private static int[][] timeDiffs = new int[2][]; 
    private static double pasNormalizer = 0;
    
    static TIGPassangerState pasEndState;
            
    public TIGConfig() {
        assert intervals==null : "Second TIGConfig creation";
        allPlayers = new ArrayList<Player>(2);
        allPlayers.add(new PlayerImpl(0,"Patrol"));
        allPlayers.add(new PlayerImpl(1,"Passanger"));
        String shuffle = System.getProperty("ACTION_SHUFFLE");
        if (shuffle != null){
            SHUFFLE = true;
            SHUFFLE_ID = Integer.parseInt(shuffle);
        }
        int[] trains = generateTrainStarts(MAX_TIME, NUM_TRAINS);
        trainStarts = new int[][]{trains,trains};
        
        Random rnd = new HighQualityRandom(123);
        intervals = new int[NUM_STOPS-1];
        //intervals at least 2 minutes to that time agter on train action can be safely advanced
        for (int i=0;i<intervals.length;i++) intervals[i]=rnd.nextInt(3)+2;
        
        timeDiffs[0] = new int[NUM_STOPS];
        int time=0;
        for (int i=0; i<NUM_STOPS; i++){
            timeDiffs[0][i]=time;
            if (i < NUM_STOPS-1) time += intervals[i];
        }
        timeDiffs[1] = new int[NUM_STOPS];
        time=0;
        for (int i=NUM_STOPS-1; i>=0; i--){
            timeDiffs[1][i]=time;
            if (i > 0) time += intervals[i-1];
        }
        
        pasEndState = new TIGPassangerState(allPlayers.get(1));
        pasEndState.amount=-1;
        
        if (pasNormalizer==0){
            for (int i=0; i<NUM_STOPS; i++) {
                for (int j=0; j<NUM_STOPS; j++) {
                    if (i!=j) pasNormalizer += stopsPopularity[i]*stopsPopularity[j];
                }
            }
            pasNormalizer *= NUM_TRAINS;
        }
        
        initPassangerAmounts();
    }
    
    static int getTrainTime(int trainDir, int trainNum, int stop){
        return trainStarts[trainDir][trainNum] + timeDiffs[trainDir][stop];
    }
    
    static int addToPass(int stop, int time, int amount){
        Pair<Integer,Integer> p = new Pair<>(stop,time);
        Integer n = passAtStops.get(p);
        if (n == null) n=0;
        n += amount;
        passAtStops.put(p,n);
        return n;
    }
    static int getPassangerAmount(int stop, int time){
        return addToPass(stop,time,0);
    }
    static int addToPass(int trainDir, int trainNum, int toStop, int amount){
        Triplet<Integer,Integer,Integer> t = new Triplet<>(trainDir,trainNum,toStop);
        Integer n = passOnTrains.get(t);
        if (n == null) n=0;
        n += amount;
        passOnTrains.put(t,n);
        return n;
    }
    static int getPassangerAmount(int trainDir, int trainNum, int toStop){
        return addToPass(trainDir,trainNum, toStop, 0);
    }
    
    static void initPassangerAmounts(){
        int allPass = 0;
        for (int small=0; small<TIGConfig.NUM_STOPS-1;small++){
            for (int large=small+1; large<TIGConfig.NUM_STOPS;large++){
                for (int dir=0; dir<2;dir++){
                    for (int num=0; num < TIGConfig.trainStarts[dir].length;num++){
                        int start, end;
                        if (dir == 0){
                            start = small; end = large;
                        } else {
                            start = large; end = small;
                        }
                        
                        int amount = getPassangerAmount(dir,num,start,end);
                        allPass += amount;
                        int startTime = getTrainTime(dir, num, start);
                        int endTime = getTrainTime(dir, num, end);
                        for (int dt=0;dt<PAS_IN_STOP_TIME;dt++){
                            addToPass(start,startTime-dt-1,amount);
                            addToPass(end,endTime+dt,amount);
                        }
                        if (dir==0){
                            for (int s=start+1;s<=end;s++){
                                addToPass(dir,num,s,amount);
                            }
                        } else {
                            for (int s=start-1;s>=end;s--){
                                addToPass(dir,num,s,amount);
                            }
                        }
                    }
                }
            }
        }
        System.out.println("Overall number of passangers: " + allPass);
    }
    
    static int[] generateTrainStarts(int maxTime, int N){
        int[] out = new int[N];
        for (int i=0; i<N; i++){
            out[i]= (int)(MAX_TIME*(Math.tan(1.88*((double)i/N)-1.1)+2)/3);
        }
        return out;
    }
    
    static int getPassangerAmount(int trainDir, int trainNum, int startStop, int endStop){
        Random passRandom = new HighQualityRandom(13*trainDir+17*startStop+23*endStop+trainDir);
        //int amount = 3*(trainNum*(trainStarts[trainDir].length-trainNum)/2+1) + 3*(endStop*(NUM_STOPS-endStop)) + passRandom.nextInt(5);
        //int amount = (int)Math.max(0,stopsPopularity[startStop]*stopsPopularity[endStop]*APPROXIMATE_PASSANGERS/NUM_STOPS + 5*passRandom.nextGaussian());
        double d = Math.max(0,stopsPopularity[startStop]*stopsPopularity[endStop]*APPROXIMATE_PASSANGERS/pasNormalizer);
        double r =  d - (int)d;
        return (int)d + (passRandom.nextDouble() < r ? 1 : 0);
    }
    
    
    
    static int nextTrain(int stop, int dir, int time){
        int startAfter = time - timeDiffs[dir][stop];
        for (int i=0; i < trainStarts[dir].length; i++){
            if (trainStarts[dir][i] > startAfter) return i;
        }
        return -1;
    }

    @Override
    public double getUtility(MDPStateActionMarginal firstPlayerAction, MDPStateActionMarginal secondPlayerAction) {
        assert (firstPlayerAction.getPlayer().getId() != secondPlayerAction.getPlayer().getId());
        MDPStateActionMarginal passangerAction,patrollerAction;
        if (firstPlayerAction.getPlayer().getId()==0){
            patrollerAction = firstPlayerAction;
            passangerAction = secondPlayerAction;
        } else {
            patrollerAction = secondPlayerAction;
            passangerAction = firstPlayerAction;
        }
        
        if (passangerAction.getAction() instanceof TIGPassangerTicketAction){
            TIGPassangerState pasState = (TIGPassangerState)passangerAction.getState();
            if (((TIGPassangerTicketAction)passangerAction.getAction()).buy){
                return (patrollerAction.getAction() instanceof TIGPatrolTicketsAction) ? FARE : 0;
            } else {//fare evader, check if captured
                if (patrollerAction.getAction() instanceof TIGPatrolStopAction){
                    double result = 0;
                    TIGPatrolStopAction patAction = (TIGPatrolStopAction)patrollerAction.getAction();
                    for (int stop : new int[]{pasState.fromStop,pasState.toStop}){
                        if (stop == patAction.stop){
                            int pasTime = getTrainTime(pasState.trainDir, pasState.trainNum, stop);
                            if (stop == pasState.toStop){
                                for (int t=pasTime;t<pasTime+PAS_IN_STOP_TIME;t++){
                                    if (t >= patAction.fromTime && t <= patAction.toTime){
                                        result += FINE*CHECKS_PER_MINUTE/getPassangerAmount(stop, t);
                                    }
                                }
                            } else {
                                for (int t=pasTime-PAS_IN_STOP_TIME;t<pasTime;t++){
                                    if (t >= patAction.fromTime && t <= patAction.toTime){
                                        result += FINE*CHECKS_PER_MINUTE/getPassangerAmount(stop, t);
                                    }
                                }
                            }
                        }
                    }
                    return result;
                } else if (patrollerAction.getAction() instanceof TIGPatrolOnTrainAction){
                    TIGPatrolOnTrainAction patAction = (TIGPatrolOnTrainAction)patrollerAction.getAction();
                    if (patAction.stay){
                        TIGPatrolState patState = (TIGPatrolState) patrollerAction.getState();
                        assert patState.onTrain;
                        if (pasState.trainDir != patState.trainDir || pasState.trainNum != patState.trainNum) return 0;
                        int nextStop = patState.stop + (patState.trainDir == 0 ? 1 : -1);
                        if (pasState.trainDir == 0 && (pasState.fromStop >= nextStop || pasState.toStop<nextStop)) return 0;
                        if (pasState.trainDir == 1 && (pasState.fromStop <= nextStop || pasState.toStop>nextStop)) return 0;
                        int interval = patState.trainDir == 0 ? intervals[nextStop-1] : intervals[nextStop];
                        return FINE*interval*CHECKS_PER_MINUTE/getPassangerAmount(patState.trainDir, patState.trainNum, nextStop);
                    }
                    return 0;
                }
            }
        }
        return 0;
    }

    @Override
    public double getBestUtilityValue(Player player) {
        //ignores the ticket collection action
        if (player.getId() == 0) {
            return 1d*FINE*APPROXIMATE_PASSANGERS/NUM_STOPS;
        } else {
            return -1d*FINE*APPROXIMATE_PASSANGERS/NUM_STOPS;
        }
    }

    @Override
    public MDPState getDomainRootState(Player player) {
        if (player.getId()==0) return new TIGPatrolState(player);
        else return new TIGPassangerState(player);
    }

    public static int getMaxTimeStep() {
        return MAX_TIME;
    }

    @Override
    public String toString() {
        return "TIG Stops=" + NUM_STOPS + "; TRAINS=" + NUM_TRAINS
                + "," + trainStarts[1].length + "; Passangers=" 
                + APPROXIMATE_PASSANGERS + "; Time=" + MAX_TIME;
    }
}
