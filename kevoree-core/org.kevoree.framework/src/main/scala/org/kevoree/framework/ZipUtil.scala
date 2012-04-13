package org.kevoree.framework

import java.io.ByteArrayOutputStream
import java.util.zip.{Inflater, Deflater}

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 13/04/12
 * Time: 13:39
 */

object ZipUtil {

    def compressByteArray(input: Array[Byte]): Array[Byte] = {
      val compressor  = new Deflater()
      compressor.setLevel(Deflater.BEST_COMPRESSION)
      compressor.setInput(input)
      compressor.finish
      val bos = new ByteArrayOutputStream(input.length);
      val buf = new Array[Byte](1024)
      while (!compressor.finished) {
        val count = compressor.deflate(buf)
        bos.write(buf, 0, count)
      }
      try {
        bos.close
        bos.flush()
      }
      catch {
        case _ => {
          None
        }
      }
        bos.toByteArray
    }

    def uncompressByteArray(compressedData: Array[Byte]): Array[Byte] = {
      val decompressor = new Inflater()
      decompressor.setInput(compressedData)
      val bos = new ByteArrayOutputStream(compressedData.length);
      val buf: Array[Byte] = new Array[Byte](1024)
      while (!decompressor.finished) {
        try {
          val count: Int = decompressor.inflate(buf)
          bos.write(buf, 0, count)
        }
        catch {
          case _ => {
            None
          }
        }
      }
      try {
        bos.close
        bos.flush()
      }
      catch {
        case _ => {
          None
        }
      }
      bos.toByteArray
    }

}