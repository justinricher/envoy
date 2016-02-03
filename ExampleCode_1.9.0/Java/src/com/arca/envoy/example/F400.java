package com.arca.envoy.example;

import static com.arca.envoy.api.currency.CurCodeEnum.USD;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashMap;

import com.arca.envoy.api.enumtypes.DeviceType;
import com.arca.envoy.api.iface.FujitsuBillParams;
import com.arca.envoy.api.iface.FujitsuDefaultMediaMappings;
import com.arca.envoy.api.iface.FujitsuDispByPosRsp;
import com.arca.envoy.api.iface.FujitsuDispenseByPositionPrm;
import com.arca.envoy.api.iface.FujitsuMechanicalResetRsp;
import com.arca.envoy.api.iface.IEnvoySystem;
import com.arca.envoy.api.iface.IF400Device;
import com.arca.envoy.api.information.DeviceInformation;

/**
 * F400.
 *
 * This example demonstrates registration and dispense of the Fujitsu F400
 * using the Envoy API.
 *
 * The program first obtains the Envoy system object and then registers the
 * device if not already registered. It obtains the device object via the
 * logical device name (LDN). Then, denominations are mapped to the logical
 * device. This enables Envoy to keep track of which denominations are in each
 * cassette and is dependent on a specific magnet setting.
 *
 * After that, bill parameters are set. In this step a minimum and maximum bill
 * length is written to F400 memory, along with bill thickness. This allows the
 * F400 to throw errors if it encounters a misfeed, or an irregular or damaged
 * bill.
 *
 * Finally, the F400 is made to dispense one bill each from the cassettes in
 * position 1 through 5.
 *
 * See README for compilation and running instructions.
 *
 * Before running this demo, you will need:
 *  - a running Envoy service is necessary to run this example.
 *  - a registered Fujitsu F400 via EnvoyHome is necessary to run this example.
 */
public class F400 {
	/**
	 * Main.
	 *
	 * @param args - Command line arguments, not used.
	 */
    public static void main(String[] args) {
        try {
            // Get the Envoy System Object.
            System.out.println("Getting the Envoy System object.");
            IEnvoySystem envoySystem = (IEnvoySystem) Naming.lookup("//localhost/envoy/system");

            // Get the first registered F400.
            System.out.println("Finding a registered F400.");
            
            String devicePath = null;
            
            // Locate the first registered F400 bill acceptor.
            for (String deviceName : envoySystem.getRegisteredDeviceNames()) {
                DeviceInformation deviceInformation = envoySystem.getRegisteredDeviceInformation(deviceName);

                if (deviceInformation.getDeviceType() == DeviceType.FUJITSU_F400) {
                    devicePath = envoySystem.getDevicePath(deviceName);
                    break;
                }
            }

            // Look up the device over RMI.  Success is if the device is not null.
            if (devicePath != null) {
                System.out.println("Looking up F400Device object.");
                IF400Device device = (IF400Device) Naming.lookup(devicePath);

                if (device != null) {
                    System.out.println("Setting Denomination Mappings to USD.");
                    device.setMediaMappings(FujitsuDefaultMediaMappings.getMapping(DeviceType.FUJITSU_F400, USD));

                    // Configure the F400 for USD Bill Parameters & no polymer support.
                    System.out.println("Performing Mechanical Reset.");
                    boolean bPolymer = false;
                    
                    //This demonstration expects that the F400 has five cassettes.
                    byte[] bBillLengths = {(byte) 0x9A, (byte) 0x9A, (byte) 0x9A, (byte) 0x9A, (byte) 0x9A};
                    byte[] bBillThicks = {(byte) 0x0D, (byte) 0x0D, (byte) 0x0D, (byte) 0x0D, (byte) 0x0D};
                    FujitsuBillParams params = new FujitsuBillParams(bBillLengths, bBillThicks, bPolymer);
                    FujitsuMechanicalResetRsp response = device.mechanicalReset(params);

                    // Perform a Dispense By Position on the F400.
                    System.out.println("Dispensing one note from each cassette.");
                    HashMap<Integer, Integer> posToCount = new HashMap<Integer, Integer>();
                    posToCount.put(1, 1); // Dispense 1 Note(s) from Position #1.
                    posToCount.put(2, 1); // Dispense 1 Note(s) from Position #2
                    posToCount.put(3, 1); // Dispense 1 Note(s) from Position #3
                    posToCount.put(4, 1); // Dispense 1 Note(s) from Position #4
                    posToCount.put(5, 1); // Dispense 1 Note(s) from Position #5
                    FujitsuDispenseByPositionPrm prm = new FujitsuDispenseByPositionPrm(posToCount);
                    FujitsuDispByPosRsp rsp = device.dispenseByPosition(prm);
                    System.out.println("Notes Actually Dispensed");
                    System.out.println("\tPos #1: " + rsp.getDispensedByPosition(1));
                    System.out.println("\tPos #2: " + rsp.getDispensedByPosition(2));
                    System.out.println("\tPos #3: " + rsp.getDispensedByPosition(3));
                    System.out.println("\tPos #4: " + rsp.getDispensedByPosition(4));
                    System.out.println("\tPos #5: " + rsp.getDispensedByPosition(5));

                }
            }

            // Any of these cases is failure.
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }

        System.out.println("\nDemo Complete.");
    }
}
