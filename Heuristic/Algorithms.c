
#include <stdio.h>
#include <string.h>
#include "Utils.h"
#include <stdlib.h>

void Iterative_algo(const char *mode, Instance *inst) {
    int algo = 0;
    if (strcmp(mode, "first") == 0)
        algo = 1;
    else if (strcmp(mode, "best") == 0)
        algo = 2;
    else if (strcmp(mode, "random") == 0)
        algo = 3;
    else
        printf("Error: Invalid mode\n");
    printf("algo: %d\n", algo);
}

int main(){
    Instance *inst = (Instance *) malloc(sizeof(Instance));
    Read_Instance("instances/Benchmarks/ta051", inst);
    Iterative_algo("first", inst);
    free_Instance(inst);
    return 0;
}