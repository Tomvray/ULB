#include <stdio.h> 
#include <stdlib.h>
void function(){
    char    buf[] = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    printf("%p\n", buf);
}

int main(){
    char    buf[1024];
    printf("%p\n", buf);

    buf[0]  = '1';
    buf[2]  = '2';
    buf[3]  = 0;
    
    printf("%s\n", buf);
    return  0;
}