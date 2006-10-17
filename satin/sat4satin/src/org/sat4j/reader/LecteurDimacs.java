/*
 * SAT4J: a SATisfiability library for Java   
 * Copyright (C) 2004 Daniel Le Berre
 * 
 * Based on the original minisat specification from:
 * 
 * An extensible SAT solver. Niklas E?n and Niklas S?rensson.
 * Proceedings of the Sixth International Conference on Theory 
 * and Applications of Satisfiability Testing, LNCS 2919, 
 * pp 502-518, 2003.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *  
 */

package org.sat4j.reader;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.zip.GZIPInputStream;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;

/**
 * Dimacs Reader written by Frederic Laihem. It is much faster that
 * DimacsReader.
 * 
 * @author leberre
 * 
 */
public class LecteurDimacs implements Reader, Serializable {

    private static final long serialVersionUID = 1L;

    /* taille du buffer */
    private final static int TAILLE_BUF = 16384;

    private ISolver s;

    private transient BufferedInputStream in;

    /* nombre de literaux dans le fichier */
    private int nbLit = 0;

    private static final char EOF = (char) -1;

    /*
     * nomFichier repr?sente le nom du fichier ? lire
     */
    public LecteurDimacs(ISolver s) {
        this.s = s;
    }

    /**
     * lit la base de clauses et la met dans le vecteur donn? en param?tre
     */
    public IProblem parseInstance(String nomFichier) throws IOException,
        ContradictionException {
        if (nomFichier.endsWith(".gz")) {
            in = new BufferedInputStream(new GZIPInputStream(
                new FileInputStream(nomFichier), TAILLE_BUF));
        } else {
            in = new BufferedInputStream(new FileInputStream(nomFichier),
                TAILLE_BUF);
        }
        s.reset();
        char car = passerCommentaire();
        if (nbLit == 0) {
            throw new IOException(
                "DIMACS non valide (nombre de Literaux non valide)");
        }
        s.newVar(nbLit);
        car = passerEspaces();
        if (car == EOF) {
            throw new IOException("DIMACS non valide (o? sont les clauses ?)");
        }
        ajouterClauses(car);
        in.close();
        return s;
    }

    /** on passe les commentaires et on lit le nombre de literaux */
    private char passerCommentaire() throws IOException {
        char car;
        for (;;) {
            car = passerEspaces();
            if (car == 'p') {
                car = lectureNombreLiteraux();
            }
            if (car != 'c' && car != 'p') {
                break; /* fin des commentaires */
            }
            car = nextLine(); /* on passe la ligne de commentaire */
            if (car == EOF) {
                break;
            }
        }
        return car;
    }

    /** lit le nombre repr?sentant le nombre de literaux */
    private char lectureNombreLiteraux() throws IOException {
        char car = nextChiffre(); /* on lit le prchain chiffre */
        if (car != EOF) {
            nbLit = car - '0';
            for (;;) { /* on lit le chiffre repr?sentant le nombre de literaux */
                car = (char) in.read();
                if (car < '0' || car > '9') {
                    break;
                }
                nbLit = 10 * nbLit + (car - '0');
            }
            if (car != EOF) {
                nextLine(); /* on lit la fin de la ligne */
            }
        }
        return car;
    }

    /** lit les clauses et les ajoute dans le vecteur donn? en param?tre */
    private void ajouterClauses(char car) throws IOException,
        ContradictionException {
        final IVecInt lit = new VecInt();
        int val = 0;
        boolean neg = false;
        for (;;) {
            /* on lit le signe du literal */
            if (car == '-') {
                neg = true;
                car = (char) in.read();
            } else if (car == '+') {
                car = (char) in.read();
            } else /* on le 1er chiffre du literal */
            if (car >= '0' && car <= '9') {
                val = car - '0';
                car = (char) in.read();
            } else {
                break;
            }
            /* on lit la suite du literal */
            while (car >= '0' && car <= '9') {
                val = (val * 10) + car - '0';
                car = (char) in.read();
            }
            if (val == 0) { // on a lu toute la clause
                s.addClause(lit);
                lit.clear();
            } else {
                /* on ajoute le literal au vecteur */
                // s.newVar(val-1);
                lit.push(neg ? -val : val);
                neg = false;
                val = 0; /* on reinitialise les variables */
            }
            if (car != EOF) {
                car = passerEspaces();
            }
            if (car == EOF) {
                break; /* on a lu tout le fichier */
            }
        }
    }

    /** passe tout les caract?res d'espacement (espace ou \n) */
    private char passerEspaces() throws IOException {
        char car;
        while ((car = (char) in.read()) == ' ' || car == '\n') {
            ;
        }
        return car;
    }

    /** passe tout les caract?res jusqu? rencontrer une fin de la ligne */
    private char nextLine() throws IOException {
        char car;
        do {
            car = (char) in.read();
        } while ((car != '\n') && (car != EOF));
        return car;
    }

    /** passe tout les caract?re jusqu'? rencontrer un chiffre */
    private char nextChiffre() throws IOException {
        char car;
        do {
            car = (char) in.read();
        } while ((car < '0') || (car > '9') && (car != EOF));
        return car;
    }

    public String decode(int[] model) {
        StringBuffer stb = new StringBuffer();
        for (int i = 0; i < model.length; i++) {
            stb.append(model[i]);
            stb.append(" ");
        }
        stb.append("0");
        return stb.toString();
    }
}
