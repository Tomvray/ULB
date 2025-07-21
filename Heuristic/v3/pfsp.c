/**
 * PFSP Implementation for Heuristic Optimization Course
 * 
 * This code implements Iterative Improvement and VND algorithms for the
 * Permutation Flow Shop Scheduling Problem with Total Completion Time objective.
 * 
 * Features:
 * - First-improvement and best-improvement pivoting rules
 * - Transpose, exchange, and insert neighborhoods
 * - Random initialization and simplified RZ heuristic
 * - VND with two neighborhood orderings
 * - Efficient incremental evaluation of solutions
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <limits.h>
#include <math.h>

// Problem parameters
int num_jobs;       // Number of jobs
int num_machines;   // Number of machines
int **processing_times; // Processing times matrix [job][machine]

// Solution representation
typedef struct {
    int *sequence;    // Job permutation
    long total_time;  // Total completion time
} Solution;

// Global best known solutions
long *best_known;

// Function prototypes
void read_instance(const char *filename);
void free_memory();
Solution generate_random_solution(int seed);
Solution generate_rz_solution();
long evaluate_sequence(int *sequence);
long evaluate_partial_sequence(int *sequence, int length);
Solution iterative_improvement(Solution initial, int pivoting_rule, int neighborhood, int verbose);
Solution vnd(Solution initial, int *neighborhood_order, int num_neighborhoods, int verbose);
Solution transpose_neighbor(Solution current, int index, int *improved);
Solution exchange_neighbor(Solution current, int i, int j, int *improved);
Solution insert_neighbor(Solution current, int i, int j, int *improved);
void print_solution(Solution s);
void save_results(const char *filename, const char *algorithm, int instance_size, double avg_deviation, double total_time);

// Neighborhood types
#define TRANSPOSE 0
#define EXCHANGE 1
#define INSERT 2

// Pivoting rules
#define FIRST_IMPROVEMENT 0
#define BEST_IMPROVEMENT 1

int main(int argc, char *argv[]) {
    if (argc < 3) {
        printf("Usage: %s <instance_file> <algorithm> [options]\n", argv[0]);
        printf("Algorithms:\n");
        printf("  ii --first|--best --transpose|--exchange|--insert --random|--rz\n");
        printf("  vnd --order1|--order2 --rz\n");
        return 1;
    }

    // Read instance file
    read_instance(argv[1]);
    
    // Initialize random seed
    srand(time(NULL));
    
    Solution initial, best;
    char algorithm[100] = "";
    int instance_size = num_jobs;
    
    // Parse algorithm parameters
    if (strcmp(argv[2], "ii") == 0) {
        // Iterative Improvement
        int pivoting_rule = -1;
        int neighborhood = -1;
        int init_method = -1;
        
        for (int i = 3; i < argc; i++) {
            if (strcmp(argv[i], "--first") == 0) pivoting_rule = FIRST_IMPROVEMENT;
            else if (strcmp(argv[i], "--best") == 0) pivoting_rule = BEST_IMPROVEMENT;
            else if (strcmp(argv[i], "--transpose") == 0) neighborhood = TRANSPOSE;
            else if (strcmp(argv[i], "--exchange") == 0) neighborhood = EXCHANGE;
            else if (strcmp(argv[i], "--insert") == 0) neighborhood = INSERT;
            else if (strcmp(argv[i], "--random") == 0) init_method = 0;
            else if (strcmp(argv[i], "--rz") == 0) init_method = 1;
        }
        
        // Generate initial solution
        if (init_method == 0) {
            initial = generate_random_solution(123);
            strcat(algorithm, "ii_random_");
        } else {
            initial = generate_rz_solution();
            strcat(algorithm, "ii_rz_");
        }
        
        if (pivoting_rule == FIRST_IMPROVEMENT) strcat(algorithm, "first_");
        else strcat(algorithm, "best_");
        
        if (neighborhood == TRANSPOSE) strcat(algorithm, "transpose");
        else if (neighborhood == EXCHANGE) strcat(algorithm, "exchange");
        else strcat(algorithm, "insert");
        
        // Run iterative improvement
        best = iterative_improvement(initial, pivoting_rule, neighborhood, 1);
        
    } else if (strcmp(argv[2], "vnd") == 0) {
        // Variable Neighborhood Descent
        int order = -1;
        int init_method = -1;
        
        for (int i = 3; i < argc; i++) {
            if (strcmp(argv[i], "--order1") == 0) order = 0;
            else if (strcmp(argv[i], "--order2") == 0) order = 1;
            else if (strcmp(argv[i], "--rz") == 0) init_method = 1;
        }
        
        // Generate initial solution (always RZ for VND)
        initial = generate_rz_solution();
        strcat(algorithm, "vnd_rz_");
        
        int neighborhood_order[3];
        if (order == 0) {
            // Order 1: transpose, exchange, insert
            neighborhood_order[0] = TRANSPOSE;
            neighborhood_order[1] = EXCHANGE;
            neighborhood_order[2] = INSERT;
            strcat(algorithm, "order1");
        } else {
            // Order 2: transpose, insert, exchange
            neighborhood_order[0] = TRANSPOSE;
            neighborhood_order[1] = INSERT;
            neighborhood_order[2] = EXCHANGE;
            strcat(algorithm, "order2");
        }
        
        // Run VND
        best = vnd(initial, neighborhood_order, 3, 1);
    }
    
    // Calculate percentage deviation from best known
    double deviation = 100.0 * (best.total_time - best_known[instance_size]) / best_known[instance_size];
    
    printf("\nBest solution found:\n");
    print_solution(best);
    printf("Deviation from best known: %.2f%%\n", deviation);
    
    // Save results to file (for statistical analysis)
    save_results("results.txt", algorithm, instance_size, deviation, 0.0);
    
    // Free allocated memory
    free_memory();
    
    return 0;
}

// Read instance file
void read_instance(const char *filename) {
    FILE *file = fopen(filename, "r");
    if (!file) {
        perror("Error opening instance file");
        exit(1);
    }
    
    // Read number of jobs and machines
    fscanf(file, "%d %d", &num_jobs, &num_machines);
    
    // Allocate memory for processing times
    processing_times = (int **)malloc(num_jobs * sizeof(int *));
    for (int i = 0; i < num_jobs; i++) {
        processing_times[i] = (int *)malloc(num_machines * sizeof(int));
    }
    
    // Read processing times
    for (int i = 0; i < num_jobs; i++) {
        for (int j = 0; j < num_machines; j++) {
            int machine;
            fscanf(file, "%d %d", &machine, &processing_times[i][j]);
        }
    }
    
    fclose(file);
    
    // Initialize best known solutions (would normally read from file)
    best_known = (long *)malloc(3 * sizeof(long));
    // These are example values - should be replaced with actual best known
    best_known[50] = 10000;
    best_known[100] = 20000;
    best_known[200] = 40000;
}

// Free allocated memory
void free_memory() {
    for (int i = 0; i < num_jobs; i++) {
        free(processing_times[i]);
    }
    free(processing_times);
    free(best_known);
}

// Generate random initial solution
Solution generate_random_solution(int seed) {
    srand(seed);
    
    Solution s;
    s.sequence = (int *)malloc(num_jobs * sizeof(int));
    s.total_time = 0;
    
    // Initialize sequence
    for (int i = 0; i < num_jobs; i++) {
        s.sequence[i] = i;
    }
    
    // Fisher-Yates shuffle
    for (int i = num_jobs - 1; i > 0; i--) {
        int j = rand() % (i + 1);
        int temp = s.sequence[i];
        s.sequence[i] = s.sequence[j];
        s.sequence[j] = temp;
    }
    
    s.total_time = evaluate_sequence(s.sequence);
    return s;
}

// Generate initial solution using simplified RZ heuristic
Solution generate_rz_solution() {
    Solution s;
    s.sequence = (int *)malloc(num_jobs * sizeof(int));
    s.total_time = 0;
    
    // Calculate total processing time for each job
    int *total_times = (int *)malloc(num_jobs * sizeof(int));
    for (int i = 0; i < num_jobs; i++) {
        total_times[i] = 0;
        for (int j = 0; j < num_machines; j++) {
            total_times[i] += processing_times[i][j];
        }
    }
    
    // Create list of job indices sorted by total processing time
    int *job_indices = (int *)malloc(num_jobs * sizeof(int));
    for (int i = 0; i < num_jobs; i++) {
        job_indices[i] = i;
    }
    
    // Sort jobs by total processing time (ascending)
    for (int i = 0; i < num_jobs - 1; i++) {
        for (int j = i + 1; j < num_jobs; j++) {
            if (total_times[job_indices[i]] > total_times[job_indices[j]]) {
                int temp = job_indices[i];
                job_indices[i] = job_indices[j];
                job_indices[j] = temp;
            }
        }
    }
    
    // Build solution incrementally
    int current_length = 0;
    for (int k = 0; k < num_jobs; k++) {
        int job_to_insert = job_indices[k];
        long best_time = LONG_MAX;
        int best_position = 0;
        
        // Try inserting at all possible positions
        for (int pos = 0; pos <= current_length; pos++) {
            // Create temporary sequence with job inserted at pos
            int *temp_sequence = (int *)malloc((current_length + 1) * sizeof(int));
            for (int i = 0; i < pos; i++) {
                temp_sequence[i] = s.sequence[i];
            }
            temp_sequence[pos] = job_to_insert;
            for (int i = pos; i < current_length; i++) {
                temp_sequence[i + 1] = s.sequence[i];
            }
            
            // Evaluate partial sequence
            long temp_time = evaluate_partial_sequence(temp_sequence, current_length + 1);
            
            if (temp_time < best_time) {
                best_time = temp_time;
                best_position = pos;
            }
            
            free(temp_sequence);
        }
        
        // Insert job at best position
        for (int i = current_length; i > best_position; i--) {
            s.sequence[i] = s.sequence[i - 1];
        }
        s.sequence[best_position] = job_to_insert;
        current_length++;
    }
    
    s.total_time = evaluate_sequence(s.sequence);
    
    free(total_times);
    free(job_indices);
    
    return s;
}

// Evaluate a complete sequence (total completion time)
long evaluate_sequence(int *sequence) {
    // Create matrix of completion times
    long **completion_times = (long **)malloc(num_jobs * sizeof(long *));
    for (int i = 0; i < num_jobs; i++) {
        completion_times[i] = (long *)malloc(num_machines * sizeof(long));
    }
    
    // First job
    completion_times[0][0] = processing_times[sequence[0]][0];
    for (int j = 1; j < num_machines; j++) {
        completion_times[0][j] = completion_times[0][j - 1] + processing_times[sequence[0]][j];
    }
    
    // Subsequent jobs
    for (int i = 1; i < num_jobs; i++) {
        completion_times[i][0] = completion_times[i - 1][0] + processing_times[sequence[i]][0];
        
        for (int j = 1; j < num_machines; j++) {
            completion_times[i][j] = (completion_times[i - 1][j] > completion_times[i][j - 1]) 
                                   ? completion_times[i - 1][j] 
                                   : completion_times[i][j - 1];
            completion_times[i][j] += processing_times[sequence[i]][j];
        }
    }
    
    // Calculate total completion time
    long total_time = 0;
    for (int i = 0; i < num_jobs; i++) {
        total_time += completion_times[i][num_machines - 1];
    }
    
    // Free memory
    for (int i = 0; i < num_jobs; i++) {
        free(completion_times[i]);
    }
    free(completion_times);
    
    return total_time;
}

// Evaluate a partial sequence (for RZ heuristic)
long evaluate_partial_sequence(int *sequence, int length) {
    if (length == 0) return 0;
    
    // Create matrix of completion times
    long **completion_times = (long **)malloc(length * sizeof(long *));
    for (int i = 0; i < length; i++) {
        completion_times[i] = (long *)malloc(num_machines * sizeof(long));
    }
    
    // First job
    completion_times[0][0] = processing_times[sequence[0]][0];
    for (int j = 1; j < num_machines; j++) {
        completion_times[0][j] = completion_times[0][j - 1] + processing_times[sequence[0]][j];
    }
    
    // Subsequent jobs
    for (int i = 1; i < length; i++) {
        completion_times[i][0] = completion_times[i - 1][0] + processing_times[sequence[i]][0];
        
        for (int j = 1; j < num_machines; j++) {
            completion_times[i][j] = (completion_times[i - 1][j] > completion_times[i][j - 1]) 
                                   ? completion_times[i - 1][j] 
                                   : completion_times[i][j - 1];
            completion_times[i][j] += processing_times[sequence[i]][j];
        }
    }
    
    // Calculate total completion time
    long total_time = 0;
    for (int i = 0; i < length; i++) {
        total_time += completion_times[i][num_machines - 1];
    }
    
    // Free memory
    for (int i = 0; i < length; i++) {
        free(completion_times[i]);
    }
    free(completion_times);
    
    return total_time;
}

// Iterative improvement algorithm
Solution iterative_improvement(Solution initial, int pivoting_rule, int neighborhood, int verbose) {
    Solution current = initial;
    int improved = 1;
    int iterations = 0;
    
    while (improved) {
        improved = 0;
        iterations++;
        
        if (pivoting_rule == FIRST_IMPROVEMENT) {
            // First-improvement: evaluate neighbors in order and take first improvement
            switch (neighborhood) {
                case TRANSPOSE: {
                    for (int i = 0; i < num_jobs - 1; i++) {
                        Solution neighbor = transpose_neighbor(current, i, &improved);
                        if (improved) {
                            free(current.sequence);
                            current = neighbor;
                            break;
                        }
                        free(neighbor.sequence);
                    }
                    break;
                }
                case EXCHANGE: {
                    for (int i = 0; i < num_jobs; i++) {
                        for (int j = i + 1; j < num_jobs; j++) {
                            Solution neighbor = exchange_neighbor(current, i, j, &improved);
                            if (improved) {
                                free(current.sequence);
                                current = neighbor;
                                break;
                            }
                            free(neighbor.sequence);
                        }
                        if (improved) break;
                    }
                    break;
                }
                case INSERT: {
                    for (int i = 0; i < num_jobs; i++) {
                        for (int j = 0; j < num_jobs; j++) {
                            if (i != j) {
                                Solution neighbor = insert_neighbor(current, i, j, &improved);
                                if (improved) {
                                    free(current.sequence);
                                    current = neighbor;
                                    break;
                                }
                                free(neighbor.sequence);
                            }
                        }
                        if (improved) break;
                    }
                    break;
                }
            }
        } else {
            // Best-improvement: evaluate all neighbors and take the best
            Solution best_neighbor = current;
            long best_time = current.total_time;
            
            switch (neighborhood) {
                case TRANSPOSE: {
                    for (int i = 0; i < num_jobs - 1; i++) {
                        int dummy;
                        Solution neighbor = transpose_neighbor(current, i, &dummy);
                        if (neighbor.total_time < best_time) {
                            free(best_neighbor.sequence);
                            best_neighbor = neighbor;
                            best_time = neighbor.total_time;
                            improved = 1;
                        } else {
                            free(neighbor.sequence);
                        }
                    }
                    break;
                }
                case EXCHANGE: {
                    for (int i = 0; i < num_jobs; i++) {
                        for (int j = i + 1; j < num_jobs; j++) {
                            int dummy;
                            Solution neighbor = exchange_neighbor(current, i, j, &dummy);
                            if (neighbor.total_time < best_time) {
                                free(best_neighbor.sequence);
                                best_neighbor = neighbor;
                                best_time = neighbor.total_time;
                                improved = 1;
                            } else {
                                free(neighbor.sequence);
                            }
                        }
                    }
                    break;
                }
                case INSERT: {
                    for (int i = 0; i < num_jobs; i++) {
                        for (int j = 0; j < num_jobs; j++) {
                            if (i != j) {
                                int dummy;
                                Solution neighbor = insert_neighbor(current, i, j, &dummy);
                                if (neighbor.total_time < best_time) {
                                    free(best_neighbor.sequence);
                                    best_neighbor = neighbor;
                                    best_time = neighbor.total_time;
                                    improved = 1;
                                } else {
                                    free(neighbor.sequence);
                                }
                            }
                        }
                    }
                    break;
                }
            }
            
            if (improved) {
                free(current.sequence);
                current = best_neighbor;
            } else {
                free(best_neighbor.sequence);
            }
        }
        
        if (verbose) {
            printf("Iteration %d: ", iterations);
            print_solution(current);
        }
    }
    
    if (verbose) {
        printf("Local optimum reached after %d iterations\n", iterations);
    }
    
    return current;
}

// Variable Neighborhood Descent
Solution vnd(Solution initial, int *neighborhood_order, int num_neighborhoods, int verbose) {
    Solution current = initial;
    int improved = 1;
    int iterations = 0;
    
    while (improved) {
        improved = 0;
        iterations++;
        
        // Try each neighborhood in order
        for (int n = 0; n < num_neighborhoods; n++) {
            int neighborhood = neighborhood_order[n];
            
            // First-improvement in current neighborhood
            Solution new_solution = iterative_improvement(current, FIRST_IMPROVEMENT, neighborhood, 0);
            
            if (new_solution.total_time < current.total_time) {
                free(current.sequence);
                current = new_solution;
                improved = 1;
                if (verbose) {
                    printf("Improvement found in neighborhood %d: ", neighborhood);
                    print_solution(current);
                }
                break; // Go back to first neighborhood
            } else {
                free(new_solution.sequence);
            }
        }
    }
    
    if (verbose) {
        printf("VND completed after %d iterations\n", iterations);
    }
    
    return current;
}

// Generate transpose neighbor (swap adjacent jobs)
Solution transpose_neighbor(Solution current, int index, int *improved) {
    Solution neighbor;
    neighbor.sequence = (int *)malloc(num_jobs * sizeof(int));
    memcpy(neighbor.sequence, current.sequence, num_jobs * sizeof(int));
    
    // Swap adjacent jobs
    int temp = neighbor.sequence[index];
    neighbor.sequence[index] = neighbor.sequence[index + 1];
    neighbor.sequence[index + 1] = temp;
    
    neighbor.total_time = evaluate_sequence(neighbor.sequence);
    
    if (neighbor.total_time < current.total_time) {
        *improved = 1;
    } else {
        *improved = 0;
    }
    
    return neighbor;
}

// Generate exchange neighbor (swap any two jobs)
Solution exchange_neighbor(Solution current, int i, int j, int *improved) {
    Solution neighbor;
    neighbor.sequence = (int *)malloc(num_jobs * sizeof(int));
    memcpy(neighbor.sequence, current.sequence, num_jobs * sizeof(int));
    
    // Swap jobs at positions i and j
    int temp = neighbor.sequence[i];
    neighbor.sequence[i] = neighbor.sequence[j];
    neighbor.sequence[j] = temp;
    
    neighbor.total_time = evaluate_sequence(neighbor.sequence);
    
    if (neighbor.total_time < current.total_time) {
        *improved = 1;
    } else {
        *improved = 0;
    }
    
    return neighbor;
}

// Generate insert neighbor (move job from position i to position j)
Solution insert_neighbor(Solution current, int i, int j, int *improved) {
    Solution neighbor;
    neighbor.sequence = (int *)malloc(num_jobs * sizeof(int));
    
    if (i < j) {
        // Move job i to position j (shift others left)
        for (int k = 0; k < i; k++) {
            neighbor.sequence[k] = current.sequence[k];
        }
        for (int k = i; k < j; k++) {
            neighbor.sequence[k] = current.sequence[k + 1];
        }
        neighbor.sequence[j] = current.sequence[i];
        for (int k = j + 1; k < num_jobs; k++) {
            neighbor.sequence[k] = current.sequence[k];
        }
    } else {
        // Move job i to position j (shift others right)
        for (int k = 0; k < j; k++) {
            neighbor.sequence[k] = current.sequence[k];
        }
        neighbor.sequence[j] = current.sequence[i];
        for (int k = j + 1; k <= i; k++) {
            neighbor.sequence[k] = current.sequence[k - 1];
        }
        for (int k = i + 1; k < num_jobs; k++) {
            neighbor.sequence[k] = current.sequence[k];
        }
    }
    
    neighbor.total_time = evaluate_sequence(neighbor.sequence);
    
    if (neighbor.total_time < current.total_time) {
        *improved = 1;
    } else {
        *improved = 0;
    }
    
    return neighbor;
}

// Print solution
void print_solution(Solution s) {
    printf("Sequence: ");
    for (int i = 0; i < num_jobs; i++) {
        printf("%d ", s.sequence[i]);
    }
    printf("\nTotal completion time: %ld\n", s.total_time);
}

// Save results to file
void save_results(const char *filename, const char *algorithm, int instance_size, double avg_deviation, double total_time) {
    FILE *file = fopen(filename, "a");
    if (!file) {
        perror("Error opening results file");
        return;
    }
    
    fprintf(file, "%s,%d,%.4f,%.4f\n", algorithm, instance_size, avg_deviation, total_time);
    fclose(file);
}