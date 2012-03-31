
byte
readPMemory (int preciseIndex)
{
  return EEPROM.read (preciseIndex);
}

void
save2MemoryNoInc (int preciseIndex, byte b)
{
  EEPROM.write (preciseIndex, b);
}

void
save2Memory (byte b)
{
  EEPROM.write (eepromIndex, b);
  eepromIndex++;
}

int
eepromReadInt (int address)
{
  int value = 0x0000;
  value = value | (EEPROM.read (address) << 8);
  value = value | EEPROM.read (address + 1);
  return value;
}

void
save2MemoryInt (int value)
{
  EEPROM.write (eepromIndex, (value >> 8) & 0xFF);
  EEPROM.write (eepromIndex + 1, value & 0xFF);
  eepromIndex = eepromIndex + 2;
}
