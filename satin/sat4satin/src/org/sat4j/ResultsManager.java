/*
 * SAT4J: a SATisfiability library for Java Copyright (C) 2004-2006 Daniel Le Berre
 * 
 * Based on the original minisat specification from:
 * 
 * An extensible SAT solver. Niklas E?n and Niklas S?rensson. Proceedings of the
 * Sixth International Conference on Theory and Applications of Satisfiability
 * Testing, LNCS 2919, pp 502-518, 2003.
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

package org.sat4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class ResultsManager {

    public static final String SEPARATOR = "=";

    public static final String EXT_JU = "WXP";

    public static final String COMMENT = "#";

    private final String wxpFileName;

    private final Map<String, ExitCode> files;

    private final boolean save;

    public ResultsManager(final String wxpFileName, final boolean save)
            throws MalformedURLException, IOException {

        this.wxpFileName = wxpFileName;
        this.save = save;
        files = getInformations(wxpFileName);
    }

    public final ResultCode compare(final String fileName,
            final ExitCode newCode) {

        final ExitCode tmp = files.get(fileName);
        final ExitCode oldCode = tmp == null ? ExitCode.UNKNOWN : tmp;
        final ResultCode resultCode = computeResultCode(oldCode, newCode);

        if (save && resultCode.equals(ResultCode.UPDATED)) {
            files.put(fileName, newCode);
        }

        return resultCode;
    }

    public final void save() throws IOException {

        save(wxpFileName);
    }

    public String[] getFiles() {
        return files.keySet().toArray(new String[0]);
    }

    public final void save(final String wxpFileName) throws IOException {

        final FileWriter fw = new FileWriter(wxpFileName);
        fw.write(getFileDescription());
        fw.close();
    }

    private final String getFileDescription() {
        final StringBuffer sb = new StringBuffer("#Evaluation : ");

        sb.append("\n\n");

        for (String file : files.keySet()) {
            sb.append(file);
            sb.append(ResultsManager.SEPARATOR);
            sb.append(files.get(file));
            sb.append('\n');
        }

        sb.append("\n\n#Evaluation END");
        return sb.toString();
    }

    private final ResultCode computeResultCode(final ExitCode oldS,
            final ExitCode newS) {
        if (oldS.equals(newS)) {
            return ResultCode.OK;
        }

        if ((ExitCode.UNKNOWN.equals(oldS))
                && ((ExitCode.UNSATISFIABLE.equals(newS) || (ExitCode.SATISFIABLE
                        .equals(newS))))) {
            return ResultCode.UPDATED;
        }

        if (((ExitCode.UNSATISFIABLE.equals(oldS) || (ExitCode.SATISFIABLE
                .equals(oldS))))
                && (ExitCode.UNKNOWN.equals(newS))) {
            return ResultCode.WARNING;
        }

        if (((ExitCode.SATISFIABLE.equals(oldS)) && (ExitCode.UNSATISFIABLE
                .equals(newS)))
                || ((ExitCode.UNSATISFIABLE.equals(oldS)) && (ExitCode.SATISFIABLE
                        .equals(newS)))) {
            return ResultCode.KO;
        }

        return ResultCode.UNKNOWN;
    }

    public static final Map<String, ExitCode> getInformations(final URL path)
            throws IOException {

        return getInformations(new InputStreamReader(path.openStream()));
    }

    public static final Map<String, ExitCode> getInformations(final String path)
            throws MalformedURLException, IOException {

        if (path.startsWith("http://")) {
            return getInformations(new URL(path));
        }
        return getInformations(new FileReader(path));
    }

    public static final Map<String, ExitCode> getInformations(final Reader in) {
        final BufferedReader br = new BufferedReader(in);

        final Map<String, ExitCode> ci = new HashMap<String, ExitCode>();
        StringTokenizer tokens;
        String line = null;
        int cpt = 1;

        try {

            line = br.readLine();
            for (; line != null; cpt++) {

                if ((!"".equals(line.trim()))
                        && (!(line.trim()).startsWith(COMMENT))) {
                    tokens = new StringTokenizer(line, SEPARATOR);

                    if (tokens.countTokens() == 2) {
                        ci.put(tokens.nextToken(), ExitCode.valueOf(tokens
                                .nextToken()));
                    } else {
                        throw new IllegalArgumentException();
                    }

                }
                line = br.readLine();
            }
        } catch (IllegalArgumentException i) {
            System.err.println("File Parsing Error in line " + cpt
                    + "\nError caused by : " + line);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return ci;
    }

    public static final String printLine(final String fileName,
            final ExitCode exitCode, final ResultCode resultCode) {
        return fileName + SEPARATOR + exitCode.toString() + " ["
                + resultCode.toString() + "]";
    }

    public static final String createPath() {
        final StringBuffer sb = new StringBuffer("Eval_");
        sb.append(Calendar.getInstance().getTime().toString().replace(" ", "_")
                .replace(":", "_"));
        // sb.append('.');
        // sb.append(EXT_JU.toLowerCase());
        return sb.toString();
    }

}
