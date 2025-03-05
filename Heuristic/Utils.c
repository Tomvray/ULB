#include <stdio.h>  
#include <stdlib.h>
#include <string.h>
#include "Utils.h"


int Read_Instance(char *path, Instance *inst)
{
    FILE *fp;
    fp = fopen(path, "r");
    if (fp == NULL)
    {
        printf("Error: File not found\n");
        return 1;
    }

    //parsing
    char line[256];
    fgets(line, sizeof(line), fp);

    // Tokenize the line by spaces
    char *token = strtok(line, " ");
    int n_jobs = atoi(token);
    token = strtok(NULL, " ");
    int n_machines = atoi(token);

    printf("n_machine: %d\n", n_machines);
    printf("n_job: %d\n", n_jobs);

    int **costs = (int **)malloc(n_jobs * sizeof(int *));
    for (int i = 0; i < n_jobs; i++)
    {
        costs[i] = (int *)malloc(n_machines * sizeof(int));
    }
    int i = 0;
    while (fgets(line, sizeof(line), fp))
    {
        // Tokenize the line by spaces
        char *token = strtok(line, " ");
        int k = 0;
        while (token != NULL)
        {
            token = strtok(NULL, " ");
            //printf("token: %s\n", token);
            if (token != NULL)
                costs[i][k] = atoi(token);
                token = strtok(NULL, " ");
            k++;
        }
        //costs[i][k] = -1;
        i++;
    }

    //printf("costs: \n");
    // i = 0;
    // int k = 0;
    // for (int i = 0; i < n_jobs; i++)
    // {
    //     k = 0;
    //     while (costs[i][k] != 0)
    //     {
    //         printf("%d ", costs[i][k]);
    //         k++;
    //     }
    //     printf("\n");

    // }
    fclose(fp);

    if (inst == NULL)
    {
        printf("Error: Instance is NULL\n");
        return 1;
    }
    inst->n_jobs = n_jobs;
    inst->n_machines = n_machines;
    inst->costs = costs;
    inst->c_matrix = malloc(inst->n_jobs * sizeof(unsigned long int *));
    if (inst->c_matrix == NULL)
    {
        printf("Error: c_matrix is NULL\n");
        return 1;
    }
    for (int i = 0; i < inst->n_jobs; i++)
    {
        inst->c_matrix[i] = malloc(inst->n_machines * sizeof(unsigned long int));
        if (inst->c_matrix[i] == NULL)
        {
            printf("Error: c_matrix[%d] is NULL\n", i);
            return 1;
        }
    }
    return 0;
}


void    free_Instance(Instance *inst)
{
    for (int i = 0; i < inst->n_jobs; i++)
    {
        free(inst->costs[i]);
        free(inst->c_matrix[i]);
    }
    free(inst->costs);
    free(inst->c_matrix);
    free(inst);
}

int Cost(Instance *inst, int *premutation)
{
    int cost = 0;
    for (int i = 0; i < inst->n_jobs; i++)
    {
        cost += inst->costs[i][premutation[i]];
    }
    return cost;
}

// int main()
// {
//     Read_Instance("/home/thomvray/documents/ULB/Heuristic/instances/Benchmarks/ta051");
//     return 0;
// }