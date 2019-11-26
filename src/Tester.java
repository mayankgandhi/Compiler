import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Tester{

    public static void consume(Process cmdProc) throws IOException{
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

    public static void TestThreeAddrGen() throws IOException, InterruptedException{
        System.out.println("*******************************************");
        System.out.println("Testing Three Address Generation");

        String  eval = "public class test { int reserved; int voidns() {return 10;}  int mainEntry(int x, int y) {x=100; y = 5; reserved = x + y; } }";
        AdvancedJava parser = new AdvancedJava();
        String fileName = "test1.c";
        parser.codeGen(eval, fileName);

        /* Run Shell command */
        Process cmdProc = Runtime.getRuntime().exec("gcc -g -Wall " + fileName + " -o test");
        cmdProc.waitFor();
        consume(cmdProc);
        cmdProc = Runtime.getRuntime().exec("./test");
        cmdProc.waitFor();
        consume(cmdProc);
        int retValue = cmdProc.exitValue();
        if( retValue != 105 )
        {
            System.out.println( "FAILED test\nExpected: 5\nActual: " + retValue );
        }
        else
        {
            System.out.println("PASSED test");
        }

    }

    public static void main(String[] args){
        try {
            TestThreeAddrGen();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
