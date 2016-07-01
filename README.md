# SpeedBagChallenge
Nate's Sparkfun Speed Bag Challenge files

Current Numbers:

3-77hits -> 77
4-81hits -> 81
5-93hits -> 94
6-79hits -> 79
Mystery1 -> 172
Mystery2 -> 158

Status:
Its been a long road to get here, as I've reworked the algorithm 6 different times. In the java code function run6() is the code that is executing if you look at the source. It seemed somewhat easy to make a Nate punch detector, but just wasn't confident any of those could work for other boxers. This 6th generation seems to be on its way to working for most boxers so I'm going to submit it, I wanted to get a horse in the race with all the work I've put in so far. Currently I have a Java program that implements the algorithm in java code that I wrote in a way the can be easily ported to C, which I haven't done yet, but plan to so shortly. The Java application can be run from a Linux or Windows command line by going to the project directory and typing:
"java -jar dist/SpeedBagAlg.jar <filename>"

You can view the source code in the src directory with any editor or use netbeans to open the project.

Approach:
Since this is to run on a micro controller I decided to not use any floating point or time consuming math functions. All math used by run6() is integer addition, subtraction, multiplication or division. I used a Front End processor to remove bias and condition the raw input signal. Then a detection processor to find the mins and maxs to count punch. I want to add a tracking processor to track the punches and be able to open and close both amplitude and frequency thresholds plus the possibility of lighting an LED when the rhythm is staying consistent.

Front End Processor:
Starts with a High Pass FIR filter to remove static bias, then decimating the signal to squelch more noise than taking the absolute value of the signal and followed by a small smoothing average to clean up the signal so it can be fed into the detection processor without the signal being too large.

Detection Processor:
Decimates the signal squelching more values near the floor to zero and then taking the magnitude squared (Mag Square) of the signal. The mag square signal is then feed into a Low Pass FIR filter. The output of the FIR filter is feed into a medium size smoothing algorithm that also spits out a delayed signal that is right in the middle of the averaged signal so it is aligned in such a way to be used as a punch threshold template. The algorithm adjusts automatically to amplitude variations to keep from getting confused on the many resonant addition and subtraction phases that seem to occur. Right now punch frequency gate is fixed at 260 ms, but this may be a problem for the same algorithm working for amateurs and speed bag pros, which is why a Tracking processor would most likely be needed to be highly accurate across boxers and different speed bag platforms.

Tracking Processor:
Not implemented yet, but will track the frequency of peak punch detections and be able to better regulate punch frequency gate to be able to increase accuracy for amateurs and speed bag pros.

