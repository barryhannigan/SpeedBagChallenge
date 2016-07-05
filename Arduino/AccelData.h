#pragma once
/*
 * Barry Hannigan 
 * 06/30/2016 - Created first version.
 * 
 * License: This code is public domain but you buy me a beer if you use this and we meet someday (Beerware license).
*/

// Data format to pass through the processing path
struct AccelData
{
  int timeStamp;
  int minZFound;
  int maxZFound;
  int z;
  int avgZ;
};

