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


package cz.agents.gtlibrary.algorithms.sequenceform.experiments;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RefCompResultCollection {
    List<RefCompResult> qreResults;
    List<RefCompResult> depthMCTSResults;
    List<RefCompResult> cfrResults;

    public RefCompResultCollection() {
        qreResults = new ArrayList<RefCompResult>();
        depthMCTSResults = new ArrayList<RefCompResult>();
        cfrResults = new ArrayList<RefCompResult>();
    }

    public void addQREResult(RefCompResult result) {
        qreResults.add(result);
    }

    public void addMCTSResult(RefCompResult result) {
        depthMCTSResults.add(result);
    }

    public void addCFRResult(RefCompResult result) {
        cfrResults.add(result);
    }

    public RefCompResult getAverageQREResult() {
        Map<String, double[]> p1Abs = new LinkedHashMap<String, double[]>();
        Map<String, double[]> p1Rel = new LinkedHashMap<String, double[]>();
        Map<String, double[]> p2Abs = new LinkedHashMap<String, double[]>();
        Map<String, double[]> p2Rel = new LinkedHashMap<String, double[]>();

        for (RefCompResult qreResult : qreResults) {
            updateMap(p1Abs, qreResult.p1Abs);
            updateMap(p1Rel, qreResult.p1Rel);
            updateMap(p2Abs, qreResult.p2Abs);
            updateMap(p2Rel, qreResult.p2Rel);
        }
        normalize(p1Abs, qreResults.size());
        return new RefCompResult(p1Abs, p1Rel, p2Abs, p2Rel);
    }

    public RefCompResult getAverageCFRResult() {
        Map<String, double[]> p1Abs = new LinkedHashMap<String, double[]>();
        Map<String, double[]> p1Rel = new LinkedHashMap<String, double[]>();
        Map<String, double[]> p2Abs = new LinkedHashMap<String, double[]>();
        Map<String, double[]> p2Rel = new LinkedHashMap<String, double[]>();

        for (RefCompResult qreResult : cfrResults) {
            updateMap(p1Abs, qreResult.p1Abs);
            updateMap(p1Rel, qreResult.p1Rel);
            updateMap(p2Abs, qreResult.p2Abs);
            updateMap(p2Rel, qreResult.p2Rel);
        }
        normalize(p1Abs, cfrResults.size());
        return new RefCompResult(p1Abs, p1Rel, p2Abs, p2Rel);
    }

    public RefCompResult getAverageMCTSResult() {
        Map<String, double[]> p1Abs = new LinkedHashMap<String, double[]>();
        Map<String, double[]> p1Rel = new LinkedHashMap<String, double[]>();
        Map<String, double[]> p2Abs = new LinkedHashMap<String, double[]>();
        Map<String, double[]> p2Rel = new LinkedHashMap<String, double[]>();

        for (RefCompResult qreResult : depthMCTSResults) {
            updateMap(p1Abs, qreResult.p1Abs);
            updateMap(p1Rel, qreResult.p1Rel);
            updateMap(p2Abs, qreResult.p2Abs);
            updateMap(p2Rel, qreResult.p2Rel);
        }
        normalize(p1Abs, depthMCTSResults.size());
        return new RefCompResult(p1Abs, p1Rel, p2Abs, p2Rel);
    }

    private void normalize(Map<String,double[]> toNormalize, int size) {
        for (double[] values : toNormalize.values()) {
            for (int i = 0; i < values.length; i++) {
               values[i] /= size;
            }
        }
    }

    private void updateMap(Map<String, double[]> toUpdate, Map<String, double[]> source) {
        for (Map.Entry<String, double[]> entry : source.entrySet()) {
            update(toUpdate, entry);
        }
    }

    private void update(Map<String, double[]> toUpdate, Map.Entry<String, double[]> source) {
        double[] absValues = toUpdate.get(source.getKey());

        if (absValues == null)
            absValues = new double[source.getValue().length];
        for (int i = 0; i < absValues.length; i++) {
            absValues[i] += source.getValue()[i];
        }
        toUpdate.put(source.getKey(), absValues);
    }


}
