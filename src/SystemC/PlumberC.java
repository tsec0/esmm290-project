package SystemC;

import SystemA.FilterFramework;
import SystemB.PressureFilter;

public class PlumberC {
    public static void main(String[] argv) {
        /****************************************************************************
         * Here we instantiate three filters.
         ****************************************************************************/

        // the id position of enums
        int[] ids = {
                FilterFramework.Ids.Time.ordinal(),
                FilterFramework.Ids.Temperature.ordinal(),
                FilterFramework.Ids.Altitude.ordinal(),
                FilterFramework.Ids.Pressure.ordinal(),
                FilterFramework.Ids.Attitude.ordinal()
        };
        SourceA SourceFilterA = new SourceA();
        SourceB SourceFilterB = new SourceB();
        Merge mergeFilter = new Merge(ids);
        PressureFilter FilterAsPressure = new PressureFilter(90.0d, 45.0d, ids);
        AttPsiFilter FilterAsAttPsi = new AttPsiFilter(65.0d, 10.0d, ids);
        SinkFilterSystemC FilterAsSink = new SinkFilterSystemC();

        /****************************************************************************
         * Here we connect the filters starting with the sink filter (Filter 1) which
         * we connect to Filter2 the middle filter. Then we connect Filter2 to the
         * source filter (Filter3).
         ****************************************************************************/

        mergeFilter.Connect(SourceFilterA, SourceFilterB);
        FilterAsPressure.Connect(mergeFilter);
        FilterAsAttPsi.Connect(FilterAsPressure);
        FilterAsSink.Connect(FilterAsAttPsi);

        /****************************************************************************
         * Here we start the filters up. All-in-all,... its really kind of boring.
         ****************************************************************************/

        SourceFilterA.start();
        SourceFilterB.start();
        mergeFilter.start();
        FilterAsPressure.start();
        FilterAsAttPsi.start();
        FilterAsSink.start();
    } // main
}
