/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package speedbagalg;

/**
 *
 * @author Barry Hannigan <support@miser-tech.com>
 */
public class Compressor
{
    private int total;
    //private double[] values;
    private int windowSize;
    //private int inIndex;
    private int numValues;
    boolean dataReady = false;

    public Compressor(int windowSize)
    {
        this.windowSize = windowSize;
        total = 0;
        numValues = 0;
        dataReady = false;
    }
    
    public void addSample(int sample)
    {
        if(!dataReady)
        {
            total += sample;
            numValues++;
        }
        if(numValues == windowSize)
            dataReady = true;
    }
    
    public int getCompressed()
    {
        int compressed = total;
        total = 0;
        numValues = 0;
        dataReady = false;
        return compressed;
    }
}
