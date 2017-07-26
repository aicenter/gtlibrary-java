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

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.simalphabeta.SimAlphaBeta;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 5/29/13
 * Time: 11:36 AM
 * To change this template use File | Settings | File Templates.
 */
public class ActionPureStrategy implements PureStrategy {

    private Action action;

    public ActionPureStrategy(Action action) {
        assert (action != null);
        this.action = action;
    }

    @Override
    public String toString() {
        return action.toString();
    }

    public Action getAction() {
        return action;
    }

	@Override
	public int hashCode() {
		if (action == null){
			SimAlphaBeta.CUT = true;
			System.out.println("NULL STRATEGY");
//			action = new ActionImpl(null) {
//				@Override
//				public void perform(GameState gameState) {
//					System.out.println("Performing NULL action.");
//				}
//				@Override
//				public String toString(){
//					return "NULL action";
//				}
//				@Override
//				public int hashCode() {
//					return 0;
//				}
//			};
			return 0;
		}
		return action.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ActionPureStrategy other = (ActionPureStrategy) obj;
		if (action == null) {
			if (other.action != null)
				return false;
		} else if (!action.equals(other.action))
			return false;
		return true;
	}
    
    
}
