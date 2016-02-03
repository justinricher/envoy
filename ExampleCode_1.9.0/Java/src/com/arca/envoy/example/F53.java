package com.arca.envoy.example;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashMap;

import com.arca.envoy.api.enumtypes.DeviceType;
import com.arca.envoy.api.iface.FujitsuBillParams;
import com.arca.envoy.api.iface.FujitsuDefaultMediaMappings;
import com.arca.envoy.api.iface.FujitsuDispenseByPositionPrm;
import com.arca.envoy.api.iface.FujitsuDispByPosRsp;
import com.arca.envoy.api.iface.FujitsuMechanicalResetRsp;
import com.arca.envoy.api.iface.IEnvoySystem;
import com.arca.envoy.api.iface.IFujitsuDevice;
import com.arca.envoy.api.information.DeviceInformation;

import static com.arca.envoy.api.currency.CurCodeEnum.USD;

/**
 * F53.
 *
 * This example demonstrates registration and dispense of the Fujitsu F53
 * using the Envoy API.
 *
 * The program first obtains the Envoy system object and then registers the
 * device if not already registered. It obtains the device object via the
 * logical device name (LDN). Then, denominations are mapped to the logical
 * device. This enables Envoy to keep track of which denominations are in
 * each cassette and is dependent on a specific magnet setting.
 *
 * After that, bill parameters are set. In this step a minimum and maximum
 * bill length is written to F53 memory, along with bill thickness. This
 * allows the F53 to throw errors if it encounters a misfeed, or an
 * irregular or damaged bill.
 *
 * Finally, the F53 is made to dispense one bill each from the cassettes in
 * position 1 and position 2.
 *
 * See README for compilation and running instructions.
 */
public class F53 {

    private static final String F53_DEVICE_NAME = "FUJITSU_F53";
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

            String devicePath = null;
            
            // Try to find a registered F53 device first.
            System.out.println("Attempting to find a registered an F53.");            
            for (String deviceName : envoySystem.getRegisteredDeviceNames()) {
                DeviceInformation deviceInformation = envoySystem.getRegisteredDeviceInformation(deviceName);

                if (deviceInformation.getDeviceType() == DeviceType.FUJITSU_F53) {
                    devicePath = envoySystem.getDevicePath(deviceName);
                    break;
                }
            }

            // If no registered F53 devices were found, register an unregistered one.
            if (devicePath == null) {
                System.out.println("Attempting to register an attached F53.");            
                for (DeviceInformation deviceInformation : envoySystem.getAllKnownDeviceInformation()) {
                    if (deviceInformation.getDeviceType() == DeviceType.FUJITSU_F53) {
                        if (envoySystem.register(F53_DEVICE_NAME, deviceInformation)) {
                            devicePath = envoySystem.getDevicePath(F53_DEVICE_NAME);
                            break;
                        }
                    }
                }
            }

            // Look up the device over RMI.  Success is if the device is not null.
            if (devicePath != null) {
                System.out.println("Looking up FujitsuDevice object.");
                IFujitsuDevice device = (IFujitsuDevice) Naming.lookup(devicePath);

                if (device != null) {
                    System.out.println("Setting Denomination Mappings to USD.");
                    device.setMediaMappings(FujitsuDefaultMediaMappings.getMapping(DeviceType.FUJITSU_F53, USD));

                    // Configure the F53 for USD Bill Parameters & no polymer support.
                    System.out.println("Performing Mechanical Reset.");
                    boolean bPolymer = false;
                    byte[] bBillLengths = {(byte) 0x9A, (byte) 0x9A};
                    byte[] bBillThicks = {(byte) 0x0D, (byte) 0x0D};
                    FujitsuBillParams params = new FujitsuBillParams(bBillLengths, bBillThicks, bPolymer);
                    FujitsuMechanicalResetRsp response = device.mechanicalReset(params);

                    // Perform a Dispense By Position on the F53.
                    System.out.println("Dispensing 1 Note from Position #1");
                    HashMap<Integer, Integer> posToCount = new HashMap<Integer, Integer>();
                    posToCount.put(1, 1); // Dispense 1 Note(s) from Position #1.
                    posToCount.put(2, 0); // Dispense 0 Note(s) from Position #2
                    FujitsuDispenseByPositionPrm prm = new FujitsuDispenseByPositionPrm(posToCount);
                    FujitsuDispByPosRsp rsp = device.dispenseByPosition(prm);
                    System.out.println("Notes Actually Dispensed");
                    System.out.println("\tPos #1: " + rsp.getDispensedByPosition(1));
                    System.out.println("\tPos #2: " + rsp.getDispensedByPosition(2));

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
