package cz.agents.gtlibrary.algorithms.sequenceform.experiments;

import java.util.Map;

public class RefCompResult {
    public Map<String, double[]> p1Abs;
    public Map<String, double[]> p1Rel;
    public Map<String, double[]> p2Abs;
    public Map<String, double[]> p2Rel;

    public RefCompResult(Map<String, double[]> p1Abs, Map<String, double[]> p1Rel, Map<String, double[]> p2Abs, Map<String, double[]> p2Rel) {
        this.p1Abs = p1Abs;
        this.p1Rel = p1Rel;
        this.p2Abs = p2Abs;
        this.p2Rel = p2Rel;
    }


}
