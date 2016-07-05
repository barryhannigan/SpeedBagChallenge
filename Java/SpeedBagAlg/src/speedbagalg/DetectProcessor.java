/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package speedbagalg;

import static java.lang.Math.abs;

/*
FIR filter designed with
 http://t-filter.appspot.com

sampling frequency: 500 Hz

fixed point precision: 16 bits

* 0 Hz - 19 Hz
  gain = 1
  desired ripple = 10 dB
  actual ripple = n/a

* 100 Hz - 250 Hz
  gain = 0
  desired attenuation = -40 dB
  actual attenuation = n/a

*/

class DETFilterKernel
{
    public final static int[] filter_taps =
    {
        -9,
        667,
        2340,
        5200,
        8662,
        11553,
        12684,
        11553,
        8662,
        5200,
        2340,
        667,
        -9
    };

    public final static int FILTER_TAP_NUM  = filter_taps.length ;
    //int[] history = new int[FEPFILTER_TAP_NUM];
    AccelData[] history = new AccelData[FILTER_TAP_NUM];
    int last_index;
    DETFilterKernel()
    {
        for (int i = 0; i < FILTER_TAP_NUM; i++)
        {
            history[i] = new AccelData();
        }
    }
} ;


/**
 *
 * @author Barry Hannigan <support@miser-tech.com>
 */
public class DetectProcessor
{
    /*
    private int orderSize = 9;
    private int buffSize = orderSize + 1;
    private int needSample = orderSize;
    private AccelData[] delayBuff = new AccelData[buffSize];
    private AccelData[] filterBuff = new AccelData[buffSize];
    private int inIndex = 0;
    private int outIndex = 0;
    private int lsbFactor = 1;
    private int fTerm = 10;
    private int sTerm = 90;
    */
    final static int DET_START_PEAK = 1;
    final static int DET_DETECT_PEAK = 2;
    final static int DET_START_MIN = 3;
    final static int DET_DETECT_MIN = 4;
    
    // Window Average
    private int windowSize = 99;
    private int total;
    private int[] values;
    private int wrapIndex;
    private int numValues;

    // Detector
    int lastMin = 0;
    int lastPeak = 200;
    int startMin = 0;
    int lastVal = 0;
    int detState = DET_START_PEAK;
    int lastStartTS = 0;
    int lastPeakTS = 0;
    int deltaPeak = 0;
    int PeakThreshold = 100;
    int MinThreshold = 10;
    
    private void detAlgorithm(AccelData sample)
    {
        int threshold = this.avg_getAverage();
        sample.avgZ = threshold;
        sample.y = 0;
        switch(detState)
        {
            case DET_START_PEAK:
                if (((sample.timeStamp - lastStartTS ) > 260) && (sample.z > threshold + (deltaPeak/10)) && sample.z > PeakThreshold)
                {
                    sample.y = 500;
                    lastStartTS = sample.timeStamp;
                    detState = DET_DETECT_PEAK;
                    deltaPeak = abs(sample.z - lastPeak);
                    lastPeak = sample.z;
                }
                break;
            case DET_DETECT_PEAK:
                if (sample.z > lastPeak)
                {
                    lastPeak = sample.z;
                    System.out.println("New Peak "+sample.z+" at "+sample.timeStamp+", delta = "+(sample.timeStamp - lastPeakTS));
                    lastPeakTS = sample.timeStamp;
                    //PeakThreshold = ((lastPeak) / 3);
                }
                else if (sample.z < threshold )
                {
                    startMin = sample.z;
                    detState = DET_START_MIN;
                }
                break;
            case DET_START_MIN:
                if (sample.z < (threshold - 50 ))
                {
                    lastMin = sample.z;
                    detState = DET_DETECT_MIN;
                }
                break;
            case DET_DETECT_MIN:
                if (sample.z < lastMin)
                {
                    lastMin = sample.z;
                }
                else
                {
                    if ((sample.z > (lastMin + 50)) || (sample.z < MinThreshold))
                    {
                        System.out.println("New Min "+sample.z+" at "+sample.timeStamp);
                        sample.x = 500;
                        detState = DET_START_PEAK;
                    }
                }
                break;
        }
    }
        
    public void DETFilter_init(DETFilterKernel f)
    {
        int i;
        for(i = 0; i < f.FILTER_TAP_NUM; ++i)
        {
            f.history[i].x = 0;
            f.history[i].y = 0;
            f.history[i].z = 0;
        }
        f.last_index = 0;
    }

    public void DETFilter_put(DETFilterKernel f, AccelData input)
    {
        // Decimate by half
        input.z = input.z / 2;
        
        // Now create Mag Squared value for filter input
        input.z = input.z * input.z;
        
        // Add the value to the next history buffer
        f.history[f.last_index].z = input.z;
        f.history[f.last_index].timeStamp = input.timeStamp;
        f.last_index++;
        if(f.last_index == f.FILTER_TAP_NUM)
            f.last_index = 0;
    }

    AccelData DETFilter_get(DETFilterKernel f)
    {
        int accZ = 0;
        int index = f.last_index, i;
        //System.out.println("NumTaps = "+ f.FEPFILTER_TAP_NUM+", last_index = "+f.last_index);
        for(i = 0; i < f.FILTER_TAP_NUM; ++i)
        {
            index = index != 0 ? index-1 : f.FILTER_TAP_NUM-1;
            accZ += f.history[index].z * f.filter_taps[i];
        }
        AccelData ret = new AccelData(f.history[f.last_index]);
        //System.out.println("accZ = "+accZ);
        ret.z = (int)(accZ >> 16L);
        //System.out.println("retZ = "+ret.z);
        ret.z = avg_addDataPoint(ret.z);
        detAlgorithm(ret);
        return ret;
    }

    public DetectProcessor()
    {
        values = new int[windowSize];
        numValues = 0;
        wrapIndex = 0;
    }
    
    private int avg_addDataPoint(int value)
    {
        int delayVal;
        if (numValues < values.length)
        {
            delayVal = values[numValues/2];
            total = total + value;
            values[numValues] = value;
            numValues++;
        }
        else
        {
            delayVal = values[(wrapIndex+(windowSize/2))%windowSize];
            total = total + (value - values[wrapIndex]);
            values[wrapIndex] = value;
            wrapIndex = (wrapIndex + 1) % values.length;
        }
        return delayVal;
    }
    
    private int avg_getAverage()
    {
        if (numValues < values.length)
            return total / numValues;
        else
            return total / values.length;
    }


}
