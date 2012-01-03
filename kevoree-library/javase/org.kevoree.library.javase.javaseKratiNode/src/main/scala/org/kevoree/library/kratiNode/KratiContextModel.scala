package org.kevoree.library.kratiNode

import krati.core.{StoreConfig, StoreFactory}
import java.io.File
import org.slf4j.LoggerFactory
import org.kevoree.api.service.core.handler.{ContextKey, ContextModel}
import java.util.Map
import krati.store.DataStore
import krati.core.segment.{MappedSegmentFactory}


/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 03/01/12
 * Time: 13:10
 * To change this template use File | Settings | File Templates.
 */

class KratiContextModel extends ContextModel {

  val tempFile = File.createTempFile("-kratiStore-","-kevoreetemp-")
  tempFile.delete()
  tempFile.mkdirs()
  val logger = LoggerFactory.getLogger(this.getClass)
  logger.info("Katri Data Store Home : "+tempFile.getAbsolutePath)

  val config = new StoreConfig(tempFile, 2);
  config.setSegmentFactory(new MappedSegmentFactory());
  config.setSegmentFileSizeMB(8);

  private val dataStore : DataStore[Array[Byte],Array[Byte]] = StoreFactory.createIndexedDataStore(config)

  def start() {
    dataStore.open()
  }

  def stop(){
    dataStore.close();
  }

  def get(p1: ContextKey): Array[Byte] = {
    dataStore.get(p1.toString.getBytes)
  }

  def select(p1: ContextKey): Map[ContextKey, Array[Byte]] = {
    null
  }

  def put(key: ContextKey, value: Array[Byte]) {
    dataStore.put(key.toString.getBytes,value)
  }
}