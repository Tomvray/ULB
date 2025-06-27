#include "Utils.h"
#include <stdio.h>
#include <string.h>
#include <limits.h>
#include <stdlib.h>
#include <math.h>


static unsigned long int max(unsigned long int a, unsigned long int b){
    if (a > b)
        return a;
    return b;
}

static unsigned long int Complation_time(int *permutation, Instance *inst, int n_jobs){
    unsigned long int cost = 0;

    //first column and first line of the matrix
    inst->c_matrix[0][0] = inst->costs[permutation[0]][0];
    for (int i = 1; i < inst->n_machines; i++)
    {
        inst->c_matrix[0][i] = inst->costs[permutation[0]][i] + inst->c_matrix[0][i - 1];
    }
    //fill the matrix
    for (int j = 1; j < n_jobs; j++)
    {
        inst->c_matrix[j][0] = inst->costs[permutation[j]][0] + inst->c_matrix[j - 1][0];
    }
    for (int i = 1; i < n_jobs; i++)
    {
        for (int j = 1; j < inst->n_machines; j++)
        {
            inst->c_matrix[i][j] = max(inst->c_matrix[i - 1][j], inst->c_matrix[i][j - 1]) + inst->costs[permutation[i]][j];
        }
    }
    cost = 0;
    for (int i = 0; i < n_jobs; i++)
    {
        cost += inst->c_matrix[i][inst->n_machines - 1];
    }
    return cost;
}

void SRZ(int *permutation, Instance *inst){
    int *T;
    int *s_seq;
    int k;

    T = malloc(inst->n_jobs * sizeof(int));
    if (T == NULL)
    {
        printf("Error: Memory allocation failed\n");
        exit(1);
    }
    s_seq = malloc(inst->n_jobs * sizeof(int));
    if (s_seq == NULL)
    {
        printf("Error: Memory allocation failed\n");
        exit(1);
    }

    for (int i = 0; i < inst->n_jobs; i++)
    {
        s_seq[i] = -1;
    }
    for (int i = 0; i < inst->n_jobs; i++)
    {
        T[i] = 0;
        for (int j = 0; j < inst->n_machines; j++)
        {
            T[i] += inst->costs[i][j];
        }
    }

    //order the jobs in increasing order of T
    for (int i = 0; i < inst->n_jobs; i++)
    {
        k = 0;
        for (int j = 0; j < inst->n_jobs; j++)
        {
            if (T[i] > T[j])
                k++;
        }
        while (s_seq[k] != -1)
            k++;
        s_seq[k] = i;
    }
    //print init_seq
    // printf("init_seq: ");
    // for (int i = 0; i < inst->n_jobs; i++)
    // {
    //     printf("%d %d\t", s_seq[i] + 1, T[s_seq[i]]);
    // }
    // printf("\n");
    unsigned int best_cost = UINT_MAX;
    int *best_permutation = malloc(inst->n_jobs * sizeof(int));

    best_permutation[0] = s_seq[0];
    for (int i = 1; i < inst->n_jobs; i++)
    {
        //copy best_permutation to permutation
        for (int j = 0; j < i; j++)
            permutation[j + 1] = best_permutation[j];
        //insert s_seq[i] on firt position
        permutation[0] = s_seq[i];

        //print permutation
        // printf("permutation: ");
        // for (int j = 0; j <= i; j++)
        // {
        //     printf("%d ", permutation[j] + 1);
        // }
        // printf("\n");
    
        best_cost = UINT_MAX;
        
        //insert s_seq[i] in the best position
        for (int j = 0; j <= i; j++)
        {
           unsigned int cost = Complation_time(permutation, inst, i+ 1);
            if (cost < best_cost)
            {
                best_cost = cost;
                for (int k = 0; k <= i; k++)
                    best_permutation[k] = permutation[k];
            }

            //swap permutation[j + 1] and s_seq[i]
            if (j < i){
                permutation[j] = permutation[j + 1];
                permutation[j + 1] = s_seq[i];
            }
        }
    }
    free(T);
    free(best_permutation);
    free(s_seq);
    return;
}


// void    print_matrix(Instance *inst){
//     for (int i = 0; i < inst->n_machines; i++)
//     {
//         for (int j = 0; j < inst->n_jobs; j++)
//         {
//             printf("%d ", inst->costs[j][i]);
//         }
//         printf("\n");
//     }
// }
// int main(){

//     Instance *inst = malloc(sizeof(Instance));
//     Read_Instance("/home/thomvray/documents/ULB/Heuristic/instances/Benchmarks/ta103", inst);
//     int *permutation = malloc(inst->n_jobs * sizeof(int));
//     if (permutation == NULL)
//     {
//         printf("Error: Memory allocation failed\n");
//         exit(1);
//     }
//     for (int i = 0; i < inst->n_jobs; i++)
//     {
//         permutation[i] = i;
//     }
//     print_matrix(inst);
//     SRZ(permutation, inst);
//     printf("complation time %ld \n", Complation_time(permutation, inst, inst->n_jobs));
//     printf("permutation: ");
//     for (int i = 0; i < inst->n_jobs; i++)
//     {
//         printf("%d ", permutation[i] + 1);
//     }

// }