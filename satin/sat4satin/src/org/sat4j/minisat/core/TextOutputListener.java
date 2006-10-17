package org.sat4j.minisat.core;

public class TextOutputListener implements SearchListener {

    public void assuming(int p) {
        System.out.println("assuming " + p);
    }

    public void propagating(int p) {
        System.out.println("implies " + p);
    }

    public void backtracking(int p) {
        System.out.println("backtracking " + p);
    }

    public void adding(int p) {
        System.out.println("adding " + p);
    }

    public void learn(Constr clause) {

    }

    public void delete(int[] clause) {

    }

    public void conflictFound() {
        System.out.println("conflict ");
    }

    public void solutionFound() {
        System.out.println("solution found ");
    }

    public void beginLoop() {
        // TODO Auto-generated method stub

    }

}
