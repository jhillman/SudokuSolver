package com.hillman.sudokusolver;

/**
 * Created by jeff on 12/19/13.
 */
public class SudokuResult {
    public enum SolutionStrategy {
        TRIM("Trim"),
        SINGLETON("Singleton"),
        GUESS("Guess"),
        NONE("No solution found");

        private String mName;

        private SolutionStrategy(String name) {
            mName = name;
        }

        @Override
        public String toString() {
            return mName;
        }
    }

    private boolean mSolutionFound;
    private SolutionStrategy mSolutionStrategy = SolutionStrategy.TRIM;
    private int[] mSolution;

    public SudokuResult(boolean solutionFound, SolutionStrategy solutionStrategy, int[] solution) {
        mSolutionFound = solutionFound;
        mSolutionStrategy = solutionStrategy;
        mSolution = solution;
    }

    public boolean solutionFound() {
        return mSolutionFound;
    }

    public SolutionStrategy getSolutionStrategy() {
        return mSolutionStrategy;
    }

    public int[] getSolution() {
        return mSolution;
    }
}
