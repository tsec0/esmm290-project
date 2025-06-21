package SystemC;

import SystemA.FilterExtension;
import SystemA.Id;
import SystemA.MeasurementDataHelper;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;

public class SourceB extends FilterExtension {
    public void run() {
        String fileName = "SubSetB.dat";    // Input data file.
        try {
            /***********************************************************************************
             *	Here we open the file and write a message to the terminal.
             ***********************************************************************************/

            inputDataStream = new DataInputStream(new FileInputStream(fileName));
            System.out.println("\n" + this.getName() + " Reading Source B file..." + "\n");

            /***********************************************************************************
             *	Here we read the data from the file and send it out the filter's output port one
             * 	byte at a time. The loop stops when it encounters an EOFExecption.
             ***********************************************************************************/

            while (true) {
                // input port not actually used since this is source filter
                Id idData = readId(this.InputReadPortA);
                MeasurementDataHelper measurementData = readMeasurement(this.InputReadPortA);

                if (idData.id != Ids.Velocity.ordinal()) {
                    writeId(idData.bytes);
                    writeMeasurement(measurementData.bytes);
                }
            } // while
        } //try
        catch (EOFException eofex) {
            System.out.println("\n" + this.getName() + " End of file reached ");
            try {
                inputDataStream.close();
                ClosePorts();
                System.out.println("\n" + this.getName() +
                        "::Read file complete, bytes read::" + bytesRead +
                        " bytes written: " + bytesWritten + "\n");
                /***********************************************************************************
                 *	The following exception is raised should we have a problem closing the file.
                 ***********************************************************************************/
            } catch (Exception closeErr) {
                System.out.println("\n" + this.getName() + " Problem closing input data file: " + closeErr.getMessage());
            } // catch
        } // catch
        /***********************************************************************************
         *	The following exception is raised should we have a problem opening the file.
         ***********************************************************************************/
        catch (IOException iox) {
            System.out.println("\n" + this.getName() + " Problem reading input data file: " + iox.getMessage());
        } // catch
        catch (EndOfStreamException e) {
            throw new RuntimeException(e);
        }
    } // run
}
