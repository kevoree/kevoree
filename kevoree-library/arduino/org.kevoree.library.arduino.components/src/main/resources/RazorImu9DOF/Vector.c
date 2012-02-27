// Sparkfun 9DOF Razor IMU AHRS
// 9 Degree of Freedom Attitude and Heading Reference System
// Firmware v1.1gke


float VDot(float v1[3], float v2[3])
{
  static float op;
  static byte i;

  op = 0.0; 
  for( i = 0; i < 3; i++ )
    op += v1[i] * v2[i];

  return op; 
} // VDot

void VCross(float VOut[3], float v1[3], float v2[3])
{
  VOut[0]= (v1[1] * v2[2]) - (v1[2] * v2[1]);
  VOut[1]= (v1[2] * v2[0]) - (v1[0] * v2[2]);
  VOut[2]= (v1[0] * v2[1]) - (v1[1] * v2[0]);
} // VCross
 
void VScale(float VOut[3], float v[3], float s)
{
  static byte i;

  for ( i = 0; i < 3; i++ )
    VOut[i] = v[i] * s; 
} // VScale

void VAdd(float VOut[3],float v1[3], float v2[3])
{
  static byte i;

  for ( i = 0; i < 3; i++ )
    VOut[i] = v1[i] + v2[i];
} // VAdd


