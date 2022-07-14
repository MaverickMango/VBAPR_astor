package fr.inria.astor.util;

import java.util.List;

public class OracleEditDistance{

    public static int minDistance(List<String> sourceTokens, List<String> targetTokens){
        int sourceLen = sourceTokens.size();
        int targetLen = targetTokens.size();

        if(sourceLen == 0){
            return targetLen;
        }
        if(targetLen == 0){
            return sourceLen;
        }

        //定义矩阵(二维数组)
        int[][]  arr = new int[sourceLen+1][targetLen+1];

        for(int i=0; i < sourceLen+1; i++){
            arr[i][0] = i;
        }
        for(int j=0; j < targetLen+1; j++){
            arr[0][j] = j;
        }

        String sourceChar = null;
        String targetChar = null;

        for(int i=1; i < sourceLen+1 ; i++){
            sourceChar = sourceTokens.get(i-1);

            for(int j=1; j < targetLen+1 ; j++){
                targetChar = targetTokens.get(j-1);

                if(sourceChar.equals(targetChar)){
                    arr[i][j] = arr[i-1][j-1];
                }else{
                    arr[i][j] = (Math.min(Math.min(arr[i-1][j], arr[i][j-1]), arr[i-1][j-1])) + 1;
                }
            }
        }
        return arr[sourceLen][targetLen];
    }

    public static double getsimilarity(List<String> str1, List<String> str2){
        double distance = minDistance(str1,str2);
        double maxlen = Math.max(str1.size(),str2.size());
        if (maxlen == 0)
            return -1;
        double res = (maxlen - distance)/maxlen;

        //System.out.println("distance="+distance);
        //System.out.println("maxlen:"+maxlen);
        //System.out.println("(maxlen - distance):"+(maxlen - distance));
        return res;
    }

    public static String evaluate(List<String> str1, List<String> str2) {
        double result = getsimilarity(str1,str2);
        return String.valueOf(result);
    }

//    public static void main(String[] args) {
//        String str1 = "dataset==null";
//        String str2 = "epsilon==null";
//        int result = minDistance(str1, str2);
//        String res = evaluate(str1,str2);
//        System.out.println("最小编辑距离minDistance:"+result);
//        System.out.println(str1+"与"+str2+"相似度为："+res);
//
//    }

}