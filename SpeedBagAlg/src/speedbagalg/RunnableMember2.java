/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package speedbagalg;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Barry Hannigan <support@miser-tech.com>
 */
public class RunnableMember2 implements Runnable
{
    private Object theObj;
    private String methodName;
    private Method theMethod;
    private volatile boolean didStart;
    private volatile boolean valid;
    private volatile Thread theThread;
    
    /**
     * Creates a Thread that will run the Member Function on the supplied Object
     * @param theObj
     * @param methodName
     */
    public RunnableMember2(Object theObj, String methodName)
    {
        this.theObj = theObj;
        this.methodName = methodName;
        this.didStart = false;
        this.valid = false;
        theThread = new Thread(this);
        
        try
        {
            // Convert the String Name to Reflection Method
            theMethod = theObj.getClass().getMethod(methodName, (Class[]) null);
            valid = true;
        } catch (NoSuchMethodException ex)
        {
            Logger.getLogger(RunnableMember2.class.getName()).log(Level.SEVERE, "No such Method ["+methodName+"]", ex);
        } catch (SecurityException ex)
        {
            Logger.getLogger(RunnableMember2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Thread getThread()
    {
        return theThread;
    }

    /**
     * Check to see if Thread has started
     * @return true if started otherwise false
     */
    public boolean didStart()
    {
        return this.didStart;
    }

    /**
     * Check to see if this instance was constructed with out error
     * @return true if this Instance is valid
     */
    public boolean isValid()
    {
        return valid;
    }
    
    @Override
    public void run()
    {
        try
        {
            // Now Invoke the Method
            theMethod.invoke(theObj, (Object[]) null);

            // If we made it here assume we started OK
            this.didStart = true;
        } catch (IllegalAccessException ex)
        {
            Logger.getLogger(RunnableMember2.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex)
        {
            Logger.getLogger(RunnableMember2.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex)
        {
            Logger.getLogger(RunnableMember2.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex)
        {
            Logger.getLogger(RunnableMember2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String toString()
    {
        return "RunnableMember{" + "theObj=" + theObj + ", methodName=" + methodName + 
                ", isValid=" + valid +" , didStart=" + didStart + '}';
    }
    
}

