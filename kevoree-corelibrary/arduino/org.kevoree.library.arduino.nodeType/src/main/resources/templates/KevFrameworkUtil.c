

static KNOINLINE void
KevSerialPrint_P (PGM_P str)
{
  //for (uint8_t c; (c = pgm_read_byte(str)); str++) Serial.print(c);
  for (uint8_t c; (c = pgm_read_byte (str)); str++)
    Serial.print ((char) c);	// fix for arduino 1.0
}

static KNOINLINE void
KevSerialPrintln_P (PGM_P str)
{
  KevSerialPrint_P (str);
  Serial.println ();
}
