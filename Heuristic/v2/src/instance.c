#include <stdio.h>
#include <stdlib.h>
#include "instance.h"

PFSPInstance* read_instance(const char* filename) {
    PFSPInstance* inst = malloc(sizeof(PFSPInstance));
    FILE* f = fopen(filename, "r");
    if (!f) exit(1);

    fscanf(f, "%d %d", &inst->num_jobs, &inst->num_machines);

    inst->processing_times = malloc(inst->num_jobs * sizeof(int*));
    for (int i = 0; i < inst->num_jobs; i++) {
        inst->processing_times[i] = malloc(inst->num_machines * sizeof(int));
        for (int j = 0; j < inst->num_machines; j++) {
            int machine_id;
            fscanf(f, "%d %d", &machine_id, &inst->processing_times[i][j]);
        }
    }
    fclose(f);
    return inst;
}

void free_instance(PFSPInstance* inst) {
    for (int i = 0; i < inst->num_jobs; i++) free(inst->processing_times[i]);
    free(inst->processing_times);
    free(inst);
}
