#pragma once
/*
 * Barry Hannigan 
 * 06/30/2016 - Created first version.
 * 
 * License: This code is public domain but you buy me a beer if you use this and we meet someday (Beerware license).
*/
#include "AccelData.h"

struct FEPFilterKernel
{
	const static int FILTER_TAP_NUM = 19;
  // Values scaled to 1/65535 Fixed Point
	int filter_taps[FILTER_TAP_NUM] =
	{
		385,
		4901,
		-179,
		-871,
		-2538,
		-4539,
		-6613,
		-8423,
		-9651,
		55446,
		-9651,
		-8423,
		-6613,
		-4539,
		-2538,
		-871,
		-179,
		4901,
		385
	};
	//int history[FILTER_TAP_NUM];
	AccelData history[FILTER_TAP_NUM];
	int last_index;
};

// Public Front End Processor Functions
void FEP_init();
void FEPFilter_init(FEPFilterKernel* f);
void FEPFilter_put(FEPFilterKernel* f, AccelData* input);

AccelData* FEPFilter_get(FEPFilterKernel* f);
