package org.kevoree.library.gossiperNetty.group

import org.junit.Test
import org.scalatest.junit.AssertionsForJUnit
import org.kevoree.library.gossiperNetty.version.Version

import java.util.UUID
import org.kevoree.library.gossiperNetty.version.Version.{ClockEntry, VectorClock}
import scala.collection.JavaConversions._
import org.kevoree.library.gossiperNetty.{Occured, VersionUtils}
import org.junit.Assert._

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 07/05/11
 * Time: 11:21
 */
class DataManagerForGroupLocalMergeTest extends AssertionsForJUnit {

  // this merge must not appears in the real application because vector clocks are equals
  @Test def mergeVectorClocksTest () {
    val remote: Version.VectorClock = Version.VectorClock.newBuilder
      .setTimestamp (System.currentTimeMillis)
      .addEnties (Version.ClockEntry.newBuilder.setNodeID ("duke").setVersion (1).setTimestamp (1l))
      .addEnties (Version.ClockEntry.newBuilder.setNodeID ("duke2").setVersion (1).setTimestamp (1l))
      .build
    val local: Version.VectorClock = Version.VectorClock.newBuilder
      .setTimestamp (System.currentTimeMillis)
      .addEnties (Version.ClockEntry.newBuilder.setNodeID ("duke").setVersion (1).setTimestamp (1l))
      .addEnties (Version.ClockEntry.newBuilder.setNodeID ("duke2").setVersion (1).setTimestamp (1l))
      .build
    val mergedClock: Version.VectorClock = Version.VectorClock.newBuilder ().
      setTimestamp (System.currentTimeMillis ()).
      addEnties (Version.ClockEntry.newBuilder ().setNodeID ("duke").setVersion (1)
      .setTimestamp (System.currentTimeMillis ())).
      addEnties (Version.ClockEntry.newBuilder ().setNodeID ("duke2").setVersion (1)
      .setTimestamp (System.currentTimeMillis ())).
      build ()

    ExtendedDataManagerForGroup.setNewVectorClock (local);
    val clock: Version.VectorClock = ExtendedDataManagerForGroup
      .mergeClock (UUID.nameUUIDFromBytes ("duke2".getBytes), remote, "");

    clock.getEntiesList.foreach {
      clockEntry: ClockEntry =>
        mergedClock.getEntiesList
          .find (sameclockEntry => clockEntry.getNodeID.equals (sameclockEntry.getNodeID)
          && clockEntry.getVersion.equals (sameclockEntry.getVersion)
          && (clockEntry.getTimestamp >= sameclockEntry.getTimestamp && clockEntry.getNodeID.equals ("duke")
          || clockEntry.getTimestamp >= sameclockEntry.getTimestamp && clockEntry.getNodeID.equals ("duke2"))) match {
          case Some (ce) => // NO OP
          case None => fail ()
        }
    }
  }

  @Test def mergeVectorClocksTestBis () {
    val remote: Version.VectorClock = Version.VectorClock.newBuilder
      .setTimestamp (System.currentTimeMillis)
      .addEnties (Version.ClockEntry.newBuilder.setNodeID ("duke").setVersion (2).setTimestamp (2l))
      .addEnties (Version.ClockEntry.newBuilder.setNodeID ("duke2").setVersion (1).setTimestamp (1l))
      .build
    val local: Version.VectorClock = Version.VectorClock.newBuilder
      .setTimestamp (System.currentTimeMillis)
      .addEnties (Version.ClockEntry.newBuilder.setNodeID ("duke").setVersion (1).setTimestamp (1l))
      .addEnties (Version.ClockEntry.newBuilder.setNodeID ("duke2").setVersion (1).setTimestamp (1l))
      .build
    val mergedClock: Version.VectorClock = Version.VectorClock.newBuilder ().
      setTimestamp (System.currentTimeMillis ()).
      addEnties (Version.ClockEntry.newBuilder ().setNodeID ("duke").setVersion (2)
      .setTimestamp (System.currentTimeMillis ())).
      addEnties (Version.ClockEntry.newBuilder ().setNodeID ("duke2").setVersion (1)
      .setTimestamp (System.currentTimeMillis ())).
      build ()

    ExtendedDataManagerForGroup.setNewVectorClock (local);
    val clock: Version.VectorClock = ExtendedDataManagerForGroup
      .mergeClock (UUID.nameUUIDFromBytes ("duke2".getBytes), remote, "");

    clock.getEntiesList.foreach {
      clockEntry: ClockEntry =>
        mergedClock.getEntiesList
          .find (sameclockEntry => clockEntry.getNodeID.equals (sameclockEntry.getNodeID)
          && clockEntry.getVersion.equals (sameclockEntry.getVersion)
          && (clockEntry.getTimestamp >= sameclockEntry.getTimestamp && clockEntry.getNodeID.equals ("duke")
          || clockEntry.getTimestamp >= sameclockEntry.getTimestamp && clockEntry.getNodeID.equals ("duke2"))) match {
          case Some (ce) => // NO OP
          case None => fail ()
        }
    }
  }

  // this merge must not appears in the real application because the local vector clock is more recent than the remote vector clock
  @Test def mergeVectorClocksTestSecond () {
    val remote: Version.VectorClock = Version.VectorClock.newBuilder
      .setTimestamp (System.currentTimeMillis)
      .addEnties (Version.ClockEntry.newBuilder.setNodeID ("duke").setVersion (1).setTimestamp (1l))
      .addEnties (Version.ClockEntry.newBuilder.setNodeID ("duke2").setVersion (1).setTimestamp (1l))
      .build
    val local: Version.VectorClock = Version.VectorClock.newBuilder
      .setTimestamp (System.currentTimeMillis)
      .addEnties (Version.ClockEntry.newBuilder.setNodeID ("duke").setVersion (1).setTimestamp (1l))
      .addEnties (Version.ClockEntry.newBuilder.setNodeID ("duke2").setVersion (2).setTimestamp (2l))
      .build
    val mergedClock: Version.VectorClock = Version.VectorClock.newBuilder ().
      setTimestamp (System.currentTimeMillis ()).
      addEnties (Version.ClockEntry.newBuilder ().setNodeID ("duke").setVersion (1)
      .setTimestamp (System.currentTimeMillis ())).
      addEnties (Version.ClockEntry.newBuilder ().setNodeID ("duke2").setVersion (2)
      .setTimestamp (System.currentTimeMillis ())).
      build ()

    ExtendedDataManagerForGroup.setNewVectorClock (local);
    val clock: Version.VectorClock = ExtendedDataManagerForGroup
      .mergeClock (UUID.nameUUIDFromBytes ("duke2".getBytes), remote, "");

    clock.getEntiesList.foreach {
      clockEntry: ClockEntry =>
        mergedClock.getEntiesList
          .find (sameclockEntry => clockEntry.getNodeID.equals (sameclockEntry.getNodeID)
          && clockEntry.getVersion.equals (sameclockEntry.getVersion)
          && (clockEntry.getTimestamp >= sameclockEntry.getTimestamp && clockEntry.getNodeID.equals ("duke")
          || clockEntry.getTimestamp >= sameclockEntry.getTimestamp && clockEntry.getNodeID.equals ("duke2"))) match {
          case Some (ce) => // NO OP
          case None => fail ()
        }
    }
  }

  @Test def mergeVectorClocksTest1 () {
    val remote: Version.VectorClock = Version.VectorClock.newBuilder ().
      setTimestamp (System.currentTimeMillis ()).
      addEnties (Version.ClockEntry.newBuilder ().setNodeID ("duke").setVersion (2).setTimestamp (1l)).
      addEnties (Version.ClockEntry.newBuilder ().setNodeID ("duke2").setVersion (1).setTimestamp (1l)).
      build ()
    val local: Version.VectorClock = Version.VectorClock.newBuilder ().
      setTimestamp (System.currentTimeMillis ()).
      //addEnties(Version.ClockEntry.newBuilder().setNodeID("duke").setVersion(1).setTimestamp(1l)).
      addEnties (Version.ClockEntry.newBuilder ().setNodeID ("duke2").setVersion (2).setTimestamp (2l)).
      build ()
    val mergedClock: Version.VectorClock = Version.VectorClock.newBuilder ().
      setTimestamp (System.currentTimeMillis ()).
      addEnties (Version.ClockEntry.newBuilder ().setNodeID ("duke").setVersion (2).setTimestamp (1l)).
      addEnties (Version.ClockEntry.newBuilder ().setNodeID ("duke2").setVersion (2)
      .setTimestamp (System.currentTimeMillis ())).
      build ()

    ExtendedDataManagerForGroup.setNewVectorClock (local);
    val clock: Version.VectorClock = ExtendedDataManagerForGroup
      .mergeClock (UUID.nameUUIDFromBytes ("duke2".getBytes), remote, "");

    clock.getEntiesList.foreach {
      clockEntry: ClockEntry =>
        mergedClock.getEntiesList
          .find (sameclockEntry => clockEntry.getNodeID.equals (sameclockEntry.getNodeID)
          && clockEntry.getVersion.equals (sameclockEntry.getVersion)
          && (clockEntry.getTimestamp == sameclockEntry.getTimestamp && clockEntry.getNodeID.equals ("duke")
          || clockEntry.getTimestamp >= sameclockEntry.getTimestamp && clockEntry.getNodeID.equals ("duke2"))) match {
          case Some (ce) => // NO OP
          case None => fail ()
        }
    }
  }

  @Test def mergeVectorClocksTest1Bis () {
    val remote: Version.VectorClock = Version.VectorClock.newBuilder ().
      setTimestamp (System.currentTimeMillis ()).
      addEnties (Version.ClockEntry.newBuilder ().setNodeID ("duke").setVersion (2).setTimestamp (2l)).
      //addEnties (Version.ClockEntry.newBuilder ().setNodeID ("duke2").setVersion (1).setTimestamp (1l)).
      build ()
    val local: Version.VectorClock = Version.VectorClock.newBuilder ().
      setTimestamp (System.currentTimeMillis ()).
      addEnties (Version.ClockEntry.newBuilder ().setNodeID ("duke").setVersion (1).setTimestamp (1l)).
      addEnties (Version.ClockEntry.newBuilder ().setNodeID ("duke2").setVersion (2).setTimestamp (2l)).
      build ()
    val mergedClock: Version.VectorClock = Version.VectorClock.newBuilder ().
      setTimestamp (System.currentTimeMillis ()).
      addEnties (Version.ClockEntry.newBuilder ().setNodeID ("duke").setVersion (2)
      .setTimestamp (System.currentTimeMillis ())).
      addEnties (Version.ClockEntry.newBuilder ().setNodeID ("duke2").setVersion (2).setTimestamp (2l)).
      build ()

    ExtendedDataManagerForGroup.setNewVectorClock (local);
    val clock: Version.VectorClock = ExtendedDataManagerForGroup
      .mergeClock (UUID.nameUUIDFromBytes ("duke2".getBytes), remote, "");

    clock.getEntiesList.foreach {
      clockEntry: ClockEntry =>
        mergedClock.getEntiesList
          .find (sameclockEntry => clockEntry.getNodeID.equals (sameclockEntry.getNodeID)
          && clockEntry.getVersion.equals (sameclockEntry.getVersion)
          && (clockEntry.getTimestamp >= sameclockEntry.getTimestamp && clockEntry.getNodeID.equals ("duke")
          || clockEntry.getTimestamp == sameclockEntry.getTimestamp && clockEntry.getNodeID.equals ("duke2"))) match {
          case Some (ce) => // NO OP
          case None => fail ()
        }
    }
  }

  @Test def mergeVectorClocksTest2 () {
    val remote: Version.VectorClock = Version.VectorClock.newBuilder ().
      setTimestamp (System.currentTimeMillis ()).
      addEnties (Version.ClockEntry.newBuilder ().setNodeID ("duke").setVersion (2).setTimestamp (1l)).
      addEnties (Version.ClockEntry.newBuilder ().setNodeID ("duke2").setVersion (3).setTimestamp (1l)).
      build ()
    val local: Version.VectorClock = Version.VectorClock.newBuilder ().
      setTimestamp (System.currentTimeMillis ()).
      addEnties (Version.ClockEntry.newBuilder ().setNodeID ("duke3").setVersion (1).setTimestamp (1l)).
      addEnties (Version.ClockEntry.newBuilder ().setNodeID ("duke2").setVersion (2).setTimestamp (2l)).
      build ()
    val mergedClock: Version.VectorClock = Version.VectorClock.newBuilder ().
      setTimestamp (System.currentTimeMillis ()).
      addEnties (Version.ClockEntry.newBuilder ().setNodeID ("duke").setVersion (2).setTimestamp (1l)).
      addEnties (Version.ClockEntry.newBuilder ().setNodeID ("duke2").setVersion (3)
      .setTimestamp (System.currentTimeMillis ())).
      addEnties (Version.ClockEntry.newBuilder ().setNodeID ("duke3").setVersion (1).setTimestamp (1l)).
      build ()

    ExtendedDataManagerForGroup.setNewVectorClock (local);
    val clock: Version.VectorClock = ExtendedDataManagerForGroup
      .mergeClock (UUID.nameUUIDFromBytes ("duke2".getBytes), remote, "");

    clock.getEntiesList.foreach {
      clockEntry: ClockEntry =>
        mergedClock.getEntiesList
          .find (sameclockEntry => clockEntry.getNodeID.equals (sameclockEntry.getNodeID)
          && clockEntry.getVersion.equals (sameclockEntry.getVersion)
          && (clockEntry.getTimestamp == sameclockEntry.getTimestamp && clockEntry.getNodeID.equals ("duke")
          || clockEntry.getTimestamp >= sameclockEntry.getTimestamp && clockEntry.getNodeID.equals ("duke2")
          || clockEntry.getTimestamp == sameclockEntry.getTimestamp && clockEntry.getNodeID.equals ("duke3"))) match {
          case Some (ce) => // NO OP
          case None => fail ()
        }
    }
  }

  @Test def mergeVectorClocksTest2Bis () {
    val remote: Version.VectorClock = Version.VectorClock.newBuilder ().
      setTimestamp (System.currentTimeMillis ()).
      addEnties (Version.ClockEntry.newBuilder ().setNodeID ("duke").setVersion (2).setTimestamp (1l)).
      addEnties (Version.ClockEntry.newBuilder ().setNodeID ("duke3").setVersion (3).setTimestamp (1l)).
      build ()
    val local: Version.VectorClock = Version.VectorClock.newBuilder ().
      setTimestamp (System.currentTimeMillis ()).
      addEnties (Version.ClockEntry.newBuilder ().setNodeID ("duke3").setVersion (1).setTimestamp (1l)).
      addEnties (Version.ClockEntry.newBuilder ().setNodeID ("duke2").setVersion (2).setTimestamp (2l)).
      build ()
    val mergedClock: Version.VectorClock = Version.VectorClock.newBuilder ().
      setTimestamp (System.currentTimeMillis ()).
      addEnties (Version.ClockEntry.newBuilder ().setNodeID ("duke").setVersion (2).setTimestamp (1l)).
      addEnties (Version.ClockEntry.newBuilder ().setNodeID ("duke2").setVersion (2).setTimestamp (2l)).
      addEnties (Version.ClockEntry.newBuilder ().setNodeID ("duke3").setVersion (3)
      .setTimestamp (System.currentTimeMillis ())).
      build ()

    ExtendedDataManagerForGroup.setNewVectorClock (local);
    val clock: Version.VectorClock = ExtendedDataManagerForGroup
      .mergeClock (UUID.nameUUIDFromBytes ("duke2".getBytes), remote, "");

    clock.getEntiesList.foreach {
      clockEntry: ClockEntry =>
        mergedClock.getEntiesList
          .find (sameclockEntry => clockEntry.getNodeID.equals (sameclockEntry.getNodeID)
          && clockEntry.getVersion.equals (sameclockEntry.getVersion)
          && (clockEntry.getTimestamp == sameclockEntry.getTimestamp && clockEntry.getNodeID.equals ("duke")
          || clockEntry.getTimestamp == sameclockEntry.getTimestamp && clockEntry.getNodeID.equals ("duke2")
          || clockEntry.getTimestamp >= sameclockEntry.getTimestamp && clockEntry.getNodeID.equals ("duke3"))) match {
          case Some (ce) => // NO OP
          case None => fail ()
        }
    }
  }

  @Test def mergeVectorClocksTest3 () {
    val remote: Version.VectorClock = Version.VectorClock.newBuilder
      .setTimestamp (System.currentTimeMillis)
      .addEnties (Version.ClockEntry.newBuilder.setNodeID ("duke").setVersion (2).setTimestamp (1l))
      .addEnties (Version.ClockEntry.newBuilder.setNodeID ("duke2").setVersion (1).setTimestamp (1l)).build
    val local: Version.VectorClock = Version.VectorClock.newBuilder
      .setTimestamp (System.currentTimeMillis)
      .addEnties (Version.ClockEntry.newBuilder.setNodeID ("duke").setVersion (1).setTimestamp (1l))
      .addEnties (Version.ClockEntry.newBuilder.setNodeID ("duke2").setVersion (2).setTimestamp (1l)).build
    val mergedClock: Version.VectorClock = Version.VectorClock.newBuilder ().
      setTimestamp (System.currentTimeMillis ()).
      addEnties (Version.ClockEntry.newBuilder ().setNodeID ("duke").setVersion (2).setTimestamp (System.currentTimeMillis ())).
      addEnties (Version.ClockEntry.newBuilder ().setNodeID ("duke2").setVersion (2).setTimestamp (System.currentTimeMillis ())).
      build ()

    //assertTrue(VersionUtils.compare(local, remote).equals(Occured.BEFORE));
    ExtendedDataManagerForGroup.setNewVectorClock (local);
    val clock: Version.VectorClock = ExtendedDataManagerForGroup
      .mergeClock (UUID.nameUUIDFromBytes ("duke2".getBytes), remote, "");

    clock.getEntiesList.foreach {
      clockEntry: ClockEntry =>
        mergedClock.getEntiesList
          .find (sameclockEntry => clockEntry.getNodeID.equals (sameclockEntry.getNodeID)
          && clockEntry.getVersion.equals (sameclockEntry.getVersion)
           && (clockEntry.getTimestamp >= sameclockEntry.getTimestamp && clockEntry.getNodeID.equals ("duke")
          || clockEntry.getTimestamp >= sameclockEntry.getTimestamp && clockEntry.getNodeID.equals ("duke2"))) match {
          case Some (ce) => // NO OP
          case None => fail ()
        }
    }
  }

  object ExtendedDataManagerForGroup extends DataManagerForGroup ("group1", "duke2", null) {

    this.start()

    def setNewVectorClock (vc: Version.VectorClock) {
      setVectorClock (vc)
    }
  }

}
