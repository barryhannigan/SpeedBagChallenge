/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package speedbagalg;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Barry Hannigan <support@miser-tech.com>
 */
public class SpeedBagAlg implements Runnable
{
    public String fileName;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        if(args.length == 0)
        {
            System.out.println("You must supply a file name!");
            System.exit(0);
        }
        SpeedBagAlg theAlg = new SpeedBagAlg();
        theAlg.fileName = args[0];
        //theAlg.fileName = "3-77hits.txt";
        java.awt.EventQueue.invokeLater(theAlg);

    }

    @Override
    public void run()
    {
        OscopeView view = new OscopeView(fileName);
        view.setVisible(true);
        
        //theLoop = new MainLoop(fileName);
        //mainThread = new Thread(theLoop);
        //mainThread.start();
        return;
/*        
        AccelData accData = new AccelData();
        
        System.out.println("Hello World!!!");
        File file = new File(fileName);
        if(!file.exists())
        {
            System.out.println("File NOT found");
            return;
        }
        MMA8452 accel = new MMA8452(file);
        OscopeView view = new OscopeView();
        view.setVisible(true);

        // Read initial value
        accel.getAccelData(accData);
        view.plotValues(accData);
        long lastMill = System.currentTimeMillis();
        long lastTs = accData.timeStamp;
        for(int i = 0; i < 5000; i++)
        {
            accel.getAccelData(accData);
            while( ((int)(System.currentTimeMillis() - lastMill) - (accData.timeStamp - lastTs)) < 0)
            {
                try
                {
                    Thread.sleep(2);
                } catch (InterruptedException ex)
                {
                    Logger.getLogger(SpeedBagAlg.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            lastTs = accData.timeStamp;
            lastMill = System.currentTimeMillis();
            view.plotValues(accData);
            System.out.println("Data: "+accData.toString());
        }
*/        
    }
    
}
