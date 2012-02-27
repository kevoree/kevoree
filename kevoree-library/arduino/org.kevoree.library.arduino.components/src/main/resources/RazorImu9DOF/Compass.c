
// Sparkfun 9DOF Razor IMU AHRS
// 9 Degree of Freedom Attitude and Heading Reference System

// Local magnetic declination not included
// http://www.ngdc.noaa.gov/geomagmodels/Declination.jsp

void ComputeHeading(void)
{
  static float MX, MY;
  static float CosRoll, SinRoll, CosPitch, SinPitch;
    
  CosRoll = cos(Roll);
  SinRoll = sin(Roll);
  CosPitch = cos(Pitch);
  SinPitch = sin(Pitch);
  
  // Tilt compensated Magnetic field X:
  MX = (float)Mag[0] * CosPitch + (float)Mag[1] * SinRoll * SinPitch + (float)Mag[2] * CosRoll * SinPitch;
  	
  // Tilt compensated Magnetic field Y:
  MY = (float)Mag[1] * CosRoll - (float)Mag[2] * SinRoll;
  
  // Magnetic Heading
  MagHeading = atan2( -MY, MX );

} // ComputeHeading

