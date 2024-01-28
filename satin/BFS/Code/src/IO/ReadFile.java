/*
 * Copyright 2017 Patrick Goddijn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package IO;

import java.io.*;

public class ReadFile {
    public static void read(String filename, Graph g, boolean directed) throws Exception {
        File input_file = new File(filename);
        DataInputStream data_in = new DataInputStream(new BufferedInputStream(new FileInputStream(input_file)));

        int a;
        int b;

        while(true) {
            try {
                a = data_in.readInt();
                b = data_in.readInt();

                g.addEdge(a, b);

                if(!directed) {
                    g.addEdge(b, a);
                }
            }
            catch(EOFException eof) {
                System.out.println ("End of File");
                g.done();
                break;
            }
        }
    }
}