package com.arca.envoy.example;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;

import com.arca.envoy.api.currency.CurCodeEnum;
import com.arca.envoy.api.currency.Denomination;
import com.arca.envoy.api.currency.MoneyTypeEnum;
import com.arca.envoy.api.enumtypes.DeviceType;
import com.arca.envoy.api.eventlistener.EnvoyEventListener;
import com.arca.envoy.api.iface.EBDSDispenseByDenomPrm;
import com.arca.envoy.api.iface.EBDSExtendedNoteInhibitsPrm;
import com.arca.envoy.api.iface.EBDSGetNoteTableRsp;
import com.arca.envoy.api.iface.EBDSGetRecyclerNoteTableRsp;
import com.arca.envoy.api.iface.EBDSRecyclerNoteEnablesPrm;
import com.arca.envoy.api.iface.Event;
import com.arca.envoy.api.iface.IEnvoyEventHandler;
import com.arca.envoy.api.iface.IEnvoySystem;
import com.arca.envoy.api.iface.ISCRDevice;
import com.arca.envoy.api.iface.NoteEventData;
import com.arca.envoy.api.information.DeviceInformation;

/**
 * SCR.
 *
 * This example demonstrates basic control and event handling for the MEI SCR
 * bill recycler. The following actions are performed:
 *
 *  - Connect to the Envoy service
 *  - Configure an Envoy event listener for MEI SCR bill recycler events
 *  - Locate the first registered MEI SCR bill recycler
 *  - Initialize the MEI SCR bill recycler
 *  - Display the note table
 *  - Display the recycler note table
 *  - Restrict acceptance to just USD$1
 *  - Set the USD$1 to be recycled
 *  - Wait for the user to insert a USD$1
 *  - Recycle the USD$1
 *  - Dispense the USD$1
 *  - Wait for the USD$1 to be presented to the user
 *  - Wait for the USD$1 to be retrieved by the user
 *
 * Before running this example, you will need:
 *  - A running Envoy service is necessary to run this example.
 *  - A registered MEI SCR on an RS-232 port, via EnvoyHome, is necessary to run this example.
 *  - A USD$1 bill is necessary to run this example.
 *
 * See README for compilation and running instructions.
 */
public class SCR {

    private static IEnvoySystem envoySystem;
    private static ISCRDevice scr;

    private static volatile boolean billEscrowed;
    private static volatile boolean billStacked;
    private static volatile boolean billRetrieved;

    private static volatile Denomination washington;

    private static void processEnvoyEvent(Event event) {
        switch (event.getEventType()) {
            case ESCROWED:
                billEscrowed = true;
                washington = ((NoteEventData) event.getEventData()).getNote().getDenoms().get(0);
                System.out.println("Bill Escrowed: " + washington.toString());
                break;

            case STACKED:
                billStacked = true;
                washington = ((NoteEventData) event.getEventData()).getNote().getDenoms().get(0);
                System.out.println("Bill Stacked to Recycler: " + washington.toString());
                break;

            case NOTE_PRESENTED:
                washington = ((NoteEventData) event.getEventData()).getNote().getDenoms().get(0);
                System.out.println("Bill Presented to User: " + washington.toString());
                break;

            case NOTE_RETRIEVED:
                billRetrieved = true;
                washington = ((NoteEventData) event.getEventData()).getNote().getDenoms().get(0);
                System.out.println("User Retrieved Bill: " + washington.toString());
                break;

            default:
                System.out.println("Event received: " + event.getEventType());
                break;
        }
    }

	/**
	 * Main.
	 *
	 * @param args - Command line arguments, not used.
	 */
    public static void main(String[] args) {
        try {
            // Connect to the Envoy service.
            envoySystem = (IEnvoySystem) Naming.lookup("//localhost/envoy/system");

            // Listen for events related to the MEI SCR bill recycler.
            EnvoyEventListener eel = new EnvoyEventListener();
            eel.registerHandler(new IEnvoyEventHandler() {
                @Override
                public void handleEnvoyEvent(Event evt) {
                    if (evt != null) {
                        processEnvoyEvent(evt);
                    }
                }
            });

            String devicePath = null;
            
            // Locate the first registered MEI SCR bill recycler.
            for (String deviceName : envoySystem.getRegisteredDeviceNames()) {
                DeviceInformation deviceInformation = envoySystem.getRegisteredDeviceInformation(deviceName);

                if (deviceInformation.getDeviceType() == DeviceType.MEI_SCR) {
                    devicePath = envoySystem.getDevicePath(deviceName);
                    break;
                }
            }

            // Connect to the MEI SCR bill recycler.
            if (devicePath != null) {
                scr = (ISCRDevice) Naming.lookup(devicePath);
            }

            // And finally, perform the demonstration.
            if (scr != null) {

                // Initialize the MEI SCR bill recycler.
                scr.mechaReset();

                // Display the note table.
                EBDSGetNoteTableRsp gntr = scr.getNoteTable();
                for (Denomination note : gntr.getNoteTable()) {
                    System.out.println("Note Table Entry: " + note.toString());
                }

                // Display the recycler note table.
                EBDSGetRecyclerNoteTableRsp grntr = scr.getRecyclerNoteTable();
                for (Denomination recycleableNote : grntr.getNoteTable()) {
                    System.out.println("Recycler Note Table Entry: " + recycleableNote.toString());
                }

                // Restrict acceptance to just USD$1.
                ArrayList<Denomination> inhibited = new ArrayList<Denomination>(0);
                inhibited.add(CurCodeEnum.USD.denom(MoneyTypeEnum.BILL, 2));
                inhibited.add(CurCodeEnum.USD.denom(MoneyTypeEnum.BILL, 5));
                inhibited.add(CurCodeEnum.USD.denom(MoneyTypeEnum.BILL, 10));
                inhibited.add(CurCodeEnum.USD.denom(MoneyTypeEnum.BILL, 20));
                inhibited.add(CurCodeEnum.USD.denom(MoneyTypeEnum.BILL, 50));
                inhibited.add(CurCodeEnum.USD.denom(MoneyTypeEnum.BILL, 100));
                scr.setNoteInhibits(new EBDSExtendedNoteInhibitsPrm(inhibited));

                // Set the USD$1 to be recycled.
                ArrayList<Denomination> recycled = new ArrayList<Denomination>(0);
                recycled.add(CurCodeEnum.USD.denom(MoneyTypeEnum.BILL, 1));
                EBDSRecyclerNoteEnablesPrm nvePrm = new EBDSRecyclerNoteEnablesPrm(recycled);
                scr.setRecyclerNoteEnables(nvePrm);

                // Enable the SCR.
                scr.enableDevice(true);

                // Inform the user that the SCR is now expecting the USD$1.
                System.out.println("Please insert a USD$1 bill.");

                // Wait for the user to insert a USD$1.
                while (!billEscrowed) {
                    // Just wait.
                }

                // Recycle the USD$1.
                scr.recycleBill();

                // Wait for the bill to be recycled.
                while (!billStacked) {
                    // A little more waiting.
                }

                // Dispense the USD$1.
                EBDSDispenseByDenomPrm scrdbdprm = new EBDSDispenseByDenomPrm(washington, 1);
                scr.dispenseByDenom(scrdbdprm);

                // Wait for the user to take the USD$1.
                System.out.println("Please take the USD$1 bill.");

                // Wait for the bill to be retrieved.
                while (!billRetrieved) {
                    // Wait.
                }

                // Disable the SCR.
                scr.enableDevice(false);
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (NotBoundException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // Demonstration complete.
        System.exit(0);
    }
}
