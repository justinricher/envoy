import jpos.BillDispenser;
import jpos.JposConst;
import jpos.JposException;

import com.arca.envoy.javapos.Init;

/**
 * F53JavaPOS.
 *
 * Standalone small application demonstrating the use of ARCA JavaPOS.
 *
 * In this simple example, the bill dispenser object is obtained as a private
 * static at the top. An Envoy method, Init.setup() is called to initialize the
 * JavaPOS interface. Then, the methods, open(), claim() and setDeviceEnabled()
 * are called in order to ready the device for operation.
 * 
 * After this, the JavaPOS checkHealth() method is called with a constant flag
 * that determines the level of check (JPOS_CH_INTERNAL, JPOS_CH_EXTERNAL).
 * 
 * Finally, the device is disabled, released and closed.
 *
 * See README for compilation and running instructions.
 *
 * @param args - Command line arguments (Not used)
 */
public class F53JavaPOS {

    private static final String LDN = "FUJITSU_F53";
    private static final int TIMEOUT = 5;
    private static final int EXITOK = 0;
    private static final int EXITERR = 1;

    private static final int CHKHEALTHLVL = JposConst.JPOS_CH_INTERNAL;
    //private static final int CHKHEALTHLVL = JposConst.JPOS_CH_EXTERNAL;

    private static final BillDispenser BDS = new BillDispenser();

	/**
	 * Main.
	 *
	 * @param args - Command line arguments, not used.
	 */
    public static void main(String[] args) {
        Init.setup(System.getProperty("java.io.tmpdir"));
        try {

            /**
             * At this point, it is assumed that:
             *   1) Envoy Service is running
             *   2) Envoy is licensed
             *   3) The F53 Dispenser is registered
             */
            // Open the connection, claim the device and enable it
            BDS.open(LDN);
            BDS.claim(TIMEOUT);
            BDS.setDeviceEnabled(true);

            /**
             *  Check health, level 1
             *
             *  The checkHeath method takes one of three integer arguments:
             *  INTERNAL    - 1 Gets the F53 status and changes getHealthText
             *  EXTERNAL    - 2 Dispense one note from each cassette into the reject tray
             *                  and change getHealthText
             *  INTERACTIVE - 3 Presents modal interface for check - NOT CURRENTLY IMPLEMENTED
             */

            BDS.checkHealth(CHKHEALTHLVL);

            // Show result of checkHealth on standard out
            System.out.println("\n>>>>>>> F53JavaPOS output >>>>>>> "
                    + BDS.getCheckHealthText() + "\n");

            // Disable the device, release it and close the connection
            BDS.setDeviceEnabled(false);
            BDS.release();
            BDS.close();

            // Exit the program
            System.exit(EXITOK);
        } catch (JposException e) {
            e.printStackTrace();
            System.exit(EXITERR);
        }
    }
}
