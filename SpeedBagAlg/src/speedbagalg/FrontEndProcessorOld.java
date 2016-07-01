/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package speedbagalg;

import static java.lang.Math.abs;

/**
 *
 * @author Barry Hannigan <support@miser-tech.com>
 */
public class FrontEndProcessorOld
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
    
    //Used Nate's algorithm
    float lastMagnitude = 0;
    float lastFirstPass = 0;
    float lastSecondPass = 0;
    float lastThirdPass = 0;
    long lastHitTime = 0;
    int secondsCounter = 0;
    AccelData accum;
   
    public FrontEndProcessorOld()
    {
        for (int i = 0; i < filterBuff.length; i++)
        {
            delayBuff[i] = new AccelData();
            filterBuff[i] = new AccelData();
        }
    }
    
    public float natesFilter(float currentMagnitude)
    {
        //This was found using a spreadsheet to view raw data and filter it
        final float WEIGHT = 0.9f;

        //Send this value through four (yes four) high pass filters
        float firstPass = currentMagnitude - (lastMagnitude * WEIGHT) - (currentMagnitude * (1 - WEIGHT));
        lastMagnitude = currentMagnitude; //Remember this for next time around

        float secondPass = firstPass - (lastFirstPass * WEIGHT) - (firstPass * (1 - WEIGHT));
        lastFirstPass = firstPass; //Remember this for next time around

        float thirdPass = secondPass - (lastSecondPass * WEIGHT) - (secondPass * (1 - WEIGHT));
        lastSecondPass = secondPass; //Remember this for next time around

        float fourthPass = thirdPass - (lastThirdPass * WEIGHT) - (thirdPass * (1 - WEIGHT));
        lastThirdPass = thirdPass; //Remember this for next time around
        //End high pass filtering

        fourthPass = abs(fourthPass); //Get the absolute value of this heavily filtered value

        return fourthPass;
    }

    private void runFilter()
    {
        accum = new AccelData();
        
        int cfIndex = inIndex - 1;
        for (int i = 0; i < orderSize; i++)
        {
            if (cfIndex < 0)
                cfIndex = orderSize - 1;
            int pfIndex = cfIndex - 1;
            if (pfIndex < 0)
            {
                pfIndex = orderSize - 1;
            }
            System.out.println("z = "+filterBuff[cfIndex].z+", "+filterBuff[pfIndex].z+" at "+cfIndex+","+pfIndex);
            //filterBuff[pfIndex].x = (filterBuff[cfIndex].x * fTerm) + (filterBuff[pfIndex].x * sTerm);
            //filterBuff[pfIndex].y = (filterBuff[cfIndex].y * fTerm) + (filterBuff[pfIndex].y * sTerm);
            //filterBuff[pfIndex].z = ((filterBuff[cfIndex].z * fTerm) + (filterBuff[pfIndex].z * sTerm));
            //filterBuff[pfIndex].z = filterBuff[pfIndex].z / lsbFactor;
//            accum.x += (filterBuff[cfIndex].x * coeff[i]);
//            accum.y += (filterBuff[cfIndex].y * coeff[i]);
            //accum.z += ((filterBuff[cfIndex].z * coeff[i]) / lsbFactor);
//            accum.z += (filterBuff[cfIndex].z * coeff[i]);
            //accum.z /= lsbFactor;
            //filterBuff[pfIndex].z = filterBuff[pfIndex].z / lsbFactor;
            //System.out.println("z["+pfIndex+"] = "+filterBuff[pfIndex].z);
            System.out.println("accumZ = "+accum.z);
            cfIndex = cfIndex - 1;
        }
/*        
        filterBuff[3].x = (sample.x * fTerm) + (filterBuff[3].x * sTerm);
        filterBuff[3].y = (sample.y * fTerm) + (filterBuff[3].y * sTerm);
        filterBuff[3].z = (sample.z * fTerm) + (filterBuff[3].z * sTerm);
        
        filterBuff[2].x = (filterBuff[3].x * fTerm) + (filterBuff[2].x * sTerm);
        filterBuff[2].y = (filterBuff[3].y * fTerm) + (filterBuff[2].y * sTerm);
        filterBuff[2].z = (filterBuff[3].z * fTerm) + (filterBuff[2].z * sTerm);
        
        filterBuff[1].x = (filterBuff[2].x * fTerm) + (filterBuff[1].x * sTerm);
        filterBuff[1].y = (filterBuff[2].y * fTerm) + (filterBuff[1].y * sTerm);
        filterBuff[1].z = (filterBuff[2].z * fTerm) + (filterBuff[1].z * sTerm);
        
        filterBuff[0].x = (filterBuff[1].x * fTerm) + (filterBuff[1].x * sTerm);
        filterBuff[0].y = (filterBuff[1].y * fTerm) + (filterBuff[1].y * sTerm);
        filterBuff[0].z = (filterBuff[1].z * fTerm) + (filterBuff[1].z * sTerm);
*/        
    }
    
    public AccelData FEP_addSample(AccelData sample)
    {
        AccelData res = null;
        
        // Scale to hold decimal val
        //sample.x *= lsbFactor;
        //sample.y *= lsbFactor;
        sample.z *= lsbFactor;
        delayBuff[inIndex] = new AccelData(sample);
        filterBuff[inIndex] = new AccelData(sample);
        inIndex = (inIndex + 1) % orderSize;
        if (needSample > 0)
        {
            needSample--;
            System.out.println("needSample = "+needSample);
        }
        else
        {
            runFilter();
            res = new AccelData(accum);
            outIndex = (outIndex + 1) % orderSize;
            res.x /= lsbFactor;
            res.y /= lsbFactor;
            res.z /= (lsbFactor);
        }
        return res;
    }

    public int FEP_getNextSample()
    {
        int val = delayBuff[outIndex].z;
        outIndex = (outIndex + 1) % orderSize;
        return val;
    }
}
