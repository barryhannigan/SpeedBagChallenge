/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package speedbagalg;

import java.io.File;
import static java.lang.Math.abs;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Barry Hannigan <support@miser-tech.com>
 */
public class MainLoop implements Runnable
{
    private String fileName;
    private File file;
    private final int timeScale = 1;
    private boolean stop = false;
    OscopeView view;
    
    MainLoop(String fname, OscopeView view)
    {
        fileName = fname;
        this.view = view;
    }
    
    private void Sleep(int mills)
    {
        try
        {
            Thread.sleep(mills);
        } catch (InterruptedException ex)
        {
            Logger.getLogger(SpeedBagAlg.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void stop()
    {
        stop = true;
    }
    
    public void resume()
    {
        stop = false;
    }
    
    @Override
    public void run()
    {
        file = new File(fileName);
        if(!file.exists())
        {
            System.out.println("File "+fileName+" NOT found");
            return;
        }
        //runF1();
        //runF2();
        //runF3();
        //runF4();
        //runF5();
        runF6();
        view.simulationComplete();
    }

    public void runF6()
    {
        //SlidingWindow avgZ = new SlidingWindow(99);
        SlidingWindow avgZ = new SlidingWindow(49);
        int hitCount = 0;
        int lastHitTime = 0;
        boolean hitFlag = false;
        FrontEndProcessor fep = new FrontEndProcessor();
        DetectProcessor det = new DetectProcessor();
        FEPFilterKernel fepKernel = new FEPFilterKernel();
        DETFilterKernel detKernel = new DETFilterKernel();
        fep.FEPFilter_init(fepKernel);
        det.DETFilter_init(detKernel);
        AccelData accData = new AccelData();
        
        long lastMill = System.currentTimeMillis();
        long lastTs = accData.timeStamp;
        
        System.out.println("Running F6!!!");
        MMA8452 accel = new MMA8452(file);
        
        // Read initial value
        accel.getAccelData(accData);
        AccelData dummyData;
        for (int i = 0; i < FEPFilterKernel.FILTER_TAP_NUM; i++)
        {
            System.out.println("Priming Filter");
            dummyData = new AccelData(accData);
            fep.FEPFilter_put(fepKernel, accData);
        }
        for (int i = 0; i < DETFilterKernel.FILTER_TAP_NUM; i++)
        {
            System.out.println("Priming Filter");
            dummyData = new AccelData(accData);
            fep.FEPFilter_put(fepKernel, accData);
            det.DETFilter_put(detKernel, fep.FEPFilter_get(fepKernel));
        }
        while(accel.valid)
        {
            AccelData newFepData = new AccelData(accData);
            fep.FEPFilter_put(fepKernel, newFepData);
            AccelData filterData = fep.FEPFilter_get(fepKernel);

            AccelData detDataIn = new AccelData(filterData);
            det.DETFilter_put(detKernel, detDataIn);
            AccelData detData = det.DETFilter_get(detKernel);
            if(detData.y > 0)
            {
                hitCount++;
                view.updateHitCount(hitCount);
            }
            while(this.stop)
                Sleep(500);

            // Wait until its time to apply
            long deltaTs = accData.timeStamp - lastTs;
            long waitMill = lastMill + (deltaTs * timeScale);
            while(System.currentTimeMillis() < waitMill)
                Sleep(0);
            lastTs = accData.timeStamp;
            lastMill = System.currentTimeMillis();

            view.plotValues(detData);
            accel.getAccelData(accData);
        }
        System.out.println("Total Hits: "+hitCount);
        System.out.println("Simulation Complete!!!");        
    }
    
    public void runF5()
    {
        FrontEndProcessorOld fep = new FrontEndProcessorOld();
        
        int avgWindow = 10;
        DelayBuff<AccelData> delayData = new DelayBuff(2);
        DelayBuff<AccelData> delayData2 = new DelayBuff(2);
        DelayBuff<AccelData> delayData3 = new DelayBuff(2);
        DelayBuff<AccelData> delayData4 = new DelayBuff(2);
        SlidingWindow avgZ = new SlidingWindow(500);
        
        AccelData accData = new AccelData();
        int hitCount = 0;
        int lastHitTime = 0;
        boolean hitFlag = false;
        int testCount = 0;
        long lastMill = System.currentTimeMillis();
        long lastTs = accData.timeStamp;
        
        System.out.println("Running F5!!!");
        MMA8452 accel = new MMA8452(file);
        
        // Read initial value
        accel.getAccelData(accData);
        int peakVal = 1;
        int peakNoise = 1;
        int peakTs = (int)lastTs;
        int lastPeakTs = peakTs;
        int freak = 150;
        float scaler = 6f;
        //float beta = 6.25f;
        float beta = 1.0f;
        float ampResponse = 0.1f;
        //float beta = 4.0f;
        boolean gateOpen = false;
        int peakCount = 0;

        int lsbVal = 1000;
        float alpha = 0.9f;
        int fTerm = (int)((1.0f - alpha) * (float)lsbVal);
        int sTerm = (int)(alpha * (float)lsbVal);
        System.out.println("fTerm = "+fTerm+", sTerm = "+sTerm);
        delayData.addSample(accData);
        //delayData2.addSample(accData);
        //delayData3.addSample(accData);
        //delayData4.addSample(accData);
        while(accel.valid)
        {
            // Get a copy of the data (Won't need in Micro C code)
            AccelData newAccData = new AccelData(accData);
            AccelData newFepData = new AccelData(accData);
            //float nVal = (int)fep.natesFilter(newFepData.z);
            AccelData fepData = fep.FEP_addSample(newFepData);
            newAccData.z = newAccData.z * lsbVal;
            newAccData.y = newAccData.z;
            avgZ.addDataPoint(newAccData.y);
            delayData.addSample(newAccData);
            AccelData dAccData = delayData.getNextSample();
            
            if (dAccData != null)
            {
                newAccData.x = (newAccData.x * 990) + (dAccData.x * 10);
                //dAccData.y = (int)avgZ.getAverage();
                //newAccData.y = (newAccData.y * 500) + (dAccData.y * 500);
                newAccData.z = (newAccData.z * fTerm) + (dAccData.z * sTerm);
                newAccData.x /= 1000;
                //newAccData.y /= 1000;
                newAccData.z /= lsbVal;
            }
            else
                System.out.println("Filling Pipe1!");

            delayData2.addSample(dAccData);
            AccelData dAccData2 = delayData2.getNextSample();
            
            if (dAccData2 != null)
            {
                dAccData.x = (dAccData.x * 990) + (dAccData2.x * 10);
                //dAccData.y = (int)avgZ.getAverage();
                //newAccData.y = (newAccData.y * 500) + (dAccData.y * 500);
                dAccData.z = (dAccData.z * fTerm) + (dAccData2.z * sTerm);
                dAccData.x /= 1000;
                //newAccData.y /= 1000;
                dAccData.z /= 1000;
            }
            else
                System.out.println("Filling Pipe2!");

            delayData3.addSample(dAccData2);
            AccelData dAccData3 = delayData3.getNextSample();
            
            if (dAccData3 != null)
            {
                dAccData2.y = dAccData2.z;
                //dAccData2.x = (dAccData2.x * 990) + (dAccData3.x * 10);
                //dAccData.y = (int)avgZ.getAverage();
                //newAccData.y = (newAccData.y * 500) + (dAccData.y * 500);
                dAccData2.z = (dAccData2.z * fTerm) + (dAccData3.z * sTerm);
                //newAccData.y /= 1000;
                dAccData2.z /= lsbVal;
            }
            else
                System.out.println("Filling Pipe3!");

            delayData4.addSample(dAccData3);
            AccelData dAccData4 = delayData4.getNextSample();
            
            if (dAccData4 != null)
            {
                dAccData3.x = dAccData3.z;
                //dAccData3.x = (dAccData3.x * 990) + (dAccData4.x * 10);
                //dAccData.y = (int)avgZ.getAverage();
                //newAccData.y = (newAccData.y * 500) + (dAccData.y * 500);
                dAccData3.z = (dAccData3.z * fTerm) + (dAccData4.z * sTerm);
                //newAccData.y /= 1000;
                dAccData3.z /= lsbVal;
            }
            else
                System.out.println("Filling Pipe4!");

            if(dAccData4 != null)
            {
                dAccData4.z /= lsbVal;
                dAccData4.y /= lsbVal;
                dAccData4.x /= lsbVal;

                //System.out.println("Nate's value = "+nVal);
                //dAccData4.y = (int)nVal;
                if(fepData != null)
                    dAccData4.y = fepData.z;
                else
                    dAccData4.y = 0;
            }
            while(this.stop)
                Sleep(500);
            
            // Wait until its time to apply
            long deltaTs = accData.timeStamp - lastTs;
            long waitMill = lastMill + (deltaTs * timeScale);
            while(System.currentTimeMillis() < waitMill)
                Sleep(0);
            lastTs = accData.timeStamp;
            lastMill = System.currentTimeMillis();

/*            int deltaTime = dAccData.timeStamp - lastHitTime;
         
            //int curSample = dAccData.x;
            int curSample = dAccData2.y;
            
            if(curSample > (5000))
                hitCount++;
*/      
            // Plot the value
            if(dAccData4 != null)
                view.plotValues(dAccData4);
            accel.getAccelData(accData);
        }
        System.out.println("Total Hits: "+hitCount);
        System.out.println("Test Count: "+testCount);
        System.out.println("Simulation Complete!!!");
        
    }
    public void runF4()
    {
        final int avgWindow = 50;
        final int delayTime = avgWindow;
        final int lsbScale  = 100;
        final int initialDec = 150;
        int decimator = initialDec;
        final int secWindow = 11;
        SlidingWindow avgX = new SlidingWindow(avgWindow);
        SlidingWindow avgY = new SlidingWindow(avgWindow);
        SlidingWindow avgZ = new SlidingWindow(avgWindow);
        SlidingWindow secSmooth = new SlidingWindow(secWindow);
        SlidingWindow pcData = new SlidingWindow(delayTime+3);
        SlidingWindow peakAvg = new SlidingWindow(3);
        DelayBuff<AccelData> delayData = new DelayBuff(avgWindow/2);
        DelayBuff<AccelData> delayData2 = new DelayBuff(secWindow/2);
        final int compWidth = 100;
        Compressor dComp = new Compressor(compWidth);

        AccelData accData = new AccelData();
        int hitCount = 0;
        int lastHitTime = 0;
        boolean hitFlag = false;
        int testCount = 0;
        long lastMill = System.currentTimeMillis();
        long lastTs = accData.timeStamp;
        
        System.out.println("Running F4!!!");
        MMA8452 accel = new MMA8452(file);
        
        // Read initial value
        accel.getAccelData(accData);
        int peakVal = 1;
        int peakNoise = 1;
        int peakTs = (int)lastTs;
        int lastPeakTs = peakTs;
        int freak = 150;
        float scaler = 6f;
        //float beta = 6.25f;
        float beta = 1.0f;
        float ampResponse = 0.1f;
        //float beta = 4.0f;
        boolean gateOpen = false;
        int peakCount = 0;
        
        while(accel.valid)
        {
            // Get a copy of the data (Won't need in Micro C code)
            AccelData newAccData = new AccelData(accData);
            
            //************************************************
            // Front End Processor
            avgX.addDataPoint(newAccData.x);
            avgY.addDataPoint(newAccData.y);
            avgZ.addDataPoint(newAccData.z);
            delayData.addSample(newAccData);
            
            // Move data from First Stage to Second Stage
            AccelData dAccData = delayData.getNextSample();
            
            // If delay pipe not filled yet, continue filling First Stage
            if(dAccData == null)
            {
                System.out.println("Filling Pipe!");
                continue;
            }
            
            // Calculate Averages and remove DC Bias
            dAccData.avgX = (int)avgX.getAverage();
            dAccData.avgY = (int)avgY.getAverage();
            dAccData.avgZ = (int)avgZ.getAverage();
            
            dAccData.z = dAccData.z - dAccData.avgZ;
            dAccData.x = dAccData.x - dAccData.avgX;
            dAccData.y = dAccData.y - dAccData.avgY;
            
            //dAccData.x = abs(dAccData.x);
            //dAccData.y = abs(dAccData.y);
            //dAccData.z = abs(dAccData.z);
            dAccData.avgZ = abs(dAccData.avgZ);
            
            // Scale to Fixed point LSB while decimating
            dAccData.x *= dAccData.x;
            dAccData.z *= dAccData.z;
            dAccData.x = dAccData.x / (decimator/lsbScale);
            dAccData.y = dAccData.y / (decimator/lsbScale);
            dAccData.z = dAccData.z / (decimator/lsbScale);
            
            dAccData.z = dAccData.z + (10 * dAccData.x);
            //dAccData.z *= dAccData.z;
            dAccData.z = dAccData.z/lsbScale; // Remove effects of square
            
            delayData2.addSample(dAccData);
            secSmooth.addDataPoint(dAccData.z);

            AccelData dAccData2 = delayData2.getNextSample();
            if(dAccData2 == null)
            {
                System.out.println("Filling Pipe2!");
                continue;
            }
            
            dAccData2.x = (int)secSmooth.getAverage();
            //System.out.println("SecAvg = "+secSmooth.getAverage());
            int deltaPeak = dAccData2.timeStamp - lastPeakTs;
            if ((deltaPeak > freak) && (dAccData2.x > (100)))
            {
                if(gateOpen == false)
                {
                    gateOpen = true;
                    System.out.println("Gate Open at "+dAccData2.timeStamp+" amplitude = "+dAccData2.x);
                    System.out.println("Decimator = "+decimator);
                    peakTs = dAccData2.timeStamp;
                }
            }

            if (gateOpen)
            {
                //System.out.println("Adding dComp Sample");
                dComp.addSample(dAccData2.z);

                if(dAccData2.x > peakVal )
                {
                    peakVal = dAccData2.x;
                    peakTs = dAccData2.timeStamp;
                    peakCount++;
                }
                
                if(dComp.dataReady)
                {
                    dAccData2.y = dComp.getCompressed();
                    gateOpen = false;
                    System.out.println("Gate Closed at "+dAccData2.timeStamp+" peakVal = "+peakVal);
                    peakAvg.addDataPoint(peakVal);
                    decimator = initialDec + (((int)peakAvg.getAverage()/1050)*100);
                    int newFreak = (peakTs - lastPeakTs) - compWidth;
                    if((newFreak < 175) && (newFreak > 125))
                        freak = newFreak;
                    System.out.println("New Freak = "+newFreak+" Freq = "+freak);
                    System.out.println("Peak Count = "+peakCount);
                    peakCount = 0;
                    peakVal = 0;
                    lastPeakTs = peakTs;
                    //lastPeakTs = dAccData2.timeStamp;
                    //pcData.addDataPoint(dAccData.y);
                }
                else
                    dAccData2.y = 0;
            }
            else
                dAccData2.y = 0;

            while(this.stop)
                Sleep(500);
            
            // Wait until its time to apply
            long deltaTs = accData.timeStamp - lastTs;
            long waitMill = lastMill + (deltaTs * timeScale);
            while(System.currentTimeMillis() < waitMill)
                Sleep(0);
            lastTs = accData.timeStamp;
            lastMill = System.currentTimeMillis();

            int deltaTime = dAccData.timeStamp - lastHitTime;
         
            //int curSample = dAccData.x;
            int curSample = dAccData2.y;
            
            if(curSample > (5000))
                hitCount++;
        
            // Plot the value
            view.plotValues(dAccData2);
            accel.getAccelData(accData);
        }
        System.out.println("Total Hits: "+hitCount);
        System.out.println("Test Count: "+testCount);
        System.out.println("Simulation Complete!!!");
    }
    
    public void runF3()
    {
        final int avgWindow = 10;
        final int delayTime = avgWindow;
        SlidingWindow avgX = new SlidingWindow(avgWindow);
        SlidingWindow avgY = new SlidingWindow(avgWindow);
        SlidingWindow avgZ = new SlidingWindow(avgWindow);
        SlidingWindow pcData = new SlidingWindow(delayTime+3);
        SlidingWindow peakAvg = new SlidingWindow(10);
        //BlockingQueue<AccelData> filterData = new ArrayBlockingQueue<>(delayTime);
        DelayBuff<AccelData> delayData = new DelayBuff(avgWindow/2);
        AccelData accData = new AccelData();
        int hitCount = 0;
        int lastHitTime = 0;
        boolean hitFlag = false;
        int testCount = 0;
        
        System.out.println("Running F3!!!");
        MMA8452 accel = new MMA8452(file);
        
        Compressor dComp = new Compressor(50);
        
        // Read initial value
        accel.getAccelData(accData);
        long lastMill = System.currentTimeMillis();
        long lastTs = accData.timeStamp;
        int peakVal = 1;
        int peakNoise = 1;
        int peakTs = (int)lastTs;
        int lastPeakTs = peakTs;
        int freak = 250;
        float scaler = 6f;
        //float beta = 6.25f;
        float beta = 1.0f;
        float ampResponse = 0.1f;
        //float beta = 4.0f;
        while(accel.valid)
        {
            // Get a copy of the data (Won't need in Micro C code)
            AccelData fAccData = new AccelData(accData);

            // Input into first stage
            avgX.addDataPoint(fAccData.x);
            avgY.addDataPoint(fAccData.y);
            avgZ.addDataPoint(fAccData.z);
            fAccData.avgX = (int)avgX.getAverage();
            fAccData.avgY = (int)avgY.getAverage();
            fAccData.avgZ = (int)avgZ.getAverage();
            delayData.addSample(fAccData);
            
            // Move data from First Stage to Second Stage
            AccelData dAccData = delayData.getNextSample();
            // If delay pipe not filled yet, continue filling First Stage
            if(dAccData == null)
            {
                System.out.println("Filling Pipe!");
                continue;
            }
            
            // Remove noise floor / bias, clamp at 0
            int xNoiseFloor = (int)(avgX.getAverage());
            int yNoiseFloor = (int)(avgY.getAverage());
            int zNoiseFloor = (int)(avgZ.getAverage());
            dAccData.x = dAccData.x - xNoiseFloor;
            dAccData.y = dAccData.y - yNoiseFloor;
            dAccData.z = dAccData.z - zNoiseFloor;
            
            //dAccData.x = (int)Math.pow(dAccData.x, 2);
            //dAccData.y = (int)Math.pow(dAccData.y, 2);
            //dAccData.z = (int)Math.pow(dAccData.z, 2);
            //dComp.addSample(abs(dAccData.z));
            //dComp.addSample(dAccData.z);
            dAccData.z = abs(dAccData.z) + abs(dAccData.x);
            pcData.addDataPoint(dAccData.z);
            //dAccData.z = dAccData.z + dAccData.y + dAccData.x;
            //dAccData.z = abs(dAccData.z) + (abs(dAccData.y) + abs(dAccData.x));
            //dAccData.z = (int) (abs(dAccData.z) + (Math.sqrt(Math.pow((double)dAccData.y,2.0) + Math.pow((double)dAccData.x,2.0)) ));
            //dAccData.z = (int) (abs(dAccData.z) + (Math.sqrt(Math.pow(dAccData.z, 2) + Math.pow((double)dAccData.y,2.0) + Math.pow((double)dAccData.x,2.0)) ));
            //dAccData.z = (int)Math.sqrt(dAccData.x + dAccData.y + dAccData.z);
            dComp.addSample((int)(float)dAccData.z);
            zNoiseFloor = abs(zNoiseFloor);
            dAccData.avgZ = zNoiseFloor;
            //if(dAccData.z < 0)
            //    dAccData.z = 0;
            //pcData.addDataPoint(dAccData.z * beta);
            //pcData.addDataPoint(dAccData.z);
            //dAccData.x = (int)(pcData.getAverage());
            //dAccData.y = (int)(pcData.getSum());
            //dAccData.y = (int)(pcData.getAverage());
            if(dComp.dataReady)
            {
                dAccData.y = dComp.getCompressed();
                //pcData.addDataPoint(dAccData.y);
            }
            //else
                //dAccData.y = 0;
            if(dAccData.y > 1000)
                testCount++;
            
            //dAccData.x = (int)(pcData.getAverage());
            //if(dAccData.x != 0)
            //    System.out.println("Ratio: "+dAccData.y );
            
            // Check if paused...
            while(this.stop)
                Sleep(500);
            
            // Wait until its time to apply
            long deltaTs = accData.timeStamp - lastTs;
            long waitMill = lastMill + (deltaTs * timeScale);
            while(System.currentTimeMillis() < waitMill)
                Sleep(1);
            lastTs = accData.timeStamp;
            lastMill = System.currentTimeMillis();

            int deltaTime = dAccData.timeStamp - lastHitTime;
            //int curSample = dAccData.x;
            int curSample = dAccData.y;
            //int curSample = dAccData.z;
            if( curSample > (zNoiseFloor*scaler))
            {
                //if(deltaTime > 250)
                //if(deltaTime > 33)
                if(deltaTime > freak)
                {
                    //hitCount++;
                    System.out.println("Gate Open: "+hitCount+" tsDelta: "+deltaTime+" curSample = "+curSample+", zVal = "+dAccData.z+", xVal = "+dAccData.x+", noise = "+zNoiseFloor);
                    System.out.println("  timestamp: "+dAccData.timeStamp);
                    hitFlag = true;
                    if(curSample > peakVal)
                    {
                        peakVal = curSample;
                        peakNoise = zNoiseFloor;
                        peakTs = dAccData.timeStamp;
                    }
                }
                else
                {
                    System.out.println("Filtered Out: tsDelta: "+deltaTime+" curSample = "+curSample+", zVal = "+dAccData.z+", xVal = "+dAccData.x+", noise = "+zNoiseFloor);
                    if(curSample > peakVal)
                    {
                        peakVal = curSample;
                        peakNoise = zNoiseFloor;
                        peakTs = dAccData.timeStamp;
                    }
                }
            }
            else
            {
                if(hitFlag)
                {
                    //if(curSample < (noiseFloor - 400))
                    //if(curSample < (noiseFloor - 700))
                    if(curSample < ((zNoiseFloor*scaler) * 0.3))
                    {
                        System.out.println("GateClosed: tsDelta: "+deltaTime+", zVal = "+dAccData.z+", xVal = "+dAccData.x+", noise = "+peakNoise+", peakVal = "+peakVal);
                        System.out.println("  timestamp: "+dAccData.timeStamp);
                        lastHitTime = dAccData.timeStamp;
                        peakAvg.addDataPoint(peakVal);
                        int newFreak = peakTs - lastPeakTs;
                        if((newFreak < 270) && (newFreak > 225))
                            freak = newFreak;
                        lastPeakTs = peakTs;
                        float ratio = (float)peakAvg.getAverage()/(float)peakNoise;
                        if (ratio < (scaler + 1f))
                            beta = beta + (ampResponse*1);
                        else if (ratio > (scaler + 1f))
                            beta = beta - (ampResponse*1);
                        System.out.println("Ratio = "+ratio+", beta = "+beta+", time = "+freak);
                        peakVal = zNoiseFloor;
                        hitFlag = false;
                        hitCount++;
                    }
                }
            }
            // Plot the value
            view.plotValues(dAccData);
            accel.getAccelData(accData);
        }
        System.out.println("Total Hits: "+hitCount);
        System.out.println("Test Count: "+testCount);
        System.out.println("Simulation Complete!!!");
    }
    
    public void runF2()
    {
        final int avgWindow = 5;
        final int delayTime = avgWindow;
        SlidingWindow avgZ = new SlidingWindow(avgWindow);
        SlidingWindow pcData = new SlidingWindow(delayTime);
        SlidingWindow peakAvg = new SlidingWindow(10);
        //BlockingQueue<AccelData> filterData = new ArrayBlockingQueue<>(delayTime);
        DelayBuff<AccelData> delayData = new DelayBuff(delayTime);
        AccelData accData = new AccelData();
        int hitCount = 0;
        int lastHitTime = 0;
        boolean hitFlag = false;
        
        System.out.println("Running F2!!!");
        MMA8452 accel = new MMA8452(file);
        
        // Read initial value
        accel.getAccelData(accData);
        long lastMill = System.currentTimeMillis();
        long lastTs = accData.timeStamp;
        int peakVal = 1;
        int peakNoise = 1;
        int peakTs = (int)lastTs;
        int lastPeakTs = peakTs;
        int freak = 250;
        float scaler = 1.1f;
        float beta = 6.25f;
        float ampResponse = 0.1f;
        //float beta = 4.0f;
        while(accel.valid)
        {
            // We dont need direction, meaningless
            accData.z = abs(accData.z);
            // Get a copy of the data (Won't need in Micro C code)
            AccelData fAccData = new AccelData(accData);

            // Input into first stage
            avgZ.addDataPoint(fAccData.z);
            fAccData.avgZ = (int)avgZ.getAverage();
            delayData.addSample(fAccData);
            
            // Move data from First Stage to Second Stage
            AccelData dAccData = delayData.getNextSample();
            // If delay pipe not filled yet, continue filling First Stage
            if(dAccData == null)
            {
                System.out.println("Filling Pipe!");
                continue;
            }
            
            // Remove noise floor / bias, clamp at 0
            int noiseFloor = (int)(avgZ.getAverage());
            dAccData.z = dAccData.z - noiseFloor;
            //if(dAccData.z < 0)
            //    dAccData.z = 0;
            pcData.addDataPoint((int)(dAccData.z * beta));
            dAccData.x = (int)(pcData.getAverage());
            dAccData.y = (int)(pcData.getSum());
            //if(dAccData.x != 0)
            //    System.out.println("Ratio: "+dAccData.y );

            // Check if paused...
            while(this.stop)
                Sleep(500);
            
            // Wait until its time to apply
            long deltaTs = accData.timeStamp - lastTs;
            long waitMill = lastMill + (deltaTs * timeScale);
            while(System.currentTimeMillis() < waitMill)
                Sleep(1);
            lastTs = accData.timeStamp;
            lastMill = System.currentTimeMillis();

            int deltaTime = dAccData.timeStamp - lastHitTime;
            int curSample = dAccData.x;
            if( curSample > (noiseFloor*scaler))
            {
                //if(deltaTime > 250)
                //if(deltaTime > 33)
                if(deltaTime > freak)
                {
                    //hitCount++;
                    System.out.println("Gate Open: "+hitCount+" tsDelta: "+deltaTime+" curSample = "+curSample+", zVal = "+dAccData.z+", xVal = "+dAccData.x+", noise = "+noiseFloor);
                    System.out.println("  timestamp: "+dAccData.timeStamp);
                    hitFlag = true;
                    if(curSample > peakVal)
                    {
                        peakVal = curSample;
                        peakNoise = noiseFloor;
                        peakTs = dAccData.timeStamp;
                    }
                }
                else
                {
                    System.out.println("Filtered Out: tsDelta: "+deltaTime+" curSample = "+curSample+", zVal = "+dAccData.z+", xVal = "+dAccData.x+", noise = "+noiseFloor);
                    if(curSample > peakVal)
                    {
                        peakVal = curSample;
                        peakNoise = noiseFloor;
                        peakTs = dAccData.timeStamp;
                    }
                    dAccData.x = 0; // Filter it
                }
            }
            else
            {
                if(hitFlag)
                {
                    //if(curSample < (noiseFloor - 400))
                    //if(curSample < (noiseFloor - 700))
                    if(curSample < ((noiseFloor*scaler) * -0.0))
                    {
                        System.out.println("GateClosed: tsDelta: "+deltaTime+", zVal = "+dAccData.z+", xVal = "+dAccData.x+", noise = "+peakNoise+", peakVal = "+peakVal);
                        System.out.println("  timestamp: "+dAccData.timeStamp);
                        lastHitTime = dAccData.timeStamp;
                        peakAvg.addDataPoint(peakVal);
                        int newFreak = peakTs - lastPeakTs;
                        if((newFreak < 270) && (newFreak > 225))
                            freak = newFreak;
                        lastPeakTs = peakTs;
                        float ratio = (float)peakAvg.getAverage()/(float)peakNoise;
                        if (ratio < (scaler + 1f))
                            beta = beta + (ampResponse*1);
                        else if (ratio > (scaler + 1f))
                            beta = beta - (ampResponse*1);
                        System.out.println("Ratio = "+ratio+", beta = "+beta+", time = "+freak);
                        peakVal = noiseFloor;
                        hitFlag = false;
                        hitCount++;
                    }
                }
            }
            // Plot the value
            view.plotValues(dAccData);
            accel.getAccelData(accData);
        }
        System.out.println("Total Hits: "+hitCount);
        System.out.println("Simulation Complete!!!");
    }
    
    public void runF1()
    {
        final int avgWindow = 5;
        final int delayTime = avgWindow;
        SlidingWindow avgZ = new SlidingWindow(avgWindow);
        SlidingWindow pcData = new SlidingWindow(avgWindow);
        BlockingQueue<AccelData> filterData = new ArrayBlockingQueue<>(delayTime);
        DelayBuff<AccelData> delayData = new DelayBuff(delayTime);
        AccelData accData = new AccelData();
        int hitCount = 0;
        int lastHitTime = 0;
        boolean hitFlag = false;
        
        System.out.println("F1 Running!!!");
        MMA8452 accel = new MMA8452(file);

        // Read initial value
        accel.getAccelData(accData);
        long lastMill = System.currentTimeMillis();
        long lastTs = accData.timeStamp;
        while(accel.valid)
        {
            //System.out.println("rData: "+accData.toString());
            avgZ.addDataPoint(abs(accData.z * 1));
            accData.avgZ = (int)(avgZ.getAverage());
            AccelData fCopy = new AccelData(accData);
            filterData.add(fCopy);
            while( ((int)(System.currentTimeMillis() - lastMill) - (accData.timeStamp - lastTs)) < 0)
            {
                Sleep(1);
            }
            lastTs = accData.timeStamp;
            lastMill = System.currentTimeMillis();
            if(avgZ.windowIsFull())
            {
                AccelData fAccData = filterData.poll();
                fAccData.z = (int) (abs(fAccData.z) - avgZ.getAverage()) ;
                pcData.addDataPoint(fAccData.z);
                fAccData.avgZ = (int)(pcData.getAverage()*5.25f);
                //System.out.println("fData: "+fAccData.toString());
                //if( fAccData.z > (avgZ.getAverage()+((fAccData.x-fAccData.y)*.707)) )
                //if( fAccData.z > avgZ.getAverage())
                if( fAccData.avgZ > avgZ.getAverage())
                {
                    int deltaTime = fAccData.timeStamp - lastHitTime;
                    if(deltaTime > 250)
                    {
                        lastHitTime = fAccData.timeStamp;
                        hitCount++;
                        System.out.println("Hit count: "+hitCount+" tsDelta: "+deltaTime+", zVal = "+fAccData.z+", xVal = "+fAccData.x+", flag = "+hitFlag);
                        hitFlag = true;
                    }
                }
                else
                    hitFlag = false;
                view.plotValues(fAccData);
            }
            // Read next values
            accel.getAccelData(accData);
        }
        System.out.println("Total Hits: "+hitCount);
        System.out.println("Simulation Complete!!!");
    }
    
}
