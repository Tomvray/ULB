#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int NB_ITER = 1;

int *parse_command_line(int argc, char *argv[]) {
    int *modes;

    modes = (int *)malloc(3 * sizeof(int));
    if (argc != 5)
    {
        printf("Error: Invalid number of arguments\n");
        return NULL;
    }
    //pivoting rule
    if (!strcmp(argv[1], "--first")){
        modes[0] = 1;
        printf("first\t");
    }
    else if (!strcmp(argv[1], "--best"))
    {
        modes[0] = 2;
        printf("best\t");
    }
    else
        printf("Error: Invalid pivoting rule\n");
    
    //neighborhood
    if (!strcmp(argv[2], "--transpose")){
        modes[1] = 1;
        printf("transpose\t");
    }
    else if (!strcmp(argv[2], "--exchange")){
        modes[1] = 2;
        printf("exchange\t");
    }
    else if (!strcmp(argv[2], "--insert")){
        modes[1] = 3;
        printf("insert\t");
    }
    else
        printf("Error: Invalid neighborhood\n");
    
    //initial solution
    if (!strcmp(argv[3],"--random-init"))
    {
        modes[2] = 1;
        printf("random\t\n");
    }
    else if (!strcmp(argv[3], "--srz"))
    {
        modes[2] = 2;
        printf("SRZ\t\n");
    }
    else
        printf("Error: Invalid initial solution\n");
    return modes;
}

#include <stdio.h>
#include <stdlib.h>
#include <dirent.h>
#include <string.h>
#include <time.h>
#include "algorithm.h"

void write_to_csv(const char *filename, const char *algorithm, int mode1, int mode2, int mode3, unsigned long int cost, long time) {
    FILE *file = fopen(filename, "a"); // Open in append mode
    if (file == NULL) {
        printf("Error: Could not open file %s for writing\n", filename);
        return;
    }

    // Write the header if the file is empty
    fseek(file, 0, SEEK_END);
    if (ftell(file) == 0) {
        fprintf(file, "Algorithm,Mode1,Mode2,Mode3,Cost,Time\n");
    }

    // Write the data
    fprintf(file, "%s,%d,%d,%d,%lu,%ld\n", algorithm, mode1, mode2, mode3, cost, time);
    fclose(file);
}

void test_all(Instance *inst, const char *instance_name) {
    __time_t start;
    int *modes = malloc(3 * sizeof(int));
    unsigned long int total_cost;
    char csv_filename[512];

    // Generate a unique CSV filename for the instance
    snprintf(csv_filename, sizeof(csv_filename), "%s_results.csv", instance_name);

    // Test Iterative_algo
    for (int i = 1; i <= 2; i++) {
        for (int j = 1; j <= 3; j++) {
            for (int k = 1; k <= 2; k++) {
                modes[0] = i;
                modes[1] = j;
                modes[2] = k;
                start = time(NULL);
                total_cost = 0;
                for (int iter = 0; iter < NB_ITER; iter++) {
                    total_cost += Iterative_algo(inst, modes);
                }
                long elapsed_time = time(NULL) - start;
                unsigned long int avg_cost = total_cost / NB_ITER;

                printf("Iterative_algo mode %i %i %i\tcost %lu\ttime %ld\n", modes[0], modes[1], modes[2], avg_cost, elapsed_time);
                write_to_csv(csv_filename, "Iterative_algo", modes[0], modes[1], modes[2], avg_cost, elapsed_time);
            }
        }
    }

    // Test VND1
    for (int k = 1; k <= 2; k++) {
        for (int l = 1; l <= 2; l++) {
            modes[0] = k;
            modes[2] = l;
            start = time(NULL);
            total_cost = 0;
            for (int j = 0; j < NB_ITER; j++) {
                total_cost += VND1(inst, modes);
            }
            long elapsed_time = time(NULL) - start;
            unsigned long int avg_cost = total_cost / NB_ITER;

            printf("VND1 mode %i %i\tcost %lu\ttime %ld\n", modes[0], modes[2], avg_cost, elapsed_time);
            write_to_csv(csv_filename, "VND1", modes[0], 0, modes[2], avg_cost, elapsed_time);
        }
    }

    // Test VND2
    for (int k = 1; k <= 2; k++) {
        for (int l = 1; l <= 2; l++) {
            modes[0] = k;
            modes[2] = l;
            start = time(NULL);
            total_cost = 0;
            for (int j = 0; j < NB_ITER; j++) {
                total_cost += VND2(inst, modes);
            }
            long elapsed_time = time(NULL) - start;
            unsigned long int avg_cost = total_cost / NB_ITER;

            printf("VND2 mode %i %i\tcost %lu\ttime %ld\n", modes[0], modes[2], avg_cost, elapsed_time);
            write_to_csv(csv_filename, "VND2", modes[0], 0, modes[2], avg_cost, elapsed_time);
        }
    }

    free(modes);
}
void read_files_from_directory(const char *dirname, int *modes) {
    Instance *inst;
    struct dirent *entry;
    DIR *dir = opendir(dirname);
    int i = 0;

    if (!dir) {
        printf("Error: Could not open directory %s\n", dirname);
        return;
    }

    char filepath[512];
    while ((entry = readdir(dir)) != NULL) {
        if (entry->d_type == DT_REG) {  // Only process regular files
            snprintf(filepath, sizeof(filepath), "%s/%s", dirname, entry->d_name);
            i++;
            printf("\nReading file: %s, it = %i\n", filepath, i); 
            inst = (Instance *) malloc(sizeof(Instance));
            if (inst == NULL) {
                printf("Error: Memory allocation failed\n");
                closedir(dir);
                return;
            }
            Read_Instance(filepath, inst);

            // Pass the instance name (without path) to test_all
            test_all(inst, entry->d_name);

            free_Instance(inst);
        }
    }

    closedir(dir);
}

void test_1(int *modes)
{
    Instance *inst = (Instance *) malloc(sizeof(Instance));
    Read_Instance("instances/Benchmarks/ta051", inst);
    Iterative_algo(inst, modes);
    free_Instance(inst);
    free(modes);
}


int main(int argc, char *argv[])
{
    char *seed_env = getenv("RANDOM_SEED");
    if (seed_env != NULL) {
        srand(atoi(seed_env));
    } else {
        // Use current time as seed if no environment variable
        srand(time(NULL));
    }

    int *modes = parse_command_line(argc, argv);
    //  test_1(modes);
    //  return 0;
    printf("file : %s\n", argv[4]);
    Instance *inst = (Instance *) malloc(sizeof(Instance));
    Read_Instance(argv[4], inst);
    Iterative_algo(inst, modes);
    //read_files_from_directory("instances/Benchmarks", modes);
    free_Instance(inst);
    free(modes);
    return 0;
}
