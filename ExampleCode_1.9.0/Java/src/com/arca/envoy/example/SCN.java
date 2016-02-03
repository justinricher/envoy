package com.arca.envoy.example;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import com.arca.envoy.api.enumtypes.DeviceType;
import com.arca.envoy.api.eventlistener.EnvoyEventListener;
import com.arca.envoy.api.iface.Event;
import com.arca.envoy.api.iface.IEnvoyEventHandler;
import com.arca.envoy.api.iface.IEnvoySystem;
import com.arca.envoy.api.iface.ISCNDevice;
import com.arca.envoy.api.information.DeviceInformation;

/**
 * SCN.
 *
 * This class demonstrates basic control and event handling for the MEI SCN
 * Cashflow:
 *
 * - Obtain the EnvoySystem object
 * - Create an EnvoyEventListener and register a handler for it
 * - Perform a Mechanical Reset
 * - Do simple Event handling.
 *
 * The program obtains the EnvoySystem object, registers an EventListener,
 * creates a handler for the events, performs a mechanical reset of the SCN,
 * enables the SCN for accepting notes, then waits for an event to occur.  When
 * an event occurs, it is printed to standard out and the program exits. The
 * user-initiated event may be one of several, such as removing the cassette
 * (cash box), inserting a bill or unplugging the USB cable.
 *
 * See README for compilation and running instructions.
 */
public class SCN {

    /** The device name used for registering an SCN device one not already registered. */
    private static final String SCN_DEVICE_NAME = "MEI_CASHFLOW";

    /** The still running boolean for simple thread blocking. */
    private static volatile boolean stillRunning = true;
    /** The EnvoySystem object we obtain via RMI. **/
    private static IEnvoySystem envoySystem;
    /** Exit status code for normal exit. **/
    private static int ok;
    /** Exit status when something went wrong. **/
    private static int error = 1;

    /*
     * Default constructor
     */
    protected SCN() {

    }

	/**
	 * Main.
	 *
	 * @param args - Command line arguments, not used.
	 */
    public static void main(String[] args) {
        try {
            /*
             * Obtain the EnvoySystem object via RMI (Remote Method Invocation)
             */
            System.out.println("Getting the Envoy System object...");
            envoySystem = (IEnvoySystem) Naming.lookup("//localhost/envoy/system");
            System.out.println("Creating an event listener for the SCN");

            /*
             * Create an EnvoyEventListener that responds to Envoy-related events
             */
            EnvoyEventListener eel = new EnvoyEventListener();
            /*
             * Register an event handler with the listener
             */
            eel.registerHandler(new IEnvoyEventHandler() {

                /*
				 * Fill out the overriden method handleEnvoyEvent with
				 * customized event handling.  In this case, we simply chose
				 * one of several events as enumerated in
				 * com.arca.envoy.api.iface.EventEnum with a switch statement
				 * in a separate method, takeAction().  We will just print a
				 * message to the console and then set our blocking while-loop
				 * boolean to false.
                 */
                @Override
                public void handleEnvoyEvent(Event evt) {
                    System.out.println("AN EVENT IS COMING IN...");
                    if (evt != null) {

                        /*
                         * Here we call a method that will take action depending on
                         * the type of event.
                         */
                        takeAction(evt);
                    }
                }

            });

            System.out.println("Finding and/or registering an MEI SCN acceptor device...");
            String devicePath = null;
            
            /*
             * First try to find a registered SCN device by iterating through all
             * the registered devices.
             */
            for (String deviceName : envoySystem.getRegisteredDeviceNames()) {
                DeviceInformation deviceInformation = envoySystem.getRegisteredDeviceInformation(deviceName);

                if (deviceInformation.getDeviceType() == DeviceType.MEI_CASHFLOW) {
                    System.out.println("Found a registered SCN");
                    devicePath = envoySystem.getDevicePath(deviceName);
                    break;
                }
            }
            
            /*
             * If no registered SCN devices are found, attempt to register the
             * first SCN device that is currently attached.
             */
            if (devicePath == null) {
                for (DeviceInformation deviceInformation : envoySystem.getAllKnownDeviceInformation()) {
                    if (deviceInformation.getDeviceType() == DeviceType.MEI_CASHFLOW) {
                        if (envoySystem.register(SCN_DEVICE_NAME, deviceInformation)) {
                            System.out.println("Found an unregistered SCN and registered it");
                            devicePath = envoySystem.getDevicePath(SCN_DEVICE_NAME);
                            break;
                        }
                    }
                }
            }

            /*
			 * Now we have the RMI path to the registered and attached SCN Cashflow 
             * device. We then obtain the SCNDevice from EnvoySystem, and enable
             * the device and perform a mechanical reset.
             */
            System.out.println("Attempting to get a device via RMI...");
            if (devicePath != null) {
                ISCNDevice device = (ISCNDevice) Naming.lookup(devicePath);

                if (device != null) {
                    System.out.println("Enabling all denominations and performing a mechanical reset...");
                    device.enableDevice(true);
                    device.mechaReset();
        
                    System.out.println();
                    System.out.println(">>>> Feed a bill to the SCN, remove the cassette or unplug the USB ....");
                    
                    /*
                     * If we have gotten this far, we are ready to process an Envoy event
                     * with our enabled SCN Cashflow.  Here, we use simple thread-blocking
                     * code to wait for event. When the boolean stillRunning is set to
                     * false, the loop will exit.
                     */
                    while (stillRunning) {
                        // Block thread until an event....
                    }

                    /*
                     * We are done, so exit normally.
                     */
                    System.out.println("... Exiting");
                    System.exit(ok);

                } else {
                    System.out.println("Could not obtain device from RMI ... Exiting");
                    System.exit(error);
                }
            } else {
                System.out.println("Could not find registered SCN device ... Exiting");
                System.exit(error);
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }
    
    private static void takeAction(Event evt) {
        switch (evt.getEventType()) {
            case  ENABLED:
                System.out.println("Event fired - ENABLED");
                stillRunning = false;
                break;

            case  ESCROWED:
                System.out.println("Event fired - ESCROWED");
                stillRunning = false;
                break;

            case  STACKED:
                System.out.println("Event fired - STACKED");
                stillRunning = false;
                break;

            case  DISABLED:
                System.out.println("Event fired - DISABLED");
                stillRunning = false;
                break;

            case  RETURNED:
                System.out.println("Event fired - RETURNED");
                stillRunning = false;
                break;

            case  CASSETTE_REMOVED:
                System.out.println("Event fired - CASSETTE_REMOVED");
                stillRunning = false;
                break;

            case  CASSETTE_ATTACHED:
                System.out.println("Event fired - CASSETTE_ATTACHED");
                stillRunning = false;
                break;

            case  CASSETTE_FULL:
                System.out.println("Event fired - CASSETTE_FULL");
                stillRunning = false;
                break;

            case  IGNORE:
                System.out.println("Event fired - IGNORE");
                stillRunning = false;
                break;

            case  USB_ATTACHED:
                System.out.println("Event fired - USB_ATTACHED");
                stillRunning = false;
                break;

            case  USB_DETACHED:
                System.out.println("Event fired - USB_DETACHED");
                stillRunning = false;
                break;
            case INVALID:
                System.out.println("Event fired - INVALID");
                stillRunning = false;
                break;
            default:
                break;
        }
    }
}
