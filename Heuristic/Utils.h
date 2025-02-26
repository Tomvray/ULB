typedef struct
{
    int n_jobs;
    int n_machines;
    int **costs;
} Instance;
int    Read_Instance(char *path, Instance *inst);
void   free_Instance(Instance *inst);