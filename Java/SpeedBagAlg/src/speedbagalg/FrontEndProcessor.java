/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package speedbagalg;

import static java.lang.Math.abs;


class FEPFilterKernel
{
    public final static int[] filter_taps =
    {
      385,
      4901,
      -179,
      -871,
      -2538,
      -4539,
      -6613,
      -8423,
      -9651,
      55446,
      -9651,
      -8423,
      -6613,
      -4539,
      -2538,
      -871,
      -179,
      4901,
      385
    };
    public static int FILTER_TAP_NUM  = filter_taps.length;
    //int[] history = new int[FEPFILTER_TAP_NUM];
    AccelData[] history = new AccelData[FILTER_TAP_NUM];
    int last_index;
    FEPFilterKernel()
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
public class FrontEndProcessor
{
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

    // Window Average
    private int windowSize = 49;
    private int total;
    private int[] values;
    private int wrapIndex;
    private int numValues;

   
/*

FIR filter designed with
 http://t-filter.appspot.com

sampling frequency: 500 Hz

fixed point precision: 19 bits

* 0 Hz - 18 Hz
  gain = 0
  desired attenuation = -40 dB
  actual attenuation = n/a

* 50 Hz - 250 Hz
  gain = 1
  desired ripple = 5 dB
  actual ripple = n/a

*/


    public void FEPFilter_init(FEPFilterKernel f)
    {
        int i;
        for(i = 0; i < f.FILTER_TAP_NUM; ++i)
        {
            f.history[i].z = 0;
        }
        f.last_index = 0;
    }

    public void FEPFilter_put(FEPFilterKernel f, AccelData input)
    {
      f.history[f.last_index].z = input.z;
      f.history[f.last_index].timeStamp = input.timeStamp;
      f.last_index++;
      if(f.last_index == f.FILTER_TAP_NUM)
        f.last_index = 0;
    }

    public AccelData FEPFilter_get(FEPFilterKernel f)
    {
        int accZ = 0;
        int index = f.last_index, i;
        //System.out.println("NumTaps = "+ f.FEPFILTER_TAP_NUM+", last_index = "+f.last_index);
        for(i = 0; i < f.FILTER_TAP_NUM; ++i)
        {
            index = index != 0 ? index-1 : f.FILTER_TAP_NUM-1;
            accZ += (f.history[index].z * f.filter_taps[i]);
        }
        AccelData ret = new AccelData(f.history[f.last_index]);
        //System.out.println("accZ = "+accZ);
        ret.z = (accZ >> 16L);
        //System.out.println("retZ = "+ret.z);
        
        // Decimate by half
        ret.z = ret.z / 2;
        // Flip all values to positive
        ret.z = abs(ret.z);
        
        // Average the data some to smooth it
        avg_addDataPoint(ret.z);
        ret.z = avg_getAverage();
        
        // Return FEP filtered values
        return ret;
    }

    public FrontEndProcessor()
    {
        for (int i = 0; i < filterBuff.length; i++)
        {
            delayBuff[i] = new AccelData();
            filterBuff[i] = new AccelData();
        }
        values = new int[windowSize];
        numValues = 0;
        wrapIndex = 0;
    }
    
    private void avg_addDataPoint(int value)
    {
        if (numValues < values.length)
        {
            total = total + value;
            values[numValues] = value;
            numValues++;
        }
        else
        {
            total = total + (value - values[wrapIndex]);
            values[wrapIndex] = value;
            wrapIndex = (wrapIndex + 1) % values.length;
        }
    }
    
    private int avg_getAverage()
    {
        if (numValues < values.length)
            return total / numValues;
        else
            return total / values.length;
    }

}
