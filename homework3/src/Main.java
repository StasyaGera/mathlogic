import java.io.*;

public class Main {
    public static void main(String[] args) {
        String usage = "Usage: file <input file> <output file> or number <a> <output file>";

        if (args.length != 3) {
            System.err.println(usage);
            System.exit(1);
        }

        int x = 0;
        switch (args[0]) {
            case "file":
                try (BufferedReader reader = new BufferedReader(new FileReader(args[1]))) {
                    x = Integer.parseInt(reader.readLine());
                } catch (UnsupportedEncodingException e) {
                    System.err.println("Expected UTF-8 encoding: " + e.getMessage());
                    System.exit(1);
                } catch (FileNotFoundException e) {
                    System.err.println("Cannot find input file: " + e.getMessage());
                    System.exit(1);
                } catch (IOException e) {
                    System.err.println("Error while working with file: " + e.getMessage());
                    System.exit(1);
                }
                break;
            case "number":
                x = Integer.parseInt(args[1]);
                break;
            default:
                System.err.println(usage);
                System.exit(1);
        }

        Main m = new Main();
        m.run(new File(args[2]), x);
    }

    private void run(File output, int x) {
        String a = convert(x);
        String header = "|- (a + 0') * (a + 0') = a * a + 0'' * a + 0'";
        String statement = "@a((a + 0') * (a + 0') = a * a + 0'' * a + 0') -> ";
        String replaced = "((a + 0') * (a + 0') = a * a + 0'' * a + 0')";
        String result = "(a + 0') * (a + 0') = a * a + 0'' * a + 0'";

        try (BufferedReader proof = new BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream("final.proof")));
//        try (BufferedReader proof = new BufferedReader(new FileReader("final.proof"));
             PrintWriter writer = new PrintWriter(output)) {
            writer.println(header.replaceAll("a", a));
            String next;
            while ((next = proof.readLine()) != null) {
                writer.println(next);
            }
            writer.print(statement);
            writer.println(replaced.replaceAll("a", a));
            writer.println(result.replaceAll("a", a));
        } catch (FileNotFoundException e) {
            System.err.println("Cannot find or create file: " + e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Error while working with file: " + e.getMessage());
            System.exit(1);
        }
    }

    private String convert(int x) {
        String result = "0";
        while (x > 0) {
            result = result.concat("\'");
            x--;
        }
        return result;
    }
}
