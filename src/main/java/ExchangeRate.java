import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * Created by fan on 2016/6/17.
 */
public class ExchangeRate {
    public static byte DOLLAR = 0;
    public static byte HK = 1;
    public static byte EURO = 1;

    public String[] currencies = {};

    public static String URL = "http://www.safe.gov.cn/AppStructured/view/project_RMBQuery.action";
    public static String FORM = "projectBean.startDate=2016-06-01&projectBean.endDate=2016-06-30&queryYN=true";

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 根据开始日期和结束日期之间的中间价网页
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 含有中间价表的html网页
     * @throws IOException
     */
    private Document getPageByDate(Calendar startDate, Calendar endDate) throws IOException {
        String startStr = DATE_FORMAT.format(startDate.getTime());
        String endStr = DATE_FORMAT.format(endDate.getTime());

        String formData = "projectBean.startDate=" + startStr + "&" +
                "projectBean.endDate" + endStr +
                "&queryYN=true";
        Document html = Jsoup.connect(URL + "?" + formData).get();
        return html;
    }

    private RatesTable parse(Document html) {
        Entities.EscapeMode.base.getMap().clear();
        Element table = html.getElementById("InfoTable");
        if (table == null) return null;

        Elements rows = table.select("tbody>tr");
        int rowCount = rows.size();
        //第一行为表头
        Elements heads = rows.get(0).select("th");
        int colCount = heads.size();

        //列名,每一列对应一种货币
        String[] colName = new String[colCount - 1];
        //行名,每一行对应一个日期
        Calendar[] rowName = new Calendar[rowCount - 1];

        //第一列为日期,因此从第二列开始
        for (int i = 1; i < colCount; i++) {
            String currency = heads.get(i).html().replace("&nbsp;", "").trim();
            colName[i - 1] = currency;
        }

        float[][] rates = new float[rowCount - 1][colCount - 1];


        //从第二行开始,遍历每一行数据
        for (int i = 1; i < rowCount; i++) {
            Element row = rows.get(i);
            Elements tds = row.select("td");
            Calendar date = Calendar.getInstance();
            try {
                //将Date类型的日期转换为Calendar
                date.setTime(DATE_FORMAT.parse(tds.get(0).html().replace("&nbsp;", "").trim()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            rowName[i - 1] = date;

            for (int j = 1; j < tds.size(); j++) {
                float r = 0f;
                try {
                    String str = tds.get(j).html().replace("&nbsp;", "").trim();
                    r = Float.parseFloat(str);
                } catch (NumberFormatException e) {
                    //e.printStackTrace();
                }
                rates[i - 1][j - 1] = r;
            }
        }
        RatesTable tab = new RatesTable(rowName, colName, rates);
        return tab;
    }

    private RatesTable getRatesTableByDate(Calendar startDate, Calendar endDate) throws IOException {
        Document doc = getPageByDate(startDate, endDate);
        return parse(doc);
    }

    public void getXLS(String fileName) throws IOException {
        Calendar today = Calendar.getInstance();
        Calendar startDay = Calendar.getInstance();
        startDay.add(Calendar.DAY_OF_MONTH, -30);
        RatesTable table = getRatesTableByDate(startDay, today);

        String[] title = {"日期", "美元", "英镑", "港元"};
        File file = new File(fileName);
        file.createNewFile();
        WritableWorkbook workbook = Workbook.createWorkbook(file);
        WritableSheet sheet = workbook.createSheet("sheet1", 0);
        try {
            //添加列名
            for (int i = 0; i < title.length; i++) {
                Label label = new Label(i, 0, title[i]);
                sheet.addCell(label);
            }
            //添加数据
            int row = 1;
            while (!startDay.after(today)) {
                float dollarMid = table.getRate(startDay, title[1]);
                float HKMid = table.getRate(startDay, title[2]);
                float poundMid = table.getRate(startDay, title[3]);

                if (dollarMid < 0 || HKMid < 0 || poundMid < 0) {

                } else {
                    sheet.addCell(new Label(0, row, DATE_FORMAT.format(startDay.getTime())));
                    sheet.addCell(new Label(1, row, String.valueOf(dollarMid)));
                    sheet.addCell(new Label(2, row, String.valueOf(poundMid)));
                    sheet.addCell(new Label(3, row, String.valueOf(HKMid)));
                    row++;
                }
                startDay.add(Calendar.DAY_OF_MONTH, 1);
            }
            workbook.write();
            workbook.close();
        } catch (WriteException e) {
            e.printStackTrace();
        }


    }

    public static void main(String[] args) throws ParseException {
        ExchangeRate er = new ExchangeRate();
        Calendar startDay = Calendar.getInstance();
        startDay.add(Calendar.DAY_OF_MONTH, -30);
        try {
            er.getXLS("D:\\Users\\fan\\Desktop\\1.xls");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     *
     */
    class RatesTable {
        public String[] cols;
        public Calendar[] rows;
        private float[][] data;

        public RatesTable(Calendar[] rows, String[] cols, float[][] data) {
            this.rows = rows;
            this.cols = cols;
            this.data = data;
        }

        public float getRate(Calendar date, String currency) {
            int row = 0;
            int col = 0;
            for (; row < rows.length; row++) {
                if (DATE_FORMAT.format(rows[row].getTime()).equals(DATE_FORMAT.format(date.getTime())))
                    break;
            }
            for (; col < cols.length; col++) {
                if (cols[col].equals(currency))
                    break;
            }
            if (row == rows.length)
                //当前日期没有数据,返回-1
                return -1f;
            else if (col == cols.length)
                //该货币无数据,返回0
                return 0f;
            else
                return data[row][col];
        }

        @Override
        public String toString() {
            StringBuilder str = new StringBuilder("日期");
            for (int i = 0; i < cols.length; i++) {
                str.append("\t" + cols[i]);
            }
            str.append("\n");
            for (int i = 0; i < rows.length; i++) {
                str.append(DATE_FORMAT.format(rows[i].getTime()));
                for (int j = 0; j < cols.length; j++) {
                    str.append("\t" + data[i][j]);
                }
                str.append("\n");
            }
            return str.toString();
        }
    }
}





