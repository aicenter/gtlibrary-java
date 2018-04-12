/************************************************************************\
|* gtf is a framework for analyzing two-player zero-sum games           *|
|* Copyright (C) 2005 Troels Bjerre Sorensen                            *|
|*                                                                      *|
|* This program is free software; you can redistribute it and/or modify *|
|* it under the terms of the GNU General Public License as published by *|
|* the Free Software Foundation; either version 2 of the License, or    *|
|* (at your option) any later version.                                  *|
|*                                                                      *|
|* This program is distributed in the hope that it will be useful, but  *|
|* WITHOUT ANY WARRANTY; without even the implied warranty of           *|
|* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU    *|
|* General Public License for more details.                             *|
|*                                                                      *|
|* You should have received a copy of the GNU General Public License    *|
|* along with this program; if not, write to the                        *|
|* Free Software Foundation, Inc., 59 Temple Place - Suite 330,         *|
|* Boston, MA 02111-1307, USA.                                          *|
\************************************************************************/
package cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.lp;

/** A PivotPair instance holds a row and a column of an 
 * {@link LPDictionary LPDictionary}.
 *  @see LPDictionary
 *  @author Peter Bro Miltersen, February 2002. */
public class PivotPair {

    /** <tt>in</tt> holds the index of a column of an 
     *  {@link LPDictionary LPDictionary}. Note that the
     *  index is to a column of the dictionary (e.g., the 5th column)
     *  and <i>not</i> to a variable (such as <i>x</i><sub>5</sub>.)
     *  The {@link LPDictionary LPDictionary} method
     *  {@link LPDictionary#variableOfColumn(int) variableOfColumn} can
     *  be used to convert the index to the index of a variable.
     *
     *  <tt>in</tt> can be 0. This indicates that no column is selected.*/
    public int in;

    /** <tt>out</tt> holds the index of a row of an 
     *  {@link LPDictionary LPDictionary}. Note that the
     *  index is to a row of the dictionary (e.g., the 5th row)
     *  and <i>not</i> to a variable (such as <i>x</i><sub>5</sub>.)
     *  The {@link LPDictionary LPDictionary} method
     *  {@link LPDictionary#variableOfRow(int) variableOfRow} can
     *  be used to convert the index to the index of a variable.
     *
     *  <tt>out</tt> can be 0. This indicates that no row is selected.
     */
    public int out;
    
    /** constructs a PivotPair with <tt>in = i</tt>, <tt>out = o</tt>. */
    public PivotPair(int i, int o){in = i; out = o;}

	public String toString() { return "(" + in + "," + out + ")"; }
}
