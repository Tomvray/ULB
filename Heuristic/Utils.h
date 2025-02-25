typedef struct
{
    int n_jobs;
    int n_machines;
    int **jobs;
} Instance;
int    Read_Instance(char *path, Instance *inst);