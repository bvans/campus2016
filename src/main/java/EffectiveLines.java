import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class EffectiveLines {

    //判断是否为空行
    public static boolean isBlank(String line) {
        return line.trim().length() == 0;
    }

    //判断是否为单行注释,不考虑多行注释的情况
    public static boolean isCommented(String line) {
        String trimmed = line.trim();
        return trimmed.startsWith("//") || trimmed.startsWith("/*");
    }

    //统计文件的有效行数
    public static int count(String file) throws FileNotFoundException {
        int count = 0;
        String line;
        BufferedReader br = new BufferedReader(new FileReader(file));
        try {
            while((line = br.readLine()) != null ) {
                if(isBlank(line) || isCommented(line))
                    continue;
                count++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return count;
    }


    public static void main(String[] args) {
        System.out.println("请输入文件名:");
        Scanner sc = new Scanner(System.in);
        String fileName = sc.next();
        int lineCount = 0;
        try {
            lineCount  = count(fileName);
            System.out.println("有效行数为:" + lineCount);
        } catch (FileNotFoundException e) {
            System.out.println("输入的文件不存在");
        }
    }
}
