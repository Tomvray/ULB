#include "Utils.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

void Iterative_algo(Instance *inst, int *modes);

int *parse_command_line(int argc, char *argv[]) {
    int *modes;

    modes = (int *)malloc(3 * sizeof(int));
    if (argc != 4)
    {
        printf("Error: Invalid number of arguments\n");
        return NULL;
    }
    //pivoting rule
    if (!strcmp(argv[1], "--first"))
        modes[0] = 1;
    else if (!strcmp(argv[1], "--best"))
        modes[0] = 2;
    else
        printf("Error: Invalid pivoting rule\n");
    
    //neighborhood
    if (!strcmp(argv[2], "--transpose"))
        modes[1] = 1;
    else if (!strcmp(argv[2], "--exchange"))
        modes[1] = 2;
    else if (!strcmp(argv[2], "--insert"))
        modes[1] = 3;
    else
        printf("Error: Invalid neighborhood\n");
    
    //initial solution
    if (!strcmp(argv[3],"--random-init"))
        modes[2] = 1;
    else if (!strcmp(argv[3], "--srz"))
        modes[2] = 2;
    else
        printf("Error: Invalid initial solution\n");
    return modes;
}

int main(int argc, char *argv[])
{
    int *modes = parse_command_line(argc, argv);
    
    Instance *inst = (Instance *) malloc(sizeof(Instance));
    Read_Instance("instances/Benchmarks/ta051", inst);
    printf("n_machine: %d\n", inst->n_machines);
    Iterative_algo(inst, modes);
    free_Instance(inst);
    free(modes);
    return 0;
}
