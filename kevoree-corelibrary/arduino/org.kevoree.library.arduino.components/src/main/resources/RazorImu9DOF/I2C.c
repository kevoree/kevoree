// Sparkfun 9DOF Razor IMU AHRS
// 9 Degree of Freedom Attitude and Heading Reference System

// I2C code for ADXL345 accelerometer and HMC5843 magnetometer 
 
#define I2C_ACC_ID 0x53
#define I2C_COMPASS_ID 0x1E //0x3C //0x3D;  //(0x42>>1);

void InitI2C(void)
{
  Wire.begin();
} // InitI2C

void InitAccelerometers()
{
  delay(100);  // let accelerometers settle

  Wire.beginTransmission(I2C_ACC_ID);
  Wire.write(0x2D);  // power register
  Wire.write(0x08);  // measurement mode
  Wire.endTransmission();
  delay(5);
  Wire.beginTransmission(I2C_ACC_ID);
  Wire.write(0x31);  // Data format register
  Wire.write(0x08);  // set to full resolution
  Wire.endTransmission();
  delay(5);

  Wire.beginTransmission(I2C_ACC_ID);
  Wire.write(0x2C);  // Rate
  Wire.write(0x09);  // set to 50Hz, normal operation
  Wire.endTransmission();
  delay(5);
} // InitAccelerometers

void GetAccelerometer()
{
  static int i;
  static byte b[6];

  Wire.beginTransmission(I2C_ACC_ID); 
  Wire.write(0x32);     
  Wire.endTransmission(); 

  Wire.beginTransmission(I2C_ACC_ID); 
  Wire.requestFrom(I2C_ACC_ID, 6);    // request 6 bytes from device

  i = 0; 
  while(Wire.available()) 
    b[i++] = Wire.read();  

  Wire.endTransmission(); 

  AccADC[0] = int(b[3]) << 8 | b[2];    // X axis (internal sensor y axis)
  AccADC[1] = int(b[1]) << 8 | b[0];    // Y axis (internal sensor x axis)
  AccADC[2] = int(b[5]) << 8 | b[4];    // Z axis

  for ( i = 0; i < 3 ; i++ )
    Acc[i] = AccSign[i] * (AccADC[i] - AccNeutralUse[i]);

} // GetAccelerometer

//_____________________________________________________________________

void InitMagnetometer()
{
  delay(100); // let magnetometer settle
  Wire.beginTransmission(I2C_COMPASS_ID);
  Wire.write(0x02); 
  Wire.write((uint8_t)0x00);   // continuous mode (default to 10Hz)
  Wire.endTransmission(); 
} // InitMagnetometer

void GetMagnetometer(void)
{
  static int i;
  static byte b[6];

  Wire.beginTransmission(I2C_COMPASS_ID); 
  Wire.write(0x03);
  Wire.endTransmission(); 

  Wire.beginTransmission(I2C_COMPASS_ID); 
  Wire.requestFrom(I2C_COMPASS_ID, 6);    // 6 bytes

  i = 0;
  while(Wire.available())
    b[i++] = Wire.read();  

  Wire.endTransmission(); //end transmission

  Mag[0] = MagSign[0] * (int(b[2]) << 8 | b[3]);    // X axis (internal sensor y axis)
  Mag[1] = MagSign[1] * (int(b[0]) << 8 | b[1]);    // Y axis (internal sensor x axis)
  Mag[2] = MagSign[2] * (int(b[4]) << 8 | b[5]);    // Z axis

} // GetMagnetometer


