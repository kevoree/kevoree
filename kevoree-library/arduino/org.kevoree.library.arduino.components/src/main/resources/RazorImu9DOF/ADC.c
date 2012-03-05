
// Sparkfun 9DOF Razor IMU AHRS
// 9 Degree of Freedom Attitude and Heading Reference System


// use oversampling and averaging method to increase the ADC resolution
// do not bother with storing Gyro values as floats

byte MuxSel = 0;
byte ADCRefSHL6;
word ADCBuff[3];
byte ADCCount[3];

const int GyroSign[3] = {
  -1,1,-1 };

void InitADCBuffers(void)
{
  static byte c;

  for ( c = 0; c < 3; c++ )
  {
    noInterrupts();
    ADCBuff[c] = ADCCount[c] = 0;
    interrupts();
  }
} // InitADCBuffers 

void GetGyro(void)
{
  word s;    
  byte c, samples, m;    

  for ( c = 0 ; c < 3 ; c++ )
  {
    m = Map[c];
    noInterrupts();
    s = ADCBuff[m];              
    samples = ADCCount[m];
    interrupts(); 

    if ( samples > 0 ) // Check for divide by zero  
      GyroADC[c] = ( s + ( samples >> 1 ) ) / samples;
    Gyro[c] = GyroSign[c] * (GyroADC[c] - GyroNeutral[c]);
    
    //Gyro[c] = constrain(Gyro[c],-900,900); 
  }
 
  InitADCBuffers();

} // GetGyro

void ADCReference(byte m)
{ // why a call?
  ADCRefSHL6 = m << 6;
} // ADCReference

void InitADC(void)
{
  ADCSRA |= ( 1 << ADIE )|( 1 << ADEN );
  ADCSRA |= ( 1 << ADSC );
  delay(100);  // let ADC cycle a few times
} // InitADC

// cycle through gyro ADC channels at maximum conversion rate 
ISR(ADC_vect)
{
  static byte low, high;

  low = ADCL;
  high = ADCH;

  if ( ADCCount[MuxSel] < 64 ) // 1023*64 < 65K
  {
    ADCBuff[MuxSel] += int( high << 8 ) | low; // accumulate samples
    ADCCount[MuxSel]++;
  }

  if ( ++MuxSel > 2 ) MuxSel = 0; 
  ADMUX = ADCRefSHL6 | MuxSel;
  ADCSRA |= ( 1 << ADSC );   // start the conversion

} // ISR



