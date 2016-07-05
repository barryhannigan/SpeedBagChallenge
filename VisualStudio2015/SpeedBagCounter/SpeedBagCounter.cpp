// SpeedBagCounter.cpp
//
// This file contains the drop in replacement for Nate's
// loop function in SpeedBagCounter.ino
//


#define DISPLAY_ADDRESS 0x71 //I2C address of OpenSegment display

int hitCounter = 0; //Keeps track of the number of hits

const int resetButton = 6; //Button that resets the display and counter
const int LED = 13; //Status LED on D3

long lastPrint; //Used for printing updates every second

//boolean displayOn; //Used to track if display is turned off or not

long TIME_TO_DISPLAY_OFF = 60L * 1000L * 5L; //5 minutes of no use

int DEFAULT_BRIGHTNESS = 50; //50% brightness to avoid burning out segments after 3 years of use


// *********************************
// This section for VS2015 compile, do not
// port to arduino
#include <stdio.h>
#include <math.h>
#include "MMA8452.h"

bool displayOn;
void loop();
void setup();

void printHits()
{
	printf("Hits = %d\n", hitCounter);
}

int main()
{
	char* fname = "3-77Hits.TXT";
	printf("Hello World!\n");
	MMA8452_init(fname);
	setup();
	while (MMA8452_isValid())
	{
		loop();
	}
	printf("Hit Counter = %d\n", hitCounter);
	return 0;
}

// ******************************************************
// Start of code to move to Arduino
#include "AccelData.h"
#include "FEP.h"
#include "DET.h"

AccelData newData;
FEPFilterKernel fep;
DETFilterKernel det;

void setup()
{
#ifdef BUILDING_FOR_ARDUINO
	wdt_reset(); //Pet the dog
	wdt_disable(); //We don't want the watchdog during init

	pinMode(resetButton, INPUT_PULLUP);
	pinMode(LED, OUTPUT);

	//By default .begin() will set I2C SCL to Standard Speed mode of 100kHz
	Wire.setClock(400000); //Optional - set I2C SCL to High Speed Mode of 400kHz
	Wire.begin(); //Join the bus as a master

	Serial.begin(115200);
	Serial.println("Speed Bag Counter");

	initDisplay();

	clearDisplay();
	Wire.beginTransmission(DISPLAY_ADDRESS);
	Wire.print("Accl"); //Display an error until accel comes online
	Wire.endTransmission();

	while (!initMMA8452()) //Test and intialize the MMA8452
		; //Do nothing

	clearDisplay();
	Wire.beginTransmission(DISPLAY_ADDRESS);
	Wire.print("0000");
	Wire.endTransmission();

	lastPrint = millis();
	lastHitTime = millis();

	wdt_enable(WDTO_250MS); //Unleash the beast
#endif
	FEP_init();
	FEPFilter_init(&fep);
	DET_init();
	DETFilter_init(&det);
	AccelData dummyData;
	getAccelData(&dummyData);
	for (int i = 0; i < FEPFilterKernel::FILTER_TAP_NUM; i++)
	{
		//System.out.println("Priming Filter");
		FEPFilter_put(&fep, &dummyData);
	}
	for (int i = 0; i < DETFilterKernel::FILTER_TAP_NUM; i++)
	{
		//System.out.println("Priming Filter");
		FEPFilter_put(&fep, &dummyData);
		DETFilter_put(&det, FEPFilter_get(&fep));
	}
}

void loop()
{
#if BUILDING_FOR_ARDUINO
	wdt_reset(); //Pet the dog

	currentTime = millis();
	if ((unsigned long)(currentTime - lastPrint) >= 1000)
	{
		if (digitalRead(LED) == LOW)
			digitalWrite(LED, HIGH);
		else
			digitalWrite(LED, LOW);

		lastPrint = millis();
	}

	//See if we should power down the display due to inactivity
	if (displayOn == true)
	{
		currentTime = millis();
		if ((unsigned long)(currentTime - lastHitTime) >= TIME_TO_DISPLAY_OFF)
		{
			Serial.println("Power save");

			hitCounter = 0; //Reset the count

			clearDisplay(); //Clear to save power
			displayOn = false;
		}
	}
#endif

	getAccelData(&newData);
	//printf("Timestamp = %d, z = %d\n", newData.timeStamp, newData.z);
	FEPFilter_put(&fep, &newData);
	AccelData* fepData = FEPFilter_get(&fep);
	DETFilter_put(&det, fepData);
	AccelData* detData = DETFilter_get(&det);
	if (detData->maxZFound > 0)
	{
		//We really do have a hit!
		hitCounter++;

		//Serial.print("Hit: ");
		//Serial.println(hitCounter);

		if (displayOn == false)
			displayOn = true;

		printHits(); //Updates the display
	}

#if 0
	//Check if we need to reset the counter and display
	if (digitalRead(resetButton) == LOW)
	{
		//This breaks the file up so we can see where we hit the reset button
		Serial.println();
		Serial.println();
		Serial.println("Reset!");
		Serial.println();
		Serial.println();

		hitCounter = 0;

		resetDisplay(); //Forces cursor to beginning of display
		printHits(); //Updates the display

		while (digitalRead(resetButton) == LOW) wdt_reset(); //Pet the dog while we wait for you to remove finger

															 //Do nothing for 250ms after you press the button, a sort of debounce
		for (int x = 0; x < 25; x++)
		{
			wdt_reset(); //Pet the dog
			delay(10);
		}
	}
#endif
}

#if 0
//This function makes sure the display is at 57600
void initDisplay()
{
	resetDisplay(); //Forces cursor to beginning of display

	printHits(); //Update display with current hit count

	displayOn = true;

	setBrightness(DEFAULT_BRIGHTNESS);
}

//Set brightness of display
void setBrightness(int brightness)
{
	Wire.beginTransmission(DISPLAY_ADDRESS);
	Wire.write(0x7A); // Brightness control command
	Wire.write(brightness); // Set brightness level: 0% to 100%
	Wire.endTransmission();
}

void resetDisplay()
{
	//Send the reset command to the display - this forces the cursor to return to the beginning of the display
	Wire.beginTransmission(DISPLAY_ADDRESS);
	Wire.write('v');
	Wire.endTransmission();

	if (displayOn == false)
	{
		setBrightness(DEFAULT_BRIGHTNESS); //Power up display
		displayOn = true;
		lastHitTime = millis();
	}
}

//Push the current hit counter to the display
void printHits()
{
	int tempCounter = hitCounter / 2; //Cut in half

	Wire.beginTransmission(DISPLAY_ADDRESS);
	Wire.write(0x79); //Move cursor
	Wire.write(4); //To right most position

	Wire.write(tempCounter / 1000); //Send the left most digit
	tempCounter %= 1000; //Now remove the left most digit from the number we want to display
	Wire.write(tempCounter / 100);
	tempCounter %= 100;
	Wire.write(tempCounter / 10);
	tempCounter %= 10;
	Wire.write(tempCounter); //Send the right most digit

	Wire.endTransmission(); //Stop I2C transmission
}

//Clear display to save power (a screen saver of sorts)
void clearDisplay()
{
	Wire.beginTransmission(DISPLAY_ADDRESS);
	Wire.write(0x79); //Move cursor
	Wire.write(4); //To right most position

	Wire.write(' ');
	Wire.write(' ');
	Wire.write(' ');
	Wire.write(' ');

	Wire.endTransmission(); //Stop I2C transmission
}
#endif

