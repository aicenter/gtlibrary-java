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


package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.io.PrintStream;
import java.util.Map;
import java.util.Set;

public interface DoubleOracleLPSolver {

    public void calculateStrategyForPlayer(int secondPlayerIndex, GameState root, DoubleOracleConfig algConfig, double currentBoundSize);

    public Double getResultForPlayer(Player player);

    public Map<Sequence, Double> getResultStrategiesForPlayer(Player player);

    public void setDebugOutput(PrintStream debugOutput);

    public long getOverallGenerationTime();

    public long getOverallConstraintGenerationTime();

    public long getOverallConstraintLPSolvingTime();

    public Set<Sequence> getNewSequencesSinceLastLPCalc(Player player);
}
