# Implementation for chapter 3 models in MASc thesis

___
*Overview: 8 solution techniques*
___
* Logic-based Benders decomposition model:
  * Pure CP master problem for batching/pure CP subproblem for scheduling
* Batching-only models:
  * Pure CP
  * Pure MIP
  * Greedy EDD heuristic
  * Size-constrained clustering
* Scheduling-only models:
  * Pure CP
  * Parallel scheme
  * Genetic algorithm with parallel scheme
___

This repository is organized as follows:
[/data](https://github.com/tangtan2/scheduling_composites_manufacturing/tree/master/data) contains the Jupyter notebooks with data visualizations and analysis of experimental results. The final graphics that will be used in my thesis can be seen in this [notebook](https://github.com/tangtan2/scheduling_composites_manufacturing/tree/master/data/analytics/experiments_2/visualizations.ipynb).

[/docs](https://github.com/tangtan2/scheduling_composites_manufacturing/tree/master/docs) contains a file detailing the project structure in more detail, including a brief explanation of each model.

[/src/main/java](https://github.com/tangtan2/scheduling_composites_manufacturing/tree/master/src/main/java) contains all of the model/algorithm source code as well as class definitions and helper functions.

The source code is self-contained and all dependencies are listed in the pom.xml file. As long as the necessary dependencies are installed on your computer, the source code is ready to download and compile. Maven will output a .jar file named run_experiments in the target folder. This file is capable of running any instance created using the template in [/data/templates](https://github.com/tangtan2/scheduling_composites_manufacturing/tree/master/data/templates), where the input is a config file generated using the gen_config.sh script. *Note: library filepaths from the CPLEX installation need to be added manually when calling java*
