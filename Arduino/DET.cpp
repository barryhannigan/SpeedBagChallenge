/*
 * Barry Hannigan 
 * 06/30/2016 - Created first version.
 * 
 * License: This code is public domain but you buy me a beer if you use this and we meet someday (Beerware license).
*/
#include "DET.h"
#include "Arduino.h"

const static int DET_START_PEAK = 1;
const static int DET_DETECT_PEAK = 2;
const static int DET_START_MIN = 3;
const static int DET_DETECT_MIN = 4;

// Window Average
const static int windowSize = 99;
static int total;
static int values[windowSize];
static int wrapIndex;
static int numValues;

// Detector
int lastMin = 0;
int lastPeak = 200;
int startMin = 0;
int lastVal = 0;
int detState = DET_START_PEAK;
int lastStartTS = 0;
int lastPeakTS = 0;
int deltaPeak = 0;
int PeakThreshold = 100;
int MinThreshold = 10;

int detAvg_addDataPoint(int value);
int detAvg_getAverage();

void detAlgorithm(AccelData* sample)
{
  int threshold = detAvg_getAverage();
  sample->avgZ = threshold;
  sample->maxZFound = 0;
  switch (detState)
  {
  case DET_START_PEAK:
    if (((sample->timeStamp - lastStartTS) > 260) && (sample->z > threshold + (deltaPeak / 10)) && sample->z > PeakThreshold)
    {
      sample->maxZFound = 500;
      lastStartTS = sample->timeStamp;
      detState = DET_DETECT_PEAK;
      deltaPeak = abs(sample->z - lastPeak);
      lastPeak = sample->z;
    }
    break;
  case DET_DETECT_PEAK:
    if (sample->z > lastPeak)
    {
      lastPeak = sample->z;
      //System.out.println("New Peak " + sample.z + " at " + sample.timeStamp + ", delta = " + (sample.timeStamp - lastPeakTS));
      lastPeakTS = sample->timeStamp;
      //PeakThreshold = ((lastPeak) / 3);
    }
    else if (sample->z < threshold)
    {
      startMin = sample->z;
      detState = DET_START_MIN;
    }
    break;
  case DET_START_MIN:
    if (sample->z < (threshold - 50))
    {
      lastMin = sample->z;
      detState = DET_DETECT_MIN;
    }
    break;
  case DET_DETECT_MIN:
    if (sample->z < lastMin)
    {
      lastMin = sample->z;
    }
    else
    {
      if ((sample->z >(lastMin + 50)) || (sample->z < MinThreshold))
      {
        //System.out.println("New Min " + sample.z + " at " + sample.timeStamp);
        sample->minZFound = 500;
        detState = DET_START_PEAK;
      }
    }
    break;
  }
}

void DETFilter_init(DETFilterKernel* f)
{
  int i;
  for (i = 0; i < f->FILTER_TAP_NUM; ++i)
  {
    f->history[i].minZFound = 0;
    f->history[i].maxZFound = 0;
    f->history[i].z = 0;
  }
  f->last_index = 0;
}

void DETFilter_put(DETFilterKernel* f, AccelData* input)
{
  // Decimate by half
  input->z = input->z / 2;

  // Now create Mag Squared value for filter input
  input->z = input->z * input->z;

  // Add the value to the next history buffer
  f->history[f->last_index].z = input->z;
  f->history[f->last_index].timeStamp = input->timeStamp;
  f->last_index++;
  if (f->last_index == f->FILTER_TAP_NUM)
    f->last_index = 0;
}

  AccelData* DETFilter_get(DETFilterKernel* f)
  {
    int accZ = 0;
    int index = f->last_index, i;
    //System.out.println("NumTaps = "+ f.FEPFILTER_TAP_NUM+", last_index = "+f.last_index);
    for (i = 0; i < f->FILTER_TAP_NUM; ++i)
    {
      index = index != 0 ? index - 1 : f->FILTER_TAP_NUM - 1;
      // Value becomes 16 bit fixed point after multiplication
      accZ += f->history[index].z * f->filter_taps[i];
    }
    AccelData& ret = f->history[f->last_index];
    //System.out.println("accZ = "+accZ);
    // Turn value back to integer
    ret.z = (int)(accZ >> 16L);
    //System.out.println("retZ = "+ret.z);
    ret.z = detAvg_addDataPoint(ret.z);
    detAlgorithm(&ret);
    return &ret;
  }

  void DET_init()
  {
    numValues = 0;
    wrapIndex = 0;
  }

  int detAvg_addDataPoint(int value)
  {
    int delayVal;
    if (numValues < windowSize)
    {
      delayVal = values[numValues / 2];
      total = total + value;
      values[numValues] = value;
      numValues++;
    }
    else
    {
      delayVal = values[(wrapIndex + (windowSize / 2)) % windowSize];
      total = total + (value - values[wrapIndex]);
      values[wrapIndex] = value;
      wrapIndex = (wrapIndex + 1) % windowSize;
    }
    return delayVal;
  }

  int detAvg_getAverage()
  {
    if (numValues < windowSize)
      return total / numValues;
    else
      return total / windowSize;
  }


