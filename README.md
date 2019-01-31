# GAME THEORETIC LIBRARY

This library contains a domain-independent framework for modeling normal-form and extensive-form games,
and offers a unique collection of algorithms for solving these games.

## MCCR (Monte Carlo Continual Resolving)

Most important files are:

    CRAlgorithm
    OOSAlgorithm
    OOSAlgorithmData
    CRExperiments

To reproduce experiments you will need to also install python with fairly standard dependencies (numpy, pandas, matplotlib). Experiments are computationally heavy and were distributed on a cluster.

Some approximate stats:

    IIGS-5  | public states: 243     memory: 0.5GB    t/1M root iters: 6      t/ 10M in gadget = 4h
    LD 116  | public states: 2176    memory: <1GB     t/1M root iters: 3.4    t/ 10M in gadget = 21h
    GP 3322 | public states: 1046    memory: <0.2GB   t/1M root iters: 5      t/ 10M in gadget = 14.5h

There are scripts which generate commands that each node in the cluster should independetly process, the results then need to be concatenated.

Look at `mccr_experiments/run_all.sh` for more details.

## License

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
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.

## GT Library is developed at:

Agent Technology Center (ATG)
Department of Computer Science and Engineering
Czech Technical University in Prague
Czech Republic

## Compile / package / run

Add cplex static libraries to java library path (for example in .bashrc).
Update path appropriately:

    export LD_LIBRARY_PATH="$LD_LIBRARY_PATH:`pwd`/libs/cplex"

Then run in root of the project (this launches tests as well):

    mvn package

You can launch some class with

    cd target
    java -cp gtlibrary.jar cz.agents.gtlibrary.algorithms.runner.SMJournalExperiments

## Integration with other projects:

package cz/agents/gtlibrary/algorithms/sequenceform/refinements/quasiperfect contains several files from GTF framework by Troels Bjerre Sorensen (http://www.itu.dk/people/trbj/gtf.html)

## Main authors:

  - Branislav Bosansky <branislav.bosansky@agents.fel.cvut.cz> (contact person)
  - Jiri Cermak <jiri.cermak@agents.fel.cvut.cz>
  - Viliam Lisy <viliam.lisy@agents.fel.cvut.cz>
  - Ondrej Vanek <ondrej.vanek@agents.fel.cvut.cz>
  - Michal Šustr <michal.sustr@aic.fel.cvut.cz> (Continual resolving algorithm)

## Other Contributors:

Games of Oshi-Zumo and Tron were implemented by Marc Lanctot.

## More information:

http://agents.felk.cvut.cz/topics/Computational_game_theory

