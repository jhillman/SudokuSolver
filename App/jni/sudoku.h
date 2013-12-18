#include "list.h"

static int guessCalls = 0;

void print_sudoku(struct list *solution[9][9]) {
    int row;
    int subRow;
    int column;
    struct node *number;
    int i;

    for (row = 0; row < 9; row++) {
        for (i = 0; i < 37; i++) {
            printf("-");
        }

        printf("\n");

        for (subRow = 0; subRow < 9; subRow += 3) {
            printf("|");

            for (column = 0; column < 9; column++) {
                number = solution[row][column]->first;

                for (i = 0; number && i < subRow; i++) {
                    number = number->next;
                }

                for (i = 0; i < 3; i++) {
                    if (number) {
                        printf("%lld", number->value);
                        number = number->next;
                    } else {
                        printf(" ");
                    }
                }

                printf("|");
            }

            printf("\n");
        }
    }

    for (i = 0; i < 37; i++) {
        printf("-");
    }

    printf("\n\n");
}

int trim_sudoku(struct list *solution[9][9]) {
    int row;
    int column;
    long long number;
    int trimRow;
    int trimColumn;
    int zoneRow = 0;
    int zoneColumn = 0;
    int trimmed;
    int totalTrimmed = 0;

    printf("Trying trim");

    do {
        trimmed = 0;

        for (row = 0; row < 9; row++) {
            if (row % 3 == 0) {
                zoneRow = row;
            }

            for (column = 0; column < 9; column++) {
                if (column % 3 == 0) {
                    zoneColumn = column;
                }

                if (solution[row][column]->length == 1) {
                    number = solution[row][column]->first->value;

                    /* row */
                    for (trimRow = 0; trimRow < 9; trimRow++) {
                        if (trimRow != row) {
                            if (list_find(solution[trimRow][column], number)) {
                                trimmed += list_remove(solution[trimRow][column], number);
                            }
                        }
                    }

                    /* column */
                    for (trimColumn = 0; trimColumn < 9; trimColumn++) {
                        if (trimColumn != column) {
                            if (list_find(solution[row][trimColumn], number)) {
                                trimmed += list_remove(solution[row][trimColumn], number);
                            }
                        }
                    }

                    /* zone */
                    for (trimRow = zoneRow; trimRow < zoneRow + 3; trimRow++) {
                        for (trimColumn = zoneColumn; trimColumn < zoneColumn + 3; trimColumn++) {
                            if (trimRow != row && trimColumn != column) {
                                if (list_find(solution[trimRow][trimColumn], number)) {
                                    trimmed += list_remove(solution[trimRow][trimColumn], number);
                                }
                            }
                        }
                    }
                }
            }
        }

        totalTrimmed += trimmed;
    } while (trimmed);

    printf("Trimmed: %d", totalTrimmed);

    return totalTrimmed;
}

void sudoku_statistics(struct list *solution[9][9],
                       struct list *rowFrequencies[9][10],
                       struct list *columnFrequencies[9][10],
                       struct list *zoneFrequencies[9][10]) {
    int row;
    int column;
    int zone;
    int zoneRow;
    int zoneColumn;
    int rowColumn;
    int rowColumnZone;
    int number;

    for (rowColumnZone = 0; rowColumnZone < 9; rowColumnZone++) {
        for (number = 0; number <= 9; number++) {
            list_clear(rowFrequencies[rowColumnZone][number]);
            list_clear(columnFrequencies[rowColumnZone][number]);
            list_clear(zoneFrequencies[rowColumnZone][number]);
        }
    }

    /* rows and columns */
    for (row = 0; row < 9; row++) {
        for (column = 0; column < 9; column++) {
            for (number = 1; number <= 9; number++) {
                if (solution[row][column]->length > 1 && list_find(solution[row][column], number)) {
                    list_push(rowFrequencies[row][number], column);
                    list_push(columnFrequencies[column][number], row);
                }
            }
        }
    }

    /* zones */
    zone = 0;

    for (zoneRow = 0; zoneRow < 9; zoneRow += 3) {
        for (zoneColumn = 0; zoneColumn < 9; zoneColumn += 3) {
            for (row = zoneRow; row < zoneRow + 3; row++) {
                for (column = zoneColumn; column < zoneColumn + 3; column++) {
                    for (number = 1; number <= 9; number++) {
                        if (solution[row][column]->length > 1 &&
                                list_find(solution[row][column], number)) {
                            rowColumn = row;
                            rowColumn <<= 4;
                            rowColumn |= column;
                            list_push(zoneFrequencies[zone][number], rowColumn);
                        }
                    }
                }
            }

            ++zone;
        }
    }
}

int singleton_sudoku(struct list *solution[9][9]) {
    int rowColumnZone;
    long long row;
    long long column;
    int zone;
    int number;
    struct list *rowFrequencies[9][10];
    struct list *columnFrequencies[9][10];
    struct list *zoneFrequencies[9][10];
    long long rowColumn;
    int singletons;
    int totalSingletons = 0;

    printf("Trying singleton");

    for (rowColumnZone = 0; rowColumnZone < 9; rowColumnZone++) {
        for (number = 0; number <= 9; number++) {
            rowFrequencies[rowColumnZone][number] = list_new();
            columnFrequencies[rowColumnZone][number] = list_new();
            zoneFrequencies[rowColumnZone][number] = list_new();
        }
    }

    do {
        singletons = 0;
        sudoku_statistics(solution, rowFrequencies, columnFrequencies, zoneFrequencies);

        /* rows */
        for (row = 0; row < 9; row++) {
            for (number = 1; number <= 9; number++) {
                if (rowFrequencies[row][number]->length == 1) {
                    column = rowFrequencies[row][number]->first->value;
                    list_clear(solution[row][column]);
                    list_push(solution[row][column], number);
                    ++singletons;
                    trim_sudoku(solution);
                    sudoku_statistics(solution, rowFrequencies, columnFrequencies, zoneFrequencies);
                }
            }
        }

        /* columns */
        for (column = 0; column < 9; column++) {
            for (number = 1; number <= 9; number++) {
                if (columnFrequencies[column][number]->length == 1) {
                    row = columnFrequencies[column][number]->first->value;
                    list_clear(solution[row][column]);
                    list_push(solution[row][column], number);
                    ++singletons;
                    trim_sudoku(solution);
                    sudoku_statistics(solution, rowFrequencies, columnFrequencies, zoneFrequencies);
                }
            }
        }

        /* zones */
        for (zone = 0; zone < 9; zone++) {
            for (number = 1; number <= 9; number++) {
                if (zoneFrequencies[zone][number]->length == 1) {
                    rowColumn = zoneFrequencies[zone][number]->first->value;
                    column = rowColumn & 0xF;
                    rowColumn >>= 4;
                    row = rowColumn;
                    list_clear(solution[row][column]);
                    list_push(solution[row][column], number);
                    ++singletons;
                    trim_sudoku(solution);
                    sudoku_statistics(solution, rowFrequencies, columnFrequencies, zoneFrequencies);
                }
            }
        }

        if (singletons) {
            totalSingletons += singletons;
        }
    } while (singletons);

    for (rowColumnZone = 0; rowColumnZone < 9; rowColumnZone++) {
        for (number = 0; number <= 9; number++) {
            list_free(rowFrequencies[rowColumnZone][number]);
            list_free(columnFrequencies[rowColumnZone][number]);
            list_free(zoneFrequencies[rowColumnZone][number]);
        }
    }

    return totalSingletons;
}

int sudoku_solved(struct list *solution[9][9]) {
    int solved = 1;
    int row;
    int column;

    for (row = 0; solved && row < 9; row++) {
        for (column = 0; solved && column < 9; column++) {
            solved = (solution[row][column]->length == 1);
        }
    }

    return solved;
}

void guess_sudoku(struct list *solution[9][9]) {
    int guessRow;
    int guessColumn = 0;
    int row;
    int column;
    struct list *guesses = 0;
    struct node *guess;
    struct list *guessedSolution[9][9];
    int solved = 0;

    for (guessRow = 0; !guesses && guessRow < 9; guessRow++) {
        for (guessColumn = 0; !guesses && guessColumn < 9; guessColumn++) {
            if (solution[guessRow][guessColumn]->length > 1) {
                guesses = list_copy(solution[guessRow][guessColumn]);
            }
        }
    }

    if (guesses) {
        --guessRow;
        --guessColumn;
        guess = guesses->first;

        while (guess && !solved) {
            printf("Native guess: %lld", guess->value);
            for (row = 0; row < 9; row++) {
                for (column = 0; column < 9; column++) {
                    guessedSolution[row][column] = list_copy(solution[row][column]);
                }
            }

            list_clear(guessedSolution[guessRow][guessColumn]);
            list_push(guessedSolution[guessRow][guessColumn], guess->value);
            trim_sudoku(guessedSolution);
            solved = sudoku_solved(guessedSolution);

            if (!solved) {
                singleton_sudoku(guessedSolution);
                solved = sudoku_solved(guessedSolution);

                if (!solved) {
                    guess_sudoku(guessedSolution);
                    solved = sudoku_solved(guessedSolution);
                }
            }

            for (row = 0; row < 9; row++) {
                for (column = 0; column < 9; column++) {
                    if (solved) {
                        list_free(solution[row][column]);
                        solution[row][column] = list_copy(guessedSolution[row][column]);
                    }

                    list_free(guessedSolution[row][column]);
                }
            }

            guess = guess->next;
        }

        list_free(guesses);
    }
}

void solve_sudoku(int puzzle[9][9]) {
    struct list *solution[9][9];
    int row;
    int column;
    int number;

    for (row = 0; row < 9; row++) {
        for (column = 0; column < 9; column++) {
            solution[row][column] = list_new();

            if (puzzle[row][column]) {
                list_push(solution[row][column], puzzle[row][column]);
            } else {
                for (number = 1; number <= 9; number++) {
                    list_push(solution[row][column], number);
                }
            }
        }
    }

    trim_sudoku(solution);

    if (!sudoku_solved(solution)) {
        printf("Trim didn't work");
        singleton_sudoku(solution);

        if (!sudoku_solved(solution)) {
            printf("Singleton didn't work");
            guess_sudoku(solution);
        }
    }

    for (row = 0; row < 9; row++) {
        for (column = 0; column < 9; column++) {
            puzzle[row][column] = (int)list_pop(solution[row][column]);
            list_free(solution[row][column]);
        }
    }
}
