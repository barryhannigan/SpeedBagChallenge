# SpeedBagChallenge
Nate's Sparkfun Speed Bag Challenge files

**Current Numbers:**

3-77hits -> 77

4-81hits -> 81

5-93hits -> 94

6-79hits -> 79

Mystery1 -> 172

Mystery2 -> 158


**Status:**

It’s been a long road to get here, as I've reworked the algorithm 6 different times. I started out coding in Java so I could easily create a GUI and plot the data to understand how to process it. It seemed somewhat easy to make a Nate punch detector, but just wasn't confident any of those could work for other boxers. This 6th generation seems to be on its way to working for most boxers so I'm going to submit it, I wanted to get a horse in the race with all the work I've put in so far. I implemented the algorithm first in java code that I wrote in a way the can be easily ported to C. Once I was ready I then quickly ported it to C/C++ in a Visual Studio 2015 project. Next I took the code from the VS 2015 project and ported it into Nate’s original Arduino project. All the code from all 3 phases is in the Git Hub repository. The Java application can be run from a Linux or Windows command line by going to the project directory and typing:
"java -jar dist/SpeedBagAlg.jar <filename>"

You can view the Java source code in the src directory under Java folder with any editor or use netbeans to open the project.


**Approach:**

Since this is to run on a micro controller I decided not to use any floating point or time consuming math functions. All math used by the algorithm is integer addition, subtraction, multiplication or division. I used a Front End processor to remove bias and condition the raw input signal, next a detection processor to further condition the signal and find the minimums and maximums to be used to count punches. Right now I’m using a fixed time to gate triggering too early on the next punch when the signal gets too noisy, but I want to add a tracking processor to track the punches and be able to open and close amplitude and frequency thresholds plus the possibility of lighting an LED when the rhythm is staying consistent.

**Front End Processor:**

The FEP starts with a High Pass FIR filter to remove static bias, then decimating the signal to squelch noise and then taking the absolute value of the signal and followed by a small smoothing average to clean up the signal so it can be fed into the detection processor.

**Detection Processor:**

The DP first decimates the signal squelching more values near the floor to zero and then taking the magnitude squared (Mag Square) of the signal. The mag square signal is then feed into a Low Pass FIR filter to help smooth the signal since the acceleration events that are of interest occur in a 2 to 4 hertz range. The output of the FIR filter is fed into a medium size averaged smoothing algorithm that also spits out a delayed original signal that is right in the middle of the averaged signal so it is aligned in such a way to be used as a punch threshold template. The algorithm adjusts automatically to amplitude variations to keep from getting confused on the many resonant addition and subtraction phases that seem to occur. Right now punch frequency gate is fixed at 260 ms, but this may be a problem for the same algorithm working for amateurs and speed bag pros, which is why a Tracking processor would most likely be needed to be highly accurate across boxers and different speed bag platforms.

**Tracking Processor:**

Not implemented yet, but will track the frequency of peak punch detections and be able to better regulate a punch frequency gate to be able to increase tracking accuracy for amateurs and speed bag pros with the same detection algorithm.

