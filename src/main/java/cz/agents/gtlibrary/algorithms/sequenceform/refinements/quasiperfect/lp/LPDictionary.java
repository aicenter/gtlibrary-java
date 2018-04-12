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

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.DoubleReal;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Rational;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Real;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.StringTokenizer;

/** Each instance of <tt>LPDictionary</tt> holds a linear programming 
 *  dictionary, as defined by Chv&aacute;tal. <tt>LPDictionary</tt> contains 
 *  methods for performing
 *  the simplex algorithm. The implementation is optimized for
 *  educational visualization purposes, rather than for efficiency and should
 *  be used to solve toy instances only (unless the method 
 {@link #PCxSolve() PCxSolve()} is used to call an external solver).
 * 
 *  <tt>LPDictionary</tt> is polymorphic; entries in the dictionary
 *  can be objects of any class implementing {@link cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Real Real}. All
 *  entries ought to be from the same class, though. 
 *  <P> Running the following program visualizes the example 
 *  presented by Chv&aacute;tal, pages 13-17.
 *  <blockquote><pre>
 *public class Test {
 *
 *    public static void main(String[] args) {
 *        int[][] i = {{0,5,4,3},{5,-2,-3,-1},{11,-4,-1,-2},{8,-3,-4,-2}};
 *        LPDictionary d = new LPDictionary(i);
 *        System.out.println(d);
 *        while(!d.isOptimal()){
 *            d.pivot();
 *            System.out.println(d);
 *        }
 *    }
 *}
 *  </pre></blockquote>
 *  @author Peter Bro Miltersen, February 2002. Kristoffer Hansen, March 2002.
 Troels Bjerre S�rensen, Feb 16. 2004
 */

public class LPDictionary<T extends Real<T>> {

	private T zero;
	private T one; 

	/** is used to indicate the status of the dictionary */
	public enum Status { INFEASIBLE, OPTIMAL, UNBOUNDED, FEASIBLE }
		
	/** is the number of non-basic variables in the dictionary. */
	public int n;

	/** is the number of equations (and basic variables) in the 
	 *  dictionary. */
	public int m;

	/*  basic_vars[1..m] holds the indices of the basic variables.
	 *  basic_vars[j] holds the index of the variable on the left hand
	 *  side of the j'th row of the tableau. */
	private int[] basic_vars;

	/*  non_basic_vars[1..n] holds the indices of the non-basic variables.
	 *  basic_vars[j] holds the index of the variable on the left hand
	 *  side of the j'th row of the tableau. */ 
	private int[] non_basic_vars;

	/*  tableau[0..m][0..n] holds the coefficients of the dictionary: 
	 *  
	 *  tableau[i][*] holds the i'th row of the tableau. The objective 
	 *  function is given as the 0'th row, while the i'th row, (i>0), 
	 *  expresses the variable with index basic_vars[i] as a linear 
	 *  combination of the non-basic variables. The entry tableau[i][0] 
	 *  holds the constant coefficient of the i'th row. The coefficent of 
	 *  the variable with index non_basic_vars[j] is given in tableau[i][j].*/
	private T[][] tableau;

	/*  If unbounded_var is not zero, the objective function can be made
	 *  arbitrarily large by increasing the variable with index 
	 *  basic_vars[unbounded_var] */
	private int unbounded_var = 0;

	/** constructs a dictionary whose tableau takes values from <tt>t</tt>.
	 *  The resulting dictionary has <tt>m = t.length - 1</tt> 
	 *  basic variables named <i>x</i><sub><tt>n</tt>+1</sub>,..,
	 *  <i>x</i><sub><tt>n+m</tt></sub>,and <tt>n = t[0].length - 1</tt> 
	 *  non-basic variables, named <i>x</i><sub>1</sub>,..,<i>x</i><sub>n</sub>.
	 *  <P><tt>t[i][*]</tt> should hold the <tt>i</tt>'th row of the tableau. 
	 *  The objective 
	 *  function should be given as the zeroth row, while the <tt>i</tt>'th row, for
	 *  <tt>i</tt> bigger than zero, 
	 *  should express <i>x</i><tt><sub>n+i</sub></tt> as a linear 
	 *  combination of the non-basic variables. The entry <tt>t[i][0]</tt> 
	 *  should hold the constant coefficient of the <tt>i</tt>'th row. 
	 *  The coefficent of <i>x</i><tt><sub>j</sub></tt> should be given in 
	 *  <tt>t[i][j]</tt>.*/
	public LPDictionary(T[][] t){
		//System.err.println("\rLPDictionary: " + t.length + " x " + t[0].length);
		m = t.length - 1;
		n = t[0].length - 1;
		zero = t[0][0].zero();
		one = t[0][0].one();
		non_basic_vars = new int[n + 1];
		for (int i = 1 ; i <= n ; i++) non_basic_vars[i] = i;
		basic_vars = new int[m + 1];
		for (int i = 1 ; i <= m ; i++) basic_vars[i] = i + n;
		tableau = t;
	}

	/** converts the <tt>int</tt> array <tt>t</tt> to a 
	 * {@link cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Rational Rational} array and then behaves as
	 * {@link #LPDictionary(Real[][]) LPDictionary(Real[][])} */
	public static LPDictionary<Rational> make(int[][] t){
		Rational[][] templeau = new Rational[t.length][t[0].length];
		for(int i = 0 ; i < t.length ; i++)
			for(int j = 0 ; j < t[i].length ; j++)
				templeau[i][j] = new Rational(t[i][j]);
		return new LPDictionary<Rational>(templeau);
	}

	/** converts the <tt>double</tt> array <tt>t</tt> to a 
	 * {@link cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.DoubleReal DoubleReal} array and then behaves as
	 * {@link #LPDictionary(Real[][]) LPDictionary(Real[][])} */
	public static LPDictionary<DoubleReal> make(double[][] t){
		DoubleReal[][] templeau = new DoubleReal[t.length][t[0].length];
		for(int i = 0 ; i <= t.length ; i++)
			for(int j = 0 ; j <= t[i].length ; j++)
				templeau[i][j] = new DoubleReal(t[i][j]);
		return new LPDictionary<DoubleReal>(templeau);
	}

	private String rowToString(T row[], String vname){
		String s = "";
		T val;
		boolean notfirst = false;
		if(!(row[0].isZero())) {
			s = row[0].toString();
			notfirst = true;
		}
		for (int j = 1 ; j <= n ; j++) {
			if (!row[j].isZero()) {
				if (notfirst){// we already had a non-zero term
					if (row[j].isPositive()){s = s+ " + "; val = row[j];}
					else {s = s + " - "; val = row[j].negate();}
					if (val.isOne())s = s + vname + non_basic_vars[j];
					else s = s + val.toString() + " " + vname + non_basic_vars[j];
				} else { // the j'th term is the first non-zero one
					if (row[j].isPositive()){val = row[j];}
					else {s="-"; val = row[j].negate();}
					if (val.isOne())s = s + vname + non_basic_vars[j];
					else s = s + val.toString() + " " + vname + non_basic_vars[j];
					notfirst = true;
				}
			}
		}
		if (s.equals("")) s = "0";
		return s;
	}

	private String rowToLatex(T row[], String vname){
		String s = "";
		T val;
		boolean notfirst = false;
		if (!(row[0].isZero())) {s=row[0].toLaTeX(); notfirst = true;}
		for (int j=1; j<=n; j++) {
			if (row[j].isZero()) {//The term is zero, so we write nothing.
				s+="& &";
			} else if (notfirst) {// we already had a non-zero term
				if (row[j].isPositive()){s = s+ "& + & "; val = row[j];}
				else {s = s + "& - & "; val = row[j].negate();}
				if(val.isOne())s = s + vname + non_basic_vars[j];
				else s = s + val.toLaTeX() + vname + non_basic_vars[j];
			} else { // the j'th term is the first non-zero one
				s+="& &";
				if (row[j].isPositive()){val = row[j];}
				else {s="-"; val = row[j].negate();}
				if(val.isOne())s = s + vname + non_basic_vars[j];
				else s = s + val.toLaTeX() +  vname + non_basic_vars[j];
				notfirst = true;}
		}
		if(s.equals("")){s = "0";}
		return s;	
	}

	/** returns a <tt>String</tt> representation of the dictionary, closely
	 *  following the conventions of Chv&aacute;tal. */
	public String toString(){
		String term;
		String s = "";
		for(int i = 1 ; i <= m ; i++)
			s = s + "x" + basic_vars[i] + " = " 
				+ rowToString(tableau[i], "x") + "\n";
		s = s + "-----------------------------------\n";
		s = s + "z  = " + rowToString(tableau[0],"x") + "\n";
		return s;
	}

	/** returns a LaTeX representation of the dictionary, closely
	 *  following the conventions of Chv&aacute;tal. */
	public String toLaTeX(){
		String s="\\[\n\\begin{array}{";
		for (int j=0; j<(n+1)*2; j++)
			s+="r@{\\hspace{5pt}}";
		s+="r}";
		for (int i=1; i<=m; i++) {
			s+="\nx_"+basic_vars[i]+" & = & " + rowToLatex(tableau[i],"x_");
			if (i==m) s+="\\smallskip";
			s+=" \\\\";
		}
		s+="\\hline";
		s = s + "\n\\rule{0pt}{10pt} z & = & " + rowToLatex(tableau[0],"x_")+" \\\\";
		s = s + "\n\\end{array}\n\\]";
		return s;
	}

	/** prints the dictionary in a new file in the MPS format. 
	 *  @author Troels Bjerre S�rensen */
	public void toMPS(String name) {
		try {
			File mpsfile = new File(name + ".mps");
			PrintStream out = new PrintStream(new FileOutputStream(mpsfile));
			out.println("NAME          " + name);
			out.println("ROWS");
			out.println(" N  COST");
			for(int y = 1 ; y <= m ; y++)
				out.println(" L  Y" + y);
			out.println("COLUMNS");
			for(int x = 1 ; x <= n ; x++) {
				String varname = "X" + x;
				for(int y = 0 ; y <= m ; y++) if(!tableau[y][x].isZero()) {
					String eqname = y == 0 ? "COST" : "Y" + y;
					String value = asciify(-tableau[y][x].doubleValue());
					out.println("    " +
							varname +
							blanks(10 - varname.length()) +
							eqname + 
							blanks(22 - eqname.length() - value.length()) +
							value);
				}
			}
			out.println("RHS");
			for(int y = 1 ; y <= m ; y++) {
				String eqname = "Y" + y;
				String value = asciify(tableau[y][0].doubleValue());
				out.println("    RHS       " +
						eqname +
						blanks(22 - eqname.length() - value.length()) +
						value);
			}
			out.println("BOUNDS");
			out.println("ENDATA");
			out.close();
		} catch (FileNotFoundException e) {
			System.err.println("SURT: "+e);
			System.exit(1);
		}
	}

	private String blanks(int k) {
		char[] result = new char[k];
		Arrays.fill(result, ' ');
		return new String(result);
	}

	private String asciify(double val) {
		String result = ""+val;
		if(result.length() <= 12) return result;
		int index = result.indexOf("E");
		if(index == -1) {
			return result.substring(0, 12);
		} else {
			String exppart = result.substring(index, result.length());
			return result.substring(0, 12 - exppart.length()) + exppart;
		}
	}

	/** returns the index of the column of variable 
	 *  <i>x</i><sub><tt>v</tt></sub> if 
	 *  <i>x</i><sub><tt>v</tt></sub> is a non-basic
	 *  variable. If not, 0 is returned. */
	public int columnOf(int v){
		for(int i = 1 ; i <= n ; i++)
			if(non_basic_vars[i] == v) return i;
		return 0;
	}

	/** returns the index of the row of variable 
	 *  <i>x</i><sub><tt>v</tt></sub> 
	 *  if <i>x</i><sub><tt>v</tt></sub> is a basic
	 *  variable. If not, 0 is returned. */
	public int rowOf(int v) {
		for(int i = 1 ; i <= m ; i++)
			if(basic_vars[i] == v) return i;
		return 0;
	}

	/** returns the index of the (non-basic) variable in column 
	 *  number <i>i</i>. */
	public int variableOfColumn(int i) {
		return non_basic_vars[i];
	}

	/** returns the index of the basic variable of row number <i>i</i>. */
	public int variableOfRow(int i) {
		return basic_vars[i];
	}

	/** returns the reward of the objective function on the solution
	 *  associated with the dictionary (even if the solution is not
	 *  feasible). */
	public T getValue(){
		return tableau[0][0];
	}

	/** returns the solution associated with the dictionary as a <tt>Real</tt>
	 *  array of size <tt>m</tt>+<tt>n</tt>+1. The reward of variable
	 *  <i>x<sub>i</sub></i> is in entry <i>i</i> of the array. */
	public T[] getSolution() {
		T[] sol = new ArrayList<T>(Collections.nCopies(m + n + 1, zero)).toArray(tableau[0]); 
		// won't fit, so new array with right type will be made...
		for(int row = 1 ; row <= m ; row++)
			sol[variableOfRow(row)] = tableau[row][0];
		return sol;
	}

	/** returns a <tt>Real</tt> array of size <tt>m</tt>+<tt>n</tt>+1 which, if
	 *  this dictionary is optimal, contains the optimal dual
	 *  solution. The reward of variable <i>y<sub>j</sub></i> is in entry
	 *  <i>j</i> of the array. Here, <i>y<sub>j</sub></i> for <i>j</i>
	 *  in 1,..,<tt>n</tt> are the dual slack variables corresponding to the
	 *  non-slack variables of the original dictionary (i.e., 
	 *  <i>x<sub>1</sub></i>,..,<i>x</i><sub><tt>n</tt></sub>), while 
	 *  the variables <i>y</i><sub><tt>n</tt>+1</sub>,
	 *  ..<i>y</i><sub><tt>n</tt>+<tt>m</tt></sub> are the non-slack dual 
	 *  variables. */
	public T[] getDualSolution(){
		T[] sol = new ArrayList<T>(Collections.nCopies(m + n + 1, zero)).toArray(tableau[0]);
		// won't fit, so new array with right type will be made...
		for(int col = 1; col <= n ; col++)
			sol[variableOfColumn(col)] = tableau[0][col].negate();
		return sol;
	}

	/** return <tt>true</tt> iff the dictionary is optimal **/
	public boolean isOptimal(){
		for(int j=1; j<=n; j++)
			if((tableau[0][j]).isPositive()){return false;}
		return true;
	}

	/** returns <tt>true</tt> iff the dictionary is feasible **/
	public boolean isFeasible(){
		for(int i=1; i<=m; i++)
			if((tableau[i][0]).isNegative()){return false;}
		return true;
	}

	int pivotnumber = 0;
	/** performs a pivot on the dictionary with <tt>p.in</tt> entering the
	 *  basis and <tt>p.out</tt> leaving it. **/
	public void pivot(PivotPair p){
		unbounded_var = 0;
		int in = p.in;
		int out = p.out;
		if (in != 0 && out != 0){
			int name_in = non_basic_vars[in];
			int name_out = basic_vars[out];
			T cof = tableau[out][in];
			T minuscof = cof.negate();
			T invcof = cof.invert();
			for (int j = 0 ; j <= n ; j++) {
				if (j == in) {
					tableau[out][j] = invcof;
				} else {
					tableau[out][j] = tableau[out][j].divide(minuscof);
				}
			}
			for (int i = 0 ; i <= m; i++)
				if (i != out) {
					cof = tableau[i][in];
					tableau[i][in] = zero;
					for(int j = 0 ; j <= n ; j++)
						tableau[i][j] = 
							tableau[i][j].add(tableau[out][j].multiply(cof));
				}
			basic_vars[out] = name_in;
			non_basic_vars[in] = name_out;
		}
		//System.err.println((++pivotnumber) + ":\t" + p + "\t" + tableau[0][0].doubleValue());
		//System.err.println(this);
	}

	/** performs the pivot on the dictionary suggested by Bland's rule */ 
	public void pivot(){
		pivot(bland());
	}

	private T increaseOf(int in){
		boolean found = false;
		int v = 0;
		T limit = zero;
		for(int i=1; i<=m; i++)
			if((tableau[i][in].compareTo(zero))<0){
				if(!found){
					found = true;
					v = i;
					limit = (tableau[i][0].divide(tableau[i][in])).negate();
				}
				else{
					T newlimit = 
						(tableau[i][0].divide(tableau[i][in])).negate();
					int c = newlimit.compareTo(limit);
					if((c<0)||((c==0)&&(basic_vars[i]<basic_vars[v]))){
						v = i;
						limit = newlimit;
					}
				}
			}
		if(!found){return null;}
		return (tableau[0][in]).multiply(limit);
	}


	private int largestIncreaseIn(){
		boolean found = false;
		int v=0;
		T increase = zero;
		for(int j=1; j<=n; j++){
			if((tableau[0][j]).compareTo(zero)>0){
				if(!found){
					found = true;
					v = j;
					increase = increaseOf(v);
					if(increase==null){return v;}
				}
				else{
					T newincrease = increaseOf(j);
					if(newincrease==null){return j;}
					if(newincrease.greater(increase))
					{v=j; increase=newincrease;}
				}
			}
		}
		return v;
	}

	private int largestCoefficientIn(){
		boolean found = false;
		int v=0;
		for(int j=1; j<=n; j++){
			if((tableau[0][j]).compareTo(zero)>0){
				if(!found){
					found = true;
					v = j;
				}
				else
					if((tableau[0][j]).greater(tableau[0][v])){v=j;}
			}
		}
		return v;
	}

	/** returns the index of the column containing the non-basic variable
	 *  that Bland's rule suggests should enter the basis. */
	private int blandIn(){
		boolean found = false;
		int v=0;
		for(int j=1; j<=n; j++)
		{
			if((tableau[0][j]).compareTo(zero)>0){
				if(!found){
					found = true;
					v = j;
				}
				else{
					if(non_basic_vars[j] < non_basic_vars[v]){v = j;}
				}
			}
		}
		return v;
	}

	/** if <tt>in</tt> is the index of a column of a variable that should 
	 *  enter the
	 *  basis, the index of the row containing the basic variable with 
	 *  the smallest index that
	 *  might leave the basic is returned. */
	public int blandOut(int in){
		boolean found = false;
		int v = 0;
		T limit = zero;
		for(int i=1; i<=m; i++)
			if((tableau[i][in].compareTo(zero))<0){
				if(!found){
					found = true;
					v = i;
					limit = (tableau[i][0].divide(tableau[i][in])).negate();
				}
				else{
					T newlimit = 
						(tableau[i][0].divide(tableau[i][in])).negate();
					int c = newlimit.compareTo(limit);
					if((c<0)||((c==0)&&(basic_vars[i]<basic_vars[v]))){
						v = i;
						limit = newlimit;
					}
				}
			}
		return v;
	}

	/** returns the pivot that Bland's rule suggests. 
	 *  The pivot is <i>not</i> performed on the dictionary. */ 
	public PivotPair bland(){
		int in = blandIn();
		if(in == 0){return new PivotPair(0,0);}
		int out = blandOut(in);
		return new PivotPair(in, out);
	}

	/** returns the pivot that the largest coefficient rule suggests.
	 *  The pivot is <i>not</i> performed on the dictionary. */
	public PivotPair largestCoefficient(){
		int in = largestCoefficientIn();
		if(in == 0){return new PivotPair(0,0);}
		int out = blandOut(in);
		return new PivotPair(in, out);
	}

	/** returns the pivot that the largest increase rule suggests. 
	 *  The pivot is <i>not</i> performed on the dictionary. */
	public PivotPair largestIncrease(){
		int in = largestIncreaseIn();
		if(in == 0){return new PivotPair(0,0);}
		int out = blandOut(in);
		return new PivotPair(in, out);
	}

	/** Performs the one-phase simplex algorithm on the dictionary. 
	 *  If the dictionary is initially infeasible, 
	 *  INFEASIBLE s returned.
	 *  Otherwise, the basic (one-phase) simplex algorithm is performed
	 *  on the dictionary, using Bland's rule. If the algorithm terminates 
	 *  with an optimal dictionary, OPTIMAL is returned.
	 *  If the final dictionary is unbounded, UNBOUNDED is
	 *  returned. */  

	public Status onePhaseSimplex() {
		if(!isFeasible()) { return Status.INFEASIBLE; }
		while(true){
			PivotPair b = bland();
			if(b.in == 0){return Status.OPTIMAL;}
			else if(b.out == 0){unbounded_var = b.in; return Status.UNBOUNDED;}
			else{pivot(b);}}
	}

	public Status twoPhaseSimplex() {
		//System.out.println("twoPhaseSimplex()\n" + this);
		// Check whether we need twoPhase
		//System.err.println("\rfirst phase...");
		int out = 0;
		for (int row = 1 ; row <= m ; row++) {
			if (out != 0) {
				if (tableau[row][0].less(tableau[out][0])) out = row;
			} else {
				if (tableau[row][0].less(zero)) out = row;
			}
		}
		if (out == 0) return onePhaseSimplex();

		//System.out.println("We need two phase");

		// Backup cost function
		
		T[] cost = tableau[0].clone();
		
		// Add new variable
		
		for (int row = 0 ; row <= m ; row++) {
			ArrayList<T> temp = new ArrayList<T>(tableau[row].length);
			for (T elm : tableau[row]) temp.add(elm);
			temp.add(one);
			tableau[row] = temp.toArray(tableau[row]);
		}
		n++;
		int[] temp = new int[n + 1];
		System.arraycopy(non_basic_vars, 0, temp, 0, non_basic_vars.length);
		temp[n] = n + m; // name of new var
		non_basic_vars = temp;
		Arrays.fill(tableau[0], zero);
		tableau[0][n] = one.negate();

		//System.out.println("Before pivot:\n" + this);

		// Make pseudo pivot
		
		pivot(new PivotPair(n, out));

		//System.out.println("After pivot:\n" + this);
		// Perform single phase with new variable
		
		if (onePhaseSimplex() != Status.OPTIMAL) throw new RuntimeException("LPDictionary: trold made some mistake... feed him");
		//System.out.println("After one phase simplex:\n" + this);
		if (! getValue().isZero()) return Status.INFEASIBLE;

		// Remove new var from basis, if it is there
		
		//System.err.println("\rfirst phase complete");
		
		for (int row = 1 ; row <= m ; row++) {
			//System.err.println("variableOfRow(" + row + ") = " + variableOfRow(row));
			if (variableOfRow(row) == m + n) { // found the new var. Pivot it out.
				//System.out.println("Phase one var in basis. Get it out");
				for (int col = 1 ; col <= n ; col++) {
					if (! tableau[row][col].isZero()) {
						//System.out.println("Found it. Pivoting " + col + " and " + row);
						pivot(new PivotPair(col, row));
						break;
					}
				}
				break;
			}
		}

		/*
		System.out.println("tableau:");
		for (T[] row : tableau) {
			for (T e : row) {
				System.out.print(e + "\t");
			}
			System.out.println();
		}
		System.out.println();
		*/
		
		// Delete new variable
		
		boolean found = false;
		for (int col = 1 ; col < n ; col++) {
			if (variableOfColumn(col) == m + n) {
				found = true;
				for (int row = 1 ; row <= m ; row++) {
					tableau[row][col] = tableau[row][n];
					tableau[row][n] = zero;
				}
				non_basic_vars[col] = non_basic_vars[n];
			}
		}
		if (! found) throw new RuntimeException("LPDictionary: Did not find new variable... trold puched uf");
		n--;

		//System.out.println("After delete of first phase var:\n" + this);
		// Restore old cost function
		
		Arrays.fill(tableau[0], zero);
		for (int row = 1 ; row <= m ; row++) {
			if (variableOfRow(row) <= n) { // variable used to be in cost function. Substitute
				for (int col = 0 ; col <= n ; col++) {
					tableau[0][col] = tableau[0][col]
						.add(tableau[row][col].multiply(cost[variableOfRow(row)]));
				}
			}
		}
		for (int col = 1 ; col <= n ; col++) {
			if (variableOfColumn(col) <= n) { // variable used to be in cost function. Copy from old
				tableau[0][col] = tableau[0][col]
					.add(cost[variableOfColumn(col)]);
			}
		}
		//System.out.println("After restoring cost function:\n" + this);
		//System.err.println("\rsecond phase...");
		return onePhaseSimplex();
	}

	private T[] primal_solution;
	private T[] dual_solution;
	private T objective_value;
	
	public double[] pcx_primal;
	public double[] pcx_dual;
	public double pcx_objective;
	/** Executes a professional quality external LP-solver (PCx) on the 
	 *  LP instance of the dictionary. The dictionary itself is not 
	 *  updated, so the methods {@link #getSolution() getSolution()},
	 {@link #getDualSolution() getDualSolution()} and
	 *  {@link #getValue() getValue()} extracting the optimal solutions and their 
	 *  reward from the dictionary do <i>not</i> in general give the
	 *  optimal reward after applying {@link #PCxSolve() PCxSolve()}.
	 *  Instead, an optimal primal solution, an optimal dual solution and
	 *  the optimal reward of the objective function is printed out by
	 {@link #PCxSolve() PCxSolve()}.
	 *  If the dictionary is
	 *  initially infeasible, INFEASIBLE is
	 *  returned. If the external solver terminates with an optimal
	 *  solution,  OPTIMAL is returned.  If the external solver
	 *  finds the instance to be unbounded, UNBOUNDED is
	 *  returned.<br><br>
	 *  <i>This method is only supported on Linux machines on DAIMI</i> 
	 *  @author Troels Bjerre S�rensen, Feb 16. 2004*/
	public Status PCxSolve() {
		pcx_primal = new double[n + 1];
		pcx_dual = new double[m + 1];
		String name = "MPS" + System.currentTimeMillis();
		toMPS(name);
		try {
			Runtime.getRuntime().exec("/users/trold/bin/i686/PCx.linux " + name + ".mps").waitFor();
		} catch (InterruptedException e) {
		} catch (IOException e){
			System.err.println("Could not execute external solver. Are you sure you are running Linux on DAIMI?");
			System.exit(1);
		}
		try {
			File outfile = new File(name+".out");
			File logfile = new File(name+".log");
			String line = "";
			BufferedReader varfile = new BufferedReader(new FileReader(outfile));
			String firstLine = varfile.readLine();
			if(firstLine.startsWith("INFEASIBLE")) {
				new File(name+".mps").delete();
				logfile.delete();
				outfile.delete();
				return Status.INFEASIBLE;
			}
			varfile.readLine();varfile.readLine(); //skip header
			StringTokenizer st;
			for(int i = 1 ; i <= n ; i++) {
				line = varfile.readLine();
				st = new StringTokenizer(line, " ", false);
				st.nextToken(); //skip line number
				st.nextToken(); //skip var name, since we know what it is...
				pcx_primal[i] = Double.parseDouble(st.nextToken());
			}
			varfile.readLine();varfile.readLine();varfile.readLine(); //skip header
			for(int i = 1 ; i <= m ; i++) {
				line = varfile.readLine();
				st = new StringTokenizer(line, " ", false);
				st.nextToken();st.nextToken();st.nextToken();st.nextToken();st.nextToken();
				pcx_dual[i] = - Double.parseDouble(st.nextToken());
			}
			varfile = new BufferedReader(new FileReader(logfile));
			while(!line.startsWith("Primal Objective = ")) line = varfile.readLine();
			pcx_objective = - Double.parseDouble(line.substring(19));
			new File(name+".mps").delete();
			logfile.delete();
			outfile.delete();
		} catch (IOException e) {
			if(e.toString().startsWith("java.io.FileNotFoundException")) {
				new File(name+".mps").delete();
				return Status.UNBOUNDED;
			}
			System.err.println("Something went wrong: " + e);
			System.exit(1);
		}
		return Status.OPTIMAL;
	}

	public T[] approxsolution;

	private void fixSolution() {
		for (int row = 1 ; row <= m ; row++) {
			if (approxsolution[variableOfRow(row)].isNegative()) {
				for (int col = 1 ; col <= n ; col++) {
					if (tableau[row][col].isPositive()) {
						T increase = approxsolution[variableOfRow(row)]
							.divide(tableau[row][col])
							.negate();
						approxsolution[variableOfColumn(col)] =
							approxsolution[variableOfColumn(col)].add(increase);
						for (int trow = 1 ; trow <= m ; trow++) {
							approxsolution[variableOfRow(trow)] =
								approxsolution[variableOfRow(trow)]
								.add(tableau[trow][col].multiply(increase));
						}
						assert approxsolution[variableOfRow(row)].isZero() : 
							"col " + col + ", X" + variableOfRow(row) + " == " + 
							approxsolution[variableOfRow(row)];
						break;
					}
				}
			}
		}
		for (int row = m ; row >= 1 ; row--) {
			if (approxsolution[variableOfRow(row)].isNegative()) {
				for (int col = 1 ; col <= n ; col++) {
					if (tableau[row][col].isPositive()) {
						T increase = approxsolution[variableOfRow(row)]
							.divide(tableau[row][col])
							.negate();
						approxsolution[variableOfColumn(col)] =
							approxsolution[variableOfColumn(col)].add(increase);
						for (int trow = 1 ; trow <= m ; trow++) {
							approxsolution[variableOfRow(trow)] =
								approxsolution[variableOfRow(trow)]
								.add(tableau[trow][col].multiply(increase));
						}
						assert approxsolution[variableOfRow(row)].isZero() : 
							"col " + col + ", X" + variableOfRow(row) + " == " + 
							approxsolution[variableOfRow(row)];
						break;
					}
				}
			}
		}
	}

	private boolean verifyApproxSolution() {
		for (int row = 1 ; row <= m ; row++) {
			T val = tableau[row][0];
			for (int col = 1 ; col <= n ; col++) {
				val = val.add(tableau[row][col].multiply(approxsolution[variableOfColumn(col)]));
			}
			if (! approxsolution[variableOfRow(row)].equals(val)) return false;
		}
		return true;
	}
	
	public Status pivotToSolution(T[] sol) {
		approxsolution = getSolution();
		for (int col = 1 ; col <= n ; col++) {
			approxsolution[variableOfColumn(col)] = sol[variableOfColumn(col)];
		}
		for (int row = 1 ; row <= m ; row++) {
			T val = tableau[row][0];
			for (int col = 1 ; col <= n ; col++) {
				val = val.add(tableau[row][col].multiply(approxsolution[variableOfColumn(col)]));
			}
			//if (val.isNegative()) return Status.INFEASIBLE;
			approxsolution[variableOfRow(row)] = val;
		}
		fixSolution();
		for (int row = 1 ; row <= m ; row++) {
			if (approxsolution[variableOfRow(row)].isNegative()) {
				return Status.INFEASIBLE;
			}
		}
		//assert verifyApproxSolution();
		for (int col = 1 ; col <= n ; col++) {
			//assert ! approxsolution[variableOfColumn(col)].isNegative() : approxsolution[variableOfColumn(col)].doubleValue();
			if (approxsolution[variableOfColumn(col)].isPositive()) {
				//System.err.print("\rcol: " + col + ", ");
				if (tableau[0][col].isPositive()) { // This variable should increase
					T limitinc = null;
					int limitrow = 0;
					for (int row = 1 ; row <= m ; row++) {
						if (tableau[row][col].isNegative()) {
							T newlimit = 
								approxsolution[variableOfRow(row)]
								.divide(tableau[row][col]).negate();
							if (limitinc == null || newlimit.less(limitinc)) {
								limitinc = newlimit;
								limitrow = row;
							}
						}
					}
					if (limitinc == null) return Status.UNBOUNDED;
					//assert ! limitinc.isNegative();
					if (limitinc.isPositive()) {
						approxsolution[variableOfColumn(col)] = 
							approxsolution[variableOfColumn(col)].add(limitinc);
						for (int row = 1 ; row <= m ; row++) {
							approxsolution[variableOfRow(row)] =
								approxsolution[variableOfRow(row)]
								.add(limitinc.multiply(tableau[row][col]));
						}
					}
					if (approxsolution[variableOfColumn(col)].isPositive()) {
						//assert approxsolution[variableOfRow(limitrow)].isZero();
						pivot(new PivotPair(col, limitrow));
					} else {
						//assert approxsolution[variableOfColumn(col)].isZero();
					}
				} else { // This variable should decrease
					T limitdec = null;
					int limitrow = 0;
					for (int row = 1 ; row <= m ; row++) {
						if (tableau[row][col].isPositive()) {
							T newlimit = 
								approxsolution[variableOfRow(row)]
								.divide(tableau[row][col]);
							if (limitdec == null || newlimit.less(limitdec)) {
								limitdec = newlimit;
								limitrow = row;
							}
						}
					}
					if (limitdec == null || approxsolution[variableOfColumn(col)].less(limitdec)) {
						limitdec = approxsolution[variableOfColumn(col)];
					}
					//assert ! limitdec.isNegative();
					if (limitdec.isPositive()) {
						approxsolution[variableOfColumn(col)] =
							approxsolution[variableOfColumn(col)].subtract(limitdec);
						for (int row = 1 ; row <= m ; row++) {
							approxsolution[variableOfRow(row)] =
								approxsolution[variableOfRow(row)]
								.subtract(limitdec.multiply(tableau[row][col]));
						}
					}
					if (approxsolution[variableOfColumn(col)].isPositive()) {
						//assert approxsolution[variableOfRow(limitrow)].isZero();
						pivot(new PivotPair(col, limitrow));
					} else {
						//assert approxsolution[variableOfColumn(col)].isZero() : approxsolution[variableOfColumn(col)].doubleValue();
					}
				}
			}
		}
		/*assert verifyApproxSolution();
		for (int col = 1 ; col <= n ; col++) assert approxsolution[variableOfColumn(col)].isZero()
			: "as[" + variableOfColumn(col) + "] = " + approxsolution[variableOfColumn(col)];
		for (int row = 1 ; row <= m ; row++) assert ! approxsolution[variableOfRow(row)].isNegative();
		*/
		return Status.FEASIBLE;
	}

	public Status pivotToPCxSolution() {
		PCxSolve();
		approxsolution = getSolution();
		Arrays.fill(approxsolution, zero);
		for (int col = 1 ; col <= n ; col++) {
			approxsolution[variableOfColumn(col)] =
				one.fromDouble(pcx_primal[variableOfColumn(col)]);
		}
		return pivotToSolution(approxsolution);
	}

	/** returns a matrix containing the tableau of the dictionary.
	 *  The format follows the same conventions as in 
	 *  {@link #LPDictionary(Real[][]) #LPDictionary(Real[][])}.
	 *  <i>Changing the matrix will also cause this dictionary to
	 *  change accordingly.</i> */ 
	public T[][] getTableau(){
		return tableau;
	}

	/** returns a table of the basic variables of the tableau. The
	 *  <tt>i</tt>'th entry of the table, <tt>i</tt> between 1 and
	 *  <tt>m</tt>, is the index of the basic variable of the <tt>i</tt>'th 
	 *  row of the tableau. <i>Changing the table will also cause this
	 *  dictionary to change accordingly.</i> */
	public int[] getBasicVariables(){
		return basic_vars;
	}

	/** returns a table of the non-basic variables of the tableau.
	 *  The <tt>j</tt>'th entry of the table, <tt>j</tt> between 1 and
	 *  <tt>n</tt>, is the index of the non-basic variable of the <tt>j</tt>'th
	 *  row of the tableau. <i>Changing the table will also cause this
	 *  dictionary to change accordingly.</i> */
	public int[] getNonBasicVariables(){
		return non_basic_vars;
	}

} 
