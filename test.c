#include <stdio.h>
#include <stdlib.h> 

int main() {
    char buff[] = "0123";
    char end[] = "0123";
    buff[4] = '4';
    buff[6] = 0;

    printf("%s\n", buff);
    printf("%s\n", end);
    exit(2);
    return 0;
}
