package cz.agents.gtlibrary.utils;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 10/25/13
 * Time: 10:33 AM
 * To change this template use File | Settings | File Templates.
 */
public class CombinationGenerator {
       public static void main (String[] args) {
           for (int i=0; i<10; i++)
                System.out.println(Arrays.toString(generateCombinationWithoutRepeating(new int[] {1,2,3,4,5,6}, 3, i)));
       }

       public static int[] generateCombinationWithoutRepeating(int[] orderedSet, int whatK, int which) {
           int[] result = new int[whatK];
           if (whatK == 1) {
               if (which >= orderedSet.length) return null; // impossible
               else result[0] = orderedSet[which];
               return result;
           } else {
               for (int nIdx = 0; nIdx < orderedSet.length; nIdx++) {
                   int bincof = binomCoef(orderedSet.length - 1 - nIdx, whatK - 1);
                   if (which < bincof) {
                       result[0] = orderedSet[nIdx];
                       int[] newOrderedSet = new int [orderedSet.length-1-nIdx];
                       for (int j=nIdx+1; j<orderedSet.length; j++) {
                            newOrderedSet[j-1-nIdx]=orderedSet[j];
                       }
                       int[] recur = generateCombinationWithoutRepeating(newOrderedSet, whatK-1, which);
                       if (recur == null) return null; // impossible
                       else {
                           for (int j=1; j<whatK; j++) {
                               result[j] = recur[j-1];
                           }
                       }
                       return result;
                   }  else {
                       which = which - bincof;
                   }

               }
           }
           return null;
       }

       public static int binomCoef(int n, int k) {
           int result = 1;
           for (int i=n; i>n-k; i--) {
               result *= i;
           }
           for (int i=1; i<=k; i++) {
               result = result / i;
           }
           return result;
       }
}
