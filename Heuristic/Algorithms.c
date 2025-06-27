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

#include <time.h>
void Initial_solution(int *permutation, Instance *inst, int *modes){
    //compute time of the function
    // __time_t start;
    // start = time(NULL);
    //random init
    if (modes[2] == 1){
        //create a random list of number from 1 to 10 with no doubles in mix order
        Random_init(inst, permutation);
    }
    //Use SRZ Heuristic
    else if (modes[2] == 2)
        SRZ(permutation, inst);
    else
        printf("Error: Invalid initial solution\n");
    
    // printf("time of initial solution: %ld\n", time(NULL) - start);
}

unsigned long int Complation_time(int *permutation, Instance *inst){
    unsigned long int cost = 0;

    inst->c_matrix[0][0] = inst->costs[permutation[0]][0];
    for (int i = 1; i < inst->n_machines; i++)
    {
        inst->c_matrix[0][i] = inst->costs[permutation[0]][i] + inst->c_matrix[0][i - 1];
    }
    for (int j = 1; j < inst->n_jobs; j++)
    {
        inst->c_matrix[j][0] = inst->costs[permutation[j]][0] + inst->c_matrix[j - 1][0];
    }
    for (int i = 1; i < inst->n_jobs; i++)
    {
        for (int j = 1; j < inst->n_machines; j++)
        {
            inst->c_matrix[i][j] = max(inst->c_matrix[i - 1][j], inst->c_matrix[i][j - 1]) + inst->costs[permutation[i]][j];
        }
    }
    cost = 0;
    for (int i = 0; i < inst->n_jobs; i++)
    {
        cost += inst->c_matrix[i][inst->n_machines - 1];
    }
    return cost;
}

unsigned long int update_Cmatrix(Instance *inst, int start, int *permutation){
    unsigned long int cost = 0;
    if (start <= 0)
        return Complation_time(permutation, inst);

    // inst->c_matrix[0][0] = inst->costs[permutation[0]][0];
    // for (int j = 1; j < inst->n_machines; j++)
    // {
    //     inst->c_matrix[0][j] = inst->costs[permutation[0]][j] + inst->c_matrix[0][j - 1];
    // }
    for (int i = start; i < inst->n_jobs; i++)
    {
        inst->c_matrix[i][0] = inst->costs[permutation[i]][0] + inst->c_matrix[i - 1][0];
    }
    for (int i = start; i < inst->n_jobs; i++)
    {
        for (int j = 1; j < inst->n_machines; j++)
        {
            inst->c_matrix[i][j] = max(inst->c_matrix[i - 1][j], inst->c_matrix[i][j - 1]) + inst->costs[permutation[i]][j];
        }
    }
    cost = 0;
    for (int i = 0; i < inst->n_jobs; i++)
    {
        cost += inst->c_matrix[i][inst->n_machines - 1];
    }
    // if (Complation_time(permutation, inst) != cost)
    // {
    //     printf("Error: update c_matrix failed\n");
    // }
    return cost;
}

unsigned long int transpose(int *permutation, Instance *inst, int *mode){
    unsigned long int best_cost;
    unsigned long int current_cost;
    unsigned long int new_cost;
    int improved = 1; // Flag to control the main loop
    int it = 0; // Iteration counter
    int n_jobs = inst->n_jobs;

    int *new_permutation = (int *) malloc(inst->n_jobs * sizeof(int));
    int *best_permutation = (int *) malloc(inst->n_jobs * sizeof(int));
    if (new_permutation == NULL || best_permutation == NULL) {
        printf("Error: malloc failed in transpose\n");
        if (new_permutation) free(new_permutation);
        if (best_permutation) free(best_permutation);
        return 0;
    }

    current_cost = Complation_time(permutation, inst);
    best_cost = current_cost;

    while (improved)
    {
        improved = 0;
        for (int i = inst->n_jobs - 2; i >= 0; i--)
        {
            // Create new permutation by swapping jobs i and i+1
            memcpy(new_permutation, permutation, n_jobs * sizeof(int));
            new_permutation[i] = permutation[i + 1];
            new_permutation[i + 1] = permutation[i];

            // Calculate cost of new permutation
            new_cost = update_Cmatrix(inst, i, new_permutation);

            // Check if this is an improvement
            if (new_cost < best_cost) {
                best_cost = new_cost;
                memcpy(best_permutation, new_permutation, n_jobs * sizeof(int));
                improved = 1;

                // If FIRST improvement mode, break immediately
                if (mode[0] == 1) {
                    break;
                }
            }
        }
        memcpy(permutation, best_permutation, n_jobs * sizeof(int));
    }
    free(new_permutation);
    free(best_permutation);
    return best_cost;
}


unsigned long int exchange(int *permutation, Instance *inst, int *modes){
    unsigned long int best_cost;
    unsigned long int current_cost;
    unsigned long int new_cost;
    int improved = 1;

    int *new_permutation = (int *) malloc(inst->n_jobs * sizeof(int));
    int *best_permutation = (int *) malloc(inst->n_jobs * sizeof(int));

    if (new_permutation == NULL || best_permutation == NULL) {
        printf("Error: malloc failed in exchange\n");
        if (new_permutation) free(new_permutation);
        if (best_permutation) free(best_permutation);
        return 0;
    }

    current_cost = Complation_time(permutation, inst);
    best_cost = current_cost;

    int it = 0;
    while(improved)
    {
        it++;
        improved = 0;
        
        for (int i = inst->n_jobs - 2; i >= 0; i--)
        {
            for (int j = i + 1; j < inst->n_jobs - 1; j++)
            {
                // Create new permutation by swapping jobs i and j
                memcpy(new_permutation, permutation, inst->n_jobs * sizeof(int));
                
                // Swap the jobs
                new_permutation[i] = permutation[j];
                new_permutation[j] = permutation[i];

                // Calculate cost of new permutation
                // new_cost = Complation_time(new_permutation, inst);
                new_cost = update_Cmatrix(inst, i, new_permutation);

                // Check if this is an improvement
                if (new_cost < best_cost) {
                    best_cost = new_cost;
                    memcpy(best_permutation, new_permutation, inst->n_jobs * sizeof(int));
                    improved = 1;
                    
                    // If FIRST improvement mode, break immediately
                    if (modes[0] == 1) {
                        goto found_improvement; // Break out of both loops
                    }
                    // If BEST improvement mode (modes[0] == 2), continue searching
                }
            }
        }
        found_improvement:
        if (improved) {
            // If we found an improvement, update the current permutation
            memcpy(permutation, best_permutation, inst->n_jobs * sizeof(int));
            current_cost = best_cost;
        }
    }
    free(best_permutation);
    free(new_permutation);
    printf("exchange iterations = %d\n", it);
    return current_cost;
}


unsigned long int insert(int *permutation, Instance *inst, int *modes){
    unsigned long int best_cost;
    unsigned long int current_cost;
    unsigned long int new_cost;
    int improved = 1; // Flag to control the main loop

    int *new_permutation = (int *) malloc(inst->n_jobs * sizeof(int));
    int *best_permutation = (int *) malloc(inst->n_jobs * sizeof(int));
    
    if (new_permutation == NULL || best_permutation == NULL) {
        printf("Error: malloc failed in insert\n");
        if (new_permutation) free(new_permutation);
        if (best_permutation) free(best_permutation);
        return 0;
    }

    current_cost = Complation_time(permutation, inst);
    int it = 0;

    // Main iterative loop - continues until no improvement is found
    while (improved) {
        it++;
        improved = 0;
        best_cost = current_cost;
        
        // Copy current permutation as the best so far
        memcpy(best_permutation, permutation, inst->n_jobs * sizeof(int));

        // Try all possible insert moves
        for (int i = inst->n_jobs - 1; i >= 0; i--) {
            Complation_time(permutation, inst); // reset the complation matrix
            for (int j = inst->n_jobs - 1; j >= 0; j--) {
                // Skip if i and j are the same (no move)
                if (i == j) continue;
                
                // Create new permutation by inserting job at position i to position j
                memcpy(new_permutation, permutation, inst->n_jobs * sizeof(int));
                
                int job_to_move = permutation[i];
                
                if (i < j) {
                    // Moving job forward: shift elements left
                    for (int k = i; k < j; k++) {
                        new_permutation[k] = permutation[k + 1];
                    }
                    new_permutation[j] = job_to_move;
                } else {
                    // Moving job backward: shift elements right
                    for (int k = i; k > j; k--) {
                        new_permutation[k] = permutation[k - 1];
                    }
                    new_permutation[j] = job_to_move;
                }

                // Calculate cost of new permutation
                new_cost = update_Cmatrix(inst, (i < j ? i : j), new_permutation);

                // Check if this is an improvement
                if (new_cost < best_cost) {
                    best_cost = new_cost;
                    memcpy(best_permutation, new_permutation, inst->n_jobs * sizeof(int));
                    improved = 1;
                    
                    // If FIRST improvement mode, break immediately
                    if (modes[0] == 1) {
                        goto found_improvement; // Break out of both loops
                    }
                    // If BEST improvement mode (modes[0] == 2), continue searching
                }
            }
        }
        
        found_improvement:
        // If we found an improvement, update the current permutation
        if (improved) {
            memcpy(permutation, best_permutation, inst->n_jobs * sizeof(int));
            current_cost = best_cost;
        }
    }

    free(new_permutation);
    free(best_permutation);
    printf("insert iterations = %d\n", it);
    printf("cost recalculated = %ld\n", Complation_time(permutation, inst));
    if (!check_permutation(permutation, inst->n_jobs)) {
        printf("Error: Invalid permutation after insert\n");
    }
    return current_cost;
}


unsigned long int VND1( Instance *inst, int *modes){
    unsigned long int cost;

    int* permutation = malloc(inst->n_jobs * sizeof(int));
    Initial_solution(permutation, inst, modes);

    modes[1] = 1;
    cost = transpose(permutation, inst, modes);
    // printf("cost after transpose = %ld\n", cost);
    modes[1] = 2;
    cost = exchange(permutation, inst, modes);
    // printf("cost after exchange = %ld\n", cost);
    modes[1] = 3;
    cost = insert(permutation, inst, modes);
    // printf("cost after insert = %ld\n", cost);
    free(permutation);
    return cost;

}

unsigned long int VND2( Instance *inst, int *modes){
    unsigned long int cost;

    int* permutation = malloc(inst->n_jobs * sizeof(int));
    Initial_solution(permutation, inst, modes);
    modes[1] = 1;
    cost = transpose(permutation, inst, modes);
    // printf("cost after transpose = %ld\n", cost);
    modes[1] = 3;
    cost = insert(permutation, inst, modes);
    // printf("cost after insert = %ld\n", cost);
    modes[1] = 2;
    cost = exchange(permutation, inst, modes);
    // printf("cost after exchange = %ld\n", cost);
    free(permutation);
    return cost;
}

unsigned long int  Iterative_algo(Instance *inst, int *modes){

    time_t start = clock();


    printf("Iterative_algo start\n");
    
    //create inital permutation
    int *permutation;
    permutation = (int *) malloc(inst->n_jobs * sizeof(int));
    if (permutation == NULL)
    {
        printf("Error: malloc failed\n");
        return 0;
    }

    Initial_solution(permutation, inst, modes);

    if (modes[1] == 1){
        
        transpose(permutation, inst, modes);
        // printf("time after transpose = %ld\n", time(NULL) - start);
    }
    else if (modes[1] == 2){
        exchange(permutation, inst, modes);
        // printf("time after exchange = %ld\n", time(NULL) - start);
    }
    else if (modes[1] == 3){
        insert(permutation, inst, modes);
        // printf("time after insert = %ld\n", time(NULL) - start);
    }
    else
        printf("Error: Invalid neighborhood rule\n");
    unsigned long int cost =  Complation_time(permutation, inst);
    printf("cost %li\n", cost);
    printf("time after Iterative_algo = %f\n",(double)  (clock() - start) / CLOCKS_PER_SEC);

    free(permutation);
    return cost;
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
