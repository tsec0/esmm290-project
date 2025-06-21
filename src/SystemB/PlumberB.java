package SystemB;

import SystemA.AltitudeFilter;
import SystemA.FilterFramework;
import SystemA.TemperatureFilter;

public class PlumberB {
    public static void main(String[] argv) {
        /****************************************************************************
         * Here we instantiate three filters.
         ****************************************************************************/

        // the id position of enums
        int[] ids = {
                FilterFramework.Ids.Time.ordinal(),
                FilterFramework.Ids.Temperature.ordinal(),
                FilterFramework.Ids.Altitude.ordinal(),
                FilterFramework.Ids.Pressure.ordinal()
        };
        SourceFilterSystemB FilterAsSource = new SourceFilterSystemB();
        TemperatureFilter FilterAsTemperature = new TemperatureFilter();
        AltitudeFilter FilterAsAltitude = new AltitudeFilter();
        PressureFilter FilterAsPressure = new PressureFilter(80.0d, 50.0d, ids);
        SinkFilterSystemB FilterAsSink = new SinkFilterSystemB();

        /****************************************************************************
         * Here we connect the filters starting with the sink filter (Filter 1) which
         * we connect to Filter2 the middle filter. Then we connect Filter2 to the
         * source filter (Filter3).
         ****************************************************************************/

        FilterAsSink.Connect(FilterAsPressure);
        FilterAsPressure.Connect(FilterAsAltitude);
        FilterAsAltitude.Connect(FilterAsTemperature); // This essentially says, connect Filter3 input port to Filter2 output port
        FilterAsTemperature.Connect(FilterAsSource); // This essentially says, connect Filter2 input port to Filter1 output port

        /****************************************************************************
         * Here we start the filters up. All-in-all,... its really kind of boring.
         ****************************************************************************/

        FilterAsSource.start();
        FilterAsTemperature.start();
        FilterAsAltitude.start();
        FilterAsPressure.start();
        FilterAsSink.start();

    } // main
}
