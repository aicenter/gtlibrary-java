package cz.agents.gtlibrary.nfg.core;

import java.util.ArrayList;
import java.util.List;


public class MeasureSet {


    private List<Measure> measures = new ArrayList<Measure>();

    public void add(Measure m) {
        measures.add(m);
    }

    public void writeTime() {
        String write = "runtime= [";
        for (Measure m : measures) {
            write += m.runTime + "; ";
        }
        System.out.println(write.substring(0, write.length() - 2) + "];");

        write = "POtime= [";
        for (Measure m : measures) {
            write += m.getTotalTime(m.patrollerOracleTimes) + "; ";
        }
        System.out.println(write.substring(0, write.length() - 2) + "];");

        write = "EOtime= [";
        for (Measure m : measures) {
            write += m.getTotalTime(m.evaderOracleTimes) + "; ";
        }
        System.out.println(write.substring(0, write.length() - 2) + "];");

        write = "Coretime= [";
        for (Measure m : measures) {
            write += m.getTotalTime(m.coreLPtimes) + "; ";
        }
        System.out.println(write.substring(0, write.length() - 2) + "];");

        write = "updatetime= [";
        for (Measure m : measures) {
            write += m.getTotalTime(m.updateCoreTimes) + "; ";
        }
        System.out.println(write.substring(0, write.length() - 2) + "];");

//		for(Measure m: measures){
//			m.writeTimes();
//		}
    }

    public void writeIterations() {
        String write = "iters: [";
        for (Measure m : measures) {
            write += m.iterations + "; ";
        }
        System.out.println(write.substring(0, write.length() - 2) + "];");
    }

}
