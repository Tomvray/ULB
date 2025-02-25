#include "Utils.h"
#include <stdio.h>
#include <stdlib.h>


int main()
{
    Instance *inst = (Instance *) malloc(sizeof(Instance));
    Read_Instance("instances/Benchmarks/ta051", inst);
    printf("n_machine: %d\n", inst->n_machines);
    return 0;
}
