package com.arca.envoy.example;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import com.arca.envoy.api.currency.CurCodeEnum;
import com.arca.envoy.api.currency.Denomination;
import com.arca.envoy.api.enumtypes.CS1oneReplyCodes;
import com.arca.envoy.api.enumtypes.DeviceType;
import com.arca.envoy.api.eventlistener.EnvoyEventListener;
import com.arca.envoy.api.iface.APICommandException;
import com.arca.envoy.api.iface.Event;
import com.arca.envoy.api.iface.ICS1oneDevice;
import com.arca.envoy.api.iface.IEnvoyEventHandler;
import com.arca.envoy.api.iface.IEnvoySystem;
import com.arca.envoy.api.information.DeviceInformation;

/**
 * CS1one.
 *
 * This example demonstrates basic control and event handling for the ARCA/CTS
 * CS1one bill acceptor. The following actions are performed:
 * 
 *  - Connect to the Envoy service
 *  - Configure an Envoy event listener for ARCA/CTS CS1one bill acceptor events
 *  - Locate the first registered ARCA/CTS CS1one bill acceptor
 *  - Initialize the ARCA/CTS CS1one bill acceptor
 *  - Set the currency to USD
 *  - Wait for the user to insert a USD note
 *  - Display the accepted note information
 *  - Seal the bag
 *  - Replace the bag
 * 
 * See README for compilation and running instructions.
 *
 * Before running this demo, you will need:
 *  - a running Envoy service is necessary to run this example.
 *  - a registered ARCA/CTS CS1one via EnvoyHome is necessary to run this example.
 *  - a single USD note
 *  - the CS1one to be empty.
 */
public class CS1one {

    private static final String MSG_UNLOCKING_SAFE_DOOR = "Unlocking the safe door";
    private static final String MSG_OPEN_SAFE_DOOR = "Please open the safe door";

    private static IEnvoySystem envoySystem;
    private static ICS1oneDevice cs1one;

    private static volatile Denomination acceptedNote;
    private static volatile boolean bagSealed;
    private static volatile boolean bagMissing;
    private static volatile boolean bagReady;
    private static volatile boolean bagReplaced;

    private static void handleException(Exception e, boolean terminate) {
        System.out.println(e.getMessage());
        e.printStackTrace();
        if (terminate) {
            System.exit(1);
        }
    }

    private static void delayMS(int msDelay) {
        try {
            Thread.sleep(msDelay);
        } catch (InterruptedException e) {
            handleException(e, false);
        }
    }

    private static void processEnvoyEvent(Event event) {
        switch (event.getEventType()) {
            case BAG_MISSING:
                bagMissing = true;
                break;
            case BAG_READY:
                bagReady = true;
                break;
            case DOOR_CLOSED:
                bagReplaced = bagReady;
                break;
            default:
                break;
        }
    }

    private static boolean acquireCS1oneDevice() {
        boolean acquired = false;

        if (envoySystem != null) {
            try {
                String devicePath = null;
                
                // Locate the first registered ARCA/CTS CS1one bill acceptor.
                for (String deviceName : envoySystem.getRegisteredDeviceNames()) {
                    DeviceInformation deviceInformation = envoySystem.getRegisteredDeviceInformation(deviceName);

                    if (deviceInformation.getDeviceType() == DeviceType.CS1ONE) {
                        devicePath = envoySystem.getDevicePath(deviceName);
                        break;
                    }
                }

                // Connect to the ARCA/CTS bill acceptor.
                if (devicePath != null) {
                    cs1one = (ICS1oneDevice) Naming.lookup(devicePath);

                    acquired = cs1one != null;
                }

            } catch (RemoteException e) {
                handleException(e, true);
            } catch (MalformedURLException e) {
                handleException(e, true);
            } catch (NotBoundException e) {
                handleException(e, true);
            }
        }

        return acquired;
    }

    /**
     * Sets the currency code on the CS1one device.
     * <p>
     * Supported currency codes:
     * <ul>
     * <li>EUR</li>
     * <li>GBP</li>
     * <li>USD</li>
     * </ul>
     *
     * @param currencyCode - The desired currency code.
     */
    private static void setCurrencyCode(CurCodeEnum currencyCode) {
        System.out.println("Setting currency code to " + currencyCode.toString());

        try {
            cs1one.setCurrencyCode(currencyCode);
            System.out.println("Successfully set currency code.");
        } catch (RemoteException e) {
            System.out.println("Failed to set currency code:");
            handleException(e, true);
        }
    }

    private static void acceptNote() {
        System.out.println("Please insert a note.");

        do {
            delayMS(500);
            try {
                acceptedNote = cs1one.getLastNoteStatus().getLastDenom();
            } catch (RemoteException e) {
                handleException(e, true);
            } catch (APICommandException e) {
                // COMMERROR: The response acquisition may time out.
                // BADSTATE: The device may not have any data available.
                switch (e.getEnvoyError()) {
                    case COMMERROR:
                    case BADSTATE:
                        // Potentially expected, so take no action.
                        break;
                    default:
                        handleException(e, true);
                        break;
                }
            }
        } while (acceptedNote == null);

        System.out.println("Accepted note: " + acceptedNote.toString());
    }

    private static void sealBag() {
        System.out.println("Sealing the bag");

        try {
            // Execute command.
            cs1one.startWelding();

            // Wait until the bag is sealed.
            while (!bagSealed) {
                bagSealed = cs1one.getStatus() == CS1oneReplyCodes.SOLDER_WELDING_COMPLETED;
            }
        } catch (RemoteException e) {
            handleException(e, true);
        }
    }

    private static void replaceBag() {
        try {
            System.out.println(MSG_UNLOCKING_SAFE_DOOR);
            System.out.println(MSG_OPEN_SAFE_DOOR);
            cs1one.openSafe();

            System.out.println("Remove the bag, and close the safe door.");

            // Wait until the bag is removed.
            while (!bagMissing) {
                delayMS(10);
            }

            System.out.println(MSG_UNLOCKING_SAFE_DOOR);
            System.out.println(MSG_OPEN_SAFE_DOOR);
            cs1one.openSafe();

            System.out.println("Insert a new bag, and close the safe door.");

            // Wait until a new bag is inserted.
            while (!bagReplaced) {
                delayMS(10);
            }
        } catch (RemoteException e) {
            handleException(e, true);
        }
    }

    /**
     * Runs the example program.
     *
     * @param args - The arguments to the program.
     */
    public static void main(String[] args) {
        try {
            // Connect to the Envoy service.
            envoySystem = (IEnvoySystem) Naming.lookup("//localhost/envoy/system");

            // Listen for events related to the ARCA/CTS CS1one bill acceptor.
            EnvoyEventListener eel = new EnvoyEventListener();
            eel.registerHandler(new IEnvoyEventHandler() {
                @Override
                public void handleEnvoyEvent(Event evt) {
                    if (evt != null) {
                        processEnvoyEvent(evt);
                    }
                }
            });

            if (acquireCS1oneDevice()) {
                // Set the currency code.
                setCurrencyCode(CurCodeEnum.USD);

                // Enable bill acceptance.
                cs1one.enableDevice("Enable");

                // Accept a note from the user.
                acceptNote();

                // Disable bill acceptance.
                cs1one.enableDevice("Disable");

                // Seal the bag.
                sealBag();

                // Permit the bag replacement procedure to proceed as fast as possible (0 = immediate).
                cs1one.safeOpenDelay(0);

                // Replace the bag.
                replaceBag();

                // Demonstration complete.
                System.out.println("Demonstration complete.");
            } else {
                System.out.println("Unable to perform demonstration - could not acquire a CS1one from the Envoy service.");
            }

        } catch (MalformedURLException e) {
            handleException(e, true);
        } catch (RemoteException e) {
            handleException(e, true);
        } catch (NotBoundException e) {
            handleException(e, true);
        }

        System.exit(0);
    }
}
