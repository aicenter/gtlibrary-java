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
