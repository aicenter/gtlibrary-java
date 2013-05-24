package cz.agents.gtlibrary.nfg.core;

import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.nfg.PureStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: all fields are public for an easy access. Refactor this unholy hideousness! An abomination of the Java language! Save your soul, you sinner!
 */
public class Measure {

    public int iterations;

    //total time = load+run+finish
    public long runTime;
    public long loadTime;
    public long finishTime;
    public int maxLength;
    public int width;
    public int length;
    public List<Integer> patrollerSuppSetSize = new ArrayList<Integer>();
    public List<Integer> evaderSuppSetSize = new ArrayList<Integer>();

    public List<Integer> patrollerStrategiesSize = new ArrayList<Integer>();
    public List<Integer> evaderStrategiesSize = new ArrayList<Integer>();


    public List<Long> patrollerOracleTimes = new ArrayList<Long>();
    public List<Long> evaderOracleTimes = new ArrayList<Long>();
    public List<Long> coreLPtimes = new ArrayList<Long>();
    public List<Long> updateCoreTimes = new ArrayList<Long>();

    public List<Double> patrollerBRVal = new ArrayList<Double>();
    public List<Double> evaderBRVal = new ArrayList<Double>();

    public List<Double> coreLPGV = new ArrayList<Double>();
    public MixedStrategy<? extends PureStrategy> playerOneMixedStrategy;
    public MixedStrategy<? extends PureStrategy> playerTwoMixedStrategy;

    public void write() {
        System.out.println("=== MEASURE ===");
        System.out.println("Iterations: " + iterations);
        System.out.println("TotalTime: " + runTime);
        if (iterations > 0) System.out.println("Time per iteration :" + runTime / iterations);
        write(patrollerOracleTimes, "pTime");
        write(evaderOracleTimes, "eTime");
        write(coreLPtimes, "ctime");
        write(patrollerBRVal, "pVal");
        write(evaderBRVal, "eVal");
        write(coreLPGV, "cVal");
        write(evaderSuppSetSize, "eSSS");
        write(patrollerSuppSetSize, "pSSS");
        write(evaderStrategiesSize, "eSize");
        write(patrollerStrategiesSize, "pSize");

        System.out.println("run time:" + runTime);
        System.out.println("load time:" + loadTime);
        System.out.println("finish time:" + finishTime);
        System.out.println("total time:" + (loadTime + runTime + finishTime));

        System.out.println("EvaderBR: " + getTotalTime(evaderOracleTimes));
        System.out.println("PatrollerBR: " + getTotalTime(patrollerOracleTimes));
        System.out.println("CoreBR: " + getTotalTime(coreLPtimes));
        //write(patrollerBRVal,"pVal");
        //write(evaderBRVal,"eVal");
        //write(coreLPGV,"cVal");
        System.out.println(getIterationCumulTimes());
        System.out.println("Update time: " + getTotalTime(updateCoreTimes));

    }

    private void write(List<?> list, String string) {
        StringBuilder builder = new StringBuilder();
        builder.append(string);
        builder.append("=[");
        for (Object o : list) {
            builder.append(o.toString());
            builder.append(";");
        }
        builder.append("];");
        System.out.println(builder.toString());
    }

    public void writeTimes() {
        System.out.println("run time:" + runTime);
        System.out.println("load time:" + loadTime);
        System.out.println("finish time:" + finishTime);

        System.out.println("EvaderBR: " + getTotalTime(evaderOracleTimes));
        System.out.println("PatrollerBR: " + getTotalTime(patrollerOracleTimes));
        System.out.println("CoreBR: " + getTotalTime(coreLPtimes));
    }

    private String getIterationTimes() {
        StringBuilder sb = new StringBuilder();
        sb.append("itimes=[");
        for (int i = 0, j = 0; i < evaderOracleTimes.size(); i++, j += 2) {
            long itime = evaderOracleTimes.get(i) +
                    patrollerOracleTimes.get(i) +
                    coreLPtimes.get(j);
            try {
                itime += coreLPtimes.get(j + 1);
            } catch (Exception ignored) {
            }
            sb.append(itime).append(";");
        }
        sb.append("];");
        return sb.toString();
    }

    private String getIterationCumulTimes() {
        StringBuilder sb = new StringBuilder();
        sb.append("itimes=[");
        long sofar = 0;
        for (int i = 0, j = 0; i < evaderOracleTimes.size(); i++, j += 2) {
            long itime = sofar + evaderOracleTimes.get(i);
            try {
                itime += patrollerOracleTimes.get(i);
            } catch (Exception ignored) {
            }
            try {
                itime += coreLPtimes.get(j);
            } catch (Exception ignored) {
            }
            try {
                itime += coreLPtimes.get(j + 1);
            } catch (Exception ignored) {
            }
            sb.append(itime).append(";");
            sofar = itime;
        }
        sb.append("];");
        return sb.toString();
    }

    public long getTotalTime(List<Long> times) {
        long total = 0;
        for (long l : times) {
            total += l;
        }
        return total;
    }


}
