Envoy Programmer's Guide
Introduction

This manual is intended for programmers who will develop applications using the Envoy Software Development Kit (SDK). Envoy is designed to unify the interaction of several transaction management devices into a single Application Programming Interface (API). The diagram below demonstrates how the Envoy API could be used to interact with various objects.


*For F53 only in this release

This guide assumes that you, the programmer, already have a working knowledge of writing applications for the operating environment that you will be using and that you have the appropriate manuals and reference sources for that environment.

The following topics are covered:

    Installation & Requirements
    Description of Functions and Responses
    Error Codes and Error Handling

Installation
System Requirements

The system requirements for installation of Envoy are as follows:

    Supported Operating System
        Microsoft Windows 7 (x86 or x64)
        Ubuntu Linux 14.04 LTS (x86 or x64)
    Java 8 Runtime Environment (x86 or x64) or later

NOTE: To prevent issues with communication when using a Serial-to-USB adapter to connect a device to a host running Envoy, ensure that the most recent manufacturer's device drivers are installed for the adapter.
Installation

Envoy is a service installed on a host PC that provides connectivity to supported devices. The Envoy service is automatically created and started on the host PC upon installation.

Installation on a Windows platform is performed by executing the Envoy Windows installation file available for download from the ARCA Developer Portal. To prevent driver installation failure, any device which uses USB must be connected during both installation and uninstallation.

Installation on a Linux platform is performed by executing the Linux installation script available for download from the ARCA Developer Portal. A quick start guide to installing Envoy on Linux is also available for download from the ARCA Developer Portal (http://developer.arca.com/envoy).

The Linux system administrator can control the Envoy Service via existing Upstart commands.

$ sudo service envoy start       // Start Envoy.
$ sudo initctl list | grep envoy // Show that Envoy is Running.
$ sudo service envoy stop        // Stop Envoy.

Envoy Home

EnvoyHome is used to complete licensing and registration of devices for operation using the Envoy platform. The EnvoyHome application can be found within the application installation directory. On Windows machines a shortcut is created within the ARCA sub-folder of the Start Menu.

Default installation paths:

        Linux: /opt/ARCA/envoy

Licensing

Before device communication can begin, the Envoy platform needs to confirm the licensing information provided by our sales team. Licensing can be performed within the left-most panel of Envoy Home. Once licensed, the panel will be updated and the prompt for licensing will be replaced with: "Licensed".

Should internet connectivity not be available on the host PC, the left panel also provides a web address to follow on an internet-capable device to complete the licensing and registration process. The site will request the unique Host ID available within the licensing panel of Envoy Home.
Connection Management

Once licensing is complete, each device needs to be paired with the locally installed Envoy service.  Using the Envoy Home application makes pairing simple, providing a Logical Device Name for each device to uniquely identify it.
C# Language Support

NOTE: These instructions are for Windows only. 

Envoy supports C# customer applications natively with the use of the IKVM DLL library (http://weblog.ikvm.net/). As is required with Java-based Envoy applications, the Envoy Service must be running in order for the C# application to work as expected.

Envoy now includes a pre-compiled LibEnvoyAPI.dll. Prior to Envoy 1.9 customers were required to compile the DLL manually. The DLL is built from IKVM version 8.1.5717.0 Release Candidate.

The DLL can be used to run the C# example code provided on the ARCA Developer Portal for the F53 and SCN devices.

Customers will need to use the NuGet package manager in Visual Studio to obtain IKVM dependencies for their C# projects. Once available it will be possible to link an application with IKVM DLLs and LibEnvoyAPI.dll.

The following command line represents how to compile your C# application:

C:\test> csc /r:LibEnvoyAPI.dll,IKVM.OpenJDK.Core.dll,IKVM.OpenJDK.Util.dll,IKVM.OpenJDK.Naming.dll,IKVM.OpenJDK.Remoting.dll,IKVM.Runtime.dll /lib:[path_to_IKVM]\bin ExampleF53.cs

Once compiled, execute the example:

C:\test> ExampleF53.exe

Platform Functions
Envoy API Functions
Get Device

Once registered, the LDN can be used to request access to the device using Get Device. The device object exposes the control API for the specified device.
License by Activation Code

License by Activation Code uses an available internet connection to license Envoy with a 16-digit activation code provided with purchase of the Envoy platform. If activation is successful a persistent license file is then written to disk.
License by File

License by File uses the contents of a license file to validate the Envoy license and if successful writes a persistent license file to disk. This function does not require an internet connection and is used when a valid license file is available such as restoring from backup.
List Devices

List Devices retrieves a list of Envoy-supported devices, which includes multiple transaction management devices. The Application should call this function to verify that the desired device is listed, available, and registered.
MoneyGram

The MoneyGram object is provided within the Envoy API and it is used to represent arbitrary collections of currency. MoneyGram collections are represented as media/quantity pairs. MoneyGrams support the representation of Non-Cash Media and traditional cash collections such as a safe, cash register, or cash bag.
Non-Cash Media

Support for Non-Cash Media is available within the Envoy API. Non-Cash Media is a non-denomination specific media that can assume any value or name given. Non-cash media can be used in an Envoy MoneyGram or any applicable device function.
Register Device

Register Device announces to Envoy that a dispenser, acceptor, or recycler device should be communicated with via a Logical Device Name (LDN). This LDN is used hereafter as a shortcut to directly control the hardware. Registration is maintained in persistent memory. This means that restarting the application, Envoy, the host PC or the device itself will not affect the LDN or any other saved values.
Envoy 1.9 Device Registration Changes

NOTE: For applications built using the JavaPOS API, the Envoy 1.9 release has no affect on backwards compatibility.

Device registration changes were made with the release of Envoy 1.9 to improve the scalability of the registration process within Envoy. While it is possible to use application-defined implementations of the new DeviceInformation interfaces for registration, the registration package (com.arca.envoy.api.registration) provides the necessary implementations for the respective device communication types.

    Removed API Classes (com.arca.envoy.api.*)
        iface.DeviceInfo
        iface.DeviceInfoLoader
        iface.ListDevRsp
    Removed API Methods (com.arca.envoy.api.iface.IEnvoySystem)
        ListDevRsp listUSBDevices()
        ListDevRsp listRS232Devices()
        void registerDevice(RegisterPrm)
        void unregisterDevice(String)
    New API Interfaces (com.arca.envoy.api.*)
        information.DeviceInformation
        information.NetworkDeviceInformation
        information.RS232DeviceInformatino
        information.USBDeviceInformation
    New API Classes (com.arca.envoy.api.*)
        registration.DeviceRegistrationRequest
        registration.NetworkDeviceRegistrationRequest
        registration.RS232DeviceRegistrationRequest
        registration.USBDeviceRegistrationRequest
    New API Methods (com.arca.envoy.api.iface.IEnvoySystem)
        boolean isValidForRegistration(String)
        boolean isRegisteredDeviceName(String)
        HashSet<String> getRegisteredDeviceNames()
        DeviceInformation getRegisteredDeviceInformation(String)
        String getRegisteredDeviceNameBySerialNumber(String)
        String getRegisteredDeviceNameByDetachementIdentifer(String)
        DeviceInformation getDeviceInformationBySerialNumber(String)
        DeviceInformation getDeviceInformationByDetachmentIdentifer(String)
        LinkedHashSet<DeviceInformation> getAllKnownDeviceInformation()
        boolean register(String, DeviceInformation)
        DeviceInformation unregister(String)

Unregister Device

Unregister Device removes the selected device and LDN from memory. Until the device is registered again, Envoy will not be able to communicate with that device.
Envoy API Support for F53 Dispenser
Auto-Substitute

Auto-substitute is a parameter of the Dispense by Amount and Dispense by Type functions. Auto-substitute permits the unit to detect if a cassette is low and, when available, it will automatically select another cassette to dispense from.

If the Dispense by Amount function is called, the unit will auto-substitute with the next highest available denomination to meet the requested amount.

If the Dispense by Type function is called, the unit will only auto-substitute if another cassette is identified with the same denomination type.
Device Status

Among the information provided is Error Registers, Sensor Registers, Cassette Registers and Bill Length/Thickness information.

In most cases this function is used for the following purposes:

    Check connection to device
    Read the cassette denomination to magnet mapping
    Read the near end sensor (low media sensor) of cassettes
    NOTE: The F53 dispenser is unable to detect if cassettes are empty
    Check POM (thickness) sensor level

Device Initialization

The Initialization command is required for normal operation of the dispenser. Not only does it set the bill parameters, but it also self-checks several sensors and motors.
Dispense by Position

The Dispense by Position command will pick bills as requested per cassette position. Since each bill is checked before presentation to the customer, the correct bill parameters must be set prior to dispensing.
Dispense by Denomination

The Dispense by Denomination command analyzes the cassette types in the dispenser and will pick bills as requested by cassette denomination Ðregardless of position. If one cassette is emptied of notes and another of the same type is not empty, Envoy will auto-substitute notes from the non-empty cassette.
Dispense by Amount

The Dispense by Amount command analyzes the cassette types in the dispenser and will pick bills as requested by total value. As long as the requested amount can be satisfied with the given cassette denomination, the optimal bill quantities will be picked. Dispense by Amount will also use the auto-substitute feature to ensure more successful dispenses.
Device Information

The Device Information command retrieves setting information, user data storage, and the factory serial number from the dispenser.
Set User Data

The Set User Data command will write up to 40 bytes of data to persistent memory for later retrieval. An example usage for this function would be to add a text string identifier to the unit that can be retrieved at a later time for identification.
Remap Denominations

The Remap Denominations function configures EnvoyHome to operate using the currency code that is passed to it as a parameter.  Currently EnvoyHome has support for USD, CAD, EUR, GBP, and MXN. NOTE: This configuration is not persistent and will be reset to USD when the EnvoyHome application is closed.
Bill Diagnosis

The Bill Diagnosis command instructs the device to collect metrics while dispensing 20 notes from a specified cassette to the reject bin. The device could then be initialized with note parameters configured for that cassette using the average length and thickness of the 20 bills dispensed.
Get Sensor Health

The Get Sensor Health command will present the signal strength of the dispenser's various sensors. This is useful for predicting when maintenance might be necessary.

The sensor health levels reported are for the following sensors:

    FDLS1
    FDLS2
    FDLS3
    FDLS4
    FDLS5
    FDLS6
    DFSS
    REJS
    BPS
    BRS1
    BRS2
    BRS3
    EJSR
    EJSF
    BCS

 
Envoy Device Event Summary: Fujitsu F53
No Envoy Events 1	 

1 Fujitsu bill dispensers indicate various states such as jammed conditions through error codes found in the response byte string. In Envoy, these are accessible through the common response object.

 
Envoy API Support for F400 Dispenser

The Fujitsu F400 Bill Dispenser features all of the same core functionality as the F53 but at a higher capacity. Whereas the F53 supports a maximum dispense of 20 notes per command, the F400 supports a maximum of 99 notes dispensed per command.
Get Sensor Health

The Get Sensor Health command will present the signal strength of the dispenser's various sensors. This is useful for predicting when maintenance might be necessary.

The sensor health levels reported are for the following sensors:

    PS1
    PS2
    PS3
    PS4
    PS5
    BPS
    GSS
    CPS
    RJS
    PS6

 
Envoy Device Event Summary: Fujitsu F400
No Envoy Events 1	 

1 Fujitsu bill dispensers indicate various states such as jammed conditions through error codes found in the response byte string. In Envoy, these are accessible through the common response object.

 
Envoy API Support for F510 Dispenser
Transport

Transport moves the dispensed notes from the device pooling section to the customer area. The call to Transport ends as soon as the bills arrive.

Options include:

    Transport to just behind the shutter via Ready to Deliver option.
    Transport to customer area via Deliver option.

Partial Dispenses are handled by either rejecting the notes or sending the partial bundle to the customer.

    Rejecting the notes from the pool is accomplished via Mechanical Reset.
    Sending to the customer requires calling Transport with Ready to Deliver option and again with Deliver option.

Retrieve

Bills that have been Transported to the customer area may be Retrieved, or pulled back and rejected. Rejected notes will be sent to the loose area of the first Cassette.

Retrieve can only act on bundles that have been Transported using either the Ready to Deliver or Deliver options.
 
Envoy Device Event Summary: Fujitsu F510
No Envoy Events 1	 

1 Fujitsu bill dispensers indicate various states such as jammed conditions through error codes found in the response byte string. In Envoy, these are accessible through the common response object.

 
Envoy API Support for MEI SCN Acceptor
Enable Device

Enable Device can be used to toggle the ready state of the MEI SCN Cashflow device. The device is enabled when the indicator lights are powered on and the unit will accept notes for validation. When disabled, the feeder will not engage when notes are presented device. The unit is available to queries and commands from the Envoy platform regardless of the ready state. 
Device Capabilities

The Display Capabilities function will provide additional information about the physical capabilities of the current device configuration. This information can be used to extend the base functionality of the unit. Additional information regarding the capabilities and how to use them is available from within the JavaDoc.
Mechanical Reset

Sends a mechanical reset command to the MEI SCN device. When a mechanical reset is performed any notes currently stored in escrow will be automatically stacked when the device is reinitialized. A mechanical reset does not alter the enabled state of the device.
Get Audit Performance Measurements

The Get Audit Performance Measurements function is used to collect performance data such as the number of jams and operating hours of the unit. The data is provided as an object with several fields that can be accessed for processing at the application level. Additional information regarding the fields and values returned is available from within the JavaDoc.
Get Audit QP Measurements

Get Audit QP (Quality Performance) Measurements provides performance data such as the rate of note acceptance and the number of denominations recognized. The data is provided as an object with several fields that can be accessed for processing at the application level. Additional information regarding the fields and values returned is available from within the JavaDoc.
Get Acceptor Application ID

Get Acceptor Application ID provides the ID of the device from firmware. The software ID is created by MEI and applied to the unit firmware. The ID is static and cannot be changed.
Get Acceptor Application Part Number

Get Acceptor Application Part Number provides the software part number from acceptor firmware. The part number is created by MEI and applied to the unit firmware. The part number is static and cannot be changed.
Get Acceptor Boot Part Number

Get Acceptor Boot Part Number provides the software part number from the boot firmware. The boot is the part of the SCN Cashflow acceptor that holds the cash box. The part number is created by MEI and applied to the unit firmware. The part number is static and cannot be changed.
Get Acceptor Type

Get Acceptor Type provides the model number for the acceptor unit. The value returned is a string of characters that represent a variety of configurable items such as model, cassette size, and interface type.
Get Lifetime Totals

Get Lifetime Totals will provides stored totals including the number of notes validated and number of motor starts. The data is provided as an object with several fields that can be accessed for processing at the application level. Additional information regarding the fields and values returned is available from within the JavaDoc.
Get Note Table

Get Note Table obtains a list of notes the acceptor is configured to validate and stack. For multiple issue denominations, e.g. 100 USD, the command will report more than one type of 100 USD note.
Get Serial Number

Get Serial Number provides the serial number of the acceptor head as a 20-byte string.
Get Variant Name

Get Variant Name provides the variant ID that indicates which banknotes can be accepted by the current firmware.
Status

The Status function returns an overall status of the acceptor. The values returned include:

    The state of the acceptor (idling, accepting, stacked, etc.)
    The status
    If the unit is enabled
    The firmware version
    The denomination of the note in escrow
    A list of denominations that the acceptor can validate. 

 
Envoy Device Event Summary: MEI SCN
DISABLED	Will not accept bills
ENABLED	Ready to accept bills
CASSETTE_ATTACHED	Cash box installed
CASSETTE_REMOVED	Cash box removed
CASSETTE_FULL	Cash box full
JAMMED	Machine has jammed
JAM_CLEARED	Previous jam now cleared
CHEATED	Machine has detected a cheat
ESCROWED	Bill has been accepted and is in the deviceÕ intermediate area.
STACKED	Note has been stored into device storage
RETURNED	Bill that was in escrow has been returned

 
Envoy API Support for MEI SCR Recycler

The MEI SCR is a device which not only possesses all of the capabilities that the MEI SCN bill acceptor does, but it can also perform bill dispense functions.

NOTE: The MEI SCR device does not have simultaneous support for multiple currencies. The unit can only operate one currency configuration type at a time.

There are conditions in which it may be possible to issue a command to the MEI SCR that does not have a directly observable result. An example of such a condition would be to issue a command to recycle a note quantity of zero. For additional information about these conditions and their output refer to Addendum: SCR Edge Conditions in EnvoyHome.
Get Recycler Note Table

The Get Recycler Note Table provides a complete list of denominations the recycler is capable of recycling. The denominations listed are based upon the current currency configuration of the unit.
Set Recycler Note Enables

The Set Recycler Note Enables function allows you to configure which denominations the unit will store within its spools for recycling.

    Only denominations listed in the Get Recycler Note Table will be available to enable.
    Only one denomination may be enabled per spool, however the same denomination may be enabled for multiple spools.
    Denominations which are not enabled to be recycled can only be stored within the device cash box.

NOTE: Changing the Recycler Note Enabled state requires that the affected spool be empty prior to execution.
Store to Recycler

Store to Recycler is used to receive an acceptable note from escrow and store it in the appropriate spool. The note is immediately counted and becomes available for dispense and float functions.
Dispense by Denom

The Dispense by Denom function removes a number of notes of the given denomination from the spool and presents them to the operator. In the event that there are multiple notes to dispense the unit will wait until the operator has taken the note presented before dispensing the next until the operation completes.
Float

The Float function will remove a number of notes of the given denomination from the unit spool and store them in the device cash box. Once the notes are stored in the cash box they are removed from the pool of recyclable notes.
 
Envoy Device Event Summary: MEI SCR
DISABLED	Will not accept bills
ENABLED	Ready to accept bills
TRANSPORT_OPEN	The bill transport path is open
TRANSPORT_CLOSED	The bill transport path has been closed
JAMMED	Machine has jammed
JAM_CLEARED	Previous jam now cleared
CHEATED	Machine has detected a cheat
ESCROWED	Bill has been accepted and is in the deviceÕ intermediate area.
STACKED	Note has been stored into device storage
RETURNED	Bill in escrow has been returned to user
CASSETTE_FULL	A spool is full and can no longer stack bills
DISPENSE_STARTED	The dispense operation has been initiated
NOTE_PRESENTED	A note is being presented to the user
NOTE_RETRIEVED	A presented note has been removed
DISPENSE_COMPLETE	Dispense operation finished
FLOAT_STARTED	A float operation has been started
FLOAT_COMPLETE	The float operation is now complete

 
Envoy API Support for Banking Automation RCD
Check Happy

Check Happy queries the RCD for status of each silo within the unit. The status for each silo is then returned to EnvoyHome to be parsed. Example return statuses include OK, Jammed, and Not Installed.
Set LED

The Set LED function can be used to independently set the LED state-pattern and color of each silo. The LED state-patterns available are steady or flashing and the color options are green, red, or yellow.
Dispense Coin

Dispense Coin will dispense coin rolls from silos and is issued on a per-row basis. As an example the operator could dispense 2 rolls from silo 1, 3 rolls from silo 2, 1 from silo 3, etc. A separate command must be issued if coins are to be dispensed from the second shelf.
 
Envoy Device Event Summary: Banking Automation RCD
SILO_EMPTY	A dispense operation resulted in fewer rolls dispensed than the number requested

 
Envoy API Support for Cummins Allison Jetsort
Jetsort State Diagram

Start Batch

The Start Batch function issues a command to the unit which begins rotation of the sorting table. Coin can then be fed into the unit to be counted, sorted, and stored as batch totals. The sorting table will automatically stop rotating after approximately 10 seconds of inactivity from the moment of the Start Batch function being called or the final coin being sorted.
Stop Batch

Stop Batch instructs the unit to end the current batch and stop rotation of the sorting table.
Continue Batch

Continue Batch will restart the motor and continue batch sorting after a Stop Motor command has been received.
End Batch

The End Batch function terminates the current batch and stops the rotation of the sorting table. Batch totals remain in the machine until cleared.
Stop Motor

The Stop Motor function will instruct the unit to immediately stop rotation of the sorting table.
Get Bag Totals

Get Bag Totals returns an Envoy MoneyGram representation of the bag totals for a batch, sub-batch, day, or a container. All coin denomination counts are also included within the MoneyGram. Additional information regarding the Envoy MoneyGram is available from within the JavaDoc on Developer Portal.
Clear Bag Total

Clear Bag Total clears the bag totals for the specified coin denomination. This function must be performed for each denomination individually. To clear all denominations a developer could collect a list of denominations using results from the Get Bag Totals function.
Set Bag Limit

The Set Bag Limit function places an upper limit on the number of coins by denomination that the unit will process. Once the limit has been reached the unit will automatically stop rotating the sorting table.
 
Envoy Device Event Summary: Cummins Allison Jetsort
JAMMED	The machine is jammed
JAM_CLEARED	Previous jam now cleared
BAG_FULL	The cash bag is full
BAG_FULLOK	Previous bag full condition has been cleared

 
Envoy Support for the CSeXtra Deposit Device
Get Status

The Get Status function returns the sensor values for each sensor. Most values return as Boolean but a full list of sensors and value details are available within the JavaDoc for CSeXtraStatusRsp.
Get Note Count by Type

Get Note Count by Type returns the note quantity currently in the bag for each denomination of the configured currency.
Get Currency Code

Displays up to three currency codes which are accepted by the CSeXtra.  Valid currencies are EUR, GBP, and USD.
Reset Device

The Reset Device function will clear notes from the note path and then it will reset the device state. As example, if power to the device was lost during operation the Reset Device function would be used to expel any notes back to the user.
Deposit Notes

The Deposit Notes function will instruct the device to begin accepting notes until there are either no additional notes within the feeder, or a specified number of notes have been accepted.  
Open Safe

The Open Safe function permits a user to open the device safe within 5 minutes of sending the command. If the safe is not opened within the timeout period the safe door will lock automatically.
Seal Bag

The Seal Bag function follows a specific series of steps to wield the current cash-bag shut. Failure to complete the steps will result in a device status code of 0x8C. The steps needed to complete the operation are as follows:

    Enter unlock code
    Execute Seal Bag command
    Open safe door
    Remove and replace bag
    Close safe door

 
Envoy Device Event Summary: CSeXtra
STACKED	Note has been stored into device storage
DOOR_OPEN	The safe door has been opened
DOOR_CLOSED	Safe door has been closed
JAMMED	The machine is jammed
JAM_CLEARED	Previous jam now cleared
BAG_FULL	The cash bag is full
BAG_FULLOK	Previous bag full condition has been cleared

 
Envoy Support for the CS1one Deposit Device

 
Enable Device

Enable device can be used to toggle the ready state of the CS1one Deposit Device. When disabled, the feeder will not engage when notes are presented device. The unit is available to queries and commands from the Envoy platform regardless of the ready state.
Software Reset

The Software Reset function sends a reset command to the device which clears the unit of all errors. Upon command completion a reply code will be returned.

NOTE: Resetting the device will not clear a Ôag FullÕstatus.
Hardware Reset

Instructs the device to perform a hardware reset and will return the device status code when communication between the host and the device has been restored.
Reboot

Instructs the device to perform a full reboot. Rebooting the device behaves as if a power cycle has occurred.
Get LAN IP

The device will return the current Internet Protocol version 4 (IPv4) address.
Set LAN IP

Allows the Internet Protocol version 4 (IPv4) address of the device to be changed.
Get LAN Mask

The device will return the currently configured IPv4 subnet mask.
Set LAN Mask

Allows the IPv4 subnet mask to be changed.
Get LAN Port

The device returns the current Internet Protocol version 4 (IPv4) port number.

NOTE: At this time, the port number cannot be configured.
Status

The Status function will return a reply code of the current device status. 
Validation Template

Validation Template will return a 30 character string representing the version of the current cash validation template.

ex.: ÒS1 ver. 1.0 USD-01.00 Ó
Get Currency Code

Get Currency Code returns the currency code that the CS1one is currently set to. For example, if the CS1one is setup to accept Euros, then the output of EnvoyHome will be, ÒUR.Ó
    EUR - Euros
    USD - US Dollars
    GBP - British Pounds

Set Currency Code

The Sets Currency Code function can be used to set or get the currency code of the CS1one for bill acceptance. The device supports the following currency codes:

    EUR for Euros
    USD for US Dollars
    GBP for British Pounds

NOTE: If a code is used that is not currently supported by Envoy is used with setCurrencyCode() the API will return an APICommandException: BADPARAMETER
Get Note Count

Get Note Count retrieves the number of banknotes for each denomination that the device has processed and returns them in a MoneyGram.
Get Last Note

The Get Last Note function will return the denomination of the last note processed.

Additionally, the response will include a boolean which indicates whether the denomination is newly processed since the last call to GetLastNote.

Example: When a USD$1 banknote is initially processed the GetLastNote function will report the note and indicate it is a new note. Calling GetLastNote again immediately will return USD$1 and indicate a non-new note.

NOTE: If no notes have yet been processed, the getLastNote() function will return an APICommandException: BADSTATE.
Seal Bag

The Seal Bag function welds the notes bag within the device closed.
Set Password

The Set Password function can be used to reset the password for one of the four CS1one roles.

    Administrator
    User
    CIT
    Master

In addition to a new password, the existing password must be provided to authenticate the change.

NOTE: Valid passwords must be exactly five digits in length.
Open Safe Door

This function instructs the unit to open the door to the safe.
Set Safe Open Delay

The Set Safe Open Delay function controls how long the device will wait after receiving the Open Safe Door command before unlocking the door.

Valid values are 0 through 30 minutes. A zero (0) value means it will open immediately upon receipt of the door open command.
Set Timeout

Set Timeout can be used to set the device timeout parameter. This timeout is the duration of inactivity before the device will automatically return to a disabled state.

Valid values are 0 through 999 seconds. A zero (0) value will allow the CS1one to be enabled indefinitely.
 
Envoy Device Event Summary: CS1one
STACKED	Note has been stored into device storage
DOOR_OPEN	The safe door has been opened
DOOR_CLOSED	The safe door has been closed
BAG_FULL	The cash bag is full
BAG_FULLOK	Previous bag full condition has been cleared
BAG_MISSING	Cash bag not detected
BAG_READY	Cash bag is ready
JAMMED	The machine is jammed
JAM_CLEARED	Previous jam now cleared

 
EnvoyAPI Support for JavaPOS API

JavaPOS API support has been introduced with Envoy 1.3 which permits interaction with Envoy supported devices using existing applications developed to the JavaPOS control class. Initializing the JavaPOS service within Envoy will establish the necessary service to interface all supported functions.

For additional information regarding the UPOS and JavaPOS APIs, please refer to the National Retail Federation and JavaPOS websites respectively.

By default the JavaPOS API library file (LibJavaPOSAPI.jar) is located in the Envoy installation directory. The JavaPOS Library Support table provides a list of device and feature support from within the Envoy API.
Platform Use
Documentation

The Envoy API is utilized through the application of one or more classes to interface with a specified device. The documentation that outlines the structure and functional expectations of each class is included within the Envoy API install package.

The JavaDoc package is deployed to the same directory used during Envoy install.

    Windows: C:\Program Files (x86)\ARCA\Envoy\EnvoyAPI_JavaDoc.jar
    Linux: /opt/ARCA/envoy/EnvoyAPI_JavaDoc.jar

Error Codes and Error Handling

There are three categories of errors:

    RemoteException ÐThe Remote Method Invocation (RMI) has experienced a connection error and your application is no longer connected with Envoy. You may want to review the host PC system configuration and Envoy Service for the cause of the issue.
    APICommandException ÐEnvoy has experienced a software error. Two potential causes for this error could be bad parameters, or a failure to read or write to a device. See EnvoyErrorEnum for more details on the kinds of errors that APICommandException can represent.
    Device Specific Error ÐEnvoy can report device-specific errors that are unique for each device type. For F53 specific errors, please consult the F53ErrorEnum within the Envoy JavaDoc support documentation.

Addendum: SCR Edge Conditions in EnvoyHome

The following table reflects the operating conditions and behaviors of a 2-spool MEI SCR Recycler as observed within EnvoyHome.
Example A
Operation Description

Dispense quantity of zero for recycle-enabled denomination with an inventory greater than zero.
Example Conditions

USD$1 and USD$20 are enabled for recycling and there are currently two $1 notes and one $20 spooled for recycling. A dispense command is issued for zero $1 notes.
Output within EnvoyHome

API: BADPARAMETER: Cannot dispense that quantity: 0
Example B
Operation Description

Dispense quantity of non-zero for denomination that is not recycle-enabled.
Example Conditions

USD$1 and USD$20 are enabled for recycling. A dispense command is issued for a single $2 note.
Output within EnvoyHome

API: BADPARAMETER: Cannot dispense that denomination: USD$2
Example C
Operation Description

Float quantity of zero for recycle-enabled denomination with an inventory greater than zero.
Example Conditions

USD$1 and USD$20 are enabled for recycling. There are currently two $1 notes and one $20 note spooled for recycling. A float command is issued for zero $1 notes.
Output within EnvoyHome

API: BADPARAMETER: Cannot float that quantity: 0
Example D
Operation Description

Float quantity of non-zero for a denomination not recycle-enabled.
Example Conditions

USD$1 and USD$20 are enabled for recycling. There are currently two $1 notes and one $20 note spooled for recycling. A float command is issued for a single $5 note.
Output within EnvoyHome

API: BADPARAMETER: Cannot float that denomination: USD$5

