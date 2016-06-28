import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;


/**
 * Created by fan on 2016/6/27.
 */
public class CountMostImport {

    public String[] getTop10Classes(String path) {
        List<String> files = new LinkedList<String>();
        getJavaFiles(files, new File(path));
        return null;
    }

    public String[] getTop10(String dir) {
        TreeMap<String, Clazz> map = getAllImportedClasses(dir);
        Clazz[] list = new Clazz[map.size()];
        int i = 0;
        for (Clazz c : map.values()) {
            list[i++] = c;
        }
        Arrays.sort(list);

        String[] classes = new String[10];
        for (i = 0; i < 10 && i < list.length; i++) {
            classes[i] = list[i].className;
        }
        return classes;
    }

    public TreeMap<String, Clazz> getAllImportedClasses(String dir) {
        List<String> files = new LinkedList<String>();
        getJavaFiles(files, new File(dir));
        TreeMap<String, Clazz> map = new TreeMap<String, Clazz>();
        for (String file : files) {
            List<String> classes = getImportedClasses(file);
            for (String name : classes) {
                if (map.get(name) == null) {
                    map.put(name, new Clazz(name, 1));
                } else {
                    map.get(name).add();
                }
            }
        }

        return map;
    }

    public List<String> getImportedClasses(String file) {
        List<String> classes = null;
        try {
            classes = new LinkedList<String>();
            Scanner sc = new Scanner(new FileInputStream(file));
            while (sc.hasNext()) {
                String line = sc.nextLine();
                String[] lines = line.split(";");
                for (int i = 0; i < lines.length; i++) {
                    line = lines[i].trim();
                    int start = line.indexOf("import ");
                    if (start == 0) {
                        String name = line.substring(7).trim();
                        classes.add(name);
                    }
                }

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return classes;

    }


    //递归查找所有java源文件
    public void getJavaFiles(List<String> javas, File dir) {
        if (dir.isFile() && dir.getName().endsWith(".java")) {
            javas.add(dir.getAbsolutePath());
            return;
        }
        if (dir.isDirectory()) {
            for (File f : dir.listFiles()) {
                getJavaFiles(javas, f);
            }
        }
    }

    //辅助类,存储了类名与import次数
    class Clazz implements Comparable {
        String className;
        int count;

        Clazz(String className, int count) {
            this.className = className;
            this.count = count;
        }

        public void add() {
            count++;
        }

        @Override
        public String toString() {
            return className + ": " + count + "次";
        }

        @Override
        public boolean equals(Object obj) {
            return this.className.equals(((Clazz) obj).className);
        }

        @Override
        public int hashCode() {
            return className.hashCode();
        }


        public int compareTo(Object o) {
            int diff = count - ((Clazz) o).count;
            if (diff != 0) {
                return 0 - diff;
            } else {
                return className.compareTo((((Clazz) o).className));
            }
        }
    }


    public static void main(String[] args) {
        String dir = "D:\\Docs\\projs\\IdeaProj\\campus2016\\src\\main\\java";
        String[] s = new CountMostImport().getTop10(dir);
        for (int i = 0; i < s.length; i++) {
            System.out.println(s[i]);
        }
    }
}
