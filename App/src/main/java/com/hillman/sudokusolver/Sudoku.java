package com.hillman.sudokusolver;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jeff on 12/18/13.
 */
public class Sudoku {
    private static int trimSudoku(List<Integer>[][] solution) {
        int row;
        int column;
        int number;
        int trimRow;
        int trimColumn;
        int zoneRow = 0;
        int zoneColumn = 0;
        int trimmed;
        int totalTrimmed = 0;

        Log.d("SudokuSolver", String.format("Trying trim"));

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

                    if (solution[row][column].size() == 1) {
                        number = solution[row][column].get(0);

                        /* row */
                        for (trimRow = 0; trimRow < 9; trimRow++) {
                            if (trimRow != row) {
                                int index = solution[trimRow][column].indexOf(number);
                                if (index > -1) {
                                    trimmed += solution[trimRow][column].remove(index);
                                }
                            }
                        }

                    /* column */
                        for (trimColumn = 0; trimColumn < 9; trimColumn++) {
                            if (trimColumn != column) {
                                int index = solution[row][trimColumn].indexOf(number);
                                if (index > -1) {
                                    trimmed += solution[row][trimColumn].remove(index);
                                }
                            }
                        }

                    /* zone */
                        for (trimRow = zoneRow; trimRow < zoneRow + 3; trimRow++) {
                            for (trimColumn = zoneColumn; trimColumn < zoneColumn + 3; trimColumn++) {
                                if (trimRow != row && trimColumn != column) {
                                    int index = solution[trimRow][trimColumn].indexOf(number);
                                    if (index > 0) {
                                        trimmed += solution[trimRow][trimColumn].remove(index);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            totalTrimmed += trimmed;
        } while (trimmed != 0);

        Log.d("SudokuSolver", "Trimmed: " + totalTrimmed);

        return totalTrimmed;
    }

    private static void sudokuStatistics(List<Integer>[][] solution,
                                         List<Integer>[][] rowFrequencies,
                                         List<Integer>[][] columnFrequencies,
                                         List<Integer>[][] zoneFrequencies) {
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
                rowFrequencies[rowColumnZone][number].clear();
                columnFrequencies[rowColumnZone][number].clear();
                zoneFrequencies[rowColumnZone][number].clear();
            }
        }

    /* rows and columns */
        for (row = 0; row < 9; row++) {
            for (column = 0; column < 9; column++) {
                for (number = 1; number <= 9; number++) {
                    if (solution[row][column].size() > 1 && solution[row][column].indexOf(number) > -1) {
                        rowFrequencies[row][number].add(column);
                        columnFrequencies[column][number].add(row);
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
                            if (solution[row][column].size() > 1 && solution[row][column].indexOf(number) > -1) {
                                rowColumn = row;
                                rowColumn <<= 4;
                                rowColumn |= column;
                                zoneFrequencies[zone][number].add(rowColumn);
                            }
                        }
                    }
                }

                ++zone;
            }
        }
    }

    private static int singletonSudoku(List<Integer>[][] solution) {
        int rowColumnZone;
        int row;
        int column;
        int zone;
        int number;
        List<Integer>[][] rowFrequencies = new ArrayList[9][10];
        List<Integer>[][] columnFrequencies = new ArrayList[9][10];
        List<Integer>[][] zoneFrequencies = new ArrayList[9][10];
        int rowColumn;
        int singletons;
        int totalSingletons = 0;

        Log.d("SudokuSolver", String.format("Trying singleton"));

        for (rowColumnZone = 0; rowColumnZone < 9; rowColumnZone++) {
            for (number = 0; number <= 9; number++) {
                rowFrequencies[rowColumnZone][number] = new ArrayList<>();
                columnFrequencies[rowColumnZone][number] = new ArrayList<>();
                zoneFrequencies[rowColumnZone][number] = new ArrayList<>();
            }
        }

        do {
            singletons = 0;
            sudokuStatistics(solution, rowFrequencies, columnFrequencies, zoneFrequencies);

        /* rows */
            for (row = 0; row < 9; row++) {
                for (number = 1; number <= 9; number++) {
                    if (rowFrequencies[row][number].size() == 1) {
                        column = rowFrequencies[row][number].get(0);
                        solution[row][column].clear();
                        solution[row][column].add(number);
                        ++singletons;
                        trimSudoku(solution);
                        sudokuStatistics(solution, rowFrequencies, columnFrequencies, zoneFrequencies);
                    }
                }
            }

        /* columns */
            for (column = 0; column < 9; column++) {
                for (number = 1; number <= 9; number++) {
                    if (columnFrequencies[column][number].size() == 1) {
                        row = columnFrequencies[column][number].get(0);
                        solution[row][column].clear();
                        solution[row][column].add(number);
                        ++singletons;
                        trimSudoku(solution);
                        sudokuStatistics(solution, rowFrequencies, columnFrequencies, zoneFrequencies);
                    }
                }
            }

        /* zones */
            for (zone = 0; zone < 9; zone++) {
                for (number = 1; number <= 9; number++) {
                    if (zoneFrequencies[zone][number].size() == 1) {
                        rowColumn = zoneFrequencies[zone][number].get(0);
                        column = rowColumn & 0xF;
                        rowColumn >>= 4;
                        row = rowColumn;
                        solution[row][column].clear();
                        solution[row][column].add(number);
                        ++singletons;
                        trimSudoku(solution);
                        sudokuStatistics(solution, rowFrequencies, columnFrequencies, zoneFrequencies);
                    }
                }
            }

            if (singletons > 0) {
                totalSingletons += singletons;
            }
        } while (singletons > 0);

        for (rowColumnZone = 0; rowColumnZone < 9; rowColumnZone++) {
            for (number = 0; number <= 9; number++) {
                rowFrequencies[rowColumnZone][number].clear();
                columnFrequencies[rowColumnZone][number].clear();
                zoneFrequencies[rowColumnZone][number].clear();
            }
        }

        return totalSingletons;
    }

    private static boolean sudokuSolved(List<Integer>[][] solution) {
        boolean solved = true;
        int row;
        int column;

        for (row = 0; solved && row < 9; row++) {
            for (column = 0; solved && column < 9; column++) {
                solved = solution[row][column].size() == 1;
            }
        }

        return solved;
    }

    private static void guessSudoku(List<Integer>[][] solution) {
        int guessRow;
        int guessColumn = 0;
        int row;
        int column;
        List<Integer> guesses = null;
        int guess;
        List<Integer>[][] guessedSolution = new ArrayList[9][9];
        boolean solved = false;

        for (guessRow = 0; guesses == null && guessRow < 9; guessRow++) {
            for (guessColumn = 0; guesses == null && guessColumn < 9; guessColumn++) {
                if (solution[guessRow][guessColumn].size() > 1){
                    guesses = new ArrayList<>(solution[guessRow][guessColumn]);
                }
            }
        }

        if (guesses != null && !guesses.isEmpty()) {
            --guessRow;
            --guessColumn;
            guess = guesses.remove(0);

            while (guess > 0 && !solved) {
                Log.d("SudokuSolver", String.format("Java guess: %d", guess));

                for (row = 0; row < 9; row++) {
                    for (column = 0; column < 9; column++) {
                        guessedSolution[row][column] = new ArrayList<>(solution[row][column]);
                    }
                }

                guessedSolution[guessRow][guessColumn].clear();
                guessedSolution[guessRow][guessColumn].add(guess);
                trimSudoku(guessedSolution);
                solved = sudokuSolved(guessedSolution);

                if (!solved) {
                    singletonSudoku(guessedSolution);
                    solved = sudokuSolved(guessedSolution);

                    if (!solved) {
                        guessSudoku(guessedSolution);
                        solved = sudokuSolved(guessedSolution);
                    }
                }

                for (row = 0; row < 9; row++) {
                    for (column = 0; column < 9; column++) {
                        if (solved) {
                            solution[row][column] = new ArrayList<>(guessedSolution[row][column]);
                        }
                    }
                }

                if (guesses.isEmpty()) {
                    break;
                }

                guess = guesses.remove(0);
            }
        }
    }

    private static void solveSudoku(int[][] puzzle) {
        List<Integer>[][] solution = new ArrayList[9][9];
        int row;
        int column;
        int number;

        for (row = 0; row < 9; row++) {
            for (column = 0; column < 9; column++) {
                solution[row][column] = new ArrayList<>();

                if (puzzle[row][column] != 0) {
                    solution[row][column].add(puzzle[row][column]);
                } else {
                    for (number = 1; number <= 9; number++) {
                        solution[row][column].add(number);
                    }
                }
            }
        }

        trimSudoku(solution);

        if (!sudokuSolved(solution)) {
            Log.d("SudokuSolver", "Trim didn't work");
            singletonSudoku(solution);

            if (!sudokuSolved(solution)) {
                Log.d("SudokuSolver", "Singleton didn't work");
                guessSudoku(solution);
            } else {
                Log.d("SudokuSolver", "here");
            }
        }

        for (row = 0; row < 9; row++) {
            for (column = 0; column < 9; column++) {
                puzzle[row][column] = solution[row][column].get(0);
            }
        }
    }

    public static int[] solve(int[] flatPuzzle) {
        int[][] puzzle = new int[9][9];

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                puzzle[i][j] = flatPuzzle[i * 9 + j];
            }
        }

        solveSudoku(puzzle);

        int[] solution = new int[81];

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                solution[i * 9 + j] = puzzle[i][j];
            }
        }

        return solution;
    }
}