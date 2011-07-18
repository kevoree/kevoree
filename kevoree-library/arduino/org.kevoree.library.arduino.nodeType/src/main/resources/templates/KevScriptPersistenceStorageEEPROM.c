
byte readPMemory(int preciseIndex){
    return EEPROM.read(preciseIndex);
}

void save2MemoryNoInc(int preciseIndex,byte b){
    EEPROM.write(preciseIndex,b);
}

void save2Memory(byte b){
    EEPROM.write(eepromIndex,b);eepromIndex++;
}

