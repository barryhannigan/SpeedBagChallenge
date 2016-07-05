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
public class AccelData
{
    public int timeStamp;
    public int x;
    public int y;
    public int z;
    public int avgX;
    public int avgY;
    public int avgZ;

    public AccelData()
    {
        timeStamp = 0;
        x = 0;
        y = 0;
        z = 0;
        avgX = 0;
        avgY = 0;
        avgZ = 0;
    }
    
    public AccelData(AccelData other)
    {
        this.timeStamp = other.timeStamp;
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
        this.avgX = other.avgX;
        this.avgY = other.avgY;
        this.avgZ = other.avgZ;
    }

    @Override
    public String toString()
    {
        return "AccelData{" + "timeStamp=" + timeStamp + ", x=" + x + ", y=" + y + ", z=" + z + '}';
    }
}
