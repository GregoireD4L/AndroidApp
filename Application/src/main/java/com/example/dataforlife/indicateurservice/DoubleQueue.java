package com.example.dataforlife.indicateurservice;

/**
 * Created by kokoghlanian on 11/05/2018.
 */

public class DoubleQueue {

    private double[] queue;
    private int firstElement;
    private int size;

    public int getFirstElement() {
        return firstElement;
    }

    public double[] getQueue() {
        return queue;
    }

    public void setQueue(double[] queue) {
        this.queue = queue;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public DoubleQueue(int argSize){
        size = argSize;
        queue = new double[size];
        firstElement = 0;

    }

    public void add(double arg){
        queue[firstElement] = arg;
        firstElement = (firstElement + 1)%size;
    }

    public double getElement(int i){
        return queue[(firstElement + i)%size];
    }

    public boolean previousIsMax(){
        return (((queue[(firstElement +size - 2)%size] - queue[(firstElement +size - 1)%size]) > 0)&((queue[(firstElement +size - 2)%size] - queue[(firstElement +size  - 3)%size]) >0));
    }

    public boolean previousIsMin(){
        return (((queue[(firstElement +size - 2)%size] - queue[(firstElement +size - 1)%size]) < 0)&((queue[(firstElement +size - 2)%size] - queue[(firstElement +size  - 3)%size]) <0));
    }

    public int getBPM(){
        double bPM = 0;
        for(int i = 1; i<size; i++){
            bPM += (queue[(firstElement + i)%size] - queue[(firstElement + i - 1 )%size])/(size-1);
        }
        bPM = 60000.0/bPM;
        return (int) bPM;

    }

    public void replaceLast(double arg){
        queue[(firstElement  + size - 1)%size] = arg;
    }

    public int getRespFreq(){
        double respFreq = 0;
        for(int i = 2; i<size; i += 2){
            respFreq += (queue[(firstElement + i)%size] - queue[(firstElement +i - 2)%size])/(size/2 - 1);
        }
        respFreq = 3000.0/respFreq;
        return (int) respFreq;
    }

    public double getMean(){
        double mean = 0;
        for(int i = 0; i< size; i++){
            mean += queue[i];
        }
        mean /= size;
        return mean;
    }

    public double getStd(){
        double std = 0;
        double mean = 0;
        for(int i = 0; i< size; i++){
            mean += queue[i];
        }
        mean /= size;
        double meanSqr = mean*mean;
        for(int i = 0; i< size; i++){
            std += queue[i]*queue[i] - meanSqr;
        }
        std /= size;
        std = Math.sqrt(std);
        return std;
    }

    public double getStdWithMean(double mean){
        double std = 0;
        double meanSqr = mean*mean;
        for(int i = 0; i < size; i++){
            std += queue[i]*queue[i] - meanSqr;
        }
        std /= size;
        std = Math.sqrt(std);
        return std;
    }
}
