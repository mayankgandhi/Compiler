import java.io.*;
public class AdvancedJava {

    public void codeGen(String eval, String fileName) {
        File file = new File(fileName);
        BufferedWriter writer = null;
        AssemblyC assembler = new AssemblyC(eval);
        String code = assembler.assembleCcode();
        try {
            writer = new BufferedWriter(new FileWriter(fileName));
            writer.write(code);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // close resources
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void consume(Process cmdProc) throws IOException {
		BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(cmdProc.getInputStream()));
		String line;
		while ((line = stdoutReader.readLine()) != null) {
			// process procs standard output here
		}

		BufferedReader stderrReader = new BufferedReader(new InputStreamReader(cmdProc.getErrorStream()));
		while ((line = stderrReader.readLine()) != null) {
			// process procs standard error here
		}
	}
    public static void main(String args[]) {
        String eval = "public class test { int x; int y; int reserved; void mainEntry() { reserved = 0; if(2 >= 3) {reserved = 120;}} }";
        AdvancedJava parser = new AdvancedJava();
        String fileName = "test.c";
        parser.codeGen(eval, fileName);
        try{
            /* Run Shell command */
        Process cmdProc = Runtime.getRuntime().exec("gcc -g -Wall " + fileName + " -o test");
        cmdProc.waitFor();
        consume(cmdProc);
        cmdProc = Runtime.getRuntime().exec("./test");
        cmdProc.waitFor();
        consume(cmdProc);
        int retValue = cmdProc.exitValue();
        System.out.println(retValue);
        }
        catch (Exception e)
        {
            System.out.println("ERROR");
        }
        
    }
}