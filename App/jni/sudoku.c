#include <jni.h>
#include <string.h>
#include <android/log.h>

#define TAG "SudokuSolverNative"
#define printf(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__);

#include "sudoku.h"

jobject Java_com_hillman_sudokusolver_activity_MainActivity_solve(JNIEnv * env, jobject this, jintArray flatPuzzle)
{
    int i, j;
    int puzzle[9][9];
    jint flatSolution[81];
    jintArray solution;
    jint* puzzleValues = (*env)->GetIntArrayElements(env, flatPuzzle, NULL);
    jobject resultClass;
    jmethodID resultConstructor;
    jobject result;
    int solutionStrategyInt;
    jboolean solutionFound = JNI_TRUE;
    jobject solutionStrategyEnum;
    jstring solutionStrategyFieldName;
    jfieldID solutionStrategyField;
    jobject solutionStrategy;

    for (i = 0; i < 9; i++) {
        for (j = 0; j < 9; j++) {
            puzzle[i][j] = puzzleValues[i * 9 + j];
        }
    }

    solutionStrategyInt = solve_sudoku(puzzle);

    solutionStrategyEnum = (*env)->FindClass(env, "com/hillman/sudokusolver/SudokuResult$SolutionStrategy");

    if (solutionStrategyEnum == NULL) {
        printf("Enum not found");
    }

    switch (solutionStrategyInt) {
        case STRATEGY_TRIM:
            solutionStrategyFieldName = "TRIM";
            break;
        case STRATEGY_SINGLETON:
            solutionStrategyFieldName = "SINGLETON";
            break;
        case STRATEGY_GUESS:
            solutionStrategyFieldName = "GUESS";
            break;
        case STRATEGY_NONE:
            solutionStrategyFieldName = "NONE";
            solutionFound = JNI_FALSE;
            break;
    }

    solutionStrategyField = (*env)->GetStaticFieldID(env,
        solutionStrategyEnum, solutionStrategyFieldName, "Lcom/hillman/sudokusolver/SudokuResult$SolutionStrategy;");

    solutionStrategy = (*env)->GetStaticObjectField(env, solutionStrategyEnum, solutionStrategyField);

    solution = (*env)->NewIntArray(env, 81);

    if (solutionFound) {
        for (i = 0; i < 9; i++) {
            for (j = 0; j < 9; j++) {
                flatSolution[i * 9 + j] = puzzle[i][j];
            }
        }

        (*env)->SetIntArrayRegion(env, solution, 0, 81, flatSolution);
    }

    resultClass = (*env)->FindClass(env, "com/hillman/sudokusolver/SudokuResult");
    resultConstructor = (*env)->GetMethodID(env, resultClass, "<init>", "(ZLcom/hillman/sudokusolver/SudokuResult$SolutionStrategy;[I)V");

    result = (*env)->NewObject(env, resultClass, resultConstructor, solutionFound, solutionStrategy, solution);

    return result;
}
