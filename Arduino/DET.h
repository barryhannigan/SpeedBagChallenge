#pragma once
/*
 * Barry Hannigan 
 * 06/30/2016 - Created first version.
 * 
 * License: This code is public domain but you buy me a beer if you use this and we meet someday (Beerware license).
*/

#include "AccelData.h"

struct DETFilterKernel
{
	const static int FILTER_TAP_NUM = 13;
  // Values scaled to 1/65535 Fixed Point
	int filter_taps[FILTER_TAP_NUM] =
	{
		-9,
		667,
		2340,
		5200,
		8662,
		11553,
		12684,
		11553,
		8662,
		5200,
		2340,
		667,
		-9
	};
	//int[] history = new int[FEPFILTER_TAP_NUM];
	AccelData history[FILTER_TAP_NUM];
	int last_index;
};

// Public Detection Processor Functions
void DET_init();
void DETFilter_init(DETFilterKernel* f);
void DETFilter_put(DETFilterKernel* f, AccelData* input);
AccelData* DETFilter_get(DETFilterKernel* f);
