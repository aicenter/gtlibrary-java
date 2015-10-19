<?xml version = "1.0" encoding="UTF-8" standalone="yes"?>
<CPLEXSolution version="1.2">
 <header
   problemName="/home/jiricermak/workspace/gtlibrary/SSEIter.lp"
   objectiveValue="21.1666666666667"
   solutionTypeValue="1"
   solutionTypeString="basic"
   solutionStatusValue="1"
   solutionStatusString="optimal"
   solutionMethodString="dual"
   primalFeasible="1"
   dualFeasible="1"
   simplexIterations="0"
   writeLevel="1"/>
 <quality
   epRHS="1e-06"
   epOpt="1e-06"
   maxPrimalInfeas="0"
   maxDualInfeas="0"
   maxPrimalResidual="5.55111512312578e-16"
   maxDualResidual="3.5527136788005e-15"
   maxX="1"
   maxPi="21.1666666666667"
   maxSlack="37.8333333333333"
   maxRedCost="27.0490196078431"
   kappa="14.9751968461308"/>
 <linearConstraints>
  <constraint name="c1" index="0" status="BS" slack="0" dual="-0"/>
  <constraint name="c1" index="1" status="LL" slack="0" dual="21.1666666666667"/>
  <constraint name="c3" index="2" status="LL" slack="0" dual="21.1666666666667"/>
  <constraint name="c5" index="3" status="LL" slack="0" dual="21.1666666666667"/>
  <constraint name="c9" index="4" status="LL" slack="0" dual="1.31666666666667"/>
  <constraint name="c11" index="5" status="LL" slack="0" dual="1.40196078431373"/>
  <constraint name="c17" index="6" status="BS" slack="0" dual="0"/>
  <constraint name="c19" index="7" status="BS" slack="-37.8333333333333" dual="0"/>
  <constraint name="c21" index="8" status="BS" slack="0" dual="0"/>
  <constraint name="c23" index="9" status="BS" slack="0" dual="0"/>
  <constraint name="c25" index="10" status="LL" slack="0" dual="-0"/>
  <constraint name="c27" index="11" status="BS" slack="0" dual="-0"/>
 </linearConstraints>
 <variables>
  <variable name="_lPl0._((Pl0,_1,_0)),Pl1._((Pl1,_3,_0))g#3" index="0" status="BS" value="0.166666666666667" reducedCost="-0"/>
  <variable name="_lPl0._((Pl0,_1,_0)),Pl1._((Pl1,_3,_1))g#4" index="1" status="BS" value="0" reducedCost="-0"/>
  <variable name="_lPl0._((Pl0,_1,_1)),Pl1._((Pl1,_3,_0))g#5" index="2" status="BS" value="0.833333333333333" reducedCost="-0"/>
  <variable name="_lPl0._((Pl0,_1,_1)),Pl1._((Pl1,_3,_1))g#6" index="3" status="LL" value="0" reducedCost="-27.0490196078431"/>
  <variable name="_lPl0._(),Pl1._()g#0" index="4" status="UL" value="1" reducedCost="21.1666666666667"/>
  <variable name="_lPl0._((Pl0,_1,_0)),Pl1._()g#1" index="5" status="BS" value="0.166666666666667" reducedCost="-0"/>
  <variable name="_lPl0._((Pl0,_1,_1)),Pl1._()g#2" index="6" status="BS" value="0.833333333333333" reducedCost="-0"/>
  <variable name="_lv,Pl1._((Pl1,_3,_0))g#8" index="7" status="LL" value="0" reducedCost="-1.31666666666667"/>
  <variable name="_lv,Pl1._((Pl1,_3,_1))g#9" index="8" status="LL" value="0" reducedCost="-1.40196078431373"/>
  <variable name="_lIS.(Pl1).Pl1._(),Pl1._((Pl1,_3,_0))g#11" index="9" status="BS" value="0" reducedCost="-0"/>
  <variable name="_lIS.(Pl1).Pl1._(),Pl1._((Pl1,_3,_1))g#12" index="10" status="LL" value="0" reducedCost="-0"/>
 </variables>
</CPLEXSolution>
v(3_0)8 = -26.7058823529
v(3_1)9 = 0
v(IS, 3_0)11 >= -26.7058823529
v(IS, 3_0)11 >= -26.7058823529
v(IS, 3_1)12 >= 0
v(IS, 3_1)12 >= 0