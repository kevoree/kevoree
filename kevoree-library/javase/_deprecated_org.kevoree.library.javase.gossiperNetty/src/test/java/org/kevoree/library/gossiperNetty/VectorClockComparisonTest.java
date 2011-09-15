package org.kevoree.library.gossiperNetty;

import org.junit.Test;
import org.kevoree.library.gossiperNetty.version.Version;

import static org.junit.Assert.assertTrue;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 06/05/11
 * Time: 10:00
 */
public class VectorClockComparisonTest {

	@Test
	public void vectorClockComparisonAFTER1Test() {
		Version.VectorClock remote = Version.VectorClock.newBuilder().
				setTimestamp(System.currentTimeMillis()).
				addEnties(Version.ClockEntry.newBuilder().setNodeID("duke").setVersion(1)/*.setTimestamp(1l)*/).
				addEnties(Version.ClockEntry.newBuilder().setNodeID("duke2").setVersion(1)/*.setTimestamp(1l)*/).
				build();
		Version.VectorClock local = Version.VectorClock.newBuilder().
				setTimestamp(System.currentTimeMillis()).
				addEnties(Version.ClockEntry.newBuilder().setNodeID("duke").setVersion(1)/*.setTimestamp(1l)*/).
				addEnties(Version.ClockEntry.newBuilder().setNodeID("duke2").setVersion(1)/*.setTimestamp(1l)*/).
				build();

		assertTrue(VersionUtils.compare(local, remote).equals(Occured.AFTER));
	}

	@Test
	public void vectorClockComparisonAFTER2Test() {
		Version.VectorClock remote = Version.VectorClock.newBuilder().
				setTimestamp(System.currentTimeMillis()).
				addEnties(Version.ClockEntry.newBuilder().setNodeID("duke").setVersion(1)/*.setTimestamp(1l)*/).
				addEnties(Version.ClockEntry.newBuilder().setNodeID("duke2").setVersion(1)/*.setTimestamp(1l)*/).
				build();
		Version.VectorClock local = Version.VectorClock.newBuilder().
				setTimestamp(System.currentTimeMillis()).
				addEnties(Version.ClockEntry.newBuilder().setNodeID("duke").setVersion(1)/*.setTimestamp(1l)*/).
				addEnties(Version.ClockEntry.newBuilder().setNodeID("duke2").setVersion(2)/*.setTimestamp(1l)*/).
				build();

		assertTrue(VersionUtils.compare(local, remote).equals(Occured.AFTER));
	}

	@Test
	public void vectorClockComparisonAFTER3Test() {
		Version.VectorClock remote = Version.VectorClock.newBuilder().
				setTimestamp(System.currentTimeMillis()).
				addEnties(Version.ClockEntry.newBuilder().setNodeID("duke").setVersion(2)/*.setTimestamp(2l)*/).
				addEnties(Version.ClockEntry.newBuilder().setNodeID("duke2").setVersion(1)/*.setTimestamp(1l)*/).
				build();
		Version.VectorClock local = Version.VectorClock.newBuilder().
				setTimestamp(System.currentTimeMillis()).
				addEnties(Version.ClockEntry.newBuilder().setNodeID("duke").setVersion(2)/*.setTimestamp(1l)*/).
				addEnties(Version.ClockEntry.newBuilder().setNodeID("duke2").setVersion(2)/*.setTimestamp(2l)*/).
				build();

		assertTrue(VersionUtils.compare(local, remote).equals(Occured.AFTER));
	}

	@Test
	public void vectorClockComparisonAFTER4Test() {
		Version.VectorClock remote = Version.VectorClock.newBuilder().
				setTimestamp(System.currentTimeMillis()).
				addEnties(Version.ClockEntry.newBuilder().setNodeID("duke").setVersion(1)/*.setTimestamp(2l)*/).
				addEnties(Version.ClockEntry.newBuilder().setNodeID("duke2").setVersion(1)/*.setTimestamp(1l)*/).
				build();
		Version.VectorClock local = Version.VectorClock.newBuilder().
				setTimestamp(System.currentTimeMillis()).
				addEnties(Version.ClockEntry.newBuilder().setNodeID("duke").setVersion(1)/*.setTimestamp(1l)*/).
				addEnties(Version.ClockEntry.newBuilder().setNodeID("duke2").setVersion(1)/*.setTimestamp(1l)*/).
				build();

		assertTrue(VersionUtils.compare(local, remote).equals(Occured.AFTER));
	}

	@Test
	public void vectorClockComparisonAFTER5Test() {
		Version.VectorClock remote = Version.VectorClock.newBuilder().
				setTimestamp(System.currentTimeMillis()).
				addEnties(Version.ClockEntry.newBuilder().setNodeID("duke").setVersion(1)/*.setTimestamp(2l)*/).
				//addEnties(Version.ClockEntry.newBuilder().setNodeID("duke2").setVersion(1).setTimestamp(1l)).
						build();
		Version.VectorClock local = Version.VectorClock.newBuilder().
				setTimestamp(System.currentTimeMillis()).
				addEnties(Version.ClockEntry.newBuilder().setNodeID("duke").setVersion(1)/*.setTimestamp(1l)*/).
				addEnties(Version.ClockEntry.newBuilder().setNodeID("duke2").setVersion(1)/*.setTimestamp(1l)*/).
				build();

		assertTrue(VersionUtils.compare(local, remote).equals(Occured.AFTER));
	}

	@Test
	public void vectorClockComparisonBEFORE1Test() {
		Version.VectorClock remote = Version.VectorClock.newBuilder().
				setTimestamp(System.currentTimeMillis()).
				addEnties(Version.ClockEntry.newBuilder().setNodeID("duke").setVersion(2)/*.setTimestamp(1l)*/).
				addEnties(Version.ClockEntry.newBuilder().setNodeID("duke2").setVersion(1)/*.setTimestamp(1l)*/).
				build();
		Version.VectorClock local = Version.VectorClock.newBuilder().
				setTimestamp(System.currentTimeMillis()).
				addEnties(Version.ClockEntry.newBuilder().setNodeID("duke").setVersion(1)/*.setTimestamp(1l)*/).
				addEnties(Version.ClockEntry.newBuilder().setNodeID("duke2").setVersion(1)/*.setTimestamp(1l)*/).
				build();

		assertTrue(VersionUtils.compare(local, remote).equals(Occured.BEFORE));
	}

	@Test
	public void vectorClockComparisonBEFORE2Test() {
		Version.VectorClock remote = Version.VectorClock.newBuilder().
				setTimestamp(System.currentTimeMillis()).
				addEnties(Version.ClockEntry.newBuilder().setNodeID("duke").setVersion(2)/*.setTimestamp(1l)*/).
				addEnties(Version.ClockEntry.newBuilder().setNodeID("duke2").setVersion(1)/*.setTimestamp(1l)*/).
				build();
		Version.VectorClock local = Version.VectorClock.newBuilder().
				setTimestamp(System.currentTimeMillis()).
				//addEnties(Version.ClockEntry.newBuilder().setNodeID("duke").setVersion(1).setTimestamp(1l)).
						addEnties(Version.ClockEntry.newBuilder().setNodeID("duke2").setVersion(1)/*.setTimestamp(2l)*/).
				build();

		assertTrue(VersionUtils.compare(local, remote).equals(Occured.BEFORE));
	}

	@Test
	public void vectorClockComparisonCONCURRENTLY1Test() {
		Version.VectorClock remote = Version.VectorClock.newBuilder().
				setTimestamp(System.currentTimeMillis()).
				addEnties(Version.ClockEntry.newBuilder().setNodeID("duke").setVersion(2)/*.setTimestamp(1l)*/).
				addEnties(Version.ClockEntry.newBuilder().setNodeID("duke2").setVersion(1)/*.setTimestamp(1l)*/).
				build();
		Version.VectorClock local = Version.VectorClock.newBuilder().
				setTimestamp(System.currentTimeMillis()).
				addEnties(Version.ClockEntry.newBuilder().setNodeID("duke").setVersion(1)/*.setTimestamp(1l)*/).
				addEnties(Version.ClockEntry.newBuilder().setNodeID("duke2").setVersion(2)/*.setTimestamp(1l)*/).
				build();

		assertTrue(VersionUtils.compare(local, remote).equals(Occured.CONCURRENTLY));
	}

	@Test
	public void vectorClockComparisonCONCURRENTLY2Test() {
		Version.VectorClock remote = Version.VectorClock.newBuilder().
				setTimestamp(System.currentTimeMillis()).
				addEnties(Version.ClockEntry.newBuilder().setNodeID("duke").setVersion(2)/*.setTimestamp(1l)*/).
				addEnties(Version.ClockEntry.newBuilder().setNodeID("duke2").setVersion(1)/*.setTimestamp(1l)*/).
				build();
		Version.VectorClock local = Version.VectorClock.newBuilder().
				setTimestamp(System.currentTimeMillis()).
				addEnties(Version.ClockEntry.newBuilder().setNodeID("duke").setVersion(1)/*.setTimestamp(1l)*/).
				addEnties(Version.ClockEntry.newBuilder().setNodeID("duke2").setVersion(2)/*.setTimestamp(2l)*/).
				build();

		assertTrue(VersionUtils.compare(local, remote).equals(Occured.CONCURRENTLY));
	}

	@Test
	public void vectorClockComparisonCONCURRENTLY3Test() {
		Version.VectorClock remote = Version.VectorClock.newBuilder().
				setTimestamp(System.currentTimeMillis()).
				addEnties(Version.ClockEntry.newBuilder().setNodeID("duke").setVersion(2)/*.setTimestamp(2l)*/).
				addEnties(Version.ClockEntry.newBuilder().setNodeID("duke2").setVersion(1)/*.setTimestamp(1l)*/).
				build();
		Version.VectorClock local = Version.VectorClock.newBuilder().
				setTimestamp(System.currentTimeMillis()).
				addEnties(Version.ClockEntry.newBuilder().setNodeID("duke").setVersion(1)/*.setTimestamp(1l)*/).
				addEnties(Version.ClockEntry.newBuilder().setNodeID("duke2").setVersion(2)/*.setTimestamp(2l)*/).
				build();

		assertTrue(VersionUtils.compare(local, remote).equals(Occured.CONCURRENTLY));
	}

	@Test
	public void vectorClockComparisonCONCURRENTLY4Test() {
		Version.VectorClock remote = Version.VectorClock.newBuilder().
				setTimestamp(System.currentTimeMillis()).
				addEnties(Version.ClockEntry.newBuilder().setNodeID("duke").setVersion(2)/*.setTimestamp(1l)*/).
				addEnties(Version.ClockEntry.newBuilder().setNodeID("duke2").setVersion(1)/*.setTimestamp(1l)*/).
				build();
		Version.VectorClock local = Version.VectorClock.newBuilder().
				setTimestamp(System.currentTimeMillis()).
				//addEnties(Version.ClockEntry.newBuilder().setNodeID("duke").setVersion(1).setTimestamp(1l)).
						addEnties(Version.ClockEntry.newBuilder().setNodeID("duke2").setVersion(2)/*.setTimestamp(2l)*/).
				build();

		assertTrue(VersionUtils.compare(local, remote).equals(Occured.CONCURRENTLY));
	}
//	 larger match {
//      case _ if (!largerBigger && !smallerBigger)=>Occured.AFTER
//      case _ if (!largerBigger && smallerBigger && sizeEquals) =>Occured.BEFORE
//      case _ if (!largerBigger && smallerBigger && !sizeEquals) =>Occured.CONCURRENTLY
//      case _ if(largerBigger && !smallerBigger && largerIsV1)=>Occured.AFTER
//      case _ if(largerBigger && !smallerBigger && !largerIsV1)=>Occured.BEFORE
//      case _ if (!largerBigger && smallerBigger && largerIsV1) =>Occured.BEFORE
//      case _ if (!largerBigger && smallerBigger && !largerIsV1) =>Occured.AFTER
//      case _ => Occured.CONCURRENTLY
}
