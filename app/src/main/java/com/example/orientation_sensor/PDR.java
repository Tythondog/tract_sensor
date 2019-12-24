package com.example.orientation_sensor; /**
* Author: TianYuan
* Time: 2019.10.30
* Function: 基于安卓手机的行人惯导
*
* 原理：代码分为三个部分：Step Detection, Orientation Estimation, Step Length Estimation，即步数检测，方向估计，
*      步长估计。若已知行人的步数，每一步的方向以及长度，在假设起点已知的情况下，那么行人的位置是可以根据这三项数据唯一确定下来的。
*      本算法就是通过这种方法来进行行人的定位。
*      1.Step Detection：
*           将原始加速度数据通过求模后减去均值，再送入带通滤波器进行滤波处理，最后通过峰值检测法获得峰值数，即为步数。
*      2.Orientation Estimation：
*           调用安卓内置的虚拟传感器Orientation Sensor直接获得航向角yaw信息，再根据上述步数划分结果对每一步内的yaw求
*       均值，将该均值作为这一步的总方向，参与最后的位置计算
*      3.Step Length Estimation：
*           使用经验公式：
*                   SL = 0.2844 + 0.2231*stepFreq + 0.0426*stepAV
*       式中stepFreq和stepAV分别为每一步acc数据的频率和方差信息
*
*      根据以上获得信息，计算每一步的坐标，并最后画出轨迹。
*
* */


import android.app.Activity;
import android.util.Log;

import com.example.orientation_sensor.myOwnPackage.biz.source_code.dsp.filter.FilterPassType;
import com.example.orientation_sensor.myOwnPackage.biz.source_code.dsp.filter.IirFilterCoefficients;
import com.example.orientation_sensor.myOwnPackage.biz.source_code.dsp.filter.IirFilterDesignExstrom;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class PDR  extends Activity {

    private static int fs = 50;
    private static double f1 = 0.75;       // 高低截止频率
    private static double f2 = 2.75;
    private static double[] wn = {f1/fs, f2/fs};
    private static int N = 4;
    private static double minPeakValue = 0.25;      // 对应matlab中findPeaks的参数0.25



    /**
     * 通过差分方程滤波
     * @param bArr 分子系数数组
     * @param aArr 分母系数数组
     * @param xArr 需要滤波的数组
     * @return 滤波之后的结果
     */
    public static double[] IIRFilter(double[] xArr, double[] bArr, double[] aArr){
        int lenB = bArr.length;
        int lenA = aArr.length;
        int lenX = xArr.length;
        int M = lenB - 1;
        int N = lenA - 1;
        double[] yArr = new double[lenX];
        for(int i = 0; i < lenX; i++){
            double yFront = 0;
            for(int j = 0; j <= M && j <= i; j++){
                yFront = yFront + bArr[j] * xArr[i - j];
            }
            double yBehind = 0;
            for(int j = 1; j <= N && j <= i; j++){
                yBehind = yBehind + aArr[j] * yArr[i - j];
            }
            yArr[i] = (yFront - yBehind) / aArr[0];
        }
        return yArr;
    }





    public static double[] filterConstruction(double[] accNorm){
        IirFilterCoefficients iirFilterCoefficients;        // iir滤波器参数对象
        // 输入设计滤波器的参数：滤波器类型，级数，低/高截止频率，得到滤波器系数iirFilterCoefficients(.a) / (.b)
        iirFilterCoefficients = IirFilterDesignExstrom.design(FilterPassType.bandpass, N, wn[0], wn[1]);    //
//        for (double each : iirFilterCoefficients.a){
//            System.out.println(each);
//        }
//        System.out.println("===========================");
//        for (double each : iirFilterCoefficients.b){
//            System.out.println(each);
//        }
//        System.out.println("===========================");
        accNorm = IIRFilter(accNorm, iirFilterCoefficients.b, iirFilterCoefficients.a);
        return accNorm;
    }


    // 返回acc数据求模并减去均值后的数组
    public static double[] calculateAccNorm(double[][] data){
        double sumValue = 0, meanValue;
        int dataLength = data.length;
        double[] accNorm = new double[dataLength];
        for (int i=0; i <dataLength; i++){
            accNorm[i] = Math.sqrt(Math.pow(data[i][0], 2) + Math.pow(data[i][1], 2) + Math.pow(data[i][2], 2));
            sumValue = sumValue + accNorm[i];
        }
        meanValue = sumValue / (dataLength);
        for (int i=0; i <dataLength; i++){
            accNorm[i] = accNorm[i] - meanValue;
        }

        return accNorm;



    }





    // 按照象限修正yaw的值，便于后续计算方向，第一列为原始值，第二列为sin值，第三列为cos值
    // 修正原则为：sin的一二象限为正，cos的一四象限为正
    public static double[][] convertYaw(double[] orientation){
        double[][] finalYaw = new double[3][orientation.length];
        for (int i=0; i<orientation.length; i++){
            double each = orientation[i];
            if (each >= 0 && each < 90){
                finalYaw[0][i] = each;
                finalYaw[1][i] = each;
                finalYaw[2][i] = each;
            }else if (each >= 90 && each < 180){
                finalYaw[0][i] = each;
                finalYaw[1][i] = 180 - each;
                finalYaw[2][i] = each;
            }else if (each >= 180 && each < 270){
                finalYaw[0][i] = each;
                finalYaw[1][i] = 180 - each;
                finalYaw[2][i] = 360 - each;
            }else if (each >= 270 && each <=360){
                finalYaw[0][i] = each;
                finalYaw[1][i] = each - 360;
                finalYaw[2][i] = 360 - each;
            }
        }
        return finalYaw;
    }


//    // 滤波后的数据送进来进行峰值查找，并且去除相隔比较近的假峰值，filteredAcc为加速度数据，index为时间轴
    public static double[][] findPeaks(double[] filteredAcc, long[] index){
        int dataLength = filteredAcc.length;
        int realPeakNums;
        List<Integer> peaksLocation = new ArrayList<Integer>();     // 三个变量分别是峰值时刻（在原数组中）的编号，数值大小，时间轴
        List<Double> peaksValue = new ArrayList<Double>();
        List<Long> peaksTime = new ArrayList<Long>();



        // 以下for循环实现与findpeaks(bandPass, 'MINPEAKHEIGHT', 0.25)相同的功能
        for (int i=1; i<dataLength-1; i++){
            if (filteredAcc[i] >= filteredAcc[i-1] && filteredAcc[i] >= filteredAcc[i+1]
                && filteredAcc[i] > minPeakValue){
                peaksLocation.add(i);
                peaksValue.add(filteredAcc[i]);
                peaksTime.add(index[i]);
            }
        }



        // 以下代码实现去除假峰值操作
        long[] timeDiffer = new long[peaksTime.size()];
     //   timeDiffer[0] = index[0];
        List<Integer> deleteIndex = new ArrayList<Integer>();
        for (int i=1; i<timeDiffer.length; i++){
            timeDiffer[i] = peaksTime.get(i) - peaksTime.get(i-1);
            if (timeDiffer[i] <= 400){
                if (peaksValue.get(i) <= peaksValue.get(i-1)){
                    deleteIndex.add(i);
                }
                else {
                    deleteIndex.add(i-1);
                }
            }
        }
        for (int i=deleteIndex.size()-1; i>=0; i--){
            int del = deleteIndex.get(i);
            peaksValue.remove(i);
            peaksLocation.remove(i);
            peaksTime.remove(i);
        }



        // 封装为数组便于返回
        realPeakNums = peaksValue.size();
        MainActivity.num_realPeakNums=realPeakNums;
        double[][] peaksInfo = new double[3][realPeakNums];
        for (int k=0; k<realPeakNums; k++){
            peaksInfo[0][k] = peaksLocation.get(k);
            peaksInfo[1][k] = peaksValue.get(k);
            peaksInfo[2][k] = timeDiffer[k];
        }
        return peaksInfo;
    }



    // 给定数组及其起点终点，计算均值
    public static double meanCalculation(int start, int end, double[] data){
        double sum = 0.0;
        for (int i=start; i<=end; i++){
            sum += data[i];
        }
        double mean = sum / (end - start + 1);
        return mean;
    }



    // 传入double[]以及起终点，计算方差值，公式：((x1-x')^2+...+(xn-x')^2) /(n-1)
    // 源代码中使用matlab的var函数，并且只传数组这一个参数，公式为/(n-1)而不是/n
    public static double varCalculation(int start, int end, double[] data){
        double var, mean, sumSquare=0;
        mean = meanCalculation(start, end, data);
        for (int i=start; i<=end; i++){
            sumSquare = sumSquare + Math.pow((data[i]-mean), 2);
        }
        var = sumSquare / (end - start);
        return var;
    }







    // string矩阵转置
    public static String[][] convertMatrix(String[][] matrix){
        int length, width;
        length = matrix.length;
        width = matrix[0].length;
        String [][] convertedMatrix = new String[width][length];
        for (int i=0; i<length; i++){
            for (int j=0; j<width; j++){
                convertedMatrix[j][i] = matrix[i][j];
            }
        }
        return convertedMatrix;
    }


    // string矩阵转置
    public static double[][] convertMatrix(double[][] matrix){
        int length, width;
        length = matrix.length;
        width = matrix[0].length;
        double [][] convertedMatrix = new double[width][length];
        for (int i=0; i<length; i++){
            for (int j=0; j<width; j++){
                convertedMatrix[j][i] = matrix[i][j];
            }
        }
        return convertedMatrix;
    }



    // String[][]转double[][]
    public static double[][] string2Double(String[][] strData){
        int length = strData.length;
        int width = strData[0].length;
        double[][] doubleData = new double[length][width];
        for (int i=0; i<length; i++){
            for (int j=0; j<width; j++){
                doubleData[i][j] = Double.parseDouble(strData[i][j]);
            }
        }
        return doubleData;
    }



    // String[][]转long[][]
    public static long[][] string2Long(String[][] strData){
        int length = strData.length;
        int width = strData[0].length;
        long[][] longData = new long[length][width];
        for (int i=0; i<length; i++){
            for (int j=0; j<width; j++){
                longData[i][j] = Long.parseLong(strData[i][j]);
            }
        }
        return longData;
    }



    // double[][]转double[]
    public static double[] arrayConvert(double[][] data){
        int length = data.length;
        double[] converted = new double[length];
        for (int i=0; i<length; i++){
            converted[i] = data[i][0];
        }
        return converted;
    }

    // long[][]转long[]
    public static long[] arrayConvert(long[][] data){
        int length = data.length;
        long[] converted = new long[length];
        for (int i=0; i<length; i++){
            converted[i] = data[i][0];
        }
        return converted;
    }



}
