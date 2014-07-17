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


package cz.agents.gtlibrary.nfg;


/**
 * Class representing Utility formulation given two strategies.
 * Author: Ondrej Vanek
 * Date: 11/16/11
 * Time: 1:13 PM
 */
public class NegativeUtility<T extends PureStrategy, U extends PureStrategy> extends Utility<T,U>{

    private Utility<U, T> utility;

    public NegativeUtility(Utility<U,T> utility){
        this.utility = utility;
    }

    @Override
    public double getUtility(T s1, U s2) {
        return -utility.getUtility(s2,s1);
    }
}
