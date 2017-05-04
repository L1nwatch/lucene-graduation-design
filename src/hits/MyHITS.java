package hits;

import web.src.models.News;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by L1n on 17/3/19.
 * 参考: http://blog.csdn.net/androidlushangderen/article/details/43311943#
 */
public class MyHITS {
    // 参与 HITS 计算的网页集
    private ArrayList<News> pagesList;
    private int pageNum;


    public MyHITS(ArrayList<News> pagesList) {
        this.pagesList = pagesList;
        this.pageNum = pagesList.size();
    }


    public static void main(String[] args) throws Exception {
        System.out.println("[*] 调用了 HITS 的 main 方法");
    }


    public ArrayList<News> sortByHITSResult(String choice) {
        if (choice.equals("Hub")) {
            // 按中心值排序
            Collections.sort(this.pagesList, Collections.reverseOrder(new Comparator<News>() {
                @Override
                public int compare(News o1, News o2) {
                    return Double.compare(o1.getHub(), o2.getHub());
                }
            }));
        } else if (choice.equals("Authority")) {
            // 开始按权威值排序
            Collections.sort(this.pagesList, Collections.reverseOrder(new Comparator<News>() {
                @Override
                public int compare(News o1, News o2) {
                    return Double.compare(o1.getAuthority(), o2.getAuthority());
                }
            }));
        }

        return this.pagesList;
    }


    /**
     * 【弃用】输出结果页面，也就是 authority 权威值最高的页面
     * 以下代码参考: http://blog.csdn.net/androidlushangderen/article/details/43311943
     */
    public ArrayList<News> hitsSort2(boolean[][] linkMatrix) {
        //误差值，用于收敛判断
        double error = Integer.MAX_VALUE;

        // 初始化 resultAuthority 和 resultHub, 默认值都为 1
        double[] resultAuthority = new double[pageNum];//网页Authority权威值
        double[] resultHub = new double[pageNum];//网页hub中心值
        Arrays.fill(resultAuthority, 1);
        Arrays.fill(resultHub, 1);

        while (error > 0.01 * pageNum) {
            // 迭代使用的权威值和中心值, 初始化为值 0
            double[] temp_authority = new double[pageNum];
            double[] temp_hub = new double[pageNum];

            // resultHub 和 resultAuthority 值的更新计算
            for (int i = 0; i < pageNum; i++) {
                for (int j = 0; j < pageNum; j++) {
                    // i 指向了 j
                    if (linkMatrix[i][j]) {
                        // 页面 p 的中心值 = 所有页面 p 指向的页面的权威值之和
                        temp_hub[i] += resultAuthority[j];
                        // 页面 p 的权威值 = 所有指向页面 p 的页面的中心值之和
                        temp_authority[j] += resultHub[i];
                    }
                }
            }

            // 求出最大 Hub 和 Authority
            double maxHub = Arrays.stream(temp_authority).max().getAsDouble();
            double maxAuthority = Arrays.stream(temp_hub).max().getAsDouble();

            // 归一化处理
            error = 0;
            for (int k = 0; k < pageNum; k++) {
                temp_authority[k] /= maxHub;
                temp_hub[k] /= maxAuthority;

                error += Math.abs(temp_authority[k] - resultHub[k]);

                resultHub[k] = temp_hub[k];
                resultAuthority[k] = temp_authority[k];
            }
        }

        System.out.println(String.format("[*] 最终收敛成功"));

        // 将计算得到的权威值、中心值保存到 news 之中
        for (int i = 0; i < resultAuthority.length; ++i) {
            this.pagesList.get(i).setAuthority(resultAuthority[i]);
            this.pagesList.get(i).setHub(resultHub[i]);
        }

        return sortByHITSResult("Authority");
    }

    /**
     * 输出结果页面，也就是 authority 权威值最高的页面
     * 以下代码参考 wiki: https://en.wikipedia.org/wiki/HITS_algorithm
     */
    public ArrayList<News> hitsSort(boolean[][] linkMatrix, String choice) {
        // 初始化 resultAuthority 和 resultHub, 默认值都为 1
        double[] resultAuthority = new double[pageNum];//网页Authority权威值
        double[] resultHub = new double[pageNum];//网页hub中心值
        Arrays.fill(resultAuthority, 1);
        Arrays.fill(resultHub, 1);

        // 进行 k 次, 论文中给出的 k 值为 20
        int p, q, r;
        for (int k = 0; k < 20; ++k) {
            double norm = 0.0;

            // 更新权威值
            for (p = 0; p < pageNum; ++p) {
                resultAuthority[p] = 0;
                for (q = 0; q < pageNum; ++q) {
                    // q 指向 p
                    if (linkMatrix[q][p]) {
                        resultAuthority[p] += resultHub[q];
                    }
                }
                norm += Math.pow(resultAuthority[p], 2);
            }
            norm = Math.sqrt(norm);
            for (p = 0; p < pageNum; ++p) {
                resultAuthority[p] = resultAuthority[p] / norm;
            }

            // 更新中心值
            norm = 0.0;
            Arrays.fill(resultHub, 0);
            for (p = 0; p < pageNum; ++p) {
                for (r = 0; r < pageNum; ++r) {
                    // p 指向 r
                    if (linkMatrix[p][r]) {
                        resultHub[p] += resultAuthority[r];
                    }
                }
                norm += Math.pow(resultHub[p], 2);
            }
            norm = Math.sqrt(norm);
            for (p = 0; p < pageNum; ++p) {
                resultHub[p] = resultHub[p] / norm;
            }
        }

        System.out.println(String.format("[*] 最终收敛成功"));

        // 将计算得到的权威值、中心值保存到 news 之中
        for (int i = 0; i < resultAuthority.length; ++i) {
            this.pagesList.get(i).setAuthority(resultAuthority[i]);
            this.pagesList.get(i).setHub(resultHub[i]);
        }

        return sortByHITSResult(choice);
    }
}