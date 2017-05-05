package sqlite.interactive;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
//import java.sql.*;

import web.src.models.News;

/**
 * Created by L1n on 17/3/20.
 * 负责与数据库交互的相关操作
 */
public class SQLInteractive {
    private Connection dbCursor = null;    // 连接数据库用的
    private boolean domain_link_matrix[][] = new boolean[286][286];
    private String domain_id_list[] = new String[286];
    private String url_list[] = new String[286];

    public SQLInteractive() {
        startConnection();
        try {
            Statement stmt = dbCursor.createStatement();

            // 创建域名、URL 数组, 方便查找
            ResultSet rs = stmt.executeQuery(
                    String.format("SELECT order_id, page_id, page_url FROM domainid2urlindoc;")
            );
            while (rs.next()) {
                domain_id_list[rs.getInt("order_id") - 1] = rs.getString("page_id");
                url_list[rs.getInt("order_id") - 1] = rs.getString("page_url");
            }

            // 创建域名链接矩阵
            rs = stmt.executeQuery(
                    String.format("SELECT domain_id, out_id FROM linkindoc;")
            );
            while (rs.next()) {
                int x, y;
                x = Arrays.asList(domain_id_list).indexOf(rs.getString("domain_id"));
                y = Arrays.asList(domain_id_list).indexOf(rs.getString("out_id"));
                domain_link_matrix[x][y] = true;
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.out.println("[-] 创建域名 ID 的索引数组出错");
            System.out.println(e);
        } finally {
            closeConnection();
        }
    }

    /**
     * 给定两个网页, 判断这两个网页之间是否存在链接关系
     * TODO: 需要高度进行优化
     * 返回结果:
     * 0: 没有链接关系
     * 1: p 指向 q
     * 2: q 指向 p
     * 3: p 与 q 互相指向
     *
     * @param p 网页 p
     * @param q 网页 q
     * @return int, 0 or 1 or 2 o 3
     */
    public int checkPagesLinkRelationShip(News p, News q) {
        int checkResult = 0;
        String pId = p.getDomainID();
        String qId = q.getDomainID();


        // 查询一下 p 是否指向 q
        int x, y;
        x = Arrays.asList(domain_id_list).indexOf(pId);
        y = Arrays.asList(domain_id_list).indexOf(qId);
        if (domain_link_matrix[x][y]) {
            checkResult = checkResult ^ 1;
        }

        // 查询一下 q 是否指向 p
        if (domain_link_matrix[y][x]) {
            checkResult = checkResult ^ 2;
        }

        return checkResult;
    }

    /*
     * 关闭数据库连接
     */
    public void closeConnection() {
        try {
            dbCursor.close();
        } catch (Exception e) {
            System.out.println(String.format("[*] 数据库关闭失败, %s:%s", e.getClass().getName(), e.getMessage()));
        }
    }

    /*
     * 开始进行数据库操作
     */
    public void startConnection() {
        try {
            dbCursor = connectDB();
            dbCursor.setAutoCommit(false);
        } catch (Exception e) {
            System.out.println(String.format("[*] 数据库连接失败, %s:%s", e.getClass().getName(), e.getMessage()));
        }
    }

    /*
     * 检查某一 domainId 是否存在于 LinkInDoc 之中
     */
    public boolean checkDomainIdInLinkInDoc(String domainId) {

        int index = Arrays.asList(domain_id_list).indexOf(domainId);
        if (index > -1) {
            return true;
        } else {
            return false;
        }

    }

    /*
     * 从表 LinkInDoc 中获取所有指向给定 domainId 的页面
     */
    public ArrayList<String> getAllInFromLinkInDoc(String domainId) {
        ArrayList<String> resultList = new ArrayList<>();

        for (int i = 0; i < domain_link_matrix.length; ++i) {
            int index = Arrays.asList(domain_id_list).indexOf(domainId);
            if (domain_link_matrix[i][index]) {
                resultList.add(domain_id_list[i]);
            }
        }
        return resultList;
    }

    /*
     * 从表 LinkInDoc 中获取给定 domainId 指向的所有页面
     */
    public ArrayList<String> getAllOutFromLinkInDoc(String domainId) {
        ArrayList<String> resultList = new ArrayList<>();

        for (int i = 0; i < domain_link_matrix.length; ++i) {
            int index = Arrays.asList(domain_id_list).indexOf(domainId);
            if (domain_link_matrix[index][i]) {
                resultList.add(domain_id_list[i]);
            }
        }

        return resultList;
    }

    public ArrayList<String> getURLFromDomainID2URL(String domainId) {
        ArrayList<String> resultList = new ArrayList<>();

        int index = Arrays.asList(domain_id_list).indexOf(domainId);
        resultList.add(url_list[index]);

        return resultList;
    }

    /*
     * 测试与数据库连接
     */
    public static void main(String args[]) throws Exception {
        SQLInteractive test = new SQLInteractive();
        test.startConnection();

        if (test.checkDomainIdInLinkInDoc("69713306c0bb3300")) {
            System.out.println("[*] 69713306c0bb3300 存在于 linkindoc 表中");
        }
        if (test.checkDomainIdInLinkInDoc("123456")) {
            System.out.println("[*] 123456 存在于 linkindoc 表中");
        }

        ArrayList<String> allIdList = test.getAllInFromLinkInDoc("69713306c0bb3300");
        allIdList.forEach(each_id -> {
            System.out.println(String.format("[*] %s 指向了 69713306c0bb3300", each_id));
        });
        allIdList = test.getAllInFromLinkInDoc("be7f32f06cef7000");
        allIdList.forEach(each_id -> {
            System.out.println(String.format("[*] %s 指向了 be7f32f06cef7000", each_id));
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

        News a = new News("url", "content", "352f32f06cef7000", "title");
        News b = new News("url", "content", "261f32f06cef7000", "title");
        int checkResult = test.checkPagesLinkRelationShip(a, b);
        System.out.println(String.format("[*] 352f32f06cef7000 与 261f32f06cef7000 的链接关系为 %d", checkResult));

        a = new News("url", "content", "261f32f06cef7000", "title");
        b = new News("url", "content", "261f32f06cef7000", "title");
        checkResult = test.checkPagesLinkRelationShip(a, b);
        System.out.println(String.format("[*] 261f32f06cef7000 与 261f32f06cef7000 的链接关系为 %d", checkResult));

        a = new News("url", "content", "a975d9a362314a50", "title");
        b = new News("url", "content", "752f32f06cef7000", "title");
        checkResult = test.checkPagesLinkRelationShip(a, b);
        System.out.println(String.format("[*] a975d9a362314a50 与 752f32f06cef7000 的链接关系为 %d", checkResult));

        a = new News("url", "content", "ba6f32f06cef7000", "title");
        b = new News("url", "content", "672f32f06cef7000", "title");
        checkResult = test.checkPagesLinkRelationShip(a, b);
        System.out.println(String.format("[*] ba6f32f06cef7000 与 672f32f06cef7000 的链接关系为 %d", checkResult));

        test.closeConnection();

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
