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
public class DelayBuff<T>
{
    private int delaySize = 1;
    private T[] delayBuff = null;
    private int inIndex = 0;
    private int outIndex = 0;
    
    public DelayBuff(int size)
    {
        delaySize = 1 + size;
        delayBuff = (T[])new Object[delaySize];
        inIndex = size;
        for (int i = 0; i < delaySize; i++)
        {
//            delayBuff[i] = (T)new Object();
        }

    }
    
    public void addSample(T sample)
    {
        delayBuff[inIndex] = sample;
        inIndex = (inIndex + 1) % delaySize;
    }

    public T getNextSample()
    {
        T val = delayBuff[outIndex];
        outIndex = (outIndex + 1) % delaySize;
        return val;
    }
}
