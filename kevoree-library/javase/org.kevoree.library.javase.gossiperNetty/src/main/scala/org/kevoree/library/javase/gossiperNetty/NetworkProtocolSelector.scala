package org.kevoree.library.javase.gossiperNetty

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 12/09/11
 * Time: 11:04
 */

class NetworkProtocolSelector() {

  var metadataProtocol : NetworkActor = _;
  var dataProtocol: NetworkActor = _;

  def selectForMetaData() : NetworkActor ={
    metadataProtocol
  }

  def selectForData() : NetworkActor ={
    dataProtocol
  }

  def setProtocolForMetadata(metadataProtocol : NetworkActor) {
    this.metadataProtocol = metadataProtocol
  }

  def setProtocolForData(dataProtocol : NetworkActor) {
    this.dataProtocol = dataProtocol
  }

}