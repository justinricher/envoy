package com.arca.envoy.examples.one.four;

import java.util.Arrays;

import jpos.ItemDispenser;
import jpos.JposException;

import com.arca.envoy.javapos.Init;

/**
 * RCDJavaPOS.
 *
 * This example program demonstrates JavaPOS item dispenser service for the
 * Banking Automation Rolled Coin Dispenser.
 *
 * Before running, you will need:
 *  - a running Envoy service instance
 *  - a registered RCD with the default logical name of 'RCD'.
 *
 * In the RCD example program, the item dispenser service is obtained and
 * prepared for use. The item dispenser dispenses one of each coin roll. When
 * the dispensing completes, a summary is written to the console, and the item
 * dispenser service is released and closed.
 *
 * See README for compilation and running instructions.
 */
public class RCDJavaPOS {
    // Describe the rolled coin dispenser configuration.
    private static final String RCD_LOGICAL_NAME = "RCD";
    private static final int[] RCD_PENNY_SILOS = new int[] {1, 3};
    private static final int[] RCD_NICKEL_SILOS = new int[] {2};
    private static final int[] RCD_DIME_SILOS = new int[] {4};
    private static final int[] RCD_QUARTER_SILOS = new int[] {5, 6};

    // Maintain a connection to the item dispenser service.
    private static final ItemDispenser IDS = new ItemDispenser();

    // Provide simple indicator of success in example application.
    private static final int EXIT_OK = 0;
    private static final int EXIT_ERR = 1;

    /**
     * Attempts to dispense the desired count of rolled coins from the specified silo.
     *
     * @param silo The silo from which to dispense rolled coins.
     * @param count The number of rolled coins to dispense.
     * @return The number of rolled coins dispensed; a negative number indicates the error that occurred.
     */
    private static int dispenseRolledCoinsFromSilo(int silo, int count) {
        int dispensed = 0;

        // If successful, dispenseItem(int[] counts, int silo) returns dispensed count in counts[0].
        int[] counts = new int[] {count};

        try {
            IDS.dispenseItem(counts, silo);
            dispensed = counts[0];
        } catch (JposException e) {
            dispensed = -e.getErrorCode();
        }

        Arrays.fill(counts, 0);
        counts = null;

        return dispensed;
    }

    /**
     * Dispenses rolled coins from multiple silos, if necessary and/or available.
     * <p>
	 * Any error that may have occurred while attempting to dispense from a
	 * particular silo is written to the console. Since there may be an empty
	 * condition on a silo, this error does not halt the dispensing attempts.
     *
     * @param silos The silos from which to dispense coins.
     * @param rollsToDispense The number of rolled coins to dispense.
     * @return The number of rolled coins dispensed across all silos.
     */
    private static int dispenseRolledCoins(int[] silos, int rollsToDispense) {
        int dispensed = 0;

        int fromSilo = -1;
        int siloIndex = 0;
        int silo = -1;

        while (dispensed < rollsToDispense && siloIndex < silos.length) {
            fromSilo = dispenseRolledCoinsFromSilo(silos[siloIndex++], rollsToDispense - dispensed);

            if (fromSilo < 0) {
                silo = silos[siloIndex - 1];
                System.out.println(String.format("An error occurred attempting to dispense rolled coins from silo %d.", silo));
            } else {
                dispensed += fromSilo;
            }
        }

        return dispensed;
    }

    /**
     * Formats the expected versus actual dispensed coin rolls in preparation for console output.
     *
     * @param coinType The multiplicative coin type, such as 'pennies'.
     * @param expected The desired count of coin rolls to dispense.
     * @param actual The actual count of dispensed rolled coins.
     * @return The formmated summary of the dispensed coin rolls.
     */
    private static String getDispensedRollSummary(String coinType, int expected, int actual) {
        return String.format("\n\tRequested %d roll(s) of %s, dispensed %d roll(s).", expected, coinType, actual);
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
         *   3) The RCD is registered
         */
        try {
            // Track desired counts of coin rolls to dispense.
            int pennyRollsToDispense = 1;
            int nickelRollsToDispense = 1;
            int dimeRollsToDispense = 1;
            int quarterRollsToDispense = 1;

            // Open, claim, and enable the item dispenser service.
            IDS.open(RCD_LOGICAL_NAME);
            IDS.claim(0);
            IDS.setDeviceEnabled(true);

            // Track actual counts of dispensed coin rolls.
            int pennyRollsDispensed = dispenseRolledCoins(RCD_PENNY_SILOS, pennyRollsToDispense);
            int nickelRollsDispensed = dispenseRolledCoins(RCD_NICKEL_SILOS, nickelRollsToDispense);
            int dimeRollsDispensed = dispenseRolledCoins(RCD_DIME_SILOS, dimeRollsToDispense);
            int quarterRollsDispensed = dispenseRolledCoins(RCD_QUARTER_SILOS, quarterRollsToDispense);

            // Display the dispensing summary.
            System.out.println("Dispensed rolled coins summary:");
            System.out.println(getDispensedRollSummary("pennies", pennyRollsToDispense, pennyRollsDispensed));
            System.out.println(getDispensedRollSummary("nickels", nickelRollsToDispense, nickelRollsDispensed));
            System.out.println(getDispensedRollSummary("dimes", dimeRollsToDispense, dimeRollsDispensed));
            System.out.println(getDispensedRollSummary("quarters", quarterRollsToDispense, quarterRollsDispensed));

            // Release and close device services.
            IDS.release();
            IDS.close();

            System.exit(EXIT_OK);
        } catch (JposException e) {
            e.printStackTrace();
            System.exit(EXIT_ERR);
        }
    }
}
