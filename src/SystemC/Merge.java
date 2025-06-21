package SystemC;

import SystemA.FilterExtension;
import SystemA.FrameHelper;
import java.io.IOException;

public class Merge extends FilterExtension {
    long millisecondsA = 0;
    long millisecondsB = 0;
    boolean hasStreamClosed = false;

    FrameHelper frameA;
    FrameHelper frameB;

    public Merge(int[] ids) {
        super(ids);
    }

    public void run() {
        // Next we write a message to the terminal to let the world know we are alive...

        System.out.print("\n" + this.getName() + " Merging... " + "\n");

        while (true) {
            try {
                if (!hasStreamClosed) {
                    if (millisecondsA == 0) {
                        frameA = this.readFrame(this.InputReadPortA);
                    }
                    if (millisecondsB == 0) {
                        frameB = this.readFrame(this.InputReadPortB);
                    }

                    millisecondsA = frameA.getTimestampInMillis();
                    millisecondsB = frameB.getTimestampInMillis();

                    if (millisecondsA >= millisecondsB) {
                        writeFrame(frameB);
                        millisecondsB = 0;
                    } else {
                        writeFrame(frameA);
                        millisecondsA = 0;
                    }
                } else {
                    if (millisecondsA == 0) { // stream A has finished first just read through rest of stream B
                        frameB = this.readFrame(this.InputReadPortB);
                        writeFrame(frameB);
                    } else { // stream B has finished first, read rest of stream A
                        frameA = this.readFrame(this.InputReadPortA);
                        writeFrame(frameA);
                    }
                }
            } // try

            catch (EndOfStreamException | IOException e) {
                if (!hasStreamClosed) {
                    hasStreamClosed = true;
                    if (millisecondsA == 0) {
                        try {
                            writeFrame(frameB);
                        } catch (IOException iox) {
                            throw new RuntimeException(iox);
                        }
                    } else {
                        try {
                            writeFrame(frameA);
                        } catch (IOException iox) {
                            throw new RuntimeException(iox);
                        }
                    }
                } else {
                    ClosePorts();
                    System.out.print("\n" + this.getName() +
                            " Merge Exiting..." +
                            " bytes read: " + bytesRead +
                            " bytes written: " + bytesWritten +"\n");
                    break;
                }
            } // catch
        }// while
    } // run
}
