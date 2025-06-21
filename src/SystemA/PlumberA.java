package SystemA;

public class PlumberA {
    public static void main(String[] argv) {
        /****************************************************************************
         * Here we instantiate three filters.
         ****************************************************************************/

        SourceFilterSystemA FilterAsSource = new SourceFilterSystemA();
        TemperatureFilter FilterAsTemperature = new TemperatureFilter();
        AltitudeFilter FilterAsAltitude = new AltitudeFilter();
        SinkFilterSystemA FilterAsSink = new SinkFilterSystemA();

        /****************************************************************************
         * Here we connect the filters starting with the sink filter (FilterAsSink) which
         * we connect to FilterAsAltitude the altitude filter. Then we connect FilterAsAltitude to the
         * temperature filter (FilterAsTemperature). Then we connect FilterAsTemperature to the
         * source filter (FilterAsSource)
         ****************************************************************************/

        FilterAsSink.Connect(FilterAsAltitude); // This essentially says, connect FilterAsSink input port to FilterAsAltitude output port
        FilterAsAltitude.Connect(FilterAsTemperature);
        FilterAsTemperature.Connect(FilterAsSource);

        /****************************************************************************
         * Here we start the filters up.
         ****************************************************************************/

        FilterAsSource.start();
        FilterAsTemperature.start();
        FilterAsAltitude.start();
        FilterAsSink.start();
    } // main
}
