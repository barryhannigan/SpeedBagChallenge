#pragma once
/*
* Barry Hannigan
* 06/30/2016 - Created first version.
*
* License: This code is public domain but you buy me a beer if you use this and we meet someday (Beerware license).
*/

#include "AccelData.h"

void getAccelData(AccelData* accelCount);

void MMA8452_init(char* fName);
bool MMA8452_isValid();
// Remove this in Arduino -- use real function
int millis();
