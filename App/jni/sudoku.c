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
    int solutionTechniqueInt;
    jboolean solutionFound = JNI_TRUE;
    jobject solutionTechniqueEnum;
    jstring solutionTechniqueFieldName;
    jfieldID solutionTechniqueField;
    jobject solutionTechnique;

    for (i = 0; i < 9; i++) {
        for (j = 0; j < 9; j++) {
            puzzle[i][j] = puzzleValues[i * 9 + j];
        }
    }

    solutionTechniqueInt = solve_sudoku(puzzle);

    solutionTechniqueEnum = (*env)->FindClass(env, "com/hillman/sudokusolver/SudokuResult$SolutionTechnique");

    if (solutionTechniqueEnum == NULL) {
        printf("Enum not found");
    }

    switch (solutionTechniqueInt) {
        case TECHNIQUE_TRIM:
            solutionTechniqueFieldName = "TRIM";
            break;
        case TECHNIQUE_SINGLETON:
            solutionTechniqueFieldName = "SINGLETON";
            break;
        case TECHNIQUE_GUESS:
            solutionTechniqueFieldName = "GUESS";
            break;
        case TECHNIQUE_NONE:
            solutionTechniqueFieldName = "NONE";
            solutionFound = JNI_FALSE;
            break;
    }

    solutionTechniqueField = (*env)->GetStaticFieldID(env,
        solutionTechniqueEnum, solutionTechniqueFieldName, "Lcom/hillman/sudokusolver/SudokuResult$SolutionTechnique;");

    solutionTechnique = (*env)->GetStaticObjectField(env, solutionTechniqueEnum, solutionTechniqueField);

    if (solutionFound) {
        solution = (*env)->NewIntArray(env, 81);

        for (i = 0; i < 9; i++) {
            for (j = 0; j < 9; j++) {
                flatSolution[i * 9 + j] = puzzle[i][j];
            }
        }

        (*env)->SetIntArrayRegion(env, solution, 0, 81, flatSolution);
    }

    resultClass = (*env)->FindClass(env, "com/hillman/sudokusolver/SudokuResult");
    resultConstructor = (*env)->GetMethodID(env, resultClass, "<init>", "(ZLcom/hillman/sudokusolver/SudokuResult$SolutionTechnique;[I)V");

    result = (*env)->NewObject(env, resultClass, resultConstructor, solutionFound, solutionTechnique, solution);

    return result;
}
