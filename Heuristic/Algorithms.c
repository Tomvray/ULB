
#include <stdio.h>
#include <string.h>
#include "Utils.h"
#include <stdlib.h>

int check_permutation(int *permutation, int n){
    for (int i = 0; i < n; i++)
    {
        int in = 0;
        for (int j = 0; j < n; j++)
        {
            if (permutation[j] == i)
                in = 1;
        }
        if (in == 0){
            printf("Error: Invalid permutation\n");
            return 0;
        }
    }
    return 1;
}

int is_inlist(int *list, int n, int size){
    for (int i = 0; i < size; i++)
    {
        if (list[i] == n)
            return 1;
    }
    return 0;
}

unsigned long int max(unsigned long int a, unsigned long int b){
    if (a > b)
        return a;
    return b;
}

void Random_init(Instance *inst, int *permutation){
    /*Create a random permutation of the jobs*/

    int random_number;

    for (int i = 0; i < inst->n_jobs; i++)
    {
        random_number = rand() % inst->n_jobs;
        while (is_inlist(permutation, random_number, i))
        {
            random_number = rand() % inst->n_jobs;
        }
        permutation[i] = random_number;
    }
}

void Initial_solution(int *permutation, Instance *inst, int *modes){
    //random init
    if (modes[2] == 1){
        //create a random list of number from 1 to 10 with no doubles in mix order
        Random_init(inst, permutation);
    }
    else if (modes[2] == 2)
        //Use SRZ Heuristic
        //TODO
        Random_init(inst, permutation);
        ;
}


unsigned long int Complation_time(int *permutation, Instance *inst){
    unsigned long int cost = 0;
    unsigned long int **c_matrix = malloc(inst->n_jobs * sizeof(unsigned long int *));

    for (int i = 0; i < inst->n_jobs; i++)
    {
        c_matrix[i] = malloc(inst->n_machines * sizeof(unsigned long int));
    }

    c_matrix[permutation[0]][0] = inst->costs[permutation[0]][0];
    for (int i = 1; i < inst->n_machines; i++)
    {
        c_matrix[permutation[0]][i] = inst->costs[permutation[0]][i] + c_matrix[permutation[0]][i - 1];
        // c_matrix[0][i] = 0;
        // for (int j = 0; j < i; j++)
        // {
        //     c_matrix[0][i] += inst->costs[permutation[0]][j];
        // }
    }
    for (int j = 1; j < inst->n_jobs; j++)
    {
        c_matrix[permutation[j]][0] = inst->costs[permutation[j]][0] + c_matrix[permutation[j - 1]][0];
        // c_matrix[j][0] = 0;
        // for (int i = 0; i < j; i++)
        // {
        //     c_matrix[j][0] += inst->costs[permutation[i]][0];
        // }
    }
    for (int i = 1; i < inst->n_jobs; i++)
    {
        for (int j = 1; j < inst->n_machines; j++)
        {
            c_matrix[permutation[i]][j] = max(c_matrix[permutation[i - 1]][j], c_matrix[permutation[i]][j - 1]) + inst->costs[permutation[i]][j];
        }
    }
    cost = 0;
    for (int i = 0; i < inst->n_jobs; i++)
    {
        cost += c_matrix[permutation[i]][inst->n_machines - 1];
    }
    // printf("Completion time matrix\n");
    // for (int i = 0; i < inst->n_jobs; i++)
    // {
    //     for (int j = 0; j < inst->n_machines; j++)
    //     {
    //         printf("%lu\t", c_matrix[i][j]);
    //     }
    //     printf("\n");
    // }
    return cost;
    for (int i = 0; i < inst->n_jobs; i++)
    {
        free(c_matrix[i]);
    }
    free(c_matrix);
    return cost;
}


unsigned long int transpose(int *permutation, Instance *inst, int *modes){
    unsigned long int best_cost;
    unsigned long int current_cost;
    unsigned long int new_cost;

    int *new_permutation = (int *) malloc(inst->n_jobs * sizeof(int));
    int *best_permutation = (int *) malloc(inst->n_jobs * sizeof(int));
    static int count = 0;
    count++;
    printf("count = %d\n", count);

    best_cost = Complation_time(permutation, inst);
    current_cost = best_cost;
    
    // for (int i = 0; i < inst->n_jobs; i++){
    //     printf("permutation[%d] = %d\n", i, permutation[i]);
    // }

    printf("best_cost = %lu\n", best_cost);

    for (int i = 0; i < inst->n_jobs - 1; i++)
    {
        // printf("i = %d\n", i);
        // for (int i = 0; i < inst->n_jobs; i++)
        // {
        //     printf("permutation[%d] = %d\n", i, permutation[i]);
        // }

        new_permutation = memcpy(new_permutation, permutation, inst->n_jobs * sizeof(int));
    
        check_permutation(new_permutation, inst->n_jobs);
        check_permutation(permutation, inst->n_jobs);
    
        for (int i = 0; i < inst->n_jobs; i++)
        {
            if (new_permutation[i] != permutation[i])
            {
                printf("Error: memcpy failed\n");
            }
        }
        new_permutation[i] = permutation[i + 1];
        new_permutation[i + 1] = permutation[i];
        int test = Complation_time(permutation, inst);
        new_cost = Complation_time(new_permutation, inst);
        current_cost = Complation_time(permutation, inst);

        if (current_cost != test)
        {
            printf("Error: Complation time failed\n");
        }

        if (new_cost < current_cost)
        {
            best_cost = new_cost;
            best_permutation = memcpy(best_permutation, new_permutation, inst->n_jobs * sizeof(int));
            if (modes[0] == 1)
            {
                printf("index = %d\n", i);
                permutation = memcpy(permutation, best_permutation, inst->n_jobs * sizeof(int));
                free(new_permutation);
                free(best_permutation);
                return (transpose(permutation, inst, modes));
            }
        }
    }
    if (best_cost < current_cost)
        {
            permutation = memcpy(permutation, best_permutation, inst->n_jobs * sizeof(int));
        }
        free(new_permutation);
        free(best_permutation);
        if (best_cost < current_cost)
        {
            return (transpose(permutation, inst, modes));
        }
        else{
            printf("best_cost = %lu\n", best_cost);
            printf("best cost from cost = %lu\n", Complation_time(permutation, inst));
            return current_cost;
        }

        
}	


void Iterative_algo(Instance *inst, int *modes){
    
    //create inital permutation
    int *permutation = (int *) malloc(inst->n_jobs * sizeof(int));

    Initial_solution(permutation, inst, modes);

    if (modes[1] == 1){
        printf("best_cost = %ld\n", transpose(permutation, inst, modes));
    }
    else if (modes[1] == 2){
        //exchange(inst, permutation, modes);
    }
    else if (modes[1] == 3){
        // insert(inst, permutation, modes);
    }
    else
        printf("Error: Invalid neighborhood\n");
    free(permutation);
}


// int main(){
//     Instance *inst = (Instance *) malloc(sizeof(Instance));
//     inst->n_jobs = 3;
//     inst->n_machines = 5;
//     inst->costs = (int **)malloc(inst->n_jobs * sizeof(int *));

//     int job3[] = {3, 3, 4, 2, 3};
//     int job2[] = {2, 1, 3, 3, 1};
//     int job1[] = {4, 2, 1, 2, 3};
//     inst->costs[0] = job1;
//     inst->costs[1] = job2;
//     inst->costs[2] = job3;

//     int permutation[] = {2, 1, 0};

//     //print matrix
//     printf("Matrix \n");
//     for (int i = 0; i < inst->n_jobs; i++)
//     {
//         for (int j = 0; j < inst->n_machines; j++)
//         {
//             printf("%d ", inst->costs[i][j]);
//         }
//         printf("\n");
//     }
//     printf("Cost: %ld\n", Complation_time(permutation, inst));
//     printf("Cost: %ld\n", Complation_time(permutation, inst));
//     return 0;
// }
