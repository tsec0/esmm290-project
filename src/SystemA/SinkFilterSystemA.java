/******************************************************************************************************************
 * File:SinkFilterSystemA.java
 * Course: 17655
 * Project: Assignment 1
 * Copyright: Copyright (c) 2003 Carnegie Mellon University
 * Versions:
 *	1.0 November 2008 - Sample Pipe and Filter code (ajl).
 *
 * Description:
 *
 * This class serves as an example for using the SinkFilterTemplate for creating a sink filter. This particular
 * filter reads some input from the filter's input port and does the following:
 *
 *	1) It parses the input stream and "decommutates" the measurement ID
 *	2) It parses the input steam for measurments and "decommutates" measurements, storing the bits in a long word.
 *
 * This filter illustrates how to convert the byte stream data from the upstream filterinto useable data found in
 * the stream: namely time (long type) and measurements (double type).
 *
 *
 * Parameters: 	None
 *
 * Internal Methods: None
 *
 ******************************************************************************************************************/
package SystemA;

import java.io.FileWriter;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SinkFilterSystemA extends FilterExtension {
    @Override
    public void run() {
        /************************************************************************************
         *	java.util Calendar class is used for timeStamp
         * 	TimeStampFormat is used to format the time value so that it can be read
         *************************************************************************************/
        Calendar TimeStamp = Calendar.getInstance();
        SimpleDateFormat TimeStampFormat = new SimpleDateFormat("yyyy:dd:HH:mm:ss");

        DecimalFormat df = new DecimalFormat("0.00000");
        df.setRoundingMode(RoundingMode.HALF_UP);

        StringBuilder outputLine = new StringBuilder();

        boolean readTemperature = false;
        boolean readAltitude = false;
        boolean appendFile = false;

        /*********************************************************************************
         *	Classification of the results after processing - Time, Altitude, Temperature
         ********************************************************************************/

        System.out.print("\n" + this.getName() + " Sink data reading " + "\n");
        outputLine.append("Time: ")
                .append("\t\t\t\t\t\t\t\t")
                .append("Altitude (m): ")
                .append("\t\t\t\t")
                .append("Temperature (C): ")
                .append("\t\t\t\t\t");
        outputLine.append("\n");
        while (true) {
            try {
                Id idData = readId(this.InputReadPortA);
                MeasurementDataHelper measurementData = readMeasurement(this.InputReadPortA);

                /****************************************************************************
                 // Here we look for an ID of 0 which indicates this is a time measurement.
                 // Every frame begins with an ID of 0, followed by a time stamp which correlates
                 // to the time that each proceeding measurement was recorded. Time is stored
                 // in milliseconds since Epoch. This allows us to use Java's calendar class to
                 // retrieve time and also use text format classes to format the output into
                 // a form humans can read. So this provides great flexibility in terms of
                 // dealing with time arithmetically or for string display purposes. This is
                 // illustrated below.
                 ****************************************************************************/
                String formattedTime;
                String formattedTemp;
                String formattedAltitude;
                if (idData.id == Ids.Time.ordinal()) {
                    TimeStamp.setTimeInMillis(measurementData.measurement);
                    formattedTime = TimeStampFormat.format(TimeStamp.getTime());
                    outputLine.append(formattedTime).append("\t\t\t\t\t");
                } // if

                /****************************************************************************
                 // Here we pick up a measurement (ID = 4 in this case), but you can pick up
                 // any measurement you want to. All measurements in the stream are
                 // decommutated by this class. Note that all data measurements are double types
                 // This illustrates how to convert the bits read from the stream into a double
                 // type. It's pretty simple using Double.longBitsToDouble(long value). So here
                 // we print the time stamp and the data associated with the ID we are interested
                 // in.
                 ****************************************************************************/

                else if (idData.id == Ids.Temperature.ordinal()) {
                    double temp = Double.longBitsToDouble(measurementData.measurement);
                    formattedTemp = df.format(temp);
                    outputLine.append(formattedTemp).append("\t\t\t\t\t");
                    readTemperature = true;
                } // if

                else if (idData.id == Ids.Altitude.ordinal()) {
                    double altitude = Double.longBitsToDouble(measurementData.measurement);
                    formattedAltitude = df.format(altitude);
                    outputLine.append(formattedAltitude).append("\t\t\t\t\t");
                    readAltitude = true;
                }

                if (readTemperature && readAltitude) {
                    readTemperature = false;
                    readAltitude = false;
                    outputLine.append("\n");
                    writeOutputToFile(outputLine, appendFile);
                    outputLine = new StringBuilder();
                    appendFile = true;
                }
            } // try

            /*******************************************************************************
             *	The EndOfStreamException below is thrown when you reach end of the input
             *	stream (duh). At this point, the filter ports are closed and a message is
             *	written letting the user know what is going on.
             ********************************************************************************/
            catch (EndOfStreamException | IOException e) {
                ClosePorts();
                System.out.print("\n" + this.getName() +
                        " Sink Exiting; bytes read: " + bytesRead +
                        " Duration in milliseconds: " + "\n");
                break;
            } // catch
        } // while
    } // run

    private void writeOutputToFile(StringBuilder outputLine, boolean append) {
        FileWriter writer = null;
        try {
            writer = new FileWriter("OutputA.txt", append);
            writer.append(outputLine);
        } catch (IOException iox) {
            System.err.println("IO Error in SinkFilterSystemA: " + iox.getMessage());
        }
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException iox) {
            System.err.println("IO Error in SinkFilterSystemA: " + iox.getMessage());
        }
    }
}
