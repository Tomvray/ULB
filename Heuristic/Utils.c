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

    int **jobs = (int **)malloc(n_jobs * sizeof(int *));
    for (int i = 0; i < n_jobs; i++)
    {
        jobs[i] = (int *)malloc(256 * sizeof(int));
    }
    int i = 0;
    while (fgets(line, sizeof(line), fp))
    {
        // Tokenize the line by spaces
        char *token = strtok(line, " ");
        int k = 0;
        while (token != NULL)
        {
            jobs[i][k] = atoi(token);
            k++;
            token = strtok(NULL, " ");
        }
        //jobs[i][k] = -1;
        i++;
    }

    printf("jobs: \n");
    i = 0;
    int k = 0;
    for (int i = 0; i < n_jobs; i++)
    {
        k = 0;
        while (jobs[i][k] != 0)
        {
            printf("%d ", jobs[i][k]);
            k++;
        }
        printf("\n");

    }
    for (int i = 0; i < n_jobs; i++)
    {
        free(jobs[i]);
    }
    free(jobs);
    fclose(fp);

    if (inst == NULL)
    {
        printf("Error: Instance is NULL\n");
        return 1;
    }
    inst->n_jobs = n_jobs;
    inst->n_machines = n_machines;
    inst->jobs = jobs;
    return 0;
}

// int main()
// {
//     Read_Instance("/home/thomvray/documents/ULB/Heuristic/instances/Benchmarks/ta051");
//     return 0;
// }