/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package speedbagalg;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import javax.swing.SwingUtilities;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.category.SlidingCategoryDataset;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;

/**
 *
 * @author Barry Hannigan <support@miser-tech.com>
 */
public class OscopeView extends javax.swing.JFrame
{
    /**
     * The time series data.
     */
    private JFreeChart chart;
    private ChartPanel thePanel;
    //private TimeSeries xData;
    //private TimeSeries yData;
    //private TimeSeries zData;
    //private TimeSeries averageData;
    private XYSeries xData;
    private XYSeries yData;
    private XYSeries zData;
    private XYSeries averageData;
    //private TimeSeries windSpeedSeries;
    //private TimeSeries rainSeries;
    private RunnableMember2 updateThread;
    private BlockingQueue<AccelData> qData = new ArrayBlockingQueue<AccelData>(100);
    boolean plotX = true;
    boolean plotY = true;
    boolean plotZ = true;
    int     minStart = 0;
    int     maxDomain = 2500;
    int     minTime = minStart;
    int     vMin = -500;
    int     vMax = 1000;
    private MainLoop theLoop;
    private Thread mainThread;
    private String fileName;
    /**
     * Creates new form OscopeView
     */
/*    
    public OscopeView(String fileName)
    {
        this.fileName = fileName;
        initComponents();
        updateThread = new RunnableMember2(this, "updateGUI");
        ChartFactory.setChartTheme(new StandardChartTheme("JFree/Shadow", true));
        // create a dataset...
        int maxAge = 2400;
        this.xData = new TimeSeries("X axis");
        this.xData.setMaximumItemAge(maxAge);
        this.yData = new TimeSeries("Y axis");
        this.yData.setMaximumItemAge(maxAge);
        this.zData = new TimeSeries("Z axis");
        this.zData.setMaximumItemAge(maxAge);
        this.averageData = new TimeSeries("Avg. Data");
        this.averageData.setMaximumItemAge(maxAge);
        //this.windSpeedSeries = new TimeSeries("Wind Speed");
        //this.windSpeedSeries.setMaximumItemAge(maxAge);
        //this.rainSeries = new TimeSeries("Rain 24hrs");
        //this.rainSeries.setMaximumItemAge(maxAge);
        
        final TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(xData);
        dataset.addSeries(yData);
        dataset.addSeries(zData);
        dataset.addSeries(averageData);
        
        //dataset.addSeries(windSpeedSeries);
        //dataset.addSeries(rainSeries);
        final JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Accel Data",
                "Time",
                "Value",
                dataset,
                true,
                true,
                false);
        final XYPlot plot = chart.getXYPlot();
        
        ValueAxis axis = plot.getDomainAxis();
        axis.setAutoRange(true);
        axis = plot.getRangeAxis();
        axis.setRange(-1000.0, 10000.0);
        
        chart.setBackgroundPaint(Color.white);

        //XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);

        // create and display a frame...
        thePanel = new ChartPanel(chart);

        Dimension d = new Dimension(200, 100);
        thePanel.setSize(d);
        thePanel.setPreferredSize(d);
        BorderLayout layout = new BorderLayout();
        jPanelOscope.setLayout(layout);
        jPanelOscope.add(thePanel, BorderLayout.CENTER);
        
        plotXCheckBox.setSelected(plotX);
        plotYCheckBox.setSelected(plotY);
        plotZCheckBox.setSelected(plotZ);
    }
*/
    public OscopeView(String fileName)
    {
        this.fileName = fileName;
        initComponents();
        updateThread = new RunnableMember2(this, "updateGUI");
        ChartFactory.setChartTheme(new StandardChartTheme("JFree/Shadow", true));
        // create a dataset...
        this.xData = new XYSeries("Min");
        //this.xData.setMaximumItemAge(maxAge);
        this.yData = new XYSeries("Peak");
        //this.yData.setMaximumItemAge(maxAge);
        this.zData = new XYSeries("Z axis");
        //this.zData.setMaximumItemAge(maxAge);
        this.averageData = new XYSeries("Avg. Data");
        //this.averageData.setMaximumItemAge(maxAge);
        //this.windSpeedSeries = new TimeSeries("Wind Speed");
        //this.windSpeedSeries.setMaximumItemAge(maxAge);
        //this.rainSeries = new TimeSeries("Rain 24hrs");
        //this.rainSeries.setMaximumItemAge(maxAge);
        
        final XYSeriesCollection dataset = new XYSeriesCollection();
        //SlidingCategoryDataset dataset = new SlidingCategoryDataset(0, 10);
        dataset.addSeries(xData);
        dataset.addSeries(yData);
        dataset.addSeries(zData);
        dataset.addSeries(averageData);
        
        //dataset.addSeries(windSpeedSeries);
        //dataset.addSeries(rainSeries);
        chart = ChartFactory.createXYLineChart(
                "Accel Data",
                "Time",
                "Value",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false);
        final XYPlot plot = chart.getXYPlot();
        
        ValueAxis axis = plot.getDomainAxis();
        axis.setAutoRange(false);
        axis.setRange(minStart, maxDomain);
        axis = plot.getRangeAxis();
        axis.setRange(vMin, vMax);
        
        
        chart.setBackgroundPaint(Color.white);

        //XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);

        // create and display a frame...
        thePanel = new ChartPanel(chart);

        Dimension d = new Dimension(200, 100);
        thePanel.setSize(d);
        thePanel.setPreferredSize(d);
        BorderLayout layout = new BorderLayout();
        jPanelOscope.setLayout(layout);
        jPanelOscope.add(thePanel, BorderLayout.CENTER);
        
        plotXCheckBox.setSelected(plotX);
        plotYCheckBox.setSelected(plotY);
        plotZCheckBox.setSelected(plotZ);
        
        vMaxSpinner.setValue(vMax);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        jPanelOscope = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        exitButton = new javax.swing.JButton();
        plotXCheckBox = new javax.swing.JCheckBox();
        plotYCheckBox = new javax.swing.JCheckBox();
        plotZCheckBox = new javax.swing.JCheckBox();
        startButton = new javax.swing.JButton();
        hStartSpinner = new javax.swing.JSpinner();
        jLabel1 = new javax.swing.JLabel();
        vMaxSpinner = new javax.swing.JSpinner();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        hitCountTextField = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanelOscope.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        javax.swing.GroupLayout jPanelOscopeLayout = new javax.swing.GroupLayout(jPanelOscope);
        jPanelOscope.setLayout(jPanelOscopeLayout);
        jPanelOscopeLayout.setHorizontalGroup(
            jPanelOscopeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanelOscopeLayout.setVerticalGroup(
            jPanelOscopeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 373, Short.MAX_VALUE)
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        exitButton.setText("Exit");
        exitButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                exitButtonActionPerformed(evt);
            }
        });

        plotXCheckBox.setText("Plot X");
        plotXCheckBox.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                plotXCheckBoxStateChanged(evt);
            }
        });

        plotYCheckBox.setText("Plot Y");
        plotYCheckBox.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                plotYCheckBoxStateChanged(evt);
            }
        });

        plotZCheckBox.setText("Plot Z");
        plotZCheckBox.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                plotZCheckBoxStateChanged(evt);
            }
        });

        startButton.setText("Start");
        startButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                startButtonActionPerformed(evt);
            }
        });

        hStartSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), null, null, Integer.valueOf(1000)));
        hStartSpinner.setToolTipText("Start Time");
        hStartSpinner.setName(""); // NOI18N
        hStartSpinner.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                hStartSpinnerStateChanged(evt);
            }
        });

        jLabel1.setText("H Start");

        vMaxSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(500), null, null, Integer.valueOf(10)));
        vMaxSpinner.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                vMaxSpinnerStateChanged(evt);
            }
        });

        jLabel2.setText("V Max");

        jLabel3.setText("Hit Count:");

        hitCountTextField.setText("0");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(startButton)
                        .addGap(104, 104, 104)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(hitCountTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(hStartSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel1))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(216, 216, 216)
                        .addComponent(vMaxSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2)))
                .addGap(52, 52, 52)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(plotYCheckBox)
                            .addComponent(plotXCheckBox))
                        .addContainerGap(158, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(plotZCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(exitButton)
                        .addGap(20, 20, 20))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(plotXCheckBox)
                            .addComponent(hStartSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(plotYCheckBox)
                        .addGap(2, 2, 2))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(vMaxSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(startButton)
                    .addComponent(plotZCheckBox)
                    .addComponent(exitButton)
                    .addComponent(jLabel3)
                    .addComponent(hitCountTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelOscope, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanelOscope, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8))
        );

        jPanelOscope.getAccessibleContext().setAccessibleName("");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void exitButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_exitButtonActionPerformed
    {//GEN-HEADEREND:event_exitButtonActionPerformed
        System.exit(0);
    }//GEN-LAST:event_exitButtonActionPerformed

    private void startButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_startButtonActionPerformed
    {//GEN-HEADEREND:event_startButtonActionPerformed
        if(startButton.getText() == "Start")
        {
            xData.clear();
            yData.clear();
            zData.clear();
            averageData.clear();
            
            minStart = 0;
            minTime = 0;
            hitCountTextField.setText("0");
            final XYPlot plot = chart.getXYPlot();
            ValueAxis axis = plot.getDomainAxis();
            axis.setAutoRange(false);
            axis.setRange(minStart, maxDomain);

            theLoop = new MainLoop(fileName, this);
            mainThread = new Thread(theLoop);
            mainThread.start();
            startButton.setText("Stop");
        }
        else if (startButton.getText() == "Stop")
        {
            theLoop.stop();
            startButton.setText("Resume");
        }
        else if (startButton.getText() == "Resume")
        {
            theLoop.resume();
            startButton.setText("Stop");
        }

    }//GEN-LAST:event_startButtonActionPerformed

    public void updateHitCount(int count)
    {
        hitCountTextField.setText(""+count);
    }
    
    public void simulationComplete()
    {
        startButton.setText("Start");
    }
    
    private void plotXCheckBoxStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_plotXCheckBoxStateChanged
    {//GEN-HEADEREND:event_plotXCheckBoxStateChanged
        if (plotX == plotXCheckBox.isSelected())
            return;
        plotX = plotXCheckBox.isSelected();
        XYSeriesCollection s = (XYSeriesCollection)chart.getXYPlot().getDataset();
        if(plotX)
            s.addSeries(xData);
        else
            s.removeSeries(xData);
    }//GEN-LAST:event_plotXCheckBoxStateChanged

    private void plotYCheckBoxStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_plotYCheckBoxStateChanged
    {//GEN-HEADEREND:event_plotYCheckBoxStateChanged
        if (plotY == plotYCheckBox.isSelected())
            return;
        plotY = plotYCheckBox.isSelected();
        XYSeriesCollection s = (XYSeriesCollection)chart.getXYPlot().getDataset();
        if(plotY)
            s.addSeries(yData);
        else
            s.removeSeries(yData);
    }//GEN-LAST:event_plotYCheckBoxStateChanged

    private void plotZCheckBoxStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_plotZCheckBoxStateChanged
    {//GEN-HEADEREND:event_plotZCheckBoxStateChanged
        if (plotZ == plotZCheckBox.isSelected())
            return;
        plotZ = plotZCheckBox.isSelected();
        XYSeriesCollection s = (XYSeriesCollection)chart.getXYPlot().getDataset();
        if(plotZ)
            s.addSeries(zData);
        else
            s.removeSeries(zData);
    }//GEN-LAST:event_plotZCheckBoxStateChanged

    private void hStartSpinnerStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_hStartSpinnerStateChanged
    {//GEN-HEADEREND:event_hStartSpinnerStateChanged
        int baseTime = (int) hStartSpinner.getValue();
        final XYPlot plot = chart.getXYPlot();
        ValueAxis axis = plot.getDomainAxis();
        axis.setAutoRange(false);
        minStart = baseTime;
        axis.setRange(minStart, minStart + maxDomain);
        //System.out.println("Spinner: "+baseTime);
    }//GEN-LAST:event_hStartSpinnerStateChanged

    private void vMaxSpinnerStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_vMaxSpinnerStateChanged
    {//GEN-HEADEREND:event_vMaxSpinnerStateChanged
        final XYPlot plot = chart.getXYPlot();
        ValueAxis axis = plot.getRangeAxis();
        axis.setAutoRange(false);
        vMax = (int)vMaxSpinner.getValue();
        axis.setRange(vMin, vMax);        
    }//GEN-LAST:event_vMaxSpinnerStateChanged

    public void plotValues(AccelData data)
    {
        qData.add(data);
        //System.out.println("plotValues: "+data.toString());
        SwingUtilities.invokeLater(updateThread);
    }
    
    public void updateGUI()
    {
        // Empty Queue
        while(!qData.isEmpty())
        {
            AccelData data = qData.poll();
            //Millisecond newTime = new Millisecond(new Date(data.timeStamp));
            FixedMillisecond newTime = new FixedMillisecond(data.timeStamp);
            if (plotX)
                xData.addOrUpdate(data.timeStamp, data.x);
                //xData.addOrUpdate(newTime, data.x);
            if (plotY)
                yData.addOrUpdate(data.timeStamp, data.y);
                //yData.addOrUpdate(newTime, data.y);
            zData.addOrUpdate(data.timeStamp, data.z);
                //zData.addOrUpdate(newTime, data.z);
            //averageData.addOrUpdate(newTime, data.avgZ);
            averageData.addOrUpdate(data.timeStamp, data.avgZ);
            //thePanel.repaint();
            if(data.timeStamp > (minTime + maxDomain))
            {
                minTime = minTime + 10;
                chart.getXYPlot().getDomainAxis().setRange(minTime, maxDomain + minTime);
                hStartSpinner.setValue(minTime);
            }
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[])
    {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try
        {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
            {
                if ("Nimbus".equals(info.getName()))
                {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex)
        {
            java.util.logging.Logger.getLogger(OscopeView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex)
        {
            java.util.logging.Logger.getLogger(OscopeView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex)
        {
            java.util.logging.Logger.getLogger(OscopeView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex)
        {
            java.util.logging.Logger.getLogger(OscopeView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        if(args.length == 0)
        {
            System.out.println("You must supply a file name");
        }
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                new OscopeView(args[0]).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton exitButton;
    private javax.swing.JSpinner hStartSpinner;
    private javax.swing.JTextField hitCountTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanelOscope;
    private javax.swing.JCheckBox plotXCheckBox;
    private javax.swing.JCheckBox plotYCheckBox;
    private javax.swing.JCheckBox plotZCheckBox;
    private javax.swing.JButton startButton;
    private javax.swing.JSpinner vMaxSpinner;
    // End of variables declaration//GEN-END:variables
}
