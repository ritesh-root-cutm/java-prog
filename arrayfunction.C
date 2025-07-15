#include <stdio.h>
#include <stdlib.h>
#include <time.h>

void display(int a[], int n) {
    for(int i = 0; i < n; i++) printf("%d ", a[i]);
    printf("\n");
}

int main() {
    int a[100], n, op, pos, item, s, i;
    printf("Enter number of elements: ");
    scanf("%d", &n);
    printf("Enter array elements: ");
    for(i = 0; i < n; i++) scanf("%d", &a[i]);
    do {
        printf("\n1-Display 2-Insert 3-Delete 4-Search 5-Sort 6-Exit\n");
        scanf("%d", &op);
        if(op == 1) display(a, n);
        else if(op == 2) {
            printf("Item & pos: ");
            scanf("%d%d", &item, &pos);
            for(i = n; i > pos; i--) a[i] = a[i-1];
            a[pos] = item; n++;
            display(a, n);
        } else if(op == 3) {
            printf("Pos: ");
            scanf("%d", &pos);
            for(i = pos; i < n-1; i++) a[i] = a[i+1];
            n--;
            display(a, n);
        } else if(op == 4) {
            printf("Search: ");
            scanf("%d", &s);
            for(i = 0; i < n; i++) if(a[i] == s) break;
            if(i < n) printf("Found at %d\n", i+1);
            else printf("Not found\n");
        } else if(op == 5) {
            for(int j = 0; j < n-1; j++)
                for(i = 0; i < n-j-1; i++)
                    if(a[i] > a[i+1]) { int t = a[i]; a[i] = a[i+1]; a[i+1] = t; }
            display(a, n);
        }
    } while(op != 6);

    long long sum = 0;
    int N;
    clock_t start, end;
    double cpu_time_used;
    printf("Enter N: ");
    scanf("%d", &N);
    start = clock();
    for(int i = 1; i <= N; i++) sum += i;
    end = clock();
    cpu_time_used = ((double) (end - start)) / CLOCKS_PER_SEC;
    printf("Sum = %lld\n", sum);
    printf("Time taken = %f seconds\n", cpu_time_used);

    return 0;
}
