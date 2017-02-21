package fi.centria.ruuvitag.activity;

import fi.centria.ruuvitag.data.RuuvitagObject;

/**
 * Created by ISOHAJA on 2.2.2017.
 */

public interface UpdatableFragment {
    public void update(RuuvitagObject beaconData);
}