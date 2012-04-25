
// Sparkfun 9DOF Razor IMU AHRS
// 9 Degree of Freedom Attitude and Heading Reference System
// Firmware v1.1gke

#if USING_UAVX == 1

byte TxCheckSum;

#if PRINT_UAVX_READABLE == 1

void TxByte(char b)
{
  Serial.print(b);
  Serial.print(',');
} // TxByte

void TxWord(int w)
{
  Serial.print(w);
  Serial.print(','); 
} // TxWord

#else

void TxByte(char b)
{
  Serial.write(b);
  TxCheckSum ^= b;
} // TxByte

void TxWord(int w)
{
  TxByte(lowByte(w));
  TxByte(highByte(w));
} // TxWord

#endif // PRINT_UAVX_READABLE



void SendAttitude(void)
{
  static byte i;

  TxByte(byte('$')); // sentinel not included in checksum
  TxCheckSum = 0;
  TxByte(UAVXRazorPacketTag);
  TxByte(23);

  // angles in milliradian
  TxWord((int)(Roll * 1000.0 ));
  TxWord((int)(Pitch * 1000.0 ));
  TxWord((int)(Yaw * 1000.0 )); 

  TxWord((int)(Omega_V[0] * 1000.0 ));
  TxWord((int)(Omega_V[1] * 1000.0 ));
  TxWord((int)(Omega_V[2] * 1000.0 ));
 
  TxWord((int)(Acc_V[0] * AccToMilliG));
  TxWord((int)(Acc_V[1] * AccToMilliG));
  TxWord((int)((Acc_V[2] - GRAVITY) * AccToMilliG));
  
  TxWord((int)(MagHeading * 1000.0));

  TxByte(TxCheckSum);

#if PRINT_UAVX_READABLE == 1
  Serial.println();
#endif // PRINT_UAVX_READABLE

} // SendAttitude

#else

static float last_roll=0;
static float last_yaw=0;
static float last_pitch=0;

  static  char str[15];
void SendAttitude(void)
{ 
  static byte i,j;

  //Serial.print("!");

#if PRINT_EULER == 1




  
float  AccMagnitude = sqrt(Acc_V[0]*Acc_V[0] + Acc_V[1]*Acc_V[1] + Acc_V[2]*Acc_V[2]);
  AccMagnitude = (AccMagnitude / (float)GRAVITY); // Scale to gravity.
  
    if(abs(last_roll-(Roll* 57.2957795131)) > 1 ||  abs(last_pitch-(Pitch* 57.2957795131)) > 1 || abs(last_yaw-(Yaw* 57.2957795131)) > 1  )
    { 
      
      last_roll  = Roll* 57.2957795131;
       last_pitch = Pitch* 57.2957795131; 
       last_yaw = Yaw* 57.2957795131; 
       
       
  Serial.print(Roll * 57.2957795131);

  Serial.print(",");
  Serial.print(Pitch * 57.2957795131);
  Serial.print(",");
  Serial.print(Yaw * 57.2957795131);
  Serial.print(",");
  Serial.println(AccMagnitude); 
    
    
    
    
int roll_d1 =(Roll * 57.2957795131);            // Get the integer part (678).
float roll_f2 = (Roll * 57.2957795131) - roll_d1;     // Get fractional part (678.0123 - 678 = 0.0123).
int roll_d2 = trunc(roll_f2 * 100);   

int pitch_d1 =(Pitch * 57.2957795131);            // Get the integer part (678).
float pitch_f2 = (Pitch * 57.2957795131) - pitch_d1;     // Get fractional part (678.0123 - 678 = 0.0123).
int pitch_d2 = trunc(pitch_f2 * 100);   

int yaw_d1 =(Yaw * 57.2957795131);            // Get the integer part (678).
float yaw_f2 = (Yaw * 57.2957795131) - yaw_d1;     // Get fractional part (678.0123 - 678 = 0.0123).
int yaw_d2 = trunc(yaw_f2 * 100);   



int acc_d1 =AccMagnitude;        // Get the integer part (678).
float acc_f2 = AccMagnitude - acc_d1;     // Get fractional part (678.0123 - 678 = 0.0123).
int acc_d2 = trunc(acc_f2 * 100);   





  // enable Slave Select
  digitalWrite(SS, LOW);    // SS is pin 10
  SPI.transfer (roll_d1);
  SPI.transfer (roll_d2);
  SPI.transfer (pitch_d1);
  SPI.transfer (pitch_d2);
  SPI.transfer (yaw_d1);
  SPI.transfer (yaw_d2);
  SPI.transfer (acc_d1);
  SPI.transfer (acc_d2);
  SPI.transfer ('\n');
  // disable Slave Select
  digitalWrite(SS, HIGH);
    }



  

 
#endif // PRINT_EULER

#if PRINT_ANALOGS == 1

  Serial.print("AN:");
  for ( i = 0; i < 3 ; i++ )
  {
    Serial.print(GyroADC[Map[i]]); 
    Serial.print(",");
  }
  for ( i = 0; i < 3 ; i++ )
  {
    Serial.print(AccADC[i]);
    Serial.print (",");
  }
  for ( i = 0; i < 3 ; i++ )
  { 
    Serial.print(Mag[i]); 
    Serial.print (","); 
  }

#endif // PRINT_ANALOGS

#if PRINT_DCM == 1

  Serial.print ("DCM:");
  for ( i = 0; i < 3 ; i++ )
    for ( j = 0; j < 3 ; j++ )
    {
      Serial.print(DCM_Matrix[i][j]*10000000);
      Serial.print (",");
    }

#endif // PRINT_DCM
 

} // SendAttitude

#endif // USING_UAVX


