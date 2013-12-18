#ifndef H_LIST
#define H_LIST

#include <malloc.h>

struct node {
    long long value;
    struct node *next;
    struct node *previous;
};

struct list {
    int length;
    struct node *first;
    struct node *last;
};

struct node *list_find(struct list *list, long long value) {
    struct node *node = list->first;

    while (node && node->value != value) {
        node = node->next;
    }

    return node;
}

struct node *list_push(struct list *list, long long value) {
    struct node *new = (struct node *)calloc(1, sizeof(struct node));
    new->value = value;

    if (list->last) {
        new->previous = list->last;
        list->last->next = new;
        list->last = new;
    } else {
        list->first = list->last = new;
    }

    ++list->length;

    return list->last;
}

struct node *list_push_front(struct list *list, long long value) {
    struct node *new = (struct node *)calloc(1, sizeof(struct node));
    new->value = value;

    if (list->first) {
        new->next = list->first;
        list->first->previous = new;
        list->first = new;
    } else {
        list->first = list->last = new;
    }

    ++list->length;
    
    return list->first;
}

long long list_pop(struct list *list) {
    long long value = -1;

    if (list->last) {
        value = list->last->value;

        if (list->last->previous) {
            list->last = list->last->previous;
            free(list->last->next);
            list->last->next = 0;
        } else {
            free(list->last);
            list->first = list->last = 0;
        }

        --list->length;
    }

    return value;
}

long long list_pop_front(struct list *list) {
    long long value = -1;

    if (list->first) {
        value = list->first->value;

        if (list->first->next) {
            list->first = list->first->next;
            free(list->first->previous);
            list->first->previous = 0;
        } else {
            free(list->first);
            list->first = list->last = 0;
        }

        --list->length;
    }

    return value;
}

int list_remove(struct list *list, long long value) {
    struct node *node = list_find(list, value);
    int removed = 0;

    while (node) {
        if (node->previous && node->next) {
            node->previous->next = node->next;
            node->next->previous = node->previous;
        } else if (node->previous) {
            node->previous->next = 0;
            list->last = node->previous;
        } else if (node->next) {
            node->next->previous = 0;
            list->first = node->next;
        } else {
            list->first = list->last = 0;
        }

        free(node);
        --list->length;
        ++removed;
        
        node = list_find(list, value);
    }

    return removed;
}

struct list *list_new() {
    return (struct list *)calloc(1, sizeof(struct list));
}

struct list *list_copy(struct list *list) {
    struct list *copy = list_new();
    struct node *node = list->first;

    while (node) {
        list_push(copy, node->value);
        node = node->next;
    }

    return copy;
}

void list_clear(struct list *list) {
    while (list->length) {
        list_pop(list);
    }
}

void list_print(struct list *list) {
    struct node *node = list->first;
    printf("[");

    while (node) {
        printf("%lld%s ", node->value, (node->next ? "," : ""));
        node = node->next;
    }

    printf("]\n");
}

void list_free(struct list *list) {
    struct node *node;

    if (list) {
        node = list->last;

        if (node) {
            while (node->previous) {
                node = node->previous;
                free(node->next);
            }

            if (node) {
                free(node);
            }
        }

        free(list);
    }

    list = 0;
}

#endif
