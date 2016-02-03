using System;

using java.net;
using java.rmi;

using com.arca.envoy.api.enumtypes;
using com.arca.envoy.api.eventlistener;
using com.arca.envoy.api.iface;
using com.arca.envoy.api.information;

/**
 * ExampleSCN
 *
 * This class demonstrates basic control and event handling for the
 *  MEI SCN Cashflow:
 *
 * - Obtain the EnvoySystem object
 * - Create an EnvoyEventListener and register a handler for it
 * - Perform a Mechanical Reset
 * - Do simple Event handling
 *
 * NOTE: A running Envoy service is necessary to run this example.
 *
 * TO COMPILE AND RUN IN WINDOWS CONSOLE
 * -------------------------------------
 * C:\test> csc /r:LibEnvoyAPI.dll,IKVM.OpenJDK.Core.dll,IKVM.OpenJDK.Util.dll,IKVM.OpenJDK.Naming.dll,IKVM.OpenJDK.Remoting.dll,IKVM.Runtime.dll /lib:[path_to_IKVM]\bin ExampleSCN.cs
 * C:\test> ExampleSCN.exe
 *
 *
 * TO COMPILE AND RUN IN LINUX TERMINAL
 * ------------------------------------
 * ~/test$ dmcs -r:LibEnvoyAPI.dll,/usr/lib/ikvm/IKVM.OpenJDK.Core.dll,/usr/lib/ikvm/IKVM.OpenJDK.Util.dll,/usr/lib/ikvm/IKVM.OpenJDK.Remoting.dll,/usr/lib/ikvm/IKVM.Runtime.dll ExampleSCN.cs
 * ~/test$ ExampleSCN.exe
 *
 *
 * The program obtains the EnvoySystem object, registers an
 * EventListener, creates a handler for the events, performs a
 * mechanical reset of the SCN, enables the SCN for accepting
 * notes, then waits for an event to occur.
 * When an event occurs, it is printed to standard out, ESC key
 * needs to be pressed to exit. The user-initiated event may be
 * one of several, such as removing the cassette (cash box),
 * inserting a bill or unplugging the USB cable.
 */

public class ExampleSCN
{
    /** The device name of the SCN, should we register it ourselves. */
    private const String SCN_DEVICE_NAME = "MEI_SCN";

    /** The SCN Device object **/
    private static ISCNDevice device;

    /** The still running boolean for simple thread blocking. */
    static bool _stillRunning;
    private static bool stillRunning
    {
        get
        {
            return _stillRunning;
        }
        set
        {
            _stillRunning = value;
        }
    }

    public static void takeAction(Event evt)
    {
        EventEnum eventType = evt.getEventType();

        if (eventType == EventEnum.ENABLED)
        {
            Console.WriteLine("Event fired - ENABLED");
        }
        else if (eventType == EventEnum.ESCROWED)
        {
            Console.WriteLine("Event fired - ESCROWED");
            if (device != null)
            {
                device.storeBill();
            }
        }
        else if (eventType == EventEnum.STACKED)
        {
            Console.WriteLine("Event fired - STACKED");
        }
        else if (eventType == EventEnum.DISABLED)
        {
            Console.WriteLine("Event fired - DISABLED");
        }
        else if (eventType == EventEnum.RETURNED)
        {
            Console.WriteLine("Event fired - RETURNED");
        }
        else if (eventType == EventEnum.CASSETTE_REMOVED)
        {
            Console.WriteLine("Event fired - CASSETTE_REMOVED");
        }
        else if (eventType == EventEnum.CASSETTE_ATTACHED)
        {
            Console.WriteLine("Event fired - CASSETTE_ATTACHED");
        }
        else if (eventType == EventEnum.CASSETTE_FULL)
        {
            Console.WriteLine("Event fired - CASSETTE_FULL");
        }
        else if (eventType == EventEnum.IGNORE)
        {
            Console.WriteLine("Event fired - IGNORE");
        }
        else if (eventType == EventEnum.USB_ATTACHED)
        {
            Console.WriteLine("Event fired - USB_ATTACHED");
        }
        else if (eventType == EventEnum.USB_DETACHED)
        {
            Console.WriteLine("Event fired - USB_DETACHED");
        }
        else if (eventType == EventEnum.INVALID)
        {
            Console.WriteLine("Event fired - INVALID");
        }
        else
        {
            stillRunning = false;
            return;
        }
    }

    private class EnvoyEventHandler : IEnvoyEventHandler
    {
        /*
         * Fill out the overriden method handleEnvoyEvent with customized event handling.
         * In this case, we simply choose one of several events as enumerated in
         * com.arca.envoy.api.iface.EventEnum with a switch statement in a separate method, takeAction().
         * We will just print a message to the console and then set our blocking while-loop boolean to false.
         */
        public void handleEnvoyEvent(Event evt)
        {
            Console.WriteLine("An event is coming in...");
            if (evt != null)
            {
                /*
                 * Here we call a method that will take action depending on
                 * the type of event.
                 */
                takeAction(evt);
            }
        }
    }
    
    /**
     * The main method.
     *
     * @param args the arguments, not used
     */
    public static void Main(String[] args)
    {
        /** The EnvoySystem object we obtain via RMI. **/
        IEnvoySystem envoySystem;

        /** Event Listener object **/
        EnvoyEventListener eel;

        /** Exit status when something went wrong. **/
        const int error = 1;

        device = null;
        stillRunning = true;

        try
        {
            /*
             * Obtain the EnvoySystem object via RMI (Remote Method Invocation)
             */
            Console.WriteLine("Getting the Envoy System object...");
            envoySystem = (IEnvoySystem)Naming.lookup("//localhost/envoy/system");
            Console.WriteLine("Creating an event listener for the SCN");

            /*
             * Create an EnvoyEventListener that responds to Envoy-related events
             */
            eel = new EnvoyEventListener();

            /*
             * Register an event handler with the listener
             */
            eel.registerHandler(new EnvoyEventHandler());

            /*
             * Get the path to a registered SCN device, even if we have to register it ourselves.
             */
            Console.WriteLine("Finding and/or registering an MEI SCN acceptor device...");
            String devicePath = null;

            foreach (String deviceName in envoySystem.getRegisteredDeviceNames())
            {
                DeviceInformation deviceInformation = envoySystem.getRegisteredDeviceInformation(deviceName);

                if (deviceInformation.getDeviceType() == DeviceType.MEI_CASHFLOW)
                {
                    Console.WriteLine("Found a registered SCN");
                    devicePath = envoySystem.getDevicePath(deviceName);
                    break;
                }
            }

            if (devicePath == null)
            {
                foreach (DeviceInformation deviceInformation in envoySystem.getAllKnownDeviceInformation())
                {
                    if (deviceInformation.getDeviceType() == DeviceType.MEI_CASHFLOW)
                    {
                        if (envoySystem.register(SCN_DEVICE_NAME, deviceInformation))
                        {
                            Console.WriteLine("Found an unregistered SCN and registered it");
                            devicePath = envoySystem.getDevicePath(SCN_DEVICE_NAME);
                            break;
                        }
                    }
                }
            }

            /*
             * Now we have a registered and attached SCN Cashflow device. We then obtain
             * the RMI path from EnvoySystem and get the SCNDevice from EnvoySystem. We
             * then enable the device and perform a mechanical reset.
             */
            Console.WriteLine("Attempting to get a device via RMI...");
            if (devicePath != null)
            {
                device = (ISCNDevice)Naming.lookup(devicePath);

                if (device != null)
                {
                    Console.WriteLine("Enabling all denominations and performing a mechanical reset...");
                    device.enableDevice(java.lang.Boolean.TRUE);
                    device.mechaReset();
                    Console.WriteLine("Enabling the SCN for accepting bills....");

                    Console.WriteLine();
                    Console.WriteLine(">>>> Feed a bill to the SCN, remove the cassette or unplug the USB ....");

                    /*
                     * If we have gotten this far, we are ready to process an Envoy event with our enabled SCN Cashflow.
                     * Here, we use simple thread-blocking code to wait for event. When the boolean stillRunning is set to
                     * false, the loop will exit.
                     */
                    while (stillRunning)
                    {
                        // Block thread until an event or keypress....
                        if (Console.ReadKey(true).Key == ConsoleKey.Escape)
                        {
                            stillRunning = false;
                        }
                    }

                }
                else
                {
                    Console.WriteLine("Could not obtain device from RMI ... Exiting");
                    Console.WriteLine("Press any key");
                    Console.ReadKey();
                    System.Environment.Exit(error);
                }
            }
            else
            {
                Console.WriteLine("Could not find registered SCN device ... Exiting");
                Console.WriteLine("Press any key");
                Console.ReadKey();
                System.Environment.Exit(error);
            }

        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
        }
        catch (NotBoundException e)
        {
            e.printStackTrace();
        }

        /*
         * We are done, so exit normally.
         */
        Console.WriteLine("Demo Done. Press any key to exit.");
        Console.ReadKey();
        System.Environment.Exit(0);
    }
}
