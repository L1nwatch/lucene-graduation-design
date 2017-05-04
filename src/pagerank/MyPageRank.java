package pagerank;

import web.src.models.News;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by L1n on 17/3/24.
 */
public class MyPageRank {
    private ArrayList<News> pagesList;
    private int pageNum;

    public MyPageRank(ArrayList<News> baseSet) {
        this.pagesList = baseSet;
        this.pageNum = baseSet.size();
    }

    /**
     * 自己的 HITS 算法采用的矩阵是 boolean 类型的, 现在要转成 Double 类型。。。。
     */
    public double[][] doubleMatrix(boolean[][] linkMatrix) {
        double[][] resultMatrix = new double[pageNum][pageNum];

        for (int i = 0; i < pageNum; ++i) {
            for (int j = 0; j < pageNum; ++j)
                if (linkMatrix[i][j]) {
                    resultMatrix[i][j] = 1;
                }
        }

        return resultMatrix;
    }

    /**
     * 将矩阵转置
     */
    private double[][] transferMatrix(double[][] linkMatrix) {
        for (double[] eachRow : linkMatrix) {
            // 计算页面链接个数
            int count = 0;
            for (double eachPage : eachRow) {
                if (eachPage == 1) {
                    ++count;
                }
            }

            // 按概率均分
            for (int i = 0; i < eachRow.length; i++) {
                if (eachRow[i] == 1) {
                    eachRow[i] /= count;
                }
            }
        }

        double t;
        // 将矩阵转置换，作为概率转移矩阵
        for (int i = 0; i < linkMatrix.length; i++) {
            for (int j = i + 1; j < linkMatrix[0].length; j++) {
                t = linkMatrix[i][j];
                linkMatrix[i][j] = linkMatrix[j][i];
                linkMatrix[j][i] = t;
            }
        }

        return linkMatrix;
    }


    /**
     * 按照 pageRank 值对网页进行排序
     *
     * @param pageRankVector: 每个网页的 pageRank 值
     * @return ArrayList<News>
     */
    private ArrayList<News> sortByPageRank(double[] pageRankVector) {
        // 将计算得到的权威值保存到 news 之中
        for (int i = 0; i < pageRankVector.length; ++i) {
            this.pagesList.get(i).setPageRank(pageRankVector[i]);
        }

        // 开始排序
        Collections.sort(this.pagesList, Collections.reverseOrder(new Comparator<News>() {
            @Override
            public int compare(News o1, News o2) {
                return Double.compare(o1.getPageRank(), o2.getPageRank());
            }
        }));
        return this.pagesList;
    }

    /**
     * 输出 pageRank 排序后的结果, 按 pageRank 值进行排序
     */
    public ArrayList<News> pageRankSort(double[][] linkMatrix) {
        linkMatrix = transferMatrix(linkMatrix);
        // 每个页面pageRank值初始向量
        double[] pageRankVector = new double[pageNum];
        Arrays.fill(pageRankVector, 1);  // 初始每个页面的pageRank值为1

        // 阻尼系数, wiki 的推荐数值
        double damp = 0.85;
        // 链接概率矩阵
        double[][] A = new double[pageNum][pageNum];
        double[][] e = new double[pageNum][pageNum];

        // 调用公式A=d*q+(1-d)*e/m，m为网页总个数,d就是damp
        double temp = (1 - damp) / pageNum;
        for (int i = 0; i < e.length; i++) {
            for (int j = 0; j < e[0].length; j++) {
                e[i][j] = temp;
            }
        }

        for (int i = 0; i < pageNum; i++) {
            for (int j = 0; j < pageNum; j++) {
                temp = damp * linkMatrix[i][j] + e[i][j];
                A[i][j] = temp;
            }
        }

        // 误差值，作为判断收敛标准
        double errorValue = Integer.MAX_VALUE;
        double[] newPRVector = new double[pageNum];

        // 当平均每个PR值误差小于0.001时就算达到收敛
        while (errorValue > 0.001 * pageNum) {
            for (int i = 0; i < pageNum; i++) {
                temp = 0;
                // 将A*pageRankVector,利用幂法求解,直到pageRankVector值收敛
                for (int j = 0; j < pageNum; j++) {
                    // temp就是每个网页到i页面的pageRank值
                    temp += A[i][j] * pageRankVector[j];
                }

                // 最后的temp就是i网页的总PageRank值
                newPRVector[i] = temp;
            }

            errorValue = 0;
            for (int i = 0; i < pageNum; i++) {
                errorValue += Math.abs(pageRankVector[i] - newPRVector[i]);
                // 新的向量代替旧的向量
                pageRankVector[i] = newPRVector[i];
            }
        }

        return sortByPageRank(pageRankVector);
    }
}
