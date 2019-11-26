public class Tester{

    public static void TestThreeAddrGen(){
        System.out.println("*******************************************");
        System.out.println("Testing Three Address Generation");

        String eval;
        AdvancedJava parser = new AdvancedJava();
        String fileName = "test.c";

        eval = "public class test { int reserved; void main() {} void mainEntry(int five) { five = 0; reserved = five; main(); } }";
        parser.codeGen(eval, fileName);
    }

    public static void main(String[] args){
        try {
            TestThreeAddrGen();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}