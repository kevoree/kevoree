
#define SD_SUBBUF_SIZE 50
byte sdLocalBuffer[SD_SUBBUF_SIZE];
int subBufIndex = -60;
byte
readPMemory (int preciseIndex)
{
  if ((preciseIndex > (subBufIndex + SD_SUBBUF_SIZE - 1))
      || (preciseIndex < subBufIndex))
    {
      if (file.curPosition () != preciseIndex)
	{
	  file.seekSet (preciseIndex);
	}
      // return file.read();
      file.read (&sdLocalBuffer, SD_SUBBUF_SIZE);
      subBufIndex = preciseIndex;
    }
  return sdLocalBuffer[(preciseIndex - subBufIndex)];
}

void
save2MemoryNoInc (int preciseIndex, byte b)
{
  subBufIndex = -60;
  if (file.curPosition () != preciseIndex)
    {
      file.seekSet (preciseIndex);
    }
  file.write (b);
  if (b == endAdminChar | b == sepAdminChar)
    file.sync ();
}

void
save2Memory (byte b)
{
  subBufIndex = -60;
  if (file.curPosition () != eepromIndex)
    {
      file.seekSet (eepromIndex);
    }
  file.write (b);
  if (b == endAdminChar | b == sepAdminChar)
    file.sync ();
  eepromIndex++;
}
