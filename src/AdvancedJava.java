import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class AdvancedJava {

	public void codeGen(String eval, String fileName){
        File file = new File("./" + fileName);
        BufferedWriter writer = null;
        fileName = "./src/" + fileName;
        AssemblyC assembler = new AssemblyC(eval);
        String code = assembler.assembleCcode();
        System.out.println(code);
        
        try {
            writer = new BufferedWriter(new FileWriter(fileName));
            writer.write(code);
        } catch ( IOException e) {
            e.printStackTrace();
        }finally{
            //close resources
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}