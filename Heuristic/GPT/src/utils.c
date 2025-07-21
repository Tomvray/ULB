#include <stdlib.h>
#include <string.h>
#include "utils.h"

void shuffle(int *array, int n) {
    for (int i = n - 1; i > 0; i--) {
        int j = rand() % (i + 1);
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }
}

int* generate_random_permutation(int n) {
    int *perm = malloc(n * sizeof(int));
    for (int i = 0; i < n; i++) perm[i] = i;
    shuffle(perm, n);
    return perm;
}

int* copy_permutation(int *perm, int n) {
    int *copy = malloc(n * sizeof(int));
    memcpy(copy, perm, n * sizeof(int));
    return copy;
}

void swap(int *perm, int i, int j) {
    int tmp = perm[i];
    perm[i] = perm[j];
    perm[j] = tmp;
}
