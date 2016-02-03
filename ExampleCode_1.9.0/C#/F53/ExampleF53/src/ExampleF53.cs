using System;
using System.Collections;
using java.rmi;
using java.net;
using java.util;
using com.arca.envoy.api.enumtypes;
using com.arca.envoy.api.currency;
using com.arca.envoy.api.iface;
using com.arca.envoy.api.information;

public class ExampleF53
{
    private const String F53_DEVICE_NAME = "FUJITSU_F53";
    
    /**
     * ExampleF53
     *
     * This example demonstrates registration and dispense of the Fujitsu F53
     * using the Envoy API.
     *
     * NOTE: A running Envoy service is necessary to run this example.
     *
     * TO COMPILE AND RUN IN WINDOWS CONSOLE
     * -------------------------------------
     * C:\test> csc /r:LibEnvoyAPI.dll,IKVM.OpenJDK.Core.dll,IKVM.OpenJDK.Util.dll,IKVM.OpenJDK.Naming.dll,IKVM.OpenJDK.Remoting.dll,IKVM.Runtime.dll /lib:[path_to_IKVM]\bin ExampleF53.cs
     * C:\test> ExampleF53.exe
     *
     *
     * TO COMPILE AND RUN IN LINUX TERMINAL
     * ------------------------------------
     * ~/test$ dmcs -r:LibEnvoyAPI.dll,/usr/lib/ikvm/IKVM.OpenJDK.Core.dll,/usr/lib/ikvm/IKVM.OpenJDK.Util.dll,/usr/lib/ikvm/IKVM.OpenJDK.Remoting.dll,/usr/lib/ikvm/IKVM.Runtime.dll ExampleF53.cs
     * ~/test$ ExampleF53.exe
     *
     * The program first obtains the Envoy system object and then registers
     * the device if not already registered. It obtains the device object
     * via the logical device name (LDN). Then, denominations are mapped to the
     * logical device. This enables Envoy to keep track of which denominations
     * are in each cassette and is dependent on a specific magnet setting.
     *
     * After that, bill parameters are set. In this step a minimum and maximum
     * bill length is written to F53 memory, along with bill thickness. This allows
     * the F53 to throw errors if it encounters a misfeed, or an irregular or damaged
     * bill.
     *
     * Finally, the F53 is made to dispense one bill each from the cassettes in
     * position 1 and position 2.
     *
     * @param args - Command line arguments, not used.
     */
    public static void Main(String[] args)
    {
        try
        {
            // Get the Envoy System Object.
            Console.WriteLine("Getting the Envoy System object.");
            IEnvoySystem envoySystem = (IEnvoySystem)Naming.lookup("//localhost/envoy/system");

            // Track the expected device path for the F53.
            String devicePath = null;

            // Try to find a registered F53 device first.
            Console.WriteLine("Attempting to find a registered F53.");
            foreach (String deviceName in envoySystem.getRegisteredDeviceNames())
            {
                DeviceInformation deviceInformation = envoySystem.getRegisteredDeviceInformation(deviceName);

                if (deviceInformation.getDeviceType() == DeviceType.FUJITSU_F53)
                {
                    devicePath = envoySystem.getDevicePath(deviceName);
                    break;
                }
            }

            // If no registered F53 devices were found, regster an unregistered one.
            if (devicePath == null)
            {
                Console.WriteLine("Attempting to register an attached F53.");
                foreach (DeviceInformation deviceInformation in envoySystem.getAllKnownDeviceInformation())
                {
                    if (deviceInformation.getDeviceType() == DeviceType.FUJITSU_F53)
                    {
                        if (envoySystem.register(F53_DEVICE_NAME, deviceInformation))
                        {
                            devicePath = envoySystem.getDevicePath(F53_DEVICE_NAME);
                            break;
                        }
                    }
                }
            }

            // Look up the device over RMI.  Success is if the device is not
            // null.
            if (devicePath != null)
            {
                Console.WriteLine("Looking up F53 device.");
                IF53Device device = (IF53Device) Naming.lookup(devicePath);

                if (device != null)
                {
                    Console.WriteLine("Setting Denomination Mappings to USD.");
                    device.setMediaMappings(FujitsuDefaultMediaMappings.getMapping(DeviceType.FUJITSU_F53, CurCodeEnum.USD));

                    // Configure the F53 for USD Bill Parameters & no polymer support.
                    Console.WriteLine("Performing Mechanical Reset.");
                    bool bPolymer = false;
                    byte[] bBillLengths = { (byte)0x9A, (byte)0x9A };
                    byte[] bBillThicks = { (byte)0x0D, (byte)0x0D };
                    FujitsuBillParams f53params = new FujitsuBillParams(bBillLengths, bBillThicks, bPolymer);
                    FujitsuMechanicalResetRsp response = device.mechanicalReset(f53params);

                    // Perform a Dispense By Position on the F53.
                    Map posToCount = new HashMap();
                    posToCount.put(java.lang.Integer.valueOf(1), java.lang.Integer.valueOf(1)); // Dispense 1 Note(s) from Position #1.
                    posToCount.put(java.lang.Integer.valueOf(2), java.lang.Integer.valueOf(1)); // Dispense 1 Note(s) from Position #2
                    FujitsuDispenseByPositionPrm prm = new FujitsuDispenseByPositionPrm(posToCount);
                    FujitsuDispByPosRsp rsp = device.dispenseByPosition(prm);
                    Console.WriteLine("Notes Actually Dispensed");
                    Console.WriteLine("\tPos #1: " + rsp.getDispensedByPosition(1));
                    Console.WriteLine("\tPos #2: " + rsp.getDispensedByPosition(2));

                }
            }
            else
            {
                Console.WriteLine("No match.");
            }

            // Any of these cases is failure.
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch (NotBoundException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            Console.WriteLine("Exception occured:");
            Console.WriteLine(e.Message);
        }

        Console.WriteLine("\nDemo Complete.");
        Console.ReadKey();
    }

}
