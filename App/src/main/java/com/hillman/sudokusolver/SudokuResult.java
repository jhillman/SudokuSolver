package com.hillman.sudokusolver;

/**
 * Created by jeff on 12/19/13.
 */
public class SudokuResult {
    public enum SolutionTechnique {
        TRIM("Trim"),
        SINGLETON("Singleton"),
        GUESS("Guess"),
        NONE("No solution found");

        private String mName;

        private SolutionTechnique(String name) {
            mName = name;
        }

        @Override
        public String toString() {
            return mName;
        }
    }

    private boolean mSolutionFound;
    private SolutionTechnique mSolutionTechnique = SolutionTechnique.TRIM;
    private int[] mSolution;

    public SudokuResult(boolean solutionFound, SolutionTechnique solutionTechnique, int[] solution) {
        mSolutionFound = solutionFound;
        mSolutionTechnique = solutionTechnique;
        mSolution = solution;
    }

    public boolean solutionFound() {
        return mSolutionFound;
    }

    public SolutionTechnique getSolutionTechnique() {
        return mSolutionTechnique;
    }

    public int[] getSolution() {
        return mSolution;
    }
}
