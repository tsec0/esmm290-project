package SystemC;

import SystemA.FilterExtension;
import SystemA.Id;
import SystemA.MeasurementDataHelper;

import java.io.FileWriter;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SinkFilterSystemC extends FilterExtension {
    public void run() {
        boolean readTemperature = false;
        boolean readAltitude = false;
        boolean readPressure = false;
        boolean readAttitude = false;

        boolean appendMainOutput = false;
        boolean appendRejectedPressure = false;
        boolean appendRejectedAttitudePressure = false;

        double currentPressure = 0;
        double currentAttitude = 0;

        /************************************************************************************
         *	TimeStamp is used to compute time using java.util's Calendar class.
         * 	TimeStampFormat is used to format the time value so that it can be easily printed
         *	to the terminal.
         *************************************************************************************/
        Calendar TimeStamp = Calendar.getInstance();
        SimpleDateFormat TimeStampFormat = new SimpleDateFormat("yyyy:dd:HH:mm:ss");

        DecimalFormat df = new DecimalFormat("0.00000");
        df.setRoundingMode(RoundingMode.HALF_UP);

        StringBuilder outputLine = new StringBuilder();
        StringBuilder outputLineAttitudePressure = new StringBuilder();
        StringBuilder outputLinePressure = new StringBuilder();

        /*************************************************************
         *	First we print the table content naming (table names)
         **************************************************************/

        System.out.print("\n" + this.getName() + "::Sink Reading " + "\n");
        outputLine.append("Time: ").
                append("\t\t\t\t\t\t\t\t")
                .append("Altitude (ft): ")
                .append("\t\t\t\t")
                .append("Temperature (F): ")
                .append("\t\t\t")
                .append("Pressure (psi): ")
                .append("\t\t\t")
                .append("Attitude: ")
                .append("\t\t\t\t\t");
        outputLine.append("\n");

        outputLineAttitudePressure.append("Time: ")
                .append("\t\t\t\t\t\t\t\t")
                .append("Pressure (psi): ")
                .append("\t\t\t")
                .append("Attitude: ")
                .append("\t\t\t");
        outputLineAttitudePressure.append("\n");

        outputLinePressure.append("Time: ")
                .append("\t\t\t\t\t\t\t\t")
                .append("Pressure (psi): ")
                .append("\t\t\t");
        outputLinePressure.append("\n");

        String formattedTime = "";
        String formattedPressure = "";
        String formattedAttitude = "";

        while (true) {
            try {
                Id data = readId(this.InputReadPortA);
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

                String formattedTemp;
                String formattedAltitude;
                if (data.id == Ids.Time.ordinal()) {
                    TimeStamp.setTimeInMillis(measurementData.measurement);
                    formattedTime = TimeStampFormat.format(TimeStamp.getTime());
                    outputLine.append(formattedTime).append("\t\t\t\t\t");
                }

                /****************************************************************************
                 // Here we pick up a measurement (ID = 4 in this case), but you can pick up
                 // any measurement you want to. All measurements in the stream are
                 // decommutated by this class. Note that all data measurements are double types
                 // This illustrates how to convert the bits read from the stream into a double
                 // type. It's pretty simple using Double.longBitsToDouble(long value). So here
                 // we print the time stamp and the data associated with the ID we are interested
                 // in.
                 ****************************************************************************/

                else if (data.id == Ids.Temperature.ordinal()) {
                    double temp = Double.longBitsToDouble(measurementData.measurement);
                    formattedTemp = df.format(temp);
                    outputLine.append(formattedTemp).append("\t\t\t\t\t");
                    if (temp > 0 && temp < 10) {
                        outputLine.append("\t");
                    }
                    readTemperature = true;
                }

                else if (data.id == Ids.Altitude.ordinal()) {
                    double altitude = Double.longBitsToDouble(measurementData.measurement);
                    formattedAltitude = df.format(altitude);
                    outputLine.append(formattedAltitude).append("\t\t\t\t\t");
                    readAltitude = true;
                }

                else if (data.id == Ids.Pressure.ordinal()) {
                    currentPressure = Double.longBitsToDouble(measurementData.measurement);
                    readPressure = true;
                } else if (data.id == Ids.Attitude.ordinal()) {
                    currentAttitude = Double.longBitsToDouble(measurementData.measurement);
                    readAttitude = true;
                }

                if (readTemperature && readAltitude && readPressure && readAttitude) {
                    readTemperature = false;
                    readAltitude = false;
                    readPressure = false;
                    readAttitude = false;

                    if (currentAttitude < 0 && currentPressure < 0) {
                        currentAttitude = Math.abs(currentAttitude);
                        currentPressure = Math.abs(currentPressure);

                        formattedPressure = df.format(currentPressure);
                        outputLineAttitudePressure.append(formattedTime).append("\t\t\t\t\t");
                        outputLineAttitudePressure.append(formattedPressure).append("\t\t\t\t\t");
                        outputLine.append(formattedPressure).append("*").append("\t\t\t\t\t");

                        formattedAttitude = df.format(currentAttitude);
                        outputLineAttitudePressure.append(formattedAttitude);
                        outputLine.append(formattedAttitude).append("*").append("\t\t\t\t\t");
                        outputLineAttitudePressure.append("\n");
                        writeOutputToFile(outputLineAttitudePressure,
                                "OutputC-WildPointsPressureAttitude.txt",
                                appendRejectedAttitudePressure);
                        outputLineAttitudePressure = new StringBuilder();
                        appendRejectedAttitudePressure = true;
                    } else if (currentPressure < 0) {
                        currentPressure = Math.abs(currentPressure);

                        formattedPressure = df.format(currentPressure);
                        formattedAttitude = df.format(currentAttitude);

                        outputLinePressure.append(formattedTime).append("\t\t\t\t\t");
                        outputLinePressure.append(formattedPressure);
                        outputLine.append(formattedPressure).append("*").append("\t\t\t\t\t");
                        outputLine.append(formattedAttitude).append("\t\t\t\t\t");

                        outputLinePressure.append("\n");
                        writeOutputToFile(outputLinePressure,
                                "OutputC-WildPointsPressure.txt",
                                appendRejectedPressure);
                        outputLinePressure = new StringBuilder();
                        appendRejectedPressure = true;
                    } else {
                        formattedPressure = df.format(currentPressure);
                        formattedAttitude = df.format(currentAttitude);

                        outputLine.append(formattedPressure).append("\t\t\t\t\t");
                        outputLine.append(formattedAttitude).append("\t\t\t\t\t");
                    }
                    outputLine.append("\n");
                    writeOutputToFile(outputLine, "OutputC.txt", appendMainOutput);
                    appendMainOutput = true;
                    outputLine = new StringBuilder();
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
                        " Sink Exiting... " +
                        " bytes read: " + bytesRead +
                        " bytes written: " + bytesWritten + "\n");
                break;
            } // catch
        } // while
    }// run

    private void writeOutputToFile(StringBuilder outputLine, String fileName, boolean append) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(fileName, append);
            writer.write(outputLine.toString());
        } catch (IOException e) {
            System.err.println("IO Error in SinkFilter: " + e.getMessage());
        }
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            System.err.println("IO Error in SinkFilter: " + e.getMessage());
        }
    }
}
