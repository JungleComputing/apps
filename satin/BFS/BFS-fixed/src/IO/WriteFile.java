package IO;

import java.io.*;

public class WriteFile {
    public static void write(String in_file_name, String out_file_name) throws Exception {
        File output_file = new File(out_file_name);
        output_file.createNewFile();

        DataOutputStream data_out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(output_file)));

        int a;
        int b;

        // Read file line for line
        try (BufferedReader br = new BufferedReader(new FileReader(in_file_name))) {
            String line;

            // For every following line, split on \t and add an edge between the two numbers.
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                a = Integer.parseInt(parts[0]);
                b = Integer.parseInt(parts[1]);
                data_out.writeInt(a);
                data_out.writeInt(b);
            }
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }

    public static void main(String[] args) {
        if(args.length == 0) {
            System.err.println("No Filename specified.");
        } else if(args.length > 1) {
            System.err.println("Too many arguments given.");
        } else {
            try {
                write(args[0] + ".txt", args[0] + ".dat");
            } catch(Exception e) {
                System.err.println("Caught Exception: " + e.getMessage());
            }
        }
    }
}