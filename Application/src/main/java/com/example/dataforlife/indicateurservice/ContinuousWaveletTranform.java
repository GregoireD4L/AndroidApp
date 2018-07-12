package com.example.dataforlife.indicateurservice;

/**
 * Created by kokoghlanian  on 11/05/2018.
 */

public class ContinuousWaveletTranform {

    public double[] motherWavelet;
    public int width;

    public ContinuousWaveletTranform(int argwidth){
        width = argwidth;
        double mu = ((double) width)*3;
        motherWavelet = new double[width*10 + 1];
        double scaleFactor = 2./ Math.sqrt(3. * (double) width)/ Math.sqrt(Math.sqrt(Math.PI));
        for(int i = 0; i < width * 10 + 1; i++){
            motherWavelet[i] = scaleFactor*(1 - ((double)i - mu)*((double)i - mu)/((double)width)/((double)width))* Math.exp(-(((double)i - mu)*((double)i - mu)/2./((double)width)/((double)width)));
        }
    }

    public double convolution(DoubleQueue signal){
        double result = 0;
        for(int i = 0; i< signal.getSize(); i++){
            result += signal.getElement(i)*motherWavelet[i];
        } return result;

    }
}
