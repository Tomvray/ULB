typedef struct
{
    int n_jobs;
    int n_machines;
    int **costs;
    unsigned long int **c_matrix;
} Instance;

int    Read_Instance(char *path, Instance *inst);
void   free_Instance(Instance *inst);
void   SRZ(int *permutation, Instance *inst);