#include <jni.h>
#include <string.h>
#include <android/log.h>

#define TAG "SudokuSolver"
#define printf(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__);

#include "sudoku.h"

jintArray Java_com_hillman_sudokusolver_activity_MainActivity_solve(JNIEnv * env, jobject this, jintArray flatPuzzle)
{
    int i, j;
    int puzzle[9][9];
    jint flatSolution[81];
    jintArray solution;
    jint* puzzleValues = (*env)->GetIntArrayElements(env, flatPuzzle, NULL);

    for (i = 0; i < 9; i++) {
        for (j = 0; j < 9; j++) {
            puzzle[i][j] = puzzleValues[i * 9 + j];
        }
    }

    solve_sudoku(puzzle);

    solution = (*env)->NewIntArray(env, 81);

    for (i = 0; i < 9; i++) {
        for (j = 0; j < 9; j++) {
            flatSolution[i * 9 + j] = puzzle[i][j];
        }
    }

    (*env)->SetIntArrayRegion(env, solution, 0, 81, flatSolution);

    return solution;
}
