/*
 * Barry Hannigan 
 * 06/30/2016 - Created first version.
 * 
 * License: This code is public domain but you buy me a beer if you use this and we meet someday (Beerware license).
*/
#include "FEP.h"
#include "Arduino.h"

// Window Average
const static int windowSize = 49;
static int total;
static int values[windowSize];
static int wrapIndex;
static int numValues;


/*

	FIR filter designed with
	http://t-filter.appspot.com

	sampling frequency: 500 Hz

	fixed point precision: 16 bits

	* 0 Hz - 18 Hz
	gain = 0
	desired attenuation = -40 dB
	actual attenuation = n/a

	* 50 Hz - 250 Hz
	gain = 1
	desired ripple = 5 dB
	actual ripple = n/a

*/

void fepAvg_addDataPoint(int value);
int fepAvg_getAverage();

void FEPFilter_init(FEPFilterKernel* f)
{
	int i;
	for (i = 0; i < f->FILTER_TAP_NUM; ++i)
	{
		f->history[i].z = 0;
	}
	f->last_index = 0;
}

void FEPFilter_put(FEPFilterKernel* f, AccelData* input)
{
	f->history[f->last_index].z = input->z;
	f->history[f->last_index].timeStamp = input->timeStamp;
	f->last_index++;
	if (f->last_index == f->FILTER_TAP_NUM)
		f->last_index = 0;
}

AccelData* FEPFilter_get(FEPFilterKernel* f)
{
	int accZ = 0;
	int index = f->last_index, i;
	//System.out.println("NumTaps = "+ f.FEPFILTER_TAP_NUM+", last_index = "+f.last_index);
	for (i = 0; i < f->FILTER_TAP_NUM; ++i)
	{
		index = index != 0 ? index - 1 : f->FILTER_TAP_NUM - 1;
   // Value becomes 16 bit fixed point after multiplication
		accZ += (f->history[index].z * f->filter_taps[i]);
	}
	AccelData* ret = &f->history[f->last_index];
	//System.out.println("accZ = "+accZ);
  // Turn value back to integer
	ret->z = (accZ >> 16L);
	//System.out.println("retZ = "+ret.z);

	// Decimate by half
	ret->z = ret->z / 2;
	// Flip all values to positive
	ret->z = abs(ret->z);

	// Average the data some to smooth it
	fepAvg_addDataPoint(ret->z);
	ret->z = fepAvg_getAverage();

	// Return FEP filtered values
	return ret;
}

	void FEP_init()
	{
		numValues = 0;
		wrapIndex = 0;
	}

	void fepAvg_addDataPoint(int value)
	{
		if (numValues < windowSize)
		{
			total = total + value;
			values[numValues] = value;
			numValues++;
		}
		else
		{
			total = total + (value - values[wrapIndex]);
			values[wrapIndex] = value;
			wrapIndex = (wrapIndex + 1) % windowSize;
		}
	}

	int fepAvg_getAverage()
	{
		if (numValues < windowSize)
			return total / numValues;
		else
			return total / windowSize;
	}

