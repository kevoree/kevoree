package org.kevoree.library.sky.libvirt;

import nu.xom.*;
import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;
import org.libvirt.Network;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.UUID;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 30/10/12
 * Time: 19:31
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class Tester {
	public static String FIXME = "\n<=====================================>\n<============== FIXME ================> ";

	public static void main (String[] args) {
		// Create the connection
		Connect conn = null;

		try {
			conn = new Connect("qemu:///system", false);
			Domain domain = conn.domainLookupByName("debian_base");
			//      XML.loadString(domain.getXMLDesc(0))

			/* Change attributes */
			Builder parser = new Builder();
			Document doc = parser.build(domain.getXMLDesc(0), null);

			/*Element nameElement = (Element) doc.query("/domain/name").get(0);
			nameElement.removeChildren();
			nameElement.appendChild("Toto");

			Element uuidElement = (Element) doc.query("/domain/uuid").get(0);
			uuidElement.removeChildren();
			uuidElement.appendChild(UUID.randomUUID().toString());

			Element memoryElement = (Element) doc.query("/domain/memory").get(0);
			memoryElement.removeChildren();
			memoryElement.appendChild("1024000");

			Element currentMemoryElement = (Element) doc.query("/domain/currentMemory").get(0);
			currentMemoryElement.removeChildren();
			currentMemoryElement.appendChild("1024000");

			Element vCPUElement = (Element) doc.query("/domain/vcpu").get(0);
			vCPUElement.removeChildren();
			vCPUElement.appendChild("1");

			Element typeElement = (Element) doc.query("/domain/os/type").get(0);
			typeElement.removeAttribute(typeElement.getAttribute("arch"));
			Attribute archAttribute = new Attribute("arch", "i686");
			typeElement.addAttribute(archAttribute);

			Element sourceElement = (Element) doc.query("/domain/devices/disk/source").get(0);
			System.out.println(sourceElement.toXML());
			sourceElement.removeAttribute(sourceElement.getAttribute("file"));
			Attribute fileAttribute = new Attribute("file", "/home/edaubert/Public/vms/debian_base.toto.qcow2");
			sourceElement.addAttribute(fileAttribute);

			Nodes interfaceElements = doc.query("/domain/devices/interface");
			for (int i = 0; i < interfaceElements.size(); i++) {
				Element macElement = (Element) ((Element) interfaceElements.get(i)).query("./mac").get(0);
				((Element) interfaceElements.get(i)).removeChild(macElement);
			}

			System.out.println(doc.toXML());*/
			Nodes interfaceElements = doc.query("/domain/devices/interface");
			for (int i = 0; i < interfaceElements.size(); i++) {
				Element macElement = (Element) ((Element) interfaceElements.get(i)).query("./mac").get(0);
				((Element) interfaceElements.get(i)).removeChild(macElement);
			}

			StringBuilder builder = new StringBuilder();
			BufferedReader reader = new BufferedReader(new FileReader("/home/edaubert/toto.xml"));
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			domain = conn.domainDefineXML(builder.toString());
		} catch (LibvirtException e) {
			e.printStackTrace();
		} catch (ValidityException e) {
			e.printStackTrace();
		} catch (ParsingException e) {
			e.printStackTrace();
		}  catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static boolean testAuthentication () {

		// Create the connection
		Connect conn = null;
		Network testNetwork = null;
		try {
			// connect to the local hypervisor
			conn = new Connect("qemu:///system", false);
//			System.out.println(conn.domainLookupByName("debian_base"));
		} catch (LibvirtException e) {
			System.out.println("exception caught:" + e);
			System.out.println(e.getError());
		}
		try {

			// TODO build a vm from a template and start it
			StringBuilder builder = new StringBuilder();
			BufferedReader reader = new BufferedReader(new FileReader("/home/edaubert/debian_base.xml"));
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}

			builder.replace(builder.indexOf("{nodeName}"), builder.indexOf("{nodeName}") + "{nodeName}".length(), "toto")
					.replace(builder.indexOf("{maxMemory}"), builder.indexOf("{maxMemory}") + "{maxMemory}".length(), "1024")
					.replace(builder.indexOf("{currentMemory}"), builder.indexOf("{currentMemory}") + "{currentMemory}".length(), "1024")
					.replace(builder.indexOf("{nbCPU}"), builder.indexOf("{nbCPU}") + "{nbCPU}".length(), "1").replace(builder.indexOf("{arch}"), builder.indexOf("{arch}") + "{arch}".length(), "i686")
					.replace(builder.indexOf("{diskPath}"), builder.indexOf("{diskPath}") + "{diskPath}".length(), "/home/edaubert/Public/vms/debian_base.toto.qcow2")
					.replace(builder.indexOf("{uuid}"), builder.indexOf("{uuid}") + +"{uuid}".length(), UUID.randomUUID().toString());
//			builder.replace(builder.indexOf("{macAddress}"), "{macAddress}".length(), "toto");

			Domain domain = conn.domainDefineXML(builder.toString());

//			System.out.println("conn.domainDefineXML:" + domain);
//
//			int status = domain.create();
//			if (status == 0) {
//				System.out.println("Creation and boot of " + domain.getName() + "succeed");
//			}
			// END build a vm from a template

			// TODO looking for a specific vm named toto
//			Domain domain = conn.domainLookupByName("toto");
			// END looking for a specific vm named toto

			// TODO suspend a vm (carefull this is not a complete shutdown)
//			domain.suspend();
			// END suspend a vm (carefull this is not a complete shutdown)

			// TODO destroy a vm (a complete shutdown without removing the domain)
//			domain.destroy();
			// END destroy a vm  (a complete shutdown without removing the domain)

			// TODO undefined a vm (remove the registratio of the vm on libvirt)
//			domain.undefine();
			// END undefined a vm (remove the registratio of the vm on libvirt)

			// Check nodeinfo
//			NodeInfo nodeInfo = conn.nodeInfo();
//			System.out.println("virNodeInfo.model:" + nodeInfo.model);
//			System.out.println("virNodeInfo.memory:" + nodeInfo.memory);
//			System.out.println("virNodeInfo.cpus:" + nodeInfo.cpus);
//			System.out.println("virNodeInfo.nodes:" + nodeInfo.nodes);
//			System.out.println("virNodeInfo.sockets:" + nodeInfo.sockets);
//			System.out.println("virNodeInfo.cores:" + nodeInfo.cores);
//			System.out.println("virNodeInfo.threads:" + nodeInfo.threads);
//
			// Exercise the information getter methods
//			System.out.println("getHostName:" + conn.getHostName());
//			System.out.println("getCapabilities:" + conn.getCapabilities());
//			System.out.println("getMaxVcpus:" + conn.getMaxVcpus("qemu"));
//			System.out.println("getType:" + conn.getType());
//			System.out.println("getURI:" + conn.getURI());
//			System.out.println("getVersion:" + conn.getVersion());
//			System.out.println("getLibVirVersion:" + conn.getLibVirVersion());
//
//			// By default, there are 1 created and 0 defined networks
//
			//=> FIXME can be a new adaptation primitives which help to build network
			// Create a new network to test the create method
//			System.out.println("conn.networkCreateXML: "
//					+ conn.networkCreateXML("<network>" + "  <name>createst</name>"
//					+ "  <uuid>004b96e1-2d78-c30f-5aa5-f03c87d21e68</uuid>" + "  <bridge name='createst'/>"
//					+ "  <forward dev='eth0'/>" + "  <ip address='192.168.66.1' netmask='255.255.255.0'>"
//					+ "    <dhcp>" + "      <range start='192.168.66.128' end='192.168.66.253'/>"
//					+ "    </dhcp>" + "  </ip>" + "</network>"));
//
//			// Same for the define method
//			System.out.println("conn.networkDefineXML: "
//					+ conn.networkDefineXML("<network>" + "  <name>deftest</name>"
//					+ "  <uuid>004b96e1-2d78-c30f-5aa5-f03c87d21e67</uuid>" + "  <bridge name='deftest'/>"
//					+ "  <forward dev='eth0'/>" + "  <ip address='192.168.88.1' netmask='255.255.255.0'>"
//					+ "    <dhcp>" + "      <range start='192.168.88.128' end='192.168.88.253'/>"
//					+ "    </dhcp>" + "  </ip>" + "</network>"));
//
//			// We should have 2:1 but it shows up 3:0 hopefully a bug in the
//			// test driver
//			System.out.println("numOfDefinedNetworks:" + conn.numOfDefinedNetworks());
//			System.out.println("listDefinedNetworks:" + Arrays.toString(conn.listDefinedNetworks()));
//			for (String c : conn.listDefinedNetworks())
//				System.out.println("	-> " + c);
//			System.out.println("numOfNetworks:" + conn.numOfNetworks());
//			System.out.println("listNetworks:" + Arrays.toString(conn.listNetworks()));
//			for (String c : conn.listNetworks())
//				System.out.println("	-> " + c);
			//=> END can be a new adaptation primitives which help to build network
//
//			// Look at the interfaces
//			// TODO Post 0.5.1
//			System.out.println("numOfInterfaces:" + conn.numOfInterfaces());
//			System.out.println("listDefinedInterfaces:" + conn.listInterfaces());
//			for(String c: conn.listInterfaces())
//			System.out.println("    -> "+c);
//
//			// Define a new Domain
//			System.out.println("conn.domainDefineXML:"
//					+ conn.domainDefineXML("<domain type='test' id='2'>" + "  <name>deftest</name>"
//					+ "  <uuid>004b96e1-2d78-c30f-5aa5-f03c87d21e70</uuid>" + "  <memory>8388608</memory>"
//					+ "  <vcpu>2</vcpu>" + "  <os><type arch='i686'>hvm</type></os>"
//					+ "  <on_reboot>restart</on_reboot>" + "  <on_poweroff>destroy</on_poweroff>"
//					+ "  <on_crash>restart</on_crash>" + "</domain>"));
//
//			System.out.println("conn.domainCreateLinux:"
//					+ conn.domainCreateLinux("<domain type='test' id='3'>" + "  <name>createst</name>"
//					+ "  <uuid>004b96e1-2d78-c30f-5aa5-f03c87d21e67</uuid>" + "  <memory>8388608</memory>"
//					+ "  <vcpu>2</vcpu>" + "  <os><type arch='i686'>hvm</type></os>"
//					+ "  <on_reboot>restart</on_reboot>" + "  <on_poweroff>destroy</on_poweroff>"
//					+ "  <on_crash>restart</on_crash>" + "</domain>", 0));
//
//			// Domain enumeration stuff
//			System.out.println("numOfDefinedDomains:" + conn.numOfDefinedDomains());
//			System.out.println("listDefinedDomains:" + Arrays.toString(conn.listDefinedDomains()));
//			for (String c : conn.listDefinedDomains())
//				System.out.println("	" + c);
//			System.out.println("numOfDomains:" + conn.numOfDomains());
//			System.out.println("listDomains:" + Arrays.toString(conn.listDomains()));
//			for (int c : conn.listDomains())
//				System.out.println("	-> " + c);
//
		} catch (LibvirtException e) {
			System.out.println(FIXME);
			System.out.println("exception caught:" + e);
			System.out.println(e.getError());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
//
//		// Network Object
//
//		try {
//			// Choose one, they should have the exact same effect
//			testNetwork = conn.networkLookupByName("deftest");
//			System.out.println("networkLookupByName: " + testNetwork.getName());
//			//testNetwork = conn.networkLookupByUUID(UUIDArray);
//			//System.out.println("networkLookupByUUID: " + testNetwork.getName());
//			testNetwork = conn.networkLookupByUUIDString("004b96e1-2d78-c30f-5aa5-f03c87d21e67");
//			System.out.println("networkLookupByUUIDString: " + testNetwork.getName());
//			testNetwork = conn.networkLookupByUUID(UUID.fromString("004b96e1-2d78-c30f-5aa5-f03c87d21e67"));
//			System.out.println("networkLookupByUUID (Java UUID): " + testNetwork.getName());
//
//			// Exercise the getter methods on the default network
//			System.out.println("virNetworkGetXMLDesc:" + testNetwork.getXMLDesc(0));
//			System.out.println("virNetworkGetAutostart:" + testNetwork.getAutostart());
//			System.out.println("virNetworkGetBridgeName:" + testNetwork.getBridgeName());
//			System.out.println("virNetworkGetName:" + testNetwork.getName());
//			System.out.println("virNetworkGetUUID:" + Arrays.toString(testNetwork.getUUID()) + " ");
//			for (int c : testNetwork.getUUID())
//				System.out.print(String.format("%02x", c));
//			System.out.println();
//			System.out.println("virNetworkGetName:" + testNetwork.getUUIDString());
//
//			// Destroy and create the network
//			System.out.println("virNetworkDestroy:");
//			testNetwork.destroy();
//			System.out.println("virNetworkCreate:");
//			testNetwork.create();
//		} catch (LibvirtException e) {
//			System.out.println(FIXME);
//			System.out.println("exception caught:" + e);
//			System.out.println(e.getError());
//		}
//		// This should raise an excpetion
//		try {
//			System.out.println("virNetworkCreate (should error):");
//			testNetwork.create();
//		} catch (LibvirtException e) {
//			System.out.println("exception caught:" + e);
//			System.out.println(e.getError());
//		}
//
//		// Domain stuff
//
//		try {
//			// Domain lookup
//			Domain testDomain = conn.domainLookupByID(1);
//			System.out.println("domainLookupByID: " + testDomain.getName());
//			testDomain = conn.domainLookupByName("test");
//			System.out.println("domainLookupByName: " + testDomain.getName());
//			testDomain = conn.domainLookupByUUIDString("004b96e1-2d78-c30f-5aa5-f03c87d21e67");
//			System.out.println("domainLookupByUUIDString: " + testDomain.getName());
//			//testDomain = conn.domainLookupByUUID(UUIDArray);
//			//System.out.println("domainLookupByUUID: " + testDomain.getName());
//			testDomain = conn.domainLookupByUUID(UUID.fromString("004b96e1-2d78-c30f-5aa5-f03c87d21e67"));
//			System.out.println("domainLookupByUUID (JAVA UID): " + testDomain.getName());
//
//			// Exercise the getter methods on the default domain
//			System.out.println("virDomainGetXMLDesc:" + testDomain.getXMLDesc(0));
//			System.out.println("virDomainGetAutostart:" + testDomain.getAutostart());
//			System.out.println("virDomainGetConnect:" + testDomain.getConnect());
//			System.out.println("virDomainGetID:" + testDomain.getID());
//			System.out.println("virDomainGetInfo:" + testDomain.getInfo());
//			System.out.println("virDomainGetMaxMemory:" + testDomain.getMaxMemory());
//			// Should fail, test driver does not support it
//			try {
//				System.out.println("virDomainGetMaxVcpus:" + testDomain.getMaxVcpus());
//				System.out.println(FIXME);
//			} catch (LibvirtException e) {
//
//			}
//			System.out.println("virDomainGetName:" + testDomain.getName());
//			System.out.println("virDomainGetOSType:" + testDomain.getOSType());
//			System.out.println("virDomainGetSchedulerType:" + Arrays.toString(testDomain.getSchedulerType()));
//			System.out.println("virDomainGetSchedulerParameters:" + Arrays.toString(testDomain.getSchedulerParameters()));
//			// Iterate over the parameters the painful way
//			for (SchedParameter c : testDomain.getSchedulerParameters()) {
//				if (c instanceof SchedIntParameter) {
//					System.out.println("Int:" + ((SchedIntParameter) c).field + ":" + ((SchedIntParameter) c).value);
//				}
//				if (c instanceof SchedUintParameter) {
//					System.out.println("Uint:" + ((SchedUintParameter) c).field + ":" + ((SchedUintParameter) c).value);
//				}
//				if (c instanceof SchedLongParameter) {
//					System.out.println("Long:" + ((SchedLongParameter) c).field + ":" + ((SchedLongParameter) c).value);
//				}
//				if (c instanceof SchedUlongParameter) {
//					System.out.println("Ulong:" + ((SchedUlongParameter) c).field + ":"
//							+ ((SchedUlongParameter) c).value);
//				}
//				if (c instanceof SchedDoubleParameter) {
//					System.out.println("Double:" + ((SchedDoubleParameter) c).field + ":"
//							+ ((SchedDoubleParameter) c).value);
//				}
//				if (c instanceof SchedBooleanParameter) {
//					System.out.println("Boolean:" + ((SchedBooleanParameter) c).field + ":"
//							+ ((SchedBooleanParameter) c).value);
//				}
//			}
//			// Iterate over the parameters the easy way
//			for (SchedParameter c : testDomain.getSchedulerParameters()) {
//				System.out.println(c.getTypeAsString() + ":" + c.field + ":" + c.getValueAsString());
//			}
//
//			// test setting a scheduled parameter
//			SchedUintParameter[] pars = new SchedUintParameter[1];
//			pars[0] = new SchedUintParameter();
//			pars[0].field = "weight";
//			pars[0].value = 100;
//			testDomain.setSchedulerParameters(pars);
//
//			System.out.println("virDomainGetUUID:" + Arrays.toString(testDomain.getUUID()));
//			for (int c : testDomain.getUUID())
//				System.out.print(String.format("%02x", c));
//			System.out.println();
//			System.out.println("virDomainGetUUIDString:" + testDomain.getUUIDString());
//			// Should fail, unimplemented in test driver
//			// System.out.println("virDomainGetVcpusInfo:" +
//			// testDomain.getVcpusInfo());
//			// Same as above
//			// System.out.println("virDomainGetVcpusCpuMap:" +
//			// testDomain.getVcpusCpuMaps());
//			// Should test pinVcpu, when we test with real xen
//			// Here
//			// Attach default network to test domain
//			// System.out.println("virDomainGetVcpusCpuMap:" +
//			// testDomain.getVcpusCpuMaps());
//
//			// Should test interfacestats and blockstats with real xen
//
//			// Close the connection
//
//			conn.close();
//		} catch (LibvirtException e) {
//			System.out.println(FIXME);
//			System.out.println("exception caught:" + e);
//			System.out.println(e.getError());
//		}
//
//		try {
//			// We should get an exception, not a crash
//			System.out.println(conn.getHostName());
//		} catch (LibvirtException e) {
//			System.out.println("exception caught:" + e);
//			System.out.println(e.getError());
//		}
		System.out.println("Fini!");
		return false;
	}

	public static boolean startVM () {
		return false;
	}

	public static boolean stopVM () {
		return false;
	}

	public static boolean removeVM () {
		return false;
	}
}
