package org.kevoree.library.reasoner.ecj;

import java.util.ArrayList;
import java.util.List;

public class BrutForceSearch {

    public static int forestWidth = 4;
    public static boolean[][] booleanArray = {{false,false,false}, {false,false,true}, {false,true,false}, {false,true,true}, {true,false,false}, {true,false,true}, {true,true,false}, {true,true,true}};

    public boolean[][] myConfig = new boolean [forestWidth*forestWidth][3];
    public float bestFitness = 100;
    public static void main(String[] args) {
        BrutForceSearch bfs = new BrutForceSearch();
        bfs.search(forestWidth*forestWidth);
        System.out.println("For width = "+forestWidth+", BestFitness = " + bfs.bestFitness);
    }

    public void search(int remainingNodes){
        if (remainingNodes <= 0){
            float fitness = computeFitness(myConfig);
            if (fitness<bestFitness) {
                bestFitness = fitness;
                System.out.println("Fitness = " + fitness);
            }
            return;
        } else {
            for (int i=0;i<booleanArray.length;i++){
                myConfig[remainingNodes-1] = booleanArray[i];
                search(remainingNodes-1);
            }
        }
    }

    public float computeFitness(boolean[][] myBool){
        if (computePrecision(myBool)>0){
            return 100;
        } else {
            return computeCPU(myBool);
        }

    }

     private float computeCPU (boolean[][] myBool){
        double[] cpuConsumptions = new double[forestWidth*forestWidth];
        for (int i=0; i<cpuConsumptions.length; i++){
            int componentNumbers = 0;
            for (int j=0;j<myBool[i].length;j++){
                if (myBool[i][j])
                    componentNumbers++;
            }
            if (componentNumbers > 0)
                cpuConsumptions[i] = 40 + componentNumbers*20;
            else
                cpuConsumptions[i] = 0;
        }
        double result = 0;
        for (int i=0; i<cpuConsumptions.length; i++){
            result+=cpuConsumptions[i];
        }
        result = result / cpuConsumptions.length;
        return (float)(result);
    }

    public float computePrecision(boolean[][] myBool){
        double precision = 0.0;
        int size = forestWidth*forestWidth;
        Precision[]precisionArray = new Precision[size];

        for (int i=0; i<size;i++){
            precisionArray[i] = new Precision();
            if (myBool[i][0]){
                precisionArray[i].setTempPrecision(100.0);
            }
            if (myBool[i][1]){
                precisionArray[i].setSmokePrecision(100.0);
            }
            if (myBool[i][2]){
                precisionArray[i].setHumidityPrecision(100.0);
            }
        }
        for (int k=0; k<size; k++){
            if (!precisionArray[k].isCompleted()){
                Integer[] neighbours = MultiObjectiveKevoreeProblem.getNeighbours(k, size);
                for (int j=0;j<neighbours.length;j++){
                    if (myBool[neighbours[j]][0]){
                        precisionArray[k].setTempPrecision(precisionArray[k].getTempPrecision()+(100.0/8.0));
                    }
                    if (myBool[neighbours[j]][1]){
                        precisionArray[k].setSmokePrecision(precisionArray[k].getSmokePrecision() + (100.0/8.0));
                    }
                    if (myBool[neighbours[j]][2]){
                        precisionArray[k].setHumidityPrecision(precisionArray[k].getHumidityPrecision()+ (100.0/8.0));
                    }
                }
            }
        }

        for (int k=0; k<precisionArray.length;k++){
            precision +=precisionArray[k].getHumidityPrecision()+ precisionArray[k].getTempPrecision() + precisionArray[k].getSmokePrecision();
        }
        precision = precision / (precisionArray.length *3);

        return (float)(100-Math.floor(precision/10)*10);
    }
}
