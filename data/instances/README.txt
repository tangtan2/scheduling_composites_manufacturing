-------------------------------------------------
2020 INSTANCES
-------------------------------------------------
There are 9 JSON files in total:
1) tang_2020_instance_params.json - parameters that are unchanged for all instances
2) tang_2020_instance_results.json - UB for packing, UB for scheduling, and runtimes for all instances and models
3) tang_2020_instances_jobs_{100, 300, 500, 1000, 2000, 3000, 4000}.json - job-specific parameters for all 7 sets of jobs with 100 to 4000 jobs per instance
-------------------------------------------------
JSON FILE FORMATS
-------------------------------------------------
tang_2020_instance_params.json:

- bottom_tools -> list of bottom tools
--- name -> tool name
--- min_fill_requirement -> YES if tool has a minimum fill requirement, NO if it does not
--- capacity -> number of slots for top tools
--- qty -> number of available bottom tools
--- size -> tool size

- top_tools -> list of top tools
--- name -> tool name
--- min_fill_requirement -> YES if tool has a minimum fill requirement, NO if it does not
--- capacity -> number of slots for jobs

- combinations -> list of job to tool combination mappings
--- job_name -> 8-digit number that represents a specific type of job
--- top -> name of top tool in combination
--- bottom -> name of bottom tool in combination

- machines -> list of machines
--- name -> name of machine
--- batch_capacity -> number of batches the machine can accommodate
--- volume_capacity -> maximum volume a batch can be to be processed in this machine; if equal to 1, then there are not volume capacity constraints on this machine

- shifts -> list of shifts
--- name -> name of shift
--- day -> day of shift
--- start -> start of shift in terms of minutes past midnight
--- end -> end of shift in terms of minutes past midnight

- machine_shifts -> list of machine availability per shift
--- name -> name of machine
--- shift_{i} for i in [0, 48] -> status of machine for each shift, ON if machine is available, OFF if not

- labour_teams -> list of labour teams
--- name -> name of labour team
--- labour_skills -> list of skills that this labour team has

- machine_skills -> list of mappings between machines and required labour teams
--- machine_name -> name of machine
--- job_part_family -> part family of job being processed
--- labour_skill -> labour skill required to process job on machine
--- labour_team_qty -> quantity of labour team units needed

- labour_shifts -> list of labour team availability per shift
--- name -> name of labour team
--- shift_{i} for i in [0, 48] -> number of labour teams unit available for each shift

- sample_jobs -> list of jobs from which instances are sampled from
--- name -> 8-digit number that represents a specific type of job
--- prep_activity -> name of required tool prep activity
--- prep_process_time -> total process time for any tool batch made from this job in tool prep
--- layup_activity -> name of required layup activity
--- layup_process_time -> total process time for any tool batch made from this job in layup
--- cure_activity -> name of required curing activity
--- cure_process_time -> total process time for any tool batch made from this job in curing
--- demould_activity -> name of required demould activity
--- demould_process_time -> total process time for any tool batch made from this job in demould
-------------------------------------------------
tang_2020_instance_results.json:

- model_{0, 1, 2, 3} -> results for models 0, 1, 2, and 3
--- jobs_{100, 300, 500} -> for instances with 100, 300, and 500 jobs per instance
------ pack_UB -> upper bound in terms of number of autoclave batches in packing solution
------ sched_UB -> upper bound in terms of sum of job tardiness in scheduling solution
------ runtime -> total runtime of all model components

- model_{3, 4, 5, 6, 7, 8} ->
--- jobs_{1000, 2000, 3000, 4000} -> for instances with 1000, 2000, 3000, and 4000 jobs per instance
------ pack_UB -> upper bound in terms of number of autoclave batches in packing solution
------ sched_UB -> upper bound in terms of sum of job tardiness in scheduling solution
------ runtime -> total runtime of all model components
-------------------------------------------------
tang_2020_instances_jobs_{100, 300, 500, 1000, 2000, 3000, 4000}.json:

list of instances, for each instance
- num_jobs -> number of jobs in instance
- horizon -> scheduling horizon
- jobs -> list of jobs in instance
--- name -> 8-digit number that represents a specific type of job
--- due_date -> due date of job in terms of minutes
-------------------------------------------------