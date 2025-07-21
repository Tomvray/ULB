#ifndef INSTANCE_H
#define INSTANCE_H

typedef struct {
    int num_jobs;
    int num_machines;
    int **processing_times;
} PFSPInstance;

PFSPInstance* read_instance(const char* filename);
void free_instance(PFSPInstance* instance);

#endif
