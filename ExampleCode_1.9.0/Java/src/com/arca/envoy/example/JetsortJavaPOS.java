package com.arca.envoy.examples.one.four;

import static jpos.CoinAcceptorConst.CACC_DEPOSIT_COMPLETE;
import jpos.CoinAcceptor;
import jpos.JposException;

import com.arca.envoy.javapos.Init;
import com.arca.envoy.javapos.Inventory;

/**
 * JetsortJavaPOS.
 *
 * This is an example program that demonstrates some of the new features
 * available in Envoy 1.4, specifically the JavaPOS coin acceptor service for
 * the Cummins Jetsort 1000 series coin sorter.
 *
 * Before running this example, you will need:
 *  - a running Envoy service instance to run properly.
 *  - a Jetsort is registered with the default logical name of 'JETSORT'.
 *
 * In the JetsortJavaPOS example program, the coin acceptor service is obtained
 * and prepared for use. The coin acceptor begins a new deposit, letting the
 * user know by spinning the table platter. After starting the deposit, a user
 * may or may not insert loose coins into the coin acceptor. After the platter
 * of the coin acceptor stops spinning, a summary of the accepted coins is
 * printed out to the console, and the coin acceptor service is released and
 * closed.
 *
 * See README for compilation and running instructions.
 */
public class JetsortJavaPOS {
    // Describe the loose coin acceptor configuration.
    private static final String JETSORT_LOGICAL_NAME = "JETSORT";

    // Maintain connections to the coin device services.
    private static final CoinAcceptor CAS = new CoinAcceptor();

    // Provide simple indicator of success in example application.
    private static final int EXIT_OK = 0;
    private static final int EXIT_ERR = 1;

    /**
     * Formats the exchange summary for a particular coin type in preparation for console output.
     *
     * @param coinType The multiplicative loose coin type, such as 'pennies'.
     * @param accepted The count of the accepted coin type.
     * @return The formmated coin exchange summary.
     */
    private static String getAcceptedCoinSummary(String coinType, int accepted) {
        return String.format("\n\tLoose %s accepted: %d", coinType, accepted);
    }

	/**
	 * Main.
	 *
	 * @param args - Command line arguments, not used.
	 */
    public static void main(String[] args) {
        // Initialize JavaPOS
        Init.setup(System.getProperty("java.io.tmpdir"));

        /**
         * At this point, it is assumed that:
         *   1) Envoy Service is running
         *   2) Envoy is licensed
         *   3) The Jetsort acceptor is registered
         */
        try {
            // Tracks counts for accpeted loose coins.
            int penniesAccepted = 0;
            int nickelsAccepted = 0;
            int dimesAccepted = 0;
            int quartersAccepted = 0;

            // Open, claim, and enable device services.
            CAS.open(JETSORT_LOGICAL_NAME);
            CAS.claim(0);
            CAS.setDeviceEnabled(true);

            // Accept loose coins.
            CAS.beginDeposit();
            CAS.fixDeposit();
            CAS.endDeposit(CACC_DEPOSIT_COMPLETE);

            // Use provided Inventory object to extract the coin counts.
            Inventory inventory = new Inventory(CAS.getDepositCounts());
            for (int denom : inventory.getDenomList()) {
                int count = inventory.getCount(denom);
                switch (denom) {
                    case 1:
                        penniesAccepted = count;
                        break;
                    case 5:
                        nickelsAccepted = count;
                        break;
                    case 10:
                        dimesAccepted = count;
                        break;
                    case 25:
                        quartersAccepted = count;
                        break;
                    default:
                        break;
                }
            }
            inventory.clear();
            inventory = null;

            // Display the exchange summary.
            System.out.println("Accepted loose coins summary:");
            System.out.println(getAcceptedCoinSummary("pennies", penniesAccepted));
            System.out.println(getAcceptedCoinSummary("nickels", nickelsAccepted));
            System.out.println(getAcceptedCoinSummary("dimes", dimesAccepted));
            System.out.println(getAcceptedCoinSummary("quarters", quartersAccepted));

            // Release and close device services.
            CAS.release();
            CAS.close();

            System.exit(EXIT_OK);
        } catch (JposException e) {
            e.printStackTrace();
            System.exit(EXIT_ERR);
        }
    }
}
