#ifndef SLS_H
#define SLS_H

#include "instance.h"

int compute_completion_time(PFSPInstance* inst, int* permutation);
int* tabu_search(PFSPInstance* inst, int max_time, int seed);
int* ant_colony_optimization(PFSPInstance* inst, int max_time, int seed);

#endif
