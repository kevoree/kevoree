
// Sparkfun 9DOF Razor IMU AHRS
// 9 Degree of Freedom Attitude and Heading Reference System
// Axis definition (positive): 
// X axis forward (to the FTDI connector)
// Y axis  right 
// and Z axis down.
// Pitch up around Y
// Roll right around X
// and Yaw clockwise around Z

/* 
 Hardware version - v13
 	
 ATMega328@3.3V w/ external 8MHz resonator
 High Fuse DA
 Low Fuse FF
 	
 ADXL345: Accelerometer
 HMC5843: Magnetometer
 LY530:	Yaw Gyro
 LPR530:	Pitch and Roll Gyro
 
 Programmer : 3.3v FTDI
 Arduino IDE : Select board  "Arduino Duemilanove w/ATmega328"
 This code works also on ATMega168 Hardware
 */

#include <Wire.h>
#include <EEPROM.h>

#include <SPI.h>
#include "pins_arduino.h"

#define TRUE 1
#define FALSE 0

#define EEPROMBase 128  // keep clear of location zero

// ADXL345 Sensitivity(from datasheet) => 4mg/LSB   1G => 1000mg/4mg = 256 steps
#define GRAVITY 256

// LPR530 & LY530 Sensitivity (from datasheet) => (3.3mv at 3v) at 3.3v: 3mV/º/s, 3.22mV/ADC step => 0.93
// Tested values : 0.92
#define GyroToDegrees 0.92 // deg/sec
#define GyroToRadian 0.016057 // radian
#define RadianToGyro 62.2781 // gyro clicks
#define AccToMilliG (1000.0/(float)GRAVITY)

#define Kp_RollPitch 0.02
#define Ki_RollPitch 0.00002
#define Kp_Yaw 1.2
#define Ki_Yaw 0.00002

#define CyclemS 8    // standard for 16MHz UAVX
#define Freq ((1000/CyclemS)/2)
#define FilterA	((long)CyclemS*256)/(1000/(6*Freq)+CyclemS)

#define CORRECT_DRIFT 1

#define PRINT_EULER 1
#define PRINT_ANALOGS 0
#define PRINT_DCM 0

#define PRINT_UAVX_READABLE 0
#define USING_UAVX 0
#define FORCE_ACC_NEUTRALS 0
#define UAVXRazorPacketTag 17

#define SERIAL_INTERRUPT_OUTPUT

#define FREE_RUN 1

#define EXTENDED 1 // includes renormalisation recovery from V1.0

//_____________________________________________________________________

const  char  AccNeutralForced[3] = { 
  0,0,0 }; // determine neutrals in aircraft setup and load here 
const byte Map[3] = {
  1,2,0}; // Map the ADC channels gyro from z,x,y to x,y,z
const int AccSign[3] = {
  1,1,1};
const int MagSign[3] = {
  -1,-1,-1}; 

float   G_Dt = 0.01; // DCM integration time 100Hz if possible

long    ClockmSp;
long    PeriodmS;
long    ClockmS;
long    CompassmS = 0;
byte    CompassInterval = 0;

int     Gyro[3], GyroADC[3], GyroNeutral[3];
int     Acc[3], AccADC[3], AccNeutral[3];
char    AccNeutralUse[3];   

int     Mag[3], MagADC[3];
float   MagHeading;

float   Roll, Pitch, Yaw;

float   RollPitchError[3] = {
  0,0,0}; 
float   YawError[3] = {
  0,0,0};

byte i;
char ch;

void Initialise()
{
  static byte i, c;

  for( i = 0; i < 32; i++ )
  {
    GetGyro();
    GetAccelerometer();
    for( c = 0; c < 3; c++ )
    {
      GyroNeutral[c] += GyroADC[c];
      AccNeutral[c] += AccADC[c];
    }
    delay(20);
  }

  for( c = 0; c < 3; c++ )
  {
    GyroNeutral[c] = GyroNeutral[c]/32.0;
    AccNeutral[c] = ( AccNeutral[c] + 16 ) >> 5;
  }

  AccNeutral[2] -= GRAVITY * AccSign[2];

#if USING_UAVX == 0 
  for ( i = 0 ; i < 3 ; i++ )
    Serial.println(GyroNeutral[i]);
  for ( i = 0 ; i < 3 ; i++ )
    Serial.println(AccNeutral[i]);
#endif // USING_UAVX

  for ( i = 0 ; i < 3 ; i++)
#if FORCE_ACC_NEUTRALS
    AccNeutralUse[i] = AccNeutralForced[i]; 
#else
  AccNeutralUse[i] = AccNeutral[i];  
#endif // FORCE_ACC_NEUTRALS

  ClockmSp = millis();
  CompassmS = ClockmSp + 20; // 50Hz

} // Initialise

void setup()
{
 Serial.begin(115200);
  ADCReference(DEFAULT); 

  InitADCBuffers();
  InitADC();
  InitI2C();
  InitAccelerometers();
  GetAccelerometer();
  InitMagnetometer();

#if USING_UAVX == 0
 // Serial.println("Ready...");
#endif // USING_UAVX

  Initialise();
  
  
    SPI.begin ();

  // Slow down the master a bit
  SPI.setClockDivider(SPI_CLOCK_DIV8);
} // setup

void DoCompass(void)
{
    CompassmS = ClockmS + 20;
    GetMagnetometer();
    ComputeHeading();
} // DoCompass
  
void DoAttitude(void)
{
  ClockmSp = ClockmS;
  G_Dt = (float)PeriodmS / 1000.0;

  GetGyro(); 
  GetAccelerometer();   

  MUpdate(); 
  Normalize();
  DriftCorrection();
  EulerAngles(); 

  SendAttitude();
#ifdef USING_UAVX == 1
//  Serial.print("      ");
//  SendAttitude(); 
#endif //USING_UAVX
} // DoIteration

void loop()
{
  // free runs at ~160Hz
  ClockmS = millis();
  if ( ClockmS > CompassmS )
    DoCompass();
    
  PeriodmS = ClockmS - ClockmSp;
  #if FREE_RUN == 0
  if  ( PeriodmS >= CyclemS )
  #endif // FREE_RUN
    DoAttitude();
    
} // Main




