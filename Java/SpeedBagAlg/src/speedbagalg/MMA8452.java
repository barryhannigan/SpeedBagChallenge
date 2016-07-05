/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package speedbagalg;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Barry Hannigan <support@miser-tech.com>
 */
public class MMA8452
{
    private Scanner dataFile;
    public volatile boolean valid = true;
    
    private int     timeStamp = 0;
    private int     xVal = 0;
    private int     yVal = 0;
    private int     zVal = 0;
    
    public MMA8452(File file)
    {
        String theLine = "";
        String[] prsLine;
        
        try
        {
            // Open data file
            dataFile = new Scanner(file);
            
            // Find first valid line
            parseNextLine();
        } catch (FileNotFoundException ex)
        {
            Logger.getLogger(MMA8452.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void getAccelData(AccelData data)
    {
        // Record Data
        data.timeStamp = timeStamp;
        data.x = xVal;
        data.y = yVal;
        data.z = zVal;
        
        parseNextLine();
        return;
    }
    
    private void parseNextLine()
    {
        String theLine;
        String[] prsLine;
        while(dataFile.hasNext())
        {
            theLine = dataFile.next();
            prsLine = theLine.split(",");
            if(prsLine.length < 4)
                continue;
            //System.out.println(theLine);
            timeStamp = Integer.parseInt(prsLine[0]);
            xVal = Integer.parseInt(prsLine[1]);
            yVal = Integer.parseInt(prsLine[2]);
            zVal = Integer.parseInt(prsLine[3]);
            // Exit loop we found the first entry
            valid = true;
            return;
        }
        valid = false;
    }

    @Override
    public String toString()
    {
        return "MMA8452{" + "timeStamp=" + timeStamp + ", xVal=" + xVal + ", yVal=" + yVal + ", zVal=" + zVal + '}';
    }
    
}
