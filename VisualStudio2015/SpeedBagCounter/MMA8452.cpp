#include "MMA8452.h"
/*
* Barry Hannigan
* 06/30/2016 - Created first version.
*
* License: This code is public domain but you buy me a beer if you use this and we meet someday (Beerware license).
*/


void readAccelData(int* accelCount);

void getAccelData(AccelData* dataPtr)
{
	int accelCount[3];  // Stores the 12-bit signed value
	readAccelData(accelCount);  // Read the x/y/z adc values
	dataPtr->timeStamp = millis();
	dataPtr->minZFound = 0;
	dataPtr->maxZFound = 0;
	dataPtr->z = accelCount[2]; // Only using Z now
/*
	//Print the readings for logging to an OpenLog
	Serial.print(millis());
	Serial.print(",");
	Serial.print(accelCount[0]);
	Serial.print(",");
	Serial.print(accelCount[1]);
	Serial.print(",");
	Serial.println(accelCount[2]);
*/

}

// ***************************************
// For simulation only, do not port to Arduino
#include <iostream>
#include <fstream>
#include <sstream>

using namespace std;

void parseNextLine();
bool dataValid = true;
ifstream* fin;
int curMilli;
int timeStamp;
int xVal;
int yVal;
int zVal;

bool MMA8452_isValid()
{
	return dataValid;
}

void readAccelData(int* accelCount)
{
	curMilli = timeStamp;
	accelCount[0] = xVal;
	accelCount[1] = yVal;
	accelCount[2] = zVal;
	parseNextLine();
}

int millis()
{
	return curMilli;
}

void MMA8452_init(char* fName)
{
	fin = new ifstream(fName);
	if (!fin->is_open())
		printf("File NOT open\n");
	parseNextLine();
	parseNextLine();
	parseNextLine();
}

void parseNextLine()
{
	string line;
	string field;


	dataValid = false;
	int charsRead = 0;
	// Get next line
	while (getline(*fin, line))
	{
		istringstream tokenizer(line);

		// Timestamp
		getline(tokenizer, field, ',');
		if (field.length() == 0)
			continue;
		istringstream tsf(field);
		tsf >> timeStamp;
		// X
		getline(tokenizer, field, ',');
		if (field.length() == 0)
			continue;
		istringstream xf(field);
		xf >> xVal;
		// Y
		getline(tokenizer, field, ',');
		if (field.length() == 0)
			continue;
		istringstream yf(field);
		yf >> yVal;
		// Z
		getline(tokenizer, field, ',');
		if (field.length() == 0)
			continue;
		istringstream zf(field);
		zf >> zVal;

		dataValid = true;
		return;
	}
}

