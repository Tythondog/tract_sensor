package com.example.orientation_sensor.myOwnPackage;

public class GenerateRandomData {

    public static void main(String[] args) {
//        double[][] sensorData = generateArray(100, 3, 15);
//        double[][] orientation = generateArray(100, 1, 360);
        double[][] fullData = generateData(100, 15, 360);
        for (int i=0; i<fullData.length; i++){
            for (int j=0; j<fullData[0].length; j++)
            {
                System.out.print(fullData[i][j] + "  ");
            }
            System.out.println("");
        }
    }

    public static int[] generateOrderedData(int lines){
        int num = 0;
        int[] numList = new int[lines];
        for (int i=0; i<lines; i++){
            numList[i] = num;
            num += 20;
        }
        return numList;
    }


    public static double[][] generateData(int lines, int dataMax, int orienMax){
        double[][] sensorData = generateArray(lines, 3, dataMax);
        double[][] orientationData = generateArray(lines, 1, orienMax);
        double[][] fullData = union2Arrays(sensorData, orientationData);
        return fullData;
    }


    public static double[] generateArray(int length, int max){
        double[] arr = new double[length];
        for(int i=0; i<length; i++){
                arr[i] = Math.random() * max;
            }
        return arr;
    }


    public static double[][] generateArray(int rows, int cols,int max){
        double[][] arr = new double[rows][cols];
        for(int i=0; i<rows; i++)
            for (int j=0; j<cols; j++){
                arr[i][j] = Math.random() * max;
        }
        return arr;
    }


    // 默认sensorData为n*3, orientationData为n*1,传入的n必须相等
    public static double[][] union2Arrays(double[][] sensorData, double[][] orientationData){
        int height = sensorData.length;
        int width1 = sensorData[0].length;
        int width2 = orientationData[0].length;
        int newWidth = width1 + width2;

        double[][] unionedData = new double[height][newWidth];
        for (int i=0; i<height; i++){
            for (int j=0; j<newWidth; j++){
                if (j >= width1){
                    int temp = j - width1;
                    unionedData[i][j] = orientationData[i][temp];
                } else {
                    unionedData[i][j] = sensorData[i][j];
                }
            }
        }
        return unionedData;
    }

}
