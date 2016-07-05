/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package speedbagalg;

/**
 *
 * @param <T> Numeric Type
 * @author Barry Hannigan <support@miser-tech.com>
 */
public class SlidingWindow
{
    private int total;
    private int[] values;
    //private int windowSize;
    private int wrapIndex;
    private int numValues;

    public SlidingWindow(int windowSize)
    {
        values = new int[windowSize];
        numValues = 0;
        wrapIndex = 0;
    }
    
    public void addDataPoint(int value)
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
    
    public int getAverage()
    {
        if (numValues < values.length)
            return total / numValues;
        else
            return total / values.length;
    }

    public double getSum()
    {
        return total;
    }
    
    public boolean windowIsFull()
    {
        return numValues == values.length;
    }
    
    @Override
    public String toString()
    {
        return "SlidingWindow{" + "total=" + total + ", windowSize=" + values.length + ", wrapIndex=" + wrapIndex + ", numValues=" + numValues + '}';
    }
}
