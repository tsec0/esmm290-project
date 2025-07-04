/******************************************************************************************************************
 * File:SourceFilterSystemA.java
 * Course: 17655
 * Project: Assignment 1
 * Copyright: Copyright (c) 2003 Carnegie Mellon University
 * Versions:
 *	1.0 November 2008 - Sample Pipe and Filter code (ajl).
 *
 * Description:
 *
 * This class serves as an example for how to use the SourceFilterTemplate to create a source filter. This particular
 * filter is a source filter that reads some input from the FlightData.dat file and writes the bytes up stream.
 *
 * Parameters: 		None
 *
 * Internal Methods: None
 *
 ******************************************************************************************************************/
package SystemA;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;

public class SourceFilterSystemA extends FilterExtension {
    @Override
    public void run() {
        String fileName = "FlightData.dat";    // Input data file.
        try {
            /***********************************************************************************
             *	Here we open the file and write a message to the terminal.
             ***********************************************************************************/

            inputDataStream = new DataInputStream(new FileInputStream(fileName));
            System.out.println("\n" + this.getName() + " Reading source file! " + "\n");

            /***********************************************************************************
             *	Here we read the data from the file and send it out the filter's output port one
             * 	byte at a time. The loop stops when it encounters an EOF execption.
             ***********************************************************************************/

            while (true) {
                Id idData = readId(this.InputReadPortA);
                MeasurementDataHelper measurementData = readMeasurement(this.InputReadPortA);

                if (idData.id == Ids.Altitude.ordinal() ||
                        idData.id == Ids.Temperature.ordinal() ||
                        idData.id == Ids.Time.ordinal()
                ) {
                    writeId(idData.bytes);
                    writeMeasurement(measurementData.bytes);
                }
            } // while
        } //try

        /***********************************************************************************
         *	The following exception is raised when we hit the end of input file. Once we
         * 	reach this point, we close the input file, close the filter ports and exit.
         ***********************************************************************************/
        catch (EOFException err) {
            System.out.println("\n" + this.getName() + " End of file reached! ");
            try {
                inputDataStream.close();
                ClosePorts();
                System.out.println("\n" + this.getName() +
                        " Read file complete." +
                        " bytes read: " + bytesRead +
                        " bytes written: " + bytesWritten + "\n");
            }

            /***********************************************************************************
             *	The following exception is raised should we have a problem closing the file.
             ***********************************************************************************/
            catch (Exception closeErr) {
                System.out.println("\n" + this.getName() + " Problem closing input data file: " + closeErr);
            } // catch
        } // catch
        /***********************************************************************************
         *	The following exception is raised should we have a problem openinging the file.
         ***********************************************************************************/
        catch (IOException iox) {
            System.out.println("\n" + this.getName() + " Problem reading input data file: " + iox);
        } // catch
        catch (EndOfStreamException e) {
            throw new RuntimeException(e);
        }
    } // run
}
