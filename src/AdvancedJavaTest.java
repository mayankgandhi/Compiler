import java.io.*;
import java.util.*;

public class AdvancedJavaTest {

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

	public static void TestThreeAddrGen() throws IOException, InterruptedException {
		System.out.println("*******************************************");
		System.out.println("Testing Three Address Generation");
		AdvancedJava parser = new AdvancedJava();

		String eval;
		String fileName;
		int retValue;

		try {
			eval = "public class test { int x; int y; int reserved; void mainEntry() { reserved = 0; if(2 < 3) {reserved = 42;}} }";
			fileName = "test.c";
			parser.codeGen(eval, fileName);
			retValue = runShellCommand(fileName);
			assert (retValue == 42);
			System.out.println("Test 1 Passed");
		} catch (AssertionError e) {
			System.out.print("Test 1 Failed");
		}

		try {
			eval = "public class test { int x; int y; int reserved; void mainEntry() { reserved = 0; if(2 > 3) {reserved = 42;}} }";
			fileName = "test.c";
			parser.codeGen(eval, fileName);
			retValue = runShellCommand(fileName);
			assert (retValue == 0);
			System.out.println("Test 2 Passed");
		} catch (AssertionError e) {
			System.out.print("Test 2 Failed");
		}

		try {
			eval = "public class test { int reserved; void mainEntry() { int x = 10; int y = 5; if (x < y) {reserved = 100; } if (x > y) {reserved = 200;} }}";
			fileName = "test.c";
			parser.codeGen(eval, fileName);
			retValue = runShellCommand(fileName);
			assert (retValue == 200);
			System.out.println("Test 3 Passed");
		} catch (AssertionError e) {
			System.out.print("Test 3 Failed");
		}

		try {
			eval = "public class test { int reserved; void mainEntry() { reserved = 0; int x = 10; int y = 5; if (y > x || x == 10) {reserved = 150;} }}";
			fileName = "test.c";
			parser.codeGen(eval, fileName);
			retValue = runShellCommand(fileName);
			assert (retValue == 150);
			System.out.println("Test 4 Passed");
		} catch (AssertionError e) {
			System.out.print("Test 4 Failed");
		}

		try {
			eval = "public class test { int reserved; void mainEntry() {}}";
			fileName = "test.c";
			parser.codeGen(eval, fileName);
			retValue = runShellCommand(fileName);
			assert (retValue == 0);
			System.out.println("Test 5 Passed");
		} catch (AssertionError e) {
			System.out.print("Test 5 Failed");
		}

		try {
			eval = "public class test { int reserved; void mainEntry() {reserved = 0; int i; i = 0; while(i<10) {reserved = reserved + i ; i = i + 1;}}}";
			fileName = "test.c";
			parser.codeGen(eval, fileName);
			retValue = runShellCommand(fileName);
			assert (retValue == 45);
			System.out.println("Test 6 Passed");
		} catch (AssertionError e) {
			System.out.print("Test 6 Failed");
		}

		try {
			eval = "public class test { int reserved; int x; void mainEntry() {int x = 0; while (x<10) {if (x !=5) {reserved = reserved + x; } x = x + 1;}}}";
			fileName = "test.c";
			parser.codeGen(eval, fileName);
			retValue = runShellCommand(fileName);
			assert (retValue == 40);
			System.out.println("Test 7 Passed");
		} catch (AssertionError e) {
			System.out.print("Test 7 Failed");
		}

		try {
			eval = "public class test { int reserved; void mainEntry() {int reserved = 10;}}";
			fileName = "test.c";
			parser.codeGen(eval, fileName);
			retValue = runShellCommand(fileName);
			assert (retValue == 0);
			System.out.println("Test 8 Passed");
		} catch (AssertionError e) {
			System.out.print("Test 8 Failed");
		}

		try {
			eval = "public class test { int reserved; void mainEntry() { reserved = 0; int x = 2; int y = 2; if (y >= x || x == 10) {reserved = 100;} }}";
			fileName = "test.c";
			parser.codeGen(eval, fileName);
			retValue = runShellCommand(fileName);
			assert (retValue == 100);
			System.out.println("Test 9 Passed");
		} catch (AssertionError e) {
			System.out.print("Test 9 Failed");
		}

		try {
			eval = "public class test { int reserved; void mainEntry() { reserved = 0; int x = 3; int y = 2; if (y >= x || x == 10) {reserved = 100;} }}";
			fileName = "test.c";
			parser.codeGen(eval, fileName);
			retValue = runShellCommand(fileName);
			assert (retValue == 0);
			System.out.println("Test 10 Passed");
		} catch (AssertionError e) {
			System.out.print("Test 10 Failed");
		}

		try {
			eval = "public class test { int reserved; void mainEntry(){reserved = 19; if(reserved < 12 && 3 <= 2){reserved = 8;}}}";
			fileName = "test.c";
			parser.codeGen(eval, fileName);
			retValue = runShellCommand(fileName);
			assert (retValue == 19);
			System.out.println("Test 11 Passed");
		} catch (AssertionError e) {
			System.out.print("Test 11 Failed");
		}

		try {
			eval = "public class test { int reserved; void mainEntry(){reserved = 19; if(reserved < 12 && 3 <= 3){reserved = 8;}}}";
			fileName = "test.c";
			parser.codeGen(eval, fileName);
			retValue = runShellCommand(fileName);
			assert (retValue == 19);
			System.out.println("Test 12 Passed");
		} catch (AssertionError e) {
			System.out.print("Test 12 Failed");
		}

		try {
			eval = "public class test { int reserved; void mainEntry(){reserved = 19; if(reserved < 20 && 3 <= 3){reserved = 8;}}}";
			fileName = "test.c";
			parser.codeGen(eval, fileName);
			retValue = runShellCommand(fileName);
			assert (retValue == 8);
			System.out.println("Test 13 Passed");
		} catch (AssertionError e) {
			System.out.print("Test 13 Failed");
		}

		try {
			eval = "public class test { int reserved; void mainEntry(){reserved = 19; if(reserved < 20 && 3 <= 3){reserved = 8;}}}";
			fileName = "test.c";
			parser.codeGen(eval, fileName);
			retValue = runShellCommand(fileName);
			assert (retValue == 8);
			System.out.println("Test 14 Passed");
		} catch (AssertionError e) {
			System.out.print("Test 14 Failed");
		}

		try {
			eval = "public class test { int reserved; void mainEntry(){reserved = 19; int x = 4; if(x != 4){int reserved = 100;}}}";
			fileName = "test.c";
			parser.codeGen(eval, fileName);
			retValue = runShellCommand(fileName);
			assert (retValue == 19);
			System.out.println("Test 15 Passed");
		} catch (AssertionError e) {
			System.out.println("**Test 15 Failed**");
		}

		try {
			eval = "public class test { int reserved; void mainEntry(){reserved = 19; int x = 4; if(x == 4){int reserved = 100;}}}";
			fileName = "test.c";
			parser.codeGen(eval, fileName);
			retValue = runShellCommand(fileName);
			assert (retValue == 19);
			System.out.println("Test 16 Passed");
		} catch (AssertionError e) {
			System.out.println("**Test 16 Failed**");
		}

		try {
			eval = "public class test { int reserved; void mainEntry(){reserved = 19-20; }}";
			fileName = "test.c";
			parser.codeGen(eval, fileName);
			retValue = runShellCommand(fileName);
			assert (retValue == -1);
			System.out.println("Test 17 Passed");
		} catch (AssertionError e) {
			System.out.println("**Test 17 Failed**");
		}

		try {
			eval = "public class test { int reserved; void mainEntry(){reserved = 20-19; }}";
			fileName = "test.c";
			parser.codeGen(eval, fileName);
			retValue = runShellCommand(fileName);
			assert (retValue == 1);
			System.out.println("Test 17 Passed");
		} catch (AssertionError e) {
			System.out.println("**Test 17 Failed**");
		}

		try {
			eval = "public class test { int reserved; void mainEntry(){reserved = 0; int x = 1; int y = 2; if (x == 1 && y == 2) {reserved = reserved + 10;} }}";
			fileName = "test.c";
			parser.codeGen(eval, fileName);
			retValue = runShellCommand(fileName);
			assert (retValue == 10);
			System.out.println("Test 18 Passed");
		} catch (AssertionError e) {
			System.out.println("**Test 18 Failed**");
		}

		try {
			eval = "public class test { int reserved; void mainEntry(){reserved = 0; int x = 1; int y = 2; if (x != 1 || y == 2) {reserved = reserved + 10;} }}";
			fileName = "test.c";
			parser.codeGen(eval, fileName);
			retValue = runShellCommand(fileName);
			assert (retValue == 10);
			System.out.println("Test 19 Passed");
		} catch (AssertionError e) {
			System.out.println("**Test 19 Failed**");
		}

		try {
			eval = "public class test { int reserved; void mainEntry(){reserved = 0; int x = 1; int y = 2; if (x != 1 && y == 2) {reserved = reserved + 10;} }}";
			fileName = "test.c";
			parser.codeGen(eval, fileName);
			retValue = runShellCommand(fileName);
			assert (retValue == 0);
			System.out.println("Test 20 Passed");
		} catch (AssertionError e) {
			System.out.println("**Test 20 Failed**");
		}

		try {
			eval = "public class test { int reserved; void mainEntry(){reserved = 0; int x = 1; int y = 2; if (x == 1 && y == 2) {reserved = reserved + 10;} if (x != 1 || y == 2) {reserved = reserved + 10;} }}";
			fileName = "test.c";
			parser.codeGen(eval, fileName);
			retValue = runShellCommand(fileName);
			assert (retValue == 20);
			System.out.println("Test 21 Passed");
		} catch (AssertionError e) {
			System.out.println("**Test 21 Failed**");
		}

		try {
			eval = "public class test { int reserved; int x; void mainEntry() {x = 10; reserved = 1; while (x<10) { reserved = reserved + 10; x = x + 1; }  }}";
			fileName = "test.c";
			parser.codeGen(eval, fileName);
			retValue = runShellCommand(fileName);
			assert (retValue == 1);
			System.out.println("Test 22 Passed");
		} catch (AssertionError e) {
			System.out.print("**Test 22 Failed**");
		}

		try {
			eval = "public class test { int reserved; int y; void mainEntry() {y = 10; reserved = y; reserved = reserved / 2;}}";
			fileName = "test.c";
			parser.codeGen(eval, fileName);
			retValue = runShellCommand(fileName);
			assert (retValue == 5);
			System.out.println("Test 23 Passed");
		} catch (AssertionError e) {
			System.out.print("**Test 23 Failed**");
		}

		System.out.println("*******************************************");
	}

	public static int runShellCommand(String fileName) {
		/* Run Shell command */
		int retValue = 0;
		Process cmdProc;
		try {
			cmdProc = Runtime.getRuntime().exec("gcc -g -Wall " + fileName + " -o test");
			cmdProc.waitFor();
			consume(cmdProc);
			cmdProc = Runtime.getRuntime().exec("./test");
			cmdProc.waitFor();
			consume(cmdProc);

			retValue = cmdProc.exitValue();
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retValue;
	}

	public static void main(String[] args) {
		try {
			TestThreeAddrGen();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
