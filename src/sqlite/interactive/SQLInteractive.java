package sqlite.interactive;

import java.util.ArrayList;
//import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;

/**
 * Created by L1n on 17/3/20.
 * 负责与数据库交互的相关操作
 */
public class SQLInteractive {

    /*
     * 检查某一 pageId 是否存在于 LinkInDoc 之中
     */
    public boolean checkPageIdInLinkInDoc(String pageId) {
        boolean result;
        try {
            Connection dbCursor = connectDB();
            dbCursor.setAutoCommit(false);

            Statement stmt = dbCursor.createStatement();
            ResultSet rs = stmt.executeQuery(String.format("SELECT * FROM linkindoc where domain_id='%s';", pageId));
            if (rs.next()) {
                result = true;
            } else {
                result = false;
            }

            rs.close();
            stmt.close();
            dbCursor.close();
            return result;

        } catch (Exception e) {
            System.out.println(String.format("[*] 检查过程中发生了错误, %s:%s", e.getClass().getName(), e.getMessage()));
            return false;
        }
    }

    /*
     * 从表 LinkInDoc 中获取所有指向给定 pageId 的页面
     */
    public ArrayList<String> getAllInFromLinkInDoc(String pageId) {
        ArrayList<String> resultList = new ArrayList<>();

        try {
            Connection dbCursor = connectDB();
            dbCursor.setAutoCommit(false);

            Statement stmt = dbCursor.createStatement();
            ResultSet rs = stmt.executeQuery(String.format("SELECT distinct domain_id FROM linkindoc where out_id='%s';", pageId));
            while (rs.next()) {
                resultList.add(rs.getString("domain_id"));
            }
            rs = stmt.executeQuery(String.format("SELECT distinct in_id FROM linkindoc where domain_id='%s';", pageId));
            while (rs.next()) {
                String inID = rs.getString("in_id");
                if (inID != null) {
                    resultList.add(inID);
                }
            }

            rs.close();
            stmt.close();
            dbCursor.close();

        } catch (Exception e) {
            System.out.println(String.format("[*] 检查过程中发生了错误, %s:%s", e.getClass().getName(), e.getMessage()));
        } finally {
            return resultList;
        }
    }

    /*
     * 从表 LinkInDoc 中获取给定 pageId 指向的所有页面
     */
    public ArrayList<String> getAllOutFromLinkInDoc(String pageId) {
        ArrayList<String> resultList = new ArrayList<>();

        try {
            Connection dbCursor = connectDB();
            dbCursor.setAutoCommit(false);

            Statement stmt = dbCursor.createStatement();
            ResultSet rs = stmt.executeQuery(String.format("SELECT distinct domain_id FROM linkindoc where in_id='%s';", pageId));
            while (rs.next()) {
                resultList.add(rs.getString("domain_id"));
            }
            rs = stmt.executeQuery(String.format("SELECT distinct out_id FROM linkindoc where domain_id='%s';", pageId));
            while (rs.next()) {
                String out_id = rs.getString("out_id");
                if (out_id != null) {
                    resultList.add(out_id);
                }
            }

            rs.close();
            stmt.close();
            dbCursor.close();

        } catch (Exception e) {
            System.out.println(String.format("[*] 检查过程中发生了错误, %s:%s", e.getClass().getName(), e.getMessage()));
        } finally {
            return resultList;
        }
    }

    public ArrayList<String> getURLFromDomainID2URL(String domainId) {
        ArrayList<String> resultList = new ArrayList<>();

        try {
            Connection dbCursor = connectDB();
            dbCursor.setAutoCommit(false);

            Statement stmt = dbCursor.createStatement();
            ResultSet rs = stmt.executeQuery(String.format("select page_url from domainid2url where page_id='%s';", domainId));
            while (rs.next()) {
                resultList.add(rs.getString("page_url"));
            }

            rs.close();
            stmt.close();
            dbCursor.close();

        } catch (Exception e) {
            System.out.println(String.format("[*] 检查过程中发生了错误, %s:%s", e.getClass().getName(), e.getMessage()));
        } finally {
            return resultList;
        }
    }

    /*
     * 测试与数据库连接
     */
    public static void main(String args[]) throws Exception {
        SQLInteractive test = new SQLInteractive();
        if (test.checkPageIdInLinkInDoc("69713306c0bb3300")) {
            System.out.println("[*] 69713306c0bb3300 存在于 linkindoc 表中");
        }
        if (test.checkPageIdInLinkInDoc("123456")) {
            System.out.println("[*] 123456 存在于 linkindoc 表中");
        }

        ArrayList<String> allIdList = test.getAllInFromLinkInDoc("69713306c0bb3300");
        allIdList.forEach(each_id -> {
            System.out.println(String.format("[*] %s 指向了 69713306c0bb3300", each_id));
        });
        allIdList = test.getAllInFromLinkInDoc("aca5d9a362314a50");
        allIdList.forEach(each_id -> {
            System.out.println(String.format("[*] %s 指向了 aca5d9a362314a50", each_id));
        });


        allIdList = test.getAllOutFromLinkInDoc("15a13306c0bb3300");
        allIdList.forEach(each_id -> {
            System.out.println(String.format("[*] 15a13306c0bb3300 指向了 %s", each_id));
        });

        ArrayList<String> allURLList = test.getURLFromDomainID2URL("15a13306c0bb3300");
        String right_url = "http://club.comment2.news.sohu.com/read_art_sub.new.php?b=zz0150&a=6143&sr=8&allchildnum=20";
        String right_url2 = "http://club.comment2.news.sohu.com/newsmain.php?c=157&b=zz0150&t=0";
        allURLList.forEach(each_url -> {
            System.out.println(String.format("[*] 15a13306c0bb3300 的 URL 是 %s, 正确答案是 %s 和 %s", each_url, right_url, right_url2));
        });

    }

    public static Connection connectDB() throws Exception {
        Connection c = null;
        try {
            Class.forName("org.sqlite.JDBC");
            String dbPath = "/Users/L1n/Desktop/Notes/毕设/毕设实现/工程文件/link_relationship.db";
            c = DriverManager.getConnection(String.format("jdbc:sqlite:%s", dbPath));
//            System.out.println("[*] 成功打开数据库");
            return c;
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            throw e;
        }
    }
}
